package org.example.service.renewal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.ExpenseType;
import org.example.dto.expense.request.NewExpenseExcelSearchPageRequestDto;
import org.example.dto.expense.request.NewExpenseSearchRequestDto;
import org.example.dto.expense.request.NewExpenseUpsertRequestDto;
import org.example.dto.expense.response.*;
import org.example.dto.renewal.BankTransactionRowDto;
import org.example.repository.renewal.NewExpenseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 비용 조회 셀렉트박스 옵션 비즈니스 로직.
 * 비용 목록 조회/전체조회 비즈니스 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewExpenseService {
    /** 엑셀 조회 페이지 크기 고정값 */
    private static final int EXCEL_PAGE_SIZE = 10;
    private static final String FIXED_LARGE_CATEGORY = "비용";
    private static final String ALL = "ALL";

    private final NewExpenseMapper newExpenseMapper;
    private final NewExcelReadService newExcelReadService;
    private final NewExcelFilterService newExcelFilterService;

    /**
     * 대분류 옵션 반환.
     * 요구사항에 따라 항상 "비용"만 내려준다.
     */
    public List<NewExpenseOptionDto> getLargeCategories() {
        return List.of(NewExpenseOptionDto.builder().value(FIXED_LARGE_CATEGORY).build());
    }

    /**
     * 중분류 옵션 조회.
     * largeCategory 입력값이 무엇이든 내부적으로 "비용"으로 고정한다.
     */
    public List<NewExpenseOptionDto> getMediumCategories(String largeCategory) {
        String fixedLarge = normalizeLargeCategory(largeCategory);
        if (isAllOrBlank(fixedLarge)) return Collections.emptyList();
        return newExpenseMapper.selectDistinctMediumCategoriesByLarge(fixedLarge);
    }

    /**
     * 소분류 옵션 조회.
     * 입력된 대분류/중분류에 맞게 소분류 조회
     * 중분류 입력이 없다면 빈 리스트 반환
     */
    public List<NewExpenseOptionDto> getSmallCategories(String largeCategory, String mediumCategory) {
        String fixedLarge = normalizeLargeCategory(largeCategory);
        String normalizedMedium = normalizeFilter(mediumCategory);
        if (isAllOrBlank(mediumCategory)) return Collections.emptyList();
        return newExpenseMapper.selectDistinctSmallCategoriesByLargeMedium(fixedLarge, normalizedMedium);
    }

    /**
     * 유형 옵션 조회.
     * 입력된 대분류/중분류/소분류에 맞게 유형 조회
     * 입력된 중분류/소분류가 없다면 빈리스트 반환
     */
    public List<NewExpenseOptionDto> getExpenseTypes(
            String largeCategory,
            String mediumCategory,
            String smallCategory
    ) {
        String fixedLarge = normalizeLargeCategory(largeCategory);
        String normalizedMedium = normalizeFilter(mediumCategory);
        String normalizedSmall = normalizeFilter(smallCategory);

        if (isAllOrBlank(normalizedMedium) || isAllOrBlank(normalizedSmall)) return Collections.emptyList();

        return newExpenseMapper.selectDistinctExpenseTypesByCategory(
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
    public NewExpensePageResponseDto getExpensePage(NewExpenseSearchRequestDto request) {
        NewExpenseSearchRequestDto normalized = normalizeForSearch(request);
        return fetchPage(normalized);
    }

    /**
     * 전체 조회 버튼용 페이징 조회.
     * - 필터는 모두 제거(대분류=비용, 중/소/유형=ALL, 날짜=null).
     */
    public NewExpensePageResponseDto getExpenseAllPage(NewExpenseSearchRequestDto request) {
        NewExpenseSearchRequestDto src = (request == null) ? new NewExpenseSearchRequestDto() : request;

        NewExpenseSearchRequestDto normalized = NewExpenseSearchRequestDto.builder()
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
    private NewExpensePageResponseDto fetchPage(NewExpenseSearchRequestDto normalized) {
        long totalCount = newExpenseMapper.countExpensePage(normalized);
        List<NewExpenseListItemDto> items = (totalCount > 0)
                ? newExpenseMapper.selectExpensePage(normalized)
                : Collections.emptyList();

        long totalPages = calculateTotalPages(totalCount, normalized.getLimit());

        return NewExpensePageResponseDto.builder()
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
    private NewExpenseSearchRequestDto normalizeForSearch(NewExpenseSearchRequestDto request) {
        NewExpenseSearchRequestDto src = (request == null) ? new NewExpenseSearchRequestDto() : request;

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

        return NewExpenseSearchRequestDto.builder()
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



    /**
     * 비용 신규 생성.
     *
     * @param request 생성 요청 DTO
     * @return 처리 결과 DTO
     */
    @Transactional
    public NewExpenseCommandResponseDto createExpense(NewExpenseUpsertRequestDto request) {
        String large = normalizeValue(request.getLargeCategory());
        String medium = normalizeValue(request.getMediumCategory());
        String small = normalizeValue(request.getSmallCategory());
        String description = normalizeValue(request.getDescription());

        if (!FIXED_LARGE_CATEGORY.equals(large)) {
            return fail("대분류는 '비용'만 허용됩니다.", null);
        }
        if (!isValidCategoryValue(medium) || !isValidCategoryValue(small)) {
            return fail("중/소분류는 필수이며 '전체'는 사용할 수 없습니다.", null);
        }
        if (request.getExpenseDate() == null) {
            return fail("발생일은 필수입니다.", null);
        }
        if (request.getTotalCost() == null || request.getTotalCost() < 0) {
            return fail("총금액은 0 이상이어야 합니다.", null);
        }
        if (description.isEmpty()) {
            return fail("비용 발생처는 필수입니다.", null);
        }

        ExpenseType expenseType = parseExpenseType(request.getExpenseType());
        if (expenseType == null) {
            return fail("유효하지 않은 비용 유형입니다.", null);
        }

        Long categoryId = newExpenseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) {
            return fail("선택한 분류 조합에 해당하는 카테고리를 찾을 수 없습니다.", null);
        }

        int duplicated = newExpenseMapper.countByUniqueKey(
                request.getExpenseDate(),
                request.getExpenseTime(),
                request.getTotalCost()
        );
        if (duplicated > 0) {
            return fail("동일한 (발생일, 발생시간, 총금액) 데이터가 이미 존재합니다.", null);
        }

        int inserted = newExpenseMapper.insertExpense(
                categoryId,
                request.getExpenseDate(),
                request.getExpenseTime(),
                expenseType.name(),
                request.getUnitCost(),
                request.getQuantity(),
                request.getTotalCost(),
                description
        );
        if (inserted < 1) {
            return fail("저장에 실패했습니다.", null);
        }

        Long id = newExpenseMapper.findExpenseIdByUniqueKey(
                request.getExpenseDate(),
                request.getExpenseTime(),
                request.getTotalCost()
        );

        return success("저장 완료", id);
    }

    /**
     * 비용 수정.
     *
     * @param id 수정 대상 ID
     * @param request 수정 요청 DTO
     * @return 처리 결과 DTO
     */
    @Transactional
    public NewExpenseCommandResponseDto updateExpense(Long id, NewExpenseUpsertRequestDto request) {
        if (id == null || id < 1) {
            return fail("유효하지 않은 ID입니다.", id);
        }

        String large = normalizeValue(request.getLargeCategory());
        String medium = normalizeValue(request.getMediumCategory());
        String small = normalizeValue(request.getSmallCategory());
        String description = normalizeValue(request.getDescription());

        if (!FIXED_LARGE_CATEGORY.equals(large)) {
            return fail("대분류는 '비용'만 허용됩니다.", id);
        }
        if (!isValidCategoryValue(medium) || !isValidCategoryValue(small)) {
            return fail("중/소분류는 필수이며 '전체'는 사용할 수 없습니다.", id);
        }
        if (request.getExpenseDate() == null) {
            return fail("발생일은 필수입니다.", id);
        }
        if (request.getTotalCost() == null || request.getTotalCost() < 0) {
            return fail("총금액은 0 이상이어야 합니다.", id);
        }
        if (description.isEmpty()) {
            return fail("비용 발생처는 필수입니다.", id);
        }

        ExpenseType expenseType = parseExpenseType(request.getExpenseType());
        if (expenseType == null) {
            return fail("유효하지 않은 비용 유형입니다.", id);
        }

        Long categoryId = newExpenseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) {
            return fail("선택한 분류 조합에 해당하는 카테고리를 찾을 수 없습니다.", id);
        }

        int duplicated = newExpenseMapper.countByUniqueKeyExcludingId(
                id,
                request.getExpenseDate(),
                request.getExpenseTime(),
                request.getTotalCost()
        );
        if (duplicated > 0) {
            return fail("수정값과 동일한 (발생일, 발생시간, 총금액) 데이터가 이미 존재합니다.", id);
        }

        int updated = newExpenseMapper.updateExpenseById(
                id,
                categoryId,
                request.getExpenseDate(),
                request.getExpenseTime(),
                expenseType.name(),
                request.getUnitCost(),
                request.getQuantity(),
                request.getTotalCost(),
                description
        );
        if (updated < 1) {
            return fail("수정 대상이 없거나 수정에 실패했습니다.", id);
        }

        return success("수정 완료", id);
    }

    /**
     * 비용 삭제.
     *
     * @param id 삭제 대상 ID
     * @return 처리 결과 DTO
     */
    @Transactional
    public NewExpenseCommandResponseDto deleteExpense(Long id) {
        if (id == null || id < 1) {
            return fail("유효하지 않은 ID입니다.", id);
        }

        int deleted = newExpenseMapper.deleteExpenseById(id);
        if (deleted < 1) {
            return fail("삭제 대상이 없거나 삭제에 실패했습니다.", id);
        }

        return success("삭제 완료", id);
    }

    // 한글 라벨 매핑은 프론트에서 처리하고, 백엔드는 enum 코드만 파싱한다.
    private ExpenseType parseExpenseType(String rawType) {
        String value = normalizeValue(rawType);
        if (value.isEmpty()) return null;

        try {
            return ExpenseType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * 공백 제거 정규화.
     *
     * @param value 원본 문자열
     * @return 정규화된 문자열
     */
    private String normalizeValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 분류값 유효성 검증(빈값/ALL/전체 금지).
     *
     * @param value 분류 문자열
     * @return 유효 여부
     */
    private boolean isValidCategoryValue(String value) {
        if (value == null || value.isBlank()) return false;
        if ("전체".equals(value)) return false;
        return !"ALL".equalsIgnoreCase(value);
    }

    /**
     * 실패 응답 DTO를 생성한다.
     *
     * @param message 실패 메시지
     * @param id 대상 ID
     * @return 실패 응답 DTO
     */
    private NewExpenseCommandResponseDto fail(String message, Long id) {
        return NewExpenseCommandResponseDto.builder()
                .success(false)
                .message(message)
                .id(id)
                .build();
    }

    /**
     * 성공 응답 DTO를 생성한다.
     *
     * @param message 성공 메시지
     * @param id 대상 ID
     * @return 성공 응답 DTO
     */
    private NewExpenseCommandResponseDto success(String message, Long id) {
        return NewExpenseCommandResponseDto.builder()
                .success(true)
                .message(message)
                .id(id)
                .build();
    }


    public NewExpenseExcelPageResponseDto searchAutoPage(
            MultipartFile file,
            NewExpenseExcelSearchPageRequestDto req
    ) {
        long started = System.currentTimeMillis();
        log.info("[searchAutoPage:start] file={}, size={}, page={}, startDate={}, endDate={}, description={}",
                safeFileName(file), (file == null ? 0 : file.getSize()),
                req.getPageOrDefault(), req.getStartDate(), req.getEndDate(), req.getDescription());

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어 있습니다.");
        }

        try (InputStream is = file.getInputStream()) {
            // 1) 업로드 파일 파싱
            List<BankTransactionRowDto> parsedRows = newExcelReadService.readRows(is);
            log.info("[searchAutoPage:parsed] totalRows={}", parsedRows.size());

            // 2) 매입처 필터 (입력값 있을 때만 적용)
            List<BankTransactionRowDto> supplierFilteredRows;
            if (req.getDescription() != null
                    && !req.getDescription().trim().isEmpty()
                    && !"전체".equals(req.getDescription().trim())) {
                supplierFilteredRows = newExcelFilterService.filterByRecipientName(parsedRows, req.getDescription().trim());
            } else {
                supplierFilteredRows = parsedRows;
            }
            log.info("[searchAutoPage:supplierFiltered] rows={}", supplierFilteredRows.size());

            // 3) 날짜 필터 + 응답 DTO 매핑
            List<NewExpenseExcelRowResponseDto> allRows = supplierFilteredRows.stream()
                    .filter(row -> matchDateRange(row.getTransactionDate(), req.getStartDate(), req.getEndDate()))
                    .map(this::toExcelRowResponse)
                    .collect(Collectors.toList());

            // 4) 페이지 응답
            NewExpenseExcelPageResponseDto page =
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


    private boolean matchDateRange(LocalDate ExpenseDate, LocalDate start, LocalDate end) {
        if (log.isTraceEnabled()) {
            log.trace("[matchDateRange] ExpenseDate={}, start={}, end={}", ExpenseDate, start, end);
        }
        if (ExpenseDate == null) return false;
        if (start != null && ExpenseDate.isBefore(start)) return false;
        if (end != null && ExpenseDate.isAfter(end)) return false;
        return true;
    }



    private NewExpenseExcelPageResponseDto toPage(
            List<NewExpenseExcelRowResponseDto> rows, int page, int size
    ) {
        log.debug("[toPage:start] rows={}, page={}, size={}", rows.size(), page, size);

        int totalCount = rows.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int safePage = totalPages == 0 ? 1 : Math.min(page, totalPages);
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, totalCount);

        List<NewExpenseExcelRowResponseDto> items =
                (from >= totalCount) ? List.of() : rows.subList(from, to);

        log.debug("[toPage:end] safePage={}, from={}, to={}, itemCount={}",
                safePage, from, to, items.size());

        return NewExpenseExcelPageResponseDto.builder()
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
    private NewExpenseExcelRowResponseDto toExcelRowResponse(BankTransactionRowDto row) {
        if (row == null) {
            return null;
        }

        return NewExpenseExcelRowResponseDto.builder()
                .expenseDate(row.getTransactionDate())
                .expenseTime(row.getTransactionTime())
                .description(row.getRecipientName())
                .totalCost(row.getWithdrawAmount())
                .build();
    }
}
