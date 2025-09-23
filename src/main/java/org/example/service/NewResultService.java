package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.repository.NewResultPartitionedMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class NewResultService {
    private final NewResultPartitionedMapper resultMapper;

    public int loadSales(LocalDate from, LocalDate to) {
        // 영향받은 row 수 반환
        return resultMapper.insertSalesIntoResult(from, to, "SALES");
    }

    public int loadDeliveryFee(LocalDate from, LocalDate to) {
        return resultMapper.insertDeliveryFeeIntoResult(from, to, "EXPENSE");
    }
}
