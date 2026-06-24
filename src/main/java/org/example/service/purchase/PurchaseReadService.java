package org.example.service.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.purchase.request.PurchaseSearchRequestDto;
import org.example.dto.purchase.response.*;
import org.example.repository.PurchaseMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 매입 조회 셀렉트박스 옵션 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseReadService {


    private static final String ALL = "ALL";

    private final PurchaseMapper purchaseMapper;


    /**
     * 대분류 옵션 조회.
     *
     * @return 대분류 옵션 리스트
     */
    public List<PurchaseOptionDto> getLargeCategories() {
        return purchaseMapper.selectDistinctLargeCategories();
    }

    /**
     * 중분류 옵션 조회.
     * largeCategory가 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 대분류
     * @return 중분류 옵션 리스트
     */
    public List<PurchaseOptionDto> getMediumCategories(String largeCategory) {
        String large = normalizeFilter(largeCategory);
        if (isAll(large)) {
            return Collections.emptyList();
        }
        return purchaseMapper.selectDistinctMediumCategoriesByLarge(large);
    }

    /**
     * 소분류 옵션 조회.
     * large/medium 중 하나라도 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @return 소분류 옵션 리스트
     */
    public List<PurchaseOptionDto> getSmallCategories(String largeCategory, String mediumCategory) {
        String large = normalizeFilter(largeCategory);
        String medium = normalizeFilter(mediumCategory);

        if (isAll(large) || isAll(medium)) {
            return Collections.emptyList();
        }

        return purchaseMapper.selectDistinctSmallCategoriesByLargeMedium(large, medium);
    }

    /**
     * 매입처 옵션 조회.
     * large/medium/small 중 하나라도 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @param smallCategory 소분류
     * @return 매입처 옵션 리스트
     */
    public List<PurchaseOptionDto> getSuppliers(
            String largeCategory,
            String mediumCategory,
            String smallCategory
    ) {
        String large = normalizeFilter(largeCategory);
        String medium = normalizeFilter(mediumCategory);
        String small = normalizeFilter(smallCategory);

        if (isAll(large) || isAll(medium) || isAll(small)) {
            return Collections.emptyList();
        }

        return purchaseMapper.selectDistinctSuppliersByCategory(large, medium, small);
    }

    /**
     * null/blank/전체/ALL을 ALL로 정규화한다.
     *
     * @param value 원본 문자열
     * @return 정규화된 문자열
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
     * ALL 여부를 검사한다.
     *
     * @param value 검사 문자열
     * @return ALL 여부
     */
    private boolean isAll(String value) {
        return ALL.equalsIgnoreCase(normalizeFilter(value));
    }


    /**
     * 조회 버튼용 페이징 조회.
     * 규칙:
     * - 대분류 전체면 전체 조회
     * - 중분류 전체면 대분류만 적용
     * - 소분류 전체면 대/중분류만 적용
     * - 매입처 전체면 대/중/소분류만 적용
     */
    public PurchasePageResponseDto getPurchasePage(PurchaseSearchRequestDto request) {
        PurchaseSearchRequestDto normalized = normalizeForSearch(request);

        long totalCount = purchaseMapper.countPurchasePage(normalized);
        List<PurchaseListItemDto> items = (totalCount > 0)
                ? purchaseMapper.selectPurchasePage(normalized)
                : Collections.emptyList();

        long totalPages = calculateTotalPages(totalCount, normalized.getLimit());

        return PurchasePageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    /** 필터 정규화 */
    private PurchaseSearchRequestDto normalizeForSearch(PurchaseSearchRequestDto request) {
        PurchaseSearchRequestDto src = (request == null) ? new PurchaseSearchRequestDto() : request;

        String large = normalizeAllFilter(src.getLargeCategory());
        String medium = normalizeAllFilter(src.getMediumCategory());
        String small = normalizeAllFilter(src.getSmallCategory());
        String supplier = normalizeAllFilter(src.getSupplierName());

        if (isAll(large)) {
            medium = ALL;
            small = ALL;
            supplier = ALL;
        }

        if (isAll(medium)) {
            small = ALL;
            supplier = ALL;
        }

        if (isAll(small)) {
            supplier = ALL;
        }

        return PurchaseSearchRequestDto.builder()
                .largeCategory(large)
                .mediumCategory(medium)
                .smallCategory(small)
                .supplierName(supplier)
                .startDate(src.getStartDate())
                .endDate(src.getEndDate())
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault())
                .build();
    }

    /** ALL/전체/null/blank 정규화 */
    private String normalizeAllFilter(String value) {
        if (value == null) return ALL;
        String v = value.trim();
        if (v.isEmpty()) return ALL;
        if ("전체".equals(v)) return ALL;
        if ("ALL".equalsIgnoreCase(v)) return ALL;
        return v;
    }


    /** 전체 페이지 수 계산 */
    private long calculateTotalPages(long totalCount, int size) {
        if (totalCount == 0) return 0;
        return (totalCount + size - 1) / size;
    }


    /**
     * 전체 조회 버튼용 페이징 조회.
     * 분류는 ALL, 날짜는 null로 강제 고정한다.
     */
    public PurchasePageResponseDto getPurchaseAllPage(PurchaseSearchRequestDto request) {
        PurchaseSearchRequestDto src = (request == null) ? new PurchaseSearchRequestDto() : request;

        PurchaseSearchRequestDto normalized = PurchaseSearchRequestDto.builder()
                .largeCategory(ALL)
                .mediumCategory(ALL)
                .smallCategory(ALL)
                .supplierName(ALL)
                .startDate(null)
                .endDate(null)
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault()) // 10 고정 정책
                .build();

        return fetchPage(normalized);
    }

    /**
     * 공통 페이징 조회 처리.
     */
    private PurchasePageResponseDto fetchPage(PurchaseSearchRequestDto normalized) {
        long totalCount = purchaseMapper.countPurchasePage(normalized);
        List<PurchaseListItemDto> items = (totalCount > 0)
                ? purchaseMapper.selectPurchasePage(normalized)
                : Collections.emptyList();

        long totalPages = (totalCount == 0) ? 0 : (totalCount + normalized.getLimit() - 1) / normalized.getLimit();

        return PurchasePageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }










}
