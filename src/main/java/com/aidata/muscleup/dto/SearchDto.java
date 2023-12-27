package com.aidata.muscleup.dto;

import lombok.Data;

@Data
public class SearchDto {
    private String colname;
    private String keyword;
    private int pageNum;
    private int listCnt;
}
