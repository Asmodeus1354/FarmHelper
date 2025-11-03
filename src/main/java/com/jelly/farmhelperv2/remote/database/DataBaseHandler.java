package com.jelly.farmhelperv2.remote.database;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FieldInfo {

        String name;
        Object value;
        String dbType;
    }

    /**
     * Returns a list of all static fields of a class with their name, value, and mapped DB type.
     *
     * @param clazz The class to inspect
     * @return List of FieldInfo records
     */
    public static List<FieldInfo> getStaticFieldsAsRecords(Class<?> clazz) {
        List<FieldInfo> fieldsList = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);

                try {
                    Object value = field.get(null); // static -> no instance needed
                    String dbType = mapToDbType(field.getType());

                    fieldsList.add(new FieldInfo(field.getName(), value, dbType));

                } catch (IllegalAccessException e) {
                    System.err.println("Could not access field: " + field.getName());
                }
            }
        }
        return fieldsList;
    }

    /**
     * Maps a Java type to a simple DB type string.
     */
    private static String mapToDbType(Class<?> type) {
        if (type == String.class) return "TEXT";
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
            if (type == float.class || type == double.class ||
                    type == Float.class || type == Double.class) return "REAL";
            return "NUMBER";
        }
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type.isEnum()) return "TEXT";
        return "BLOB"; // fallback for objects, arrays, etc.
    }

    public static void fetch() {
        List<FieldInfo> fields = getStaticFieldsAsRecords(FarmHelperConfig.class);

    }

    public static void connect() {

    }

    public static void update() {

    }

    /**
     * Checks if a connection to a database can be established.
     *
     * @return true if the connection is successful, false otherwise.
     */
    public static boolean checkConnection() {
        String url = buildJdbcUrl();

        if (url == null) {
            System.err.println("❌ Unsupported database type: " + FarmHelperConfig.dbType);
            return false;
        }

        try (Connection connection = DriverManager.getConnection(url, FarmHelperConfig.dbUsername, FarmHelperConfig.dbPassword)) {
            System.out.println("✅ Connection successful to " + FarmHelperConfig.dbType.toUpperCase() + " database!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Builds a JDBC URL based on the database type and connection parameters.
     */
    private static String buildJdbcUrl() {
        String dbName = FarmHelperConfig.dbName;
        int port = FarmHelperConfig.dbPort;
        String ip = FarmHelperConfig.dbHost;
        switch (FarmHelperConfig.dbType.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC", ip, port, dbName);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", ip, port, dbName);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", ip, port, dbName);
            default:
                return null;
        }
    }

    /**
     * Fetches all rows from a table and returns them as a list of FieldInfo records.
     *
     * @return List of rows, where each row is a list of FieldInfo
     * @throws SQLException if a database access error occurs
     */
    private static List<List<FieldInfo>> fetchAllRows() throws SQLException {
        List<List<FieldInfo>> result = new ArrayList<>();
        String url = buildJdbcUrl();
        if (url == null) return null;

        try (Connection connection = DriverManager.getConnection(url, FarmHelperConfig.dbUsername, FarmHelperConfig.dbPassword)) {
            String query = "SELECT * FROM " + FarmHelperConfig.dbName;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    List<FieldInfo> row = new ArrayList<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String name = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        String dbType = mapToDbType(meta.getColumnTypeName(i));

                        row.add(new FieldInfo(name, value, dbType));
                    }
                    result.add(row);
                }
            }
        }

        return result;
    }

    /**
     * Maps SQL types (from metadata) to a simplified DB type string.
     */
    private static String mapToDbType(String sqlTypeName) {
        String t = sqlTypeName.toUpperCase();

        if (t.contains("CHAR") || t.contains("TEXT") || t.contains("CLOB")) return "TEXT";
        if (t.contains("INT") || t.contains("NUM") || t.contains("DEC") || t.contains("REAL") || t.contains("DOUBLE")) return "NUMBER";
        if (t.contains("BOOL")) return "BOOLEAN";
        if (t.contains("DATE") || t.contains("TIME")) return "DATE";
        return "BLOB"; // fallback for binary/unknown types
    }


}
