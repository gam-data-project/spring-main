package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.NewProductInfo;
import org.example.dto.newProduct.NewProductUpsertDto;
import org.example.dto.newSales.SalesMappingDto;
import org.example.repository.NewProductInfoMapper;
import org.example.repository.NewSalesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewProductMappingService {

    private final NewSalesMapper salesMapper;
    private final NewProductInfoMapper productMapper;

    @Transactional
    public boolean mapOne(){
        SalesMappingDto salesMappingDto = salesMapper.findUnmappedSales();
        if(salesMappingDto == null) return false;

        NewProductUpsertDto productUpsertDto = NewProductUpsertDto.builder()
                .productName(salesMappingDto.getProductNameRaw())
                .unitPrice(salesMappingDto.getUnitPrice())
                .platform(salesMappingDto.getPlatform())
                .shippingIncluded(salesMappingDto.getShippingIncluded())
                .startDate(salesMappingDto.getOrderDate())
                .endDate(salesMappingDto.getOrderDate())
                .build();

        // Upsert 실행 product_id 세팅됨
        productMapper.upsertAndReturnId(productUpsertDto);

        // category_id 조회 후 sales 매핑
        salesMapper.updateProductIdMapping(salesMappingDto.getId(), productUpsertDto.getId());
        return true;
    }

    public int backfillByDateRange(LocalDate from, LocalDate to) {
        int total = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            int done = backfillOneDate(d);
            total += done;
            // 필요하면 로그
            // log.info("date={} processed={} (cum={})", d, done, total);
        }
        return total;
    }

    public int backfillOneDate(LocalDate orderDate) {
        //long lastId = 0L;
        int processed = 0;

        //while (true) {
        List<SalesMappingDto> batch =
                salesMapper.findUnmappedBatchByDate(orderDate);
        if (batch.isEmpty()) return 0;

        processed += mapBatchTransactional(batch);
            //lastId = batch.get(batch.size() - 1).getId(); // 키셋 포인터 이동
        //}
        return processed;
    }

    @Transactional
    protected int mapBatchTransactional(List<SalesMappingDto> batch) {
        int cnt = 0;
        for (SalesMappingDto s : batch) {
            NewProductUpsertDto upsert = NewProductUpsertDto.builder()
                    .productName(s.getProductNameRaw())
                    .unitPrice(s.getUnitPrice())
                    .platform(s.getPlatform())
                    .shippingIncluded(s.getShippingIncluded())
                    .startDate(s.getOrderDate())  // 최초 생성 시만 의미, 중복시 미갱신
                    .endDate(s.getOrderDate())    // 최신 판매일로 갱신
                    .build();

            productMapper.upsertAndReturnId(upsert);        // upsert.id ← product_id
            salesMapper.updateProductIdMapping(s.getId(), upsert.getId());
            cnt++;
        }
        return cnt;
    }

}
