package com.gonggu.pay.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class TransactionRequest {

    private Integer order;
    private Integer filter;
    private String start;
    private String end;
}
