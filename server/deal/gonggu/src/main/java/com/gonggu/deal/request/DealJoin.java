package com.gonggu.deal.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

@Getter
@Builder
public class DealJoin {

    private Integer quantity;

    @JsonCreator
    public DealJoin(Integer quantity){
        this.quantity = quantity;
    }
}
