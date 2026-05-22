package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.legacy.NewDeliveryFee;

@Mapper
public interface DeliveryFeeMapper {
    int insertNewDeliveryFee(NewDeliveryFee fee);
}
