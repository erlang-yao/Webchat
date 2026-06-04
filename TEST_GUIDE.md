# Webchat 项目测试执行指南

## 目录结构

```
backend/
├── src/
│   ├── main/java/com/zzw/chatserver/   # 主代码
│   └── test/java/com/zzw/chatserver/   # 测试代码
│       ├── auth/
│       │   └── SecurityTest.java        # 安全认证测试
│       ├── controller/
│       │   ├── UserControllerTest.java
│       │   ├── GoodFriendControllerTest.java
│       │   ├── GroupControllerTest.java
│       │   └── SingleMessageControllerTest.java
│       └── service/
│           └── UserServiceTest.java     # 用户服务单元测试
└── pom.xml
```

## 测试框架与依赖

### 已包含的依赖
- **JUnit 5**: 现代化的测试框架（`junit-jupiter`）
- **Spring Boot Test**: 集成测试支持
- **Mockito**: 单元测试mock框架
- **REST Assured**: REST API测试
- **AssertJ**: 流畅的断言库

### 添加依赖 (可选)

如果需要更高级的功能，可在 `pom.xml` 添加：

```xml
<!-- 性能测试 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- 代码覆盖率 -->
<dependency>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
</dependency>

<!-- 数据库测试 -->
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo</artifactId>
    <scope>test</scope>
</dependency>
```

## 运行测试

### 1. 运行所有测试

```bash
# 使用Maven
mvn clean test

# 使用IDE
在IDE中右键点击src/test目录 → Run Tests
```

### 2. 运行特定测试类

```bash
# 运行用户控制器测试
mvn test -Dtest=UserControllerTest

# 运行所有控制器测试
mvn test -Dtest=*ControllerTest
```

### 3. 运行特定测试方法

```bash
# 运行单个测试方法
mvn test -Dtest=UserControllerTest#testLoginSuccess
```

### 4. 生成测试报告

```bash
# 生成HTML报告
mvn surefire-report:report

# 报告位置: target/site/surefire-report.html
```

## 单元测试 (Service层)

### UserServiceTest
测试用户服务层的业务逻辑

```bash
mvn test -Dtest=UserServiceTest
```

**覆盖的用例：**
- TC_USER_001: 正常注册用户
- TC_USER_002: 用户名已存在
- TC_USER_003: 密码为空
- TC_LOGIN_001: 正常登录
- TC_LOGIN_002: 用户不存在
- TC_LOGIN_003: 密码错误
- TC_USER_UPDATE_001: 修改用户昵称
- TC_USER_UPDATE_003: 修改密码

**Mock说明：**
使用 Mockito 模拟 UserDao 和 BCryptPasswordEncoder，不依赖真实数据库。

---

## 集成测试 (Controller层)

### UserControllerTest
测试用户管理API

```bash
mvn test -Dtest=UserControllerTest
```

**覆盖的用例：**
- TC_USER_001: 正常注册用户
- TC_USER_002: 用户名已存在
- TC_USER_003: 密码为空
- TC_USER_004: 重复密码不匹配
- TC_LOGIN_001: 正常登录
- TC_LOGIN_002: 用户不存在
- TC_LOGIN_003: 密码错误
- TC_AUTH_004: 未认证访问
- TC_USER_UPDATE_001: 修改用户昵称

**特点：**
- 使用 MockMvc 测试HTTP请求/响应
- 真实初始化Spring应用上下文
- 使用内存H2数据库

---

### GoodFriendControllerTest
测试好友管理API

```bash
mvn test -Dtest=GoodFriendControllerTest
```

**覆盖的用例：**
- TC_FRIEND_001: 发送好友请求
- TC_FRIEND_002: 接受好友请求
- TC_FRIEND_003: 拒绝好友请求
- TC_FRIEND_004: 删除好友
- TC_FRIEND_006: 自己向自己发送好友请求
- TC_FRIEND_007: 查看好友列表
- TC_FRIEND_NOTE_001: 设置好友备注
- TC_FRIEND_GROUP_001: 创建分组
- TC_FRIEND_GROUP_002: 修改分组名称
- TC_FRIEND_GROUP_003: 删除分组
- TC_FRIEND_GROUP_005: 移动好友到分组

**测试流程：**
1. 在 setUp() 中注册两个测试用户并登录
2. 建立好友关系
3. 测试各项好友管理功能

---

### GroupControllerTest
测试群组管理API

```bash
mvn test -Dtest=GroupControllerTest
```

**覆盖的用例：**
- TC_GROUP_001: 创建群组
- TC_GROUP_002: 创建群组-空成员列表
- TC_GROUP_003: 修改群组信息
- TC_GROUP_004: 非群主修改群组信息
- TC_GROUP_005: 解散群组
- TC_GROUP_006: 非群主解散群组
- TC_GROUP_007: 查询群组信息
- TC_GROUP_008: 搜索群组
- TC_GROUP_MEMBER_001: 邀请好友加入群组
- TC_GROUP_MEMBER_006: 退出群组
- TC_GROUP_MEMBER_008: 删除群成员

---

### SingleMessageControllerTest
测试单聊消息API

```bash
mvn test -Dtest=SingleMessageControllerTest
```

