/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.example.manus.tool.innerStorage;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.example.manus.config.ManusProperties;
import com.alibaba.cloud.ai.example.manus.tool.ToolCallBiFunctionDef;
import com.alibaba.cloud.ai.example.manus.tool.code.CodeUtils;
import com.alibaba.cloud.ai.example.manus.tool.code.ToolExecuteResult;
import com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartFileOperator;
import com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 *
 * 内部存储工具，用于MapReduce流程中的中间数据管理 自动管理基于planID和Agent的目录结构，提供简化的文件操作
 * 支持智能内容管理：当返回内容过长时自动存储并返回摘要
 *
 */
public class InnerStorageTool implements ToolCallBiFunctionDef {

	private static final Logger log = LoggerFactory.getLogger(InnerStorageTool.class);

	private final String workingDirectoryPath;

	private final InnerStorageService innerStorageService;

	private final SmartFileOperator smartFileOperator;

	private String planId;

	public InnerStorageTool(InnerStorageService innerStorageService, SmartFileOperator smartFileOperator) {
		this.innerStorageService = innerStorageService;
		this.smartFileOperator = smartFileOperator;
		ManusProperties manusProperties = innerStorageService.getManusProperties();
		workingDirectoryPath = CodeUtils.getWorkingDirectory(manusProperties.getBaseDir());
	}

	/**
	 * 向后兼容的构造函数
	 */
	public InnerStorageTool(InnerStorageService innerStorageService) {
		this.innerStorageService = innerStorageService;
		this.smartFileOperator = new SmartFileOperator(innerStorageService);
		ManusProperties manusProperties = innerStorageService.getManusProperties();
		workingDirectoryPath = CodeUtils.getWorkingDirectory(manusProperties.getBaseDir());
	}

	/**
	 * 测试专用构造函数，直接指定工作目录路径
	 */
	public InnerStorageTool(InnerStorageService innerStorageService, SmartFileOperator smartFileOperator,
			String workingDirectoryPath) {
		this.innerStorageService = innerStorageService;
		this.smartFileOperator = smartFileOperator;
		this.workingDirectoryPath = workingDirectoryPath;
	}

	/**
	 * 测试专用构造函数（向后兼容），直接指定工作目录路径
	 */
	public InnerStorageTool(InnerStorageService innerStorageService, String workingDirectoryPath) {
		this.innerStorageService = innerStorageService;
		this.smartFileOperator = new SmartFileOperator(innerStorageService);
		this.workingDirectoryPath = workingDirectoryPath;
	}

	private static final String TOOL_NAME = "inner_storage_tool";

	private static final String TOOL_DESCRIPTION = """
			内部存储工具，用于所有大量字符类中间结果的处理。
			可以根据一个key找到对应的文件，并提供如下能力：
			- append: 向文件追加内容（自动创建文件和目录）
			- replace: 替换文件中的特定文本
			- get_lines: 获取文件的指定行号范围内容，最多不超过300行一次
			- list_contents: 列出当前任务相关的所有内容ID和摘要
			- get_content: 根据内容ID获取详细内容

			当返回内容过长时，工具会自动存储详细内容并返回摘要和内容ID，以降低上下文压力。

			""";

	private static final String PARAMETERS = """
			{
			    "type": "object",
			    "oneOf": [
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "append",
			                    "description": "向文件追加内容"
			                },
			                "file_name": {
			                    "type": "string",
			                    "description": "文件名（带扩展名），不需要带目录路径"
			                },
			                "content": {
			                    "type": "string",
			                    "description": "要追加的内容"
			                }
			            },
			            "required": ["action", "file_name", "content"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "replace",
			                    "description": "替换文件中的特定文本"
			                },
			                "file_name": {
			                    "type": "string",
			                    "description": "文件名（带扩展名），不需要带目录路径"
			                },
			                "source_text": {
			                    "type": "string",
			                    "description": "要被替换的文本"
			                },
			                "target_text": {
			                    "type": "string",
			                    "description": "替换后的文本"
			                }
			            },
			            "required": ["action", "file_name", "source_text", "target_text"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "get_lines",
			                    "description": "获取文件的指定行号范围内容"
			                },
			                "file_name": {
			                    "type": "string",
			                    "description": "文件名（带扩展名），不需要带目录路径"
			                },
			                "start_line": {
			                    "type": "integer",
			                    "description": "起始行号，默认为1",
			                    "minimum": 1
			                },
			                "end_line": {
			                    "type": "integer",
			                    "description": "结束行号，默认为文件末尾",
			                    "minimum": 1
			                }
			            },
			            "required": ["action", "file_name"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "list_contents",
			                    "description": "列出当前任务相关的所有内容ID和摘要"
			                }
			            },
			            "required": ["action"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "get_content",
			                    "description": "根据内容ID获取详细内容"
			                },
			                "content_id": {
			                    "type": "string",
			                    "description": "内容ID，用于获取特定的存储内容"
			                }
			            },
			            "required": ["action", "content_id"],
			            "additionalProperties": false
			        }
			    ]
			}
			""";

