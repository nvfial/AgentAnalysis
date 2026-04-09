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
package com.alibaba.cloud.ai.dataagent.util;

import java.sql.Types;

public final class ColumnTypeUtil {

	private ColumnTypeUtil() {
	}

	public static String wrapType(String typeName) {
		if (typeName == null) {
			return "VARCHAR";
		}
		String upper = typeName.toUpperCase();
		if (upper.contains("INT") || upper.contains("BIGINT") || upper.contains("SMALLINT")
				|| upper.contains("TINYINT")) {
			return upper;
		}
		if (upper.contains("DECIMAL") || upper.contains("NUMERIC") || upper.contains("FLOAT")
				|| upper.contains("DOUBLE")) {
			return upper;
		}
		if (upper.contains("VARCHAR") || upper.contains("CHAR") || upper.contains("TEXT")) {
			return upper;
		}
		if (upper.contains("DATE") || upper.contains("TIME") || upper.contains("TIMESTAMP")) {
			return upper;
		}
		if (upper.contains("BLOB") || upper.contains("BINARY")) {
			return upper;
		}
		return "VARCHAR(" + extractSize(typeName) + ")";
	}

	public static String wrapType(String typeName, int sqlType) {
		if (typeName == null) {
			return "VARCHAR";
		}
		switch (sqlType) {
			case Types.VARCHAR:
			case Types.CHAR:
				return wrapStringType(typeName);
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.SMALLINT:
			case Types.TINYINT:
				return wrapNumericType(typeName);
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				return wrapDecimalType(typeName);
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return wrapDateType(typeName);
			case Types.BOOLEAN:
			case Types.BIT:
				return wrapBooleanType(typeName);
			case Types.BLOB:
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				return wrapBinaryType(typeName);
			case Types.CLOB:
			case Types.LONGVARCHAR:
				return wrapTextType(typeName);
			default:
				return typeName.toUpperCase();
		}
	}

	private static String wrapStringType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("VARCHAR") || upper.contains("CHAR") || upper.contains("TEXT")) {
			return upper;
		}
		return "VARCHAR(" + extractSize(typeName) + ")";
	}

	private static String wrapNumericType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("INT") || upper.contains("DECIMAL") || upper.contains("NUMERIC")) {
			return upper;
		}
		return "INTEGER";
	}

	private static String wrapDecimalType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("DECIMAL") || upper.contains("NUMERIC") || upper.contains("FLOAT")
				|| upper.contains("DOUBLE")) {
			return upper;
		}
		return "DECIMAL(10,2)";
	}

	private static String wrapDateType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("DATE") || upper.contains("TIME") || upper.contains("TIMESTAMP")) {
			return upper;
		}
		return "DATETIME";
	}

	private static String wrapBooleanType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("BOOL") || upper.contains("BIT")) {
			return upper;
		}
		return "BOOLEAN";
	}

	private static String wrapBinaryType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("BLOB") || upper.contains("BINARY") || upper.contains("VARBINARY")) {
			return upper;
		}
		return "BLOB";
	}

	private static String wrapTextType(String typeName) {
		String upper = typeName.toUpperCase();
		if (upper.contains("TEXT") || upper.contains("CLOB") || upper.contains("VARCHAR")) {
			return upper;
		}
		return "TEXT";
	}

	private static int extractSize(String typeName) {
		if (typeName == null) {
			return 255;
		}
		StringBuilder sb = new StringBuilder();
		for (char c : typeName.toCharArray()) {
			if (Character.isDigit(c)) {
				sb.append(c);
			}
		}
		if (sb.length() > 0) {
			try {
				return Math.min(Integer.parseInt(sb.toString()), 65535);
			}
			catch (NumberFormatException e) {
				return 255;
			}
		}
		return 255;
	}

}
