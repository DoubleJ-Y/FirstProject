package com.aidata.muscleup.controller;

import com.aidata.muscleup.dao.MemberDao;
import com.aidata.muscleup.dto.MemberDto;
import com.aidata.muscleup.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class memberController {
    @Autowired
    MemberService mServ;
    @Autowired
    MemberDao mDao;

    @GetMapping("/")
    public String home() {
        log.info("home()");
        return "index";
    }

    @GetMapping("joinForm")
    public String joinForm() {
        log.info("joinForm");
        return "joinForm";
    }

    @GetMapping("idCheck")
    @ResponseBody
    public String idCheck(String mid) {
        log.info("idCheck()");
        String result = mServ.idCheck(mid);
        return result;
    }

    @PostMapping("joinProc")
    public String joinProc(MemberDto member,
                           RedirectAttributes rttr) {
        log.info("joinProc()");
        String view = mServ.memberJoin(member, rttr);
        return view;
    }

    @GetMapping("loginForm")
    public String loginForm() {
        log.info("loginForm");
        return "loginForm";
    }

    @PostMapping("loginProc")
    public String loginProc(MemberDto member,
                            HttpSession session,
                            RedirectAttributes rttr) {
        log.info("loginProc");
        String view = mServ.loginProc(member, session, rttr);
        return view;
    }

    @GetMapping("logout")
    public String logout(HttpSession session) {
        log.info("logout()");
        String view = mServ.logout(session);
        return view;
    }

    @GetMapping("mypageForm")
    public String mypageForm() {
        log.info("mypageForm()");
        return "mypageForm";
    }

    @GetMapping("m_updateForm")
    public String m_updateForm() {
        log.info("m_updateForm()");
        return "m_updateForm";
    }

    @PostMapping("m_updateProc")
    public String m_updateProc(MemberDto member,
                               HttpSession session,
                               RedirectAttributes rttr) {
        log.info("m_updateProc");
        String view = mServ.memberUpdate(member, session, rttr);
        return view;
    }

}


