package org.example.dto.migration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 신규 매입 데이터 저장에 사용하는 DTO.
 *
 * 은행 입출금 엑셀에서 추출한 거래 정보를
 * 매입 데이터 형식으로 변환한 뒤 전달할 때 사용한다.
 *
 * category_id 기준으로 매입을 관리하며,
 * 과거 이관 데이터의 경우 수량은 1로 고정하고
 * 출금금액을 단가와 총금액으로 함께 사용한다.
 *
 * @since 2026.05.08
 */


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PurchaseMigrationDto {

    // PK
    private long id;

    // 상품 ID, 초기 저장 시 null 가능
    private Long productId;

    // 매입일자
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    // 매입시간
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime purchaseTime;

    // 매입 수량
    private Integer quantity;

    // 단가
    private Integer unitCost;

    // 총 매입 금액
    private Integer totalCost;

    // 거래처명
    private String supplierName;
}


