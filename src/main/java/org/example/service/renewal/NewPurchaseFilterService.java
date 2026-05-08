package org.example.service.renewal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.renewal.BankTransactionRowDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewPurchaseFilterService {

    @Value("${app.renewal.purchase-import.recipient-filter}")
    private String recipientFilter;

    private final NewPurchaseExcelReadService newPurchaseExcelReadService;

    /**
     * 엑셀 파일에서 전체 거래내역을 읽은 뒤
     * 기록사항이 "이순희"인 거래만 필터링하여 반환한다.
     *
     * 현재는 오이 매입 후보를 확인하기 위한 1차 필터링 용도로 사용한다.
     *
     * @param fileName C:\gamfile 아래에 있는 엑셀 파일명
     * @return 이순희 거래만 담긴 리스트
     * @throws IOException 파일 읽기 실패 시 발생
     */
    public List<BankTransactionRowDto> filterOiSupplierRows(String fileName) throws IOException {
        List<BankTransactionRowDto> rows = newPurchaseExcelReadService.readRows(fileName);

        log.info("은행 거래내역 총 {}건 읽음", rows.size());

        List<BankTransactionRowDto> filteredRows = rows.stream()
                .filter(row -> row.getRecipientName() != null)
                .filter(row -> row.getRecipientName().contains(recipientFilter))
                .toList();

        log.info("{} 거래 {}건 필터링 완료", recipientFilter, filteredRows.size());

        filteredRows.stream()
                .limit(10)
                .forEach(row -> log.info(
                        "sample -> date={}, time={}, amount={}, recipient={}",
                        row.getTransactionDate(),
                        row.getTransactionTime(),
                        row.getWithdrawAmount(),
                        row.getRecipientName()
                ));

        return filteredRows;
    }

    /**
     * 전달받은 거래 목록 중 특정 거래처명(기록사항)을 포함하는 행만 필터링한다.
     *
     * 추후 안현우, 곽근탁, 이순우 등 다른 거래처 기준으로도
     * 재사용할 수 있도록 공통 필터 메서드로 분리한다.
     *
     * @param rows 전체 거래 목록
     * @param recipientName 기록사항에 포함되어야 하는 거래처명
     * @return 해당 거래처명으로 필터링된 거래 목록
     */
    public List<BankTransactionRowDto> filterByRecipientName(List<BankTransactionRowDto> rows, String recipientName) {
        if (rows == null || recipientName == null || recipientName.isBlank()) {
            return List.of();
        }

        return rows.stream()
                .filter(row -> row.getRecipientName() != null)
                .filter(row -> row.getRecipientName().contains(recipientName))
                .toList();
    }
}
