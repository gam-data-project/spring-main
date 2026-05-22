package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.log.ApiResponseLogDto;
import org.example.dto.purchase.request.PurchaseExcelSearchPageRequestDto;
import org.example.dto.purchase.request.PurchaseProductMappingSearchPageRequestDto;
import org.example.dto.purchase.request.PurchaseSearchRequestDto;
import org.example.dto.purchase.request.PurchaseUpsertRequestDto;
import org.example.dto.purchase.response.*;
import org.example.service.purchase.PurchaseReadService;
import org.example.service.purchase.PurchaseExcelSearchService;
import org.example.service.purchase.PurchaseManageService;
import org.example.service.purchase.PurchaseProductSearchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 매입 조회 화면의 대/중/소분류 및 매입처 옵션 API.
 */
@RestController
@RequestMapping("/renewal/purchase")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {
    private final PurchaseReadService purchaseReadService;
    private final PurchaseManageService purchaseManageService;
    private final PurchaseExcelSearchService purchaseExcelSearchService;
    private final PurchaseProductSearchService purchaseProductSearchService;

    /**
     * 대분류 옵션 조회.
     *
     * @return 대분류 옵션 리스트
     */
    @GetMapping("/options/large")
    public ResponseEntity<List<PurchaseOptionDto>> getLargeCategories() {
        return ResponseEntity.ok(purchaseReadService.getLargeCategories());
    }

    /**
     * 중분류 옵션 조회.
     * largeCategory가 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 선택된 대분류
     * @return 중분류 옵션 리스트
     */
    @GetMapping("/options/medium")
    public ResponseEntity<List<PurchaseOptionDto>> getMediumCategories(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory
    ) {
        return ResponseEntity.ok(purchaseReadService.getMediumCategories(largeCategory));
    }

    /**
     * 소분류 옵션 조회.
     * large/medium 중 하나라도 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 선택된 대분류
     * @param mediumCategory 선택된 중분류
     * @return 소분류 옵션 리스트
     */
    @GetMapping("/options/small")
    public ResponseEntity<List<PurchaseOptionDto>> getSmallCategories(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory
    ) {
        return ResponseEntity.ok(
                purchaseReadService.getSmallCategories(largeCategory, mediumCategory)
        );
    }

    /**
     * 매입처 옵션 조회.
     * large/medium/small 중 하나라도 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 선택된 대분류
     * @param mediumCategory 선택된 중분류
     * @param smallCategory 선택된 소분류
     * @return 매입처 옵션 리스트
     */
    @GetMapping("/options/supplier")
    public ResponseEntity<List<PurchaseOptionDto>> getSuppliers(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory,
            @RequestParam(required = false, defaultValue = "ALL") String smallCategory
    ) {
        return ResponseEntity.ok(
                purchaseReadService.getSuppliers(largeCategory, mediumCategory, smallCategory)
        );
    }


    /** 조회 버튼용 페이징 조회 */
    @GetMapping("/page")
    public ResponseEntity<PurchasePageResponseDto> getPurchasePage(
            @ModelAttribute PurchaseSearchRequestDto request
    ) {
        return ResponseEntity.ok(purchaseReadService.getPurchasePage(request));
    }

    /**
     * 전체 조회 버튼용 페이징 조회.
     * 분류/날짜 조건을 무시하고 전체 조인 결과를 반환한다.
     */
    @GetMapping("/page/all")
    public ResponseEntity<PurchasePageResponseDto> getPurchaseAllPage(
            @ModelAttribute PurchaseSearchRequestDto request
    ) {
        return ResponseEntity.ok(purchaseReadService.getPurchaseAllPage(request));
    }



    /** 저장/수정 폼용 제품 옵션 조회 */
    @GetMapping("/options/products")
    public ResponseEntity<List<PurchaseOptionDto>> getProducts(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory,
            @RequestParam(required = false, defaultValue = "ALL") String smallCategory
    ) {
        return ResponseEntity.ok(purchaseManageService.getProducts(largeCategory, mediumCategory, smallCategory));
    }

    /** 매입 저장 */
    @PostMapping
    public ResponseEntity<PurchaseCommandResponseDto> createPurchase(
            @RequestBody PurchaseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(purchaseManageService.createPurchase(request));
    }

    /** 매입 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseCommandResponseDto> updatePurchase(
            @PathVariable Long id,
            @RequestBody PurchaseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(purchaseManageService.updatePurchase(id, request));
    }

    /** 매입 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<PurchaseCommandResponseDto> deletePurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseManageService.deletePurchase(id));
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
    public PurchaseExcelPageResponseDto excelSearchPage(
            @RequestPart("file") MultipartFile file,
            @ModelAttribute PurchaseExcelSearchPageRequestDto req
    ) {
        PurchaseExcelPageResponseDto result = purchaseExcelSearchService.searchAutoPage(file, req);
        ApiResponseLogDto<PurchaseExcelPageResponseDto> logDto =
                ApiResponseLogDto.<PurchaseExcelPageResponseDto>builder()
                        .success(true)
                        .code("OK")
                        .message("excel-search completed")
                        .data(result) // 너무 길면 null로 바꿔도 됨
                        .timestamp(LocalDateTime.now())
                        .build();

        log.info("[excelSearchPage] {}", logDto);
        return result;
    }


    /**
     * 제품 매핑 조회(발생일 + 키워드) 페이지 API
     *
     * @param request 발생일/키워드/페이지
     * @return 페이징 조회 결과
     */
    @GetMapping("/product-mapping")
    public ResponseEntity<PurchaseProductMappingPageResponseDto> getProductMappingPage(
            @ModelAttribute PurchaseProductMappingSearchPageRequestDto request
    ) {
        return ResponseEntity.ok(purchaseProductSearchService.getProductMappingPage(request));
    }
}
