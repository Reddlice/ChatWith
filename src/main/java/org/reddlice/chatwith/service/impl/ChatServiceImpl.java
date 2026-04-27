package org.reddlice.chatwith.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.config.ClaudeCodeConfig;
import org.reddlice.chatwith.dto.ChatRequest;
import org.reddlice.chatwith.dto.ChatResponse;
import org.reddlice.chatwith.service.ChatService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ClaudeCodeConfig claudeCodeConfig;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        String prompt = chatRequest.getPrompt();
        String skillName = chatRequest.getSkillName();
        String skillContent = null;

        // 1. 如果指定了技能名称，加载技能内容
        if (skillName != null && !skillName.isBlank()) {
            skillContent = loadSkillContent(skillName);
            if (skillContent == null) {
                log.warn("技能未找到: {}", skillName);
                return new ChatResponse("技能未找到: " + skillName, skillName, false);
            }
        }

        // 2. 合并系统提示：skill 内容 + 请求中的 systemPrompt
        String systemPrompt = skillContent;
        if (chatRequest.getSystemPrompt() != null && !chatRequest.getSystemPrompt().isBlank()) {
            if (systemPrompt != null) {
                systemPrompt = systemPrompt + "\n\n" + chatRequest.getSystemPrompt();
            } else {
                systemPrompt = chatRequest.getSystemPrompt();
            }
        }

        // 3. 构建命令
        List<String> command = new ArrayList<>();
        command.add(claudeCodeConfig.getPath());
        command.add("-p");
        command.add(prompt);
        if (systemPrompt != null) {
            command.add("--system-prompt");
            command.add(systemPrompt);
        }

//        log.info("执行命令: {}", String.join(" ", command));

        try {
            // 3. 启动子进程
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            // 4. 等待完成（带超时）
            boolean finished = process.waitFor(claudeCodeConfig.getTimeout(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("Claude Code 子进程超时 ({}ms)", claudeCodeConfig.getTimeout());
                return new ChatResponse("请求超时", skillName, false);
            }

            // 5. 读取 stdout
            StringBuilder replyBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (replyBuilder.length() > 0) {
                        replyBuilder.append("\n");
                    }
                    replyBuilder.append(line);
                }
            }
            String reply = replyBuilder.toString();

            // 6. 检查退出码
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                StringBuilder errorBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), Charset.defaultCharset()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (errorBuilder.length() > 0) {
                            errorBuilder.append("\n");
                        }
                        errorBuilder.append(line);
                    }
                }
                log.error("Claude Code 退出码: {}, stderr: {}", exitCode, errorBuilder);
                return new ChatResponse("Claude Code 执行错误 (退出码 " + exitCode + ")", skillName, false);
            }

            log.info("Chat 成功, skill={}, reply length={} replay = {}", skillName, reply.length() ,reply);
            return new ChatResponse(reply, skillName, true);

        } catch (Exception e) {
            log.error("执行 Claude Code 时异常", e);
            return new ChatResponse("系统错误: " + e.getMessage(), skillName, false);
        }
    }

    /**
     * 查找并加载技能正文（去掉 YAML frontmatter）
     */
    private String loadSkillContent(String skillName) {
        // 1. 项目级: .claude/skills/<skillName>/SKILL.md
        Path projectPath = Paths.get(claudeCodeConfig.getSkillsDir(), skillName, "SKILL.md");
        if (Files.isRegularFile(projectPath)) {
            return extractSkillBody(projectPath);
        }

        // 2. 用户级: ~/.claude/skills/<skillName>.md
        String userHome = System.getProperty("user.home");
        Path userPath = Paths.get(userHome, ".claude", "skills", skillName + ".md");
        if (Files.isRegularFile(userPath)) {
            return extractSkillBody(userPath);
        }

        return null;
    }

    /**
     * 读取 SKILL.md，去掉 YAML frontmatter（两个 --- 之间的内容），返回纯正文
     */
    private String extractSkillBody(Path path) {
        try {
            List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
            StringBuilder body = new StringBuilder();
            int delimCount = 0;

            for (String line : lines) {
                if (line.trim().equals("---")) {
                    delimCount++;
                    continue;
                }
                if (delimCount >= 2) {
                    if (body.length() > 0) {
                        body.append("\n");
                    }
                    body.append(line);
                }
            }

            return body.toString();
        } catch (Exception e) {
            log.warn("读取技能内容失败: {}", path, e);
            return null;
        }
    }
}
