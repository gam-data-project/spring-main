package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.NewDeliveryFee;
import org.example.repository.NewDeliveryFeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewDeliveryFeeService {

    private final NewDeliveryFeeMapper newDeliveryFeeMapper ;

    @Transactional
    public int save(NewDeliveryFee fee){
        return newDeliveryFeeMapper.insertNewDeliveryFee(fee);
    }
}
