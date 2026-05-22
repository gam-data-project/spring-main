package org.example.dto.expense.request;


import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 엑셀 비용 조회(페이지) 요청 DTO
 */
@Getter
@Setter
public class NewExpenseExcelSearchPageRequestDto {
    /** 시작일(없으면 하한 없음) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 종료일(없으면 상한 없음) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 비용 발생처 키워드(부분 검색) */
    private String description;

    /** 페이지 번호(1-base) */
    private Integer page;

    /**
     * 유효한 페이지 번호를 반환한다.
     *
     * @return page가 null/1 미만이면 1, 아니면 입력값
     */
    public int getPageOrDefault() {
        return (page == null || page < 1) ? 1 : page;
    }
}
