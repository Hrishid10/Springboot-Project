package com.example.webhooksql.service;

import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void startProcess() {
        try {
            // Step 1: Generate webhook
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                  "name": "John Doe",
                  "regNo": "REG12347",
                  "email": "john@example.com"
                }
                """;

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(generateUrl, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String webhook = (String) response.getBody().get("webhook");
                String accessToken = (String) response.getBody().get("accessToken");

                System.out.println("Webhook URL: " + webhook);
                System.out.println("Access Token: " + accessToken);

                if (webhook == null || accessToken == null) {
                    System.err.println("Webhook or accessToken missing");
                    return;
                }

                // Step 2: Your final SQL query
                String finalSqlQuery = """
                    SELECT
                        p.AMOUNT AS SALARY,
                        CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                        FLOOR(DATEDIFF(CURRENT_DATE, e.DOB) / 365.25) AS AGE,
                        d.DEPARTMENT_NAME
                    FROM PAYMENTS p
                    JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                    WHERE DAY(p.PAYMENT_TIME) <> 1
                      AND p.AMOUNT = (
                        SELECT MAX(AMOUNT)
                        FROM PAYMENTS
                        WHERE DAY(PAYMENT_TIME) <> 1
                    )
                    ;
                    """;

                // Step 3: Submit solution
                submitSolution(webhook, accessToken, finalSqlQuery);

            } else {
                System.err.println("Failed to generate webhook. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitSolution(String webhook, String token, String sqlQuery) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            String body = String.format("""
                {
                  "finalQuery": "%s"
                }
                """, sqlQuery.replace("\"", "\\\"").replace("\n", " "));

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(webhook, HttpMethod.POST, request, String.class);

            System.out.println("Submission Status: " + response.getStatusCode());
            System.out.println("Submission Response: " + response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
