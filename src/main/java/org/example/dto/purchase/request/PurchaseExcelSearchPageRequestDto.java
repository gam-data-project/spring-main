package org.example.dto.purchase.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 엑셀 매입 조회(페이지) 요청 DTO
 */
@Getter
@Setter
public class PurchaseExcelSearchPageRequestDto {
    /** 시작일 (없으면 하한 없음) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 종료일 (없으면 상한 없음) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 매입처 키워드 (부분일치) */
    private String supplierName;

    /** 페이지 번호(1-base) */
    private Integer page;

    /**
     * 페이지 기본값 보정
     * @return page가 null/0이하면 1, 아니면 입력값
     */
    public int getPageOrDefault() {
        return (page == null || page < 1) ? 1 : page;
    }


}


