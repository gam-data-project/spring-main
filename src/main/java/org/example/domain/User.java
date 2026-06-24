package org.example.domain;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User implements UserDetails {

    String username;
    String password;

    @Builder
    public User(String username, String password, String auth){
        this.username = username;
        this.password = password;
    }

    @Override // 사용자가 가지고 있는 권한의 목록을 반환
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("user"));
        //return null;
    }

    // 사용자의 id를 반환(고유한 값)
    @Override
    public String getUsername(){
        return this.username;
    }

    // 사용자의 password 반환
    @Override
    public String getPassword() {
        return this.password;
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        //만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    @Override
    public boolean isAccountNonLocked(){
        //계정 잠금되었는지 확인하는 로직
        return true; // true-> 잠금되지 않았음
    }

    @Override
    public boolean isCredentialsNonExpired(){
        //패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        //계정이 사용 가능한지 확인하는 로직
        return true; //true -> 사용 가능
    }
}
