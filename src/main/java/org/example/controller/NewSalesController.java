package org.example.controller;

import org.example.domain.NewSales;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renewal")
public class NewSalesController {

    @PostMapping("/salesData")
    public String insertNewSales(@RequestBody NewSales newSales){
        // 받은 데이터 확인 (콘솔 출력)
        System.out.println("=== New Sales Data Received ===");

        // 개별 필드 확인 (원하면 상세 출력도 가능)
        System.out.println("주문번호: " + newSales.getOrderNumber());
        System.out.println("플랫폼: " + newSales.getPlatform());
        System.out.println("제품명: " + newSales.getProductNameRaw());
        System.out.println("수량: " + newSales.getQuantity());
        System.out.println("총합: " + newSales.getProductTotal());
        System.out.println("단가: " + newSales.getUnitPrice());
        System.out.println("배송비 포함 여부: " + newSales.isShippingIncluded());
        System.out.println("주문일: " + newSales.getOrderDate());

        return "데이터 수신 완료!";
    }

}
