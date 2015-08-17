package fr.treeptik.services;


import fr.treeptik.domains.Todo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import com.github.aesteve.vertx.nubes.services.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoService implements Service{

	private MongoClient mongoClient;
	private JsonObject confMongo;
	
	@Override
	public void init(Vertx vertx) {
		mongoClient = MongoClient.createShared(vertx, confMongo);
	}

	public TodoService(JsonObject confMongo) {
		this.confMongo = confMongo;
	}

	public void getAll(Handler<AsyncResult<List<JsonObject>>> result){

		mongoClient.find("todo",new JsonObject(), result);
	}

	public void getSpecific(String type, String value, Handler<AsyncResult<List<JsonObject>>> result) {

		JsonObject query = new JsonObject();

		switch (type){
			case "id":{
				query.put(type,Integer.parseInt(value));
				break;
			}
			case "action":{
				query.put(type, value);
				break;
			}
			case "done": {
				query.put(type, Boolean.parseBoolean(value));
				break;
			}
			default:{
				result.handle(Future.failedFuture("couldn't find the attribute: "+ type));
			}
		}

		mongoClient.find("todo", query, result);
	}

	public void delete(String action, Handler<AsyncResult<Void>> result) {

		mongoClient.find("todo",new JsonObject().put("action",action),h-> {
			if (h.succeeded()) {
				String id = h.result().get(0).getString("_id");
				JsonObject query = new JsonObject();
				query.put("_id", id);
				mongoClient.remove("todo", query, result);
			}
		});

	}

	public void updateStatus(String action, Boolean newStatus, Handler<AsyncResult<Void>> result) {

		mongoClient.find("todo",new JsonObject().put("action",action),h->{
			if (h.succeeded()){
				String id = h.result().get(0).getString("_id");
				JsonObject query = new JsonObject();
				JsonObject setTodo = new JsonObject();
				query.put("_id",id);
				setTodo.put("$set",new JsonObject().put("done",newStatus));
				mongoClient.update("todo",query,setTodo,result);
			}
		});


	}

	public void save(Todo todo, Handler<AsyncResult<JsonObject>> result) {
		JsonObject newTodo = new JsonObject();

		newTodo.put("action",todo.getAction());
		newTodo.put("done",todo.getDone());
		mongoClient.insert("todo",newTodo,res -> {
			if (res.failed()) {
				result.handle(Future.failedFuture(res.cause()));
				return;
			}
			newTodo.put("_id", res.result());
			result.handle(Future.succeededFuture(newTodo));
		});
	}

	@Override
	public void start(Future<Void> future) {
		future.complete();
	}

	@Override
	public void stop(Future<Void> future) {
		mongoClient.close();
		future.complete();
	}
}
