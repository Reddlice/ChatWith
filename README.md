# ChatWith

基于 Spring Boot 3.5.14 和 Vue 3 的 AI 聊天应用，封装 Anthropic Claude Code CLI 作为后端推理引擎，集成 GPT-SoVITS 语音合成与百度翻译，支持多会话管理和角色技能系统。

## 功能特性

- **Claude Code CLI 封装**：通过 `ProcessBuilder` 调用本地 Claude Code CLI，支持 `--session-id` / `--resume` 多轮对话
- **异步 TTS 语音合成管线**：中文回复 → 文本清洗 → 百度翻译转日语 → GPT-SoVITS 语音克隆 → Base64 WAV 存储
- **多会话管理**：会话的创建、重命名、删除、切换，元数据持久化到 `.claude/sessions.json`
- **聊天日志记录**：JSONL 格式保存到 `chat-logs/` 目录，兼容 LoRA 训练数据格式转换
- **角色技能系统**：默认集成「春日野穹」角色扮演技能（`sora-perspective`），支持自定义技能扩展
- **文件型持久化**：无需数据库，所有数据以 JSON / JSONL / WAV 文件存储

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.14 |
| JDK | Java | 17 |
| 构建工具 | Maven | 3.9+ (wrapper 内置) |
| 工具库 | Hutool | 5.8.27 |
| 简化代码 | Lombok | - |
| 前端框架 | Vue | 3.5.32 |
| 状态管理 | Pinia (含 persistedstate 插件) | 3.0.4 |
| 路由 | Vue Router | 5.0.6 |
| 构建工具 | Vite | 8.0.8 |
| AI 推理 | Claude Code CLI (`claude.exe`) | - |
| 语音合成 | GPT-SoVITS API v2 | - |
| 翻译 | 百度翻译 API (通用 NMT) | - |

## 前置要求

- **JDK 17+** — 后端编译与运行
- **Maven 3.8+** — 或使用项目内置的 `mvnw` wrapper
- **Node.js ^20.19.0 或 >=22.12.0** — 前端开发与构建
- **Claude Code CLI** — 已通过 npm 全局安装并完成认证（`claude login` 或 API Key 配置）
- **GPT-SoVITS v2**（可选）— 本地 TTS 服务，需独立启动
- **百度翻译 API 凭证**（可选）— 用于 TTS 管线中的中文→日语翻译

## 快速开始

### 1. 克隆项目

```bash
git clone <repo-url>
cd ChatWith
```

### 2. 配置 application.yml

编辑 `src/main/resources/application.yml`，修改以下关键配置（**必须修改**的项已标注）：

```yaml
claude-code:
  path: D:\path\to\claude.exe        # ⚠️ 必改：Claude Code CLI 可执行文件的绝对路径
  timeout: 300000                     # 超时时间 (ms)，默认 5 分钟

tts:
  url: http://127.0.0.1:9880         # GPT-SoVITS API 地址（可选，不需要 TTS 可忽略）
  ref-audio-path: D:\path\to\ref.wav # ⚠️ 必改：参考音频文件绝对路径

baidu:
  translate:
    app-id: your_app_id               # 百度翻译 APP ID（可选）
    secret-key: your_secret_key       # 百度翻译密钥（可选）
```

### 3. 启动后端

```bash
# 使用 Maven wrapper（无需安装 Maven）
./mvnw spring-boot:run

# 或者先打包再运行
./mvnw clean package -DskipTests
java -jar target/ChatWith-0.0.1-SNAPSHOT.jar
```

后端启动后监听 `http://localhost:8080`。

### 4. 启动前端

```bash
cd front/chat-with-front
npm install
npm run dev
```

前端开发服务器默认监听 `http://localhost:5173`，并自动将 `/chat-with` 请求代理到后端 8080 端口。

### 5. 验证

1. 浏览器打开 `http://localhost:5173`
2. 首次启动将自动创建默认会话
3. 输入消息并发送，等待 AI 文本回复
4. 若 TTS 服务已配置，音频将在文本回复后异步生成，按钮从「语音生成中...」变为可播放状态

## 配置参考

### claude-code

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `claude-code.path` | String | - | Claude Code CLI 可执行文件的绝对路径 |
| `claude-code.timeout` | int | `300000` | 子进程超时时间，单位毫秒 |
| `claude-code.skillsDir` | String | `.claude/skills` | 技能目录，相对于项目根目录 |

### setting

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `setting.skillName` | String | `sora-perspective` | 每次请求默认加载的技能名称 |
| `setting.systemPrompt` | String | 中文回复指令 | 追加在技能内容后的系统提示词 |

