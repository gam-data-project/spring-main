package org.example.controller.legacy;

import lombok.RequiredArgsConstructor;
import org.example.service.legacy.NewResultService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/renewal")
@RequiredArgsConstructor
public class NewResultController {

    private final NewResultService resultService;

    @PostMapping("/result/sales")
    public ResponseEntity<String> loadSales(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int inserted = resultService.loadSales(from, to);

        stopWatch.stop();

        String resultMsg = String.format(
                "매출 적재 완료: %d건, 기간: %s ~ %s, 실행 시간: %d ms",
                inserted, from, to, stopWatch.getTotalTimeMillis()
        );
        return ResponseEntity.ok(resultMsg);
    }


    @PostMapping("/result/delivery")
    public ResponseEntity<String> loadDeliveryFee(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int inserted = resultService.loadDeliveryFee(from, to);
        stopWatch.stop();

        String resultMsg = String.format(
                "매출 적재 완료: %d건, 기간: %s ~ %s, 실행 시간: %d ms",
                inserted, from, to, stopWatch.getTotalTimeMillis()
        );
        return ResponseEntity.ok(resultMsg);

    }
}
