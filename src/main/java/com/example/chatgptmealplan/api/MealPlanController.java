package com.example.chatgptmealplan.api;

import com.example.chatgptmealplan.dtos.MyResponse;
import com.example.chatgptmealplan.service.OpenAiService;
import org.springframework.web.bind.annotation.*;

/**
 * This class handles fetching personalized meal plans via the ChatGPT API
 */
@RestController
@RequestMapping("/api/v1/mealplan") // Changed the endpoint to reflect meal planning
@CrossOrigin(origins = "*")
public class MealPlanController {

    private final OpenAiService service;

    /**
     * This contains the message to the ChatGPT API, telling the AI how it should act in regard to the requests it gets for meal plans.
     */
    final static String SYSTEM_MESSAGE = "You are a helpful assistant that focuses on generating personalized and concise meal plans and recipes for a full week, from Monday to Sunday. Users will provide their age, weight, height, workout plan, dietary preferences, and dietary restrictions. Based on this information, your task is to create a detailed yet succinct list of meals for each day, covering breakfast, lunch, dinner, and snacks, each aligned with their caloric needs and dietary requirements. Each meal suggestion should include a list of main ingredients, a brief description of how to prepare the dish, and the total calorie content for each meal. Focus on delivering straightforward, easy-to-follow weekly meal plans that specify portions and preparation steps, ensuring users receive the nutritional support they need for their fitness journey. If the user asks a question that deviates from these topics, remind them to provide their personal information and workout details to offer the most accurate nutritional advice.";

    /**
     * The controller called from the browser client.
     * @param service
     */
    public MealPlanController(OpenAiService service) {
        this.service = service;
    }

    /**
     * Handles the request from the browser client to generate a meal plan.
     * @param details contains the input that ChatGPT uses to create the meal plan.
     * @return the response from ChatGPT.
     */
    @GetMapping
    public MyResponse getMealPlan(@RequestParam String details) {

        return service.makeRequest(details, SYSTEM_MESSAGE);
    }
}
