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

/**
 * 智能文件操作处理结果
 * 包含摘要信息和内容ID，支持通过ID直接进行文件操作
 */
public class SmartProcessResult {

	private final String summary;

	private final String contentId;

	private final boolean isStored;

	/**
	 * 构造函数 - 内容已存储的情况
	 * @param summary 摘要信息
	 * @param contentId 内容ID，可用于文件操作
	 */
	public SmartProcessResult(String summary, String contentId) {
		this.summary = summary;
		this.contentId = contentId;
		this.isStored = true;
	}

	/**
	 * 构造函数 - 内容未存储的情况（内容长度未超过阈值）
	 * @param summary 原始内容作为摘要
	 */
	public SmartProcessResult(String summary) {
		this.summary = summary;
		this.contentId = null;
		this.isStored = false;
	}

	/**
	 * 获取摘要信息
	 * @return 摘要信息
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * 获取内容ID
	 * @return 内容ID，如果内容未存储则返回null
	 */
	public String getContentId() {
		return contentId;
	}

	/**
	 * 判断内容是否已存储
	 * @return 如果内容已存储返回true，否则返回false
	 */
	public boolean isStored() {
		return isStored;
	}

	/**
	 * 判断是否有内容ID可用于文件操作
	 * @return 如果有内容ID返回true，否则返回false
	 */
	public boolean hasContentId() {
		return contentId != null && !contentId.isEmpty();
	}

	@Override
	public String toString() {
		return "ProcessedResult{" + "summary='" + summary + '\'' + ", contentId='" + contentId + '\''
				+ ", isStored=" + isStored + '}';
	}

}
