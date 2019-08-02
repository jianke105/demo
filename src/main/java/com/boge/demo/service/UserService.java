package com.boge.demo.service;


import com.boge.demo.dataobject.UserDO;
import com.boge.demo.service.model.UserModel;

public interface UserService {


    int isUserExist(String username);

    int isPhoneExist(String telphone);

    UserModel getUserByUsername(String username);
}
