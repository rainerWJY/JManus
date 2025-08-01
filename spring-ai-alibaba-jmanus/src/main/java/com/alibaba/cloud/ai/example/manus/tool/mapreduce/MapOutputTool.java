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

<<<<<<< HEAD
import java.io.File;
=======
>>>>>>> main
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.alibaba.cloud.ai.example.manus.tool.AbstractBaseTool;
import com.alibaba.cloud.ai.example.manus.tool.TerminableTool;
import com.alibaba.cloud.ai.example.manus.tool.code.ToolExecuteResult;
import com.alibaba.cloud.ai.example.manus.tool.filesystem.UnifiedDirectoryManager;
import com.alibaba.cloud.ai.example.manus.config.ManusProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * Map output recording tool for MapReduce workflow Responsible for recording Map stage
 * processing results and task status management
 */
public class MapOutputTool extends AbstractBaseTool<MapOutputTool.MapOutputInput> implements TerminableTool {

	private static final Logger log = LoggerFactory.getLogger(MapOutputTool.class);

<<<<<<< HEAD
	// ==================== 配置常量 ====================
=======
	// ==================== Configuration Constants ====================
>>>>>>> main

	/**
	 * Task directory name All tasks are stored under this directory
	 */
	private static final String TASKS_DIRECTORY_NAME = "tasks";

	/**
	 * Task status file name Stores task execution status information
	 */
	private static final String TASK_STATUS_FILE_NAME = "status.json";

	/**
	 * Task output file name Stores results after Map stage processing completion
	 */
	private static final String TASK_OUTPUT_FILE_NAME = "output.md";

	/**
	 * Task input file name Stores document fragment content after splitting
	 */
	private static final String TASK_INPUT_FILE_NAME = "input.md";

	/**
	 * Task status: completed
	 */
	/**
	 * Internal input class for defining Map output tool input parameters
	 */
	public static class MapOutputInput {

<<<<<<< HEAD
=======
		@com.fasterxml.jackson.annotation.JsonProperty("task_id")
		private String taskId;

>>>>>>> main
		private List<List<Object>> data;

		@com.fasterxml.jackson.annotation.JsonProperty("has_value")
		private boolean hasValue;

		public MapOutputInput() {
		}

<<<<<<< HEAD
=======
		public String getTaskId() {
			return taskId;
		}

		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}

>>>>>>> main
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

	private static final String TOOL_NAME = "map_output_tool";

