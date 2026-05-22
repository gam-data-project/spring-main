package org.example.controller.renewal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.expense.request.NewExpenseExcelSearchPageRequestDto;
import org.example.dto.expense.request.NewExpenseSearchRequestDto;
import org.example.dto.expense.request.NewExpenseUpsertRequestDto;
import org.example.dto.expense.response.NewExpenseCommandResponseDto;
import org.example.dto.expense.response.NewExpenseExcelPageResponseDto;
import org.example.dto.expense.response.NewExpenseOptionDto;
import org.example.dto.expense.response.NewExpensePageResponseDto;
import org.example.dto.log.ApiResponseLogDto;
import org.example.service.renewal.NewExpenseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 비용 조회 화면의 대/중/소분류 및 유형 셀렉트박스 옵션 API.
 */
@RestController
@RequestMapping("/renewal/expense")
@RequiredArgsConstructor
@Slf4j
public class NewExpenseController {
    private final NewExpenseService newExpenseService;

    /**
     * 대분류 옵션 조회.
     * 조건: 항상 "비용" 1개만 반환한다.
     */
    @GetMapping("/options/large")
    public ResponseEntity<List<NewExpenseOptionDto>> getLargeCategories() {
        return ResponseEntity.ok(newExpenseService.getLargeCategories());
    }

    /**
     * 중분류 옵션 조회.
     * 조건: 대분류가 "비용"인 중분류만 반환한다.
     */
    @GetMapping("/options/medium")
    public ResponseEntity<List<NewExpenseOptionDto>> getMediumCategories(
            @RequestParam(required = false, defaultValue = "비용") String largeCategory
    ) {
        return ResponseEntity.ok(newExpenseService.getMediumCategories(largeCategory));
    }

    /**
     * 소분류 옵션 조회.
     * 조건: 대분류=비용 + 선택한 중분류 기준으로 반환한다.
     * 중분류가 전체(ALL/전체)면 소분류 전체 반환.
     */
    @GetMapping("/options/small")
    public ResponseEntity<List<NewExpenseOptionDto>> getSmallCategories(
            @RequestParam(required = false, defaultValue = "비용") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory
    ) {
        return ResponseEntity.ok(newExpenseService.getSmallCategories(largeCategory, mediumCategory));
    }

    /**
     * 유형 옵션 조회.
     * 조건: 대/중/소분류 필터에 해당하는 expense_type DISTINCT 반환.
     * 중/소분류가 전체(ALL/전체)면 해당 조건 제외.
     */
    @GetMapping("/options/types")
    public ResponseEntity<List<NewExpenseOptionDto>> getExpenseTypes(
            @RequestParam(required = false, defaultValue = "비용") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory,
            @RequestParam(required = false, defaultValue = "ALL") String smallCategory
    ) {
        return ResponseEntity.ok(
                newExpenseService.getExpenseTypes(largeCategory, mediumCategory, smallCategory)
        );
    }

    /**
     * 비용 조건 조회(조회 버튼).
     *
     * @param request 대/중/소/유형/기간 + 페이지 정보
     * @return 페이징 결과
     */
    @GetMapping("/page")
    public ResponseEntity<NewExpensePageResponseDto> getExpensePage(
            @ModelAttribute NewExpenseSearchRequestDto request
    ) {
        return ResponseEntity.ok(newExpenseService.getExpensePage(request));
    }

    /**
     * 비용 전체 조회(전체 조회 버튼).
     * 필터 파라미터 없이 호출 가능하며, page/size는 선택값이다.
     *
     * @param request 페이지 정보(없으면 기본 page=1,size=10)
     * @return 페이징 결과
     */
    @GetMapping("/page/all")
    public ResponseEntity<NewExpensePageResponseDto> getExpenseAllPage(
            @ModelAttribute NewExpenseSearchRequestDto request
    ) {
        return ResponseEntity.ok(newExpenseService.getExpenseAllPage(request));
    }


    /**
     * 비용 신규 등록.
     *
     * @param request 생성 요청 본문
     * @return 처리 결과
     */
    @PostMapping
    public ResponseEntity<NewExpenseCommandResponseDto> createExpense(
            @RequestBody NewExpenseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(newExpenseService.createExpense(request));
    }

    /**
     * 비용 수정.
     *
     * @param id 수정 대상 ID
     * @param request 수정 요청 본문
     * @return 처리 결과
     */
    @PutMapping("/{id}")
    public ResponseEntity<NewExpenseCommandResponseDto> updateExpense(
            @PathVariable Long id,
            @RequestBody NewExpenseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(newExpenseService.updateExpense(id, request));
    }

    /**
     * 비용 삭제.
     *
     * @param id 삭제 대상 ID
     * @return 처리 결과
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<NewExpenseCommandResponseDto> deleteExpense(@PathVariable Long id) {
        return ResponseEntity.ok(newExpenseService.deleteExpense(id));
    }

    /**
     * 엑셀 업로드 기반 매입 조회(페이지)
     * - 기존 auto-search 파싱/필터 로직 재사용
     *
     * @param file 업로드 엑셀 파일
     * @param req 조회 조건 + 페이지
     * @return 페이지 응답
     */
    @PostMapping(value = "/excel-search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewExpenseExcelPageResponseDto excelSearchPage(
            @RequestPart("file") MultipartFile file,
            @ModelAttribute NewExpenseExcelSearchPageRequestDto req
    ) {
        NewExpenseExcelPageResponseDto result = newExpenseService.searchAutoPage(file, req);
        ApiResponseLogDto<NewExpenseExcelPageResponseDto> logDto =
                ApiResponseLogDto.<NewExpenseExcelPageResponseDto>builder()
                        .success(true)
                        .code("OK")
                        .message("excel-search completed")
                        .data(result) // 너무 길면 null로 바꿔도 됨
                        .timestamp(LocalDateTime.now())
                        .build();

        log.info("[excelSearchPage] {}", logDto);
        return result;
    }
}
