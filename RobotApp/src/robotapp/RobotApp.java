/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hiber
 */
public class RobotApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/command", new CommandsHandler());
            server.createContext("/static", new StaticHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException ex) {
            System.out.println("Start web server error: " + ex.getMessage());
        }
    }

    static class CommandsHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {
            try {
                String response = "";
                String query = t.getRequestURI().getQuery();
                Map<String, String> params = queryToMap(query);
                if (params.containsKey("cmd")) {
                    try {
                        String angle = "0";
                        String distance = "";
                        if (params.containsKey("angle"))
                            angle = params.get("angle");
                        if (params.containsKey("distance"))
                            angle = params.get("distance");
                        response = RaspCommander.doCommand(params.get("cmd"), angle, distance);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RobotApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Headers h = t.getResponseHeaders();
                h.add("Content-Type", "application/json");
                byte[] rbytes = response.getBytes("utf-8");
                t.sendResponseHeaders(200, rbytes.length);
                OutputStream os = t.getResponseBody();
                os.write(rbytes);
                os.close();
            } catch (Exception ex) {
                System.out.println("Start web server error: " + ex.getMessage());
            }

        }

        public Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                } else {
                    result.put(pair[0], "");
                }
            }
            return result;
        }
    }

    static class StaticHandler implements HttpHandler {

        private static String getContentType(String fileExt) {
            Map<String, String> contentTypes = new HashMap<>();
            contentTypes.put("html", "text/html; charset=UTF-8");
            contentTypes.put("htm", "text/html; charset=UTF-8");
            contentTypes.put("js", "application/javascript");
            contentTypes.put("css", "text/css");
            contentTypes.put("jpg", "image/jpeg");
            contentTypes.put("jpeg", "image/jpeg");

            if (contentTypes.containsKey(fileExt)) {
                return contentTypes.get(fileExt);
            } else {
                return "text/plain";
            }
        }

        public void handle(HttpExchange t) throws IOException {
            try {
                String fileName = (new StringBuilder(t.getRequestURI().getPath())).deleteCharAt(0).toString();
                File file = new File(fileName);
                System.out.println("File request " + fileName);
                if (file.exists()) {
                    String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                    Headers h = t.getResponseHeaders();
                    if (tokens.length >= 2) {
                        h.add("Content-Type", StaticHandler.getContentType(tokens[1]));
                    } else {
                        h.add("Content-Type", "");
                    }
                    byte[] bytearray = new byte[(int) file.length()];
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(bytearray, 0, bytearray.length);

                    t.sendResponseHeaders(200, file.length());
                    OutputStream os = t.getResponseBody();
                    os.write(bytearray, 0, bytearray.length);
                    os.close();
                } else {
                    String response = "404 (Not Found)\n";
                    t.sendResponseHeaders(404, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } catch (Exception ex) {
                System.out.println("Start web server error: " + ex.getMessage());
            }
        }
    }

}
