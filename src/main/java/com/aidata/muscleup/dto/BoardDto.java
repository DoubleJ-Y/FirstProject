package com.aidata.muscleup.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BoardDto {
    private int b_num;
    private String b_title;
    private String b_contents;
    private String b_id;
    private Timestamp b_date;
    private int b_views;
    private String m_name;
}
