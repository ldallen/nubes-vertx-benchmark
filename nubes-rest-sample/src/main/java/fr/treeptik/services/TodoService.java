package fr.treeptik.services;


import fr.treeptik.domains.Todo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import com.github.aesteve.vertx.nubes.services.Service;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;


public class TodoService implements Service{

	private JDBCClient jdbcClient;
	private JsonObject confJdbc;

	@Override
	public void init(Vertx vertx) {
		jdbcClient = JDBCClient.createShared(vertx, confJdbc);
	}

	public TodoService(JsonObject confJdbc) {
		this.confJdbc = confJdbc;
	}

	public void getAll(Handler<AsyncResult<JsonArray>> result){

		jdbcClient.getConnection(r -> {
			if (r.succeeded()) {

				SQLConnection sqlConnection = r.result();
				String query = "Select * from todos limit 20";

				sqlConnection.query(query, h -> {
					if (h.succeeded()) {

						JsonArray resList = new JsonArray(h.result().getRows());
						result.handle(Future.succeededFuture(resList));
					} else {
						result.handle(Future.failedFuture(h.cause().getMessage()));
					}
					sqlConnection.close();
				});
			}
		});

	}

	public void getSpecific(String type, String value, Handler<AsyncResult<JsonArray>> result) {

		jdbcClient.getConnection(r -> {
			if (r.succeeded()) {

				SQLConnection sqlConnection = r.result();
				String query = "Select * from todos where todos.";
				JsonArray param = new JsonArray();
				switch (type) {
					case "id": {
						query += "id=?";
						param.add(Integer.parseInt(value));
						break;
					}
					case "action": {
						query += "action=?";
						param.add(value);
						break;
					}
					case "done": {
						query += "done=?";
						param.add(Boolean.parseBoolean(value));
						break;
					}
					default: {
						result.handle(Future.failedFuture("couldn't find the attribute: " + type));
					}
				}
				query += " limit 20";

				sqlConnection.queryWithParams(query, param, h -> {
					if (h.succeeded()) {
						JsonArray resList = new JsonArray(h.result().getRows());
						result.handle(Future.succeededFuture(resList));
					} else {
						result.handle(Future.failedFuture(h.cause().getMessage()));
					}
					sqlConnection.close();
				});
			}
		});

	}

	public void delete(Integer id, Handler<AsyncResult<String>> result) {

		jdbcClient.getConnection(res -> {
			final SQLConnection sqlConnection = res.result();
			JsonArray sqlParams = new JsonArray();
			sqlParams.add(id);

			sqlConnection.updateWithParams("Delete from todos where todos.id=?", sqlParams, h -> {
				if (h.succeeded()) {
					if (h.result().getUpdated() > 0)
						result.handle(Future.succeededFuture("successfully deleted task " + id));
					else
						result.handle(Future.failedFuture("couldn't delete task: " + id + "...Wrong id ?"));

				} else {
					result.handle(Future.failedFuture(h.cause().getMessage()));
				}
				sqlConnection.close();
			});
		});

	}

	public void updateStatus(Integer id, Boolean newStatus, Handler<AsyncResult<String>> result) {

		jdbcClient.getConnection(r -> {
			if (r.succeeded()) {

				SQLConnection sqlConnection = r.result();
				JsonArray sqlParams = new JsonArray();
				sqlParams.add(newStatus);
				sqlParams.add(id);
				sqlConnection.updateWithParams("Update todos set done=? where todos.id=?", sqlParams, h -> {
					if (h.succeeded()) {
						if (h.result().getUpdated() > 0)
							result.handle(Future.succeededFuture("successfully changed status of task: " + id + "\nNew value is: " + newStatus));
						else
							result.handle(Future.failedFuture("couldn't update task: " + id + "...Wrong id ?"));

					} else {
						result.handle(Future.failedFuture(h.cause().getMessage()));
					}
					sqlConnection.close();
				});
			}
		});
	}

	public void save(Todo todo, Handler<AsyncResult<JsonObject>> result) {
		jdbcClient.getConnection(r -> {
			if (r.succeeded()) {

				SQLConnection sqlConnection = r.result();
				JsonArray sqlParams = new JsonArray();
				sqlParams.add(todo.getAction());
				sqlParams.add(todo.getDone());
				sqlConnection.updateWithParams("Insert Into todos (action, done) Values (?,?)", sqlParams, h -> {
					if(h.succeeded()) {
						Integer newId = h.result().getKeys().getInteger(0);
						todo.setId(newId);
						JsonObject jsonRes = new JsonObject().put("action", todo.getAction()).put("done", todo.getDone()).put("id", todo.getId());
						result.handle(Future.succeededFuture(jsonRes));
					}else {
						result.handle(Future.failedFuture(h.cause().getMessage()));
					}
					sqlConnection.close();
				});
			}
		});
	}

	@Override
	public void start(Future<Void> future) {
		future.complete();
	}

	@Override
	public void stop(Future<Void> future) {
		future.complete();
	}
}
