package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.delivery.request.DeliveryFeeRowDto;

import java.util.List;

@Mapper
public interface DeliveryFeeMapper {
    int insertNewDeliveryFee(DeliveryFeeRowDto fee);

    int insertDeliveryFeeList(@Param("list") List<DeliveryFeeRowDto> deliveryFeeList);
}
