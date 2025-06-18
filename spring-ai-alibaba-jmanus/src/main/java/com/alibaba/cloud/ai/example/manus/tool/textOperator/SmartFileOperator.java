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
package com.alibaba.cloud.ai.example.manus.tool.textOperator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibaba.cloud.ai.example.manus.tool.innerStorage.InnerStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 智能文件操作工具基类 当返回值超过指定阈值时，自动使用 InnerStorageService 保存详细内容并返回摘要信息
 */
@Component
public class SmartFileOperator {

	private static final Logger log = LoggerFactory.getLogger(SmartFileOperator.class);

	// 默认阈值：字符数
	private static final int DEFAULT_CONTENT_THRESHOLD = 300;

	private final InnerStorageService innerStorageService;

	@Autowired
	public SmartFileOperator(InnerStorageService innerStorageService) {
		this.innerStorageService = innerStorageService;
	}

	/**
	 * 获取内容阈值
	 * @param planId 计划ID
	 * @return 阈值（字节数）
	 */
	protected int getContentThreshold(String planId) {
		return DEFAULT_CONTENT_THRESHOLD;
	}

	/**
	 * 智能处理内容 如果内容长度超过阈值，则使用 InnerStorageService 保存详细内容并返回摘要和ID
	 * @param content 要处理的内容
	 * @param planId 计划ID
	 * @param workingDirectoryPath 工作目录路径
	 * @return 处理结果，包含摘要和内容ID
	 */
	public SmartProcessResult processResult(String content, String planId, String workingDirectoryPath) {
		if (planId == null || content == null) {
			return new SmartProcessResult(content != null ? content : "");
		}
		int threshold = getContentThreshold(planId);

		log.info("Processing result for plan {}: content length = {}, threshold = {}", planId, content.length(),
				threshold);

		// 如果内容未超过阈值，直接返回
		if (content.length() <= threshold) {
			log.info("Content length {} is within threshold {}, returning original content", content.length(),
					threshold);
			return new SmartProcessResult(content);
		}

		log.info("Content length {} exceeds threshold {}, triggering auto storage", content.length(), threshold);

		try {
			// 生成内容ID作为文件名
			String contentId = generateContentId(planId);
			String storageFileName = contentId + ".md";

			// 确保agent目录存在
			String agentName = innerStorageService.getPlanAgent(planId);
			Path agentDir = innerStorageService.getAgentDirectory(workingDirectoryPath, planId, agentName);
			innerStorageService.ensureDirectoryExists(agentDir);

			// 保存详细内容到 InnerStorage
			Path storagePath = innerStorageService.getFilePath(workingDirectoryPath, planId, storageFileName);
			saveDetailedContentToStorage(storagePath, content, contentId);

			// 生成简化摘要
			String summary = generateSmartSummary(content, contentId, storageFileName);

			log.info("Content exceeds threshold ({} bytes), saved to storage file: {}, content ID: {}", threshold,
					storageFileName, contentId);

			return new SmartProcessResult(summary, contentId);

		}
		catch (IOException e) {
			log.error("Failed to save content to storage for plan {}", planId, e);
			// 如果保存失败，返回截断的内容
			String truncatedContent = content.substring(0, threshold) + "\n\n... (内容过长，已截断)";
			return new SmartProcessResult(truncatedContent);
		}
	}

	/**
	 * 生成内容ID
	 */
	private String generateContentId(String planId) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		int randomSuffix = (int) (Math.random() * 1000); // 0-999的随机数
		return String.format("%s_%s_%d", planId, timestamp, randomSuffix);
	}

	/**
	 * 保存详细内容到存储
	 */
	private void saveDetailedContentToStorage(Path storagePath, String content, String contentId) throws IOException {
		StringBuilder detailedContent = new StringBuilder();
		detailedContent.append("=".repeat(60)).append("\n");
		detailedContent.append("自动存储的详细内容\n");
		detailedContent.append("=".repeat(60)).append("\n");
		detailedContent.append("生成时间: ").append(LocalDateTime.now()).append("\n");
		detailedContent.append("内容ID: ").append(contentId).append("\n");
		detailedContent.append("内容长度: ").append(content.length()).append(" 字符\n");
		detailedContent.append("=".repeat(60)).append("\n\n");
		detailedContent.append(content);

		Files.writeString(storagePath, detailedContent.toString(), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * 生成智能摘要
	 */
	private String generateSmartSummary(String content, String contentId, String storageFileName) {
		StringBuilder summary = new StringBuilder();

		// 添加简化的操作摘要
		summary.append("- 操作成功完成，但返回结果过长，因此已将内容保存到文件中。");
		summary.append("如果需要使用文件内容，请使用 InnerStorageTool 的相关操作(list_contents、get_content、search)。\n");

		// 添加存储文件索引信息
		summary.append("- 存储文件index : ").append(contentId).append("\n");

		// 添加简化的内容预览（前5行）
		String[] lines = content.split("\n");
		int previewLines = Math.min(5, lines.length);
		summary.append("- 内容预览 (前").append(previewLines).append("行):\n");

		for (int i = 0; i < previewLines; i++) {
			String line = lines[i].trim();
			if (line.length() > 80) {
				line = line.substring(0, 80) + "...";
			}
			summary.append("    ").append(line);
			if (i < previewLines - 1) {
				summary.append(" \\n");
			}
			summary.append("\n");
		}

		if (lines.length > previewLines) {
			summary.append("    ...\n");
		}

		return summary.toString();
	}

	/**
	 * 清理计划相关的资源
	 * @param planId 计划ID
	 * @param workingDirectoryPath 工作目录路径
	 */
	public void cleanupPlan(String planId, String workingDirectoryPath) {
		// 清理 InnerStorage 中的相关文件
		try {
			innerStorageService.cleanupPlan(workingDirectoryPath, planId);
			log.info("Cleaned up plan resources: {}", planId);
		}
		catch (Exception e) {
			log.error("Failed to cleanup plan resources: {}", planId, e);
		}
	}

}
