package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;

public interface UserService {
    User registerUser(SignUpRequest signUpRequest);
}
