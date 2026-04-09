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
import com.alibaba.cloud.ai.dataagent.mapper.ChatSessionMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

	private final ChatSessionMapper chatSessionMapper;

	@Override
	public List<ChatSession> findByDatasourceId(Integer datasourceId) {
		return chatSessionMapper.selectByDatasourceId(datasourceId);
	}

	@Override
	public ChatSession findBySessionId(String sessionId) {
		return chatSessionMapper.selectBySessionId(sessionId);
	}

	@Override
	public ChatSession createSession(Integer datasourceId, String title) {
		String sessionId = UUID.randomUUID().toString();
		ChatSession session = ChatSession.builder()
			.id(sessionId)
			.datasourceId(datasourceId)
			.title(title != null ? title : "新会话")
			.status("active")
			.build();
		chatSessionMapper.insert(session);
		log.info("Created new chat session: {} for datasource: {}", sessionId, datasourceId);
		return session;
	}

	@Override
	public void clearSessionsByDatasourceId(Integer datasourceId) {
		LocalDateTime now = LocalDateTime.now();
		int updated = chatSessionMapper.softDeleteByDatasourceId(datasourceId, now);
		log.info("Cleared {} sessions for datasource: {}", updated, datasourceId);
	}

	@Override
	public void updateSessionTime(String sessionId) {
		LocalDateTime now = LocalDateTime.now();
		chatSessionMapper.updateSessionTime(sessionId, now);
	}

	@Override
	public void renameSession(String sessionId, String newTitle) {
		LocalDateTime now = LocalDateTime.now();
		chatSessionMapper.updateTitle(sessionId, newTitle, now);
		log.info("Renamed session: {} to: {}", sessionId, newTitle);
	}

	@Override
	public void deleteSession(String sessionId) {
		LocalDateTime now = LocalDateTime.now();
		chatSessionMapper.softDeleteById(sessionId, now);
		log.info("Deleted session: {}", sessionId);
	}

}