	@Override
	public String getName() {
		return TOOL_NAME;
	}

	@Override
	public String getDescription() {
		return TOOL_DESCRIPTION;
	}

	@Override
	public String getParameters() {
		return PARAMETERS;
	}

	@Override
	public Class<?> getInputType() {
		return String.class;
	}

	@Override
	public boolean isReturnDirect() {
		return false;
	}

	@Override
	public void setPlanId(String planId) {
		this.planId = planId;
	}

	@Override
	public String getServiceGroup() {
		return "inner-storage";
	}

	/**
	 * 使用 SmartFileOperator 处理结果
	 */
	private ToolExecuteResult processResult(ToolExecuteResult result, String operationType, String fileName) {
		if (result == null || result.getOutput() == null) {
			return result;
		}
		
		SmartProcessResult processedResult = 
			smartFileOperator.processResult(result.getOutput(), planId, workingDirectoryPath);
		
		return new ToolExecuteResult(processedResult.getSummary());
	}

	public static OpenAiApi.FunctionTool getToolDefinition() {
		OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(TOOL_DESCRIPTION, TOOL_NAME,
				PARAMETERS);
		return new OpenAiApi.FunctionTool(function);
	}

	public static FunctionToolCallback<String, ToolExecuteResult> getFunctionToolCallback(
			InnerStorageService innerStorageService, SmartFileOperator smartFileOperator) {
		return FunctionToolCallback.builder(TOOL_NAME, new InnerStorageTool(innerStorageService, smartFileOperator))
			.description(TOOL_DESCRIPTION)
			.inputSchema(PARAMETERS)
			.inputType(String.class)
			.build();
	}

	public static FunctionToolCallback<String, ToolExecuteResult> getFunctionToolCallback(
			InnerStorageService innerStorageService) {
		return FunctionToolCallback.builder(TOOL_NAME, new InnerStorageTool(innerStorageService))
			.description(TOOL_DESCRIPTION)
			.inputSchema(PARAMETERS)
			.inputType(String.class)
			.build();
	}

	public static FunctionToolCallback<String, ToolExecuteResult> getFunctionToolCallback(String planId,
			InnerStorageService innerStorageService, SmartFileOperator smartFileOperator) {
		InnerStorageTool tool = new InnerStorageTool(innerStorageService, smartFileOperator);
		tool.setPlanId(planId);
		return FunctionToolCallback.builder(TOOL_NAME, tool)
			.description(TOOL_DESCRIPTION)
			.inputSchema(PARAMETERS)
			.inputType(String.class)
			.build();
	}

	public static FunctionToolCallback<String, ToolExecuteResult> getFunctionToolCallback(String planId,
			InnerStorageService innerStorageService) {
		InnerStorageTool tool = new InnerStorageTool(innerStorageService);
		tool.setPlanId(planId);
		return FunctionToolCallback.builder(TOOL_NAME, tool)
			.description(TOOL_DESCRIPTION)
			.inputSchema(PARAMETERS)
			.inputType(String.class)
			.build();
	}

