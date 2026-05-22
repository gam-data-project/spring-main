package org.example.service.category;

import lombok.RequiredArgsConstructor;
import org.example.dto.category.request.CategoryUpsertRequestDto;
import org.example.dto.category.response.CategoryCommandResponseDto;
import org.example.repository.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryManageService {

    private final CategoryMapper categoryMapper;
    /**
     * 카테고리 신규 생성.
     *
     * @param request 생성 요청 DTO
     * @return 처리 결과 DTO
     */
    @Transactional
    public CategoryCommandResponseDto createCategory(CategoryUpsertRequestDto request) {
        String large = normalizeValue(request.getLargeCategory());
        String medium = normalizeValue(request.getMediumCategory());
        String small = normalizeValue(request.getSmallCategory());

        // 대/중/소 분류 값 확인(정확한 데이터 들어가 있어야함)
        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small)) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("대/중/소분류는 필수이며 '전체'는 저장할 수 없습니다.")
                    .build();
        }

        // 저장할 데이터와 동일한 카테고리 데이터가 존재하는지 확인
        int duplicated = categoryMapper.countByUniqueKey(large, medium, small);
        if (duplicated > 0) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("동일한 카테고리가 이미 존재합니다.")
                    .build();
        }

        // 카테고리 저장 완료 됐는지 확인 (완료되면 id 값 반환)
        int inserted = categoryMapper.insertCategory(large, medium, small);
        if (inserted < 1) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("저장에 실패했습니다.")
                    .build();
        }

        // 새로 생성된 카테고리의 id 값 반환하며 저장 완료 알림
        Long id = categoryMapper.findIdByUniqueKey(large, medium, small);

        return CategoryCommandResponseDto.builder()
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
    public CategoryCommandResponseDto updateCategory(Long id, CategoryUpsertRequestDto request) {
        // id가 유효한지 체크
        if (id == null || id < 1) {
            return CategoryCommandResponseDto.builder()
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
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("대/중/소분류는 필수이며 '전체'는 수정값으로 사용할 수 없습니다.")
                    .id(id)
                    .build();
        }

        // 본인 id 제외한 중복 id 있는제 체크
        int duplicated = categoryMapper.countByUniqueKeyExcludingId(id, large, medium, small);
        if (duplicated > 0) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("수정 대상과 동일한 카테고리가 이미 존재합니다.")
                    .id(id)
                    .build();
        }

        // 업데이트 쿼리 수행 후 id 값 리턴
        int updated = categoryMapper.updateCategoryById(id, large, medium, small);
        if (updated < 1) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("수정 대상이 없거나 수정에 실패했습니다.")
                    .id(id)
                    .build();
        }

        // 수정 완료 메세지 전달
        return CategoryCommandResponseDto.builder()
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
    public CategoryCommandResponseDto deleteCategory(Long id) {
        //id 값이 유효한지 확인
        if (id == null || id < 1) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("유효하지 않은 ID입니다.")
                    .id(id)
                    .build();
        }

        // id 값으로 삭제 수행, 삭제수행: 1, 삭제미수행: 0
        int deleted = categoryMapper.deleteCategoryById(id);

        // 삭제 되지 않았을 경우
        if (deleted < 1) {
            return CategoryCommandResponseDto.builder()
                    .success(false)
                    .message("삭제 대상이 없거나 조건이 일치하지 않습니다.")
                    .id(id)
                    .build();
        }

        // 삭제되었을 경우 삭제 완료 메세지 전달
        return CategoryCommandResponseDto.builder()
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
