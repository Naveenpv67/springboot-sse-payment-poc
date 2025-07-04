# Payment SSE Spring Boot POC

This project demonstrates a payment flow using Spring Boot, Server-Sent Events (SSE), and a simulated async callback (like a payment gateway). 

## Features
- Initiate payment (`/pay`)
- Listen for payment status via SSE (`/pay/status/{refId}`)
- Simulate third-party callback (`/pay/callback/{refId}`)
- Simple frontend in `index.html`

## Requirements
- Java 17+
- Maven

## How to Run
1. Build: `mvn clean package`
2. Run: `mvn spring-boot:run`
3. Open [http://localhost:8080/](http://localhost:8080/) in your browser

## API
- `POST /pay` — Initiate payment
- `GET /pay/status/{refId}` — SSE stream for status
- `POST /pay/callback/{refId}` — Simulate callback

## OpenAPI
Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

This is a POC. No real payment processing is performed.
