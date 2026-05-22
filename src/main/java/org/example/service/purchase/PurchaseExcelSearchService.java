package org.example.service.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.purchase.request.PurchaseExcelSearchPageRequestDto;
import org.example.dto.purchase.response.PurchaseExcelPageResponseDto;
import org.example.dto.purchase.response.PurchaseExcelRowResponseDto;
import org.example.dto.excel.BankTransactionRowDto;
import org.example.service.excel.NewExcelFilterService;
import org.example.service.excel.NewExcelReadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseExcelSearchService {
    private static final int EXCEL_PAGE_SIZE = 10;

    private final NewExcelReadService newExcelReadService;
    private final NewExcelFilterService newExcelFilterService;

    public PurchaseExcelPageResponseDto searchAutoPage(
            MultipartFile file,
            PurchaseExcelSearchPageRequestDto req
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
            List<PurchaseExcelRowResponseDto> allRows = supplierFilteredRows.stream()
                    .filter(row -> matchDateRange(row.getTransactionDate(), req.getStartDate(), req.getEndDate()))
                    .map(this::toExcelRowResponse)
                    .collect(Collectors.toList());

            // 4) 페이지 응답
            PurchaseExcelPageResponseDto page =
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



    private PurchaseExcelPageResponseDto toPage(
            List<PurchaseExcelRowResponseDto> rows, int page, int size
    ) {
        log.debug("[toPage:start] rows={}, page={}, size={}", rows.size(), page, size);

        int totalCount = rows.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int safePage = totalPages == 0 ? 1 : Math.min(page, totalPages);
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, totalCount);

        List<PurchaseExcelRowResponseDto> items =
                (from >= totalCount) ? List.of() : rows.subList(from, to);

        log.debug("[toPage:end] safePage={}, from={}, to={}, itemCount={}",
                safePage, from, to, items.size());

        return PurchaseExcelPageResponseDto.builder()
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
    private PurchaseExcelRowResponseDto toExcelRowResponse(BankTransactionRowDto row) {
        if (row == null) {
            return null;
        }

        return PurchaseExcelRowResponseDto.builder()
                .purchaseDate(row.getTransactionDate())
                .purchaseTime(row.getTransactionTime())
                .supplierName(row.getRecipientName())
                .totalCost(row.getWithdrawAmount())
                .build();
    }

}
