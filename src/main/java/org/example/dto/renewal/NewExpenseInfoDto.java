package org.example.dto.renewal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.example.domain.ExpenseType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewExpenseInfoDto {
    // PK
    private long id;

    // 비용 발생 일자
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    // 비용 발생 시간
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime expenseTime;

    // 카테고리 ID
    private long categoryId;

    // 비용 타입
    private ExpenseType expenseType;

    // 단가
    private Integer unitCost;

    // 수량
    private Integer quantity;

    // 총 비용
    private Integer totalCost;

    // 상세 설명
    private String description;

}
