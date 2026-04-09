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
package com.alibaba.cloud.ai.dataagent.service.datasource.impl;

import com.alibaba.cloud.ai.dataagent.bo.DbConfigBO;
import com.alibaba.cloud.ai.dataagent.connector.pool.DBConnectionPoolFactory;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.service.datasource.DatasourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DatasourceServiceImpl implements DatasourceService {

	private final DatasourceMapper datasourceMapper;

	private final DBConnectionPoolFactory poolFactory;

	@Override
	public List<Datasource> getAllDatasource() {
		return datasourceMapper.selectAll();
	}

	@Override
	public List<Datasource> getDatasourceByStatus(String status) {
		return datasourceMapper.selectByStatus(status);
	}

	@Override
	public List<Datasource> getDatasourceByType(String type) {
		return datasourceMapper.selectByType(type);
	}

	@Override
	public Datasource getDatasourceById(Integer id) {
		return datasourceMapper.selectById(id);
	}

	@Override
	public Datasource createDatasource(Datasource datasource) {
		datasourceMapper.insert(datasource);
		return datasource;
	}

	@Override
	public Datasource updateDatasource(Datasource datasource) {
		datasourceMapper.updateById(datasource);
		return datasource;
	}

	@Override
	public void deleteDatasource(Integer id) {
		datasourceMapper.deleteById(id);
	}

	@Override
	public DbConfigBO getDbConfig(Integer datasourceId) {
		Datasource datasource = datasourceMapper.selectById(datasourceId);
		if (datasource == null) {
			return null;
		}
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
		return config;
	}

}
