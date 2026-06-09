package com.passaaqui.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Seed desativado — usar create-admin.sql manualmente
    }
}
