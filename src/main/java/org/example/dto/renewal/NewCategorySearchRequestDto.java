package org.example.dto.renewal;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * 카테고리 목록 조회(페이징) 요청 DTO.
 * 대/중/소 분류 필터와 페이지 정보를 받는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewCategorySearchRequestDto {
    /** 대분류 필터 (ALL/전체면 조건 제외) */
    private String largeCategory;

    /** 중분류 필터 (ALL/전체면 조건 제외) */
    private String mediumCategory;

    /** 소분류 필터 (ALL/전체면 조건 제외) */
    private String smallCategory;

    /** 페이지 번호(1-base) */
    private Integer page;

    /** 페이지 크기 */
    private Integer size;

    /**
     * 유효한 페이지 번호를 반환한다.
     *
     * @return 최소 1 보장 페이지 번호
     */
    public int getPageOrDefault() {
        return (page == null || page < 1) ? 1 : page;
    }

    /**
     * 유효한 페이지 크기를 반환한다.
     *
     * @return 기본 10, 최대 10으로 고정된 페이지 크기
     */
    public int getSizeOrDefault() {
        if (size == null || size < 1) return 10;
        return Math.min(size, 10);
    }

    /**
     * DB OFFSET 값을 계산한다.
     *
     * @return offset
     */
    public int getOffset() {
        return (getPageOrDefault() - 1) * getSizeOrDefault();
    }

    /**
     * DB LIMIT 값을 반환한다.
     *
     * @return limit
     */
    public int getLimit() {
        return getSizeOrDefault();
    }
}
