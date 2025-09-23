package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.repository.NewProductInfoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class NewProductDeactiveService {
    private final NewProductInfoMapper newProductInfoMapper;

    public int deactivateProduct(LocalDate date){
        // end_date로부터 3개월이 지나 'date'에 도달/초과한 행만 비활성화
        return newProductInfoMapper.deactivateProduct(date);
    }
}
