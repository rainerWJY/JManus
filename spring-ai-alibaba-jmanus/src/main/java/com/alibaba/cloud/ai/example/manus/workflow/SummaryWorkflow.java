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
package com.alibaba.cloud.ai.example.manus.workflow;

import com.alibaba.cloud.ai.example.manus.planning.PlanningFactory;
import com.alibaba.cloud.ai.example.manus.planning.coordinator.PlanIdDispatcher;
import com.alibaba.cloud.ai.example.manus.planning.coordinator.PlanningCoordinator;
import com.alibaba.cloud.ai.example.manus.planning.model.vo.ExecutionContext;
import com.alibaba.cloud.ai.example.manus.planning.model.vo.ExecutionStep;
import com.alibaba.cloud.ai.example.manus.planning.model.vo.mapreduce.MapReduceExecutionPlan;
import com.alibaba.cloud.ai.example.manus.dynamic.prompt.service.PromptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MapReduce-based content summarization workflow for intelligent extraction and
 * structured summarization of large amounts of content
 */
@Component
public class SummaryWorkflow implements ISummaryWorkflow {

	private static final Logger logger = LoggerFactory.getLogger(SummaryWorkflow.class);

	@Autowired
	private PlanningFactory planningFactory;

	@Autowired
	private PlanIdDispatcher planIdDispatcher;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PromptService promptService;

	/**
	 * Get summary plan template from PromptService
	 */
<<<<<<< HEAD
	private static final String SUMMARY_PLAN_TEMPLATE = """
			{
			  "planType": "advanced",
			  "planId": "%s",
			  "title": "内容智能的对大文件进行汇总，最后总结时需要把合并后的文件名在总结时输出出来",
			  "steps": [
			    {
			      "type": "mapreduce",
			      "dataPreparedSteps": [
			        {
			          "stepRequirement": "[MAPREDUCE_DATA_PREPARE_AGENT] 使用map_reduce_tool，对 %s 进行内容分割",
			          "terminateColumns": "%s"
			        }
			      ],
			      "mapSteps": [
			        {
			          "stepRequirement": "[MAPREDUCE_MAP_TASK_AGENT] 分析文件，找到与 %s 相关的关键信息，信息要全面，包含所有数据，事实和观点等，全面的信息，不要遗漏",
			          "terminateColumns": "%s"
			        }
			      ],
			      "reduceSteps": [
			        {
			          "stepRequirement": "[MAPREDUCE_REDUCE_TASK_AGENT] 合并该分片的信息到文件中，在保持信息完整性的前提下，合并所有内容，同时也要去掉未找到内容的那些结果",
			          "terminateColumns": "%s"
			        }
			      ],
				  "postProcessSteps": [
					{
					  "stepRequirement": "[MAPREDUCE_FIN_AGENT] 当导出完成后，读取导出的结果后，完整输出所有导出的内容",
					  "terminateColumns": "file_path"
					}
				  ]

			    }
			  ]
			}
			""";
=======
	private String getSummaryPlanTemplate() {
		return promptService.getPromptByName("SUMMARY_PLAN_TEMPLATE").getPromptContent();
	}
>>>>>>> main

	/**
	 * Execute content summarization workflow
	 * @param planId Caller's plan ID to ensure subprocess can find corresponding
	 * directory
	 * @param fileName File name
	 * @param content File content
	 * @param queryKey Query keywords
	 * @param thinkActRecordId Think-act record ID for sub-plan execution tracking
	 * @return Future of summarization result
	 */
	public CompletableFuture<String> executeSummaryWorkflow(String parentPlanId, String fileName, String content,
			String queryKey, Long thinkActRecordId, String terminateColumnsString) {

		// 1. Build MapReduce execution plan using caller's planId
		MapReduceExecutionPlan executionPlan = buildSummaryExecutionPlan(parentPlanId, fileName, content, queryKey,
				terminateColumnsString);

		// 2. Execute plan directly, passing thinkActRecordId
		return executeMapReducePlanWithContext(parentPlanId, executionPlan, thinkActRecordId);
	}

