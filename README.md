<div align="center">
<img src="https://github.com/quarkiverse/.github/blob/main/assets/images/quarkus.svg" width="67" height="70" ><img src="https://github.com/quarkiverse/.github/blob/main/assets/images/plus-sign.svg" height="70" ><img src="https://www.vectorlogo.zone/logos/debeziumio/debeziumio-icon.svg" height="70" >

# Quarkus Debezium Consumer
</div>



This project demonstrates Change Data Capture (CDC) using [Debezium](https://debezium.io/) with Oracle as the source database and Kafka Streams for processing customer data changes. It includes a Quarkus application that consumes Debezium CDC events, processes them using Kafka Streams, and can optionally sync the data to PostgreSQL.

## Architecture

The application:
- Captures changes from Oracle DB using [Debezium Oracle Connector](https://debezium.io/documentation/reference/stable/connectors/oracle.html)
- Processes CDC events through [Quarkus Kafka Streams](https://quarkus.io/guides/kafka-streams)
- Tracks database record changes (creates, updates, deletes)
- Uses [Javers](https://github.com/javers/javers) for detailed change tracking on updates
- Can sync changes to PostgreSQL using [Debezium JDBC Sink Connector](https://debezium.io/documentation/reference/stable/connectors/jdbc.html)

## Use Cases
- Real-time data synchronization between databases such as Oracle and PostgreSQL
- CDC pipeline for event-driven applications and microservices
- Sending small payloads of database CDC events through a WebSocket to the UI to render only what needs to be updated
- Third party application can stream event driven data changes from Kafka

[![Use Cases](https://github.com/melloware/quarkus-debezium/blob/main/docker/debezium-use-cases.png)](https://github.com/melloware/quarkus-debezium)

## Track Changes

Original Database Table (CUSTOMERS):

| ID   | FIRST_NAME | LAST_NAME | EMAIL               |
|------|------------|-----------|---------------------|
| 1033 | Bob        | Belcher   | bob@burgerboss.com  |

After UPDATE:

| ID   | FIRST_NAME | LAST_NAME | EMAIL                 |
|------|------------|-----------|------------------------|
| 1033 | Robert     | Belcher   | bob.belcher@gmail.com |

CDC Event Format (Flattened with Change Tracking):

| COLUMN_NAME | OLD_VALUE     | NEW_VALUE           |
|-------------|---------------|---------------------|
| ID          | 1033          | 1033                |
| FIRST_NAME  | Bob           | Robert              |
| LAST_NAME   | Belcher       | Belcher             |
| EMAIL       | bob@burgerboss.com | bob.belcher@gmail.com |


## Running the Stack

This guide provides step-by-step instructions for setting up an Oracle database with Debezium, configuring source and sink connectors, and verifying data flow using Kafka and PostgreSQL.

## Prerequisites

- Docker and Docker Compose installed
- PowerShell for executing connector scripts
- Java and Apache Maven installed

## Steps

### 1. Start the Docker Containers

Bring up the required services using Docker Compose:

```sh
docker compose up -d
```

### 2. Access the Oracle Container

Once the containers are running, execute the following command to open a shell in the Oracle container:

```sh
docker exec -it oracle /bin/bash
```

### 3. Initialize the Oracle Database

Run the initialization script inside the Oracle container:

```sh
./oracle-initialize.sh
```

### 4. Create the Oracle Connector

Use PowerShell to submit the Debezium Oracle source connector:

```powershell
.\submit-connector.ps1 -JsonFilePath ".\create-oracle-source-connector.json"
```

Verify the status of the connector:

```powershell
.\log-connectors.ps1
```

Expected output:

```plaintext
source | oracle-source-connector | RUNNING | RUNNING | io.debezium.connector.oracle.OracleConnector
```

### 5. Insert and Update Data in Oracle

Access Oracle SQL*Plus with the `debezium` user:

```sh
sqlplus debezium/dbz@localhost:1521/freepdb1
```

Enable autocommit:

```sql
SET AUTOCOMMIT ON;
```

Insert sample customer data:

```sql
INSERT INTO customers(ID, FIRST_NAME, LAST_NAME, EMAIL) VALUES (1031, 'Homer', 'Simpson', 'homer@springfield.gov');
INSERT INTO customers(ID, FIRST_NAME, LAST_NAME, EMAIL) VALUES (1023, 'Rick', 'Sanchez', 'rick@citadel.com');
INSERT INTO customers(ID, FIRST_NAME, LAST_NAME, EMAIL) VALUES (1033, 'Bob', 'Belcher', 'bob@burgerboss.com');
```

Update customer records:

```sql
UPDATE CUSTOMERS SET email = 'homer.simpson@gmail.com' WHERE id = 1031;
UPDATE CUSTOMERS SET email = 'rick.sanchez@gmail.com' WHERE id = 1023;
UPDATE CUSTOMERS SET email = 'bob.belcher@gmail.com', first_name='Robert' WHERE id = 1033;
```

Update a product description:

```sql
UPDATE PRODUCTS SET description = 'Razor Scooter' WHERE id = 101;
```

Insert an order:

```sql
INSERT INTO orders VALUES (NULL, '16-JAN-2025', 1001, 6, 101);
```

Delete a customer record:

```sql
DELETE FROM CUSTOMERS WHERE id = 1033;
```

### 6. Verify Kafka Topic Messages

Access the Kafka UI Dashboard to monitor messages:

1. Open your web browser and navigate to http://localhost:8081
2. Click on the "Topics" tab in the left sidebar
3. Find and click on the "test.DEBEZIUM.CUSTOMERS" topic
4. Select the "Messages" tab to view the change events in real-time

### 7. Create the PostgreSQL Sink Connector

Submit the PostgreSQL sink connector to replicate Oracle changes into PostgreSQL:

```powershell
.\submit-connector.ps1 -JsonFilePath ".\create-postgres-sink-connector.json"
```

Verify the connectors:

```powershell
.\log-connectors.ps1
```

Expected output:

```plaintext
source | oracle-source-connector | RUNNING | RUNNING | io.debezium.connector.oracle.OracleConnector
sink | postgres-sink-connector | RUNNING | RUNNING | io.debezium.connector.jdbc.JdbcSinkConnector
```

### 8. Verify Data in PostgreSQL

Access the PostgreSQL container:

```sh
docker exec -it postgres /bin/bash
```

Start the PostgreSQL CLI:

```sh
psql -U postgres
```

Check if customer data has been replicated:

```sql
SELECT * FROM customers;
```

### 9. Run the Quarkus Application

Run the Quarkus application which subscribes to the Kafka "customers" topic and processes the CDC events:

```sh
./mvnw compile quarkus:dev
```

## Conclusion

This guide outlines how to set up an Oracle database with Debezium, configure source and sink connectors, and verify real-time data streaming into PostgreSQL using Kafka. By following these steps, you can ensure data consistency across different databases using CDC (Change Data Capture).




