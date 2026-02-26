package org.example.service;


import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.dto.testData.TestCategoryGet;
import org.example.dto.testData.TestCategoryInsert;
import org.example.repository.TestCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestCategoryService {
    private final TestCategoryMapper testCategoryMapper;
    private final SqlSessionFactory sqlSessionFactory;

    public void loadTestCategory() {

        List<TestCategoryGet> samples = testCategoryMapper.selectCategoryTest();
        if (samples == null || samples.isEmpty()) {
            throw new IllegalStateException("new_category에 샘플 데이터가 없습니다.");
        }

        int total = 40_000;
        int batchSize = 1000;
        int u = 1;
        Random rnd = new Random(42); // 재현성 확보

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {

            TestCategoryMapper batchMapper = session.getMapper(TestCategoryMapper.class);

            for (int i = 1; i <= total; i++) {
                TestCategoryGet sample = samples.get(rnd.nextInt(samples.size()));

                TestCategoryInsert tc = new TestCategoryInsert();
                tc.setLargeCategory(sample.getLargeCategory());
                tc.setMediumCategory(sample.getMediumCategory());
                tc.setSmallCategory(sample.getSmallCategory() + "_" + u++);

                batchMapper.insertCategoryTest(tc);

                if (i % batchSize == 0) {
                    session.flushStatements();
                }
            }

            session.commit();
        }
    }
}
