package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.dto.testData.TestSales;
import org.example.dto.testData.TestSalesInsert;
import org.example.repository.TestSalesMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestSalesService {
    private final SqlSessionFactory sqlSessionFactory;
    private final TestSalesMapper testSalesMapper;

    public void loadData() {
        List<TestSales> samples = testSalesMapper.selectSalesTest(10000);
        List<String> orderNumbers = testSalesMapper.selectOrderNumbersTest();

        Random rnd = new Random(42); // 재현성 확보
        LocalDate start = LocalDate.of(2010, 1, 1);
        long days = ChronoUnit.DAYS.between(start, LocalDate.of(2020, 12, 31));

        int batchSize = 2000;

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            TestSalesMapper batchMapper = session.getMapper(TestSalesMapper.class);

            for (int i = 0; i < orderNumbers.size(); i++) {
                TestSales sample = samples.get(rnd.nextInt(samples.size()));

                TestSalesInsert ts = new TestSalesInsert();
                ts.setOrderNumber(orderNumbers.get(i));
                ts.setPlatform("nongra");
                ts.setProductNameRaw(sample.getProductNameRaw());
                ts.setQuantity(sample.getQuantity());
                ts.setUnitPrice(sample.getUnitPrice());
                ts.setProductTotal(sample.getProductTotal());
                ts.setShippingIncluded(sample.getShippingIncluded());

                LocalDate randomDate = start.plusDays(rnd.nextLong(days + 1));
                ts.setOrderDate(randomDate);

                batchMapper.insertTestSales(ts);

                if (i > 0 && i % batchSize == 0) {
                    session.flushStatements();
                    session.commit();
                }
            }

            session.flushStatements();
            session.commit();
        }
    }

}
