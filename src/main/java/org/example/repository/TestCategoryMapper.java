package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.dto.testData.TestCategoryGet;
import org.example.dto.testData.TestCategoryInsert;

import java.util.List;

@Mapper
public interface TestCategoryMapper {
    List<TestCategoryGet> selectCategoryTest();
    void insertCategoryTest(TestCategoryInsert testCategoryInsert);

    List<Long> selectAllCategoryIds();
}
