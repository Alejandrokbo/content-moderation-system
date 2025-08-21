# Content Moderation System

This project implements a **content moderation pipeline** in **Quarkus**.  
It processes CSV files with millions of rows, interacts with external **Translation** and **Scoring** services, applies caching for duplicate messages, aggregates results per user, and outputs a CSV report.

The system provides both **REST API** and **programmatic interfaces** for processing CSV files.

---

## Development mode

Run with hot reload and stub endpoints:

```bash
./gradlew quarkusDev
```

Dev UI is available at: http://localhost:8080/q/dev/  ````provided by default Quarkus dev mode.````

You can access the REST API at: http://localhost:8080 ``` main page with API links.```

---

## Stub endpoints (dev profile only)
```http request
GET /dev/translate?q=hello → returns "hello" with latency 50–200ms
GET /dev/score?q=hola → returns "0.42" with latency 50
```
These simulate external services for local development.

## REST API Usage

Once the application is running, you can use the REST API:

**macOS/Linux (curl):**
```bash
# Check API health
curl http://localhost:8080/api/csv/health

# Get sample CSV for testing
curl http://localhost:8080/api/csv/sample

# Process CSV via HTTP POST
curl -X POST \
  -H "Content-Type: text/plain" \
  --data-binary @sample-messages.csv \
  http://localhost:8080/api/csv/process
```

**PowerShell equivalent:**
```powershell
# Check API health
Invoke-RestMethod -Uri "http://localhost:8080/api/csv/health" -Method GET

# Get sample CSV
Invoke-RestMethod -Uri "http://localhost:8080/api/csv/sample" -Method GET

# Process CSV file
$csv = Get-Content "sample-messages.csv" -Raw
Invoke-RestMethod -Uri "http://localhost:8080/api/csv/process" `
  -Method POST `
  -ContentType "text/csv" `
  -Body $csv
```
---
### Input CSV
There is 2 sample CSV files provided in the `root` directory, spanish and english that can be used.
- example:
```csv
user_id,message
u1,hello
u2,hola
u1,bonjour
````
### Output CSV
```csv
user_id,total_messages,avg_score
u1,2,0.456789
u2,1,0.876543
```

**📁 File Storage:**
When processing CSV files via the REST API, the processed results are automatically saved to the `output/` directory in the project root with the following naming convention:
- **Format:** `processed-YYYYMMDD-HHMMSS.csv`
- **Location:** `./output/processed-YYYYMMDD-HHMMSS.csv`
- **Example:** `./output/processed-20250821-093507.csv`

The API response includes the filename in the `X-Output-File` header for reference.
---
### Running tests
Run tests with:
```bash
./gradlew test
```
---
### Test cases implemented

#### Unit tests
- **MessageNormalizerTest**: verifies normalization and stable hashing.
- **AggregationServiceTest**: verifies per-user totals and average scores. 
- **CsvProcessorOneRowTest**: verifies output generation for a single-row CSV.
- **SmokeTest**: basic end-to-end pipeline functionality test.

#### Integration tests (WireMock)
- **LatencyAndCacheTest**: simulates Translation/Scoring with 50–200ms latency, checks the pipeline processes messages correctly, and validates that duplicate messages hit the cache (calls ≤2).
- **FailureResilienceTest**: simulates a 500 error on /score followed by recovery, checks that the pipeline retries and completes.

#### Metrics tests
- **MetricsTest**: validates Micrometer counter functionality
  - Tests `pipeline.messages.processed` and `pipeline.messages.failed` counters
  - Verifies metrics tracking for successful and failed processing
  - Validates counter accessibility through MeterRegistry
- **PrometheusMetricsTest**: validates Prometheus metrics endpoint
  - Tests `/q/metrics` endpoint availability and format
  - Verifies custom pipeline metrics are exposed in Prometheus format
  - Validates JVM metrics inclusion
  - Tests endpoint performance (response time < 1s)

#### Translation tests
- **BasicTranslationTest**: validates Spanish-to-English translation functionality
  - Tests basic word translation ("hola" → "hello")
  - Tests phrase translation ("buenos días" → "good morning")
  - Tests case preservation (uppercase, title case)
  - Tests unknown words remain unchanged
  - Tests complex phrases ("me gusta el gato" → "I like the cat")

