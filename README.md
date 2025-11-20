# p4pa-workflow-hub

This application belong to the **batch** tier of the **Piattaforma Unitaria** product.

See [PU Microservice Architecture](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1405845916/Architettura+microservizi) for more details.

## 🧱 Role

* To handle workflow scheduling and execution through [Temporal.io](https://temporal.io/);
  * See [Temporal.io Confluence page](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1776189802/Temporal.io) for details on its usage;
  * See [Workflow Confluence page](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1287356459/Workflow) for implemented Workflows;
* To handle custom debt position synchronization workflow (see [Confluence page](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1579286576)).

## 🌐 APIs
See [OpenAPI](openapi/generated.openapi.json), exposed through the following path:
* `/swagger-ui/index.html`

See [Postman collection](/postman/p4pa-workflow-hub-E2E.postman_collection.json) and [Postman Environment](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1094615081/Environment+collection+postman).

### 📌 Relevant APIs
* `POST /workflowhub/workflow-type-orgs`: To relate a debt position type org to a debt position custom synchronization workflow;
* `GET /workflowhub/workflows/{workflowId}/status`: To get workflow status;
* `GET /workflowhub/schedules/{scheduleId}/info`: To get schedule info;
* `POST /workflowhub/workflows/{workflowId}/wait-completion`: To wait for workflow termination;
* `POST /workflowhub/workflow/debt-position/sync`: To start debt position synchronization workflow;
* `POST /workflowhub/workflow/ingestion-flow/{ingestionFlowFileId}`: To start ingestion flow file processing workflow;
* `POST /workflowhub/workflow/export-file/{exportFileId}`: To start export file processing workflow;
* `POST /workflowhub/workflow/send-notification/{sendNotificationId}/start`: To start SEND notification workflow;
* `POST /workflowhub/workflow/assessments/receipt/{receiptId}`: To start assessments creation workflow;
* `POST /workflowhub/workflow/pagopa-fetch/payments-reporting/{organizationId}`: To manually start organization's payments reporting fetch from pagoPa workflow;
* `POST /workflowhub/workflow/taxonomy/synchronize`: To manually start pagoPa taxonomy synchronization workflow;

### 📌 Common HTTP status returned:
* `401`: Invalid access token provided, thus a new login is required;
* `403`: Trying to access a not authorized resource.

## 🌐 AsyncAPIs
See [AsyncAPI](asyncapi/generated.asyncapi.json), exposed through the following path:
* `/springwolf/asyncapi-ui.html`

## 🔎 Monitoring
See available actuator endpoints through the following path:
* `/actuator`

### 📌 Relevant endpoints
* Health (provide an accessToken to see details): `/actuator/health`
  * Liveness: `/actuator/health/liveness`
  * Readiness: `/actuator/health/readiness`
* Metrics: `/actuator/metrics`
  * Prometheus: `/actuator/prometheus`

Further endpoints are exposed through the JMX console.

## ✏️ Logging
See [log configured pattern](/src/main/resources/logback-spring.xml).

## 🔗 Dependencies

### 🗄️ Resources
* PostgreSQL
* Kafka
* Temporal.io

### 🧩 Microservices
* [p4pa-auth](https://github.com/pagopa/p4pa-auth):
  * To obtain a technical access token (used on WF to call inner microservices);
* [p4pa-organization](https://github.com/pagopa/p4pa-organization):
  * To retrieve the Broker related to a given organization.

## 🗃️ Entities handled
* `workflow_type`
* `workflow_type_org`
* `debt_position_workflow_type`

## 🔧 Configuration

See [application.yml](src/main/resources/application.yml) for each configurable property.

### 📌 Relevant configurations

#### 🌐 Application Server
| ENV         | DESCRIPTION                       | DEFAULT |
|-------------|-----------------------------------|---------|
| SERVER_PORT | Application server listening port | 8080    |

#### ✏️ Logging
| ENV                                   | DESCRIPTION                                                                                                                                                                     | DEFAULT |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| LOG_LEVEL_ROOT                        | Base level                                                                                                                                                                      | INFO    |
| LOG_LEVEL_PAGOPA                      | Base level of custom classes                                                                                                                                                    | INFO    |
| LOG_LEVEL_SPRING                      | Level applied to Spring framework                                                                                                                                               | INFO    |
| LOG_LEVEL_SPRING_BOOT_AVAILABILITY    | To print availability events                                                                                                                                                    | DEBUG   |
| LOGGING_LEVEL_API_REQUEST_EXCEPTION   | Level applied to APIs exception                                                                                                                                                 | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG             | Level applied to [PerformanceLog](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.-Log-di-performance)                                               | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_API_REQUEST | Level applied to [API Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.1.-Log-di-perfomance-per-le-API)                            | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_REST_INVOKE | Level applied to [REST invoke Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.2.-Log-di-performance-per-i-servizi-REST-integrati) | INFO    |

#### 🔁 Integrations

##### 🗄️ Resources
| ENV                          | DESCRIPTION                                                                   | DEFAULT                                                                                                                      |
|------------------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| SHOW_SQL                     | To print SQL statements                                                       | false                                                                                                                        |
| WORKFLOW_HUB_DB_URL          | PostgreSQL connection string (to use in order to customize the entire string) | jdbc:postgresql://${CLASSIFICATION_DB_HOST}:${CLASSIFICATION_DB_PORT}/${CLASSIFICATION_DB_NAME}?currentSchema=debt_positions |
| WORKFLOW_HUB_DB_HOST         | PostgreSQL Host                                                               | localhost                                                                                                                    |
| WORKFLOW_HUB_DB_PORT         | PostgreSQL port                                                               | 5432                                                                                                                         |
| WORKFLOW_HUB_DB_NAME         | PostgreSQL Database name                                                      | payhub                                                                                                                       |
| WORKFLOW_HUB_DB_USER         | PostgreSQL username                                                           |                                                                                                                              |
| WORKFLOW_HUB_DB_PASSWORD     | PostgreSQL password                                                           |                                                                                                                              |

##### 📋 [Caching](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1542128077/Caching)
| ENV                        | DESCRIPTION                                 | DEFAULT |
|----------------------------|---------------------------------------------|---------|
| CACHE_ORGANIZATION_SIZE    | Organization data cache size                | 100     |
| CACHE_ORGANIZATION_MINUTES | Organization data cache retention (minutes) | 60      |

##### 🔗 REST
| ENV                                               | DESCRIPTION                               | DEFAULT |
|---------------------------------------------------|-------------------------------------------|---------|
| DEFAULT_REST_CONNECTION_POOL_SIZE                 | Default connection pool size              | 10      |
| DEFAULT_REST_CONNECTION_POOL_SIZE_PER_ROUTE       | Default connection pool size per route    | 5       |
| DEFAULT_REST_CONNECTION_POOL_TIME_TO_LIVE_MINUTES | Default connection pool TTL (minutes)     | 10      |
| DEFAULT_REST_TIMEOUT_CONNECT_MILLIS               | Default connection timeout (milliseconds) | 120000  |
| DEFAULT_REST_TIMEOUT_READ_MILLIS                  | Default read timeout (milliseconds)       | 120000  |

##### 🧩 Microservices
| ENV                                | DESCRIPTION                                    | DEFAULT |
|------------------------------------|------------------------------------------------|---------|
| AUTH_BASE_URL                      | Auth microservice URL                          |         |
| AUTH_MAX_ATTEMPTS                  | Auth API max attempts                          | 3       |
| AUTH_WAIT_TIME_MILLIS              | Auth retry waiting time (milliseconds)         | 500     |
| AUTH_PRINT_BODY_WHEN_ERROR         | To print body when an error occurs             | true    |
| ORGANIZATION_BASE_URL              | Organization microservice URL                  |         |
| ORGANIZATION_MAX_ATTEMPTS          | Organization API max attempts                  | 3       |
| ORGANIZATION_WAIT_TIME_MILLIS      | Organization retry waiting time (milliseconds) | 500     |
| ORGANIZATION_PRINT_BODY_WHEN_ERROR | To print body when an error occurs             | true    |

##### 🌀 KAFKA
| ENV                                | DESCRIPTION                                                        | DEFAULT                |
|------------------------------------|--------------------------------------------------------------------|------------------------|
| KAFKA_BINDER_BROKER                | Comma separated list of brokers to which the Kafka binder connects |                        |
| KAFKA_PAYMENTS_BINDER_BROKER       | Comma separated list of brokers to which the Kafka binder connects | ${KAFKA_BINDER_BROKER} |
| KAFKA_DATA_EVENTS_BINDER_BROKER    | Comma separated list of brokers to which the Kafka binder connects | ${KAFKA_BINDER_BROKER} |
| KAFKA_CONFIG_HEARTBEAT_INTERVAL_MS | Hearth beat interval (milliseconds)                                | 3000                   |
| KAFKA_CONFIG_SESSION_TIMEOUT_MS    | Session timeout (milliseconds)                                     | 30000                  |
| KAFKA_CONFIG_REQUEST_TIMEOUT_MS    | Request timeout (milliseconds)                                     | 60000                  |
| KAFKA_CONFIG_METADATA_MAX_AGE      | Metadata max age (milliseconds)                                    | 180000                 |
| KAFKA_CONFIG_SASL_MECHANISM        | SASL mechanism                                                     | PLAIN                  |
| KAFKA_CONFIG_SECURITY_PROTOCOL     | Security protocol                                                  | SASL_SSL               |
| KAFKA_CONFIG_MAX_REQUEST_SIZE      | Max request size                                                   | 1000000                |

###### 📤 KAFKA PRODUCERS
| ENV                                                 | DESCRIPTION                                       | DEFAULT                     |
|-----------------------------------------------------|---------------------------------------------------|-----------------------------|
| KAFKA_TOPIC_PAYMENTS                                | Topic where to publish payment event              | p4pa-payhub-payments-evh    |
| KAFKA_PAYMENTS_PRODUCER_SASL_JAAS_CONFIG            | JAAS Config string used to perform authentication |                             |
| KAFKA_PAYMENTS_PRODUCER_CONNECTION_MAX_IDLE_TIME    | Max producer idle time (milliseconds)             | 180000                      |
| KAFKA_PAYMENTS_PRODUCER_RETRY_MS                    | Producer retry waiting time (milliseconds)        | 10000                       |
| KAFKA_PAYMENTS_PRODUCER_LINGER_MS                   | Producer linger time (milliseconds)               | 2                           |
| KAFKA_PAYMENTS_PRODUCER_BATCH_SIZE                  | Producer batch size                               | 16384                       |
| KAFKA_TOPIC_DATA_EVENTS                             | Topic where to publish data events                | p4pa-payhub-data-events-evh | 
| KAFKA_DATA_EVENTS_PRODUCER_SASL_JAAS_CONFIG         | JAAS Config string used to perform authentication |                             |
| KAFKA_DATA_EVENTS_PRODUCER_CONNECTION_MAX_IDLE_TIME | Max producer idle time (milliseconds)             | 180000                      |
| KAFKA_DATA_EVENTS_PRODUCER_RETRY_MS                 | Producer retry waiting time (milliseconds)        | 10000                       |
| KAFKA_DATA_EVENTS_PRODUCER_LINGER_MS                | Producer linger time (milliseconds)               | 2                           |
| KAFKA_DATA_EVENTS_PRODUCER_BATCH_SIZE               | Producer batch size                               | 16384                       |

###### 📥 KAFKA CONSUMERS
| ENV                                              | DESCRIPTION                                                                                    | DEFAULT                                            |
|--------------------------------------------------|------------------------------------------------------------------------------------------------|----------------------------------------------------|
| KAFKA_CONSUMER_CONFIG_AUTO_COMMIT                | True if the acknowledgement of the message is implicit if there are not errors                 | true                                               |
| KAFKA_CONSUMER_CONFIG_CONNECTIONS_MAX_IDLE_MS    | Maximum lifetime for idle connections (milliseconds)                                           | 180000                                             |
| KAFKA_CONFIG_MAX_POLL_INTERVAL_TIMEOUT_MS        | Maximum interval between polls declared toward the broker (milliseconds)                       | 300000                                             |
| KAFKA_CONSUMER_CONFIG_MAX_POLL_SIZE              | Maximum number of messages fetch for each poll                                                 | 500                                                |
| KAFKA_CONSUMER_CONFIG_CONNECTION_TIMEOUT_MS      | Initial timeout configured for the connection process (milliseconds)                           | 100000                                             |
| KAFKA_CONSUMER_CONFIG_CONNECTION_TIMEOUT_MAX_MS  | Maximum timeout configured when connection attempts repeatedly fail (milliseconds)             | 200000                                             |
| KAFKA_CONSUMER_CONFIG_STANDARD_HEADERS           | If ask for contextual metadata headers when reading messages                                   | both                                               |
| KAFKA_CONSUMER_CONFIG_START_OFFSET               | Where the consumer should begins consuming messages from a topic's partition (earliest/latest) | earliest                                           |
| KAFKA_TOPIC_PAYMENTS                             | Topic where to read payment event                                                              | p4pa-payhub-payments-evh                           |
| KAFKA_PAYMENTS_SASL_JAAS_CONFIG                  | JAAS Config string used to perform authentication                                              |                                                    |
| KAFKA_PAYMENTS_GROUP_ID                          | Consumer group id                                                                              | p4pa-workflow-hub-consumer-group                   |
| KAFKA_PAYMENTS_CONSUMER_ENABLED                  | If the consumer should read messages                                                           | true                                               |
| KAFKA_PAYMENTS_AUTO_COMMIT                       | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_AUTO_COMMIT}               |
| KAFKA_PAYMENTS_REQUEST_CONNECTIONS_MAX_IDLE_MS   | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_CONNECTIONS_MAX_IDLE_MS}   |
| KAFKA_PAYMENTS_INTERVAL_TIMEOUT_MS               | See default config description                                                                 | ${KAFKA_CONFIG_MAX_POLL_INTERVAL_TIMEOUT_MS}       |
| KAFKA_PAYMENTS_MAX_POLL_SIZE                     | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_MAX_POLL_SIZE}             |
| KAFKA_PAYMENTS_REQUEST_CONNECTION_TIMEOUT_MAX_MS | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_CONNECTION_TIMEOUT_MAX_MS} |
| KAFKA_PAYMENTS_REQUEST_CONNECTION_TIMEOUT_MS     | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_CONNECTION_TIMEOUT_MS}     |
| KAFKA_PAYMENTS_STANDARD_HEADERS                  | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_STANDARD_HEADERS}          |
| KAFKA_PAYMENTS_REQUEST_START_OFFSET              | See default config description                                                                 | ${KAFKA_CONSUMER_CONFIG_START_OFFSET}              |

