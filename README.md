# Omnichannel Broadcast Engine

A Java + JSF/PrimeFaces prototype demonstrating an asynchronous priority broadcast system with real-time progress tracking.

The application allows an operator to send a message to a list of customers while processing deliveries asynchronously in the background.

---

## Features

- Priority broadcast to multiple customers
- Java 21 backend
- Jakarta Faces (JSF) web application
- PrimeFaces UI components
- Asynchronous background processing
- Fixed-size worker thread pool
- Real-time broadcast progress
- Per-customer delivery status
- Sent and failed counters
- Elapsed time tracking
- Estimated remaining time
- Simulated external messaging API
- Random 10% API failure simulation
- Simulated 1–2 second API response time
- Thread-safe broadcast state
- Responsive UI

---

# Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Application development |
| Jakarta Faces | 4.1.3 | Server-side web framework |
| PrimeFaces | 15.0.17 | UI components and AJAX |
| Jakarta CDI | 4.1 | Dependency injection |
| Jakarta Servlet | 6.1 | Web application support |
| Maven | 3.9+ | Build and dependency management |
| Payara Micro | 7.2026.8 | Application server |

---

# Prerequisites

Before running the application, install the following:

## 1. Java 21

The application requires **Java 21**.

Verify:

```bash
java -version
```

The output should show Java 21.

Also verify the Java version used by Maven:

```bash
mvn -version
```

The Maven output should show Java 21.

## 2. Apache Maven 3.9+

Maven is required to build the project.

Verify:

```bash
mvn -version
```

The output should show Maven 3.9 or newer.

## 3. Payara Micro 7

The application is packaged as a WAR file and runs on Payara Micro.

The project was tested with:

```text
Payara Micro 7.2026.8
```

The Payara Micro JAR is required to run the generated WAR.

---

# Quick Start

For a quick local run:

```bash
mvn clean package
```

Then start Payara Micro:

```bash
java -jar payara-micro-7.2026.8.jar \
  --deploy target/omnichannel-broadcast.war \
  --port 8080 \
  --nocluster
```

Open:

```text
http://localhost:8080/omnichannel-broadcast/broadcast.xhtml
```

For the complete setup and execution steps, see the section below.

---

# How to Run

## Step 1 — Get the project

Clone the repository:

```bash
git clone <repository-url>
cd OmnichannelBroadcast
```

If the project is provided as a ZIP file, extract it and open a terminal in the extracted project directory.

Example:

```text
D:\git\OmnichannelBroadcast
```

---

## Step 2 — Verify Java

Run:

```bash
java -version
```

Confirm that Java 21 is installed.

Then verify the Java version used by Maven:

```bash
mvn -version
```

Make sure the output contains Java 21.

Example:

```text
Apache Maven 3.9.x
Java version: 21.x.x
```

> If multiple Java versions are installed, make sure `JAVA_HOME` points to the Java 21 installation before building.

## Step 4 — Start Payara Micro

Run Payara Micro using the generated WAR:

### Linux / macOS

```bash
java -jar payara-micro-7.2026.8.jar \
  --deploy target/omnichannel-broadcast.war \
  --port 8080 \
  --nocluster
```

### Windows PowerShell

```powershell
java -jar "C:\path\to\payara-micro-7.2026.8.jar" `
  --deploy "target\omnichannel-broadcast.war" `
  --port 8080 `
  --nocluster
```

Replace the Payara JAR path with the actual location of your Payara Micro installation.

The `--nocluster` option is used for local development.

---

## Step 5 — Wait for Payara to Start

Wait until Payara has completed deployment and the application is available.

The WAR is deployed using the application context:

```text
/omnichannel-broadcast
```

---

## Step 6 — Open the Application

Open the following URL in a browser:

```text
http://localhost:8080/omnichannel-broadcast/broadcast.xhtml
```

---

# How to Use the Application

## Step 1 — Enter a Message

Enter a message in the broadcast message field.

The application validates that:

- The message is not empty.
- The message does not exceed 500 characters.

## Step 2 — Start the Broadcast

Click:

```text
Start Priority Broadcast
```

The application creates a new broadcast job and begins processing customers asynchronously.

## Step 3 — Monitor the Broadcast

While the broadcast is running, the UI displays:

- Overall progress
- Processed customer count
- Sent count
- Failed count
- Individual customer status
- Start time
- Elapsed time
- Estimated remaining time

## Step 4 — Wait for Completion

Each customer progresses through:

```text
PENDING
   ↓
