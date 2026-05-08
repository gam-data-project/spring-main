package org.example.controller.renewal;

import lombok.RequiredArgsConstructor;
import org.example.domain.NewSales;
import org.example.service.NewSalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renewal")
@RequiredArgsConstructor
public class NewSalesController {

    private final NewSalesService newSalesService;

    @PostMapping("/salesData")
    public ResponseEntity<String> insertNewSales(@RequestBody NewSales sales){
        // 받은 데이터 확인 (콘솔 출력)
        System.out.println("=== New Sales Data Received ===");
        System.out.println(sales);

        int inserted = newSalesService.save(sales);
        //없서트

        return ResponseEntity.ok("데이터 수신/저장 완료(" + inserted + ")");
    }



}