	private static String getToolDescription(List<String> terminateColumns) {
		String baseDescription = """
				Map output recording tool for MapReduce workflow.
<<<<<<< HEAD
				接受 Map 阶段处理完成后的内容，自动生成文件名并创建输出文件。
				记录任务状态并管理结构化数据输出。

				**重要参数说明：**
				- has_value: 布尔值，表示是否有有效数据
				  - 如果没有找到任何有效数据，设置为 false
				  - 如果有数据需要输出，设置为 true
				- data: 当 has_value 为 true 时必须提供数据
=======
				Accept content after Map phase processing completion, automatically generate filename and create output file.
				Record task status and manage structured data output.

				**Important Parameter Description:**
				- task_id: String, task ID identifier for identifying the currently processing Map task (required)
				- has_value: Boolean value indicating whether there is valid data
				  - If no valid data is found, set to false
				  - If there is data to output, set to true
				- data: Must provide data when has_value is true
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
					  ["%s Example1", "%s Example1"],
					  ["%s Example2", "%s Example2"]
					]
					""", columnsFormat, terminateColumns.get(0),
					terminateColumns.size() > 1 ? terminateColumns.get(1) : "data", terminateColumns.get(0),
					terminateColumns.size() > 1 ? terminateColumns.get(1) : "data");
>>>>>>> main
		}

		return baseDescription;
	}

	/**
	 * Generate parameters JSON for MapOutputTool with predefined columns format
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
<<<<<<< HEAD
				        "has_value": {
				            "type": "boolean",
				            "description": "是否有有效数据。如果没有找到任何有效数据设置为false，有数据时设置为true"
=======
				        "task_id": {
				            "type": "string",
				            "description": "Task ID identifier for identifying the currently processing Map task"
				        },
				        "has_value": {
				            "type": "boolean",
				            "description": "Whether there is valid data. Set to false if no valid data is found, set to true when there is data"
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
				        }
				    },
				    "required": ["has_value"],
				    "additionalProperties": false
				}
				""".formatted(columnsDesc);
=======
				            "description": "%s (only required when has_value is true)"
				        }
				    },
				    "required": ["task_id", "has_value"],
				    "additionalProperties": false
				}
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
	private List<String> terminateColumns;

	// Track if map output recording has completed, allowing termination
	private volatile boolean mapOutputRecorded = false;

<<<<<<< HEAD
	private static final ObjectMapper objectMapper = new ObjectMapper();

	// Main constructor with List<String> terminateColumns
	public MapOutputTool(String planId, ManusProperties manusProperties, MapReduceSharedStateManager sharedStateManager,
			UnifiedDirectoryManager unifiedDirectoryManager, List<String> terminateColumns) {
=======
	private final ObjectMapper objectMapper;

	// Main constructor with List<String> terminateColumns
	public MapOutputTool(String planId, ManusProperties manusProperties, MapReduceSharedStateManager sharedStateManager,
			UnifiedDirectoryManager unifiedDirectoryManager, List<String> terminateColumns, ObjectMapper objectMapper) {
>>>>>>> main
		this.currentPlanId = planId;
		this.unifiedDirectoryManager = unifiedDirectoryManager;
		this.sharedStateManager = sharedStateManager;
		this.terminateColumns = terminateColumns;
<<<<<<< HEAD
=======
		this.objectMapper = objectMapper;
>>>>>>> main
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

	@Override
	public String getDescription() {
		return getToolDescription(terminateColumns);
	}

	@Override
	public String getParameters() {
		return generateParametersJson(terminateColumns);
	}

	@Override
	public Class<MapOutputInput> getInputType() {
		return MapOutputInput.class;
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
	 * Execute Map output recording operation
	 */
	@Override
	public ToolExecuteResult run(MapOutputInput input) {
<<<<<<< HEAD
		log.info("MapOutputTool input: hasValue={}", input.isHasValue());
		try {
			List<List<Object>> data = input.getData();
			boolean hasValue = input.isHasValue();

=======
		log.info("MapOutputTool input: taskId={}, hasValue={}", input.getTaskId(), input.isHasValue());
		try {
			String taskId = input.getTaskId();
			List<List<Object>> data = input.getData();
			boolean hasValue = input.isHasValue();

			// Validate taskId
			if (taskId == null || taskId.trim().isEmpty()) {
				return new ToolExecuteResult("Error: task_id parameter is required");
			}

>>>>>>> main
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
				// Convert structured data to content string
				String content = formatStructuredData(effectiveTerminateColumns, data);
<<<<<<< HEAD
				return recordMapTaskOutput(content);
			}
			else {
				// When hasValue is false, create empty output
				return recordMapTaskOutput("");
=======
				return recordMapTaskOutput(content, taskId);
			}
			else {
				// When hasValue is false, create empty output
				return recordMapTaskOutput("", taskId);
>>>>>>> main
			}

		}
		catch (Exception e) {
			log.error("MapOutputTool execution failed", e);
			return new ToolExecuteResult("Tool execution failed: " + e.getMessage());
		}
	}

	/**
	 * Format structured data similar to TerminateTool
	 * @param terminateColumns the column names
	 * @param data the data rows
	 * @return formatted string representation of the structured data
	 */
	private String formatStructuredData(List<String> terminateColumns, List<List<Object>> data) {
		StringBuilder sb = new StringBuilder();
<<<<<<< HEAD
		sb.append("Structured Map output data:\n");
=======
>>>>>>> main
		sb.append("Columns: ").append(terminateColumns).append("\n");
		sb.append("Data:\n");
		for (List<Object> row : data) {
			sb.append("  ").append(row).append("\n");
		}
		return sb.toString();
	}

	/**
<<<<<<< HEAD
	 * Record Map task output result with completed status by default Task ID is obtained
	 * from the current execution context
	 */
	private ToolExecuteResult recordMapTaskOutput(String content) {
=======
	 * Record Map task output result with completed status by default Task ID is provided
	 * as parameter instead of being obtained from the current execution context
	 */
	private ToolExecuteResult recordMapTaskOutput(String content, String taskId) {
>>>>>>> main
		try {
			// Get timeout configuration for this operation
			Integer timeout = getMapReduceTimeout();
			log.debug("Recording Map task output with timeout: {} seconds", timeout);

			// Ensure planId exists
			if (currentPlanId == null || currentPlanId.trim().isEmpty()) {
				return new ToolExecuteResult("Error: currentPlanId not set, cannot record task status");
			}
<<<<<<< HEAD

			// Get current taskId by finding the most recent task directory without
			// output.md
			String taskId = findCurrentTaskId();

			if (taskId == null || taskId.trim().isEmpty()) {
				return new ToolExecuteResult("Error: No current task ID available for recording output");
=======
			// Validate taskId parameter
			if (taskId == null || taskId.trim().isEmpty()) {
				return new ToolExecuteResult("Error: taskId parameter is required for recording output");
>>>>>>> main
			}

			// Locate task directory - use hierarchical structure:
			// inner_storage/{rootPlanId}/{currentPlanId}/tasks/{taskId}
			Path rootPlanDir = getPlanDirectory(rootPlanId);
			Path currentPlanDir = rootPlanDir.resolve(currentPlanId);
			Path taskDir = currentPlanDir.resolve(TASKS_DIRECTORY_NAME).resolve(taskId);

			if (!Files.exists(taskDir)) {
				return new ToolExecuteResult("Error: Task directory does not exist: " + taskId);
			}

			// Create output.md file
			Path outputFile = taskDir.resolve(TASK_OUTPUT_FILE_NAME);
			// Write processing content directly without adding extra metadata information
			Files.write(outputFile, content.getBytes());
			String outputFilePath = outputFile.toAbsolutePath().toString();
			// Update task status file
			Path statusFile = taskDir.resolve(TASK_STATUS_FILE_NAME);
			TaskStatus taskStatus;

			if (Files.exists(statusFile)) {
				// Read existing status
				String existingStatusJson = new String(Files.readAllBytes(statusFile));
				taskStatus = objectMapper.readValue(existingStatusJson, TaskStatus.class);
			}
			else {
				// Create new status
				taskStatus = new TaskStatus();
				taskStatus.taskId = taskId;
				taskStatus.inputFile = taskDir.resolve(TASK_INPUT_FILE_NAME).toAbsolutePath().toString();
			}

			// Update status information
			taskStatus.outputFilePath = outputFilePath;
			taskStatus.status = "completed";
			taskStatus.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			// Store in shared state manager
			if (sharedStateManager != null) {
				MapReduceSharedStateManager.TaskStatus sharedTaskStatus = new MapReduceSharedStateManager.TaskStatus();
				sharedTaskStatus.taskId = taskStatus.taskId;
				sharedTaskStatus.inputFile = taskStatus.inputFile;
				sharedTaskStatus.outputFilePath = taskStatus.outputFilePath;
				sharedTaskStatus.status = taskStatus.status;
				sharedTaskStatus.timestamp = taskStatus.timestamp;
				sharedStateManager.recordMapTaskStatus(currentPlanId, taskId, sharedTaskStatus);
			}

			// Write updated status file
			String statusJson = objectMapper.writeValueAsString(taskStatus);
			Files.write(statusFile, statusJson.getBytes());

			// Mark that map output has been recorded, allowing termination
			this.mapOutputRecorded = true;

			String result = String.format("Task %s status recorded: %s, output file: %s", taskId, "completed",
					TASK_OUTPUT_FILE_NAME);

			String resultStr = result.toString();
			if (sharedStateManager != null) {
				sharedStateManager.setLastOperationResult(currentPlanId, resultStr);
			}
			log.info(result);
			return new ToolExecuteResult(result);

		}
		catch (Exception e) {
			String error = "Recording Map task status failed: " + e.getMessage();
			log.error(error, e);
			return new ToolExecuteResult(error);
		}
	}

	@Override
	public String getCurrentToolStateString() {
		if (sharedStateManager != null && currentPlanId != null) {
			return sharedStateManager.getCurrentToolStateString(currentPlanId);
		}

		// Fallback solution
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}

	@Override
	public void cleanup(String planId) {
		// Clean up shared state
		if (sharedStateManager != null && planId != null) {
			sharedStateManager.cleanupPlanState(planId);
		}
		log.info("MapOutputTool cleanup completed for planId: {}", planId);
	}

	@Override
	public ToolExecuteResult apply(MapOutputInput input, ToolContext toolContext) {
		return run(input);
	}

	/**
<<<<<<< HEAD
	 * Find the current task ID by looking for task directories without output.md file
	 * This helps identify which task is currently being processed
	 */
	private String findCurrentTaskId() {
		try {
			// Get task directories path
			Path rootPlanDir = getPlanDirectory(rootPlanId);
			Path currentPlanDir = rootPlanDir.resolve(currentPlanId);
			Path tasksDir = currentPlanDir.resolve(TASKS_DIRECTORY_NAME);

			if (!Files.exists(tasksDir)) {
				log.warn("Tasks directory does not exist: {}", tasksDir);
				return null;
			}

			// Get all task directories using traditional File operations
			File tasksDirFile = tasksDir.toFile();
			File[] taskFiles = tasksDirFile.listFiles();
			if (taskFiles == null || taskFiles.length == 0) {
				log.warn("No task directories found in: {}", tasksDir);
				return null;
			}

			// Find incomplete tasks (without output.md)
			List<String> incompleteTaskIds = new ArrayList<>();
			List<String> allTaskIds = new ArrayList<>();

			for (File taskFile : taskFiles) {
				if (taskFile.isDirectory()) {
					String taskId = taskFile.getName();
					allTaskIds.add(taskId);

					// Check if output.md exists
					File outputFile = new File(taskFile, TASK_OUTPUT_FILE_NAME);
					if (!outputFile.exists()) {
						incompleteTaskIds.add(taskId);
					}
				}
			}

			// If we have incomplete tasks, return the earliest one (sorted)
			if (!incompleteTaskIds.isEmpty()) {
				Collections.sort(incompleteTaskIds);
				String incompleteTaskId = incompleteTaskIds.get(0);
				log.debug("Found incomplete task: {}", incompleteTaskId);
				return incompleteTaskId;
			}

			// If no incomplete tasks found, get the latest task (reverse sorted)
			if (!allTaskIds.isEmpty()) {
				Collections.sort(allTaskIds, Collections.reverseOrder());
				String latestTaskId = allTaskIds.get(0);
				log.debug("No incomplete tasks found, using latest task: {}", latestTaskId);
				return latestTaskId;
			}

			log.warn("No task directories found");
			return null;

		}
		catch (Exception e) {
			log.error("Error finding current task ID", e);
			return null;
		}
	}

	/**
=======
>>>>>>> main
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

	/**
	 * Task status internal class
	 */
	@SuppressWarnings("unused")
	private static class TaskStatus {

		public String taskId;

		public String inputFile;

		public String outputFilePath;

		public String status;

		public String timestamp;

	}

	/**
	 * Get MapReduce operation timeout configuration
	 * @return Timeout in seconds, returns default value of 300 seconds if not configured
	 */
	private Integer getMapReduceTimeout() {
		// For now, use default timeout until the configuration is added to
		// ManusProperties
		return 300; // Default timeout is 5 minutes
	}

	// ==================== TerminableTool interface implementation
	// ====================

	@Override
	public boolean canTerminate() {
		// MapOutputTool can be terminated only after map output has been recorded
		// This ensures that the tool completes its processing cycle before termination
		return mapOutputRecorded;
	}

}
