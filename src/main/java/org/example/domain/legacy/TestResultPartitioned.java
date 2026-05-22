package org.example.domain.legacy;


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
public class TestResultPartitioned {
    private long id;
    // 테이블 타입(매출/구매/비용)
    private SourceType type;
    // 원천테이블의 id
    private long sourceId;
    // 카테고리 스냅샷
    private String largeCategory;
    private String mediumCategory;
    private String smallCategory;
    //이름(표시용): 매출/구매=제품명, 비용=비용유형
    private String productName;
    //수치
    private Integer quantity;
    private Integer unitPrice;
    private Integer total;
    // 데이터 발생일
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

}
