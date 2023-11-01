package com.xcfei.community.service;

import com.xcfei.community.dao.UserMapper;
import com.xcfei.community.entity.User;
import com.xcfei.community.util.CommunityUtil;
import com.xcfei.community.util.MailClient;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {
    public final static int ACTIVATION_SUCCESS = 0;

    public final static int ACTIVATION_REPEAT = 1;
    public final static int ACTIVATION_FAILURE = 2;

    private final UserMapper userMapper;
    private final MailClient mailClient;
    private final TemplateEngine templateEngine;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    public UserService(UserMapper userMapper, MailClient mailClient, TemplateEngine templateEngine) {
        this.userMapper = userMapper;
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
    }

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(@NonNull User user) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "Username cannot be null");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "Password cannot be null");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "Email cannot be null");
            return map;
        }

        // Validate if user exists
        User userFromDB = userMapper.selectByName(user.getUsername());
        if (userFromDB != null) {
            map.put("usernameMsg", "Account already exists");
            return map;
        }

        // Validate email
        userFromDB = userMapper.selectByEmail(user.getEmail());
        if (userFromDB != null) {
            map.put("emailMsg", "Email already been registered");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0, 6));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("Https://images.newcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // Activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activate your Account", content);

        return map;
    }

    public int activate(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, ACTIVATION_SUCCESS);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }
}
