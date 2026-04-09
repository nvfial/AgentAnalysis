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

import java.util.List;

/**
 * Chat Session Service Class
 */
public interface ChatSessionService {

	/**
	 * Get session list by datasource ID
	 */
	List<ChatSession> findByDatasourceId(Integer datasourceId);

	/**
	 * Create a new session
	 */
	ChatSession createSession(Integer datasourceId, String title);

	/**
	 * Find session by id.
	 */
	ChatSession findBySessionId(String sessionId);

	/**
	 * Clear all sessions for a datasource
	 */
	void clearSessionsByDatasourceId(Integer datasourceId);

	/**
	 * Update the last activity time of a session
	 */
	void updateSessionTime(String sessionId);

	/**
	 * Rename session
	 */
	void renameSession(String sessionId, String newTitle);

	/**
	 * Delete a single session
	 */
	void deleteSession(String sessionId);

}
