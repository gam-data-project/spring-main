package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.dto.testData.TestProductGet;
import org.example.dto.testData.TestProductInsert;
import org.example.repository.TestProductMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestProductService {
    private final TestProductMapper mapper;
    private final SqlSessionFactory sqlSessionFactory;

    public void loadTestProducts() {

        List<TestProductGet> samples = mapper.selectProductTest();

        Random rnd = new Random(42);
        LocalDate start = LocalDate.of(2010,1,1);
        long days = ChronoUnit.DAYS.between(start, LocalDate.of(2020,12,31));

        try (SqlSession session =
                     sqlSessionFactory.openSession(ExecutorType.BATCH)) {

            TestProductMapper batchMapper =
                    session.getMapper(TestProductMapper.class);

            int batchSize = 1000;
            int index = 1;
            int total = 70_000;
            for (int i = 0; i < total; i++) {
                TestProductGet sample = samples.get(i % samples.size());

                TestProductInsert tp = new TestProductInsert();
                tp.setProductName(sample.getProductName() + "_" + index++);
                tp.setUnitPrice(sample.getUnitPrice());
                tp.setUnitCnt(sample.getUnitCnt());
                tp.setShippingIncluded(sample.getShippingIncluded());
                tp.setPlatform("SMARTSTORE");
                tp.setActive(null);

                tp.setStartDate(start.plusDays(rnd.nextInt((int) days)));
                tp.setEndDate(start.plusDays(rnd.nextInt((int) days)));

                batchMapper.insertProductTest(tp);

                if (i % batchSize == 0) {
                    session.flushStatements();
                }
            }
            session.commit();
        }
    }
}
