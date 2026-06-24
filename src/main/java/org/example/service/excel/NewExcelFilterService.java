package org.example.service.excel;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.excel.BankTransactionRowDto;
import org.example.dto.migration.PurchaseMigrationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewExcelFilterService {

    @Value("${excel.common-import.recipient-filter}")
    private String recipientFilter;

    private final NewExcelReadService newExcelReadService;
    private static final int FIXED_QUANTITY = 1;

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
        List<BankTransactionRowDto> rows = newExcelReadService.readRows(fileName);

        log.info("은행 거래내역 총 {}건 읽음", rows.size());

        List<BankTransactionRowDto> filteredRows = rows.stream()
                .filter(row -> row.getRecipientName() != null)
                //.filter(row -> row.getRecipientName().contains(recipientFilter))
                .filter(row -> row.getRecipientName().trim().equals(recipientFilter))
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
                .filter(row -> row.getRecipientName().trim().equals(recipientName))
                //.filter(row -> row.getRecipientName().contains(recipientName))
                .toList();
    }

    /**
     * 은행 거래 1건을 신규 매입 데이터 DTO로 변환한다.
     *
     * 현재는 고정 카테고리(category_id = 7) 기준으로 매핑하며,
     * 과거 이관 데이터 특성상 수량은 1로 고정하고
     * 출금금액을 단가와 총금액으로 동일하게 사용한다.
     *
     * @param row 은행 입출금 엑셀에서 추출한 거래 1건
     * @return 신규 매입 데이터 DTO, 입력값이 null이면 null
     * @since 2026.05.08
     */
    public PurchaseMigrationDto toNewPurchaseInfo(BankTransactionRowDto row) {
        if (row == null) {
            return null;
        }

        return PurchaseMigrationDto.builder()
                .id(0L)
                .productId(null)
                .purchaseDate(row.getTransactionDate())
                .purchaseTime(row.getTransactionTime())
                .quantity(FIXED_QUANTITY)
                .unitCost(row.getWithdrawAmount())
                .totalCost(row.getWithdrawAmount())
                .supplierName(row.getRecipientName())
                .build();
    }

    /**
     * 은행 거래 목록을 신규 매입 데이터 DTO 목록으로 일괄 변환한다.
     *
     * 각 거래는 toNewPurchaseInfo() 규칙에 따라 변환되며,
     * 입력 목록이 null 이거나 비어 있으면 빈 리스트를 반환한다.
     *
     * @param rows 은행 입출금 엑셀에서 추출한 거래 목록
     * @return 신규 매입 데이터 DTO 목록
     * @since 2026.05.08
     */
    public List<PurchaseMigrationDto> toNewPurchaseInfoList(List<BankTransactionRowDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        return rows.stream()
                .map(this::toNewPurchaseInfo)
                .toList();
    }

}
