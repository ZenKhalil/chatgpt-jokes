package com.example.chatgptmealplan.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data // Lombok annotation to create getters, setters, equals, hash, and toString methods
public class ChatCompletionRequest {

  private String model;
  private double temperature;
  private int max_tokens;
  private double top_p;
  private double frequency_penalty;
  private double presence_penalty;
  private List<Message> messages = new ArrayList<>();

  public ChatCompletionRequest(String model, double temperature, int max_tokens,
                               double top_p, double frequency_penalty, double presence_penalty) {
    this.model = model;
    this.temperature = temperature;
    this.max_tokens = max_tokens;
    this.top_p = top_p;
    this.frequency_penalty = frequency_penalty;
    this.presence_penalty = presence_penalty;
  }

  @Data // Generates all the getters and setters for the fields in the class
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
      this.role = role;
      this.content = content;
    }
  }
}
