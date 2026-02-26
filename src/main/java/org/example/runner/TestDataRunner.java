package org.example.runner;

import lombok.RequiredArgsConstructor;
import org.example.repository.TestCategoryMapper;
import org.example.service.TestCategoryService;
import org.example.service.TestMatchIdService;
import org.example.service.TestProductService;
import org.example.service.TestSalesService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataRunner implements CommandLineRunner {
    //private final TestSalesService testSalesService;
    //private final TestProductService testProductService;
    //private final TestCategoryService testCategoryService;
    private final TestMatchIdService testMatchIdService;

    @Override
    public void run(String... args) throws Exception {
//        testSalesService.loadData();
      //  testProductService.loadTestProducts();
        //testCategoryService.loadTestCategory();
        testMatchIdService.matchSalesProductIds();
    }

}
