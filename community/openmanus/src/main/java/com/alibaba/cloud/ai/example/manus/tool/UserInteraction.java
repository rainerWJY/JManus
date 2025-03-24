package com.alibaba.cloud.ai.example.manus.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.function.FunctionToolCallback;
import com.alibaba.cloud.ai.example.manus.tool.support.ToolExecuteResult;

public class UserInteraction implements BiFunction<String, ToolContext, ToolExecuteResult> {

    private static final Logger log = LoggerFactory.getLogger(UserInteraction.class);

    private static String PARAMETERS = """
        {
            "type": "object",
            "properties": {
                "message": {
                    "type": "string",
                    "description": "(required) The message to send to the user."
                },
                "type": {
                    "type": "string",
                    "description": "(optional) The type of message (info, warning, error).",
                    "enum": ["info", "warning", "error"],
                    "default": "info"
                }
            },
            "required": ["message"]
        }
        """;

    private static final String name = "user_interaction";

    private static final String description = """
        Send messages to the user during task execution.
        Use this tool when you need to:
        - Ask for user confirmation
        - Provide progress updates
        - Request additional information
        - Show intermediate results
        The message will be displayed in the user interface.
        """;

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        return new OpenAiApi.FunctionTool(function);
    }

    public static FunctionToolCallback getFunctionToolCallback() {
        return FunctionToolCallback.builder(name, new UserInteraction())
            .description(description)
            .inputSchema(PARAMETERS)
            .inputType(String.class)
            .build();
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("UserInteraction toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String message = (String) toolInputMap.get("message");
            String type = (String) toolInputMap.getOrDefault("type", "info");

            // 格式化消息，添加适当的前缀
            String formattedMessage = formatMessage(message, type);
            
            log.info("Sending message to user: " + formattedMessage);
            return new ToolExecuteResult(formattedMessage);
        } catch (Exception e) {
            log.error("Error processing user interaction", e);
            return new ToolExecuteResult("Error: Failed to process message - " + e.getMessage());
        }
    }

    private String formatMessage(String message, String type) {
        String prefix = switch (type.toLowerCase()) {
            case "warning" -> "warning: ";
            case "error" -> "error: ";
            default -> "default: ";
        };
        return prefix + message;
    }

    @Override
    public ToolExecuteResult apply(String s, ToolContext toolContext) {
        return run(s);
    }
}
