<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

This is a Spring Boot SSE Payment POC project. Use Java 17, Spring Boot 3.x, and follow the structure and conventions as shown in the workspace. Use Lombok for DTOs. The main flow is: /pay (POST) creates a transaction, /pay/status/{refId} (GET, SSE) streams status, /pay/callback/{refId} (POST) simulates async callback.
