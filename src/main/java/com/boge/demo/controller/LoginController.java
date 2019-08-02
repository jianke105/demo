package com.boge.demo.controller;


import com.boge.demo.commons.CodeUtil;
import com.boge.demo.commons.MD5Utils;
import com.boge.demo.commons.RedisUtils;
import com.boge.demo.response.EmBusinessMyError;
import com.boge.demo.response.ResponseType;
import com.boge.demo.service.Impl.UserServiceImpl;
import com.boge.demo.service.model.UserModel;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Controller
@CrossOrigin
@SessionAttributes("usermodel")
public class LoginController extends BaseController {

    @Autowired
    private UserServiceImpl userService;

    //使用前要注入
    @Autowired
    private RedisUtils redisUtils;


    @RequestMapping("/login")
    public String goLogin() {

        return "Login";
    }

    @RequestMapping("/register")
    public String goRegister() {

        return "register";
    }

    //开启登录验证码
    @Value("${verifycodeonoff}")
    private String onoff ;

    String verifystr = "";


    //是否开启验证码
    @ResponseBody
    @RequestMapping(value = "/getverifycode", method = RequestMethod.POST, consumes = (responsehead))
    public ResponseType isgetcd(@RequestParam("reqpa") String reqpa){

        if (onoff.equals("true")) {

            verifystr = UUID.randomUUID().toString().replace("-", "");
            redisUtils.set("verify_code_id_" + verifystr, verifystr, 600);

            return ResponseType.Create(verifystr);
        } else {
            return ResponseType.Create("fail", "noyet");
        }

    }

