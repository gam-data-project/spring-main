package org.example.controller.legacy;

import org.example.domain.Category;
import org.example.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    //카테고리 입력
    @PostMapping("/savecategory")
    public void saveCategory(@RequestBody Category category){
        categoryService.saveCategory(category);
    }
    //카테고리 전체 리스트 조회
    @GetMapping("/selectallcategory")
    public List<Category> selectAllCategory(){
        return categoryService.selectAllCategory();
    }
    //중분류로 소분류 카테고리 리스트 조회
    @GetMapping("/selectbycategory")
    public List<Category> selectByCategory(@RequestParam String mediumdata){
        System.out.println(mediumdata);
        return categoryService.selectByCategory(mediumdata);
    }
    //카테고리클래스의 제품 정보를 수정
    //원본 제품 정보와 수정 제품 정보를 입력받음 (JSON을 MAP타입으로 받아서 Category 타입으로 변환)
    //원본 제품의 거래완료날짜 자동 수정되고 수정제품정보 새로 등록함
    @PostMapping("/updatecategory")
    public Boolean updateCategory(@RequestBody Map<String, Object> object){
        Category origin = new Category((Map<String, Object>) object.get("origin"));
        Category modify = new Category((Map<String, Object>) object.get("modify"));
        System.out.println(origin.toString());
        System.out.println(modify.toString());
        return categoryService.updateCategory(origin,modify);
        //return categoryService.updateCategory(category);
    }

    @DeleteMapping("/deletecategory")
    public Boolean deleteCategory(@RequestBody Category category){
        return categoryService.deleteCategory(category);
    }
}
