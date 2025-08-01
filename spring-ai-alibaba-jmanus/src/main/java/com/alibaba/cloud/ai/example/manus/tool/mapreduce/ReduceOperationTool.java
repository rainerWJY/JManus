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
package com.alibaba.cloud.ai.example.manus.tool.mapreduce;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.cloud.ai.example.manus.tool.AbstractBaseTool;
import com.alibaba.cloud.ai.example.manus.tool.TerminableTool;
import com.alibaba.cloud.ai.example.manus.tool.code.ToolExecuteResult;
import com.alibaba.cloud.ai.example.manus.tool.filesystem.UnifiedDirectoryManager;
import com.alibaba.cloud.ai.example.manus.config.ManusProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * Reduce operation tool for MapReduce workflow Supports append operation for file
 * manipulation in root plan directory
 */
public class ReduceOperationTool extends AbstractBaseTool<ReduceOperationTool.ReduceOperationInput>
		implements TerminableTool {

	private static final Logger log = LoggerFactory.getLogger(ReduceOperationTool.class);

<<<<<<< HEAD
	// ==================== 配置常量 ====================
=======
	// ==================== Configuration Constants ====================
>>>>>>> main

	/**
	 * Fixed file name for reduce operations
	 */
	private static final String REDUCE_FILE_NAME = "reduce_output.md";

	/**
	 * Internal input class for defining Reduce operation tool input parameters
	 */
	public static class ReduceOperationInput {

		private List<List<Object>> data;

		@com.fasterxml.jackson.annotation.JsonProperty("has_value")
		private boolean hasValue;

		public ReduceOperationInput() {
		}

		public List<List<Object>> getData() {
			return data;
		}

		public void setData(List<List<Object>> data) {
			this.data = data;
		}

		public boolean isHasValue() {
			return hasValue;
		}

		public void setHasValue(boolean hasValue) {
			this.hasValue = hasValue;
		}

	}

	private static final String TOOL_NAME = "reduce_operation_tool";

	private static String getToolDescription(List<String> terminateColumns) {
		String baseDescription = """
				Reduce operation tool for MapReduce workflow file manipulation.
				Aggregates and merges data from multiple Map tasks and generates final consolidated output.

<<<<<<< HEAD
				**重要参数说明：**
				- has_value: 布尔值，表示是否有有效数据需要写入
				  - 如果没有找到任何有效数据，设置为 false
				  - 如果有数据需要输出，设置为 true
				- data: 当 has_value 为 true 时必须提供数据

				**IMPORTANT**: 操作完成后工具将自动终止。
				请在单次调用中完成所有内容输出。
=======
				**Important Parameter Description:**
				- has_value: Boolean value indicating whether there is valid data to write
				  - If no valid data is found, set to false
				  - If there is data to output, set to true
				- data: Must provide data when has_value is true

				**IMPORTANT**: Tool will automatically terminate after operation completion.
				Please complete all content output in a single call.
>>>>>>> main
				""";

		if (terminateColumns != null && !terminateColumns.isEmpty()) {
			String columnsFormat = String.join(", ", terminateColumns);
			baseDescription += String.format("""

<<<<<<< HEAD
					**数据格式要求（当 has_value=true 时）：**
					您必须按照以下固定格式提供数据，每行数据包含：[%s]

					示例格式：
					[
					  ["%s示例1", "%s示例1"],
					  ["%s示例2", "%s示例2"]
					]
					""", columnsFormat, terminateColumns.get(0),
					terminateColumns.size() > 1 ? terminateColumns.get(1) : "数据", terminateColumns.get(0),
					terminateColumns.size() > 1 ? terminateColumns.get(1) : "数据");
=======
					**Data Format Requirements (when has_value=true):**
					You must provide data in the following fixed format, each line containing: [%s]

					Example format:
					[
					  ["%s Example 1", "%s Example 1"],
					  ["%s Example 2", "%s Example 2"]
					]
					""", columnsFormat, terminateColumns.get(0),
					terminateColumns.size() > 1 ? terminateColumns.get(1) : "data", terminateColumns.get(0),
					terminateColumns.size() > 1 ? terminateColumns.get(1) : "data");
>>>>>>> main
		}

		return baseDescription;
	}

	/**
	 * Generate parameters JSON for ReduceOperationTool with predefined columns format
<<<<<<< HEAD
	 * @param terminateColumns the columns specification (e.g., "url,说明")
=======
	 * @param terminateColumns the columns specification (e.g., "url,description")
>>>>>>> main
	 * @return JSON string for parameters schema
	 */
	private static String generateParametersJson(List<String> terminateColumns) {
		// Generate columns description from terminateColumns
<<<<<<< HEAD
		String columnsDesc = "数据行列表";
		if (terminateColumns != null && !terminateColumns.isEmpty()) {
			columnsDesc = "数据行列表，每行按照以下格式：[" + String.join(", ", terminateColumns) + "]";
=======
		String columnsDesc = "data row list";
		if (terminateColumns != null && !terminateColumns.isEmpty()) {
			columnsDesc = "data row list, each row in the following format: [" + String.join(", ", terminateColumns)
					+ "]";
>>>>>>> main
		}

		return """
				{
				    "type": "object",
				    "properties": {
				        "has_value": {
				            "type": "boolean",
<<<<<<< HEAD
				            "description": "是否有有效数据需要写入。如果没有找到任何有效数据设置为false，有数据时设置为true"
=======
				            "description": "Whether there is valid data to write. Set to false if no valid data is found, set to true when there is data"
>>>>>>> main
				        },
				        "data": {
				            "type": "array",
				            "items": {
				                "type": "array",
				                "items": {"type": "string"}
				            },
<<<<<<< HEAD
				            "description": "%s（仅当has_value为true时需要提供）"
=======
				            "description": "%s (only required when has_value is true)"
>>>>>>> main
				        }
				    },
				    "required": ["has_value"],
				    "additionalProperties": false
				}
<<<<<<< HEAD
				""".formatted(columnsDesc);
=======
				"""
			.formatted(columnsDesc);
>>>>>>> main
	}

	private UnifiedDirectoryManager unifiedDirectoryManager;

<<<<<<< HEAD
	// 共享状态管理器，用于管理多个Agent实例间的共享状态
=======
	// Shared state manager for managing shared state between multiple Agent instances
>>>>>>> main
	private MapReduceSharedStateManager sharedStateManager;

	// Class-level terminate columns configuration - takes precedence over input
	// parameters
	private final List<String> terminateColumns;

<<<<<<< HEAD
	// ==================== TerminableTool 相关字段 ====================

	// 线程安全锁，用于保护append操作和终止状态
	private final ReentrantLock operationLock = new ReentrantLock();

	// 终止状态相关字段
=======
	// ==================== TerminableTool Related Fields ====================

	// Thread-safe lock to protect append operations and termination state
	private final ReentrantLock operationLock = new ReentrantLock();

	// Termination state related fields
>>>>>>> main
	private volatile boolean isTerminated = false;

	private String lastTerminationMessage = "";

	private String terminationTimestamp = "";

	public ReduceOperationTool(String planId, ManusProperties manusProperties,
			MapReduceSharedStateManager sharedStateManager, UnifiedDirectoryManager unifiedDirectoryManager,
			List<String> terminateColumns) {
		this.currentPlanId = planId;
		this.unifiedDirectoryManager = unifiedDirectoryManager;
		this.sharedStateManager = sharedStateManager;
		this.terminateColumns = terminateColumns;
	}

	/**
<<<<<<< HEAD
	 * 设置共享状态管理器
=======
	 * Set shared state manager
>>>>>>> main
	 */
	public void setSharedStateManager(MapReduceSharedStateManager sharedStateManager) {
		this.sharedStateManager = sharedStateManager;
	}

	@Override
	public String getName() {
		return TOOL_NAME;
	}

	/**
	 * Get task directory list
	 */
	public List<String> getSplitResults() {
		if (sharedStateManager != null && currentPlanId != null) {
			return sharedStateManager.getSplitResults(currentPlanId);
		}
		return new ArrayList<>();
	}

	@Override
	public String getDescription() {
		return getToolDescription(terminateColumns);
	}

	@Override
	public String getParameters() {
		return generateParametersJson(terminateColumns);
	}

	@Override
	public Class<ReduceOperationInput> getInputType() {
		return ReduceOperationInput.class;
	}

	@Override
	public String getServiceGroup() {
		return "data-processing";
	}

	public static OpenAiApi.FunctionTool getToolDefinition() {
		// Use default terminate columns for static tool definition
		return getToolDefinition(null);
	}

	public static OpenAiApi.FunctionTool getToolDefinition(List<String> terminateColumns) {
		String parameters = generateParametersJson(terminateColumns);
		String description = getToolDescription(terminateColumns);
		OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, TOOL_NAME,
				parameters);
		return new OpenAiApi.FunctionTool(function);
	}

	/**
	 * Execute Reduce operation
	 */
	@Override
	public ToolExecuteResult run(ReduceOperationInput input) {
		log.info("ReduceOperationTool input: hasValue={}", input.isHasValue());
		try {
			List<List<Object>> data = input.getData();
			boolean hasValue = input.isHasValue();

			// Use class-level terminateColumns
			List<String> effectiveTerminateColumns = this.terminateColumns;
			if (effectiveTerminateColumns == null || effectiveTerminateColumns.isEmpty()) {
				return new ToolExecuteResult("Error: terminate columns not configured for this tool");
			}

			// Check hasValue logic
			if (hasValue) {
				// When hasValue is true, data must be provided
				if (data == null || data.isEmpty()) {
					return new ToolExecuteResult("Error: data parameter is required when has_value is true");
				}

				// Validate data structure
				ToolExecuteResult validationResult = validateDataStructure(data, effectiveTerminateColumns);
				if (validationResult != null) {
					return validationResult; // Return validation error
				}

				// Convert structured data to JSON format and append
				String jsonContent = formatStructuredDataAsJson(effectiveTerminateColumns, data);
				return appendToFile(REDUCE_FILE_NAME, jsonContent);
			}
			else {
				// When hasValue is false, do not append anything but still mark as
				// terminated
				this.isTerminated = true;
				this.lastTerminationMessage = "No data to append, operation completed";
				this.terminationTimestamp = java.time.LocalDateTime.now().toString();
				log.info("Tool marked as terminated (no data to append) for planId: {}", currentPlanId);
				return new ToolExecuteResult("No data to append, operation completed successfully");
			}

		}
		catch (Exception e) {
			log.error("ReduceOperationTool execution failed", e);
			// Mark as terminated even on failure
			this.isTerminated = true;
			this.lastTerminationMessage = "Operation failed: " + e.getMessage();
			this.terminationTimestamp = java.time.LocalDateTime.now().toString();
			return new ToolExecuteResult("Tool execution failed: " + e.getMessage());
		}
	}

	/**
	 * Validate data structure against expected terminate columns
	 * @param data the input data
	 * @param terminateColumns expected column structure
	 * @return ToolExecuteResult with error message if validation fails, null if valid
	 */
	private ToolExecuteResult validateDataStructure(List<List<Object>> data, List<String> terminateColumns) {
		int expectedColumnCount = terminateColumns.size();

		for (int i = 0; i < data.size(); i++) {
			List<Object> row = data.get(i);
			if (row.size() != expectedColumnCount) {
				String error = String.format("""
<<<<<<< HEAD
						数据结构不一致！
						期望的列数: %d
						实际第%d行的列数: %d

						**要求的数据结构：**
						每行数据必须包含：[%s]

						示例格式：
						[
						  ["%s示例1", "%s示例1"],
						  ["%s示例2", "%s示例2"]
						]
						""", expectedColumnCount, i + 1, row.size(), String.join(", ", terminateColumns),
						terminateColumns.get(0), terminateColumns.size() > 1 ? terminateColumns.get(1) : "数据",
						terminateColumns.get(0), terminateColumns.size() > 1 ? terminateColumns.get(1) : "数据");
=======
						Data structure inconsistent!
						Expected column count: %d
						Actual column count for row %d: %d

						**Required data structure:**
						Each row must contain: [%s]

						Example format:
						[
						  ["%s Example1", "%s Example1"],
						  ["%s Example2", "%s Example2"]
						]
						""", expectedColumnCount, i + 1, row.size(), String.join(", ", terminateColumns),
						terminateColumns.get(0), terminateColumns.size() > 1 ? terminateColumns.get(1) : "data",
						terminateColumns.get(0), terminateColumns.size() > 1 ? terminateColumns.get(1) : "data");
>>>>>>> main
				return new ToolExecuteResult(error);
			}
		}
		return null; // Validation passed
	}

	/**
	 * Format structured data as JSON list format
	 * @param terminateColumns the column names
	 * @param data the data rows
	 * @return JSON formatted string representation of the structured data
	 */
	private String formatStructuredDataAsJson(List<String> terminateColumns, List<List<Object>> data) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  \"columns\": ").append(java.util.Arrays.toString(terminateColumns.toArray())).append(",\n");
		sb.append("  \"data\": [\n");

		for (int i = 0; i < data.size(); i++) {
			List<Object> row = data.get(i);
			sb.append("    ").append(java.util.Arrays.toString(row.toArray()));
			if (i < data.size() - 1) {
				sb.append(",");
			}
			sb.append("\n");
		}

		sb.append("  ]\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Append content to file in root plan directory Similar to
	 * InnerStorageTool.appendToFile() but operates on root plan directory This method is
	 * thread-safe and will set termination status after execution
	 */
	private ToolExecuteResult appendToFile(String fileName, String content) {
		operationLock.lock();
		try {
			if (content == null) {
				content = "";
			}

			// Get file from root plan directory
			Path rootPlanDir = getPlanDirectory(rootPlanId);
			ensureDirectoryExists(rootPlanDir);

			// Get file path and append content
			Path filePath = rootPlanDir.resolve(fileName);

			String resultMessage;
			// If file doesn't exist, create new file
			if (!Files.exists(filePath)) {
				Files.writeString(filePath, content);
				log.info("File created and content added: {}", fileName);
				resultMessage = String.format("File created successfully and content added: %s", fileName);
			}
			else {
				// Append content (add newline)
				Files.writeString(filePath, "\n" + content, StandardOpenOption.APPEND);
				log.info("Content appended to file: {}", fileName);
				resultMessage = String.format("Content appended successfully: %s", fileName);
			}

			// Read the file and get last 3 lines with line numbers
			List<String> lines = Files.readAllLines(filePath);
			StringBuilder result = new StringBuilder();
			result.append(resultMessage).append("\n\n");
			result.append("Last 3 lines of file:\n");
			result.append("-".repeat(30)).append("\n");

			int totalLines = lines.size();
			int startLine = Math.max(0, totalLines - 3);

			for (int i = startLine; i < totalLines; i++) {
				result.append(String.format("%4d: %s\n", i + 1, lines.get(i)));
			}

			String resultStr = result.toString();
			if (sharedStateManager != null) {
				sharedStateManager.setLastOperationResult(currentPlanId, resultStr);
			}

<<<<<<< HEAD
			// 设置终止状态
=======
			// Set termination status
>>>>>>> main
			this.isTerminated = true;
			this.lastTerminationMessage = "Append operation completed successfully";
			this.terminationTimestamp = java.time.LocalDateTime.now().toString();
			log.info("Tool marked as terminated after append operation for planId: {}", currentPlanId);

			return new ToolExecuteResult(resultStr);

		}
		catch (IOException e) {
			log.error("Failed to append to file", e);
<<<<<<< HEAD
			// 即使失败也设置终止状态
=======
			// Set termination status even if failed
>>>>>>> main
			this.isTerminated = true;
			this.lastTerminationMessage = "Append operation failed: " + e.getMessage();
			this.terminationTimestamp = java.time.LocalDateTime.now().toString();
			return new ToolExecuteResult("Failed to append to file: " + e.getMessage());
		}
		finally {
			operationLock.unlock();
		}
	}

	@Override
	public void cleanup(String planId) {
		// Clean up shared state
		if (sharedStateManager != null && planId != null) {
			sharedStateManager.cleanupPlanState(planId);
		}
		log.info("ReduceOperationTool cleanup completed for planId: {}", planId);
	}

	@Override
	public ToolExecuteResult apply(ReduceOperationInput input, ToolContext toolContext) {
		return run(input);
	}

	/**
	 * Get inner storage root directory path
	 */
	private Path getInnerStorageRoot() {
		return unifiedDirectoryManager.getInnerStorageRoot();
	}

	/**
	 * Get plan directory path
	 */
	private Path getPlanDirectory(String planId) {
		return getInnerStorageRoot().resolve(planId);
	}

	/**
	 * Ensure directory exists
	 */
	private void ensureDirectoryExists(Path directory) throws IOException {
		unifiedDirectoryManager.ensureDirectoryExists(directory);
	}

	/**
	 * Get class-level terminate columns configuration
	 * @return terminate columns as comma-separated string
	 */
	public String getTerminateColumns() {
		if (this.terminateColumns == null || this.terminateColumns.isEmpty()) {
			return null;
		}
		return String.join(",", this.terminateColumns);
	}

	/**
	 * Get class-level terminate columns configuration as List
	 * @return terminate columns as List<String>
	 */
	public List<String> getTerminateColumnsList() {
		return this.terminateColumns == null ? null : new ArrayList<>(this.terminateColumns);
	}

	// ==================== TerminableTool interface implementation
	// ====================

	@Override
	public boolean canTerminate() {
<<<<<<< HEAD
		// 检查是否已经执行了append操作，如果执行了则可以终止
=======
		// Check if append operation has been executed, if so then can terminate
>>>>>>> main
		return isTerminated;
	}

	/**
<<<<<<< HEAD
	 * 获取终止状态信息，包含原有状态和终止相关状态
=======
	 * Get termination status information, including original status and
	 * termination-related status
>>>>>>> main
	 */
	@Override
	public String getCurrentToolStateString() {
		StringBuilder sb = new StringBuilder();

<<<<<<< HEAD
		// 原有的共享状态信息
=======
		// Original shared state information
>>>>>>> main
		if (sharedStateManager != null && currentPlanId != null) {
			sb.append(sharedStateManager.getCurrentToolStateString(currentPlanId));
			sb.append("\n\n");
		}

<<<<<<< HEAD
		// 简化的终止状态信息
=======
		// Simplified termination status information
>>>>>>> main
		sb.append(String.format("ReduceOperationTool: %s", isTerminated ? "🛑 Terminated" : "⚡ Active"));

		return sb.toString();
	}

	/**
<<<<<<< HEAD
	 * 检查工具是否已经终止
=======
	 * Check if tool has already terminated
>>>>>>> main
	 */
	public boolean isTerminated() {
		return isTerminated;
	}

	/**
<<<<<<< HEAD
	 * 获取最后的终止消息
=======
	 * Get last termination message
>>>>>>> main
	 */
	public String getLastTerminationMessage() {
		return lastTerminationMessage;
	}

	/**
<<<<<<< HEAD
	 * 获取终止时间戳
=======
	 * Get termination timestamp
>>>>>>> main
	 */
	public String getTerminationTimestamp() {
		return terminationTimestamp;
	}

}
