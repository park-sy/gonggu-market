package com.gonggu.pay.request;

import com.gonggu.pay.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class RemitRequest {

    private String to;
    private Long amount;
}
