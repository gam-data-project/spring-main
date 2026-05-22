package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.product.response.ProductUpsertDto;

import java.time.LocalDate;

@Mapper
public interface ProductInfoMapper {
    int upsertAndReturnId(ProductUpsertDto productUpsertDto); // 호출 후 info.id 채워짐
    //Long getCategoryId(@Param("productId") Long productId);
    int deactivateProduct(@Param("date") LocalDate date);
}