	public ToolExecuteResult run(String toolInput) {
		log.info("InnerStorageTool toolInput: {}", toolInput);
		try {
			Map<String, Object> toolInputMap = new ObjectMapper().readValue(toolInput,
					new TypeReference<Map<String, Object>>() {
					});

			String action = (String) toolInputMap.get("action");
			if (action == null) {
				return new ToolExecuteResult("错误：action参数是必需的");
			}

			return switch (action) {
				case "append" -> {
					String fileName = (String) toolInputMap.get("file_name");
					String content = (String) toolInputMap.get("content");
					ToolExecuteResult result = appendToFile(fileName, content);
					yield processResult(result, "append", fileName);
				}
				case "replace" -> {
					String fileName = (String) toolInputMap.get("file_name");
					String sourceText = (String) toolInputMap.get("source_text");
					String targetText = (String) toolInputMap.get("target_text");
					ToolExecuteResult result = replaceInFile(fileName, sourceText, targetText);
					yield processResult(result, "replace", fileName);
				}
				case "get_lines" -> {
					String fileName = (String) toolInputMap.get("file_name");
					Integer startLine = (Integer) toolInputMap.get("start_line");
					Integer endLine = (Integer) toolInputMap.get("end_line");
					ToolExecuteResult result = getFileLines(fileName, startLine, endLine);
					yield processResult(result, "get_lines", fileName);
				}
				case "list_contents" -> {
					ToolExecuteResult result = listStoredContents();
					yield processResult(result, "list_contents", null);
				}
				case "get_content" -> {
					String contentId = (String) toolInputMap.get("content_id");
					yield getStoredContent(contentId);
				}
				case "set_agent" -> new ToolExecuteResult("错误：set_agent 操作已不再支持。Agent 应该在工具初始化时设置。");
				default -> new ToolExecuteResult("未知操作: " + action);
			};

		}
		catch (Exception e) {
			log.error("InnerStorageTool执行失败", e);
			return new ToolExecuteResult("工具执行失败: " + e.getMessage());
		}
	}

	/**
	 * 追加内容到文件
	 */
	private ToolExecuteResult appendToFile(String fileName, String content) {
		try {
			if (fileName == null || fileName.trim().isEmpty()) {
				return new ToolExecuteResult("错误：file_name参数是必需的");
			}
			if (content == null) {
				content = "";
			}

			// 确保目录存在
			String agentName = innerStorageService.getPlanAgent(planId);
			Path agentDir = innerStorageService.getAgentDirectory(workingDirectoryPath, planId, agentName);
			innerStorageService.ensureDirectoryExists(agentDir);

			// 获取文件路径并追加内容
			Path filePath = innerStorageService.getFilePath(workingDirectoryPath, planId, fileName);

			// 如果文件不存在，创建新文件
			if (!Files.exists(filePath)) {
				Files.writeString(filePath, content);
				return new ToolExecuteResult(String.format("文件创建成功并添加内容: %s", fileName));
			}
			else {
				// 追加内容（添加换行符）
				Files.writeString(filePath, "\n" + content, StandardOpenOption.APPEND);
				return new ToolExecuteResult(String.format("内容追加成功: %s", fileName));
			}

		}
		catch (IOException e) {
			log.error("追加文件失败", e);
			return new ToolExecuteResult("追加文件失败: " + e.getMessage());
		}
	}

	/**
	 * 替换文件中的文本
	 */
	private ToolExecuteResult replaceInFile(String fileName, String sourceText, String targetText) {
		try {
			if (fileName == null || fileName.trim().isEmpty()) {
				return new ToolExecuteResult("错误：file_name参数是必需的");
			}
			if (sourceText == null || targetText == null) {
				return new ToolExecuteResult("错误：source_text和target_text参数都是必需的");
			}

			Path filePath = innerStorageService.getFilePath(workingDirectoryPath, planId, fileName);

			if (!Files.exists(filePath)) {
				return new ToolExecuteResult("错误：文件不存在: " + fileName);
			}

			String content = Files.readString(filePath);
			String newContent = content.replace(sourceText, targetText);
			Files.writeString(filePath, newContent);

			return new ToolExecuteResult(String.format("文本替换成功: %s", fileName));

		}
		catch (IOException e) {
			log.error("替换文件文本失败", e);
			return new ToolExecuteResult("替换文件文本失败: " + e.getMessage());
		}
	}

