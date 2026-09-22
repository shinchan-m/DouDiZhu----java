# 斗地主小游戏

Java 17 + Swing 实现的单机斗地主游戏，支持真人玩家、两名 AI 玩家、难度选择、完整牌型校验、倒计时和本地账号注册。

## 环境要求

- JDK 17
- Maven Wrapper 已包含在项目中，不需要单独安装 Maven
- Windows、macOS、Linux 均可运行

## 快速运行

### Windows

双击：

`run.bat`

或者在命令行执行：

```bat
mvnw.cmd clean package
java -jar target\doudizhu.jar
```

### macOS / Linux

```bash
sh run.sh
```

或者：

```bash
sh ./mvnw clean package
java -jar target/doudizhu.jar
```

### IntelliJ IDEA

1. 使用 IDEA 打开项目根目录，IDEA 会根据 `pom.xml` 自动导入 Maven 项目。
2. Project SDK 选择 JDK 17。
3. 运行 `src/main/java/App.java` 中的 `main` 方法。
4. Run Configuration 的 Working directory 使用项目根目录。

## 首次使用

1. 启动程序后点击“注册”。
2. 注册用户名和密码。
3. 返回登录界面，输入账号和验证码。
4. 选择简单、中等或困难难度。
5. 进入牌桌开始游戏。

游戏左上角的“重玩”按钮可以随时重新开局，当前难度和累计比分保持不变。

真人玩家每次出牌有 60 秒倒计时。每局结束后不会自动续局，必须明确选择“继续下一局”或“退出游戏”。

## DeepSeek AI

中等和困难难度使用 DeepSeek 官方接口：

- `base_url`：`https://api.deepseek.com`
- `model`：`deepseek-flash`
- Thinking：关闭
- 输出：严格 JSON

在登录界面点击“AI设置”，粘贴 API Key 并保存即可，不需要手动打开配置文件。

简单难度不调用 DeepSeek；未配置 API Key 时，中等和困难也会自动使用本地兜底策略。

## 数据目录

程序不会把账号或 API Key 写进项目源码，而是统一保存在当前用户目录：

`~/.doudizhu/`

Windows 对应：

`C:\Users\你的用户名\.doudizhu\`

包含：

- `users.txt`：注册账号
- `deepseek.key`：DeepSeek API Key

账号密码按项目需求使用明文保存，请勿使用重要密码。

## 项目结构

```text
src/main/java         Java 源码
src/main/resources    图片资源，会打包进 JAR
src/test/java         本地测试
pom.xml               Maven 配置
mvnw / mvnw.cmd       Maven Wrapper
run.bat / run.sh      一键构建并启动
test.bat              本地测试脚本
需求文档.md            完整需求说明
```

## 测试

Windows 双击 `test.bat`。

也可以在 Maven 打包后运行：

```bat
java -cp "target\test-classes;target\doudizhu.jar" CardRulesTest
java -cp "target\test-classes;target\doudizhu.jar" AIResponseParserTest
java -cp "target\test-classes;target\doudizhu.jar" CodeUtilTest
```
