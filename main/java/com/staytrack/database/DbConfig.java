package com.staytrack.database;

public final class DbConfig {
    public static final String HOST = System.getProperty("staytrack.db.host", "localhost");
    public static final String PORT = System.getProperty("staytrack.db.port", "3306");
    public static final String DATABASE = System.getProperty("staytrack.db.name", "staytrack_db");
    public static final String USER = System.getProperty("staytrack.db.user", "root");
    public static final String PASSWORD = System.getProperty("staytrack.db.password", "");
    public static final String SERVER_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private DbConfig() {
    }
}
