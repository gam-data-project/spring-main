package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.User;

import java.util.Optional;

@Mapper
public interface UserMapper {
    public User findByEmail(String username);
    public Long saveUser(User userDetail);
}
