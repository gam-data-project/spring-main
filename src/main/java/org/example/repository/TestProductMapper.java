package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.testData.TestProductGet;
import org.example.dto.testData.TestProductInsert;

import java.util.List;

@Mapper
public interface TestProductMapper {
    List<TestProductGet> selectProductTest();
    void insertProductTest(TestProductInsert testProductInsert);

    List<Long> selectProductIdsWithoutCategory();
    void updateProductCategory(@Param("productId") long productId,
                               @Param("categoryId") long categoryId);

    List<Long> selectAllProductIds();
}