##### 🕒 Temporal.io
| ENV                                                       | DESCRIPTION                                                            | DEFAULT   |
|-----------------------------------------------------------|------------------------------------------------------------------------|-----------|
| TEMPORAL_SERVER_HOST                                      | Temporal hostname                                                      | localhost |
| TEMPORAL_SERVER_PORT                                      | Temporal port                                                          | 7233      |
| TEMPORAL_SERVER_ENABLE_HTTPS                              | To use HTTPS when invoking Temporal                                    | false     |
| TEMPORAL_SERVER_NAMESPACE                                 | Temporal namespace                                                     | pu        |
| DEFAULT_ACTIVITY_CONFIG_START_TO_CLOSE_TIMEOUT_IN_SECONDS | Default startToClose activity timeout (seconds)                        | 300       |
| DEFAULT_ACTIVITY_CONFIG_RETRY_INITIAL_INTERVAL_IN_MILLIS  | Default initial interval to wait during retries (milliseconds)         | 1000      |
| DEFAULT_ACTIVITY_CONFIG_RETRY_BACKOFF_COEFFICIENT         | Default backoff coefficient used to increase the delay between retries | 1.5       |
| DEFAULT_ACTIVITY_CONFIG_RETRY_MAXIMUM_ATTEMPTS            | Default maximum number of retries                                      | 30        |

