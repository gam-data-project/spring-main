package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.product.NewProductDeactiveService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/renewal")
@RequiredArgsConstructor
public class ProductDeactiveController {

    private final NewProductDeactiveService newProductDeactiveService;

    // 예: POST /admin/npi/deactivate?date=2022-06-01
    @PostMapping("/deactivate")
    public String deactivateProduct(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        int updated = newProductDeactiveService.deactivateProduct(date);
        return "deactivated rows: " + updated;
    }
}
