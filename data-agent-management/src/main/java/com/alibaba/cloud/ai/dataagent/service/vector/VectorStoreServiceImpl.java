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
package com.alibaba.cloud.ai.dataagent.service.vector;

import com.alibaba.cloud.ai.dataagent.service.vectorstore.AgentVectorStoreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class VectorStoreServiceImpl implements VectorStoreService {

	private final VectorStore vectorStore;

	private final AgentVectorStoreService agentVectorStoreService;

	@Override
	public String retrieveRelevantSchema(String query, String datasourceId) {
		log.info("Retrieving relevant schema for query: {}, datasourceId: {}", query, datasourceId);
		try {
			List<Document> documents = agentVectorStoreService.search(query, datasourceId, 5);
			if (documents == null || documents.isEmpty()) {
				log.info("No relevant documents found, returning empty schema context");
				return "";
			}
			return documents.stream()
				.map(Document::getText)
				.collect(Collectors.joining("\n\n"));
		}
		catch (Exception e) {
			log.error("Failed to retrieve relevant schema from vector store", e);
			return "";
		}
	}

}
