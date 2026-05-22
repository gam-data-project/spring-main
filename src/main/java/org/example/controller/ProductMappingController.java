package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.product.NewProductMappingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/renewal")
@RequiredArgsConstructor
@Slf4j
public class ProductMappingController {

    private final NewProductMappingService productMappingService;

    /**
     * 특정 기간의 sales 데이터를 product_info로 업서트
     */
    @PostMapping("/product")
    public ResponseEntity<String> backfill(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        int total = productMappingService.backfillByDateRange(from, to);

        String msg = String.format("Backfill done. from=%s, to=%s, processed=%d", from, to, total);
        log.info("{}", msg);

        return ResponseEntity.ok(msg);

    }

}
