/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed in in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.service.llm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

@Slf4j
@AllArgsConstructor
public class StreamLlmService implements LlmService {

	private final ChatClient chatClient;

	@Override
	public Flux<ChatResponse> call(String system, String user) {
		return chatClient.prompt()
			.system(system)
			.user(user)
			.stream()
			.chatResponse();
	}

	@Override
	public Flux<ChatResponse> callSystem(String system) {
		return chatClient.prompt()
			.system(system)
			.stream()
			.chatResponse();
	}

	@Override
	public Flux<ChatResponse> callUser(String user) {
		return chatClient.prompt()
			.user(user)
			.stream()
			.chatResponse();
	}

}
