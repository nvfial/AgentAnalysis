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
package com.alibaba.cloud.ai.dataagent.service.nl2sql;

import com.alibaba.cloud.ai.dataagent.service.llm.LlmService;
import com.alibaba.cloud.ai.dataagent.prompt.PromptHelper;
import com.alibaba.cloud.ai.dataagent.util.MarkdownParserUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class Nl2SqlServiceImpl implements Nl2SqlService {

	public final LlmService llmService;

	@Override
	public String generateSql(String query, String datasourceId) {
		log.info("Generating SQL for query: {}, datasourceId: {}", query, datasourceId);

		String prompt = PromptHelper.buildNewSqlGeneratorPrompt(query, datasourceId);
		log.debug("SQL generator prompt: \n{} \n", prompt);

		StringBuilder sqlBuilder = new StringBuilder();
		llmService.callSystem(prompt)
			.doOnNext(response -> {
				String text = response.getResult().getOutput().getText();
				if (text != null) {
					sqlBuilder.append(text);
				}
			})
			.blockLast();

		String generatedSql = sqlBuilder.toString();
		return sqlTrim(generatedSql);
	}

	@Override
	public String generateSqlWithStreaming(String query, String datasourceId, String schemaContext,
			Consumer<String> chunkConsumer) {
		log.info("Generating SQL with streaming for query: {}, datasourceId: {}", query, datasourceId);

		String prompt = PromptHelper.buildNewSqlGeneratorPrompt(query, datasourceId);
		if (StringUtils.hasText(schemaContext)) {
			prompt = prompt.replace("{schema_context}", schemaContext);
		}
		log.debug("SQL generator prompt with streaming: \n{} \n", prompt);

		StringBuilder sqlBuilder = new StringBuilder();
		llmService.callSystem(prompt)
			.doOnNext(response -> {
				String text = response.getResult().getOutput().getText();
				if (text != null) {
					sqlBuilder.append(text);
					chunkConsumer.accept(text);
				}
			})
			.blockLast();

		String generatedSql = sqlBuilder.toString();
		return sqlTrim(generatedSql);
	}

}
