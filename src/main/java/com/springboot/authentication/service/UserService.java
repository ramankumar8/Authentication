package com.springboot.authentication.service;

import com.springboot.authentication.dao.UserDao;
import com.springboot.authentication.dto.UserDetailsDTO;
import com.springboot.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDao userDao;

    public UserDetailsDTO getUserDetail(UUID id) {
        return userDao.getUserByPublicId(id);
    }

}
