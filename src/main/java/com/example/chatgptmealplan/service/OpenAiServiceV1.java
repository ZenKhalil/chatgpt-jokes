package com.example.chatgptmealplan.service;

import com.example.chatgptmealplan.dtos.ChatCompletionResponse;
import com.example.chatgptmealplan.dtos.MyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
Shows an alternative way to create the request body, using a Map<String,Object> instead of a DTO
Observe the other version provides better error handling. Add this if you want to experiment with this strategy.
 */
@Service
public class OpenAiServiceV1 {


  @Value("${app.api-key}")
  private String API_KEY;

  //See here for a decent explanation of the parameters send to the API via the requestBody
  //https://platform.openai.com/docs/api-reference/completions/create

  @Value("${app.url}")
  public String URL;

  @Value("${app.model}")
  public String MODEL;

  @Value("${app.temperature}")
  public double TEMPERATURE;

  @Value("${app.max_tokens}")
  public int MAX_TOKENS;

  @Value("${app.frequency_penalty}")
  public double FREQUENCY_PENALTY;

  @Value("${app.presence_penalty}")
  public double PRESENCE_PENALTY;

  public MyResponse makeRequest(String userPrompt, String systemMessage) {
    WebClient client = WebClient.create();

    Map<String, Object> body = new HashMap<>();
    body.put("model", MODEL);
    body.put("temperature", TEMPERATURE);
    body.put("max_tokens", MAX_TOKENS);
    body.put("top_p", 1);
    body.put("frequency_penalty", FREQUENCY_PENALTY);
    body.put("presence_penalty", PRESENCE_PENALTY);

    List<Map<String, String>> messages = new ArrayList<>();
    Map<String, String> systemMessageMap = new HashMap<>();
    systemMessageMap.put("role", "system");
    systemMessageMap.put("content", systemMessage);
    messages.add(systemMessageMap);

    Map<String, String> userMessageMap = new HashMap<>();
    userMessageMap.put("role", "user");
    userMessageMap.put("content", userPrompt);
    messages.add(userMessageMap);

    body.put("messages", messages);

    ObjectMapper mapper = new ObjectMapper();
    String json = "";

    try {
      json = mapper.writeValueAsString(body);
      ChatCompletionResponse response = client.post()
              .uri(new URI(URL))
              .header("Authorization", "Bearer " + API_KEY)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .body(BodyInserters.fromValue(json))
              .retrieve()
              .bodyToMono(ChatCompletionResponse.class)
              .block();

      String responseMsg = response.getChoices().get(0).getMessage().getContent();
      return new MyResponse(responseMsg);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing the OpenAI API request.");
    }
  }
}
