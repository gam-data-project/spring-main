package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface NewResultPartitionedMapper {
    int insertSalesIntoResult(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("type") String type);

    int insertDeliveryFeeIntoResult(@Param("from") LocalDate from,
                                    @Param("to") LocalDate to,
                                    @Param("type") String type);
}
