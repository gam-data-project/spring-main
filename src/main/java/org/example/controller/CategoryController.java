package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.category.request.CategorySearchRequestDto;
import org.example.dto.category.request.CategoryUpsertRequestDto;
import org.example.dto.category.response.CategoryCommandResponseDto;
import org.example.dto.category.response.CategoryOptionDto;
import org.example.dto.category.response.CategoryPageResponseDto;
import org.example.service.category.CategoryManageService;
import org.example.service.category.CategoryReadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 셀렉트 박스(대/중/소) 옵션 조회 API 컨트롤러.
 */
@RestController
@RequestMapping("/renewal/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryReadService categoryReadService;
    private final CategoryManageService categoryManageService;

    /**
     * 대분류 전체 유니크 목록을 조회한다.
     *
     * @return 대분류 옵션 리스트
     */
    @GetMapping("/large")
    public ResponseEntity<List<CategoryOptionDto>> getLargeCategories() {
        return ResponseEntity.ok(categoryReadService.getLargeCategories());
    }

    /**
     * 선택한 대분류에 속한 중분류 유니크 목록을 조회한다.
     *
     * @param largeCategory 선택된 대분류
     * @return 중분류 옵션 리스트
     */
    @GetMapping("/medium")
    public ResponseEntity<List<CategoryOptionDto>> getMediumCategories(
            @RequestParam String largeCategory
    ) {
        return ResponseEntity.ok(categoryReadService.getMediumCategories(largeCategory));
    }

    /**
     * 선택한 대분류+중분류에 속한 소분류 유니크 목록을 조회한다.
     *
     * @param largeCategory 선택된 대분류
     * @param mediumCategory 선택된 중분류
     * @return 소분류 옵션 리스트
     */
    @GetMapping("/small")
    public ResponseEntity<List<CategoryOptionDto>> getSmallCategories(
            @RequestParam String largeCategory,
            @RequestParam String mediumCategory
    ) {
        return ResponseEntity.ok(
                categoryReadService.getSmallCategories(largeCategory, mediumCategory)
        );
    }

    /**
     * 카테고리 목록 페이징 조회.
     * large/medium/small이 ALL(또는 전체)이면 해당 조건은 제외한다.
     */
    @GetMapping("/page")
    public ResponseEntity<CategoryPageResponseDto> getCategoryPage(
            @ModelAttribute CategorySearchRequestDto request
    ) {
        return ResponseEntity.ok(categoryReadService.getCategoryPage(request));
    }




    /**
     * 카테고리 신규 등록.
     *
     * @param request 생성 요청 본문
     * @return 처리 결과
     */
    @PostMapping
    public ResponseEntity<CategoryCommandResponseDto> createCategory(
            @RequestBody CategoryUpsertRequestDto request
    ) {
        return ResponseEntity.ok(categoryManageService.createCategory(request));
    }

    /**
     * 카테고리 수정.
     *
     * @param id 수정 대상 ID
     * @param request 수정 요청 본문
     * @return 처리 결과
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryCommandResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryUpsertRequestDto request
    ) {
        return ResponseEntity.ok(categoryManageService.updateCategory(id, request));
    }

    /**
     * 카테고리 삭제.
     *
     * @param id 삭제 대상 ID
     * @return 처리 결과
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryCommandResponseDto> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryManageService.deleteCategory(id));
    }
}