### tts

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `tts.url` | String | `http://127.0.0.1:9880` | GPT-SoVITS API v2 地址 |
| `tts.ref-audio-path` | String | - | 参考音频文件 (WAV) 的绝对路径 |
| `tts.prompt-text` | String | 日语示例句 | 参考音频对应的文本内容 |
| `tts.prompt-lang` | String | `ja` | 参考音频的语言代码 |
| `tts.text-lang` | String | `ja` | 合成目标语言代码 |
| `tts.media-type` | String | `wav` | 输出音频格式 |
| `tts.speed-factor` | float | `1.0` | 语速倍率 |

### baidu.translate

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `baidu.translate.app-id` | String | - | 百度翻译开放平台 APP ID |
| `baidu.translate.secret-key` | String | - | 百度翻译开放平台密钥 |
| `baidu.translate.api-url` | String | `https://fanyi-api.baidu.com/api/trans/vip/translate` | API 端点 |

### audio

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `audio.store-dir` | String | `audio-store` | TTS 音频 WAV 文件的持久化目录 |

## API 文档

所有端点均在 `/chat-with` 路径前缀下。

| 方法 | 路径 | 请求体 | 响应体 | 说明 |
|------|------|--------|--------|------|
| `POST` | `/chat-with/send` | `{"prompt": "..."}` | `{"reply": "...", "skillUsed": "...", "success": true, "messageId": "..."}` | 发送聊天消息，返回 AI 回复和音频追踪 ID |
| `GET` | `/chat-with/audio/{messageId}` | - | `200`: `{"audioBase64": "...", "ready": true}` / `204`: No Content | 轮询 TTS 音频结果（前端每 2 秒轮询） |
| `GET` | `/chat-with/audio/session/{sessionId}` | - | `200`: `{"msgId": "base64...", ...}` / `204`: No Content | 批量获取会话下所有音频（页面加载、切换会话时恢复） |
| `GET` | `/chat-with/sessions` | - | `[{"id": "...", "name": "...", "createdAt": "...", "resumed": bool}]` | 列出所有会话 |
| `POST` | `/chat-with/sessions` | `{"name": "..."}` | `{"id": "...", "name": "...", "createdAt": "...", "resumed": false}` | 创建新会话 |
| `PUT` | `/chat-with/sessions/{id}` | `{"name": "..."}` | - | 重命名会话 |
| `DELETE` | `/chat-with/sessions/{id}` | - | - | 删除会话及关联的 Claude Code 会话文件 |
| `PUT` | `/chat-with/sessions/{id}/activate` | - | - | 切换当前活跃会话 |
| `POST` | `/chat-with/log` | `{"sessionId": "...", "sessionName": "...", "createdAt": "...", "messages": [...]}` | - | 保存聊天日志到 JSONL 文件 |

## 项目结构

```
ChatWith/
├── pom.xml                              # Maven 构建配置
├── mvnw / mvnw.cmd                      # Maven Wrapper 脚本
├── .claude/                             # Claude Code 运行时数据
│   ├── settings.local.json              # Claude Code 权限配置
│   ├── sessions.json                    # 会话元数据 (ID/名称/创建时间/resumed 标记)
│   ├── .chatwith-session                # 当前活跃会话 UUID
│   └── skills/
│       └── sora-perspective/            # 春日野穹角色技能
│           ├── SKILL.md                 # 技能主文件 (YAML frontmatter + Markdown 正文)
│           └── references/research/     # 角色深度调研资料
├── src/main/java/org/reddlice/chatwith/
│   ├── ChatWithApplication.java         # Spring Boot 入口 (@EnableAsync)
│   ├── config/                          # 配置类 (@ConfigurationProperties)
│   │   ├── ClaudeCodeConfig.java        # claude-code.* → ClaudeCodeConfig
│   │   ├── TtsConfig.java               # tts.* → TtsConfig
│   │   ├── BaiduTranslateConfig.java    # baidu.translate.* → BaiduTranslateConfig
│   │   ├── AudioConfig.java             # audio.* → AudioConfig
│   │   └── DefaultSettingConfig.java    # setting.* → DefaultSettingConfig
│   ├── controller/
│   │   └── ChatController.java          # REST 控制器 (10 个端点)
│   ├── dto/                             # 数据传输对象
│   │   ├── ChatRequest.java
│   │   ├── ChatResponse.java
│   │   ├── ChatLogRequest.java
│   │   ├── SessionInfo.java
│   │   └── SkillInfo.java
│   ├── service/                         # 服务接口
│   │   ├── ChatService.java
│   │   ├── TtsService.java
│   │   ├── TranslateService.java
│   │   └── ChatLogService.java
│   ├── service/impl/                    # 服务实现
│   │   ├── ChatServiceImpl.java         # 核心聊天逻辑 (ProcessBuilder 调用 Claude Code CLI)
│   │   ├── TtsServiceImpl.java          # 异步 TTS 管线 (清洗→翻译→合成→存储)
│   │   ├── TranslateServiceImpl.java    # 百度翻译 API (MD5 签名, 自动检测→日语)
│   │   └── ChatLogServiceImpl.java      # JSONL 日志写入
│   ├── component/
│   │   ├── SessionManager.java          # 会话 CRUD + 文件持久化 + Claude Code 会话清理
│   │   └── AudioStore.java              # 音频内存缓存 (ConcurrentHashMap) + 磁盘读写
│   └── util/
│       └── TextCleaner.java             # 文本清洗 (去除括号及换行符, 为 TTS 准备)
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   └── SR000104.wav                     # TTS 参考音频 (日语女声)
├── front/chat-with-front/               # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js                   # Vite 配置 (代理 /chat-with → :8080)
│   ├── src/
│   │   ├── main.js                      # Vue 应用入口 (Pinia + Router)
│   │   ├── App.vue                      # 根组件
│   │   ├── router/index.js              # 路由 (单页: / → ChatView)
│   │   ├── stores/chat.js               # Pinia Store (消息/会话/音频轮询/日志)
│   │   ├── views/ChatView.vue           # 聊天主界面 (侧边栏 + 聊天气泡 + 音频播放)
│   │   └── assets/soro.png              # 春日野穹头像
│   └── index.html
├── audio-store/                         # TTS 音频持久化目录 (按 sessionId 组织)
├── chat-logs/                           # JSONL 聊天日志 (按 sessionId 命名)
├── api.md                               # GPT-SoVITS API v1/v2 参考文档
└── md/                                  # 实现设计文档 (中文)
    ├── step.md
    ├── step02.md
    ├── 会话.md
    ├── 异步.md
    ├── 日志.md
    └── 音频持久化.md
```

