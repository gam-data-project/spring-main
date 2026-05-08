package org.example.controller.legacy;

import org.example.domain.Purchase;
import org.example.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PurchaseController {


    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/savepurchase")
    public void savePurchase(@RequestBody Purchase purchase) {
        purchaseService.save(purchase);
    }

    @GetMapping("/selectallpurchase")
    public List<Purchase> selectAll() {
        return purchaseService.selectAll();
    }

    @GetMapping("/selectdatepurchase")
    public List<Purchase> selectByDate(@RequestParam String inputdate) {
        return purchaseService.selectByDate(inputdate);
    }
    //날짜, 소분류, 제품이름이 같으면 수량과 금액을 수정할 수 있음(잘못 입력했을때 사용)
    @PostMapping("/updatepurchase")
    public Boolean updatePurchase(@RequestBody Purchase purchase) {
        return purchaseService.updatePurchase(purchase);
    }

    @DeleteMapping("/deletepurchase")
    public Boolean deletePurchase(@RequestBody Purchase purchase){
        return purchaseService.deletePurchase(purchase);
    }
}
