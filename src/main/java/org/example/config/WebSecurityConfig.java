package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.service.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {
    //실제 인증 처리를 하는 시큐리티 설정 파일

    private final CustomUserDetailService userService;

    //스프링 시큐리티 기능 비활성화
    @Bean
    public WebSecurityCustomizer configure(){
        return (web) -> web.ignoring()
                //.requestMatchers(toH2Console())
                .requestMatchers("/css/**", "/js/**", "/images/**");
    }
    //특정 HTTP요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf -> csrf.disable()) //csrf 비활성화
                .authorizeRequests(auth -> auth
                        // ✅ /renewal/** 전체 오픈
                        .requestMatchers("/renewal/**").permitAll()

                        // 기존 공개 경로
                        .requestMatchers("/login","/user/login", "/admin/**", "/static/**", "/signup", "/user")
                        .permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()) //인증, 인가 설정

                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/articles", true)
//                        .permitAll())// 폼 기반 로그인 설정
//
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/login")
//                        .invalidateHttpSession(true)) //로그아웃 설정
                .build();
    }

    //인증 관리자 관련 설정
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
                                                       CustomUserDetailService userDetailService) throws Exception{
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userService) // 사용자 정보 서비스 설정
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }
    //패스워드 인코더로 사용할 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}


