package fr.treeptik;

import io.vertx.core.*;

public class Loader{

    public static void main(String[] args) {

        //Create Vertx instance to load 10 instances of the Server
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle("fr.treeptik.Server");
    }
}
