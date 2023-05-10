package com.gonggu.pay.controller;

import com.gonggu.pay.domain.User;
import com.gonggu.pay.request.PaymentCharge;
import com.gonggu.pay.request.RemitRequest;
import com.gonggu.pay.request.TransactionRequest;
import com.gonggu.pay.response.PaymentInfo;
import com.gonggu.pay.response.TransactionResponse;
import com.gonggu.pay.service.KafkaProducer;
import com.gonggu.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final KafkaProducer kafkaProducer;
    //페이 정보 불러오기
    @GetMapping("/payment")
    public PaymentInfo getInfo(@AuthenticationPrincipal User user){
        return paymentService.getInfo(user);
    }
    //코인 충전
    @PostMapping("/payment/charge")
    public void chargeCoin(@AuthenticationPrincipal User user, @RequestBody PaymentCharge paymentCharge){
        paymentService.charge(paymentCharge, user);
    }
    //코인 반환
    @PostMapping("/payment/discharge")
    public void dischargeCoin(@AuthenticationPrincipal User user, @RequestBody PaymentCharge paymentCharge){
        paymentService.discharge(paymentCharge, user);
    }
    //코인 송금
    @PostMapping("/payment/remit")
    public void remit(@AuthenticationPrincipal User user,@RequestBody RemitRequest request){
        paymentService.remit(user,request);
        kafkaProducer.sendPushRemitInfo("remitCreate", user, request.getAmount());
    }
    //거래내역 조회
    @GetMapping("/payment/transaction")
    public List<TransactionResponse> getMyTransaction(@AuthenticationPrincipal User user,
                                                      @ModelAttribute TransactionRequest transactionRequest){
        return paymentService.getMyTransaction(transactionRequest, user);
    }
    @PostMapping("/payment")
    public void createPayment(@AuthenticationPrincipal User user){
        paymentService.createPayment(user);
    }
    @GetMapping("/hello")
    public String hello(){
        return "hello world";
    }
}
