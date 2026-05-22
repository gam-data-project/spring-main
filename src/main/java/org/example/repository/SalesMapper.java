package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.sales.request.SalesRequestDto;
import org.example.dto.sales.response.SalesMappingDto;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalesMapper {
    int insertNewSales(SalesRequestDto sales);
    SalesMappingDto findUnmappedSales();
    int updateProductIdMapping(@Param("salesId") Long salesId,
                      @Param("productId") Long productId);
    List<SalesMappingDto> findUnmappedBatchByDate(@Param("orderDate") LocalDate orderDate);

}
