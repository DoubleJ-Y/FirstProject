package com.aidata.muscleup.dao;

import com.aidata.muscleup.dto.BoardDto;
import com.aidata.muscleup.dto.BoardFileDto;
import com.aidata.muscleup.dto.ReplyDto;
import com.aidata.muscleup.dto.SearchDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardDao {

    List<BoardDto> selectBoardList(SearchDto sdto);

    //전체 개시글 개수 구하는메소드
    int selectBoardCnt(SearchDto sdto);

    //게시글 저장
    void insertBoard(BoardDto board);

    //파일 정보 저장
    void insertFile(BoardFileDto bfDto);

    //게시글 하나만 가져오는 메소드
    BoardDto selectBoard(int b_num);

    //게시글 번호에 해당하는 파일목록을 가져온는 메소드
    List<BoardFileDto> selectFileList(int b_num);

    //파일의 저장 이름 목록 구하는 메소드
    List<String> selectFnameList(int b_num);

    //게시글 번호에 해당되는 파일목록 삭제 메소드
    void deleteFiles(int b_num);

    //게시글 번호에 해당되는 댓글목록 삭제 메소드
    void deleteReplays(int b_num);

    //게시글 번호에 해당되는 게시글 삭제 메소드
    void deleteBoard(int b_num);

    //수정 시 단독 파일 삭제
    void deleteFile(String sysname);

    //게시글 수정 메소드
    void updateBoard(BoardDto board);

    //댓글 저장(작성 메소드)
    void insertReply(ReplyDto reply);

    //최신 댓글정보 가져오는 메소드
    ReplyDto selectLastReply(int rNum);

    //게시글 번호에 해당하는 댓글목록을 가져오는 메소드
    List<ReplyDto> selectReplyList(int b_num);

    //조회수 업데이트
    void updateViews(BoardDto bdto);
}
