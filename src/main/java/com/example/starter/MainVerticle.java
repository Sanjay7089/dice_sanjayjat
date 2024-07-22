package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
public class MainVerticle extends AbstractVerticle {
  private MySQLPool mypool;

  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {


    Database.createPool(vertx).onSuccess(pool -> {
      this.mypool = pool;
      makeTable();
      setupRouter(startPromise);
    }).onFailure(error -> {
      System.err.println("Failed to create database pool: " + error.getMessage());
      startPromise.fail(error);
    });
  }

  private void setupRouter(Promise<Void> startPromise) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.post("/login").handler(this::loginHandler);
    router.get("/users").handler(this::fetchUserHandler);

    vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void makeTable() {
    String query = "CREATE TABLE IF NOT EXISTS allusers (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255), password VARCHAR(255), success BOOLEAN)";
    mypool.query(query).execute(ar -> {
      if (ar.failed()) {
        System.err.println("Failed to make table" + ar.cause());
      }
    });
  }

  private void loginHandler(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();
    String username = body.getString("username");
    String password = body.getString("password");

    boolean success = isLongestPalindromicSubstring(username, password);

    String sql = "INSERT INTO allusers (username, password, success) VALUES (?, ?, ?)";
    Tuple params = Tuple.of(username, password, success);

    mypool.preparedQuery(sql).execute(params, ar -> {
      if (ar.succeeded()) {
        ctx.response()
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("success", success).encode());
      } else {
        System.err.println("Database error in fetchUserHandler: " + ar.cause().getMessage());
        ctx.response()
          .setStatusCode(500)
          .end(new JsonObject().put("error", "Database error: " + ar.cause().getMessage()).encode());
      }
    });
  }

  private void fetchUserHandler(RoutingContext ctx) {
    String sql = "SELECT username, password FROM allusers WHERE success = TRUE";
    mypool.query(sql).execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> result = ar.result();
        JsonArray users = new JsonArray();
        for (Row row : result) {
          users.add(new JsonObject()
            .put("username", row.getString("username"))
            .put("password", row.getString("password")));
        }
        ctx.response()
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("users", users).encode());
      } else {
        ctx.response()
          .setStatusCode(500)
          .end(new JsonObject().put("error", "Database error").encode());
      }
    });
  }

  private boolean isLongestPalindromicSubstring(String username, String password) {
    String longest = "";
    for (int i = 0; i < username.length(); i++) {
      for (int j = i + 1; j <= username.length(); j++) {
        String substr = username.substring(i, j);
        if (isPalindrome(substr) && substr.length() > longest.length()) {
          longest = substr;
        }
      }
    }
    return password.equals(longest);
  }

  private boolean isPalindrome(String s) {
    int left = 0;
    int right = s.length() - 1;
    while (left < right) {
      if (s.charAt(left) != s.charAt(right)) {
        return false;
      }
      left++;
      right--;
    }
    return true;
  }
}
