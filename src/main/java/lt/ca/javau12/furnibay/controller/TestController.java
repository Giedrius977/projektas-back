package lt.ca.javau12.furnibay.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "Simple endpoints for API testing")
public class TestController {
    
    @Operation(summary = "Basic health check")
    @GetMapping
    public String testEndpoint() {
        return "API is working";
    }
}