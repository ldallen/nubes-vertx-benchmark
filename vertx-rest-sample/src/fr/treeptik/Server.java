package fr.treeptik;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.*;
import io.vertx.core.http.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.*;
import io.vertx.ext.jdbc.*;


public class Server extends AbstractVerticle {


    private JDBCClient jdbcClient;
    private TodoService todoService;
    @Override
    public void start() throws Exception {

        JsonObject config = new JsonObject()
                .put("url", "jdbc:mysql://localhost:3306/vertxTodo")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("user","root")
                .put("password","root");


        jdbcClient = JDBCClient.createShared(vertx, config);

        todoService = new TodoService(jdbcClient);

        HttpServerOptions options = new HttpServerOptions().setMaxWebsocketFrameSize(1000000).setPort(8000).setHost("localhost");
        HttpServer server = vertx.createHttpServer(options);

        Router router = Router.router(vertx);

        router.route("/api").handler(BodyHandler.create());
        router.route("/api").handler(CookieHandler.create());
        router.route("/api").handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        router.route("/api").handler(context -> {
            context.setAcceptableContentType("application/json");
            context.response().putHeader("content-type", "application/json");
            context.next();
        });

        router.get("/api/todos").handler(context->{
            todoService.listTasks(handler -> {
                if (handler.succeeded()) {
                    context.response().end(handler.result().encodePrettily());
                }
                else {
                    context.response().end(handler.cause().getMessage());
                }
            });

        });

        router.get("/api/todo/view/:id").handler(context->{

            String id = context.request().getParam("id");
            todoService.getSpecific("id", id, handler -> {
                if (handler.succeeded()) {
                    context.response().end(handler.result().getJsonObject(0).encodePrettily());
                } else {
                    context.response().end(handler.cause().getMessage());
                }
            });

        });

        router.get("/api/todo/done/:done").handler(context->{

            String done = context.request().getParam("done");
            todoService.getSpecific("done", done, handler -> {
                if (handler.succeeded()) {
                    context.response().end(handler.result().encodePrettily());
                } else {
                    context.response().end(handler.cause().getMessage());
                }
            });

        });

        router.get("/api/todo/action/:action").handler(context->{

            String action = context.request().getParam("action");
            todoService.getSpecific("action", action, handler -> {
                if (handler.succeeded()) {
                    context.response().end(handler.result().encodePrettily());
                } else {
                    context.response().end(handler.cause().getMessage());
                }
            });

        });

        router.post("/api/todo").handler(context->{

            context.request().bodyHandler(buff -> {
                JsonObject todo = new JsonObject(buff.toString());
                if (todo == null)
                    context.response().setStatusCode(400).end();
                else {
                    todoService.save(todo, handler -> {
                        if (handler.succeeded()) {
                            context.response().end(handler.result().encodePrettily());
                        } else {
                            context.response().end(handler.cause().getMessage());
                        }
                    });
                }
            });

        });

        router.put("/api/todo/:id").handler(context -> {


            String id = context.request().getParam("id");
            context.request().bodyHandler(buff -> {
                String done = buff.toString();
                if (done == null)
                    context.response().setStatusCode(400).end();
                else {
                    todoService.updateStatus(Integer.parseInt(id), Boolean.parseBoolean(done), handler -> {
                        if (handler.succeeded()) {
                            context.response().end(handler.result());
                        } else {
                            context.response().end(handler.cause().getMessage());
                        }
                    });
                }
            });

            });

            router.delete("/api/todo/:id").handler(context -> {

                String id = context.request().getParam("id");
                todoService.delete(Integer.parseInt(id), handler -> {
                    if (handler.succeeded()) {
                        context.response().end(handler.result());
                    } else {
                        context.response().end(handler.cause().getMessage());
                    }
                });

            });

            server.requestHandler(router::accept).listen(8000);
        }
    }