    //验证码开启状态时，让前端拉取验证码
    @ResponseBody
    @RequestMapping(value = "/getverify", method = RequestMethod.GET)
    public ResponseType getcd(HttpServletResponse response,@RequestParam("vertoken") String vertoken) throws IOException {
        if (onoff.equals("true")) {
            if(redisUtils.hasKey("verify_code_id_" + vertoken)){
                Map<String, Object> map = CodeUtil.generateCodeAndPic();
                redisUtils.set("verify_code_" + vertoken,map.get("code"),600);
                ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());

                logger.info("验证码的值为：" + map.get("code"));
                return ResponseType.Create("success");
            }
            return ResponseType.Create("fail", "noyet");
        } else {

            return ResponseType.Create("fail", "noyet");
        }

    }

    //验证登录信息,如果通过，则放行，根据角色权限生成界面
    @ResponseBody
    @RequestMapping(value = "/login.do", method = RequestMethod.POST, consumes = (responsehead))
    public Object Login(@RequestParam("username") String username,
                        @RequestParam("password") String password, @RequestParam("verifycode") String verifycode,
                        @RequestParam("vertoken") String vertoken) {
        Map<String, Object> resp = new HashMap<>();

        logger.info(username + "  " + password + "登录");
        if (StringUtils.isAllEmpty(username) || StringUtils.isAllEmpty(password)) {
            logger.info("输入用户名或密码不能为空");
            resp.put("errorcode", EmBusinessMyError.PARMAS_NOT_VALUEABLE.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.PARMAS_NOT_VALUEABLE.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }
        //shiro验证用户名是否存在，密码是否正确，如果正确有哪些权限
        UserModel userModel = userService.getUserByUsername(username);
        //  logger.info(userModel.getTelphone());
        //数据库拿到的加密的密码
        String psdstr = userModel.getPassword();
        //   logger.info(2);
        //传来的密码
        String telmd5 = MD5Utils.getSaltMD5oph(userModel.getTelphone(), password);

        if (StringUtils.isAllEmpty(userModel.getTelphone())) {
            //      logger.info(1);
            resp.put("errorcode", EmBusinessMyError.USER_NOT_EXIST.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_NOT_EXIST.getErrorMsg());
            return ResponseType.Create("fail", resp);
        } else {
            if (onoff.equals("true")) {
                if (verifycode == null || verifycode.equals("")) {
                    resp.put("errorcode", EmBusinessMyError.VERIFYCODE_ERROR.getErrorCode());
                    resp.put("errorMsg", EmBusinessMyError.VERIFYCODE_ERROR.getErrorMsg());
                    return ResponseType.Create("fail", resp);
                } else {
                    String inrediscode = (String) redisUtils.get("verify_code_" + vertoken);
                    if (!redisUtils.hasKey("verify_code_" + vertoken)&&inrediscode == null) {
                        resp.put("errorcode", EmBusinessMyError.VERIFYCODE_PASS.getErrorCode());
                        resp.put("errorMsg", EmBusinessMyError.VERIFYCODE_PASS.getErrorMsg());
                        return ResponseType.Create("fail", resp);
                    } else if (StringUtils.equalsIgnoreCase(verifycode, inrediscode)) {
                        return loginverify(psdstr, resp, telmd5, userModel);
                    } else {
                        resp.put("errorcode", EmBusinessMyError.VERIFYCODE_ERROR.getErrorCode());
                        resp.put("errorMsg", EmBusinessMyError.VERIFYCODE_ERROR.getErrorMsg());
                        return ResponseType.Create("fail", resp);
                    }
                }
            }
            return loginverify(psdstr, resp, telmd5, userModel);
        }
    }

    //判断用户是否登录，登录是否过期，默认1小时不操作登录过期
    @ResponseBody
    @RequestMapping(value = "/IsLogin", method = RequestMethod.GET)
    public ResponseType BooleanIsLogin(@RequestParam("username") String username) {
        Map<String, Object> resp = new HashMap<>();
        //  logger.info(username);
        if (redisUtils.hasKey(username) && StringUtils.equals((CharSequence) redisUtils.get(username),
                MD5Utils.getSaltMD5AStr(username, "LOGIN_USER"))) {

            //     logger.info("已登录"+redisUtils.get(username));
            redisUtils.expire(username, 3600);
            redisUtils.expire(username + "loginid", 3600);
            resp.put("ispass", redisUtils.get(username + "loginid"));

            return ResponseType.Create(resp);

        } else if (!redisUtils.hasKey(username)) {
            //       logger.info("没有登录");
            resp.put("errorcode", EmBusinessMyError.USER_NOT_LOGIN.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_NOT_LOGIN.getErrorMsg());
            return ResponseType.Create("fail", resp);
        } else {
            //      logger.info("登录已过期");
            resp.put("errorcode", EmBusinessMyError.USER_LOGIN_LOSE.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_LOGIN_LOSE.getErrorMsg());
            return ResponseType.Create("fail", resp);

        }

    }

    //判断用户是否登录，登录是否过期，默认1小时不操作登录过期
    @ResponseBody
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public Object LoginOut(@RequestParam("username") String username) {
        Map<String, Object> resp = new HashMap<>();
        redisUtils.del(username);
        redisUtils.del(username + "loginid");
        return ResponseType.Create("loginoutsuccess");

    }


    @RequestMapping(value = "/admin/getotp.do", method = RequestMethod.POST, consumes = {responsehead})
    @ResponseBody
    public Object getOtpCode(@RequestParam("telphone") String telphone, @RequestParam("type") String type) {
        Map<String, Object> resp = new HashMap<>();
        // RedisUtils redisUtils = new RedisUtils();
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(type)) {

            resp.put("errorcode", EmBusinessMyError.PARMAS_NOT_VALUEABLE.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.PARMAS_NOT_VALUEABLE.getErrorMsg());
            return ResponseType.Create("fail", resp);
        } else {
            Random random = new Random();
            int randomint = random.nextInt(899999);
            int otpCode = randomint + 100000;
            //短信验证码存入Redis，有效时间60秒
            if (type.equals("login") || type.equals("reg") || type.equals("findpw")) {
                if (redisUtils.hasKey(telphone + type)) {
                    redisUtils.del(telphone + type);
                    redisUtils.set(telphone + type, String.valueOf(otpCode), 60);
                    logger.info("redis删除存入验证码成功!");
                } else {
                    redisUtils.set(telphone + type, String.valueOf(otpCode), 60);
                    logger.info("redis存入验证码成功!");
                }
            } else {
                ResponseType.Create("fail", EmBusinessMyError.UNKNOWN_ERROR);
            }
            logger.info(telphone + "  ====  " + otpCode + " === " + type);
            return ResponseType.Create("success");
        }


    }


    public ResponseType loginverify(String psdstr, Map<String, Object> resp, String telmd5, UserModel userModel) {
        if (StringUtils.isAllEmpty(psdstr)) {
            //   logger.info("服务器错误");
            resp.put("errorcode", EmBusinessMyError.UNKNOWN_ERROR.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.UNKNOWN_ERROR.getErrorMsg());
            return ResponseType.Create("fail", resp);
        } else if (StringUtils.equals(psdstr, telmd5)) {
            //用户名密码正确

            String loginstr = MD5Utils.getSaltMD5AStr(userModel.getUsername(), "LOGIN_USER");
            redisUtils.set(userModel.getUsername(), loginstr, 3600);
            redisUtils.set("loginid_" + userModel.getUsername(), userModel, 3600);
            return ResponseType.Create("success");
        } else {
            //  logger.info("密码不正确");

            resp.put("errorcode", EmBusinessMyError.USER_OR_PSD_ERROR.getErrorCode());
            resp.put("errorMsg", EmBusinessMyError.USER_OR_PSD_ERROR.getErrorMsg());
            return ResponseType.Create("fail", resp);
        }
    }
}