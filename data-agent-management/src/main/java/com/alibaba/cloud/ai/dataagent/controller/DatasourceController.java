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

import com.alibaba.cloud.ai.dataagent.connector.pool.DBConnectionPoolFactory;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.service.datasource.DatasourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/datasources")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class DatasourceController {

	private final DatasourceService datasourceService;

	private final DBConnectionPoolFactory poolFactory;

	@GetMapping
	public ResponseEntity<List<Datasource>> getAllDatasources() {
		return ResponseEntity.ok(datasourceService.getAllDatasource());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Datasource> getDatasource(@PathVariable Integer id) {
		return ResponseEntity.ok(datasourceService.getDatasourceById(id));
	}

	@PostMapping
	public ResponseEntity<Datasource> createDatasource(@RequestBody Datasource datasource) {
		Datasource created = datasourceService.createDatasource(datasource);
		return ResponseEntity.ok(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Datasource> updateDatasource(@PathVariable Integer id,
			@RequestBody Datasource datasource) {
		datasource.setId(id);
		return ResponseEntity.ok(datasourceService.updateDatasource(datasource));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDatasource(@PathVariable Integer id) {
		datasourceService.deleteDatasource(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/test")
	public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Integer id) {
		try {
			Datasource datasource = datasourceService.getDatasourceById(id);
			if (datasource == null) {
				return ResponseEntity.badRequest()
					.body(Map.of("success", false, "message", "数据源不存在"));
			}

			boolean canConnect = poolFactory.testConnection(datasource);

			if (canConnect) {
				return ResponseEntity.ok(Map.of("success", true, "message", "连接成功"));
			}
			else {
				return ResponseEntity.ok(Map.of("success", false, "message", "连接失败"));
			}
		}
		catch (Exception e) {
			log.error("Test connection failed for datasource {}: {}", id, e.getMessage(), e);
			return ResponseEntity.ok(Map.of("success", false, "message", "连接失败: " + e.getMessage()));
		}
	}

}
