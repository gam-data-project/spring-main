package org.example.service.settlementReport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.settlementReport.request.SettlementReportRequestDto;
import org.example.dto.settlementReport.response.SettlementReportResponseDto;
import org.example.dto.settlementReport.response.SettlementReportRowDto;
import org.example.repository.SettlementReportMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementReportReadService {
    private final SettlementReportMapper settlementMapper;

    public SettlementReportResponseDto getSettlementReport(SettlementReportRequestDto request) {
        // 입력값 검수
        int year = normalizeYear(request);
        log.info("[getSettlementReport:start] year={}", year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = startDate.plusYears(1);

        //데이터 조회
        List<SettlementReportRowDto> rawRows =
                settlementMapper.selectMonthlySettlementReport(startDate, endDate);
        log.info(
                "[getSettlementReport:queried] year={}, startDate={}, endDate={}, rawRowCount={}, rawRows={}",
                year, startDate, endDate, rawRows.size(), rawRows
        );

        // 리스트를 map으로 (날짜를 쉽게 찾기위해 Map 사용)
        Map<String, SettlementReportRowDto> rowMap = rawRows.stream()
                .collect(Collectors.toMap(
                        SettlementReportRowDto::getYm, // key: 년월
                        row -> row, // value: SettlementReportRowDto
                        (left, right) -> left, //같은 key가 중복됐을 때
                        LinkedHashMap::new
                ));

        // 1월부터 12월까지 돌면서, 있으면 실제 데이터 넣고 없으면 0으로 채워서 항상 12개 월별 행을 만듬
        List<SettlementReportRowDto> items = IntStream.rangeClosed(1, 12) //1~12 숫자 순회
                .mapToObj(month -> {
                    String ym = YearMonth.of(year, month).toString(); //현재 처리 중인 월의 키를 생성
                    SettlementReportRowDto row = rowMap.get(ym); //각 월 숫자를 SettlementReportRowDto 객체로 바꿈

                    if (row == null) { // 그 달 데이터가 DB 결과에 없으면 0으로 채움
                        return SettlementReportRowDto.builder()
                                .ym(ym)
                                .salesAmount(0L)
                                .purchaseAmount(0L)
                                .expenseAmount(0L)
                                .profit(0L)
                                .build();
                    }
                    //조회된 데이터가 있으면 사용, 혹시 값이 null이면 0으로 바꿈
                    return SettlementReportRowDto.builder()
                            .ym(ym)
                            .salesAmount(nvl(row.getSalesAmount()))
                            .purchaseAmount(nvl(row.getPurchaseAmount()))
                            .expenseAmount(nvl(row.getExpenseAmount()))
                            .profit(nvl(row.getProfit()))
                            .build();
                })
                .collect(Collectors.toList());

        // 총액 row 계산
        long totalSalesAmount = items.stream()
                .mapToLong(item -> nvl(item.getSalesAmount()))
                .sum();

        long totalPurchaseAmount = items.stream()
                .mapToLong(item -> nvl(item.getPurchaseAmount()))
                .sum();

        long totalExpenseAmount = items.stream()
                .mapToLong(item -> nvl(item.getExpenseAmount()))
                .sum();

        long totalProfit = items.stream()
                .mapToLong(item -> nvl(item.getProfit()))
                .sum();

        // 마지막 행에 총액 추가
        items.add(
                SettlementReportRowDto.builder()
                        .ym("총액")
                        .salesAmount(totalSalesAmount)
                        .purchaseAmount(totalPurchaseAmount)
                        .expenseAmount(totalExpenseAmount)
                        .profit(totalProfit)
                        .build()
        );


        SettlementReportResponseDto response = SettlementReportResponseDto.builder()
                .year(year)
                .items(items)
                .build();


        log.info("[getSettlementReport:end] items={}",
                items.stream()
                        .map(item -> String.format("%s[sales=%d,purchase=%d,expense=%d,profit=%d]",
                                item.getYm(),
                                item.getSalesAmount(),
                                item.getPurchaseAmount(),
                                item.getExpenseAmount(),
                                item.getProfit()))
                        .toList());

        return response;
    }

    private int normalizeYear(SettlementReportRequestDto request) {
        if (request == null || request.getYear() == null) {
            throw new IllegalArgumentException("year는 필수입니다.");
        }

        int year = request.getYear();
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("year 범위가 올바르지 않습니다.");
        }

        return year;
    }

    private long nvl(Long value) {
        return value == null ? 0L : value;
    }
}