## 架构与数据流

### 聊天请求流程（文本响应）

```
[Vue 前端] --POST /chat-with/send {prompt}--> [ChatController]
  └─> ChatServiceImpl.chat()
        ├─> loadSkillContent(skillName)     → 读取 .claude/skills/<name>/SKILL.md
        ├─> merge system prompt              → 技能正文 + setting.systemPrompt
        ├─> SessionManager.getActiveSessionId()
        ├─> 决定 CLI 参数:
        │     resumed=false → claude.exe --session-id <uuid> -p "..." --system-prompt "..."
        │     resumed=true  → claude.exe --resume <uuid> -p "..."
        ├─> ProcessBuilder 启动子进程, waitFor(timeout), 读取 stdout
        ├─> UUID.randomUUID() → messageId
        ├─> (异步) ttsService.textToSpeechAsync(reply, messageId, sessionId)
        ├─> markSessionResumed(sessionId)
        └─> return ChatResponse {reply, messageId} → [Vue 前端]
```

### 异步 TTS 语音合成管线（@Async 线程池）

```
[TtsServiceImpl.textToSpeechAsync()] --@Async-->
  ├─> TextCleaner.cleanForTts(reply)
  │     └─ 去除全角（）半角()括号及换行符
  ├─> TranslateServiceImpl.toJapanese(cleanedText)
  │     └─ POST 百度翻译 API (MD5 签名, from=auto, to=jp)
  │     └─ 失败时降级返回中文原文
  ├─> POST GPT-SoVITS {ttsUrl}/tts
  │     └─ 使用参考音频进行语音克隆, 返回 WAV 字节
  ├─> Base64 编码
  └─> AudioStore.put(sessionId, messageId, base64)
        ├─ ConcurrentHashMap 内存缓存
        └─ 写入磁盘: audio-store/{sessionId}/{messageId}.wav

[Vue 前端] 每 2 秒轮询 GET /chat-with/audio/{messageId}
  ├─ 204 No Content → 继续轮询 (最多 30 次 / 60 秒)
  └─ 200 {audioBase64, ready:true} → 显示播放按钮
```

### 会话管理流程

```
SessionManager 在 .claude/sessions.json 中存储会话元数据:
  [{id, name, createdAt, resumed}, ...]

resumed 标志决定 CLI 参数:
  resumed=false → --session-id <uuid>  (新会话)
  resumed=true  → --resume <uuid>      (恢复会话)

Claude Code 原生的会话历史保存在:
  ~/.claude/projects/<project-hash>/<sessionId>.jsonl

删除会话时同步清理:
  ├─ .claude/sessions.json 中的条目
  ├─ ~/.claude/projects/<hash>/<id>.jsonl (Claude Code 会话历史)
  └─ ~/.claude/sessions/<pid>.json (Claude Code 会话跟踪文件)
```

## 技能系统

