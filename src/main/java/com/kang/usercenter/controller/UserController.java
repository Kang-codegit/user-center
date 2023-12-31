package com.kang.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kang.usercenter.model.User;
import com.kang.usercenter.model.domain.request.UserLoginRequest;
import com.kang.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.kang.usercenter.constant.userConstant.ADMIN_ROLE;
import static com.kang.usercenter.constant.userConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 * @author kang
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserLoginRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }
        return userService.userRegister(userAccount,userPassword,checkPassword);
    }
    @PostMapping("login")
    public User userLogin(@RequestBody UserLoginRequest userRegisterRequest, HttpServletRequest request){
        if(userRegisterRequest==null){
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
//        HttpSession session = request.getSession();
//        User user = (User) session.getAttribute(USER_LOGIN_STATE);
        return userService.userLogin(userAccount,userPassword,request);

    }
    @GetMapping("/search")
    public List<User> searchUsers(String username,HttpServletRequest request){
        //仅管理员可见
        if (!isAdmin(request)){
            return new ArrayList<>();
        }
        QueryWrapper queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList=userService.list(queryWrapper);
//        return userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    }

    @PostMapping("/delete")
    public boolean deleteUsers(@RequestBody long id,HttpServletRequest request){
        if (!isAdmin(request)){
            return false;
        }
        if (id<=0){
            return false;
        }
        return userService.removeById(id);
    }

        private boolean isAdmin(HttpServletRequest request){
            //仅管理员可见
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            User user=(User) userObj;
            return user != null && user.getUserRole() == ADMIN_ROLE;
        }
}
