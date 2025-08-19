package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.NewDeliveryFee;
import org.example.service.NewDeliveryFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renewal")
@RequiredArgsConstructor
public class NewDeliveryFeeController {

    private final NewDeliveryFeeService newDeliveryFeeService;

    @PostMapping("/deliveryFeeData")
    public ResponseEntity<String> insertDeliveryFee(@RequestBody NewDeliveryFee deliveryFee){
        // 받은 데이터 확인 (콘솔 출력)
        System.out.println("=== Delivery Fee Data Received ===");
        System.out.println(deliveryFee);

        int inserted = newDeliveryFeeService.save(deliveryFee);

        return ResponseEntity.ok("배송비 데이터 수신/저장 완료 (" + inserted + ")");
    }
}
