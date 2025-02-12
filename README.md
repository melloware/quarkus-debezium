# Quarkus Debezium Consumer

This project demonstrates Change Data Capture (CDC) using Debezium with Oracle as the source database and Kafka Streams for processing customer data changes. It includes a Quarkus application that consumes Debezium CDC events, processes them using Kafka Streams, and can optionally sync the data to PostgreSQL.

## Architecture

The application:
- Captures changes from Oracle DB using Debezium Oracle Connector
- Processes CDC events through Kafka Streams
- Tracks customer record changes (creates, updates, deletes)
- Uses Javers for detailed change tracking on updates
- Can sync changes to PostgreSQL using Debezium JDBC Sink Connector

## Use Cases
- Real-time data synchronization between databases such as Oracle and PostgreSQL
- CDC pipeline for event-driven applications and microservices
- Sending small payloads of change data through a WebSocket to the UI to render only what needs to be updated
- Third party application can stream event driven data changes from Kafka

## Running the Stack

This guide provides step-by-step instructions for setting up an Oracle database with Debezium, configuring source and sink connectors, and verifying data flow using Kafka and PostgreSQL.

## Prerequisites

- Docker and Docker Compose installed
- PowerShell for executing connector scripts
- Oracle and PostgreSQL database images set up in Docker

## Steps

### 1. Start the Docker Containers

Bring up the required services using Docker Compose:

```sh
docker compose up
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
source | oracle-customer-source-connector-00 | RUNNING | RUNNING | io.debezium.connector.oracle.OracleConnector
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
INSERT INTO customers(ID, FIRST_NAME, LAST_NAME, EMAIL) VALUES (1033, 'Jerry', 'Smith', 'jerry.smith@nevertrying.org');
```

Update customer records:

```sql
UPDATE CUSTOMERS SET email = 'homer.simpson@gmail.com' WHERE id = 1031;
UPDATE CUSTOMERS SET email = 'rick.sanchez@gmail.com' WHERE id = 1023;
UPDATE CUSTOMERS SET email = 'jerry.smith@gmail.com', last_name='Smith Jr.' WHERE id = 1033;
```

Update a product description:

```sql
UPDATE PRODUCTS SET description = 'Test Scooter Change' WHERE id = 101;
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

Pull the Kafka message consumer tool:

```sh
docker pull edenhill/kcat:1.7.1
```

Run `kcat` to check for changes in the `test.DEBEZIUM.CUSTOMERS` topic:

```sh
docker run --rm --tty --network debezium-oracle_default edenhill/kcat:1.7.1 -b kafka:9092 -C -t test.DEBEZIUM.CUSTOMERS
```

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
sink | postgres-sink-connector | RUNNING | RUNNING | io.debezium.connector.jdbc.JdbcSinkConnector
source | oracle-customer-source-connector-00 | RUNNING | RUNNING | io.debezium.connector.oracle.OracleConnector
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

## Conclusion

This guide outlines how to set up an Oracle database with Debezium, configure source and sink connectors, and verify real-time data streaming into PostgreSQL using Kafka. By following these steps, you can ensure data consistency across different databases using CDC (Change Data Capture).


