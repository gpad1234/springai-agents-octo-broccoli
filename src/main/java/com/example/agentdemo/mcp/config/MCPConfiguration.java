package com.example.agentdemo.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPConfiguration {
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
