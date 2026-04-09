/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.prompt;

import java.util.Map;

public class PromptConstant {

	private static final String NEW_SQL_GENERATE_TEMPLATE = """
		你是一个NL2SQL专家。请根据用户的问题和数据库schema信息，生成准确的SQL查询语句。

		数据库信息:
		{datasourceId}

		Schema上下文:
		{schema_context}

		用户问题: {query}

		请生成SQL查询语句，只返回SQL代码，不要包含其他解释。

		要求:
		1. 只返回SQL语句
		2. 如果无法回答，返回"无法回答该问题"
		3. 确保SQL语法正确
		""";

	private static final String SCHEMA_RECALL_TEMPLATE = """
		你是一个数据库专家。请根据用户的问题，从知识库中检索相关的数据库schema信息。

		用户问题: {query}

		数据库标识: {datasourceId}

		请从向量知识库中检索相关的表结构信息。
		""";

	private static final String JSON_FIX_TEMPLATE = """
		你是一个JSON修复专家。请修复以下JSON字符串中的语法错误。

		原始JSON:
		{json_string}

		错误信息:
		{error_message}

		请只返回修复后的JSON字符串，不要包含其他解释或标记。
		""";

	public static String getNewSqlGeneratorPromptTemplate() {
		return NEW_SQL_GENERATE_TEMPLATE;
	}

	public static String getSchemaRecallPromptTemplate() {
		return SCHEMA_RECALL_TEMPLATE;
	}

	public static String getJsonFixPromptTemplate() {
		return JSON_FIX_TEMPLATE;
	}

	public static String render(String template, Map<String, String> variables) {
		if (template == null || variables == null) {
			return template;
		}
		String result = template;
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String placeholder = "{" + entry.getKey() + "}";
			String value = entry.getValue() != null ? entry.getValue() : "";
			result = result.replace(placeholder, value);
		}
		return result;
	}

}
