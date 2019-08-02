package com.boge.demo.service.Impl;


import com.boge.demo.dataobject.UserDO;
import com.boge.demo.mapper.UserDOMapper;
import com.boge.demo.service.UserService;
import com.boge.demo.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Override
    public int isUserExist(String username) {
        return userDOMapper.isUserExist(username);
    }

    @Override
    public int isPhoneExist(String telphone) {
        return userDOMapper.isPhoneExist(telphone);
    }

    @Override
    public UserModel getUserByUsername(String username) {
        UserModel userModel = new UserModel();
        UserDO userDO = userDOMapper.getUserByUsername(username);
        userModel = convertFromUserDO(userDO);
        return userModel;
    }

    public UserModel convertFromUserDO(UserDO userDO) {
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        return userModel;
    }
}
