package com.aidata.muscleup.service;

import com.aidata.muscleup.dao.BoardDao;
import com.aidata.muscleup.dao.MemberDao;
import com.aidata.muscleup.dto.*;
import com.aidata.muscleup.util.PagingUtil;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@Service
@Slf4j
public class BoardService {
    @Autowired
    private MemberDao mDao;
    @Autowired
    private BoardDao bDao;

    //트랜잭션 객체
    @Autowired
    private PlatformTransactionManager manager;
    @Autowired
    private TransactionDefinition definition;

    private int listcnt = 5; //한 페이지에 보여질 글 개수

    public ModelAndView getBoardList(SearchDto sdto,
                                     HttpSession session) {
        log.info("getBoardList()");
        ModelAndView mv = new ModelAndView();

        //DB에서 글 가져오기
        int num = sdto.getPageNum();

        if (num == 0) {
            num = 1;
        }
        if (sdto.getListCnt() == 0) {
            sdto.setListCnt(listcnt);
        }

        //pageNum을 LIMIT 시작 번호로 변경 (BoardDao)
        sdto.setPageNum((num - 1) * sdto.getListCnt());
        List<BoardDto> bList = bDao.selectBoardList(sdto);
        //DB에서 가져온 데이터를 mv에 담기
        mv.addObject("bList", bList);

        //페이징 처리
        sdto.setPageNum(num);
        String pageHtml = getPaging(sdto);
        mv.addObject("paging", pageHtml);

        //페이지 번호와 검색 내용을 세션에 저장
        if (sdto.getColname() != null) {
            session.setAttribute("sdto", sdto);
        } else {
            session.removeAttribute("sdto"); //검색이 아닐때는 제거
        }

        //페이지 번호 저장
        session.setAttribute("pageNum", num);

        mv.setViewName("boardList");
        return mv;
    }

    private String getPaging(SearchDto sdto) {
        String pageHtml = null;

        //전체 글개수 확인(DB)
        int maxNum = bDao.selectBoardCnt(sdto);
        //페이지에 보여질 번호 개수
        int pageCnt = 5;
        String listName = "boardList?";
        if (sdto.getColname() != null) {
            listName += "colname=" + sdto.getColname() + "&keyword=" + sdto.getKeyword() + "&";
        }
        PagingUtil paging = new PagingUtil(
                maxNum,
                sdto.getPageNum(),
                sdto.getListCnt(),
                pageCnt,
                listName
        );
        pageHtml = paging.makePaging();

        return pageHtml;
    }

    public String boardWrite(List<MultipartFile> files,
                             BoardDto board,
                             HttpSession session,
                             RedirectAttributes rttr) {
        log.info("boardWrite()");

        TransactionStatus status = manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            bDao.insertBoard(board);

            fileUpload(files, session, board.getB_num());

            manager.commit(status);//최종 승인
            view = "redirect:boardList?pageNum=1";
            msg = "작성 성공";
        } catch (Exception e) {
            e.printStackTrace();
            manager.rollback(status); //취소
            view = "redirect:writeForm";
            msg = "작성 실패";
        }
        rttr.addFlashAttribute("msg", msg);

