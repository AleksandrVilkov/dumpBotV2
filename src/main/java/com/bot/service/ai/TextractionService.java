package com.bot.service.ai;

import com.bot.config.AIConfig;
import com.bot.model.ai.AIRequest;
import com.bot.model.ai.AIResponse;
import com.bot.model.ai.AIResult;
import com.bot.model.ai.RequestParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


//https://rapidapi.com/TextractionAI/api/ai-textraction/

@Service
public class TextractionService implements AIService {
    @Autowired
    AIConfig aiConfig;

    public AIResult parseText(String text) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(aiConfig.getUri()))
                .header("content-type", aiConfig.getContentType())
                .header("X-RapidAPI-Key", aiConfig.getRapidAPIKey())
                .header("X-RapidAPI-Host", aiConfig.getRapidAPIHost())
                .method("POST", HttpRequest.BodyPublishers.ofString(createBody(text))).build();


        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return AIResponse.fromJson(response.body()).getResults();
    }

    private String createBody(String text) {
        var request = AIRequest.builder().
                text(text).
                entities(List.of(
                        RequestParameter.builder().varName("price").type("string")
                                .description("the cost of the product, only price in numbers, no letters").build(),
                        RequestParameter.builder().varName("description").type("string")
                                .description("Description of the product they want to sell or find").build(),
                        RequestParameter.builder().varName("cars").type("array[string]")
                                .description("The vehicle or vehicles to which the request relates").build(),
                        RequestParameter.builder().varName("type").type("string")
                                .description("Request type: Buy or sell").build()
                )).
                build();
        return request.toJsonFormat();
    }
}
