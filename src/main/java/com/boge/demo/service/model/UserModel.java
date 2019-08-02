package com.boge.demo.service.model;

public class UserModel {

    //    @NotNull(message = "用户名不能为空")
//    @NotEmpty(message = "用户名不能为空")
//    @Size(max = 30,min = 3,message = "用户名长度不符合")
    private String username;

    //    @NotNull(message = "密码不能为为空")
//    @NotEmpty(message = "密码不能为为空")
//    @Size(max = 50,min = 3,message = "用户名长度不符合")
    private String password;


    private Integer id;
    private String telphone;
    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
