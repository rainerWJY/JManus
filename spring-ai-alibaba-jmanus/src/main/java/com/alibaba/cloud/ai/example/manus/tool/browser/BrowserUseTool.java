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
package com.alibaba.cloud.ai.example.manus.tool.browser;

import com.alibaba.cloud.ai.example.manus.config.ManusProperties;
import com.alibaba.cloud.ai.example.manus.tool.ToolCallBiFunctionDef;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.BrowserRequestVO;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.ClickByElementAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.CloseTabAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.ExecuteJsAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.GetHtmlAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.GetTextAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.InputTextAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.KeyEnterAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.NavigateAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.NewTabAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.RefreshAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.ScreenShotAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.ScrollAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.SwitchTabAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.GetElementPositionByNameAction;
import com.alibaba.cloud.ai.example.manus.tool.browser.actions.MoveToAndClickAction;
import com.alibaba.cloud.ai.example.manus.tool.code.CodeUtils;
import com.alibaba.cloud.ai.example.manus.tool.code.ToolExecuteResult;

import com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartFileOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.function.FunctionToolCallback;

public class BrowserUseTool implements ToolCallBiFunctionDef {

	private static final Logger log = LoggerFactory.getLogger(BrowserUseTool.class);

	private final ChromeDriverService chromeDriverService;

	private final String workingDirectoryPath;

	private final SmartFileOperator smartFileOperator;

	private String planId;

	// Initialize ObjectMapper instance
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public BrowserUseTool(ChromeDriverService chromeDriverService, SmartFileOperator smartFileOperator) {
		this.chromeDriverService = chromeDriverService;
		this.smartFileOperator = smartFileOperator;
		ManusProperties manusProperties = chromeDriverService.getManusProperties();
		this.workingDirectoryPath = CodeUtils.getWorkingDirectory(manusProperties.getBaseDir());
	}

	public DriverWrapper getDriver() {
		return chromeDriverService.getDriver(planId);
	}

	/**
	 * 获取浏览器操作的超时时间配置
	 * @return 超时时间（秒），如果未配置则返回默认值30秒
	 */
	private Integer getBrowserTimeout() {
		Integer timeout = getManusProperties().getBrowserRequestTimeout();
		return timeout != null ? timeout : 30; // 默认超时时间为 30 秒
	}

	private final String PARAMETERS = """
			{
			    "type": "object",
			    "oneOf": [
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "navigate",
			                    "description": "访问特定URL"
			                },
			                "url": {
			                    "type": "string",
			                    "description": "要访问的URL地址"
			                }
			            },
			            "required": ["action", "url"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "click",
			                    "description": "按索引点击元素"
			                },
			                "index": {
			                    "type": "integer",
			                    "description": "要点击的元素索引",
			                    "minimum": 0
			                }
			            },
			            "required": ["action", "index"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "input_text",
			                    "description": "在元素中输入文本"
			                },
			                "index": {
			                    "type": "integer",
			                    "description": "要输入文本的元素索引",
			                    "minimum": 0
			                },
			                "text": {
			                    "type": "string",
			                    "description": "要输入的文本内容"
			                }
			            },
			            "required": ["action", "index", "text"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "key_enter",
			                    "description": "按回车键"
			                },
			                "index": {
			                    "type": "integer",
			                    "description": "要按回车键的元素索引",
			                    "minimum": 0
			                }
			            },
			            "required": ["action", "index"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "screenshot",
			                    "description": "捕获屏幕截图"
			                }
			            },
			            "required": ["action"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "get_html",
			                    "description": "获取当前页面的HTML内容"
			                }
			            },
			            "required": ["action"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "get_text",
			                    "description": "获取当前页面文本内容"
			                }
			            },
			            "required": ["action"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "execute_js",
			                    "description": "执行JavaScript代码"
			                },
			                "script": {
			                    "type": "string",
			                    "description": "要执行的JavaScript代码"
			                }
			            },
			            "required": ["action", "script"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "scroll",
			                    "description": "滚动页面"
			                },
			                "scroll_amount": {
			                    "type": "integer",
			                    "description": "滚动像素数（正数向下滚动，负数向上滚动）"
			                }
			            },
			            "required": ["action", "scroll_amount"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "switch_tab",
			                    "description": "切换到特定标签页"
			                },
			                "tab_id": {
			                    "type": "integer",
			                    "description": "要切换到的标签页ID",
			                    "minimum": 0
			                }
			            },
			            "required": ["action", "tab_id"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "new_tab",
			                    "description": "打开新标签页"
			                },
			                "url": {
			                    "type": "string",
			                    "description": "在新标签页中打开的URL地址"
			                }
			            },
			            "required": ["action", "url"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "close_tab",
			                    "description": "关闭当前标签页"
			                }
			            },
			            "required": ["action"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "refresh",
			                    "description": "刷新当前页面"
			                }
			            },
			            "required": ["action"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "get_element_position",
			                    "description": "通过关键词获取元素的位置坐标"
			                },
			                "element_name": {
			                    "type": "string",
			                    "description": "要查找的元素名称或关键词"
			                }
			            },
			            "required": ["action", "element_name"],
			            "additionalProperties": false
			        },
			        {
			            "properties": {
			                "action": {
			                    "type": "string",
			                    "const": "move_to_and_click",
			                    "description": "移动到指定的绝对位置并点击"
			                },
			                "position_x": {
			                    "type": "integer",
			                    "description": "X坐标",
			                    "minimum": 0
			                },
			                "position_y": {
			                    "type": "integer",
			                    "description": "Y坐标",
			                    "minimum": 0
			                }
			            },
			            "required": ["action", "position_x", "position_y"],
			            "additionalProperties": false
			        }
			    ]
			}
			""";

