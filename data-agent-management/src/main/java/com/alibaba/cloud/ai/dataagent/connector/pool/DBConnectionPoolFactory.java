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
package com.alibaba.cloud.ai.dataagent.connector.pool;

import com.alibaba.cloud.ai.dataagent.bo.DbConfigBO;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DBConnectionPoolFactory {

	private final Map<String, DBConnectionPool> poolMap = new ConcurrentHashMap<>();

	public DBConnectionPoolFactory(List<DBConnectionPool> pools) {
		pools.forEach(this::register);
	}

	public void register(DBConnectionPool pool) {
		poolMap.put(pool.getConnectionPoolType(), pool);
	}

	public boolean isRegistered(String type) {
		return poolMap.containsKey(type);
	}

	public DBConnectionPool getPoolByType(String type) {
		DBConnectionPool direct = poolMap.get(type);
		if (direct != null) {
			return direct;
		}
		return poolMap.values().stream().filter(p -> p.supportedDataSourceType(type)).findFirst().orElse(null);
	}

	public DBConnectionPool getPoolByDbType(String type) {
		return poolMap.values()
			.stream()
			.filter(p -> p.supportedDataSourceType(type))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No DB connection pool found for type: " + type));
	}

	public boolean testConnection(Datasource datasource) {
		DbConfigBO config = new DbConfigBO();
		config.setId(datasource.getId());
		config.setName(datasource.getName());
		config.setType(datasource.getType());
		config.setHost(datasource.getHost());
		config.setPort(datasource.getPort());
		config.setDatabase(datasource.getDatabaseName());
		config.setSchema(datasource.getDatabaseName());
		config.setUsername(datasource.getUsername());
		config.setPassword(datasource.getPassword());
		config.setUrl(datasource.getConnectionUrl());

		try {
			DBConnectionPool pool = getPoolByDbType(datasource.getType());
			if (pool == null) {
				log.warn("No connection pool found for datasource type: {}", datasource.getType());
				return false;
			}
			return pool.ping(config) == com.alibaba.cloud.ai.dataagent.enums.ErrorCodeEnum.SUCCESS;
		}
		catch (Exception e) {
			log.error("Test connection failed for datasource {}: {}", datasource.getId(), e.getMessage(), e);
			return false;
		}
	}

}
