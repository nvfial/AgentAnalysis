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
package com.alibaba.cloud.ai.dataagent.service.chat;

import com.alibaba.cloud.ai.dataagent.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionTitleService {

	private static final String DEFAULT_TITLE = "新会话";

	private final ChatSessionService chatSessionService;

	private final Set<String> runningTasks = ConcurrentHashMap.newKeySet();

	@Async
	public void scheduleTitleGeneration(String sessionId, String userMessage) {
		if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(userMessage)) {
			return;
		}
		if (!runningTasks.add(sessionId)) {
			return;
		}
		CompletableFuture.runAsync(() -> generateAndPersist(sessionId, userMessage))
			.whenComplete((unused, throwable) -> runningTasks.remove(sessionId));
	}

	private void generateAndPersist(String sessionId, String userMessage) {
		try {
			ChatSession session = chatSessionService.findBySessionId(sessionId);
			if (session == null) {
				log.warn("Session {} not found when generating title", sessionId);
				return;
			}
			if (hasCustomTitle(session)) {
				log.debug("Session {} already has custom title, skip generating", sessionId);
				return;
			}

			String title = generateTitle(userMessage);
			if (!StringUtils.hasText(title)) {
				title = fallbackTitle(userMessage);
			}

			chatSessionService.renameSession(sessionId, title);
			log.info("Generated title '{}' for session {}", title, sessionId);
		}
		catch (Exception e) {
			log.error("Failed to generate title for session {}: {}", sessionId, e.getMessage(), e);
		}
	}

	private boolean hasCustomTitle(ChatSession session) {
		return session.getTitle() != null && !DEFAULT_TITLE.equals(session.getTitle());
	}

	private String generateTitle(String userMessage) {
		int maxLen = 20;
		if (userMessage.length() <= maxLen) {
			return userMessage;
		}
		return userMessage.substring(0, maxLen) + "...";
	}

	private String fallbackTitle(String userMessage) {
		int maxLen = 20;
		if (userMessage.length() <= maxLen) {
			return userMessage;
		}
		return userMessage.substring(0, maxLen) + "...";
	}

}
