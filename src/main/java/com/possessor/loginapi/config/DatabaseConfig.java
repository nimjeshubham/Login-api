package com.possessor.loginapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.possessor.loginapi.repository")
public class DatabaseConfig {
}