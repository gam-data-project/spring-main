package org.example.service.category;

import lombok.RequiredArgsConstructor;
import org.example.dto.category.request.CategorySearchRequestDto;
import org.example.dto.category.response.CategoryListItemDto;
import org.example.dto.category.response.CategoryOptionDto;
import org.example.dto.category.response.CategoryPageResponseDto;
import org.example.repository.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 카테고리 셀렉트 박스 옵션 조회 비즈니스 서비스.
 */
@Service
@RequiredArgsConstructor
public class CategoryReadService {
    private final CategoryMapper categoryMapper;

    /**
     * 대분류 유니크 목록을 조회한다.
     *
     * @return 대분류 옵션 리스트
     */
    public List<CategoryOptionDto> getLargeCategories() {
        return categoryMapper.selectDistinctLargeCategories();
    }

    /**
     * 대분류 값으로 중분류 유니크 목록을 조회한다.
     *
     * @param largeCategory 대분류
     * @return 중분류 옵션 리스트 (대분류 미입력 시 빈 리스트)
     */
    public List<CategoryOptionDto> getMediumCategories(String largeCategory) {
        if (largeCategory == null || largeCategory.isBlank()) {
            return Collections.emptyList();
        }
        return categoryMapper.selectDistinctMediumCategoriesByLarge(largeCategory.trim());
    }

    /**
     * 대분류+중분류 값으로 소분류 유니크 목록을 조회한다.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @return 소분류 옵션 리스트 (필수값 미입력 시 빈 리스트)
     */
    public List<CategoryOptionDto> getSmallCategories(String largeCategory, String mediumCategory) {
        if (largeCategory == null || largeCategory.isBlank()
                || mediumCategory == null || mediumCategory.isBlank()) {
            return Collections.emptyList();
        }
        return categoryMapper.selectDistinctSmallCategoriesByLargeMedium(
                largeCategory.trim(),
                mediumCategory.trim()
        );
    }


    /**
     * 카테고리 목록 페이징 조회.
     *
     * @param request 조회 조건/페이지 정보
     * @return 페이징 응답 DTO
     */
    public CategoryPageResponseDto getCategoryPage(CategorySearchRequestDto request) {
        CategorySearchRequestDto normalized = normalizeRequest(request);

        long totalCount = categoryMapper.countCategoryPage(normalized);
        List<CategoryListItemDto> items = (totalCount > 0)
                ? categoryMapper.selectCategoryPage(normalized)
                : Collections.emptyList();

        long totalPages = calculateTotalPages(totalCount, normalized.getLimit());

        return CategoryPageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    /**
     * 요청값을 정규화한다.
     * - null/blank/전체/ALL -> ALL
     * - page/size 범위 보정
     */
    private CategorySearchRequestDto normalizeRequest(CategorySearchRequestDto request) {
        CategorySearchRequestDto src = (request == null) ? new CategorySearchRequestDto() : request;

        return CategorySearchRequestDto.builder()
                .largeCategory(normalizeCategoryFilter(src.getLargeCategory()))
                .mediumCategory(normalizeCategoryFilter(src.getMediumCategory()))
                .smallCategory(normalizeCategoryFilter(src.getSmallCategory()))
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault())
                .build();
    }

    /**
     * 카테고리 필터 문자열을 ALL 규칙으로 정규화한다.
     */
    private String normalizeCategoryFilter(String value) {
        if (value == null) return "ALL";
        String v = value.trim();
        if (v.isEmpty()) return "ALL";
        if ("전체".equals(v)) return "ALL";
        if ("ALL".equalsIgnoreCase(v)) return "ALL";
        return v;
    }

    /**
     * 전체 페이지 수를 계산한다.
     */
    private long calculateTotalPages(long totalCount, int size) {
        if (totalCount == 0) return 0;
        return (totalCount + size - 1) / size;
    }





}
