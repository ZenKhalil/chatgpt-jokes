package com.example.chatgptmealplan.dtos;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // This will tell Jackson to ignore any unknown properties in the JSON response
public class ChatCompletionResponse {
  private String id;
  private String object;
  private long created;
  private String model;
  private List<Choice> choices;
  private Usage usage;

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true) // Also ignore unknown properties inside each choice
  public static class Choice {
    private int index;
    private Message message;
    private String finish_reason;
    // private Object logprobs; // Uncomment this if you want to capture the logprobs
  }

  @Getter
  @Setter
  public static class Message {
    private String role;
    private String content;
  }

  @Getter
  @Setter
  public static class Usage {
    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;
  }
}
