package hu.sajatnev.projekt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VevokezeloApplication {

    public static void main(String[] args) {
        // Ez az egyetlen sor indítja el a beágyazott Tomcat szervert és a teljes Spring Boot keretrendszert
        SpringApplication.run(VevokezeloApplication.class, args);
    }
}
