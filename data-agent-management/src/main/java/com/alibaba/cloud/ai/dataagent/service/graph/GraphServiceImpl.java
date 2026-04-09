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
package com.alibaba.cloud.ai.dataagent.service.graph;

import com.alibaba.cloud.ai.dataagent.dto.GraphRequest;
import com.alibaba.cloud.ai.dataagent.service.llm.LlmService;
import com.alibaba.cloud.ai.dataagent.service.schema.SchemaService;
import com.alibaba.cloud.ai.dataagent.service.nl2sql.Nl2SqlService;
import com.alibaba.cloud.ai.dataagent.service.vector.VectorStoreService;
import com.alibaba.cloud.ai.dataagent.vo.GraphNodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.cloud.ai.dataagent.constant.Constant.*;

@Slf4j
@Service
public class GraphServiceImpl implements GraphService {

	private final Nl2SqlService nl2SqlService;

	private final SchemaService schemaService;

	private final VectorStoreService vectorStoreService;

	private final LlmService llmService;

	private final ConcurrentHashMap<String, StreamContext> streamContextMap = new ConcurrentHashMap<>();

	public GraphServiceImpl(Nl2SqlService nl2SqlService, SchemaService schemaService,
			VectorStoreService vectorStoreService, LlmService llmService) {
		this.nl2SqlService = nl2SqlService;
		this.schemaService = schemaService;
		this.vectorStoreService = vectorStoreService;
		this.llmService = llmService;
	}

	@Override
	public String nl2sql(String naturalQuery, String datasourceId) {
		return nl2SqlService.generateSql(naturalQuery, datasourceId);
	}

	@Override
	public void graphStreamProcess(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, GraphRequest graphRequest) {
		if (!StringUtils.hasText(graphRequest.getThreadId())) {
			graphRequest.setThreadId(UUID.randomUUID().toString());
		}
		String threadId = graphRequest.getThreadId();
		StreamContext context = streamContextMap.computeIfAbsent(threadId, k -> new StreamContext());
		context.setSink(sink);

		new Thread(() -> {
			try {
				processRequest(context, graphRequest);
			}
			catch (Exception e) {
				log.error("Error processing request for threadId: {}", threadId, e);
				emitError(context, graphRequest.getDatasourceId(), threadId, e.getMessage());
			}
			finally {
				streamContextMap.remove(threadId);
			}
		}).start();
	}

	private void processRequest(StreamContext context, GraphRequest graphRequest) {
		String query = graphRequest.getQuery();
		String datasourceId = graphRequest.getDatasourceId();
		String threadId = graphRequest.getThreadId();
		boolean nl2sqlOnly = graphRequest.isNl2sqlOnly();

		emitNodeStart(context, datasourceId, threadId, "schema_recall");

		String schemaContext = "";
		try {
			schemaContext = schemaService.getSchemaContext(query, datasourceId);
			emitNodeComplete(context, datasourceId, threadId, "schema_recall", schemaContext);
		}
		catch (Exception e) {
			log.error("Failed to get schema context", e);
			emitError(context, datasourceId, threadId, "Schema retrieval failed: " + e.getMessage());
			return;
		}

		emitNodeStart(context, datasourceId, threadId, "sql_generate");

		String sql;
		try {
			if (nl2sqlOnly) {
				sql = nl2SqlService.generateSql(query, datasourceId);
			}
			else {
				sql = nl2SqlService.generateSqlWithStreaming(query, datasourceId, schemaContext, chunk -> {
					emitChunk(context, datasourceId, threadId, "sql_generate", chunk);
				});
			}
			emitNodeComplete(context, datasourceId, threadId, "sql_generate", sql);
		}
		catch (Exception e) {
			log.error("Failed to generate SQL", e);
			emitError(context, datasourceId, threadId, "SQL generation failed: " + e.getMessage());
			return;
		}

		emitComplete(context, datasourceId, threadId);
	}

	@Override
	public void stopStreamProcessing(String threadId) {
		if (!StringUtils.hasText(threadId)) {
			return;
		}
		log.info("Stopping stream processing for threadId: {}", threadId);
		StreamContext context = streamContextMap.remove(threadId);
		if (context != null) {
			context.getSink().tryEmitComplete();
		}
	}

	private void emitNodeStart(StreamContext context, String datasourceId, String threadId, String nodeName) {
		if (context.getSink().currentSubscriberCount() > 0) {
			GraphNodeResponse response = GraphNodeResponse.builder()
				.datasourceId(datasourceId)
				.threadId(threadId)
				.nodeName(nodeName)
				.event("node_start")
				.build();
			context.getSink().tryEmitNext(ServerSentEvent.builder(response).build());
		}
	}

	private void emitNodeComplete(StreamContext context, String datasourceId, String threadId, String nodeName,
			String result) {
		if (context.getSink().currentSubscriberCount() > 0) {
			GraphNodeResponse response = GraphNodeResponse.builder()
				.datasourceId(datasourceId)
				.threadId(threadId)
				.nodeName(nodeName)
				.text(result)
				.event("node_complete")
				.build();
			context.getSink().tryEmitNext(ServerSentEvent.builder(response).build());
		}
	}

	private void emitChunk(StreamContext context, String datasourceId, String threadId, String nodeName, String chunk) {
		if (context.getSink().currentSubscriberCount() > 0) {
			GraphNodeResponse response = GraphNodeResponse.builder()
				.datasourceId(datasourceId)
				.threadId(threadId)
				.nodeName(nodeName)
				.text(chunk)
				.build();
			context.getSink().tryEmitNext(ServerSentEvent.builder(response).build());
		}
	}

	private void emitError(StreamContext context, String datasourceId, String threadId, String errorMessage) {
		if (context.getSink().currentSubscriberCount() > 0) {
			GraphNodeResponse response = GraphNodeResponse.error(datasourceId, threadId, errorMessage);
			context.getSink()
				.tryEmitNext(ServerSentEvent.builder(response).event(STREAM_EVENT_ERROR).build());
			context.getSink().tryEmitComplete();
		}
	}

	private void emitComplete(StreamContext context, String datasourceId, String threadId) {
		if (context.getSink().currentSubscriberCount() > 0) {
			GraphNodeResponse response = GraphNodeResponse.complete(datasourceId, threadId);
			context.getSink()
				.tryEmitNext(ServerSentEvent.builder(response).event(STREAM_EVENT_COMPLETE).build());
			context.getSink().tryEmitComplete();
		}
	}

	public static class StreamContext {

		private Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink;

		public Sinks.Many<ServerSentEvent<GraphNodeResponse>> getSink() {
			return sink;
		}

		public void setSink(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink) {
			this.sink = sink;
		}

	}

}
