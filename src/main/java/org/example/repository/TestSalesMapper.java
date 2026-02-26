package org.example.repository;

import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.example.dto.testData.TestSales;
import org.example.dto.testData.TestSalesInsert;

import java.util.List;


@Mapper
public interface TestSalesMapper {
    // 1만개 샘플 가져오기
    List<TestSales> selectSalesTest(@Param("limit") int limit);

    // 10만개 order_number 가져오기
    List<String> selectOrderNumbersTest();  // String으로 바로 받아도 됨

    void insertTestSales(TestSalesInsert testSalesInsert);


    List<Long> selectSalesIdsWithoutProduct();
    void updateSalesProduct(@Param("salesId") long salesId,
                            @Param("productId") long productId);
}
