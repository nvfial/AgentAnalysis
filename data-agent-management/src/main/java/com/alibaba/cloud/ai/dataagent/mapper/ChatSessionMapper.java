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
package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.ChatSession;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatSessionMapper {

	@Select("""
			SELECT * FROM chat_session
			WHERE datasource_id = #{datasourceId} AND status != 'deleted'
			ORDER BY update_time DESC
			""")
	List<ChatSession> selectByDatasourceId(@Param("datasourceId") Integer datasourceId);

	@Select("""
			SELECT * FROM chat_session
			WHERE id = #{sessionId} AND status != 'deleted'
			""")
	ChatSession selectBySessionId(@Param("sessionId") String sessionId);

	@Update("""
			UPDATE chat_session
			SET status = 'archived', update_time = #{updateTime}
			WHERE datasource_id = #{datasourceId}
			""")
	int softDeleteByDatasourceId(@Param("datasourceId") Integer datasourceId, @Param("updateTime") LocalDateTime updateTime);

	@Update("""
			UPDATE chat_session
			SET update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int updateSessionTime(@Param("sessionId") String sessionId, @Param("updateTime") LocalDateTime updateTime);

	@Update("""
			UPDATE chat_session SET
				title = #{title},
				update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int updateTitle(@Param("sessionId") String sessionId, @Param("title") String title,
			@Param("updateTime") LocalDateTime updateTime);

	@Update("""
			UPDATE chat_session
			SET status = 'archived', update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int softDeleteById(@Param("sessionId") String sessionId, @Param("updateTime") LocalDateTime updateTime);

	@Insert("""
			INSERT INTO chat_session (id, datasource_id, title, status, create_time, update_time)
			VALUES (#{id}, #{datasourceId}, #{title}, #{status}, NOW(), NOW())
			""")
	int insert(ChatSession session);

}
