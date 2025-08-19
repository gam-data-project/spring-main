package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.NewSales;

@Mapper
public interface NewSalesMapper {
    int insertNewSales(NewSales sales);
}
