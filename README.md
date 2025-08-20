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

Stub endpoints (dev profile only)
```http request
GET /dev/translate?q=hello → returns "hello" with latency 50–200ms
GET /dev/score?q=hola → returns "0.42" with latency 50
```
These simulate external services for local development.

## REST API Usage

Once the application is running, you can use the REST API:

```bash
# Check API health
curl http://localhost:8080/api/csv/health

# Get sample CSV for testing
curl http://localhost:8080/api/csv/sample

# Process CSV via HTTP POST
curl -X POST \
  -H "Content-Type: text/csv" \
  --data-binary @input.csv \
  http://localhost:8080/api/csv/process
```

**PowerShell equivalent:**
```powershell
# Check API health
Invoke-RestMethod -Uri "http://localhost:8080/api/csv/health" -Method GET

# Get sample CSV
Invoke-RestMethod -Uri "http://localhost:8080/api/csv/sample" -Method GET

# Process CSV file
$csv = Get-Content "input.csv" -Raw
Invoke-RestMethod -Uri "http://localhost:8080/api/csv/process" `
  -Method POST `
  -ContentType "text/csv" `
  -Body $csv
```
---
### Input CSV
```user_id,message
u1,hello
u2,hola
u1,bonjour
````
### Output CSV
```
user_id,total_messages,avg_score
u1,2,0.456789
u2,1,0.876543
```
---
### Running tests
Run tests with:
```bash
./gradlew test
```
---
### Test cases implemented
Unit tests
- MessageNormalizerTest: verifies normalization and stable hashing.
- AggregationServiceTest: verifies per-user totals and average scores. 
- CsvProcessorOneRowTest: verifies output generation for a single-row CSV.

Integration tests (WireMock)
- LatencyAndCacheIT: simulates Translation/Scoring with 50–200ms latency, checks the pipeline processes messages correctly, and validates that duplicate messages hit the cache (calls ≤2).
- FailureResilienceIT: simulates a 500 error on /score followed by recovery, checks that the pipeline retries and completes.
- ---

### Performance testing (large datasets)
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
### Configuration
application.properties:
```properties
processing.concurrency=32

quarkus.rest-client.translation.url=http://localhost:8080/dev/translate
quarkus.rest-client.translation.connect-timeout=1000
quarkus.rest-client.translation.read-timeout=500

quarkus.rest-client.scoring.url=http://localhost:8080/dev/score
quarkus.rest-client.scoring.connect-timeout=1000
quarkus.rest-client.scoring.read-timeout=500

quarkus.micrometer.export.prometheus.enabled=true
quarkus.log.console.json=true
quarkus.log.category."io.quarkus".level=DEBUG
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

### Metrics and Monitoring 📈
Prometheus metrics exposed at: http://localhost:8080/q/metrics
```plaintext
(Counters)
pipeline.messages.processed
pipeline.messages.failed
(Cache behavior visible in debug logs)
💾 Cache HIT/MISS tracking for performance optimization
```
---
### Summary

This system demonstrates:
- CSV streaming for large datasets (millions of rows).
- Reactive concurrency with back-pressure control.
- Caching for duplicate messages (bots/spam scenarios).
- Resilience: timeout, retry, and circuit breaker on external calls.
- Unit and integration test coverage with WireMock.
- Metrics and structured logging.