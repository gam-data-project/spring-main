package org.example.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewCategory {
    private long id;
    //대분류
    private String largeCategory;
    //중분류
    private String mediumCategory;
    //소분류
    private String smallCategory;
}
