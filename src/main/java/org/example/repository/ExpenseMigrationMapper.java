package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.dto.migration.ExpenseMigrationDto;

import java.util.List;

@Mapper
public interface ExpenseMigrationMapper {
    int insertNewExpenseList(List<ExpenseMigrationDto> expenseList);
}
