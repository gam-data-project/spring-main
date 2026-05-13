package org.example.service.renewal;


import lombok.extern.slf4j.Slf4j;
import org.example.dto.renewal.BankTransactionRowDto;
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
public class NewExcelFilterServiceTest {

    @Value("${excel.common-import.recipient-filter}")
    private String recipientFilter;

    @Value("${excel.common-import.file-name}")
    private String fileName;


    @Autowired
    private NewExcelReadService newExcelReadService;

    @Autowired
    private NewExcelFilterService newExcelFilterService;

    @Autowired
    private NewPurchaseSaveService newPurchaseSaveService;


    @Test
    @DisplayName("필터링된 매입 데이터를 DB에 저장한다")
    void saveFilteredPurchases_success() throws IOException {
        int insertedCount = newPurchaseSaveService.saveFilteredPurchases(fileName);

        log.info("저장된 매입 데이터 건수={}", insertedCount);

        Assertions.assertTrue(insertedCount > 0, "저장된 데이터가 1건 이상이어야 합니다.");
    }

    @Test
    @DisplayName("엑셀 파일을 읽어 전체 거래내역을 DTO 리스트로 변환한다")
    void readRows_success() throws IOException {
        List<BankTransactionRowDto> rows = newExcelReadService.readRows(fileName);

        log.info("전체 거래내역 건수={}", rows.size());

        rows.stream()
                .limit(10)
                .forEach(row -> log.info(
                        "sample -> date={}, time={}, amount={}, recipient={}",
                        row.getTransactionDate(),
                        row.getTransactionTime(),
                        row.getWithdrawAmount(),
                        row.getRecipientName()
                ));

        Assertions.assertNotNull(rows);
        Assertions.assertFalse(rows.isEmpty(), "엑셀에서 읽은 거래내역이 비어 있으면 안 됩니다.");
    }

    @Test
    @DisplayName("거래 필터링한다")
    void filterOiSupplierRows_success() throws IOException {
        List<BankTransactionRowDto> filteredRows =
                newExcelFilterService.filterOiSupplierRows(fileName);

        log.info("{} 필터 결과 건수={}", recipientFilter ,filteredRows.size());

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
        Assertions.assertFalse(filteredRows.isEmpty(), "거래가 하나도 없으면 필터 조건을 다시 확인해야 합니다.");
        Assertions.assertTrue(
                filteredRows.stream().allMatch(row ->
                        row.getRecipientName() != null && row.getRecipientName().trim().equals(recipientFilter)),
//                        row.getRecipientName() != null && row.getRecipientName().contains(recipientFilter)),
                "결과에는 필터링한 데이터만 있어야 합니다."
        );
    }
}
