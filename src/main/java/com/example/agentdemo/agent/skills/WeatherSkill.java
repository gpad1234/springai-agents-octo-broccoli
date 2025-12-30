package com.example.agentdemo.agent.skills;

import com.example.agentdemo.agent.Skill;
import com.example.agentdemo.model.ActionResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Example skill that provides mock weather information.
 * Demonstrates how to create a new skill with:
 * - Pattern matching in canHandle()
 * - Simple business logic
 * - Structured output
 * 
 * Examples:
 * - "weather in Seattle"
 * - "what's the weather like in Paris"
 * - "get weather for Tokyo"
 */
@Component
public class WeatherSkill implements Skill {
    
    private static final Random random = new Random();
    
    // Mock weather data
    private static final String[] CONDITIONS = {
        "Sunny", "Cloudy", "Partly Cloudy", "Rainy", "Stormy", "Snowy", "Foggy"
    };
    
    private static final Map<String, String> CITY_TIMEZONES = new HashMap<>();
    static {
        CITY_TIMEZONES.put("seattle", "PST");
        CITY_TIMEZONES.put("new york", "EST");
        CITY_TIMEZONES.put("london", "GMT");
        CITY_TIMEZONES.put("paris", "CET");
        CITY_TIMEZONES.put("tokyo", "JST");
        CITY_TIMEZONES.put("sydney", "AEST");
    }
    
    @Override
    public boolean canHandle(String goal) {
        if (goal == null) return false;
        String g = goal.toLowerCase();
        return g.contains("weather") || 
               g.contains("temperature") || 
               g.contains("forecast");
    }
    
    @Override
    public ActionResult execute(String goal) {
        try {
            // Extract city name from the goal
            String city = extractCity(goal);
            
            if (city == null || city.isEmpty()) {
                return new ActionResult(false, "WeatherSkill", 
                    "Please specify a city. Example: 'weather in Seattle'");
            }
            
            // Generate mock weather data
            String weather = generateMockWeather(city);
            
            return new ActionResult(true, "WeatherSkill", weather);
            
        } catch (Exception e) {
            return new ActionResult(false, "WeatherSkill", 
                "Error getting weather: " + e.getMessage());
        }
    }
    
    /**
     * Extract city name from goal text
     */
    private String extractCity(String goal) {
        String lower = goal.toLowerCase();
        
        // Try common patterns
        String[] patterns = {"in ", "for ", "at "};
        for (String pattern : patterns) {
            int idx = lower.indexOf(pattern);
            if (idx != -1) {
                String rest = goal.substring(idx + pattern.length()).trim();
                // Take first word/phrase (stop at punctuation)
                return rest.split("[,?.!]")[0].trim();
            }
        }
        
        // If no pattern found, try to find a known city
        for (String city : CITY_TIMEZONES.keySet()) {
            if (lower.contains(city)) {
                return capitalize(city);
            }
        }
        
        return null;
    }
    
    /**
     * Generate mock weather information
     */
    private String generateMockWeather(String city) {
        String condition = CONDITIONS[random.nextInt(CONDITIONS.length)];
        int temperature = 32 + random.nextInt(60); // 32-92°F
        int humidity = 30 + random.nextInt(60); // 30-90%
        int windSpeed = 5 + random.nextInt(25); // 5-30 mph
        
        String timezone = CITY_TIMEZONES.getOrDefault(city.toLowerCase(), "Local");
        
        return String.format(
            "🌤️ Weather for %s:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Condition: %s\n" +
            "Temperature: %d°F (%d°C)\n" +
            "Humidity: %d%%\n" +
            "Wind Speed: %d mph\n" +
            "Timezone: %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⚠️  Note: This is mock data for demonstration",
            city,
            condition,
            temperature,
            fahrenheitToCelsius(temperature),
            humidity,
            windSpeed,
            timezone
        );
    }
    
    private int fahrenheitToCelsius(int fahrenheit) {
        return (int) Math.round((fahrenheit - 32) * 5.0 / 9.0);
    }
    
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
