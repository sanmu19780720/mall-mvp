# Health

## Requirements

### Requirement: Health Check Endpoint
The system SHALL expose an unauthenticated HTTP endpoint at `GET /api/health` that reports service liveness.

#### Scenario: Service is up
- GIVEN the backend application is running
- WHEN a client sends `GET /api/health`
- THEN the response status is 200
- AND the response Content-Type is `application/json`
- AND the response body is `{"status":"ok"}`

#### Scenario: No authentication required
- GIVEN a client without any authentication credentials
- WHEN the client sends `GET /api/health`
- THEN the request is not rejected for authentication reasons
- AND the response status is 200
