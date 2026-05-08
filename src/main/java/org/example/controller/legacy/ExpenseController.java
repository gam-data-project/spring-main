package org.example.controller.legacy;

import org.example.domain.Expense;
import org.example.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ExpenseController {
    @Autowired
    private ExpenseService expenseService;
    //비용 등록
    @PostMapping("/saveexpense")
    public void saveExpense(@RequestBody Expense expense){
        expenseService.saveExpense(expense);
    }
    //전체 목록 조회
    @GetMapping("/selectallexpense")
    public List<Expense> selectAllExpense(){
        return expenseService.selectAllExpense();
    }
    // 비용 검색(날짜로 검색)
    @GetMapping("/selectexpensebydate")
    public List<Expense> selectExpenseByDate(@RequestParam String inputdate){
        return expenseService.selectExpenseByDate(inputdate);
    }
    // 비용 수정(날짜, 소분류, 제품이름이 같으면 수량과 금액을 수정할 수 있음)
    @PostMapping("/updateexpense")
    public Boolean updateExpense(@RequestBody Expense expense){
        return expenseService.updateExpense(expense);
    }
    // 비용 삭제
    @DeleteMapping("/deleteexpense")
    public Boolean deleteExpense(@RequestBody Expense expense){
        return expenseService.deleteExpense(expense);
    }
}