	/**
	 * 获取文件的指定行号内容
	 */
	private ToolExecuteResult getFileLines(String fileName, Integer startLine, Integer endLine) {
		try {
			if (fileName == null || fileName.trim().isEmpty()) {
				return new ToolExecuteResult("错误：file_name参数是必需的");
			}

			Path filePath = innerStorageService.getFilePath(workingDirectoryPath, planId, fileName);

			if (!Files.exists(filePath)) {
				return new ToolExecuteResult("错误：文件不存在: " + fileName);
			}

			List<String> lines = Files.readAllLines(filePath);

			if (lines.isEmpty()) {
				return new ToolExecuteResult("文件为空");
			}

			// 设置默认值
			int start = (startLine != null && startLine > 0) ? startLine - 1 : 0;
			int end = (endLine != null && endLine > 0) ? Math.min(endLine, lines.size()) : lines.size();

			// 验证范围
			if (start >= lines.size()) {
				return new ToolExecuteResult("起始行号超出文件范围");
			}

			if (start >= end) {
				return new ToolExecuteResult("起始行号不能大于或等于结束行号");
			}

			StringBuilder result = new StringBuilder();
			result.append(String.format("文件: %s (第%d-%d行，共%d行)\n", fileName, start + 1, end, lines.size()));
			result.append("=".repeat(50)).append("\n");

			for (int i = start; i < end; i++) {
				result.append(String.format("%4d: %s\n", i + 1, lines.get(i)));
			}

			return new ToolExecuteResult(result.toString());

		}
		catch (IOException e) {
			log.error("读取文件行失败", e);
			return new ToolExecuteResult("读取文件行失败: " + e.getMessage());
		}
	}

	/**
	 * 获取自动存储的文件（以 auto_ 开头的文件）
	 */
	private List<InnerStorageService.FileInfo> getAutoStoredFiles() {
		List<InnerStorageService.FileInfo> allFiles = innerStorageService.getDirectoryFiles(workingDirectoryPath,
				planId);
		return allFiles.stream()
			.filter(file -> file.getRelativePath().contains("auto_"))
			.collect(java.util.stream.Collectors.toList());
	}

	/**
	 * 列出当前任务相关的所有存储内容
	 */
	private ToolExecuteResult listStoredContents() {
		try {
			StringBuilder contentList = new StringBuilder();
			contentList.append("📋 当前任务存储内容列表\n\n");

			// 列出文件内容
			List<InnerStorageService.FileInfo> files = innerStorageService.getDirectoryFiles(workingDirectoryPath,
					planId);
			if (!files.isEmpty()) {
				contentList.append("📁 文件内容:\n");
				for (int i = 0; i < files.size(); i++) {
					InnerStorageService.FileInfo file = files.get(i);
					contentList.append(String.format("  [%d] %s (%d bytes, %s)\n", i + 1, file.getRelativePath(),
							file.getSize(), file.getLastModified()));
				}
				contentList.append("\n");
			}

			// 列出自动存储的内容
			List<InnerStorageService.FileInfo> autoStoredFiles = getAutoStoredFiles();
			if (!autoStoredFiles.isEmpty()) {
				contentList.append("🤖 自动存储的内容:\n");
				for (int i = 0; i < autoStoredFiles.size(); i++) {
					InnerStorageService.FileInfo file = autoStoredFiles.get(i);
					contentList.append(String.format("  [auto_%d] %s (%d bytes, %s)\n", i + 1, file.getRelativePath(),
							file.getSize(), file.getLastModified()));
				}
				contentList.append("\n");
			}

			if (files.isEmpty() && autoStoredFiles.isEmpty()) {
				contentList.append("❌ 当前任务没有存储的内容");
			}
			else {
				contentList.append("💡 提示:\n");
				contentList.append("  - 使用 get_lines 操作读取文件内容\n");
				contentList.append("  - 使用 get_content 操作根据ID获取内容");
			}

			return new ToolExecuteResult(contentList.toString());

		}
		catch (Exception e) {
			log.error("列出存储内容失败", e);
			return new ToolExecuteResult("列出内容失败: " + e.getMessage());
		}
	}

