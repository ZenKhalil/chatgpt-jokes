package com.example.chatgptmealplan.api;

import com.example.chatgptmealplan.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.details;

@RestController
@RequestMapping("/api/v1/mealplan")
@CrossOrigin(origins = "*")
public class MealPlanController {

    public static final String SYSTEM_MESSAGE = "";
    private final OpenAiService service;

    @Autowired
    public MealPlanController(OpenAiService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMealPlan(@RequestParam String details) {
        return service.getWeeklyMealPlan(details);
    }
}