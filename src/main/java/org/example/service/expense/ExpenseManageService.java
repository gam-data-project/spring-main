package org.example.service.expense;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.enums.ExpenseType;
import org.example.dto.expense.request.ExpenseUpsertRequestDto;
import org.example.dto.expense.response.ExpenseCommandResponseDto;
import org.example.repository.ExpenseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseManageService {

    private final ExpenseMapper expenseMapper;

    private static final String FIXED_LARGE_CATEGORY = "비용";

    /**
     * 비용 신규 생성.
     *
     * @param request 생성 요청 DTO
     * @return 처리 결과 DTO
     */
    @Transactional
    public ExpenseCommandResponseDto createExpense(ExpenseUpsertRequestDto request) {
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

        Long categoryId = expenseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) {
            return fail("선택한 분류 조합에 해당하는 카테고리를 찾을 수 없습니다.", null);
        }

        int duplicated = expenseMapper.countByUniqueKey(
                request.getExpenseDate(),
                request.getExpenseTime(),
                request.getTotalCost()
        );
        if (duplicated > 0) {
            return fail("동일한 (발생일, 발생시간, 총금액) 데이터가 이미 존재합니다.", null);
        }

        int inserted = expenseMapper.insertExpense(
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

        Long id = expenseMapper.findExpenseIdByUniqueKey(
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
    public ExpenseCommandResponseDto updateExpense(Long id, ExpenseUpsertRequestDto request) {
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

        Long categoryId = expenseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) {
            return fail("선택한 분류 조합에 해당하는 카테고리를 찾을 수 없습니다.", id);
        }

        int duplicated = expenseMapper.countByUniqueKeyExcludingId(
                id,
                request.getExpenseDate(),
                request.getExpenseTime(),
                request.getTotalCost()
        );
        if (duplicated > 0) {
            return fail("수정값과 동일한 (발생일, 발생시간, 총금액) 데이터가 이미 존재합니다.", id);
        }

        int updated = expenseMapper.updateExpenseById(
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
    public ExpenseCommandResponseDto deleteExpense(Long id) {
        if (id == null || id < 1) {
            return fail("유효하지 않은 ID입니다.", id);
        }

        int deleted = expenseMapper.deleteExpenseById(id);
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
    private ExpenseCommandResponseDto fail(String message, Long id) {
        return ExpenseCommandResponseDto.builder()
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
    private ExpenseCommandResponseDto success(String message, Long id) {
        return ExpenseCommandResponseDto.builder()
                .success(true)
                .message(message)
                .id(id)
                .build();
    }
}
