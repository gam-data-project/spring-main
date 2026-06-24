package org.example.service.user;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.request.AddUserRequest;
import org.example.domain.User;
import org.example.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService{
    @Autowired
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public Long save(AddUserRequest dto) {
        System.out.println(dto.toString());
        User userDetail = User.builder()
                                .username(dto.getUsername())
                                .password(bCryptPasswordEncoder.encode(dto.getPassword())).build();
        Long result = userMapper.saveUser(userDetail);
        System.out.println("result ; "+result);
        return result;

    }


}