	private final String name = "browser_use";

	private final String description = """
			与网页浏览器交互，执行各种操作，如导航、元素交互、内容提取和标签页管理。搜索类优先考虑此工具。
			支持的操作包括：
			- 'navigate'：访问特定URL，默认使用https://baidu.com
			- 'click'：按索引点击元素
			- 'input_text'：在元素中输入文本，对于百度(Baidu)，输入框的索引是
			- 'key_enter'：按回车键
			- 'screenshot'：捕获屏幕截图
			- 'get_html'：获取当前页面的HTML内容(不支持url参数)
			- 'get_text'：获取当前页面文本内容(不支持url参数)
			- 'execute_js'：执行JavaScript代码
			- 'scroll'：滚动页面
			- 'switch_tab'：切换到特定标签页
			- 'new_tab'：打开新标签页
			- 'close_tab'：关闭当前标签页
			- 'refresh'：刷新当前页面
			- 'get_element_position'：通过关键词获取元素的位置坐标(x,y)
			- 'move_to_and_click'：移动到指定的绝对位置(x,y)并点击
			""";

	public OpenAiApi.FunctionTool getToolDefinition() {
		OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
		OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
		return functionTool;
	}

	public FunctionToolCallback<String, ToolExecuteResult> getFunctionToolCallback(
			ChromeDriverService chromeDriverService, SmartFileOperator smartFileOperator) {
		return FunctionToolCallback.builder(name, new BrowserUseTool(chromeDriverService, smartFileOperator))
			.description(description)
			.inputSchema(PARAMETERS)
			.inputType(String.class)
			.build();
	}

	public ToolExecuteResult run(String toolInput) {
		log.info("BrowserUseTool toolInput:" + toolInput);

		// 直接将JSON字符串解析为BrowserRequestVO对象
		BrowserRequestVO requestVO;
		// Add exception handling for JSON deserialization
		try {
			requestVO = objectMapper.readValue(toolInput, BrowserRequestVO.class);
		}
		catch (Exception e) {
			log.error("Error deserializing JSON", e);
			return new ToolExecuteResult("Error deserializing JSON: " + e.getMessage());
		}

		// 从RequestVO中获取参数
		String action = requestVO.getAction();
		try {
			if (action == null) {
				return new ToolExecuteResult("Action parameter is required");
			}

			ToolExecuteResult result;
			switch (action) {
				case "navigate": {
					result = new NavigateAction(this).execute(requestVO);
					break;
				}
				case "click": {
					result = new ClickByElementAction(this).execute(requestVO);
					break;
				}
				case "input_text": {
					result = new InputTextAction(this).execute(requestVO);
					break;
				}
				case "key_enter": {
					result = new KeyEnterAction(this).execute(requestVO);
					break;
				}
				case "screenshot": {
					result = new ScreenShotAction(this).execute(requestVO);
					break;
				}
				case "get_html": {
					result = new GetHtmlAction(this).execute(requestVO);
					// HTML内容通常很长，使用智能处理
					if (result != null && result.getOutput() != null) {
						com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
							smartFileOperator.processResult(result.getOutput(), planId, workingDirectoryPath);
						return new ToolExecuteResult(processedResult.getSummary());
					}
					return result;
				}
				case "get_text": {
					result = new GetTextAction(this).execute(requestVO);
					// 文本内容可能很长，使用智能处理
					if (result != null && result.getOutput() != null) {
						com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
							smartFileOperator.processResult(result.getOutput(), planId, workingDirectoryPath);
						return new ToolExecuteResult(processedResult.getSummary());
					}
					return result;
				}
				case "execute_js": {
					result = new ExecuteJsAction(this).execute(requestVO);
					// JS执行结果可能很长，使用智能处理
					if (result != null && result.getOutput() != null) {
						com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
							smartFileOperator.processResult(result.getOutput(), planId, workingDirectoryPath);
						return new ToolExecuteResult(processedResult.getSummary());
					}
					return result;
				}
				case "scroll": {
					result = new ScrollAction(this).execute(requestVO);
					break;
				}
				case "new_tab": {
					result = new NewTabAction(this).execute(requestVO);
					break;
				}
				case "close_tab": {
					result = new CloseTabAction(this).execute(requestVO);
					break;
				}
				case "switch_tab": {
					result = new SwitchTabAction(this).execute(requestVO);
					break;
				}
				case "refresh": {
					result = new RefreshAction(this).execute(requestVO);
					break;
				}
				case "get_element_position": {
					result = new GetElementPositionByNameAction(this).execute(requestVO);
					break;
				}
				case "move_to_and_click": {
					result = new MoveToAndClickAction(this).execute(requestVO);
					break;
				}
				default:
					return new ToolExecuteResult("Unknown action: " + action);
			}

			// 对于其他操作，也进行智能处理（但阈值通常不会超过）
			if (result != null && result.getOutput() != null) {
				com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
					smartFileOperator.processResult(result.getOutput(), planId, workingDirectoryPath);
				return new ToolExecuteResult(processedResult.getSummary());
			}
			return result;
		}
		catch (Exception e) {
			log.error("Browser action '" + action + "' failed", e);
			return new ToolExecuteResult("Browser action '" + action + "' failed: " + e.getMessage());
		}
	}

