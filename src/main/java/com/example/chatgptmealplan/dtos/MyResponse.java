package com.example.chatgptmealplan.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyResponse {
  private String answer;
  private int tokensUsed;
  @Getter
  @Setter
  private double cost;
  private List<Map<String, String>> messages;

  public MyResponse(String answer, int tokensUsed, double cost) {
    this.answer = answer;
    this.tokensUsed = tokensUsed;
    this.cost = cost;
  }

  public MyResponse(String answer, List<Map<String, String>> messages) {
    this.answer = answer;
    this.messages = messages;
  }

  public MyResponse(String responseMsg) {
  }

  // Existing getters and setters
  public String getMessage() {
    return answer;
  }

  public void setMessage(String answer) {
    this.answer = answer;
  }
}

