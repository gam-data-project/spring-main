package org.example.service.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.excel.BankTransactionRowDto;
import org.example.dto.migration.PurchaseMigrationDto;
import org.example.repository.PurchaseMigrationMapper;
import org.example.service.excel.NewExcelFilterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseMigrationService {
    private static final int CHUNK_SIZE = 100;

    private final NewExcelFilterService newPurchaseFilterService;
    private final PurchaseMigrationMapper purchaseMigrationMapper;

    /**
     * 엑셀 파일에서 거래 데이터를 읽고,
     * 설정된 거래처명 기준으로 필터링한 뒤
     * 신규 매입 데이터로 변환하여 청크 단위로 저장한다.
     *
     * @param fileName 읽을 엑셀 파일명
     * @return 최종 저장된 행 수
     * @throws IOException 엑셀 파일 읽기 실패 시 발생
     */
    @Transactional
    public int saveFilteredPurchases(String fileName) throws IOException {
        List<BankTransactionRowDto> filteredRows =
                newPurchaseFilterService.filterOiSupplierRows(fileName);

        List<PurchaseMigrationDto> purchaseList =
                newPurchaseFilterService.toNewPurchaseInfoList(filteredRows);

        return savePurchaseListInChunks(purchaseList);
    }

    /**
     * 신규 매입 데이터 목록을 100건 단위 청크로 나누어 저장한다.
     *
     * 한 번에 너무 많은 데이터를 INSERT 하지 않도록 분할하여 처리하며,
     * 각 청크는 MyBatis foreach 기반 일괄 저장으로 수행한다.
     *
     * @param purchaseList 저장할 신규 매입 데이터 목록
     * @return 총 저장된 행 수
     */
    @Transactional
    public int savePurchaseListInChunks(List<PurchaseMigrationDto> purchaseList) {
        if (purchaseList == null || purchaseList.isEmpty()) {
            return 0;
        }

        int insertedCount = 0;

        for (int start = 0; start < purchaseList.size(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, purchaseList.size());
            List<PurchaseMigrationDto> chunk = purchaseList.subList(start, end);

            log.info("매입 데이터 청크 저장 시작: {} ~ {}", start, end - 1);

            insertedCount += purchaseMigrationMapper.insertNewPurchaseList(chunk);
        }

        log.info("매입 데이터 총 {}건 저장 완료", insertedCount);

        return insertedCount;
    }
}
