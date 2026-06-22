package org.example.service.delivery;

import lombok.RequiredArgsConstructor;
import org.example.dto.delivery.request.DeliveryFeeRowDto;
import org.example.repository.DeliveryFeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SingleDeliveryFeeService {

    private final DeliveryFeeMapper deliveryFeeMapper;

    @Transactional
    public int save(DeliveryFeeRowDto fee){
        return deliveryFeeMapper.insertNewDeliveryFee(fee);
    }
}
