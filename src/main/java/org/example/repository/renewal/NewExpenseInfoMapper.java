package org.example.repository.renewal;

import org.apache.ibatis.annotations.Mapper;
import org.example.dto.renewal.NewExpenseInfoDto;

import java.util.List;

@Mapper
public interface NewExpenseInfoMapper {
    int insertNewExpenseList(List<NewExpenseInfoDto> expenseList);
}
