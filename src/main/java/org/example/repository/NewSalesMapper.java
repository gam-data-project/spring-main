package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.domain.NewSales;
import org.example.dto.newSales.SalesMappingDto;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface NewSalesMapper {
    int insertNewSales(NewSales sales);
    SalesMappingDto findUnmappedSales();
    int updateProductIdMapping(@Param("salesId") Long salesId,
                      @Param("productId") Long productId);
    List<SalesMappingDto> findUnmappedBatchByDate(@Param("orderDate") LocalDate orderDate);

}
