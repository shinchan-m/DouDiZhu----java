package com.itheima.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itheima.util.ApiKeyStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 官方 OpenAI 兼容接口客户端。
 */
public class DeepSeekClient {
    private static final String BASE_URL = "https://api.deepseek.com";
    private static final String MODEL = "deepseek-flash";
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final String apiKey;

    public DeepSeekClient() {
        this.apiKey = ApiKeyStore.load();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String request(List<Map<String, Object>> messages) {
        if (!isConfigured()) {
            throw new IllegalStateException("未配置 DEEPSEEK_API_KEY");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.add("messages", GSON.toJsonTree(messages));
        body.add("response_format", jsonObject("type", "json_object"));
        body.add("thinking", jsonObject("type", "disabled"));
        body.addProperty("temperature", 0.2);
        body.addProperty("max_tokens", 128);
        body.addProperty("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/chat/completions"))
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body),
                        StandardCharsets.UTF_8))
                .build();

        IOException lastIoError = null;
        // 网络层最多重试两次，语义修复由 AIPlayer 单独控制。
        for (int attempt = 0; attempt <= 2; attempt++) {
            try {
                HttpResponse<String> response = HTTP_CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return extractContent(response.body());
                }
                if (status != 429 && status < 500) {
                    throw new IllegalStateException("DeepSeek 请求失败，HTTP " + status);
                }
            } catch (IOException e) {
                lastIoError = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("DeepSeek 请求被中断", e);
            }
            if (attempt < 2) {
                sleep((attempt + 1L) * 1000L);
            }
        }
        throw new IllegalStateException("DeepSeek 网络请求失败", lastIoError);
    }

    private String extractContent(String responseBody) {
        JsonElement root = JsonParser.parseString(responseBody);
        if (!root.isJsonObject()) {
            throw new IllegalStateException("DeepSeek 响应格式错误");
        }
        JsonArray choices = root.getAsJsonObject().getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("DeepSeek 响应缺少 choices");
        }
        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            throw new IllegalStateException("DeepSeek 响应缺少 message.content");
        }
        return message.get("content").getAsString();
    }

    private JsonObject jsonObject(String key, String value) {
        JsonObject object = new JsonObject();
        object.addProperty(key, value);
        return object;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
