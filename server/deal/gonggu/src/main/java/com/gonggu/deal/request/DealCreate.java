package com.gonggu.deal.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DealCreate {

    private String title;
    private String content;
    private Long price;
    private Integer unitQuantity;
    private String unit;
    private Integer nowCount;
    private Integer totalCount;
    private String url;
    private Long categoryId;
    private LocalDateTime expireTime;
    private List<String> keywords;
    private List<String> images;

}
