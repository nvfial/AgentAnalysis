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
package com.alibaba.cloud.ai.dataagent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@EnableAsync
public class DataAgentConfiguration {

	@Value("${spring.webflux.multipart.max-file-size:10MB}")
	private String maxFileSize;

	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(
					HttpClient.create().responseTimeout(Duration.ofSeconds(600))));
	}

	@Bean
	public ExecutorService dbOperationExecutor() {
		int corePoolSize = Math.max(4, Math.min(Runtime.getRuntime().availableProcessors() * 2, 16));
		log.info("Database operation executor initialized with {} threads", corePoolSize);

		ThreadFactory threadFactory = new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "db-operation-" + threadNumber.getAndIncrement());
				t.setPriority(Thread.NORM_PRIORITY);
				return t;
			}
		};

		return new java.util.concurrent.ThreadPoolExecutor(corePoolSize, corePoolSize, 60L,
				java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.LinkedBlockingQueue<>(500),
				threadFactory, new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
	}

}
