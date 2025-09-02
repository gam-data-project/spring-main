package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.example.domain.NewSales;
import org.example.repository.NewSalesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewSalesService {

    private final NewSalesMapper newSalesMapper;

    @Transactional
    public int save(NewSales sales) {
        return newSalesMapper.insertNewSales(sales);

    }
}