        return view;
    }

    private void fileUpload(List<MultipartFile> files,
                            HttpSession session,
                            int bNum) throws Exception {
        log.info("fileUpload()");

        String realPath = session.getServletContext().getRealPath("/");

        log.info(realPath);
        realPath += "upload/";

        File folder = new File(realPath);
        if (folder.isDirectory() == false) {
            folder.mkdir();
        }

        for (MultipartFile mf : files) {
            String orignname = mf.getOriginalFilename();
            if (orignname.equals("")) {
                return; //파일없음 작업종료
            }

            BoardFileDto bfDto = new BoardFileDto();
            bfDto.setBf_bnum(bNum);//게시글번호 저장.
            bfDto.setBf_oriname(orignname);//원래 파일명 저장.
            String sysname = System.currentTimeMillis() + orignname.substring(orignname.lastIndexOf("."));

            bfDto.setBf_sysname(sysname);

            File file = new File(realPath + sysname);
            mf.transferTo(file);

            bDao.insertFile(bfDto);
        }
    }

    public ModelAndView getBoard(int b_num, HttpSession session, BoardDto bdto) {
        log.info("getBoard");
        ModelAndView mv = new ModelAndView();

        //조회수 증가
        MemberDto member = (MemberDto) session.getAttribute("member");
        int views = bdto.getB_views();
        if (member.getM_id().equals(bdto.getB_id())) {
            views += 0;
            bdto.setB_views(views);
            bDao.updateViews(bdto);
        } else {
            views++;
            bdto.setB_views(views);
            bDao.updateViews(bdto);
        }
        bdto = bDao.selectBoard(bdto.getB_num());
        session.setAttribute("board", bdto);

        //게시글 번호로 선택한 게시물 가져오기
        BoardDto board = bDao.selectBoard(b_num);
        mv.addObject("board", board);

        //게시글 댓글목록 가져오기
        List<ReplyDto> rList = bDao.selectReplyList(b_num);
        mv.addObject("rList", rList);

        //파일목록 가져오기
        List<BoardFileDto> bfList = bDao.selectFileList(b_num);
        mv.addObject("bfList", bfList);

        mv.setViewName("boardDetail");

        return mv;
    }

    public ResponseEntity<Resource> fileDownload(BoardFileDto bfile,
                                                 HttpSession session) throws IOException {
        log.info("fileDownload()");

        String realPath = session.getServletContext().getRealPath("/");

        realPath += "upload/" + bfile.getBf_sysname();

        InputStreamResource fResource = new InputStreamResource(new FileInputStream(realPath));

        String fileName = URLEncoder.encode(bfile.getBf_oriname(), "UTF-8");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(fResource);
    }

    //게시글 삭제(파일목록+파일, 댓글목록 함께 삭제)
    public String deleteBoard(int b_num,
                              HttpSession session,
                              RedirectAttributes rttr) {
        log.info("deleteBoard()");

        //트랜젝션
        TransactionStatus status =
                manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            //0. 파일 삭제 목록 구하기
            List<String> fList = bDao.selectFnameList(b_num);

            //1. 파일목록 삭제
            bDao.deleteFiles(b_num);
            //1. 댓글목록 삭제
            bDao.deleteReplays(b_num);
            //2. 게시글 삭제
            bDao.deleteBoard(b_num);

            //파일 삭제 처리
            if (fList.size() != 0) {
                deleteFiles(fList, session);
            }

            manager.commit(status);

            view = "redirect:boardList?pageNum=1";
            msg = "삭제 성공";
        } catch (Exception e) {
            e.printStackTrace();

            manager.rollback(status);

            view = "redirect:boardDetail?b_num=" + b_num;
            msg = "삭제 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    private void deleteFiles(List<String> fList,
                             HttpSession session)
            throws Exception {
        log.info("deleteFiles()");
        //파일 위치
        String realPath = session.getServletContext()
                .getRealPath("/");
        realPath += "upload/";

        for (String sn : fList) {
            File file = new File(realPath + sn);
            if (file.exists() == true) {//파일 존재 확인 후
                file.delete();//파일 삭제
            }
        }
    }

    public ModelAndView updateBoard(int b_num) {
        log.info("updateBoard()");
        ModelAndView mv = new ModelAndView();
        //게시글 내용 가져오기
        BoardDto board = bDao.selectBoard(b_num);
        //파일목록 가져오기
        List<BoardFileDto> fList = bDao.selectFileList(b_num);
        //mv에 담기
        mv.addObject("board", board);
        mv.addObject("fList", fList);
        //템플릿 지정.
        mv.setViewName("updateForm");
        return mv;
    }

    public List<BoardFileDto> delFile(BoardFileDto bFile,
                                      HttpSession session) {
        log.info("delFile()");
        List<BoardFileDto> fList = null;

        //파일 경로 설정.
        String realPath = session.getServletContext().getRealPath("/");
        realPath += "upload/" + bFile.getBf_sysname();
        try {
            //파일 삭제
            File file = new File(realPath);
            if (file.exists()) {
                if (file.delete()) {
                    //해당 파일 정보 삭제(DB)
                    bDao.deleteFile(bFile.getBf_sysname());
                    //나머지 파일 목록 다시 가져오기
                    fList = bDao.selectFileList(bFile.getBf_bnum());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fList;
    }

    public String updateBoard(List<MultipartFile> files,
                              BoardDto board,
                              HttpSession session,
                              RedirectAttributes rttr) {
        log.info("updateBoard()");

        TransactionStatus status = manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            bDao.updateBoard(board);
            fileUpload(files, session, board.getB_num());

            manager.commit(status);
            view = "redirect:boardDetail?b_num=" + board.getB_num();
            msg = "수정 성공";
        } catch (Exception e) {
            e.printStackTrace();
            manager.rollback(status);
            view = "redirect:updateForm?b_num=" + board.getB_num();
            msg = "수정 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    public ReplyDto replyInsert(ReplyDto reply) {
        log.info("replyInsert");

        try {
            bDao.insertReply(reply);
            reply = bDao.selectLastReply(reply.getR_num());
        } catch (Exception e) {
            e.printStackTrace();
            reply = null;
        }
        return reply;
    }
}
