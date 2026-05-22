package org.example.service.renewal;

import lombok.RequiredArgsConstructor;
import org.example.dto.category.request.NewCategorySearchRequestDto;
import org.example.dto.category.request.NewCategoryUpsertRequestDto;
import org.example.dto.category.response.NewCategoryCommandResponseDto;
import org.example.dto.category.response.NewCategoryListItemDto;
import org.example.dto.category.response.NewCategoryOptionDto;
import org.example.dto.category.response.NewCategoryPageResponseDto;
import org.example.repository.renewal.NewCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 카테고리 셀렉트 박스 옵션 조회 비즈니스 서비스.
 */
@Service
@RequiredArgsConstructor
public class NewCategoryService {
    private final NewCategoryMapper newCategoryMapper;

    /**
     * 대분류 유니크 목록을 조회한다.
     *
     * @return 대분류 옵션 리스트
     */
    public List<NewCategoryOptionDto> getLargeCategories() {
        return newCategoryMapper.selectDistinctLargeCategories();
    }

    /**
     * 대분류 값으로 중분류 유니크 목록을 조회한다.
     *
     * @param largeCategory 대분류
     * @return 중분류 옵션 리스트 (대분류 미입력 시 빈 리스트)
     */
    public List<NewCategoryOptionDto> getMediumCategories(String largeCategory) {
        if (largeCategory == null || largeCategory.isBlank()) {
            return Collections.emptyList();
        }
        return newCategoryMapper.selectDistinctMediumCategoriesByLarge(largeCategory.trim());
    }

    /**
     * 대분류+중분류 값으로 소분류 유니크 목록을 조회한다.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @return 소분류 옵션 리스트 (필수값 미입력 시 빈 리스트)
     */
    public List<NewCategoryOptionDto> getSmallCategories(String largeCategory, String mediumCategory) {
        if (largeCategory == null || largeCategory.isBlank()
                || mediumCategory == null || mediumCategory.isBlank()) {
            return Collections.emptyList();
        }
        return newCategoryMapper.selectDistinctSmallCategoriesByLargeMedium(
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
    public NewCategoryPageResponseDto getCategoryPage(NewCategorySearchRequestDto request) {
        NewCategorySearchRequestDto normalized = normalizeRequest(request);

        long totalCount = newCategoryMapper.countCategoryPage(normalized);
        List<NewCategoryListItemDto> items = (totalCount > 0)
                ? newCategoryMapper.selectCategoryPage(normalized)
                : Collections.emptyList();

        long totalPages = calculateTotalPages(totalCount, normalized.getLimit());

        return NewCategoryPageResponseDto.builder()
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
    private NewCategorySearchRequestDto normalizeRequest(NewCategorySearchRequestDto request) {
        NewCategorySearchRequestDto src = (request == null) ? new NewCategorySearchRequestDto() : request;

        return NewCategorySearchRequestDto.builder()
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




    /**
     * 카테고리 신규 생성.
     *
     * @param request 생성 요청 DTO
     * @return 처리 결과 DTO
     */
    @Transactional
    public NewCategoryCommandResponseDto createCategory(NewCategoryUpsertRequestDto request) {
        String large = normalizeValue(request.getLargeCategory());
        String medium = normalizeValue(request.getMediumCategory());
        String small = normalizeValue(request.getSmallCategory());

        // 대/중/소 분류 값 확인(정확한 데이터 들어가 있어야함)
        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small)) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("대/중/소분류는 필수이며 '전체'는 저장할 수 없습니다.")
                    .build();
        }

        // 저장할 데이터와 동일한 카테고리 데이터가 존재하는지 확인
        int duplicated = newCategoryMapper.countByUniqueKey(large, medium, small);
        if (duplicated > 0) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("동일한 카테고리가 이미 존재합니다.")
                    .build();
        }

        // 카테고리 저장 완료 됐는지 확인 (완료되면 id 값 반환)
        int inserted = newCategoryMapper.insertCategory(large, medium, small);
        if (inserted < 1) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("저장에 실패했습니다.")
                    .build();
        }

        // 새로 생성된 카테고리의 id 값 반환하며 저장 완료 알림
        Long id = newCategoryMapper.findIdByUniqueKey(large, medium, small);

        return NewCategoryCommandResponseDto.builder()
                .success(true)
                .message("저장 완료")
                .id(id)
                .build();
    }

    /**
     * 카테고리 수정.
     *
     * @param id 수정 대상 ID
     * @param request 수정 요청 DTO
     * @return 처리 결과 DTO
     */
    @Transactional
    public NewCategoryCommandResponseDto updateCategory(Long id, NewCategoryUpsertRequestDto request) {
        // id가 유효한지 체크
        if (id == null || id < 1) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("유효하지 않은 ID입니다.")
                    .id(id)
                    .build();
        }

        String large = normalizeValue(request.getLargeCategory());
        String medium = normalizeValue(request.getMediumCategory());
        String small = normalizeValue(request.getSmallCategory());

        // 대/중/소 분류 값 확인(정확한 데이터 들어가 있어야함)
        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small)) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("대/중/소분류는 필수이며 '전체'는 수정값으로 사용할 수 없습니다.")
                    .id(id)
                    .build();
        }

        // 본인 id 제외한 중복 id 있는제 체크
        int duplicated = newCategoryMapper.countByUniqueKeyExcludingId(id, large, medium, small);
        if (duplicated > 0) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("수정 대상과 동일한 카테고리가 이미 존재합니다.")
                    .id(id)
                    .build();
        }

        // 업데이트 쿼리 수행 후 id 값 리턴
        int updated = newCategoryMapper.updateCategoryById(id, large, medium, small);
        if (updated < 1) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("수정 대상이 없거나 수정에 실패했습니다.")
                    .id(id)
                    .build();
        }

        // 수정 완료 메세지 전달
        return NewCategoryCommandResponseDto.builder()
                .success(true)
                .message("수정 완료")
                .id(id)
                .build();
    }

    /**
     * 카테고리 삭제.
     *
     * @param id 삭제 대상 ID
     * @return 처리 결과 DTO
     */
    @Transactional
    public NewCategoryCommandResponseDto deleteCategory(Long id) {
        //id 값이 유효한지 확인
        if (id == null || id < 1) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("유효하지 않은 ID입니다.")
                    .id(id)
                    .build();
        }

        // id 값으로 삭제 수행, 삭제수행: 1, 삭제미수행: 0
        int deleted = newCategoryMapper.deleteCategoryById(id);

        // 삭제 되지 않았을 경우
        if (deleted < 1) {
            return NewCategoryCommandResponseDto.builder()
                    .success(false)
                    .message("삭제 대상이 없거나 조건이 일치하지 않습니다.")
                    .id(id)
                    .build();
        }

        // 삭제되었을 경우 삭제 완료 메세지 전달
        return NewCategoryCommandResponseDto.builder()
                .success(true)
                .message("삭제 완료")
                .id(id)
                .build();
    }

    /**
     * 문자열 정규화(공백 제거).
     */
    private String normalizeValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 저장/수정 가능한 분류값인지 검증한다.
     * 빈값, ALL, 전체는 금지한다.
     */
    private boolean isValidCategoryValue(String value) {
        if (value == null || value.isBlank()) return false;
        if ("전체".equals(value)) return false;
        return !"ALL".equals(value.toUpperCase(Locale.ROOT));
    }

}
