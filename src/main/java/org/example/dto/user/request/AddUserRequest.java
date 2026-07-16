package org.example.dto.user.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
    private String username;
    private String password;
}
