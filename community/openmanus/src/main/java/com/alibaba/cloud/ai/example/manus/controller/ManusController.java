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
package com.alibaba.cloud.ai.example.manus.controller;

import com.alibaba.cloud.ai.example.manus.flow.PlanningFlow;
import com.alibaba.cloud.ai.example.manus.model.PlanStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/manus")
@CrossOrigin(origins = "*")
public class ManusController {

	private static final Logger log = LoggerFactory.getLogger(ManusController.class);

	private final PlanningFlow planningFlow;

	ManusController(PlanningFlow planningFlow) {
		this.planningFlow = planningFlow;
	}

	@GetMapping("/chat")
	public ResponseEntity<Map<String, String>> simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
		try {
			log.info("Received chat query: {}", query);
			planningFlow.setActivePlanId("plan_" + System.currentTimeMillis());
			String result = planningFlow.execute(query);
			
			Map<String, String> response = new HashMap<>();
			response.put("result", result);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error processing chat request", e);
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "处理请求时出错: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/status")  // 确认这个路径匹配前端请求的路径
	public ResponseEntity<Object> getPlanStatus() {
		try {
			
			PlanStatus status = planningFlow.getPlanStatus();
			
			if (status == null) {
				log.warn("No active plan found");
				Map<String, String> emptyResponse = new HashMap<>();
				emptyResponse.put("message", "未找到活动计划");
				return ResponseEntity.status(HttpStatus.OK).body(emptyResponse);
			}
			
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			log.error("Error retrieving plan status", e);
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "获取计划状态时出错: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleException(Exception e) {
		log.error("Global exception in controller", e);
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("error", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