技能文件存放在 `.claude/skills/<skillName>/SKILL.md`（项目级）或 `~/.claude/skills/<skillName>.md`（用户级）。每个 SKILL.md 使用 YAML frontmatter 声明元数据（`name` / `description`），正文为 Markdown 格式的角色设定或行为指令。

启动时系统读取技能正文（去除 `---` 分隔的 frontmatter），将其与 `setting.systemPrompt` 拼接后，作为 `--system-prompt` 参数传给 Claude Code CLI。

### 默认技能：sora-perspective

基于《ヨスガノソラ》（缘之空）原作视觉小说、TV 动画及角色设定资料深度调研，提炼出 6 个核心心智模型、8 条行为决策启发式和完整表达 DNA，实现对春日野穹（Kasugano Sora）的角色模拟。技能文件包含约 25KB 的角色设定资料和深度研究报告。

### 自定义技能

1. 在 `.claude/skills/` 下创建新目录，放入 `SKILL.md`
2. 修改 `application.yml` 中 `setting.skillName` 为你的技能名称
3. 重启后端生效

## 降级与容错

整个 TTS 管线设计为「尽力而为」模式，任何环节失败都不会影响文本回复的正常返回：

| 环节 | 失败行为 |
|------|----------|
| 文本清洗 | 不会失败（纯正则替换） |
| 百度翻译 | 降级返回中文原文，TTS 直接用中文合成 |
| GPT-SoVITS TTS | 记录警告日志，返回 null，前端不显示播放按钮 |
| 音频轮询超时 | 前端最多轮询 30 次（60 秒），超时后清除音频状态 |
| localStorage 配额不足 | 音频数据不存入 localStorage；切换会话时通过 API 从磁盘恢复 |
| Claude Code CLI 超时/异常 | 返回 error 信息，`success: false` |

## 注意事项

1. **路径配置**：`application.yml` 中的 `claude-code.path` 和 `tts.ref-audio-path` 为开发者的本地绝对路径，其他开发者克隆后**必须修改**这两项
2. **Claude Code 认证**：需单独安装并完成认证（`claude login` 或配置 API Key），否则 Claude Code CLI 无法正常调用
3. **GPT-SoVITS 独立运行**：TTS 服务需单独启动，参考命令：`python api_v2.py -a 127.0.0.1 -p 9880`，详见项目根目录的 `api.md`
4. **百度翻译凭证**：需在 [百度翻译开放平台](https://fanyi-api.baidu.com/) 注册开发者账号获取 APP ID 和密钥
5. **会话数据分离**：ChatWith 管理元数据在 `.claude/sessions.json`，Claude Code 管理实际对话历史在 `~/.claude/projects/`，两者通过 UUID 关联
6. **开发模式代理**：Vite 的前端代理 (`/chat-with` → `localhost:8080`) 仅在开发模式生效；生产环境需配置 Nginx 反向代理，或将前端构建产物放入 Spring Boot 静态资源目录
7. **操作系统**：项目在 Windows 上开发，配置文件使用反斜杠路径；Java 代码中使用正斜杠（跨平台兼容）

## 生产构建

```bash
# --- 后端 ---
./mvnw clean package -DskipTests
# 产物: target/ChatWith-0.0.1-SNAPSHOT.jar

# --- 前端 ---
cd front/chat-with-front
npm run build
# 产物: dist/

# --- 部署方式一: Nginx 反向代理 ---
# 将 dist/ 部署到 Nginx, /chat-with 代理到后端 8080 端口

# --- 部署方式二: 前后端合一 ---
# 将 dist/ 内容复制到 src/main/resources/static/ 后重新打包 JAR
```

## 常见问题

**Q: Claude Code 进程超时怎么办？**
A: 增大 `application.yml` 中的 `claude-code.timeout`（单位毫秒，默认 300000 = 5 分钟）。

**Q: 前端出现 CORS 错误？**
A: 确保 Vite 开发服务器已启动且代理配置正确（`vite.config.js` 中 `/chat-with` → `http://localhost:8080`），同时确保后端运行在 8080 端口。

**Q: TTS 音频一直显示「语音生成中...」？**
A: 检查 GPT-SoVITS 服务是否启动（`http://127.0.0.1:9880`），百度翻译凭证是否有效，参考音频路径是否存在。

**Q: 提示「技能未找到: sora-perspective」？**
A: 确认 `.claude/skills/sora-perspective/SKILL.md` 文件存在于项目根目录下。

**Q: 端口 8080 已被占用？**
A: 修改 `application.yml` 中的 `server.port` 为其他端口，同时更新 `vite.config.js` 中的代理目标端口。

**Q: Claude Code CLI 返回非零退出码？**
A: 查看 Spring Boot 控制台日志中的 stdout/stderr 输出，排查 Claude Code 认证或账号问题。
