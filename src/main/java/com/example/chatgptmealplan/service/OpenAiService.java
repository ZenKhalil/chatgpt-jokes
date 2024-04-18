package com.example.chatgptmealplan.service;

import com.example.chatgptmealplan.dtos.ChatCompletionRequest;
import com.example.chatgptmealplan.dtos.ChatCompletionResponse;
import com.example.chatgptmealplan.dtos.MyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.net.URI;
import java.util.LinkedHashMap;

@Service
public class OpenAiService {
  public static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

  @Value("${app.frequency_penalty}")
  private double frequency_penalty;

  @Value("${app.presence_penalty}")
  private double presence_penalty;

  @Value("${app.api-key}")
  private String apiKey;

  @Value("${app.url}")
  private String apiUrl;

  @Value("${app.model}")
  private String model;

  @Value("${app.temperature}")
  private double temperature;

  @Value("${app.max_tokens}")
  private int maxTokens;

  @Value("${app.frequency_penalty}")
  private double frequencyPenalty;

  @Value("${app.presence_penalty}")
  private double presencePenalty;

  @Value("${app.top_p}")
  private double topP;

  private final WebClient client = WebClient.create();


  public ResponseEntity<Map<String, Object>> getWeeklyMealPlan(String userDetails) {

    logger.info("User details received: {}", userDetails);
    int caloricNeeds = calculateCaloricNeeds(userDetails);
    logger.info("Calculated caloric needs: {}", caloricNeeds);

    Map<String, Object> response = new HashMap<>();
    response.put("caloricNeeds", caloricNeeds);
    List<String> plans = new ArrayList<>();

    String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    for (String day : daysOfWeek) {
      String prompt = String.format("Provide a detailed meal plan for %s that includes calorie counts for a person who needs %d calories daily. Include breakfast, lunch, dinner, and snacks. Do not repeat meals from other days.", day, caloricNeeds);
      logger.info("Generated prompt for {}: {}", day, prompt);
      MyResponse dailyMealPlanResponse = makeRequest(prompt, "Generate a meal plan");

      if (dailyMealPlanResponse != null && dailyMealPlanResponse.getMessage() != null) {
        plans.add(dailyMealPlanResponse.getMessage());
      } else {
        logger.error("Failed to retrieve the meal plan for {}", day);
        return ResponseEntity.internalServerError().body(Map.of("error", "Failed to retrieve the meal plan for " + day));
      }
    }

    response.put("mealPlans", plans);
    return ResponseEntity.ok(response);
  }

  private int calculateCaloricNeeds(String userDetails) {
    Map<String, String> userDetailMap = parseUserDetails(userDetails);
    int age = Integer.parseInt(userDetailMap.get("age"));
    int height = Integer.parseInt(userDetailMap.get("height"));
    int weight = Integer.parseInt(userDetailMap.get("weight"));
    String gender = userDetailMap.get("gender");
    String activityLevel = userDetailMap.get("activityLevel");

    int bmr = calculateBMR(weight, height, age, gender);
    return calculateTotalCalories(bmr, activityLevel);
  }

  private int calculateBMR(int weight, int height, int age, String gender) {
    if ("male".equalsIgnoreCase(gender)) {
      return (int) (10 * weight + 6.25 * height - 5 * age + 5);
    } else {
      return (int) (10 * weight + 6.25 * height - 5 * age - 161);
    }
  }

  private int calculateTotalCalories(int bmr, String activityLevel) {
    switch (activityLevel.toLowerCase()) {
      case "sedentary":
        return (int) (bmr * 1.2);
      case "lightly active":
        return (int) (bmr * 1.375);
      case "moderately active":
        return (int) (bmr * 1.55);
      case "very active":
        return (int) (bmr * 1.725);
      case "extra active":
        return (int) (bmr * 1.9);
      default:
        return bmr;
    }
  }

  private Map<String, String> parseUserDetails(String userDetails) {
    Map<String, String> detailsMap = new LinkedHashMap<>();
    String[] parts = userDetails.split(",");
    for (String part : parts) {
      String[] keyValue = part.split("=");
      if (keyValue.length == 2) {
        detailsMap.put(keyValue[0], keyValue[1]);
      } else {
        logger.error("Invalid user detail format: {}", part);
      }
    }
    return detailsMap;
  }

  public MyResponse makeRequest(String userPrompt, String systemMessage) {
    ChatCompletionRequest requestDto = new ChatCompletionRequest(
            model, temperature, maxTokens, topP, frequency_penalty, presence_penalty);
    requestDto.getMessages().add(new ChatCompletionRequest.Message("system", systemMessage));
    requestDto.getMessages().add(new ChatCompletionRequest.Message("user", userPrompt));

    ObjectMapper mapper = new ObjectMapper();
    String json;
    try {
      json = mapper.writeValueAsString(requestDto);
      logger.info("Request JSON: {}", json);

      ResponseEntity<String> responseEntity = client.post()
              .uri(new URI(apiUrl))
              .header("Authorization", "Bearer " + apiKey)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .body(BodyInserters.fromValue(json))
              .retrieve()
              .toEntity(String.class)
              .block();

      String responseBody = responseEntity.getBody();
      logger.info("API Response Body: {}", responseBody);

      if (responseEntity.getStatusCode().is2xxSuccessful() && responseBody != null) {
        ChatCompletionResponse response = mapper.readValue(responseBody, ChatCompletionResponse.class);
        int tokensUsed = response.getUsage().getTotal_tokens();
        double costPerThousandTokens = 0.0015;  // Adjust as necessary
        double cost = (tokensUsed * costPerThousandTokens) / 1000;

        logger.info("Tokens used for this request: {}", tokensUsed);
        logger.info("Estimated cost for this request: ${}", String.format("%.6f", cost));

        String responseMsg = response.getChoices().get(0).getMessage().getContent();
        return new MyResponse(responseMsg, tokensUsed, cost);
      } else {
        logger.error("Non-successful response status code: {}", responseEntity.getStatusCode());
        logger.error("Response body: {}", responseBody);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing the OpenAI API request: " + responseBody);
      }
    } catch (WebClientResponseException e) {
      logger.error("WebClient response status code: {}", e.getStatusCode());
      logger.error("WebClient response body: {}", e.getResponseBodyAsString(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "WebClient error during the OpenAI API request", e);
    } catch (Exception e) {
      logger.error("Exception during the OpenAI API request", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in processing the OpenAI API request", e);
    }
  }
}