**覆盖的用例：**
- TC_MSG_SINGLE_001: 发送文本消息
- TC_MSG_SINGLE_002: 发送消息给非好友
- TC_MSG_SINGLE_004: 撤回消息
- TC_MSG_SINGLE_006: 查询聊天历史
- TC_MSG_SINGLE_007: 标记消息已读
- TC_MSG_SINGLE_008: 删除消息

---

### SecurityTest
测试安全认证功能

```bash
mvn test -Dtest=SecurityTest
```

**覆盖的用例：**
- TC_AUTH_001: JWT Token验证
- TC_AUTH_002: 无效Token
- TC_AUTH_003: Token过期
- TC_AUTH_004: 未认证访问
- TC_AUTH_005: 缺少Authorization头
- TC_AUTH_006: 错误的Authorization格式
- TC_PERMISSION_001: 访问公开端点

---

## 测试数据设置

### 自动化Setup

每个集成测试类都有 `@BeforeEach` 方法，自动化执行：

```java
@BeforeEach
public void setUp() throws Exception {
    // 注册测试用户
    registerUser("usera", "password123");
    registerUser("userb", "password123");
    
    // 登录获取token
    userAToken = loginUser("usera", "password123");
    userBToken = loginUser("userb", "password123");
    
    // 建立好友关系等前置条件
}
```

### 测试用户列表

| 用户名 | 密码 | 用途 |
|--------|------|------|
| usera | password123 | 测试发起者 |
| userb | password123 | 测试接收者 |
| userc | password123 | 非好友用户 |

---

## 常见问题排查

### 1. 测试连接数据库失败

**现象：** `java.net.ConnectException: Connection refused`

**解决：**
```bash
# 确保MongoDB正在运行
mongod --dbpath /path/to/data

# 或使用Docker
docker run -d -p 27017:27017 mongo
```

### 2. 测试连接Redis失败

**现象：** `redis.clients.jedis.exceptions.JedisConnectionException`

**解决：**
```bash
# 启动Redis
redis-server

# 或使用Docker
docker run -d -p 6379:6379 redis
```

### 3. Token过期导致测试失败

**原因：** JwtUtils中 EXPIRE 设置为 24 小时

**解决：** 在测试中重新生成新token

```java
String newToken = JwtUtils.createJwt("userId", "username");
```

### 4. 测试间数据污染

**原因：** 多个测试使用相同的用户名

**解决：** 在setUp中使用UUID生成唯一用户名

```java
String uniqueUsername = "user_" + UUID.randomUUID().toString();
```

---

## 测试覆盖率分析

### 生成覆盖率报告

```bash
# 配置POM添加JaCoCo插件
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
    </executions>
</plugin>

# 运行测试生成覆盖率
mvn clean test jacoco:report

# 报告位置: target/site/jacoco/index.html
```

### 覆盖率目标

| 模块 | 目标 | 说明 |
|------|------|------|
| Controller | 80% | API端点覆盖 |
| Service | 85% | 业务逻辑覆盖 |
| Util | 70% | 工具类覆盖 |
| DAO | 60% | 数据访问层 |

---

## 性能测试

### 压力测试示例

```java
@Test
public void testConcurrentLogins() throws Exception {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        executorService.execute(() -> {
            try {
                loginUser("usera", "password123");
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    executorService.shutdown();
}
```

### 运行性能测试

```bash
mvn test -Dtest=*PerformanceTest -DargLine="-Xmx1024m"
```

---

## CI/CD集成

### GitHub Actions 示例

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mongodb:
        image: mongo
        options: >-
          --health-cmd "mongo --eval 'db.admin().ping()'"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 27017:27017
      
      redis:
        image: redis
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379
    
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 8
      uses: actions/setup-java@v2
      with:
        java-version: 8
    - name: Run tests
      run: mvn clean test
    - name: Generate coverage report
      run: mvn jacoco:report
```

---

## 最佳实践

### 1. 测试命名规范
```
Test前缀: 测试类应以 *Test 结尾
方法命名: test + 功能 + 场景
示例: testLoginSuccess, testLoginWrongPassword
```

### 2. 断言最佳实践
```java
// ❌ 不推荐
assertTrue(result != null);

// ✅ 推荐
assertNotNull(result);
assertEquals("expected", result.getValue());
assertThat(result).isNotNull().hasFieldOrPropertyWithValue("id", "123");
```

### 3. 测试独立性
- 每个测试应该独立运行
- 不依赖其他测试的执行结果
- 在 @BeforeEach 中初始化所需数据

### 4. Mock vs 集成测试
```java
// 单元测试：使用Mock
@Test
public void unitTest() {
    UserService service = new UserService(mockUserDao);
}

// 集成测试：真实Spring容器
@SpringBootTest
public class IntegrationTest {
    @Autowired
    private UserService service;
}
```

---

## 常用Maven命令

```bash
# 运行所有测试
mvn clean test

# 跳过测试运行应用
mvn clean install -DskipTests

# 仅运行指定包的测试
mvn test -Dtest=com.zzw.chatserver.controller.*

# 使用特定的测试报告格式
mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

# 运行测试并生成报告
mvn clean test surefire-report:report

# 显示测试输出
mvn test -X
```

---

## 故障排查

### 详细日志输出

```xml
<!-- 在pom.xml中添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>
```

```bash
# 运行时启用DEBUG日志
mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

### 调试单个测试

在IDE中：
1. 右键测试方法 → Debug
2. 在关键行设置断点
3. 使用IDE调试工具检查变量状态

---

