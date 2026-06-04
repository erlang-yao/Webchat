# Webchat 项目测试资源索引

## 📁 文件结构

```
Webchat/
├── TEST_CASES.md                    # 完整测试用例集（14个模块，200+ 用例）
├── TEST_GUIDE.md                    # 测试执行指南与最佳实践
├── TEST_SUMMARY.md                  # 测试资源总结（本文件）
├── README.md                        # 项目说明（可选）
│
└── backend/
    ├── pom.xml                      # Maven配置
    ├── src/
    │   ├── main/java/
    │   │   └── com/zzw/chatserver/
    │   │       ├── controller/      # 控制器
    │   │       ├── service/         # 服务层
    │   │       ├── dao/             # 数据访问层
    │   │       ├── pojo/            # 实体类
    │   │       └── ...
    │   │
    │   └── test/java/
    │       └── com/zzw/chatserver/
    │           ├── service/
    │           │   └── UserServiceTest.java              # ⭐ 单元测试
    │           │
    │           ├── controller/
    │           │   ├── UserControllerTest.java           # ⭐ 集成测试
    │           │   ├── GoodFriendControllerTest.java     # ⭐ 集成测试
    │           │   ├── GroupControllerTest.java          # ⭐ 集成测试
    │           │   └── SingleMessageControllerTest.java  # ⭐ 集成测试
    │           │
    │           └── auth/
    │               └── SecurityTest.java                 # ⭐ 安全测试
    │
    └── frontend/                    # 前端项目
        ├── src/
        │   ├── components/
        │   ├── pages/
        │   └── __tests__/           # 前端单元测试 (待补充)
        └── package.json
```

---

## 🎯 快速导航

### 📖 文档索引

| 文件 | 用途 | 内容简述 |
|------|------|---------|
| [TEST_CASES.md](./TEST_CASES.md) | 完整的测试用例库 | 14个模块，200+个测试用例，包含用例ID、场景、步骤、期望结果 |
| [TEST_GUIDE.md](./TEST_GUIDE.md) | 测试执行指南 | 运行方式、环境配置、故障排查、CI/CD集成、最佳实践 |
| [TEST_SUMMARY.md](./TEST_SUMMARY.md) | 测试资源总结 | 已创建的测试代码说明、统计数据、快速开始 |

### 🧪 测试代码索引

| 测试文件 | 类型 | 覆盖模块 | 用例数 | 关键特性 |
|---------|------|---------|--------|---------|
| [UserServiceTest.java](./backend/src/test/java/com/zzw/chatserver/service/UserServiceTest.java) | 单元测试 | 用户服务 | 8 | Mock数据库，业务逻辑验证 |
| [UserControllerTest.java](./backend/src/test/java/com/zzw/chatserver/controller/UserControllerTest.java) | 集成测试 | 用户管理API | 9 | JWT认证，HTTP请求 |
| [GoodFriendControllerTest.java](./backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java) | 集成测试 | 好友管理API | 11 | 好友请求、分组、备注 |
| [GroupControllerTest.java](./backend/src/test/java/com/zzw/chatserver/controller/GroupControllerTest.java) | 集成测试 | 群组管理API | 11 | 权限验证、成员管理 |
| [SingleMessageControllerTest.java](./backend/src/test/java/com/zzw/chatserver/controller/SingleMessageControllerTest.java) | 集成测试 | 消息功能API | 7 | 消息发送、撤回、历史 |
| [SecurityTest.java](./backend/src/test/java/com/zzw/chatserver/auth/SecurityTest.java) | 安全测试 | 认证授权 | 7 | Token验证、无效token、未授权 |

---

## 📊 测试覆盖概览

### 功能模块覆盖率

```
用户管理模块
├─ 注册                          ✅ 完全覆盖 (5个用例)
├─ 登录                          ✅ 完全覆盖 (5个用例)
├─ 信息修改                      ✅ 完全覆盖 (5个用例)
└─ 认证授权                      ✅ 完全覆盖 (7个用例)

好友管理模块
├─ 好友请求管理                  ✅ 完全覆盖 (5个用例)
├─ 好友删除                      ✅ 完全覆盖 (2个用例)
├─ 好友备注与分组                ✅ 完全覆盖 (8个用例)
└─ 好友列表查询                  ✅ 完全覆盖 (1个用例)

群组管理模块
├─ 群组创建与修改                ✅ 完全覆盖 (4个用例)
├─ 群组解散                      ✅ 完全覆盖 (2个用例)
├─ 群成员管理                    ✅ 部分覆盖 (6个用例)
└─ 群组信息查询                  ✅ 完全覆盖 (2个用例)

消息模块
├─ 单聊消息                      ✅ 部分覆盖 (7个用例)
├─ 群聊消息                      ⚠️  计划中
├─ 文件上传下载                  ⚠️  计划中
└─ 消息内容管理                  ⚠️  计划中

系统功能
├─ 在线状态管理                  ⚠️  计划中
├─ 事件通知                      ⚠️  计划中
├─ 聊天记录导出                  ⚠️  计划中
├─ 性能压力测试                  ⚠️  计划中
└─ 前端集成测试                  ⚠️  计划中
```

