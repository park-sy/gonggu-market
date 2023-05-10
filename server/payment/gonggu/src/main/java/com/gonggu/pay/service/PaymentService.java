package com.gonggu.pay.service;

import com.gonggu.pay.domain.Account;
import com.gonggu.pay.domain.Payment;
import com.gonggu.pay.domain.Transaction;
import com.gonggu.pay.domain.User;
import com.gonggu.pay.exception.PayChargeFailed;
import com.gonggu.pay.exception.PayRemitFailed;
import com.gonggu.pay.exception.PaymentNotFound;
import com.gonggu.pay.exception.UserNotFound;
import com.gonggu.pay.repository.AccountRepository;
import com.gonggu.pay.repository.PaymentRepository;
import com.gonggu.pay.repository.TransactionRepository;
import com.gonggu.pay.repository.UserRepository;
import com.gonggu.pay.request.PaymentCharge;
import com.gonggu.pay.request.RemitRequest;
import com.gonggu.pay.request.TransactionRequest;
import com.gonggu.pay.response.PaymentInfo;
import com.gonggu.pay.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PaymentInfo getInfo(User user) {
        return new PaymentInfo(paymentRepository.findByUser(user)
                .orElseThrow(() -> new PaymentNotFound("지갑을 생성해주세요.")));
    }

    public void charge(PaymentCharge paymentCharge, User user) {
        Account account = accountRepository.findByUser(user);
        if(account.getBalance() < paymentCharge.getRequestCoin()) throw new PayChargeFailed("충전에 실패하였습니다.");
        //거래 업데이트
        Payment payment = paymentRepository.findByUser(user).orElseThrow(PaymentNotFound::new);
        account.minusBalance(paymentCharge.getRequestCoin());
        payment.plusBalance(paymentCharge.getRequestCoin());
    }

    public void discharge(PaymentCharge paymentCharge, User user) {
        Payment payment = paymentRepository.findByUser(user).orElseThrow(PaymentNotFound::new);
        if(payment.getBalance() < paymentCharge.getRequestCoin()) throw new PayChargeFailed("인출에 실패하였습니다.");
        //거래 업데이트
        Account account = accountRepository.findByUser(user);
        payment.minusBalance(paymentCharge.getRequestCoin());
        account.plusBalance(paymentCharge.getRequestCoin());
    }

    public void remit(User from, RemitRequest request) {
        User to = userRepository.findByNickname(request.getTo()).orElseThrow(UserNotFound::new);
        Payment fromPayment = paymentRepository.findByUser(from).orElseThrow(PaymentNotFound::new);
        if(fromPayment.getBalance() < request.getAmount()) throw new PayRemitFailed();
        //송금 로직

        Payment toPayment = paymentRepository.findByUser(to).orElseThrow(PaymentNotFound::new);

        fromPayment.minusBalance(request.getAmount());
        toPayment.plusBalance(request.getAmount());

        //거래내역 생성
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .from(from)
                .to(to)
                .amount(request.getAmount())
                .date(now).build();
        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransaction(TransactionRequest transactionRequest, User user){
        return transactionRepository.getList(user, transactionRequest).stream()
                .map(TransactionResponse::new).collect(Collectors.toList());
    }

    public void createPayment(User user) {
        if(paymentRepository.findByUser(user).isPresent()){
            throw new PayChargeFailed("이미 생성된 지갑이 있습니다.");
        }
        Payment payment = Payment.builder()
                .user(user)
                .balance(0L).build();
        Account account = Account.builder()
                .user(user)
                .balance(100000L).build();
        paymentRepository.save(payment);
        accountRepository.save(account);
    }
}
