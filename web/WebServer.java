package web;

import algorithms.AlgorithmBenchmarkRunner;
import algorithms.PatternMatcher;
import auth.AuthService;
import engine.FraudRiskEngine;
import model.AlgorithmBenchmarkResult;
import model.RiskAssessment;
import model.Transaction;
import model.User;
import storage.CsvFileStorageServiceImpl;
import storage.FileStorageService;
import util.MyArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebServer {

    private static FileStorageService storageService;
    private static AuthService authService;
    private static FraudRiskEngine riskEngine;
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        storageService = new CsvFileStorageServiceImpl();
        authService = new AuthService(storageService);
        riskEngine = new FraudRiskEngine(storageService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/transactions", new TransactionsHandler());
        server.createContext("/api/simulate", new SimulateHandler());
        server.createContext("/api/execute", new ExecuteHandler());
        server.createContext("/api/benchmark", new BenchmarkHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("==========================================================");
        System.out.println("   FINANCIAL FRAUD DETECTION SYSTEM - WEB SERVER RUNNING  ");
        System.out.println("==========================================================");
        System.out.println("   Dashboard URL: http://localhost:" + PORT);
        System.out.println("   Press Ctrl + C in terminal to stop the server");
        System.out.println("==========================================================");
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                String notFound = "404 Not Found";
                sendResponse(exchange, 404, notFound, "text/plain");
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                contentType = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript; charset=UTF-8";
            } else if (path.endsWith(".json")) {
                contentType = "application/json; charset=UTF-8";
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            MyArrayList<User> users = storageService.loadUsers();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (i > 0) sb.append(",");
                sb.append(userToJson(u));
            }
            sb.append("]");
            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            String username = getJsonStringField(body, "username");
            String password = getJsonStringField(body, "password");

            User user = authService.login(username, password);
            if (user != null) {
                sendResponse(exchange, 200, userToJson(user), "application/json");
            } else {
                sendResponse(exchange, 401, "{\"error\":\"Invalid username or password\"}", "application/json");
            }
        }
    }

    private static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            String username = getJsonStringField(body, "username");
            String password = getJsonStringField(body, "password");
            double balance = getJsonDoubleField(body, "balance", 1000.0);

            if (username.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Username and password required\"}", "application/json");
                return;
            }

            boolean success = authService.register(username, password, balance);
            if (success) {
                User created = storageService.findUserByUsername(username);
                sendResponse(exchange, 200, userToJson(created), "application/json");
            } else {
                sendResponse(exchange, 409, "{\"error\":\"Username already exists\"}", "application/json");
            }
        }
    }

    private static class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String userId = "";
            if (query != null && query.contains("userId=")) {
                String[] parts = query.split("=");
                if (parts.length > 1) {
                    userId = parts[1];
                }
            }

            MyArrayList<Transaction> txs = storageService.loadTransactions();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (int i = 0; i < txs.size(); i++) {
                Transaction tx = txs.get(i);
                if (userId.isEmpty() || tx.getSenderId().equalsIgnoreCase(userId) || tx.getReceiverId().equalsIgnoreCase(userId)) {
                    if (!first) sb.append(",");
                    sb.append(transactionToJson(tx));
                    first = false;
                }
            }
            sb.append("]");
            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private static class SimulateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            String senderId = getJsonStringField(body, "senderId");
            String receiverId = getJsonStringField(body, "receiverId");
            double amount = getJsonDoubleField(body, "amount", 0.0);
            String memo = getJsonStringField(body, "memo");
            int algorithmIndex = getJsonIntField(body, "algorithmIndex", 0);

            User sender = storageService.findUserById(senderId);
            User receiver = storageService.findUserById(receiverId);

            if (sender == null || receiver == null) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid sender or receiver ID\"}", "application/json");
                return;
            }

            if (sender.getBalance() < amount) {
                sendResponse(exchange, 400, "{\"error\":\"Insufficient balance\"}", "application/json");
                return;
            }

            MyArrayList<PatternMatcher> matchers = AlgorithmBenchmarkRunner.getMatchers();
            if (algorithmIndex < 0 || algorithmIndex >= matchers.size()) {
                algorithmIndex = 0;
            }
            PatternMatcher matcher = matchers.get(algorithmIndex);

            int txCount = storageService.loadTransactions().size() + 1001;
            String txId = "TX" + txCount;

            Transaction draft = new Transaction(
                    txId,
                    sender.getUserId(),
                    receiver.getUserId(),
                    amount,
                    memo,
                    LocalDateTime.now(),
                    0.0,
                    "PENDING"
            );

            RiskAssessment assessment = riskEngine.assessTransaction(draft, matcher);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"txId\":\"").append(escapeJson(txId)).append("\",");
            sb.append("\"finalScore\":").append(assessment.getFinalScore()).append(",");
            sb.append("\"riskTier\":\"").append(escapeJson(assessment.getRiskTier())).append("\",");
            sb.append("\"isBlocked\":").append(assessment.isBlocked()).append(",");
            sb.append("\"algorithmUsed\":\"").append(matcher.getClass().getSimpleName().replace("PatternMatcher", "")).append("\",");
            sb.append("\"factors\":[");
            MyArrayList<String> factors = assessment.getRiskFactorsList();
            for (int i = 0; i < factors.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(factors.get(i))).append("\"");
            }
            sb.append("]}");

            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private static class ExecuteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            String txId = getJsonStringField(body, "txId");
            String senderId = getJsonStringField(body, "senderId");
            String receiverId = getJsonStringField(body, "receiverId");
            double amount = getJsonDoubleField(body, "amount", 0.0);
            String memo = getJsonStringField(body, "memo");
            double score = getJsonDoubleField(body, "score", 0.0);
            String status = getJsonStringField(body, "status");

            User sender = storageService.findUserById(senderId);
            User receiver = storageService.findUserById(receiverId);

            if (sender == null || receiver == null) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid sender or receiver\"}", "application/json");
                return;
            }

            if ("APPROVED".equalsIgnoreCase(status)) {
                if (sender.getBalance() < amount) {
                    sendResponse(exchange, 400, "{\"error\":\"Insufficient balance\"}", "application/json");
                    return;
                }
                sender.setBalance(sender.getBalance() - amount);
                receiver.setBalance(receiver.getBalance() + amount);

                MyArrayList<User> users = storageService.loadUsers();
                for (int i = 0; i < users.size(); i++) {
                    User u = users.get(i);
                    if (u.getUserId().equalsIgnoreCase(sender.getUserId())) {
                        u.setBalance(sender.getBalance());
                    } else if (u.getUserId().equalsIgnoreCase(receiver.getUserId())) {
                        u.setBalance(receiver.getBalance());
                    }
                }
                storageService.saveUsers(users);
            }

            Transaction tx = new Transaction(
                    txId,
                    sender.getUserId(),
                    receiver.getUserId(),
                    amount,
                    memo,
                    LocalDateTime.now(),
                    score,
                    status
            );
            storageService.addTransaction(tx);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            sb.append("\"txId\":\"").append(escapeJson(txId)).append("\",");
            sb.append("\"status\":\"").append(escapeJson(status)).append("\",");
            sb.append("\"newSenderBalance\":").append(sender.getBalance());
            sb.append("}");

            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private static class BenchmarkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            String text = getJsonStringField(body, "text");
            String pattern = getJsonStringField(body, "pattern");

            if (text.isEmpty()) {
                text = "The suspect was caught conducting illegal money laundering activities. Investigators found evidence "
                     + "of a tax evasion scheme using an offshore bank account, a shell company, and a mule account to run "
                     + "a phishing scan and wire transfer scam.";
            }
            if (pattern.isEmpty()) {
                pattern = "scam";
            }

            MyArrayList<AlgorithmBenchmarkResult> results = AlgorithmBenchmarkRunner.runAll(text, pattern);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"textLength\":").append(text.length()).append(",");
            sb.append("\"patternLength\":").append(pattern.length()).append(",");
            sb.append("\"pattern\":\"").append(escapeJson(pattern)).append("\",");
            sb.append("\"results\":[");

            for (int i = 0; i < results.size(); i++) {
                AlgorithmBenchmarkResult res = results.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"algorithmName\":\"").append(escapeJson(res.getAlgorithmName())).append("\",");
                sb.append("\"matchesFound\":").append(res.getMatchesFound()).append(",");
                sb.append("\"comparisonsCount\":").append(res.getComparisonsCount()).append(",");
                sb.append("\"executionTimeNano\":").append(res.getExecutionTimeNano()).append(",");
                sb.append("\"timeComplexity\":\"").append(escapeJson(res.getTimeComplexity())).append("\",");
                sb.append("\"spaceComplexity\":\"").append(escapeJson(res.getSpaceComplexity())).append("\"");
                sb.append("}");
            }
            sb.append("]}");

            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private static String userToJson(User u) {
        boolean banned = riskEngine.isAccountBanned(u.getUserId());
        double avgRisk = riskEngine.getAccountRiskScore(u.getUserId());
        return "{" +
                "\"userId\":\"" + escapeJson(u.getUserId()) + "\"," +
                "\"username\":\"" + escapeJson(u.getUsername()) + "\"," +
                "\"balance\":" + u.getBalance() + "," +
                "\"isBanned\":" + banned + "," +
                "\"accountStatus\":\"" + (banned ? "BANNED" : "ACTIVE") + "\"," +
                "\"accountRiskScore\":" + (Math.round(avgRisk * 10.0) / 10.0) + "," +
                "\"createdAt\":\"" + (u.getCreatedAt() != null ? u.getCreatedAt().toString() : "") + "\"" +
                "}";
    }

    private static String transactionToJson(Transaction tx) {
        User sender = storageService.findUserById(tx.getSenderId());
        User receiver = storageService.findUserById(tx.getReceiverId());
        String sName = sender != null ? sender.getUsername() : tx.getSenderId();
        String rName = receiver != null ? receiver.getUsername() : tx.getReceiverId();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = tx.getTimestamp() != null ? tx.getTimestamp().format(dtf) : "";

        return "{" +
                "\"txId\":\"" + escapeJson(tx.getTxId()) + "\"," +
                "\"senderId\":\"" + escapeJson(tx.getSenderId()) + "\"," +
                "\"senderName\":\"" + escapeJson(sName) + "\"," +
                "\"receiverId\":\"" + escapeJson(tx.getReceiverId()) + "\"," +
                "\"receiverName\":\"" + escapeJson(rName) + "\"," +
                "\"amount\":" + tx.getAmount() + "," +
                "\"memo\":\"" + escapeJson(tx.getMemo()) + "\"," +
                "\"timestamp\":\"" + escapeJson(formattedTime) + "\"," +
                "\"riskScore\":" + tx.getRiskScore() + "," +
                "\"status\":\"" + escapeJson(tx.getStatus()) + "\"" +
                "}";
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendOptions(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String getJsonStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return "";
        int colonIdx = json.indexOf(":", idx + key.length());
        if (colonIdx == -1) return "";
        int quoteStart = json.indexOf("\"", colonIdx + 1);
        if (quoteStart == -1) return "";
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return "";
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private static double getJsonDoubleField(String json, String field, double defVal) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return defVal;
        int colonIdx = json.indexOf(":", idx + key.length());
        if (colonIdx == -1) return defVal;
        int end = colonIdx + 1;
        while (end < json.length() && (Character.isWhitespace(json.charAt(end)) || json.charAt(end) == '"')) {
            end++;
        }
        int start = end;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return defVal;
        }
    }

    private static int getJsonIntField(String json, String field, int defVal) {
        return (int) getJsonDoubleField(json, field, defVal);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\b') sb.append("\\b");
            else if (c == '\f') sb.append("\\f");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else if (c < 32) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }
}
