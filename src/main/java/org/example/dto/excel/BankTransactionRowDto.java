package org.example.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


/**
 *은행 입출금 엑셀 파일의 한 행을 읽어 담는 DTO.
 *
 * 실제 DB 테이블과 바로 매핑되는 도메인 객체가 아니라,
 * Apache POI로 추출한 원본 거래 데이터를 서비스 계층으로 전달하기 위한 용도로 사용한다.
 *
 * 이후 매입/비용 분류 규칙에 따라 NewPurchaseInfo 또는 NewExpenseInfo로 변환된다.
 *
 * @since 2026.05.07
 */


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionRowDto {

    // 거래일자 (B열)
    private LocalDate transactionDate;

    // 시간 (C열)
    private LocalTime transactionTime;

    // 찾으신금액 (D열)
    private Integer withdrawAmount;

    // 기록사항 (H열)
    private String recipientName;
}