See `workflow.*` properties on [application.yml](src/main/resources/application.yml) to check configuration for each workflow.

###### 📥 TaskQueue poller sizes
| ENV                                                 | DESCRIPTION                                                                      | DEFAULT |
|-----------------------------------------------------|----------------------------------------------------------------------------------|---------|
| WF_LOW_PRIORITY_POLLER_SIZE                         | Poller size configured for Temporal task queue `LowPriorityWF`                   | 3       |
| WF_DP_LOW_PRIORITY_POLLER_SIZE                      | Poller size configured for Temporal task queue `DebtPositionWF`                  | 3       |
| WF_DP_RESERVED_SYNC_POLLER_SIZE                     | Poller size configured for Temporal task queue `DebtPositionSyncWF`              | 10      |
| WF_DP_RESERVED_SYNC_LOCAL_POLLER_SIZE               | Poller size configured for Temporal task queue `DebtPositionSyncWF_LOCAL`        | 5       |
| WF_DP_RESERVED_CUSTOM_SYNC_POLLER_SIZE              | Poller size configured for Temporal task queue `DebtPositionCustomSyncWF`        | 5       |
| WF_DP_RESERVED_CUSTOM_SYNC_LOCAL_POLLER_SIZE        | Poller size configured for Temporal task queue `DebtPositionCustomSyncWF_LOCAL`  | 5       |
| WF_IMPORT_MEDIUM_PRIORITY_POLLER_SIZE               | Poller size configured for Temporal task queue `IngestionFlowFileWF`             | 3       |
| WF_IMPORT_MEDIUM_PRIORITY_LOCAL_POLLER_SIZE         | Poller size configured for Temporal task queue `IngestionFlowFileWF_LOCAL`       | 2       |
| WF_EXPORT_MEDIUM_PRIORITY_POLLER_SIZE               | Poller size configured for Temporal task queue `ExportFileWF`                    | 3       |
| WF_EXPORT_MEDIUM_PRIORITY_LOCAL_POLLER_SIZE         | Poller size configured for Temporal task queue `ExportFileWF_LOCAL`              | 2       |
| WF_CLASSIFICATION_MEDIUM_PRIORITY_POLLER_SIZE       | Poller size configured for Temporal task queue `ClassificationWF`                | 3       |
| WF_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL_POLLER_SIZE | Poller size configured for Temporal task queue `ClassificationWF_LOCAL`          | 2       |
| WF_SEND_RESERVED_NOTIFICATION_POLLER_SIZE           | Poller size configured for Temporal task queue `SendNotificationProcessWF`       | 3       |
| WF_SEND_RESERVED_NOTIFICATION_LOCAL_POLLER_SIZE     | Poller size configured for Temporal task queue `SendNotificationProcessWF_LOCAL` | 2       |
| WF_SEND_LOW_PRIORITY_POLLER_SIZE                    | Poller size configured for Temporal task queue `SendWF`                          | 3       |
| WF_ASSESSMENTS_RESERVED_CREATION_POLLER_SIZE        | Poller size configured for Temporal task queue `AssessmentCreationWF`            | 5       |
| WF_ASSESSMENTS_POLLER_SIZE                          | Poller size configured for Temporal task queue `AssessmentsWF`                   | 5       |
| WF_ASSESSMENTS_CLASSIFICATION_POLLER_SIZE           | Poller size configured for Temporal task queue `AssessmentClassificationWF`      | 5       |

