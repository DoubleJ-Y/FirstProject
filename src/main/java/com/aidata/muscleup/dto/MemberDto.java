package com.aidata.muscleup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//or @Data 사용
public class MemberDto {
    private String m_id;
    private String m_sex;
    private String m_pwd;
    private String m_name;
    private String m_birth;
    private String m_addr;
    private String m_phone;
    private String m_height;
    private String my_weight;
    private String goal_weight;
}
