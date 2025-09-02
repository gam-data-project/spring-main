package org.example.dto.newProduct;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.domain.Platform;

import java.time.LocalDate;

/**
 * @author ARa
 *
 * product 테이블을 업서트 하기 위한 Dto
 * 조회한 sales 테이블과 매핑 가능한 product 테이블 요소
 *
 */
@Getter
@Setter
@Builder
public class NewProductUpsertDto {
    // sales에 매핑할 id 값(업서트 시 리턴한 값)
    private Long id;
    // UK
    private String productName;
    // UK
    private Integer unitPrice;
    // UK
    private Platform platform;
    // 택배비 포함된 제품인지
    private Boolean shippingIncluded;
    // 첫 판매 시작일
    private LocalDate startDate;
    // 마지막 판매일(order_date 업데이트)
    private LocalDate endDate;
}
