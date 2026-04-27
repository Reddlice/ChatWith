# ChatWith 实现步骤

## 第一步：配置文件
1. 删除 `application.properties`，创建 `application.yml`，写入 Spring 基础配置和 `claude-code` 自定义配置（路径、超时、技能目录）
2. 创建 `config/ClaudeCodeConfig.java` —— 用 `@ConfigurationProperties(prefix = "claude-code")` 读取配置

## 第二步：DTO 数据传输对象（3 个文件）
1. **`dto/ChatRequest.java`** —— 请求体：`prompt`（必填）、`skillName`（可选）、`systemPrompt`（可选）
2. **`dto/ChatResponse.java`** —— 响应体：`reply`、`skillUsed`、`success`
3. **`dto/SkillInfo.java`** —— 技能信息：`name`、`description`

## 第三步：核心服务 `service/ClaudeCodeService.java`
两个核心方法：
- **`chat(ChatRequest)`** —— 用 `ProcessBuilder` 启动 `claude -p "提示词"` 子进程，等它执行完，读 stdout 返回
  - 如果请求指定了 `skillName`，读取对应的 `SKILL.md`，去掉 YAML 前置元数据，把内容通过 `--system-prompt` 传进去
  - 配置超时控制，防止请求卡死
- **`listSkills()`** —— 扫描 `.claude/skills/` 目录，解析每个 `SKILL.md` 的 YAML frontmatter，提取 name 和 description

## 第四步：控制器 `controller/ChatController.java`
两个 REST 端点：
- **`POST /api/chat`** —— 接收 `ChatRequest` JSON，调用 Service，返回 `ChatResponse` JSON
- **`GET /api/skills`** —— 返回技能列表

## 第五步：构建验证
- `./mvnw clean package` 确认编译通过
- `./mvnw spring-boot:run` 启动服务
- 用 curl 测试三个场景：
  - `GET /api/skills` 列出技能
  - `POST /api/chat` 无 skill 的普通对话
  - `POST /api/chat` 带 `skillName` 的技能对话
