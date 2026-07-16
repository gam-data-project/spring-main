package org.example.service.expense;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.expense.request.ExpenseSearchRequestDto;
import org.example.dto.expense.response.*;
import org.example.repository.ExpenseMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 비용 조회 셀렉트박스 옵션 비즈니스 로직.
 * 비용 목록 조회/전체조회 비즈니스 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseReadService {

    private static final String FIXED_LARGE_CATEGORY = "비용";
    private static final String ALL = "ALL";

    private final ExpenseMapper expenseMapper;


    /**
     * 대분류 옵션 반환.
     * 요구사항에 따라 항상 "비용"만 내려준다.
     */
    public List<ExpenseOptionDto> getLargeCategories() {
        return List.of(ExpenseOptionDto.builder().value(FIXED_LARGE_CATEGORY).build());
    }

    /**
     * 중분류 옵션 조회.
     * largeCategory 입력값이 무엇이든 내부적으로 "비용"으로 고정한다.
     */
    public List<ExpenseOptionDto> getMediumCategories(String largeCategory) {
        String fixedLarge = normalizeLargeCategory(largeCategory);
        if (isAllOrBlank(fixedLarge)) return Collections.emptyList();
        return expenseMapper.selectDistinctMediumCategoriesByLarge(fixedLarge);
    }

    /**
     * 소분류 옵션 조회.
     * 입력된 대분류/중분류에 맞게 소분류 조회
     * 중분류 입력이 없다면 빈 리스트 반환
     */
    public List<ExpenseOptionDto> getSmallCategories(String largeCategory, String mediumCategory) {
        String fixedLarge = normalizeLargeCategory(largeCategory);
        String normalizedMedium = normalizeFilter(mediumCategory);
        if (isAllOrBlank(mediumCategory)) return Collections.emptyList();
        return expenseMapper.selectDistinctSmallCategoriesByLargeMedium(fixedLarge, normalizedMedium);
    }

    /**
     * 유형 옵션 조회.
     * 입력된 대분류/중분류/소분류에 맞게 유형 조회
     * 입력된 중분류/소분류가 없다면 빈리스트 반환
     */
    public List<ExpenseOptionDto> getExpenseTypes(
            String largeCategory,
            String mediumCategory,
            String smallCategory
    ) {
        String fixedLarge = normalizeLargeCategory(largeCategory);
        String normalizedMedium = normalizeFilter(mediumCategory);
        String normalizedSmall = normalizeFilter(smallCategory);

        if (isAllOrBlank(normalizedMedium) || isAllOrBlank(normalizedSmall)) return Collections.emptyList();

        return expenseMapper.selectDistinctExpenseTypesByCategory(
                fixedLarge, normalizedMedium, normalizedSmall
        );
    }

    /**
     * 대분류는 고정값("비용")만 허용한다.
     */
    private String normalizeLargeCategory(String ignored) {
        return FIXED_LARGE_CATEGORY;
    }

    /**
     * null/blank/전체/ALL 값을 내부 공통 토큰 ALL로 정규화한다.
     */
    private String normalizeFilter(String value) {
        if (value == null) return ALL;
        String v = value.trim();
        if (v.isEmpty()) return ALL;
        if ("전체".equals(v)) return ALL;
        if ("ALL".equalsIgnoreCase(v)) return ALL;
        return v;
    }

    private boolean isAllOrBlank(String v) {
        if (v == null) return true;
        String s = v.trim();
        return s.isEmpty() || "전체".equals(s) || "ALL".equalsIgnoreCase(s);
    }


    /**
     * 조회 버튼용 페이징 조회.
     * - 대분류는 내부에서 항상 "비용"으로 고정한다.
     * - 중분류=ALL이면 소/유형도 ALL로 강제한다.
     * - 소분류=ALL이면 유형을 ALL로 강제한다.
     */
    public ExpensePageResponseDto getExpensePage(ExpenseSearchRequestDto request) {
        ExpenseSearchRequestDto normalized = normalizeForSearch(request);
        return fetchPage(normalized);
    }

    /**
     * 전체 조회 버튼용 페이징 조회.
     * - 필터는 모두 제거(대분류=비용, 중/소/유형=ALL, 날짜=null).
     */
    public ExpensePageResponseDto getExpenseAllPage(ExpenseSearchRequestDto request) {
        ExpenseSearchRequestDto src = (request == null) ? new ExpenseSearchRequestDto() : request;

        ExpenseSearchRequestDto normalized = ExpenseSearchRequestDto.builder()
                .largeCategory(FIXED_LARGE_CATEGORY)
                .mediumCategory(ALL)
                .smallCategory(ALL)
                .expenseType(ALL)
                .startDate(null)
                .endDate(null)
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault())
                .build();

        return fetchPage(normalized);
    }

    /**
     * 공통 페이징 조회 수행.
     */
    private ExpensePageResponseDto fetchPage(ExpenseSearchRequestDto normalized) {
        long totalCount = expenseMapper.countExpensePage(normalized);
        List<ExpenseListItemDto> items = (totalCount > 0)
                ? expenseMapper.selectExpensePage(normalized)
                : Collections.emptyList();

        long totalPages = calculateTotalPages(totalCount, normalized.getLimit());

        return ExpensePageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    /**
     * 조회 요청값 정규화.
     */
    private ExpenseSearchRequestDto normalizeForSearch(ExpenseSearchRequestDto request) {
        ExpenseSearchRequestDto src = (request == null) ? new ExpenseSearchRequestDto() : request;

        String medium = normalizeAllFilter(src.getMediumCategory());
        String small = normalizeAllFilter(src.getSmallCategory());
        String type = normalizeAllFilter(src.getExpenseType());

        // 요구사항: 중분류가 전체면 소/유형도 전체
        if (isAll(medium)) {
            small = ALL;
            type = ALL;
        }

        // 요구사항: 소분류가 전체면 유형도 전체
        if (isAll(small)) {
            type = ALL;
        }

        return ExpenseSearchRequestDto.builder()
                .largeCategory(FIXED_LARGE_CATEGORY) // 대분류 고정
                .mediumCategory(medium)
                .smallCategory(small)
                .expenseType(type)
                .startDate(src.getStartDate())
                .endDate(src.getEndDate())
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault())
                .build();
    }

    /**
     * 필터 문자열을 ALL 규칙으로 정규화한다.
     */
    private String normalizeAllFilter(String value) {
        if (value == null) return ALL;
        String v = value.trim();
        if (v.isEmpty()) return ALL;
        if ("전체".equals(v)) return ALL;
        if ("ALL".equalsIgnoreCase(v)) return ALL;
        return v;
    }

    /**
     * ALL 여부를 판별한다.
     */
    private boolean isAll(String value) {
        // 문자열 대소문자 비교 함수
        return ALL.equalsIgnoreCase(normalizeAllFilter(value));
    }

    /**
     * 전체 페이지 수를 계산한다.
     */
    private long calculateTotalPages(long totalCount, int size) {
        if (totalCount == 0) return 0;
        return (totalCount + size - 1) / size;
    }


}
