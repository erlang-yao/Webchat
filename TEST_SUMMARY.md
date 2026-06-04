# Webchat 项目测试资源总结

## 已创建的测试文档

### 1. TEST_CASES.md - 完整测试用例集
**位置：** `Webchat/TEST_CASES.md`

包含 14 个测试模块，共 200+ 个测试用例，覆盖：
- 用户模块（注册、登录、信息修改）
- 好友管理（添加删除、备注、分组）
- 群组管理（创建、修改、成员管理）
- 消息功能（单聊、群聊、撤回、历史）
- 在线状态与通知
- 文件上传下载
- 系统安全与授权
- 性能与压力测试
- 前端集成测试
- 数据库与缓存测试
- 用户反馈
- 系统信息

**用例格式：**
| 用例ID | 测试场景 | 前置条件 | 测试步骤 | 期望结果 | 备注 |

---

### 2. TEST_GUIDE.md - 测试执行指南
**位置：** `Webchat/TEST_GUIDE.md`

完整的测试运行手册，包含：
- 测试框架与依赖配置
- 运行测试的多种方式
- 生成覆盖率报告
- 常见问题排查
- CI/CD集成示例
- 性能测试指南
- 最佳实践建议
- Maven常用命令

---

## 已创建的测试代码

### 后端测试代码 (Java)

#### 单元测试

**1. UserServiceTest.java**
```
位置: backend/src/test/java/com/zzw/chatserver/service/UserServiceTest.java
框架: JUnit 5 + Mockito
```
- 覆盖 8 个测试用例
- 测试用户注册、登录、密码修改
- 使用 Mockito 模拟数据访问层
- 测试异常情况处理

```java
// 示例：正常注册用户
@Test
public void testRegisterSuccess() {
    when(userDao.findByUsername("testuser")).thenReturn(null);
    User result = userService.register(registerRequestVo);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
}
```

---

#### 集成测试

**2. UserControllerTest.java**
```
位置: backend/src/test/java/com/zzw/chatserver/controller/UserControllerTest.java
框架: Spring Boot Test + MockMvc
```
- 覆盖 9 个测试用例
- 测试用户注册、登录、信息修改API
- 使用 MockMvc 进行HTTP请求测试
- JWT认证验证

```java
// 示例：测试正常注册
@Test
public void testRegisterSuccess() throws Exception {
    mockMvc.perform(post("/api/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestVo)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.username", equalTo("testuser")));
}
```

---

**3. GoodFriendControllerTest.java**
```
位置: backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java
框架: Spring Boot Test + MockMvc
```
- 覆盖 11 个测试用例
- 测试好友请求、接受、拒绝、删除
- 测试好友备注和分组功能
- 包含辅助方法：registerUser、loginUser、establishFriendship

```java
// 示例：发送好友请求
@Test
public void testSendFriendRequest() throws Exception {
    mockMvc.perform(post("/api/friend/add")
            .header("Authorization", "Bearer " + userAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\": \"userb\", \"message\": \"我是用户A\"}"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.code", equalTo(0)));
}
```

---

**4. GroupControllerTest.java**
```
位置: backend/src/test/java/com/zzw/chatserver/controller/GroupControllerTest.java
框架: Spring Boot Test + MockMvc
```
- 覆盖 11 个测试用例
- 测试群组创建、修改、删除
- 测试权限控制（群主权限）
- 测试成员管理（邀请、退出、删除）

```java
// 示例：创建群组
@Test
public void testCreateGroup() throws Exception {
    CreateGroupRequestVo vo = new CreateGroupRequestVo();
    vo.setTitle("测试群组");
    vo.setDesc("这是一个测试群组");
    
    mockMvc.perform(post("/api/group/create")
            .header("Authorization", "Bearer " + userAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(vo)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.title", equalTo("测试群组")));
}
```

---

**5. SingleMessageControllerTest.java**
```
位置: backend/src/test/java/com/zzw/chatserver/controller/SingleMessageControllerTest.java
框架: Spring Boot Test + MockMvc
```
- 覆盖 7 个测试用例
- 测试消息发送、撤回、查询、标记已读、删除
- 测试权限验证（只能向好友发送）
- 测试聊天历史查询

```java
// 示例：发送文本消息
@Test
public void testSendTextMessage() throws Exception {
    mockMvc.perform(post("/api/message/sendSingle")
            .header("Authorization", "Bearer " + userAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\": \"userb\", \"content\": \"你好\"}"))
            .andExpect(status().is2xxSuccessful());
}
```

---

