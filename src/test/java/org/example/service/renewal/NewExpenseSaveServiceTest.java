package org.example.service.renewal;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.ExpenseType;
import org.example.dto.renewal.BankTransactionRowDto;
import org.example.dto.renewal.NewExpenseInfoDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootTest
public class NewExpenseSaveServiceTest {

    @Value("${excel.common-import.file-name}")
    private String fileName;

    @Value("${excel.common-import.recipient-filter}")
    private String recipientFilter;

    @Value("${excel.expense-import.expense-type}")
    private ExpenseType expenseType;

    @Value("${excel.expense-import.category-id}")
    private Long categoryId;

    @Autowired
    private NewExcelFilterService newExcelFilterService;

    @Autowired
    private NewExpenseSaveService newExpenseSaveService;

    @Test
    @DisplayName("계좌 거래 중 비용 데이터를 DB에 저장한다")
    void saveFilteredExpenses_success() throws IOException {
        int insertedCount = newExpenseSaveService.saveFilteredExpenses(
                fileName,
                categoryId,
                expenseType
        );

        log.info("{} 저장된 비용 데이터 건수={}", recipientFilter, insertedCount);

        Assertions.assertTrue(insertedCount > 0, "저장된 비용 데이터가 1건 이상이어야 합니다.");
    }

    @Test
    @DisplayName("비용 대상 거래를 필터링한다")
    void filterExpenseRows_success() throws IOException {
        List<BankTransactionRowDto> filteredRows =
                newExcelFilterService.filterOiSupplierRows(fileName);

        log.info("필터 결과 건수={}", filteredRows.size());

        filteredRows.stream()
                .limit(10)
                .forEach(row -> log.info(
                        "filtered -> date={}, time={}, amount={}, recipient={}",
                        row.getTransactionDate(),
                        row.getTransactionTime(),
                        row.getWithdrawAmount(),
                        row.getRecipientName()
                ));

        Assertions.assertNotNull(filteredRows);
        Assertions.assertFalse(filteredRows.isEmpty(), "비용 대상 거래가 1건 이상 있어야 합니다.");
        Assertions.assertTrue(
                filteredRows.stream().allMatch(row ->
                        row.getRecipientName() != null && row.getRecipientName().trim().equals(recipientFilter)),
                        //row.getRecipientName() != null && row.getRecipientName().contains(recipientFilter),
                "필터 결과에는 recipientFilter에 해당하는 거래만 있어야 합니다."
        );
    }

    @Test
    @DisplayName("필터링한 거래를 비용 DTO로 변환한다")
    void toNewExpenseInfoList_success() throws IOException {
        List<BankTransactionRowDto> filteredRows =
                newExcelFilterService.filterOiSupplierRows(fileName);

        List<NewExpenseInfoDto> expenseList = filteredRows.stream()
                .map(row -> newExpenseSaveService.toNewExpenseInfo(row, categoryId, expenseType))
                .toList();

        log.info("비용 DTO 변환 건수={}", expenseList.size());

        expenseList.stream()
                .limit(10)
                .forEach(expense -> log.info(
                        "expense -> date={}, time={}, categoryId={}, expenseType={}, totalCost={}, description={}",
                        expense.getExpenseDate(),
                        expense.getExpenseTime(),
                        expense.getCategoryId(),
                        expense.getExpenseType(),
                        expense.getTotalCost(),
                        expense.getDescription()
                ));

        Assertions.assertNotNull(expenseList);
        Assertions.assertFalse(expenseList.isEmpty(), "비용 DTO 변환 결과가 비어 있으면 안 됩니다.");
    }


}
