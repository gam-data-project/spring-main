package org.example.service.renewal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.ExpenseType;
import org.example.dto.renewal.BankTransactionRowDto;
import org.example.dto.renewal.NewExpenseInfoDto;
import org.example.repository.renewal.NewExpenseInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewExpenseSaveService {
    private static final int CHUNK_SIZE = 100;

    private final NewExcelFilterService newPurchaseFilterService;
    private final NewExpenseInfoMapper newExpenseInfoMapper;

    /**
     * 엑셀 파일에서 거래 데이터를 읽고,
     * 매입 필터와 동일한 거래처명 필터를 적용한 뒤
     * 신규 비용 데이터로 변환하여 저장한다.
     *
     * @param fileName 읽을 엑셀 파일명
     * @param categoryId 저장할 비용 카테고리 ID
     * @param expenseType 저장할 비용 타입
     * @return 최종 저장된 행 수
     * @throws IOException 엑셀 파일 읽기 실패 시 발생
     */
    @Transactional
    public int saveFilteredExpenses(String fileName,
                                    Long categoryId,
                                    ExpenseType expenseType) throws IOException {

        List<BankTransactionRowDto> filteredRows =
                newPurchaseFilterService.filterOiSupplierRows(fileName);

        List<NewExpenseInfoDto> expenseList = filteredRows.stream()
                .map(row -> toNewExpenseInfo(row, categoryId, expenseType))
                .toList();

        return saveExpenseListInChunks(expenseList);
    }

    /**
     * 은행 거래 1건을 신규 비용 데이터 DTO로 변환한다.
     *
     * @param row 은행 입출금 엑셀에서 추출한 거래 1건
     * @param categoryId 저장할 비용 카테고리 ID
     * @param expenseType 저장할 비용 타입
     * @return 신규 비용 데이터 DTO, 입력값이 null이면 null
     */
    public NewExpenseInfoDto toNewExpenseInfo(BankTransactionRowDto row,
                                              Long categoryId,
                                              ExpenseType expenseType) {
        if (row == null) {
            return null;
        }

        return NewExpenseInfoDto.builder()
                .id(0L)
                .expenseDate(row.getTransactionDate())
                .expenseTime(row.getTransactionTime())
                .categoryId(categoryId)
                .expenseType(expenseType)
                .unitCost(null)
                .quantity(null)
                .totalCost(row.getWithdrawAmount())
                .description(row.getRecipientName())
                .build();
    }

    /**
     * 신규 비용 데이터 목록을 100건 단위 청크로 나누어 저장한다.
     *
     * @param expenseList 저장할 신규 비용 데이터 목록
     * @return 총 저장된 행 수
     */
    @Transactional
    public int saveExpenseListInChunks(List<NewExpenseInfoDto> expenseList) {
        if (expenseList == null || expenseList.isEmpty()) {
            return 0;
        }

        int insertedCount = 0;

        for (int start = 0; start < expenseList.size(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, expenseList.size());
            List<NewExpenseInfoDto> chunk = expenseList.subList(start, end);

            log.info("비용 데이터 청크 저장 시작: {} ~ {}", start, end - 1);

            insertedCount += newExpenseInfoMapper.insertNewExpenseList(chunk);
        }

        log.info("비용 데이터 총 {}건 저장 완료", insertedCount);

        return insertedCount;
    }
}
