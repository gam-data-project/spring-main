package org.example.service.settlementDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.settlementDetail.request.SettlementDetailRequestDto;
import org.example.dto.settlementDetail.response.SettlementDetailOptionDto;
import org.example.dto.settlementDetail.response.SettlementDetailResponseDto;
import org.example.dto.settlementDetail.response.SettlementDetailRowDto;
import org.example.repository.SettlementDetailMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementDetailReadService {
    private static final String ALL = "ALL";

    private final SettlementDetailMapper settlementDetailMapper;

    /**
     * 정산 상세조회 화면의 대분류 옵션 목록을 조회한다.
     *
     * @return 대분류 셀렉트박스 옵션 목록
     */
    public List<SettlementDetailOptionDto> getLargeCategories() {
        return settlementDetailMapper.selectDistinctLargeCategories();
    }

    /**
     * 선택한 대분류 기준으로 중분류 옵션 목록을 조회한다.
     * 대분류가 미선택(null, blank, ALL, 전체)이면 빈 리스트를 반환한다.
     *
     * @param largeCategory 선택한 대분류
     * @return 중분류 셀렉트박스 옵션 목록
     */
    public List<SettlementDetailOptionDto> getMediumCategories(String largeCategory) {
        String large = normalizeFilter(largeCategory);
        if (isAll(large)) {
            return Collections.emptyList();
        }
        return settlementDetailMapper.selectDistinctMediumCategoriesByLarge(large);
    }

    /**
     * 선택한 대분류 + 중분류 기준으로 소분류 옵션 목록을 조회한다.
     * 대분류 또는 중분류 중 하나라도 미선택(null, blank, ALL, 전체)이면 빈 리스트를 반환한다.
     *
     * @param largeCategory 선택한 대분류
     * @param mediumCategory 선택한 중분류
     * @return 소분류 셀렉트박스 옵션 목록
     */
    public List<SettlementDetailOptionDto> getSmallCategories(
            String largeCategory,
            String mediumCategory
    ) {
        String large = normalizeFilter(largeCategory);
        String medium = normalizeFilter(mediumCategory);

        if (isAll(large) || isAll(medium)) {
            return Collections.emptyList();
        }

        return settlementDetailMapper.selectDistinctSmallCategoriesByLargeMedium(
                large,
                medium
        );
    }


    /**
     * 정산 상세조회 결과를 조회한다.
     * 상세 행 목록은 items 로 내려주고,
     * 차감 택배비 / 매출합계 / 매입합계 / 순익은 별도 필드로 계산하여 반환한다.
     *
     * @param request 조회 조건
     * @return 정산 상세조회 응답 DTO
     */
    public SettlementDetailResponseDto getSettlementDetail(SettlementDetailRequestDto request) {
        SettlementDetailRequestDto normalized = normalizeForSearch(request);
        validateDateRange(normalized);

        List<SettlementDetailRowDto> rawRows = settlementDetailMapper.selectSettlementDetailRows(normalized);

        // 상세 조회된 값들만 items 리스트로
        List<SettlementDetailRowDto> items = rawRows.stream()
                .filter(this::isDetailRow)
                .toList();
        // 합계 금액을 찾아서 Dto에
        long deductedDeliveryFee = findAmount(rawRows, "차감 택배비");
        long salesTotal = findAmount(rawRows, "매출합계");
        long purchaseTotal = findAmount(rawRows, "매입합계");
        long profit = salesTotal - deductedDeliveryFee - purchaseTotal;

        log.info(
                "[getSettlementDetail] startDate={}, endDate={}, largeCategory={}, mediumCategory={}, smallCategory={}, itemCount={}, deductedDeliveryFee={}, salesTotal={}, purchaseTotal={}, profit={}",
                normalized.getStartDate(),
                normalized.getEndDate(),
                normalized.getLargeCategory(),
                normalized.getMediumCategory(),
                normalized.getSmallCategory(),
                items.size(),
                deductedDeliveryFee,
                salesTotal,
                purchaseTotal,
                profit
        );

        return SettlementDetailResponseDto.builder()
                .items(items)
                .deductedDeliveryFee(deductedDeliveryFee)
                .salesTotal(salesTotal)
                .purchaseTotal(purchaseTotal)
                .profit(profit)
                .build();
    }


    /**
     * 조회 요청 파라미터를 검색 규칙에 맞게 정규화한다.
     * null / blank / ALL / 전체 는 ALL 로 통일하며,
     * 대분류가 ALL 이면 중분류/소분류도 ALL,
     * 중분류가 ALL 이면 소분류도 ALL 로 강제한다.
     *
     * @param request 원본 요청 DTO
     * @return 정규화된 요청 DTO
     */
    private SettlementDetailRequestDto normalizeForSearch(SettlementDetailRequestDto request) {
        SettlementDetailRequestDto src = (request == null) ? new SettlementDetailRequestDto() : request;

        String large = normalizeFilter(src.getLargeCategory());
        String medium = normalizeFilter(src.getMediumCategory());
        String small = normalizeFilter(src.getSmallCategory());

        if (isAll(large)) {
            medium = ALL;
            small = ALL;
        }

        if (isAll(medium)) {
            small = ALL;
        }

        return SettlementDetailRequestDto.builder()
                .startDate(src.getStartDate())
                .endDate(src.getEndDate())
                .largeCategory(large)
                .mediumCategory(medium)
                .smallCategory(small)
                .build();
    }


    /**
     * 조회 기간의 시작일/종료일 관계가 올바른지 검증한다.
     * 둘 다 입력된 경우 startDate 가 endDate 보다 늦으면 예외를 발생시킨다.
     *
     * @param request 정규화된 요청 DTO
     */
    private void validateDateRange(SettlementDetailRequestDto request) {
        if (request.getStartDate() != null
                && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("startDate는 endDate보다 늦을 수 없습니다.");
        }
    }

    /**
     * 조회 결과 행이 상세 행인지 판단한다.
     * dt 가 존재하면 상세 행, null 이면 요약 행으로 본다.
     *
     * @param row 조회 결과 행
     * @return 상세 행 여부
     */
    private boolean isDetailRow(SettlementDetailRowDto row) {
        return row != null && row.getDt() != null;
    }

    /**
     * 조회 결과 목록에서 지정한 구분(gb)의 금액을 찾아 반환한다.
     * 대상 행이 없으면 0을 반환한다.
     *
     * @param rows 전체 조회 결과
     * @param gb 찾을 구분값
     * @return 해당 구분의 금액
     */
    private long findAmount(List<SettlementDetailRowDto> rows, String gb) {
        return rows.stream()
                .filter(row -> gb.equals(row.getGb()))
                .map(SettlementDetailRowDto::getAmount)
                .mapToLong(this::nvl)
                .findFirst()
                .orElse(0L);
    }

    /**
     * 카테고리 필터 값을 공통 규칙에 따라 정규화한다.
     * null / blank / ALL / 전체 는 모두 ALL 로 통일한다.
     *
     * @param value 원본 필터 값
     * @return 정규화된 필터 값
     */
    private String normalizeFilter(String value) {
        if (value == null) return ALL;
        String v = value.trim();
        if (v.isEmpty()) return ALL;
        if ("전체".equals(v)) return ALL;
        if ("ALL".equalsIgnoreCase(v)) return ALL;
        return v;
    }

    /**
     * 전달받은 값이 미선택 상태(ALL)인지 판별한다.
     *
     * @param value 검사할 값
     * @return ALL 여부
     */
    private boolean isAll(String value) {
        return ALL.equalsIgnoreCase(normalizeFilter(value));
    }

    /**
     * Long 금액 값이 null 이면 0으로 치환한다.
     *
     * @param value 원본 금액
     * @return null-safe 금액
     */
    private long nvl(Long value) {
        return value == null ? 0L : value;
    }
}