	private List<Map<String, Object>> getTabsInfo(Page page) {
		return page.context().pages().stream().map(p -> {
			Map<String, Object> tabInfo = new HashMap<>();
			tabInfo.put("url", p.url());
			tabInfo.put("title", p.title());

			return tabInfo;
		}).toList();
	}

	public Map<String, Object> getCurrentState(Page page) {
		Map<String, Object> state = new HashMap<>();

		try {
			// 等待页面加载完成，避免在导航过程中获取信息时出现上下文销毁错误
			try {
				Integer timeout = getBrowserTimeout();
				page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
						new Page.WaitForLoadStateOptions().setTimeout(timeout * 1000));
			}
			catch (Exception loadException) {
				log.warn("Page load state wait timeout or failed, continuing anyway: {}", loadException.getMessage());
			}

			// 获取基本信息
			String currentUrl = page.url();
			String title = page.title();
			state.put("url", currentUrl);
			state.put("title", title);

			// 获取标签页信息
			List<Map<String, Object>> tabs = getTabsInfo(page);
			state.put("tabs", tabs);

			String interactiveElements = chromeDriverService.getDriver(planId)
				.getInteractiveElementRegistry()
				.generateElementsInfoText(page);
			state.put("interactive_elements", interactiveElements);

			return state;

		}
		catch (Exception e) {
			log.error("Failed to get browser state", e);
			state.put("error", "Failed to get browser state: " + e.getMessage());
			return state;
		}
	}

	@Override
	public ToolExecuteResult apply(String t, ToolContext u) {

		return run(t);
	}

	@Override
	public String getServiceGroup() {
		return "default-service-group";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
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
	public String getCurrentToolStateString() {
		DriverWrapper driver = getDriver();
		Map<String, Object> state = getCurrentState(driver.getCurrentPage());
		// 构建URL和标题信息
		String urlInfo = String.format("\n   URL: %s\n   Title: %s", state.get("url"), state.get("title"));

		// 构建标签页信息
		List<Map<String, Object>> tabs = (List<Map<String, Object>>) state.get("tabs");
		String tabsInfo = (tabs != null) ? String.format("\n   %d tab(s) available", tabs.size()) : "";
		if (tabs != null) {
			for (int i = 0; i < tabs.size(); i++) {
				Map<String, Object> tab = tabs.get(i);
				String tabUrl = (String) tab.get("url");
				String tabTitle = (String) tab.get("title");
				tabsInfo += String.format("\n   [%d] %s: %s", i, tabTitle, tabUrl);
			}
		}
		// 获取滚动信息
		Map<String, Object> scrollInfo = (Map<String, Object>) state.get("scroll_info");
		String contentAbove = "";
		String contentBelow = "";
		if (scrollInfo != null) {
			Long pixelsAbove = (Long) scrollInfo.get("pixels_above");
			Long pixelsBelow = (Long) scrollInfo.get("pixels_below");
			contentAbove = pixelsAbove > 0 ? String.format(" (%d pixels)", pixelsAbove) : "";
			contentBelow = pixelsBelow > 0 ? String.format(" (%d pixels)", pixelsBelow) : "";
		}

		// 获取交互元素信息
		String elementsInfo = (String) state.get("interactive_elements");

		// 构建最终的状态字符串
		String retString = String.format("""

				- Current URL and page title:
				%s

				- Available tabs:
				%s

				- Interactive elements and their indices:
				%s

				- Content above%s or below%s the viewport (if indicated)

				- Any action results or errors:
				%s
				""", urlInfo, tabsInfo, elementsInfo != null ? elementsInfo : "", contentAbove, contentBelow,
				state.containsKey("error") ? state.get("error") : "");

		// 使用 SmartFileOperator 处理可能过长的状态字符串
		if (planId != null) {
			com.alibaba.cloud.ai.example.manus.tool.textOperator.SmartProcessResult processedResult = 
				smartFileOperator.processResult(retString, planId, workingDirectoryPath);
			return processedResult.getSummary();
		}

		return retString;
	}

	// cleanup 方法已经存在，只需确保它符合接口规范
	@Override
	public void cleanup(String planId) {
		if (planId != null) {
			log.info("Cleaning up Chrome resources for plan: {}", planId);
			this.chromeDriverService.closeDriverForPlan(planId);
		}
	}

	public ManusProperties getManusProperties() {
		return this.chromeDriverService.getManusProperties();
	}

}
