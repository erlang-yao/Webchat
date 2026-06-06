package com.zzw.chatserver.service;

import com.zzw.chatserver.common.ResultEnum;
import com.zzw.chatserver.dao.AccountPoolDao;
import com.zzw.chatserver.dao.UserDao;
import com.zzw.chatserver.pojo.User;
import com.zzw.chatserver.pojo.vo.RegisterRequestVo;
import com.zzw.chatserver.pojo.vo.UpdateUserPwdRequestVo;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private AccountPoolDao accountPoolDao;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequestVo registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestVo();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("123456");
        registerRequest.setRePassword("123456");
    }

    @Test
    void registerReturnsSuccessAfterUserIsPersisted() {
        when(userDao.findUserByUsername("testuser")).thenReturn(null);
        when(bCryptPasswordEncoder.encode("123456")).thenReturn("encrypted");
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(new ObjectId());
            return user;
        });

        Map<String, Object> result = userService.register(registerRequest);

        assertEquals(ResultEnum.REGISTER_SUCCESS.getCode(), result.get("code"));
        verify(accountPoolDao).save(any());
        verify(userDao).save(any(User.class));
    }

    @Test
    void registerRejectsExistingUsernameWithoutWriting() {
        when(userDao.findUserByUsername("testuser")).thenReturn(new User());

        Map<String, Object> result = userService.register(registerRequest);

        assertEquals(ResultEnum.USER_HAS_EXIST.getCode(), result.get("code"));
        assertNull(result.get("userCode"));
        verify(accountPoolDao, never()).save(any());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void registerRejectsMissingPasswordWithoutQueryingDatabase() {
        registerRequest.setPassword(null);

        Map<String, Object> result = userService.register(registerRequest);

        assertEquals(ResultEnum.INCORRECT_PASSWORD_TWICE.getCode(), result.get("code"));
        verify(userDao, never()).findUserByUsername(any());
    }

    @Test
    void getUserInfoUsesMongoObjectId() {
        ObjectId userId = new ObjectId();
        User user = new User();
        user.setUserId(userId);
        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserInfo(userId.toHexString());

        assertSame(user, result);
    }

    @Test
    void updatePasswordRejectsMismatchedConfirmationWithoutDatabaseAccess() {
        UpdateUserPwdRequestVo request = new UpdateUserPwdRequestVo();
        request.setUserId(new ObjectId().toHexString());
        request.setOldPwd("123456");
        request.setNewPwd("newpass123");
        request.setReNewPwd("differentpass");

        Map<String, Object> result = userService.updateUserPwd(request);

        assertEquals(ResultEnum.INCORRECT_PASSWORD_TWICE.getCode(), result.get("code"));
        verify(userDao, never()).findById(any());
    }
}
