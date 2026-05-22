package org.example.controller.renewal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.log.ApiResponseLogDto;
import org.example.dto.purchase.request.NewPurchaseExcelSearchPageRequestDto;
import org.example.dto.purchase.request.NewPurchaseProductMappingSearchPageRequestDto;
import org.example.dto.purchase.request.NewPurchaseSearchRequestDto;
import org.example.dto.purchase.request.NewPurchaseUpsertRequestDto;
import org.example.dto.purchase.response.*;
import org.example.service.renewal.NewPurchaseService;
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
public class NewPurchaseController {
    private final NewPurchaseService newPurchaseService;

    /**
     * 대분류 옵션 조회.
     *
     * @return 대분류 옵션 리스트
     */
    @GetMapping("/options/large")
    public ResponseEntity<List<NewPurchaseOptionDto>> getLargeCategories() {
        return ResponseEntity.ok(newPurchaseService.getLargeCategories());
    }

    /**
     * 중분류 옵션 조회.
     * largeCategory가 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 선택된 대분류
     * @return 중분류 옵션 리스트
     */
    @GetMapping("/options/medium")
    public ResponseEntity<List<NewPurchaseOptionDto>> getMediumCategories(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory
    ) {
        return ResponseEntity.ok(newPurchaseService.getMediumCategories(largeCategory));
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
    public ResponseEntity<List<NewPurchaseOptionDto>> getSmallCategories(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory
    ) {
        return ResponseEntity.ok(
                newPurchaseService.getSmallCategories(largeCategory, mediumCategory)
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
    public ResponseEntity<List<NewPurchaseOptionDto>> getSuppliers(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory,
            @RequestParam(required = false, defaultValue = "ALL") String smallCategory
    ) {
        return ResponseEntity.ok(
                newPurchaseService.getSuppliers(largeCategory, mediumCategory, smallCategory)
        );
    }


    /** 조회 버튼용 페이징 조회 */
    @GetMapping("/page")
    public ResponseEntity<NewPurchasePageResponseDto> getPurchasePage(
            @ModelAttribute NewPurchaseSearchRequestDto request
    ) {
        return ResponseEntity.ok(newPurchaseService.getPurchasePage(request));
    }

    /**
     * 전체 조회 버튼용 페이징 조회.
     * 분류/날짜 조건을 무시하고 전체 조인 결과를 반환한다.
     */
    @GetMapping("/page/all")
    public ResponseEntity<NewPurchasePageResponseDto> getPurchaseAllPage(
            @ModelAttribute NewPurchaseSearchRequestDto request
    ) {
        return ResponseEntity.ok(newPurchaseService.getPurchaseAllPage(request));
    }



    /** 저장/수정 폼용 제품 옵션 조회 */
    @GetMapping("/options/products")
    public ResponseEntity<List<NewPurchaseOptionDto>> getProducts(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory,
            @RequestParam(required = false, defaultValue = "ALL") String smallCategory
    ) {
        return ResponseEntity.ok(newPurchaseService.getProducts(largeCategory, mediumCategory, smallCategory));
    }

    /** 매입 저장 */
    @PostMapping
    public ResponseEntity<NewPurchaseCommandResponseDto> createPurchase(
            @RequestBody NewPurchaseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(newPurchaseService.createPurchase(request));
    }

    /** 매입 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<NewPurchaseCommandResponseDto> updatePurchase(
            @PathVariable Long id,
            @RequestBody NewPurchaseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(newPurchaseService.updatePurchase(id, request));
    }

    /** 매입 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<NewPurchaseCommandResponseDto> deletePurchase(@PathVariable Long id) {
        return ResponseEntity.ok(newPurchaseService.deletePurchase(id));
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
    public NewPurchaseExcelPageResponseDto excelSearchPage(
            @RequestPart("file") MultipartFile file,
            @ModelAttribute NewPurchaseExcelSearchPageRequestDto req
    ) {
        NewPurchaseExcelPageResponseDto result = newPurchaseService.searchAutoPage(file, req);
        ApiResponseLogDto<NewPurchaseExcelPageResponseDto> logDto =
                ApiResponseLogDto.<NewPurchaseExcelPageResponseDto>builder()
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
    public ResponseEntity<NewPurchaseProductMappingPageResponseDto> getProductMappingPage(
            @ModelAttribute NewPurchaseProductMappingSearchPageRequestDto request
    ) {
        return ResponseEntity.ok(newPurchaseService.getProductMappingPage(request));
    }
}