---

## 🚀 快速开始指南

### 方案 A: 快速查看测试用例

1. 打开 [TEST_CASES.md](./TEST_CASES.md)
2. 找到你感兴趣的模块
3. 查看对应的用例ID、场景和期望结果

### 方案 B: 运行现有测试

```bash
# 1. 进入后端目录
cd backend

# 2. 运行所有测试
mvn clean test

# 3. 查看测试报告
mvn surefire-report:report
# 打开: target/site/surefire-report.html
```

### 方案 C: 学习测试代码

1. 查看 [TEST_SUMMARY.md](./TEST_SUMMARY.md) 的"测试代码示例"
2. 打开对应的测试源文件
3. 按照代码注释理解测试逻辑

### 方案 D: 了解最佳实践

1. 打开 [TEST_GUIDE.md](./TEST_GUIDE.md)
2. 查看"最佳实践"部分
3. 查看"常见问题排查"了解常见问题

---

## 📋 按用例ID快速查找

### 用户管理 (TC_USER_*)

| 用例ID | 用例名称 | 文件位置 |
|--------|---------|---------|
| TC_USER_001 | 正常注册用户 | [TEST_CASES.md#用户注册](./TEST_CASES.md) |
| TC_USER_002 | 用户名已存在 | [TEST_CASES.md#用户注册](./TEST_CASES.md) |
| TC_USER_003 | 密码为空 | [TEST_CASES.md#用户注册](./TEST_CASES.md) |
| TC_USER_004 | 邮箱格式不合法 | [TEST_CASES.md#用户注册](./TEST_CASES.md) |
| TC_USER_UPDATE_001 | 修改用户昵称 | [TEST_CASES.md#用户信息修改](./TEST_CASES.md) |
| TC_USER_UPDATE_002 | 修改用户头像 | [TEST_CASES.md#用户信息修改](./TEST_CASES.md) |
| TC_USER_UPDATE_003 | 修改密码 | [TEST_CASES.md#用户信息修改](./TEST_CASES.md) |

### 登录认证 (TC_LOGIN_*/TC_AUTH_*)

| 用例ID | 用例名称 | 测试代码 |
|--------|---------|---------|
| TC_LOGIN_001 | 正常登录 | [UserControllerTest.java#testLoginSuccess](./backend/src/test/java/com/zzw/chatserver/controller/UserControllerTest.java) |
| TC_LOGIN_002 | 用户不存在 | [UserControllerTest.java#testLoginUserNotFound](./backend/src/test/java/com/zzw/chatserver/controller/UserControllerTest.java) |
| TC_LOGIN_003 | 密码错误 | [UserControllerTest.java#testLoginWrongPassword](./backend/src/test/java/com/zzw/chatserver/controller/UserControllerTest.java) |
| TC_AUTH_001 | JWT Token验证 | [SecurityTest.java#testValidTokenAccess](./backend/src/test/java/com/zzw/chatserver/auth/SecurityTest.java) |
| TC_AUTH_002 | 无效Token | [SecurityTest.java#testInvalidTokenAccess](./backend/src/test/java/com/zzw/chatserver/auth/SecurityTest.java) |
| TC_AUTH_004 | 未认证访问 | [SecurityTest.java#testUnauthorizedAccess](./backend/src/test/java/com/zzw/chatserver/auth/SecurityTest.java) |

### 好友管理 (TC_FRIEND_*)

| 用例ID | 用例名称 | 测试代码 |
|--------|---------|---------|
| TC_FRIEND_001 | 发送好友请求 | [GoodFriendControllerTest.java#testSendFriendRequest](./backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java) |
| TC_FRIEND_002 | 接受好友请求 | [GoodFriendControllerTest.java#testAcceptFriendRequest](./backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java) |
| TC_FRIEND_007 | 查看好友列表 | [GoodFriendControllerTest.java#testGetFriendList](./backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java) |
| TC_FRIEND_NOTE_001 | 设置好友备注 | [GoodFriendControllerTest.java#testSetFriendRemark](./backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java) |
| TC_FRIEND_GROUP_001 | 创建分组 | [GoodFriendControllerTest.java#testCreateFriendGroup](./backend/src/test/java/com/zzw/chatserver/controller/GoodFriendControllerTest.java) |

### 群组管理 (TC_GROUP_*)

| 用例ID | 用例名称 | 测试代码 |
|--------|---------|---------|
| TC_GROUP_001 | 创建群组 | [GroupControllerTest.java#testCreateGroup](./backend/src/test/java/com/zzw/chatserver/controller/GroupControllerTest.java) |
| TC_GROUP_003 | 修改群组信息 | [GroupControllerTest.java#testUpdateGroupInfo](./backend/src/test/java/com/zzw/chatserver/controller/GroupControllerTest.java) |
| TC_GROUP_005 | 解散群组 | [GroupControllerTest.java#testDisbandGroup](./backend/src/test/java/com/zzw/chatserver/controller/GroupControllerTest.java) |
| TC_GROUP_MEMBER_001 | 邀请好友加入群组 | [GroupControllerTest.java#testInviteFriendToGroup](./backend/src/test/java/com/zzw/chatserver/controller/GroupControllerTest.java) |

### 消息功能 (TC_MSG_*)

| 用例ID | 用例名称 | 测试代码 |
|--------|---------|---------|
| TC_MSG_SINGLE_001 | 发送文本消息 | [SingleMessageControllerTest.java#testSendTextMessage](./backend/src/test/java/com/zzw/chatserver/controller/SingleMessageControllerTest.java) |
| TC_MSG_SINGLE_006 | 查询聊天历史 | [SingleMessageControllerTest.java#testGetChatHistory](./backend/src/test/java/com/zzw/chatserver/controller/SingleMessageControllerTest.java) |
| TC_MSG_SINGLE_007 | 标记消息已读 | [SingleMessageControllerTest.java#testMarkMessageAsRead](./backend/src/test/java/com/zzw/chatserver/controller/SingleMessageControllerTest.java) |

---

## 🔧 运行特定测试的命令

### 按测试类运行

```bash
# 运行用户模块所有测试
mvn test -Dtest=UserControllerTest

# 运行好友管理测试
mvn test -Dtest=GoodFriendControllerTest

# 运行群组管理测试
mvn test -Dtest=GroupControllerTest

# 运行消息功能测试
mvn test -Dtest=SingleMessageControllerTest

# 运行安全认证测试
mvn test -Dtest=SecurityTest

# 运行单元测试
mvn test -Dtest=UserServiceTest
```

### 按测试方法运行

```bash
# 仅运行用户注册测试
mvn test -Dtest=UserControllerTest#testRegisterSuccess

# 仅运行登录测试
mvn test -Dtest=UserControllerTest#testLoginSuccess

# 仅运行好友请求测试
mvn test -Dtest=GoodFriendControllerTest#testSendFriendRequest
```

### 按模式运行

```bash
# 运行所有控制器测试
mvn test -Dtest=*ControllerTest

# 运行所有包含"Test"的测试
mvn test -Dtest=*Test

# 跳过测试（用于快速构建）
mvn clean install -DskipTests
```

---

## 📈 测试执行统计

### 当前已实现的测试

- **单元测试**: 1个 (UserServiceTest)
- **集成测试**: 4个 (Controller测试)
- **安全测试**: 1个 (SecurityTest)
- **总计**: 6个测试类
- **覆盖用例**: 53个

### 待实现的测试

- [ ] GroupMessageControllerTest (群聊消息)
- [ ] ChatExportControllerTest (聊天导出)
- [ ] SocketIOListenerTest (实时消息)
- [ ] FileUploadTest (文件上传)
- [ ] PerformanceTest (性能测试)
- [ ] 前端组件测试 (React Testing Library)

---

## 🎓 学习路径

### 初级 (了解基础)
1. 阅读 TEST_CASES.md 的前几个模块
2. 了解基本的用例结构
3. 学习如何编写简单的测试用例

### 中级 (能编写测试)
1. 学习 TEST_GUIDE.md 的"最佳实践"
2. 研究现有的测试代码
3. 尝试编写自己的单元测试

### 高级 (能维护和优化)
1. 理解所有测试框架和工具
2. 掌握性能测试和压力测试
3. 设计完整的测试方案

---

## 📚 相关资源链接

### 测试框架文档
- [JUnit 5官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Test官方文档](https://spring.io/guides/gs/testing-web/)
- [Mockito官方文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### 最佳实践
- [Google测试最佳实践](https://testing.googleblog.com/)
- [Martin Fowler博客](https://martinfowler.com/articles/testing-strategies.html)
