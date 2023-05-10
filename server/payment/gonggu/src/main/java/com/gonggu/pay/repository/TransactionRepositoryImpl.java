package com.gonggu.pay.repository;

import com.gonggu.pay.domain.QTransaction;
import com.gonggu.pay.domain.Transaction;
import com.gonggu.pay.domain.User;
import com.gonggu.pay.request.TransactionRequest;
import com.gonggu.pay.response.TransactionResponse;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.gonggu.pay.domain.QTransaction.transaction;

@RequiredArgsConstructor
@ToString
public class TransactionRepositoryImpl implements TransactionRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<Transaction> getList(User user, TransactionRequest request){
        return jpaQueryFactory.selectFrom(transaction)
                .where(
                        filter(user, request.getFilter()),
                        goeDate(request.getStart()),
                        loeDate(request.getEnd())
                )
                .orderBy(sortOrder(request.getOrder()))
                .fetch();
    }

    private OrderSpecifier<?> sortOrder(Integer order){
        if(order == null) {
            return transaction.id.desc();
        } else if(order == 1) {
            return transaction.amount.desc();
        }
        return transaction.id.desc();
    }
    private BooleanExpression filter(User user, Integer filter){
        if(filter == null) {
            return transaction.from.eq(user).or(transaction.to.eq(user));
        } else if(filter == 1) {
            return transaction.from.eq(user);
        }
        return transaction.to.eq(user);
    }
    private BooleanExpression goeDate(String date){
        if(date == null) {
            return null;
        }
        LocalDateTime start = LocalDateTime.parse(date);
        return transaction.date.goe(start);
    }
    private BooleanExpression loeDate(String date){
        if(date == null) {
            return null;
        }
        LocalDateTime end = LocalDateTime.parse(date);
        return transaction.date.loe(end);
    }
}