	/**
	 * 根据内容ID获取存储的内容
	 */
	private ToolExecuteResult getStoredContent(String contentId) {
		if (contentId == null || contentId.trim().isEmpty()) {
			return new ToolExecuteResult("错误：content_id参数是必需的");
		}

		try {
			// 尝试解析内容ID
			if ("desc".equals(contentId)) {
				// 获取自动存储内容的概览作为描述
				List<InnerStorageService.FileInfo> autoStoredFiles = innerStorageService
					.searchAutoStoredFiles(workingDirectoryPath, planId, "");
				if (!autoStoredFiles.isEmpty()) {
					StringBuilder desc = new StringBuilder();
					desc.append("任务 ").append(planId).append(" 的自动存储内容详情:\n\n");
					for (InnerStorageService.FileInfo file : autoStoredFiles) {
						try {
							String content = innerStorageService.readFileContent(workingDirectoryPath, planId,
									file.getRelativePath());
							desc.append("📄 ").append(file.getRelativePath()).append(":\n");
							desc.append(content).append("\n\n");
						}
						catch (IOException e) {
							desc.append("❌ 无法读取文件: ").append(file.getRelativePath()).append("\n\n");
						}
					}
					return new ToolExecuteResult(desc.toString());
				}
				else {
					return new ToolExecuteResult("未找到任何自动存储的内容");
				}
			}

			// 尝试按数字索引获取文件内容
			try {
				int index = Integer.parseInt(contentId) - 1; // 转换为0基索引
				List<InnerStorageService.FileInfo> files = innerStorageService.getDirectoryFiles(workingDirectoryPath,
						planId);

				if (index >= 0 && index < files.size()) {
					InnerStorageService.FileInfo file = files.get(index);
					// 使用 planDirectory + relativePath 来构建完整路径
					Path planDir = innerStorageService.getPlanDirectory(workingDirectoryPath, planId);
					Path filePath = planDir.resolve(file.getRelativePath());

					if (Files.exists(filePath)) {
						String content = Files.readString(filePath);
						ToolExecuteResult result = new ToolExecuteResult(
								String.format("📁 文件: %s\n%s\n%s", file.getRelativePath(), "=".repeat(50), content));
						return processResult(result, "get_content", file.getRelativePath());
					}
				}
			}
			catch (NumberFormatException e) {
				// 不是数字，尝试按文件名查找
				List<InnerStorageService.FileInfo> files = innerStorageService.getDirectoryFiles(workingDirectoryPath,
						planId);
				for (InnerStorageService.FileInfo file : files) {
					if (file.getRelativePath().contains(contentId)) {
						Path planDir = innerStorageService.getPlanDirectory(workingDirectoryPath, planId);
						Path filePath = planDir.resolve(file.getRelativePath());

						if (Files.exists(filePath)) {
							String content = Files.readString(filePath);
							ToolExecuteResult result = new ToolExecuteResult(String.format("📁 文件: %s\n%s\n%s",
									file.getRelativePath(), "=".repeat(50), content));
							return processResult(result, "get_content", file.getRelativePath());
						}
					}
				}
			}

			return new ToolExecuteResult("未找到内容ID为 '" + contentId + "' 的内容。请使用 list_contents 查看可用的内容ID。");

		}
		catch (IOException e) {
			log.error("获取存储内容失败", e);
			return new ToolExecuteResult("获取内容失败: " + e.getMessage());
		}
	}

	@Override
	public String getCurrentToolStateString() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("InnerStorage 当前状态:\n");
			sb.append("- Plan ID: ").append(planId != null ? planId : "未设置").append("\n");
			sb.append("- 工作目录: ").append(workingDirectoryPath).append("\n");

			// 获取当前目录下的所有文件信息
			List<InnerStorageService.FileInfo> files = innerStorageService.getDirectoryFiles(workingDirectoryPath,
					planId);

			if (files.isEmpty()) {
				sb.append("- 内部文件: 无\n");
			}
			else {
				sb.append("- 内部文件 (").append(files.size()).append("个):\n");
			}

			return sb.toString();
		}
		catch (Exception e) {
			log.error("获取工具状态失败", e);
			return "InnerStorage 状态获取失败: " + e.getMessage();
		}
	}

	@Override
	public void cleanup(String planId) {
		if (planId != null) {
			log.info("Cleaning up inner storage for plan: {}", planId);
			innerStorageService.cleanupPlan(workingDirectoryPath, planId);
		}
	}

	@Override
	public ToolExecuteResult apply(String s, ToolContext toolContext) {
		return run(s);
	}

}
