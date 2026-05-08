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
public class NewPurchaseFilterServiceTest {

    @Value("${app.renewal.purchase-import.recipient-filter}")
    private String recipientFilter;

    @Value("${app.renewal.purchase-import.file-name}")
    private String fileName;


    @Autowired
    private NewPurchaseExcelReadService newPurchaseExcelReadService;

    @Autowired
    private NewPurchaseFilterService newPurchaseFilterService;

    @Test
    @DisplayName("엑셀 파일을 읽어 전체 거래내역을 DTO 리스트로 변환한다")
    void readRows_success() throws IOException {
        List<BankTransactionRowDto> rows = newPurchaseExcelReadService.readRows(fileName);

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
                newPurchaseFilterService.filterOiSupplierRows(fileName);

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
                        row.getRecipientName() != null && row.getRecipientName().contains(recipientFilter)),
                "결과에는 필터링한 데이터만 있어야 합니다."
        );
    }
}
