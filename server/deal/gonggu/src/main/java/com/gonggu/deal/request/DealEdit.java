package com.gonggu.deal.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
public class DealEdit {

    private String content;
    private List<String> keywords;
    private List<String> images;
//    @JsonCreator
//    public DealEdit(String content){
//        this.content = content;
//    }
}
