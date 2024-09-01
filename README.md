# Endpoints Monitoring Service

A Spring Boot REST API microservice for monitoring HTTP/HTTPS URLs. The service allows users to create, update, delete, and monitor endpoints. The service logs status codes and payloads returned from the monitored URLs.

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.0+

## Setup and Run

1. **Clone the repository:**

   ```bash
   git clone <your-repo-url>
   cd code-assessment
   ```

2. **Configure the database:**
   Update the application.properties file in src/main/resources/ with your MySQL database settings.

3. **Build and Run**

## Available API Endpoints

- **User Registration:**

  - `POST /monitoring-app/users/register`
  - **Set:** `username`, `email`

- **Get User Details:**

  - `GET /monitoring-app/users/me`

- **Create a Monitored Endpoint:**

  - `POST /monitoring-app/endpoints`
  - **Set:** `name`, `url`, `monitoredInterval`

- **Get All Monitored Endpoints:**

  - `GET /monitoring-app/endpoints`

- **Update a Monitored Endpoint:**

  - `PUT /monitoring-app/endpoints/{id}`
  - **Set:** `name`, `url`, `monitoredInterval`

- **Delete a Monitored Endpoint:**

  - `DELETE /monitoring-app/endpoints/{id}`

- **Get Last 10 Monitoring Results:**
  - `GET /monitoring-app/results/{endpointId}`
