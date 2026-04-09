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
package com.alibaba.cloud.ai.dataagent.enums;

public enum BizDataSourceTypeEnum {

	MYSQL("MySQL", "mysql", "jdbc", "mysql"),

	POSTGRESQL("PostgreSQL", "postgresql", "jdbc", "postgresql"),

	ORACLE("Oracle", "oracle", "jdbc", "oracle"),

	H2("H2", "h2", "jdbc", "h2"),

	SQLSERVER("SQLServer", "sqlserver", "jdbc", "mssql");

	private final String description;

	private final String code;

	private final String protocol;

	private final String typeName;

	BizDataSourceTypeEnum(String description, String code, String protocol, String typeName) {
		this.description = description;
		this.code = code;
		this.protocol = protocol;
		this.typeName = typeName;
	}

	public String getDescription() {
		return description;
	}

	public String getCode() {
		return code;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getDialect() {
		return code;
	}

	public static boolean isSqlServerDialect(String typeName) {
		return SQLSERVER.code.equalsIgnoreCase(typeName);
	}

	public static boolean isOracleDialect(String typeName) {
		return ORACLE.code.equalsIgnoreCase(typeName);
	}

	public static boolean isPgDialect(String typeName) {
		return POSTGRESQL.code.equalsIgnoreCase(typeName);
	}

	public static BizDataSourceTypeEnum fromCode(String code) {
		for (BizDataSourceTypeEnum type : values()) {
			if (type.code.equalsIgnoreCase(code)) {
				return type;
			}
		}
		return null;
	}

	public static BizDataSourceTypeEnum fromTypeName(String typeName) {
		if (typeName == null) {
			return null;
		}
		for (BizDataSourceTypeEnum type : values()) {
			if (type.typeName.equalsIgnoreCase(typeName) || type.name().equalsIgnoreCase(typeName)) {
				return type;
			}
		}
		return fromCode(typeName);
	}

}
