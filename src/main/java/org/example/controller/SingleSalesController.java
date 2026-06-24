package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.sales.request.SalesRequestDto;
import org.example.service.sales.SingleSalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renewal")
@RequiredArgsConstructor
public class SingleSalesController {

    private final SingleSalesService newSalesService;

    @PostMapping("/salesData")
    public ResponseEntity<String> insertNewSales(@RequestBody SalesRequestDto sales){
        // 받은 데이터 확인 (콘솔 출력)
        System.out.println("=== New Sales Data Received ===");
        System.out.println(sales);

        int inserted = newSalesService.save(sales);
        //없서트

        return ResponseEntity.ok("데이터 수신/저장 완료(" + inserted + ")");
    }



}