PROCESSING
   ↓
SENT / FAILED
```

When all customers have been processed:

- Progress reaches 100%.
- The broadcast is marked as completed.
- AJAX polling stops.
- The Start button becomes available again.

# Architecture

## Prototype Architecture

```text
                         Browser
                            |
                            v
                    PrimeFaces / JSF
                            |
                            v
              BroadcastViewController
                    (@ViewScoped)
                            |
                            v
                   BroadcastService
                            |
                            v
                   ExecutorService
                     Fixed Pool
                      5 Workers
                            |
          +---------+-------+-------+---------+
          |         |       |       |         |
          v         v       v       v         v
       Worker 1  Worker 2 Worker 3 Worker 4 Worker 5
          |         |       |       |         |
          +---------+-------+-------+---------+
                            |
                            v
                 MockMessagingService
                            |
                            v
                     BroadcastJob
                            |
                            v
                 PrimeFaces AJAX Polling
                            |
                            v
                         Browser
```

The controller starts the broadcast and returns control to the browser while worker threads process the messages in the background.

---

# Threading Model

The prototype uses:

```java
Executors.newFixedThreadPool(5)
```

A fixed pool of five workers provides bounded concurrency.

This prevents the application from creating an unbounded number of threads and limits the number of concurrent calls to the external messaging API.

The implementation intentionally uses a straightforward `ExecutorService` rather than adding additional asynchronous frameworks because the challenge is focused on demonstrating the core broadcast flow.

---

# Thread Safety

`BroadcastJob` is shared between background workers and the JSF request thread.

Thread-safe structures are therefore used:

- `ConcurrentHashMap` for customer statuses.
- `AtomicInteger` for sent and failed counters.
- `volatile` state for the completion timestamp.

This allows workers to update the broadcast while the UI reads its current state during AJAX polling.

---

# Real-Time Progress

PrimeFaces AJAX polling is used to periodically retrieve the current broadcast state.

```text
Browser
   |
   | AJAX request
   v
JSF Controller
   |
   v
BroadcastJob
   |
   | Current progress
   v
Browser
```

Polling starts when the broadcast begins and stops when all customers have been processed.

---

# Estimated Time Remaining

The prototype estimates the remaining time using:

```text
Average processing time per completed customer
                    ×
              Remaining customers
                    =
          Estimated remaining time
```

This is intentionally a simple estimate suitable for the small prototype.

A production implementation could use rolling throughput, queue depth, provider latency, and historical processing data for a more stable estimate.

---

# Failure Handling

Each customer is processed independently.

If the external API simulation fails, the customer is marked as:

```text
FAILED
```

The failed counter is incremented and the remaining customers continue processing.

Unexpected exceptions are also handled at the individual customer level so that a failure for one customer does not terminate the entire broadcast.

---

# Scalability

The current implementation is designed for the challenge's small prototype workload.

If the system needs to support approximately 50,000 recipients, the architecture should evolve rather than simply increasing the number of application threads.

A production implementation could introduce:

- Kafka or RabbitMQ for durable message distribution.
- PostgreSQL or another persistent database for job and delivery state.
- Horizontally scalable worker instances.
- Redis or another distributed rate limiter.
- Backpressure.
- Retry queues.
- Exponential backoff with jitter.
- Idempotency keys.
- Monitoring and alerting.

A possible production architecture:

```text
                         Browser
                            |
                            v
                    Load Balancer
                            |
             +--------------+--------------+
             |                             |
             v                             v
       Application 1                 Application 2
             |                             |
             +--------------+--------------+
                            |
                            v
                    Persistent Job
                            |
                            v
                    Kafka / RabbitMQ
                            |
             +--------------+--------------+
             |                             |
             v                             v
         Worker 1                       Worker 2
             |                             |
             +--------------+--------------+
                            |
                            v
                 External Messaging API
                            |
                            v
                   Persistent Status
```

The application servers handle incoming requests, while the message queue and workers handle the large volume of delivery tasks.

# Author

Technical Challenge Submission by Jethro Otto
