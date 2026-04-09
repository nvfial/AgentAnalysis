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
package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.dto.GraphRequest;
import com.alibaba.cloud.ai.dataagent.service.graph.GraphService;
import com.alibaba.cloud.ai.dataagent.vo.GraphNodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static com.alibaba.cloud.ai.dataagent.constant.Constant.STREAM_EVENT_COMPLETE;
import static com.alibaba.cloud.ai.dataagent.constant.Constant.STREAM_EVENT_ERROR;

/**
 * Graph Controller - SSE流式输出接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class GraphController {

	private final GraphService graphService;

	@GetMapping(value = "/stream/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<GraphNodeResponse>> streamSearch(@RequestParam("datasourceId") String datasourceId,
			@RequestParam(value = "threadId", required = false) String threadId, @RequestParam("query") String query,
			@RequestParam(value = "nl2sqlOnly", required = false) boolean nl2sqlOnly, ServerHttpResponse response) {
		response.getHeaders().add("Cache-Control", "no-cache");
		response.getHeaders().add("Connection", "keep-alive");
		response.getHeaders().add("X-Accel-Buffering", "no");

		Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = Sinks.many().unicast().onBackpressureBuffer();

		GraphRequest request = GraphRequest.builder()
			.datasourceId(datasourceId)
			.threadId(threadId)
			.query(query)
			.nl2sqlOnly(nl2sqlOnly)
			.build();
		graphService.graphStreamProcess(sink, request);

		return sink.asFlux().filter(sse -> {
			if (STREAM_EVENT_COMPLETE.equals(sse.event()) || STREAM_EVENT_ERROR.equals(sse.event())) {
				return true;
			}
			return sse.data() != null && sse.data().getText() != null && !sse.data().getText().isEmpty();
		})
			.doOnSubscribe(subscription -> log.info("Client subscribed, threadId: {}", request.getThreadId()))
			.doOnCancel(() -> {
				log.info("Client disconnected, threadId: {}", request.getThreadId());
				if (request.getThreadId() != null) {
					graphService.stopStreamProcessing(request.getThreadId());
				}
			})
			.doOnError(e -> {
				log.error("Stream error, threadId: {}: ", request.getThreadId(), e);
				if (request.getThreadId() != null) {
					graphService.stopStreamProcessing(request.getThreadId());
				}
			})
			.doOnComplete(() -> log.info("Stream completed, threadId: {}", request.getThreadId()));
	}

}
