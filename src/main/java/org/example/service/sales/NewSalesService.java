package org.example.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.dto.sales.request.SalesRequestDto;
import org.example.repository.SalesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewSalesService {

    private final SalesMapper salesMapper;

    @Transactional
    public int save(SalesRequestDto sales) {
        return salesMapper.insertNewSales(sales);

    }
}
