package org.example.service.renewal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.purchase.request.NewPurchaseExcelSearchPageRequestDto;
import org.example.dto.purchase.request.NewPurchaseProductMappingSearchPageRequestDto;
import org.example.dto.purchase.request.NewPurchaseSearchRequestDto;
import org.example.dto.purchase.request.NewPurchaseUpsertRequestDto;
import org.example.dto.purchase.response.*;
import org.example.dto.renewal.BankTransactionRowDto;
import org.example.repository.renewal.NewPurchaseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 매입 조회 셀렉트박스 옵션 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewPurchaseService {

    /** 엑셀 조회 페이지 크기 고정값 */
    private static final int EXCEL_PAGE_SIZE = 10;
    private static final String ALL = "ALL";

    private final NewPurchaseMapper newPurchaseMapper;
    private final NewExcelReadService newExcelReadService;
    private final NewExcelFilterService newExcelFilterService;

    /**
     * 대분류 옵션 조회.
     *
     * @return 대분류 옵션 리스트
     */
    public List<NewPurchaseOptionDto> getLargeCategories() {
        return newPurchaseMapper.selectDistinctLargeCategories();
    }

    /**
     * 중분류 옵션 조회.
     * largeCategory가 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 대분류
     * @return 중분류 옵션 리스트
     */
    public List<NewPurchaseOptionDto> getMediumCategories(String largeCategory) {
        String large = normalizeFilter(largeCategory);
        if (isAll(large)) {
            return Collections.emptyList();
        }
        return newPurchaseMapper.selectDistinctMediumCategoriesByLarge(large);
    }

    /**
     * 소분류 옵션 조회.
     * large/medium 중 하나라도 ALL/전체면 빈 리스트를 반환한다.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @return 소분류 옵션 리스트
     */
    public List<NewPurchaseOptionDto> getSmallCategories(String largeCategory, String mediumCategory) {
        String large = normalizeFilter(largeCategory);
        String medium = normalizeFilter(mediumCategory);

        if (isAll(large) || isAll(medium)) {
            return Collections.emptyList();
        }

        return newPurchaseMapper.selectDistinctSmallCategoriesByLargeMedium(large, medium);
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
    public List<NewPurchaseOptionDto> getSuppliers(
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

        return newPurchaseMapper.selectDistinctSuppliersByCategory(large, medium, small);
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
    public NewPurchasePageResponseDto getPurchasePage(NewPurchaseSearchRequestDto request) {
        NewPurchaseSearchRequestDto normalized = normalizeForSearch(request);

        long totalCount = newPurchaseMapper.countPurchasePage(normalized);
        List<NewPurchaseListItemDto> items = (totalCount > 0)
                ? newPurchaseMapper.selectPurchasePage(normalized)
                : Collections.emptyList();

        long totalPages = calculateTotalPages(totalCount, normalized.getLimit());

        return NewPurchasePageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    /** 필터 정규화 */
    private NewPurchaseSearchRequestDto normalizeForSearch(NewPurchaseSearchRequestDto request) {
        NewPurchaseSearchRequestDto src = (request == null) ? new NewPurchaseSearchRequestDto() : request;

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

        return NewPurchaseSearchRequestDto.builder()
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
    public NewPurchasePageResponseDto getPurchaseAllPage(NewPurchaseSearchRequestDto request) {
        NewPurchaseSearchRequestDto src = (request == null) ? new NewPurchaseSearchRequestDto() : request;

        NewPurchaseSearchRequestDto normalized = NewPurchaseSearchRequestDto.builder()
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
    private NewPurchasePageResponseDto fetchPage(NewPurchaseSearchRequestDto normalized) {
        long totalCount = newPurchaseMapper.countPurchasePage(normalized);
        List<NewPurchaseListItemDto> items = (totalCount > 0)
                ? newPurchaseMapper.selectPurchasePage(normalized)
                : Collections.emptyList();

        long totalPages = (totalCount == 0) ? 0 : (totalCount + normalized.getLimit() - 1) / normalized.getLimit();

        return NewPurchasePageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }




    /** 저장/수정 폼용 제품 옵션 조회 */
    public List<NewPurchaseOptionDto> getProducts(String largeCategory, String mediumCategory, String smallCategory) {
        String large = normalizeFilter(largeCategory);
        String medium = normalizeFilter(mediumCategory);
        String small = normalizeFilter(smallCategory);

        if (isAll(large) || isAll(medium) || isAll(small)) return Collections.emptyList();
        return newPurchaseMapper.selectDistinctProductsByCategory(large, medium, small);
    }

    /** 매입 저장 */
    @Transactional
    public NewPurchaseCommandResponseDto createPurchase(NewPurchaseUpsertRequestDto req) {
        String large = normalizeValue(req.getLargeCategory());
        String medium = normalizeValue(req.getMediumCategory());
        String small = normalizeValue(req.getSmallCategory());
        String productName = normalizeValue(req.getProductName());
        String supplier = normalizeValue(req.getSupplierName());

        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small))
            return fail("대/중/소분류는 필수이며 '전체'는 저장값으로 사용할 수 없습니다.", null);
        if (productName.isEmpty()) return fail("제품명은 필수입니다.", null);
        if (req.getPurchaseDate() == null) return fail("매입일은 필수입니다.", null);
        if (supplier.isEmpty()) return fail("매입처는 필수입니다.", null);
        if (req.getUnitCost() == null || req.getUnitCost() < 0) return fail("단가는 0 이상이어야 합니다.", null);
        if (req.getQuantity() == null || req.getQuantity() < 0) return fail("수량은 0 이상이어야 합니다.", null);
        if (req.getTotalCost() == null || req.getTotalCost() < 0) return fail("총금액은 0 이상이어야 합니다.", null);

        Long categoryId = newPurchaseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) return fail("선택한 분류 조합을 찾을 수 없습니다.", null);

        Long productId = newPurchaseMapper.selectProductIdByCategoryAndName(categoryId, productName);
        if (productId == null) return fail("선택한 분류에 해당하는 제품을 찾을 수 없습니다.", null);

        int duplicated = newPurchaseMapper.countByUniqueKey(req.getPurchaseDate(), req.getPurchaseTime(), req.getTotalCost());
        if (duplicated > 0) return fail("동일한 (매입일, 매입시간, 총금액) 데이터가 이미 존재합니다.", null);

        int inserted = newPurchaseMapper.insertPurchase(
                productId, req.getPurchaseDate(), req.getPurchaseTime(),
                req.getQuantity(), req.getUnitCost(), req.getTotalCost(), supplier
        );
        if (inserted < 1) return fail("저장에 실패했습니다.", null);

        Long id = newPurchaseMapper.findPurchaseIdByUniqueKey(req.getPurchaseDate(), req.getPurchaseTime(), req.getTotalCost());
        return success("저장 완료", id);
    }



    /** 매입 수정 */
    @Transactional
    public NewPurchaseCommandResponseDto updatePurchase(Long id, NewPurchaseUpsertRequestDto req) {
        if (id == null || id < 1) return fail("유효하지 않은 ID입니다.", id);
        if (newPurchaseMapper.existsPurchaseById(id) < 1) return fail("수정 대상이 없습니다.", id);

        String large = normalizeValue(req.getLargeCategory());
        String medium = normalizeValue(req.getMediumCategory());
        String small = normalizeValue(req.getSmallCategory());
        String productName = normalizeValue(req.getProductName());
        String supplier = normalizeValue(req.getSupplierName());

        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small))
            return fail("대/중/소분류는 필수이며 '전체'는 수정값으로 사용할 수 없습니다.", id);
        if (productName.isEmpty()) return fail("제품명은 필수입니다.", id);

        Long categoryId = newPurchaseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) return fail("선택한 분류 조합을 찾을 수 없습니다.", id);

        Long productId = newPurchaseMapper.selectProductIdByCategoryAndName(categoryId, productName);
        if (productId == null) return fail("선택한 분류에 해당하는 제품을 찾을 수 없습니다.", id);

        int duplicated = newPurchaseMapper.countByUniqueKeyExcludingId(id, req.getPurchaseDate(), req.getPurchaseTime(), req.getTotalCost());
        if (duplicated > 0) return fail("수정값과 동일한 (매입일, 매입시간, 총금액) 데이터가 이미 존재합니다.", id);

        int updated = newPurchaseMapper.updatePurchaseById(
                id, productId, req.getPurchaseDate(), req.getPurchaseTime(),
                req.getQuantity(), req.getUnitCost(), req.getTotalCost(), supplier
        );
        if (updated < 1) return fail("수정에 실패했습니다.", id);

        return success("수정 완료", id);
    }

    /** 매입 삭제 */
    @Transactional
    public NewPurchaseCommandResponseDto deletePurchase(Long id) {
        if (id == null || id < 1) return fail("유효하지 않은 ID입니다.", id);
        int deleted = newPurchaseMapper.deletePurchaseById(id);
        if (deleted < 1) return fail("삭제 대상이 없습니다.", id);
        return success("삭제 완료", id);
    }

    /** 문자열 공백 제거 */
    private String normalizeValue(String v) { return v == null ? "" : v.trim(); }

    /** 분류값 검증 (빈값/ALL/전체 금지) */
    private boolean isValidCategoryValue(String value) {
        if (value == null || value.isBlank()) return false;
        if ("전체".equals(value)) return false;
        return !"ALL".equalsIgnoreCase(value);
    }

    /** 실패 응답 생성 */
    private NewPurchaseCommandResponseDto fail(String message, Long id) {
        return NewPurchaseCommandResponseDto.builder().success(false).message(message).id(id).build();
    }

    /** 성공 응답 생성 */
    private NewPurchaseCommandResponseDto success(String message, Long id) {
        return NewPurchaseCommandResponseDto.builder().success(true).message(message).id(id).build();
    }




    public NewPurchaseExcelPageResponseDto searchAutoPage(
            MultipartFile file,
            NewPurchaseExcelSearchPageRequestDto req
    ) {
        long started = System.currentTimeMillis();
        log.info("[searchAutoPage:start] file={}, size={}, page={}, startDate={}, endDate={}, supplier={}",
                safeFileName(file), (file == null ? 0 : file.getSize()),
                req.getPageOrDefault(), req.getStartDate(), req.getEndDate(), req.getSupplierName());

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어 있습니다.");
        }

        try (InputStream is = file.getInputStream()) {
            // 1) 업로드 파일 파싱
            List<BankTransactionRowDto> parsedRows = newExcelReadService.readRows(is);
            log.info("[searchAutoPage:parsed] totalRows={}", parsedRows.size());

            // 2) 매입처 필터 (입력값 있을 때만 적용)
            List<BankTransactionRowDto> supplierFilteredRows;
            if (req.getSupplierName() != null
                    && !req.getSupplierName().trim().isEmpty()
                    && !"전체".equals(req.getSupplierName().trim())) {
                supplierFilteredRows = newExcelFilterService.filterByRecipientName(parsedRows, req.getSupplierName().trim());
            } else {
                supplierFilteredRows = parsedRows;
            }
            log.info("[searchAutoPage:supplierFiltered] rows={}", supplierFilteredRows.size());

            // 3) 날짜 필터 + 응답 DTO 매핑
            List<NewPurchaseExcelRowResponseDto> allRows = supplierFilteredRows.stream()
                    .filter(row -> matchDateRange(row.getTransactionDate(), req.getStartDate(), req.getEndDate()))
                    .map(this::toExcelRowResponse)
                    .collect(Collectors.toList());

            // 4) 페이지 응답
            NewPurchaseExcelPageResponseDto page =
                    toPage(allRows, req.getPageOrDefault(), EXCEL_PAGE_SIZE);

            log.info("[searchAutoPage:end] filteredRows={}, page={}, totalPages={}, elapsedMs={}",
                    allRows.size(), page.getPage(), page.getTotalPages(),
                    System.currentTimeMillis() - started);

            return page;
        } catch (Exception e) {
            log.error("[searchAutoPage:error] file={}, message={}", safeFileName(file), e.getMessage(), e);
            throw new RuntimeException("엑셀 조회 처리 중 오류가 발생했습니다.", e);
        }
    }


    private boolean matchDateRange(LocalDate purchaseDate, LocalDate start, LocalDate end) {
        if (log.isTraceEnabled()) {
            log.trace("[matchDateRange] purchaseDate={}, start={}, end={}", purchaseDate, start, end);
        }
        if (purchaseDate == null) return false;
        if (start != null && purchaseDate.isBefore(start)) return false;
        if (end != null && purchaseDate.isAfter(end)) return false;
        return true;
    }



    private NewPurchaseExcelPageResponseDto toPage(
            List<NewPurchaseExcelRowResponseDto> rows, int page, int size
    ) {
        log.debug("[toPage:start] rows={}, page={}, size={}", rows.size(), page, size);

        int totalCount = rows.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int safePage = totalPages == 0 ? 1 : Math.min(page, totalPages);
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, totalCount);

        List<NewPurchaseExcelRowResponseDto> items =
                (from >= totalCount) ? List.of() : rows.subList(from, to);

        log.debug("[toPage:end] safePage={}, from={}, to={}, itemCount={}",
                safePage, from, to, items.size());

        return NewPurchaseExcelPageResponseDto.builder()
                .page(safePage)
                .size(size)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    private String safeFileName(MultipartFile file) {
        return file == null ? "null" : file.getOriginalFilename();
    }



    /**
     * 은행 거래 행 DTO를 엑셀 조회 응답 DTO로 변환한다.
     *
     * @param row 파싱된 은행 거래 행
     * @return 화면 응답용 행 DTO
     */
    private NewPurchaseExcelRowResponseDto toExcelRowResponse(BankTransactionRowDto row) {
        if (row == null) {
            return null;
        }

        return NewPurchaseExcelRowResponseDto.builder()
                .purchaseDate(row.getTransactionDate())
                .purchaseTime(row.getTransactionTime())
                .supplierName(row.getRecipientName())
                .totalCost(row.getWithdrawAmount())
                .build();
    }


    /**
     * 제품 매핑 조회(발생일 + 키워드) 페이지 처리
     *
     * @param request 조회 요청
     * @return 페이징 응답
     */
    public NewPurchaseProductMappingPageResponseDto getProductMappingPage(
            NewPurchaseProductMappingSearchPageRequestDto request
    ) {
        NewPurchaseProductMappingSearchPageRequestDto normalized = normalizeProductMappingRequest(request);

        if (normalized.getOccurredDate() == null) {
            throw new IllegalArgumentException("발생일은 필수입니다.");
        }

        long totalCount = newPurchaseMapper.countProductMappingPage(normalized);
        List<NewPurchaseProductMappingListItemDto> items = (totalCount > 0)
                ? newPurchaseMapper.selectProductMappingPage(normalized)
                : Collections.emptyList();

        long totalPages = (totalCount == 0) ? 0 : (totalCount + normalized.getLimit() - 1) / normalized.getLimit();

        return NewPurchaseProductMappingPageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    /**
     * 제품 매핑 조회 요청값 정규화
     *
     * @param request 원본 요청
     * @return 정규화된 요청
     */
    private NewPurchaseProductMappingSearchPageRequestDto normalizeProductMappingRequest(
            NewPurchaseProductMappingSearchPageRequestDto request
    ) {
        NewPurchaseProductMappingSearchPageRequestDto src =
                (request == null) ? new NewPurchaseProductMappingSearchPageRequestDto() : request;

        String keyword = (src.getKeyword() == null) ? null : src.getKeyword().trim();
        if (keyword != null && keyword.isEmpty()) keyword = null;

        return NewPurchaseProductMappingSearchPageRequestDto.builder()
                .occurredDate(src.getOccurredDate())
                .keyword(keyword)
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault())
                .build();
    }

}
