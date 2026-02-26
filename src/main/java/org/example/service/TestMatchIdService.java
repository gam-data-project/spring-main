package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.repository.TestCategoryMapper;
import org.example.repository.TestProductMapper;
import org.example.repository.TestSalesMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestMatchIdService {

    private final TestCategoryMapper testCategoryMapper;
    private final TestProductMapper testProductMapper;
    private final TestSalesMapper testSalesMapper;
    private final SqlSessionFactory sqlSessionFactory;

    /**
     * product.category_id 채우기
     * - category id가 연속적이지 않아도 OK (실존 id 목록에서만 선택)
     * - category가 더 적으면 순환 매핑
     * - 필요하면 shuffle로 분포 랜덤화 가능 (재현성 seed 적용)
     */
    public void matchProductCategoryIds() {
        List<Long> categoryIds = testCategoryMapper.selectAllCategoryIds();
        List<Long> productIds = testProductMapper.selectProductIdsWithoutCategory();

        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalStateException("test_category에 category id가 없습니다.");
        }
        if (productIds == null || productIds.isEmpty()) {
            return; // 채울 대상 없음
        }

        // 분포를 랜덤하게 하고 싶으면 아래 주석 해제 (재현성 유지)
        Collections.shuffle(categoryIds, new Random(42));

        int batchSize = 1000;

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestProductMapper batchProductMapper = session.getMapper(TestProductMapper.class);

            for (int i = 0; i < productIds.size(); i++) {
                long productId = productIds.get(i);
                long categoryId = categoryIds.get(i % categoryIds.size()); // 순환 매핑

                batchProductMapper.updateProductCategory(productId, categoryId);

                if ((i + 1) % batchSize == 0) {
                    session.flushStatements();
                }
            }

            session.flushStatements();
            session.commit();
        }
    }

    /**
     * sales.product_id 채우기
     * - product id가 연속적이지 않아도 OK (실존 id 목록에서만 선택)
     * - product가 더 적으면 순환 매핑
     */
    public void matchSalesProductIds() {
        List<Long> allProductIds = testProductMapper.selectAllProductIds();
        List<Long> salesIds = testSalesMapper.selectSalesIdsWithoutProduct();

        if (allProductIds == null || allProductIds.isEmpty()) {
            throw new IllegalStateException("test_product_info에 product id가 없습니다.");
        }
        if (salesIds == null || salesIds.isEmpty()) {
            return; // 채울 대상 없음
        }

        // 랜덤 분포 원하면 셔플 가능
        Collections.shuffle(allProductIds, new Random(42));

        int batchSize = 1000;

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestSalesMapper batchSalesMapper = session.getMapper(TestSalesMapper.class);

            for (int i = 0; i < salesIds.size(); i++) {
                long salesId = salesIds.get(i);
                long productId = allProductIds.get(i % allProductIds.size()); // 순환 매핑

                batchSalesMapper.updateSalesProduct(salesId, productId);

                if ((i + 1) % batchSize == 0) {
                    session.flushStatements();
                }
            }

            session.flushStatements();
            session.commit();
        }
    }

    /**
     * 한 번에 다 돌리고 싶을 때
     */
    public void matchAll() {
        matchProductCategoryIds();
        matchSalesProductIds();
    }
}
