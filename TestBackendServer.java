import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.*;

public class TestBackendServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Test connection endpoint
        server.createContext("/api/test", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                // Add CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                    return;
                }
                
                String response = "{\"code\":200,\"message\":\"Success\",\"data\":\"User service is running!\",\"timestamp\":" + System.currentTimeMillis() + "}";
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            }
        });
        
        // Login endpoint
        server.createContext("/api/auth/login", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                // Add CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                    return;
                }
                
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Read request body
                    InputStream is = exchange.getRequestBody();
                    String requestBody = new String(is.readAllBytes(), "UTF-8");
                    
                    String response;
                    if (requestBody.contains("\"username\":\"admin\"") && requestBody.contains("\"password\":\"password\"")) {
                        response = "{\"code\":200,\"message\":\"Success\",\"data\":{\"token\":\"test-jwt-token-12345\",\"user\":{\"username\":\"admin\",\"realName\":\"Administrator\",\"role\":\"ARCHITECT\"}},\"timestamp\":" + System.currentTimeMillis() + "}";
                    } else {
                        response = "{\"code\":500,\"message\":\"Invalid username or password\",\"timestamp\":" + System.currentTimeMillis() + "}";
                    }
                    
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                    exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes("UTF-8"));
                    os.close();
                }
            }
        });
        
        // Logout endpoint
        server.createContext("/api/auth/logout", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                // Add CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                    return;
                }
                
                String response = "{\"code\":200,\"message\":\"Logout successful\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            }
        });
        
        // Get user profile endpoint
        server.createContext("/api/users/profile", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                // Add CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                    return;
                }
                
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                String response;
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    response = "{\"code\":200,\"message\":\"Success\",\"data\":{\"username\":\"admin\",\"realName\":\"Administrator\",\"role\":\"ARCHITECT\"},\"timestamp\":" + System.currentTimeMillis() + "}";
                } else {
                    response = "{\"code\":401,\"message\":\"Unauthorized\",\"timestamp\":" + System.currentTimeMillis() + "}";
                }
                
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes("UTF-8"));
                os.close();
            }
        });
        
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        System.out.println("Test backend server started on port: 8080");
        System.out.println("API base URL: http://localhost:8080/api");
        System.out.println("Available endpoints:");
        System.out.println("  GET  /api/test - Test connection");
        System.out.println("  POST /api/auth/login - User login");
        System.out.println("  POST /api/auth/logout - User logout");
        System.out.println("  GET  /api/users/profile - Get user profile");
        System.out.println("");
        System.out.println("Test credentials: admin / password");
        System.out.println("Please open test-integration.html for frontend-backend integration testing");
        System.out.println("");
        System.out.println("Press Ctrl+C to stop the server");
    }
}