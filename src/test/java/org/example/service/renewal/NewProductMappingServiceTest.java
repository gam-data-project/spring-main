package org.example.service.renewal;


import lombok.extern.slf4j.Slf4j;
import org.example.service.product.NewProductDeactiveService;
import org.example.service.product.NewProductMappingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
public class NewProductMappingServiceTest {

    @Autowired
    private NewProductMappingService newProductMappingService;

    @Autowired
    private NewProductDeactiveService newProductDeactiveService;

    @Test
    @DisplayName("work 테이블 기준으로 판매 데이터를 product 정보로 upsert 한다")
    void backfillByDateRange_success() {
        LocalDate from = LocalDate.of(2023, 11, 4);
        LocalDate to = LocalDate.of(2024, 1, 3);

        int processed = newProductMappingService.backfillByDateRange(from, to);

        log.info("product backfill processed={}", processed);

        Assertions.assertTrue(processed > 0, "업서트된 판매 데이터가 1건 이상이어야 합니다.");
    }

    @Test
    @DisplayName("deactivate 후 work 테이블 기준으로 판매 데이터를 product 정보로 upsert 한다")
    void deactivateThenBackfill_success() {
        LocalDate from = LocalDate.of(2026, 4, 4);
        LocalDate to = LocalDate.of(2026, 8, 3);

        int deactivated = newProductDeactiveService.deactivateProduct(from);
        int processed = newProductMappingService.backfillByDateRange(from, to);

        log.info("deactivated={}, processed={}", deactivated, processed);

        Assertions.assertTrue(processed > 0, "업서트된 판매 데이터가 1건 이상이어야 합니다.");
    }
}
