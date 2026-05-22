package org.example.service.user;

import lombok.RequiredArgsConstructor;
import org.example.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.example.domain.User;
@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService{
    //스프링 시큐리티에서 사용자 정보를 가져오는 인터페이스

    @Autowired
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(username);

        if (user == null){
            throw new UsernameNotFoundException("User not found");
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) //이미 암호화된 비밀번호
                //.roles(user.getRole()) // 역할 부여
                .build();
    }
}