## Performance testing (large datasets)
You can generate synthetic datasets to validate throughput and caching

- Bash (Linux/macOS)
```bash
# 100k rows (~6–8 MB). Increase N for more (e.g. 1000000).
N=100000
awk -v N="$N" 'BEGIN{
  srand();
  print "user_id,message";
  phrases[0]="hello"; phrases[1]="hola"; phrases[2]="bonjour";
  phrases[3]="ciao"; phrases[4]="Hola!!!"; phrases[5]="  hóla   !!! ";
  for (i=1; i<=N; i++) {
    u = "u" (i % 100);     # 100 distinct users
    p = phrases[i % 6];    # deterministic mix with duplicates
    printf "%s,%s %d\n", u, p, i;
  }
}' > input.csv

./gradlew build
time java -jar build/quarkus-app/quarkus-run.jar flag-users --in input.csv --out output.csv

head output.csv
```

- PowerShell (Windows)
```powershell
$N = 100000
"user_id,message" | Out-File -Encoding UTF8 input.csv
$phrases = @("hello","hola","bonjour","ciao","Hola!!!","  hóla   !!! ")
1..$N | ForEach-Object {
  $u = "u$($_ % 100)"
  $p = $phrases[ $_ % $phrases.Count ]
  "$u,$p $_" | Add-Content -Encoding UTF8 input.csv
}

./gradlew build
Measure-Command {
  java -jar build/quarkus-app/quarkus-run.jar flag-users --in input.csv --out output.csv
} | Select-Object TotalSeconds

Get-Content output.csv -TotalCount 10
```
---

## Docker Deployment 🐳

The project includes multiple Dockerfiles for different deployment scenarios, all pre-configured and optimized by Quarkus:

### Available Docker Images

#### 1. JVM Mode (Recommended for most cases)
```bash
# Build the application
./gradlew build

# Build Docker image
docker build -f src/main/docker/Dockerfile.jvm -t content-moderation-system:jvm .

# Run the container
docker run -i --rm -p 8080:8080 content-moderation-system:jvm
```
**Purpose**: Standard JVM deployment with fast startup and good performance. Best for production environments.

#### 2. Native Mode (Ultra-fast startup)
```bash
# Build native executable (requires GraalVM or Docker)
./gradlew build -Dquarkus.native.enabled=true

# Build Docker image
docker build -f src/main/docker/Dockerfile.native -t content-moderation-system:native .

# Run the container
docker run -i --rm -p 8080:8080 content-moderation-system:native
```
**Purpose**: Near-instant startup (~milliseconds) and lower memory footprint. Ideal for serverless, microservices, and auto-scaling scenarios.

#### 3. Native Micro (Smallest footprint)
```bash
# Build native executable
./gradlew build -Dquarkus.native.enabled=true

# Build micro Docker image
docker build -f src/main/docker/Dockerfile.native-micro -t content-moderation-system:micro .

# Run the container
docker run -i --rm -p 8080:8080 content-moderation-system:micro
```
**Purpose**: Smallest possible container size. Perfect for edge computing and resource-constrained environments.

#### 4. Legacy JAR (Compatibility)
```bash
# Build legacy JAR format
./gradlew build -Dquarkus.package.jar.type=legacy-jar

# Build Docker image
docker build -f src/main/docker/Dockerfile.legacy-jar -t content-moderation-system:legacy .

# Run the container
docker run -i --rm -p 8080:8080 content-moderation-system:legacy
```
**Purpose**: Traditional "fat JAR" format for compatibility with older deployment systems.

### Docker Configuration Options

All Docker images support environment variables for configuration:

```bash
# Example with custom configuration
docker run -i --rm -p 8080:8080 \
  -e JAVA_OPTS_APPEND="-Xmx512m" \
  -e QUARKUS_HTTP_HOST="0.0.0.0" \
  content-moderation-system:jvm
```

