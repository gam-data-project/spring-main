package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.dto.migration.PurchaseMigrationDto;

import java.util.List;

@Mapper
public interface PurchaseMigrationMapper {
    /**
     * 신규 매입 데이터 목록을 new_purchase_info 테이블에 일괄 저장한다.
     *
     * MyBatis foreach 구문을 사용하여 전달된 목록을 한 번의 INSERT 문으로 처리한다.
     *
     * @param purchaseList 저장할 신규 매입 데이터 목록
     * @return 저장된 행 수
     */
    int insertNewPurchaseList(List<PurchaseMigrationDto> purchaseList);
}