	/**
	 * Build MapReduce-based summarization execution plan
	 * @param planId Use caller-provided plan ID to ensure subprocess can find
	 * corresponding directory
	 * @param fileName File name
	 * @param content File content (not directly used yet, but kept as extension
	 * parameter)
	 * @param queryKey Query keywords
	 */
	private MapReduceExecutionPlan buildSummaryExecutionPlan(String parentPlanId, String fileName, String content,
			String queryKey, String terminateColumnsString) {

		try {
			// Use caller-provided planId instead of generating a new one
			logger.info("Building summary execution plan with provided planId: {}", parentPlanId);

<<<<<<< HEAD
			// 生成计划JSON，使用传入的planId
			String planJson = String.format(SUMMARY_PLAN_TEMPLATE, parentPlanId, // 计划ID
					fileName, // dataPreparedSteps 文件名
					terminateColumnsString, // dataPreparedSteps terminateColumns
					queryKey, // mapSteps 查询关键词
					terminateColumnsString, // mapSteps terminateColumns
					terminateColumnsString, // reduceSteps terminateColumns
					terminateColumnsString // postProcessSteps terminateColumns（会自动加上 ,
											// fileURL）
=======
			// Generate plan JSON using template from PromptService
			String planJson = String.format(getSummaryPlanTemplate(), parentPlanId, // Plan
																					// ID
					fileName, // dataPreparedSteps file name
					terminateColumnsString, // dataPreparedSteps terminateColumns
					queryKey, // mapSteps query key
					terminateColumnsString, // mapSteps terminateColumns
					terminateColumnsString, // reduceSteps terminateColumns
					terminateColumnsString // postProcessSteps terminateColumns (will auto
											// add fileURL)
>>>>>>> main
			);

			// Parse JSON to MapReduceExecutionPlan object
			MapReduceExecutionPlan plan = objectMapper.readValue(planJson, MapReduceExecutionPlan.class);
<<<<<<< HEAD
			// terminateColumns 直接在 JSON 模板中配置，无需在此处设置
=======
			// terminateColumns are configured directly in JSON template, no need to set
			// here
>>>>>>> main

			return plan;

		}
		catch (Exception e) {
			logger.error("Failed to build summary execution plan, planId: {}", parentPlanId, e);
			throw new RuntimeException("Failed to build MapReduce summary execution plan: " + e.getMessage(), e);
		}
	}

	/**
	 * Execute MapReduce plan - supports sub-plan context
	 */
	private CompletableFuture<String> executeMapReducePlanWithContext(String rootPlanId,
			MapReduceExecutionPlan executionPlan, Long thinkActRecordId) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// Generate a unique sub-plan ID using PlanIdDispatcher, similar to
				// generatePlan method

				String subPlanId = planIdDispatcher.generateSubPlanId(rootPlanId, thinkActRecordId);

				logger.info("Generated sub-plan ID: {} for parent plan: {}, think-act record: {}", subPlanId,
						rootPlanId, thinkActRecordId);

				// Get planning coordinator using generated sub-plan ID
				PlanningCoordinator planningCoordinator = planningFactory.createPlanningCoordinator(subPlanId);

				// Create execution context
				ExecutionContext context = new ExecutionContext();
				context.setCurrentPlanId(subPlanId);
				context.setRootPlanId(rootPlanId);
				context.setThinkActRecordId(thinkActRecordId);

				// Update execution plan ID to sub-plan ID
				executionPlan.setCurrentPlanId(subPlanId);
				executionPlan.setRootPlanId(rootPlanId);
				context.setPlan(executionPlan);
				context.setNeedSummary(false);
				context.setUserRequest("Execute MapReduce-based intelligent content summarization");

				// Set think-act record ID to support sub-plan execution
				if (thinkActRecordId != null) {
					context.setThinkActRecordId(thinkActRecordId);
				}

				// Execute plan (skip plan creation step, execute directly)
				planningCoordinator.executeExistingPlan(context);

				logger.info("MapReduce summary plan executed successfully, sub-plan ID: {}, parent plan ID: {}",
						subPlanId, rootPlanId);

				List<ExecutionStep> allSteps = context.getPlan().getAllSteps();
				ExecutionStep lastStep = allSteps.get(allSteps.size() - 1);
				return "getContent executed successfully, execution result log: " + lastStep.getResult();
			}
			catch (Exception e) {
				logger.error("MapReduce summary plan execution failed", e);
				return "❌ MapReduce content summarization execution failed: " + e.getMessage();
			}
		});
	}

}