#### 💼 Business logic
| ENV                                                   | DESCRIPTION                                                    | DEFAULT      |
|-------------------------------------------------------|----------------------------------------------------------------|--------------|
| SCHEDULE_PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH_CRON | Frequency of pagoPa payments reporting fetch (cron expression) | 0 1/6 * * *  |
| SCHEDULE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_CRON       | Frequency of pagoPa taxonomy synchronization (cron expression) | 0 0 * * *    |
| SCHEDULE_EXPORT_FILE_EXPIRATION_DAYS                  | Expiration days applied to exported files                      | 30           |

#### 🔑 keys
| ENV                                  | DESCRIPTION                                                                              | DEFAULT |
|--------------------------------------|------------------------------------------------------------------------------------------|---------|
| JWT_TOKEN_PUBLIC_KEY                 | p4pa-auth JWT public key                                                                 |         |
| CIPHER_DP_SYNC_WF_CONFIG_ENCRYPT_PSW | Base64 encoded key (256 bit) used to encrypt debt position workflow execution parameters |         |
| AUTH_CLIENT_SECRET                   | client_secret used on M2M authentication to get a technical access token                 |         |

## 🛠️ Getting Started

### 📝 Prerequisites

Ensure the following tools are installed on your machine:

1. **Java 21+**
2. **Gradle** (or use the Gradle wrapper included in the repository)
3. **Docker** (to build and run on an isolated environment, optional)
4. **GITHUB_TOKEN environment variable**

### 🔐 Write Locks

```sh
./gradlew dependencies --write-locks
```

### ⚙️ Build

```sh
./gradlew clean build
```

### 🧪 Test

#### 📌 JUnit
```sh
./gradlew test
```

### 🚀 Run local

```sh
./gradlew bootRun
```

### 🐳 Build & run through Docker
```sh
docker build -t <APP_NAME> .
docker run --env-file <ENV_FILE> <APP_NAME>
```

### ⚖️ Generate dependencies licenses
```sh
./gradlew generateLicenseReport
```
