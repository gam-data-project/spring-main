package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.NewDeliveryFee;

@Mapper
public interface NewDeliveryFeeMapper {
    int insertNewDeliveryFee(NewDeliveryFee fee);
}
