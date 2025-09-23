package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.domain.NewProductInfo;
import org.example.dto.newProduct.NewProductUpsertDto;

import java.time.LocalDate;

@Mapper
public interface NewProductInfoMapper {
    int upsertAndReturnId(NewProductUpsertDto productUpsertDto); // 호출 후 info.id 채워짐
    //Long getCategoryId(@Param("productId") Long productId);
    int deactivateProduct(@Param("date") LocalDate date);
}
