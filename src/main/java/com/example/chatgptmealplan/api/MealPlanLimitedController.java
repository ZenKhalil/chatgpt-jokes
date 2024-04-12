package com.example.chatgptmealplan.api;

import com.example.chatgptmealplan.dtos.MyResponse;
import com.example.chatgptmealplan.service.OpenAiService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles fetching personalized meal plans via the ChatGPT API, but is IP-rate limited.
 */
@RestController
@RequestMapping("/api/v1/mealplanlimited")
@CrossOrigin(origins = "*")
public class MealPlanLimitedController {

  @Value("${app.bucket_capacity}")
  private int BUCKET_CAPACITY;

  @Value("${app.refill_amount}")
  private int REFILL_AMOUNT;

  @Value("${app.refill_time}")
  private int REFILL_TIME;

  private OpenAiService service;

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  /**
   * The controller called from the browser client.
   * @param service
   */
  public MealPlanLimitedController(OpenAiService service) {
    this.service = service;
  }

  /**
   * Creates the bucket for handling IP-rate limitations.
   * @return bucket
   */
  private Bucket createNewBucket() {
    Bandwidth limit = Bandwidth.classic(BUCKET_CAPACITY, Refill.greedy(REFILL_AMOUNT, Duration.ofMinutes(REFILL_TIME)));
    return Bucket.builder().addLimit(limit).build();
  }

  /**
   * Returns an existing bucket via key or creates a new one.
   * @param key the IP address
   * @return bucket
   */
  private Bucket getBucket(String key) {
    return buckets.computeIfAbsent(key, k -> createNewBucket());
  }

  /**
   * Handles the request from the browser to generate a meal plan with rate limiting.
   * @param details contains the input that ChatGPT uses to create the meal plan.
   * @param request the current HTTP request used
   * @return the response from ChatGPT.
   */
  @GetMapping()
  public MyResponse getMealPlanLimited(@RequestParam String details, HttpServletRequest request) {

    // Get the IP of the client.
    String ip = request.getRemoteAddr();
    // Get or create the bucket for the given IP/key.
    Bucket bucket = getBucket(ip);
    // Check if the request adheres to the IP-rate limitations.
    if (!bucket.tryConsume(1)) {
      // If not, tell the client "Too many requests".
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, try again later");
    }
    // Otherwise request the meal plan and return the response.
    return service.makeRequest(details, MealPlanController.SYSTEM_MESSAGE);
  }
}
