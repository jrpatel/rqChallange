# Implementation Details

## Running the Application

Start the API module:
```
./gradlew api:bootRun
```

Run all test cases:
```
./gradlew test
```

## API Documentation

Swagger UI is available at: `http://localhost:8111/swagger-ui/index.html`

OpenAPI specification: `http://localhost:8111/v3/api-docs`

## API Endpoints

The Employee API is available at `/api/v2/employee` with the following endpoints:

- `GET /api/v2/employee` - Get all employees
- `GET /api/v2/employee/search/{searchString}` - Search employees by name
- `GET /api/v2/employee/{id}` - Get employee by ID
- `GET /api/v2/employee/highestSalary` - Get highest salary
- `GET /api/v2/employee/topTenHighestEarningEmployeeNames` - Get top 10 earners
- `POST /api/v2/employee` - Create new employee
- `DELETE /api/v2/employee/{id}` - Delete employee by ID

## Architecture Decisions

### WebClient vs RestTemplate
**Decision**: Used WebClient with blocking calls (`.block()`)

**Trade-offs**:
- **Pros**: Modern reactive client, better resource utilization, future-ready for async operations
- **Cons**: Blocking calls negate reactive benefits, could use RestTemplate for simpler blocking operations
- **Rationale**: Chosen for future scalability and modern Spring ecosystem alignment

### Retry Logic
**Implementation**: Exponential backoff optimized for random rate limiting
```yaml
max-attempts: 5
initial-delay: 500ms
max-backoff: 10000ms
```

**Benefits**:
- Handles random rate limiting from mock API effectively
- 5 attempts over ~16 second window accommodates rate limit resets
- Lower initial delay (500ms) suitable for random vs systematic failures
- Higher max backoff (10s) allows rate limit windows to expire

**Note**: Testing showed that longer wait times (10-12 seconds) work better with the current mock server's rate limiting logic, but such delays are not production-appropriate. Current configuration balances retry effectiveness with reasonable response times.

### Caching Strategy
**Implementation**: Spring Cache with Caffeine provider

**Configuration**:
- Cache provider: Caffeine
- Maximum size: 1000 entries
- TTL: 5 minutes (expireAfterWrite)
- Single cache: `employees`

**Cache Keys**:
- `'all'` - All employees
- `#id` - Individual employee by ID  
- `'search_' + #searchString` - Search results
- `'highestSalary'` - Highest salary calculation
- `'top10'` - Top 10 earners

**Benefits**:
- Reduces API calls to mock server
- Improves response times for repeated requests
- Caffeine provides high-performance, near-optimal hit rates
- Automatic cache eviction on create/delete operations
- Time-based expiration prevents stale data

## Potential Improvements

### Circuit Breaker Pattern
```java
@CircuitBreaker(name = "employee-service", fallbackMethod = "fallbackMethod")
```
- Prevent cascading failures when mock API is down
- Fail fast when service is unavailable
- Automatic recovery detection

### Rate Limiting
- Implement client-side rate limiting to respect mock API constraints
- Use token bucket or sliding window algorithms

### Async Processing
- Convert to fully reactive implementation
- Use `Mono<>` and `Flux<>` return types
- Non-blocking I/O for better scalability

### Enhanced Error Handling
- Custom exception types for different failure scenarios
- Structured error responses with error codes
- Correlation IDs for request tracing

### Metrics & Monitoring
- Micrometer metrics for API performance
- Health checks for mock API connectivity
- Request/response logging with correlation IDs

### API Documentation (Implemented)
**Swagger/OpenAPI Configuration**:
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI employeeAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Employee API")
                .description("Employee management system")
                .version("v2.0"));
    }
}
```
- ✅ Interactive API documentation at `http://localhost:8111/swagger-ui/index.html`
- ✅ OpenAPI 3.0 specification at `http://localhost:8111/v3/api-docs`
- ✅ Request/response examples
- ✅ Try-it-out functionality for testing endpoints