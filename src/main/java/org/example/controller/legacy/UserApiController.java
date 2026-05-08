package org.example.controller.legacy;

import org.example.domain.AddUserRequest;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

//@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    @Autowired
    public UserApiController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    // 로그인 페이지 반환
    @GetMapping("/login")
    public String loginPage() {
        return "/admin/login.html"; // 정적 HTML 파일 제공
    }

    // 일반
    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            // 사용자 인증 시도
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 성공 응답 반환
            return ResponseEntity.ok().body("로그인 성공");
        } catch (Exception e) {
            // 인증 실패 시 에러 메시지 반환
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다.");
        }
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "/admin/signup.html";
    }


//회원가입
    @PostMapping("/user/signup")
    public String signup(@RequestBody Map <String,String> m){
        System.out.println(m.toString());
        AddUserRequest UserRequest = new AddUserRequest();
        UserRequest.setUsername(m.get("username"));
        UserRequest.setPassword(m.get("password"));
        userService.save(UserRequest); //회원 가입 메서드 호출
        return "redirect:/login"; // 회원 가입이 완료된 이후에 로그인 페이지로 이동
    }




}

