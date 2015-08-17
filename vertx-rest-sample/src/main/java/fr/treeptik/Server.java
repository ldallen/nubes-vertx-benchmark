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

        JsonObject config = context.config();
        JsonObject confJdbc;
        HttpServerOptions options;

        JsonObject defaultJdbcConf = new JsonObject()
                .put("url", "jdbc:mysql://localhost:3306/vertxTodo")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("user", "root")
                .put("password", "root")
                .put("initial_pool_size", 50)
                .put("max_pool_size", 1000);
        if (config == null) {
            confJdbc = defaultJdbcConf;
            options = new HttpServerOptions().setMaxWebsocketFrameSize(1000000).setPort(8090).setHost("localhost");

        }
        else {

            confJdbc = config.getJsonObject("jdbc", defaultJdbcConf);

            options = new HttpServerOptions().setMaxWebsocketFrameSize(1000000).setPort(8090).setHost(config.getString("host","localhost"));

        }


        jdbcClient = JDBCClient.createShared(vertx, confJdbc);

        todoService = new TodoService(jdbcClient);
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

        router.get("/api/todo/view/:id").handler(context-> {

            String id = context.request().getParam("id");
            todoService.getSpecific("id", id, handler -> {
                if (handler.succeeded()) {
                    context.response().end(handler.result().encodePrettily());
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
                JsonObject done = new JsonObject(buff.toString());
                if (done == null)
                    context.response().setStatusCode(400).end();
                else {
                    todoService.updateStatus(Integer.parseInt(id), done.getBoolean("done"), handler -> {
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

        server.requestHandler(router::accept).listen(8090);
    }
}