### Debug Mode in Docker
```bash
# Enable remote debugging
docker run -i --rm -p 8080:8080 -p 5005:5005 \
  -e JAVA_DEBUG=true \
  -e JAVA_DEBUG_PORT="*:5005" \
  content-moderation-system:jvm
```

### Production Deployment
For production, use the JVM or native images with:
- Proper resource limits
- Health checks
- Monitoring integration
- External configuration

```bash
# Production example with resource limits
docker run -d \
  --name content-moderation \
  -p 8080:8080 \
  --memory="512m" \
  --cpus="1.0" \
  --health-cmd="curl -f http://localhost:8080/q/health || exit 1" \
  --health-interval=30s \
  content-moderation-system:jvm
```

```
NOTE: The Dockerfiles are automatically generated by Quarkus and optimized for production use. 
This project does not need manual Dockerfile management. Use the provided Dockerfiles for building and running the application in different modes.
``` 
---

## Configuration
application.properties:
```properties
# Packaging configuration
quarkus.package.jar.enabled=true
quarkus.package.jar.type=fast-jar
quarkus.native.enabled=false

quarkus.log.console.format=%d{HH:mm:ss} %-5p [%c{2.}] %s%e

quarkus.rest-client.translation.url=http://localhost:8080/dev/translate
quarkus.rest-client.scoring.url=http://localhost:8080/dev/score

processing.concurrency=32
cache.maxSize=100000
cache.expireAfterWrite=300

quarkus.rest-client.translation.connect-timeout=1000
quarkus.rest-client.translation.read-timeout=500

quarkus.rest-client.scoring.connect-timeout=1000
quarkus.rest-client.scoring.read-timeout=500

# Metrics - minimal configuration
quarkus.micrometer.enabled=true
quarkus.micrometer.binder.jvm=true
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.export.prometheus.default-registry=true

# Logging simplified configuration
quarkus.log.level=INFO
quarkus.log.category."org.ravenpack".level=DEBUG
quarkus.log.console.enable=true

quarkus.http.port=8080

# Corrected HTTP configuration
quarkus.http.root-path=/
quarkus.http.non-application-root-path=/q

# Additional configurations for stability
quarkus.http.host=0.0.0.0
quarkus.http.cors=true
quarkus.http.enable-compression=true

# CDI and Shutdown
quarkus.shutdown.timeout=10s
quarkus.arc.remove-unused-beans=false
```
---
### Interactive Logging 🔍
The system features rich, interactive logging with emojis and detailed progress tracking:

```
🚀 Starting content moderation pipeline...
📂 Input file: /path/to/input.csv
📝 Output file: /path/to/output.csv
⚡ Concurrency level: 32
📊 Parsing CSV input file...
✅ Parsed 8 messages to process
🔄 Starting async processing of 8 messages...

💬 Processing message from user alice: 'Hello!' -> 'hello!'
🌍 Translating message for user alice...
🌐 Cache MISS for translation: 'hello!' - calling external API
🌍 Translation API response: 'hello!' -> 'hello!'
🎯 Scoring translated message for user alice...
🎯 Cache MISS for scoring: 'hello!' - calling external API  
🎯 Scoring API response: 'hello!' -> 0.900000
🆕 New user detected: alice
📈 Updated stats for user alice: 1 messages, avg score: 0.900000

📈 Progress: 8/8 messages processed (100.0%)
✅ Async processing completed in 668 ms
🎉 Content moderation pipeline completed successfully!
📈 Summary: 8 messages processed, 4 unique users, 0 failures
⏱️ Total execution time: 670 ms
⚡ Average processing speed: 8.00 messages/second
```

## Metrics and Monitoring 📈
Prometheus metrics exposed at: http://localhost:8080/q/metrics
```plaintext
(Counters)
pipeline.messages.processed
pipeline.messages.failed
(Cache behavior visible in debug logs)
💾 Cache HIT/MISS tracking for performance optimization
```
---
## Summary

This system demonstrates:
- CSV streaming for large datasets (millions of rows).
- Reactive concurrency with back-pressure control.
- Caching for duplicate messages (bots/spam scenarios).
- Resilience: timeout, retry, and circuit breaker on external calls.
- Unit and integration test coverage with WireMock.
- Metrics and structured logging.
