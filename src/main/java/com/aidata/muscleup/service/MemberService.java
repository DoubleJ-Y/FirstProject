package com.aidata.muscleup.service;


import com.aidata.muscleup.dao.MemberDao;
import com.aidata.muscleup.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Service
public class MemberService {
    @Autowired
    private MemberDao mDao;

    //비밀번호 암호화 인코더
    private BCryptPasswordEncoder pEncoder = new BCryptPasswordEncoder();

    public String idCheck(String mid) {
        log.info("idCheck()");
        String res = null;

        int mcount = mDao.checkid(mid);
        if (mcount == 0) {
            res = "ok";
        } else {
            res = "fail";
        }
        return res;
    }

    public String memberJoin(MemberDto member,
                             RedirectAttributes rttr) {
        log.info("memberJoin()");

        String view = null; //가입성공시 첫페이지, 가입실패시 가입페이지
        String msg = null; //가입성공 및 가입실패 메세지 띄우기

        //비밀번호 암호화
        String encpwd = pEncoder.encode(member.getM_pwd());
        log.info(encpwd);

        //평문으로 쓰여진 비밀번호를 암호화 비밀번호로 덮어쓰기
        member.setM_pwd(encpwd);

        //Dao insert 처리(DB 저장)
        try {
            mDao.insertMember(member); //MemberDao 의 insertMember에 member 정보 저장
            msg = "회원가입이 완료되었습니다.";
            view = "redirect:/";
        } catch (Exception e) {
            msg = "회원가입에 실패하였습니다.";
            view = "redirect:joinForm";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    //회원 정보 수정
    public String memberUpdate(MemberDto member,
                             HttpSession session,
                             RedirectAttributes rttr) {
        log.info("memberUpdate()");

        String view = null;
        String msg = null;

        try {
            mDao.updateMember(member);
            session.setAttribute("member", member);
            msg = "정보수정이 완료되었습니다.";
            view = "redirect:mypageForm";
        } catch (Exception e) {
            msg = "정보수정에 실패하였습니다.";
            view = "redirect:m_updateForm";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    //로그인처리
    public String loginProc(MemberDto member,
                            HttpSession session,
                            RedirectAttributes rttr) {
        log.info("loginProc()");
        String view = null; // 로그인 성공 및 실패시 이동할 창을위해 생성
        String msg = null; // 로그인 성공 및 실패시 띄울 메시지를 위해 생성

        String encpwd = mDao.findPassword(member.getM_id()); //DB에서 해당 아이디의 비밀번호(암호문)를 가져온다.

        if (encpwd != null) {
            if (pEncoder.matches(member.getM_pwd(), encpwd)) {
                member = mDao.selectMember(member.getM_id());
                session.setAttribute("member", member);
                view = "redirect:/";
                msg = "로그인 성공!!";
            } else {
                view = "redirect:loginForm";
                msg = "비밀번호가 틀립니다.";
            }
        } else {
            view = "redirect:logForm";
            msg = "존재하지 않는 아이디입니다.";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    public String logout(HttpSession session) {
        log.info("logout()");
        session.invalidate();
        return "redirect:/";
    }
}
