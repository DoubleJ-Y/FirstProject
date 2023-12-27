package com.aidata.muscleup.dao;

import com.aidata.muscleup.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberDao {
    //아이디 중복체크 확인용 (select)
    int checkid (String mid);

    //회원정보 저장용
    void insertMember (MemberDto member);

    //로그인시 비밀번호 가져오기
    String findPassword(String mid);

    //로그인 성공시 회원 정보를 가져오는 메서드
    MemberDto selectMember (String mid);

    //회원정보 업데이트
    void updateMember (MemberDto member);


}
