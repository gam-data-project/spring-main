package org.example.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewExpenseInfo {
    private long id;
    // 비용 발생 날짜
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;
    //카테고리 id
    private long categoryId;
    //비용타입
    private ExpenseType expenseType;
    //단가
    private Integer unitCost;
    //비용수량
    private Integer quantity;
    //총 비용
    private Integer totalCost;
    //상세설명
    private String description;

}
