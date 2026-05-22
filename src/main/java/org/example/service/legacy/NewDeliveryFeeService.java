package org.example.service.legacy;

import lombok.RequiredArgsConstructor;
import org.example.domain.legacy.NewDeliveryFee;
import org.example.repository.DeliveryFeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewDeliveryFeeService {

    private final DeliveryFeeMapper deliveryFeeMapper;

    @Transactional
    public int save(NewDeliveryFee fee){
        return deliveryFeeMapper.insertNewDeliveryFee(fee);
    }
}
