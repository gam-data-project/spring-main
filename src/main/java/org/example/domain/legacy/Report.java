package org.example.domain.legacy;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Report {
    //정산달
    String date;
    //대분류
    String largeCategory;
    //중분류
    String mediumCategory;
    //소분류
    String smallCategory;
    //매출 총금액
    Integer sumSales;
    //매입 총금액
    Integer sumPurchase;
    //비용 총금액
    Integer sumExpense;
    //총 이익금
    Integer profit;
}
