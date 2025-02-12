#!/bin/bash
set -e

MARKER_FILE="/opt/oracle/oradata/.setup_done"

if [ ! -f "$MARKER_FILE" ]; then
    echo "Running initial Oracle setup..."

    mkdir -p /opt/oracle/oradata/recovery_area

    sh ./oracle_logminer-setup.sh
    cat oracle-load-data.sql | sqlplus debezium/dbz@//localhost:1521/freepdb1

    touch "$MARKER_FILE"
    echo "Setup complete."
else
    echo "Skipping setup, already completed."
fi
