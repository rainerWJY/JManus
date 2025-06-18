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

import com.alibaba.cloud.ai.example.manus.config.ManusProperties;
import com.alibaba.cloud.ai.example.manus.tool.innerStorage.InnerStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 SmartFileOperator 的自动存储功能
 */
class SmartFileOperatorTest {

	@TempDir
	Path tempDir;

	private InnerStorageService innerStorageService;

	private SmartFileOperator smartOperator;

	private String testPlanId = "test-plan-001";

	@BeforeEach
	void setUp() {
		ManusProperties properties = new ManusProperties();
		innerStorageService = new InnerStorageService(properties);
		smartOperator = new SmartFileOperator(innerStorageService);
		// 设置测试代理
		innerStorageService.setPlanAgent(testPlanId, "test-agent");
	}

	@Test
	void testShortContentNoStorage() {
		// 短内容不应该触发自动存储
		String shortContent = "这是一段短内容";

		com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
			smartOperator.processResult(shortContent, testPlanId, tempDir.toString());

		assertEquals(shortContent, processedResult.getSummary());
		assertFalse(processedResult.isStored());
		assertNull(processedResult.getContentId());
	}

	@Test
	void testLongContentAutoStorage() {
		// 长内容应该触发自动存储
		StringBuilder longContent = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			longContent.append("这是第").append(i).append("行的测试内容，用于测试长内容的自动存储功能。\n");
		}

		com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
			smartOperator.processResult(longContent.toString(), testPlanId, tempDir.toString());

		assertTrue(processedResult.getSummary().contains("已将内容保存到文件中"));
		assertTrue(processedResult.getSummary().contains("存储文件index"));
		assertTrue(processedResult.getSummary().contains("内容预览"));
		assertTrue(processedResult.isStored());
		assertNotNull(processedResult.getContentId());
	}

}
