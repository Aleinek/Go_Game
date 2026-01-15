package com.gogame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Go Game server application.
 * <p>
 * This Spring Boot application provides a REST API and WebSocket support
 * for playing the game of Go (Weiqi/Baduk) according to Japanese rules.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}