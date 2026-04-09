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
package com.alibaba.cloud.ai.dataagent.service.schema;

import com.alibaba.cloud.ai.dataagent.service.vector.VectorStoreService;
import com.alibaba.cloud.ai.dataagent.service.datasource.DatasourceService;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.bo.DbConfigBO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class SchemaServiceImpl implements SchemaService {

	private final VectorStoreService vectorStoreService;

	private final DatasourceService datasourceService;

	@Override
	public String getSchemaContext(String query, String datasourceId) {
		log.info("Getting schema context for query: {}, datasourceId: {}", query, datasourceId);
		try {
			return vectorStoreService.retrieveRelevantSchema(query, datasourceId);
		}
		catch (Exception e) {
			log.error("Failed to retrieve schema context from vector store", e);
			return getSchemaByDatasourceId(Integer.valueOf(datasourceId));
		}
	}

	@Override
	public String getSchemaByDatasourceId(Integer datasourceId) {
		log.info("Getting schema for datasourceId: {}", datasourceId);
		try {
			Datasource datasource = datasourceService.getDatasourceById(datasourceId);
			if (datasource != null) {
				return buildSchemaFromDatasource(datasource);
			}
			return "";
		}
		catch (Exception e) {
			log.error("Failed to get schema for datasourceId: {}", datasourceId, e);
			return "";
		}
	}

	private String buildSchemaFromDatasource(Datasource datasource) {
		StringBuilder sb = new StringBuilder();
		sb.append("数据源: ").append(datasource.getName());
		sb.append("\n类型: ").append(datasource.getType());
		if (datasource.getDescription() != null) {
			sb.append("\n描述: ").append(datasource.getDescription());
		}
		return sb.toString();
	}

}
