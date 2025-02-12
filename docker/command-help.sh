#Login in Oracle
sqlplus debezium/dbz@//localhost:1521/freepdb1


# Create the Oracle connector with PowerShell
.\submit-connector.ps1 -JsonFilePath ".\create-oracle-source-connector.json"

# Create the Postgres connector with PowerShell
.\submit-connector.ps1 -JsonFilePath ".\create-postgres-sink-connector.json"

# Delete Oracle connector
curl -i -X DELETE http://localhost:8083/connectors/oracle-source-connector

# Delete Postgres connector
curl -i -X DELETE http://localhost:8083/connectors/postgres-sink-connector