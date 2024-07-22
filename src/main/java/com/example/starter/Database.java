package com.example.starter;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
//import  io.vertx.mysqlclient.*;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class Database {
  public static Pool createPool(Vertx vertx) {
    // Load .env file
    Dotenv dotenv = Dotenv.load();

    // Get database config from .env
    String host = dotenv.get("DB_HOST", "localhost");
    int port = Integer.parseInt(dotenv.get("DB_PORT", "3306"));
    String database = dotenv.get("DB_NAME", "dice");
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
      .setPassword(password)
      .setPipeliningLimit(16);  // Enable pipelining

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    return MySQLBuilder.pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();
  }
}