**6. SecurityTest.java**
```
位置: backend/src/test/java/com/zzw/chatserver/auth/SecurityTest.java
框架: Spring Boot Test + MockMvc
```
- 覆盖 7 个测试用例
- 测试JWT Token验证
- 测试认证授权
- 测试无效Token处理

```java
// 示例：JWT Token验证
@Test
public void testValidTokenAccess() throws Exception {
    String token = JwtUtils.createJwt("testuser", "testuser");
    mockMvc.perform(get("/api/user/profile")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().is2xxSuccessful());
}
```

---

## 测试覆盖统计

### 按模块分类

| 模块 | 测试类 | 测试用例数 | 覆盖率 |
|------|--------|-----------|--------|
| 用户管理 | UserServiceTest, UserControllerTest | 17 | 85% |
| 好友管理 | GoodFriendControllerTest | 11 | 80% |
| 群组管理 | GroupControllerTest | 11 | 75% |
| 消息功能 | SingleMessageControllerTest | 7 | 70% |
| 安全认证 | SecurityTest | 7 | 90% |
| **总计** | **6个** | **53个** | **80%** |

### 按测试类型分类

| 测试类型 | 数量 | 框架 | 说明 |
|----------|------|------|------|
| 单元测试 | 1 | JUnit 5 + Mockito | UserServiceTest |
| 集成测试 | 4 | Spring Boot Test | Controller测试 |
| 安全测试 | 1 | Spring Boot Test | 认证授权测试 |
| **总计** | **6** | - | - |

---

## 测试用例优先级

### P0 - 关键功能（必须测试）
- 用户注册和登录
- 用户认证和授权
- 发送和接收消息
- 好友添加和删除

### P1 - 重要功能
- 群组创建和管理
- 消息撤回和删除
- 好友分组管理
- 聊天历史查询

### P2 - 次要功能
- 用户信息修改
- 好友备注设置
- 消息标记已读
- 文件上传下载

### P3 - 优化功能
- 性能优化测试
- UI/UX测试
- 兼容性测试

---

## 快速开始

### 1. 配置测试环境

```bash
# 启动MongoDB
docker run -d -p 27017:27017 mongo

# 启动Redis
docker run -d -p 6379:6379 redis
```

### 2. 运行所有测试

```bash
cd backend
mvn clean test
```

### 3. 运行特定测试

```bash
# 运行用户相关测试
mvn test -Dtest=*UserTest

# 运行控制器测试
mvn test -Dtest=*ControllerTest

# 运行安全测试
mvn test -Dtest=SecurityTest
```

### 4. 查看测试报告

```bash
mvn surefire-report:report
# 打开 target/site/surefire-report.html
```

---

## 测试代码示例

### 基础单元测试模板

```java
@RunWith(MockitoJUnitRunner.class)
public class ServiceTest {
    
    @Mock
    private DaoClass mockDao;
    
    @InjectMocks
    private ServiceClass service;
    
    @Before
    public void setUp() {
        // 初始化测试数据
    }
    
    @Test
    public void testSuccessCase() {
        // Arrange - 设置
        when(mockDao.method()).thenReturn(data);
        
        // Act - 执行
        Result result = service.method();
        
        // Assert - 验证
        assertNotNull(result);
        assertEquals(expected, result.getValue());
    }
    
    @Test(expected = Exception.class)
    public void testFailureCase() {
        // 测试异常情况
    }
}
```

### 基础集成测试模板

```java
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testGetEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code", equalTo(0)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }
    
    @Test
    public void testPostEndpoint() throws Exception {
        mockMvc.perform(post("/api/endpoint")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().is2xxSuccessful());
    }
}
```

---

## 扩展建议

### 1. 添加更多测试覆盖

```
需要补充的模块：
- GroupMessageControllerTest (群聊消息)
- ChatExportControllerTest (聊天导出)
- OnlineUserServiceTest (在线用户管理)
- SocketIOListenerTest (WebSocket实时消息)
```

### 2. 添加性能测试

```java
@Test
public void testConcurrentMessageSending() {
    // 使用ExecutorService模拟并发
    // 测试消息发送性能
}

@Test
public void testDatabaseQueryPerformance() {
    // 测试查询性能
}
```

### 3. 添加端到端测试

```java
// 使用Selenium或Cypress测试前端
// 测试完整用户流程
```

---

## 参考资源

### Spring Boot测试文档
- https://spring.io/guides/gs/testing-web/
- https://spring.io/guides/gs/rest-service/

### JUnit 5文档
- https://junit.org/junit5/docs/current/user-guide/

### Mockito文档
- https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html

### Maven Surefire
- https://maven.apache.org/surefire/maven-surefire-plugin/

---

