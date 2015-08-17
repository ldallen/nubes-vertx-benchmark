package fr.treeptik.controllers;

import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.routing.http.DELETE;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.PUT;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import fr.treeptik.domains.Todo;
import fr.treeptik.services.TodoService;


@Controller("/api")
@ContentType("application/json")
public class TodoController {

	@Service("todoService")
	private TodoService todoService;

	@GET("/todos")
	public void getAll(RoutingContext context, Payload<JsonArray> result) {

		todoService.getAll(handler->{
			if (handler.succeeded()) {
				result.set(new JsonArray(handler.result()));
				context.next();
			}
			else {
				context.response().end(handler.cause().getMessage());
			}
		});

	}

	@GET("/todo/view/:id")
	public void getWithId(@Param("id") Integer id, RoutingContext context, Payload<JsonArray> result){

		todoService.getSpecific("id", Integer.toString(id), handler -> {
			if (handler.succeeded()) {
				result.set(new JsonArray(handler.result()));
				context.next();
			}
			else {
				context.response().end(handler.cause().getMessage());
			}
		});
	}
	@GET("/todo/done/:done")
	public void getStatus(@Param("done") String done, RoutingContext context, Payload<JsonArray> result){


		todoService.getSpecific("done",done, handler -> {
			if (handler.succeeded()) {
				result.set(new JsonArray(handler.result()));
				context.next();
			}
			else {
				context.response().end(handler.cause().getMessage());
			}
		});
	}

	@GET("/todo/action/:action")
	public void getWithAction(@Param("action") String action, RoutingContext context, Payload<JsonArray> result){


		todoService.getSpecific("action", action, handler -> {
			if (handler.succeeded()) {
				result.set(new JsonArray(handler.result()));
				context.next();
			}
			else {
				context.response().end(handler.cause().getMessage());
			}
		});
	}


	@POST("/todo")
	public void addTodo(@RequestBody Todo todo, RoutingContext context, Payload<JsonObject> result){


		todoService.save(todo, handler -> {
			if (handler.succeeded()){
				result.set(handler.result());
				context.next();
			}
			else {
				context.response().end(handler.cause().getMessage());
			}
		});
	}

	@PUT("/todo/:action")
	public void updateStatus(@Param("action") String action, @RequestBody JsonObject status, RoutingContext context, Payload<String> result) {


		todoService.updateStatus(action, status.getBoolean("done"), handler -> {
			if (handler.succeeded()) {
				result.set("update succeeded!");
				context.next();
			} else {
				context.response().end(handler.cause().getMessage());
			}
		});
	}

	@DELETE("/todo/:action")
	public void deleteTodo(@Param("action") String action, RoutingContext context, Payload<String> result ){
		todoService.delete(action, handler -> {
			if (handler.succeeded()){
				result.set("delete succeeded!");
				context.next();
			}
			else {
				context.response().end(handler.cause().getMessage());
			}
		});
	}

}
