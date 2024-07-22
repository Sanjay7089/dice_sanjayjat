package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {
  public static Future<MySQLPool> createPool(Vertx vertx) {
    // Load .env file
    Dotenv dotenv = Dotenv.load();

    // Get database config  from .env
    String host = dotenv.get("DB_HOST", "localhost");
    int port = Integer.parseInt(dotenv.get("DB_PORT", "3306"));
    String database = dotenv.get("DB_NAME", "default_db");
    String user = dotenv.get("DB_USER", "root");
    String password = dotenv.get("DB_PASSWORD", "");

    // Print loaded values for debugging
    System.out.println("Database configuration:");
    System.out.println("DB_HOST: " + host);
    System.out.println("DB_PORT: " + port);
    System.out.println("DB_NAME: " + database);
    System.out.println("DB_USER: " + user);
    System.out.println("DB_PASSWORD: " + (password.isEmpty() ? "not set" : "******"));

    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(port)
      .setHost(host)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    return Future.succeededFuture(MySQLPool.pool(vertx, connectOptions, poolOptions));
  }
}
