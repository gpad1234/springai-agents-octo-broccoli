package com.example.agentdemo;

import com.example.agentdemo.agent.AgentService;
import com.example.agentdemo.model.ActionResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class AgentDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentDemoApplication.class, args);
    }

    // @Bean
    // public CommandLineRunner interactiveChatRunner(ApplicationContext context) {
    //     return args -> {
    //         // Check if --chat flag is present
    //         boolean chatMode = false;
    //         for (String arg : args) {
    //             if ("--chat".equals(arg) || "-c".equals(arg)) {
    //                 chatMode = true;
    //                 break;
    //             }
    //         }

    //         if (chatMode) {
    //             AgentService agentService = context.getBean(AgentService.class);
    //             runInteractiveChat(agentService);
    //         }
    //     };
    // }

    private void runInteractiveChat(AgentService agentService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n========================================");
        System.out.println("  Spring AI Agent - Interactive Chat");
        System.out.println("========================================");
        System.out.println("Available skills: Calculator, Search, Summarize, Osquery");
        System.out.println("Type 'exit' or 'quit' to stop");
        System.out.println("========================================\n");

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                System.out.println("\nGoodbye!");
                break;
            }

            try {
                List<ActionResult> results = agentService.executeGoal(input);
                
                System.out.println("\nAgent:");
                for (ActionResult result : results) {
                    if (result.isSuccess()) {
                        System.out.println("  [" + result.getSkillName() + "] " + result.getOutput());
                    } else {
                        System.out.println("  [ERROR] " + result.getOutput());
                    }
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println("\n  [ERROR] " + e.getMessage() + "\n");
            }
        }
    }
}
