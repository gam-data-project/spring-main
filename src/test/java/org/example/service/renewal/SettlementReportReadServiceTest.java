package org.example.service.renewal;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.settlementReport.request.SettlementReportRequestDto;
import org.example.dto.settlementReport.response.SettlementReportResponseDto;
import org.example.dto.settlementReport.response.SettlementReportRowDto;
import org.example.service.settlementReport.SettlementReportReadService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class SettlementReportReadServiceTest {

    @Autowired
    private SettlementReportReadService settlementReportReadService;

    @Test
    @DisplayName("연도 기준 정산 리포트를 조회하면 12개월 + 총액 1행 데이터가 반환된다")
    void getSettlementReport_success() {
        SettlementReportRequestDto request = SettlementReportRequestDto.builder()
                .year(2022)
                .build();

        SettlementReportResponseDto response =
                settlementReportReadService.getSettlementReport(request);

        log.info("response year={}, itemCount={}",
                response.getYear(), response.getItems().size());

        response.getItems().forEach(item -> log.info(
                "item -> ym={}, sales={}, purchase={}, expense={}, profit={}",
                item.getYm(),
                item.getSalesAmount(),
                item.getPurchaseAmount(),
                item.getExpenseAmount(),
                item.getProfit()
        ));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2022, response.getYear());
        Assertions.assertNotNull(response.getItems());
        Assertions.assertEquals(13, response.getItems().size(),
                "정산 리포트는 항상 12개월 + 총액 1행 데이터여야 합니다.");

        List<SettlementReportRowDto> items = response.getItems();

        for (int month = 1; month <= 12; month++) {
            SettlementReportRowDto item = items.get(month - 1);
            String expectedYm = String.format("2022-%02d", month);

            Assertions.assertEquals(expectedYm, item.getYm(),
                    "월 순서는 1월부터 12월까지 고정되어야 합니다.");
            Assertions.assertNotNull(item.getSalesAmount(), "매출 값은 null이면 안 됩니다.");
            Assertions.assertNotNull(item.getPurchaseAmount(), "매입 값은 null이면 안 됩니다.");
            Assertions.assertNotNull(item.getExpenseAmount(), "비용 값은 null이면 안 됩니다.");
            Assertions.assertNotNull(item.getProfit(), "순이익 값은 null이면 안 됩니다.");
        }
        // 총액 검수
        SettlementReportRowDto totalItem = items.get(12);

        long expectedTotalSales = items.subList(0, 12).stream()
                .mapToLong(SettlementReportRowDto::getSalesAmount)
                .sum();

        long expectedTotalPurchase = items.subList(0, 12).stream()
                .mapToLong(SettlementReportRowDto::getPurchaseAmount)
                .sum();

        long expectedTotalExpense = items.subList(0, 12).stream()
                .mapToLong(SettlementReportRowDto::getExpenseAmount)
                .sum();

        long expectedTotalProfit = items.subList(0, 12).stream()
                .mapToLong(SettlementReportRowDto::getProfit)
                .sum();
        // 총액 계산이 맞는지 확인
        Assertions.assertEquals(expectedTotalSales, totalItem.getSalesAmount(), "총액 행의 매출 합계가 맞아야 합니다.");
        Assertions.assertEquals(expectedTotalPurchase, totalItem.getPurchaseAmount(), "총액 행의 매입 합계가 맞아야 합니다.");
        Assertions.assertEquals(expectedTotalExpense, totalItem.getExpenseAmount(), "총액 행의 비용 합계가 맞아야 합니다.");
        Assertions.assertEquals(expectedTotalProfit, totalItem.getProfit(), "총액 행의 순이익 합계가 맞아야 합니다.");
    }

    @Test
    @DisplayName("year가 없으면 예외가 발생한다")
    void getSettlementReport_withoutYear_throwsException() {
        SettlementReportRequestDto request = SettlementReportRequestDto.builder()
                .year(null)
                .build();

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> settlementReportReadService.getSettlementReport(request)
        );

        log.info("exception message={}", exception.getMessage());
        Assertions.assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("year 범위가 올바르지 않으면 예외가 발생한다")
    void getSettlementReport_invalidYear_throwsException() {
        SettlementReportRequestDto request = SettlementReportRequestDto.builder()
                .year(1999)
                .build();

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> settlementReportReadService.getSettlementReport(request)
        );

        log.info("exception message={}", exception.getMessage());
        Assertions.assertNotNull(exception.getMessage());
    }
}
