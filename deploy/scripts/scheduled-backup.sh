#!/bin/bash
# scheduled-backup.sh
# Script for scheduled database backups with rotation

# Check if environment parameter is provided
if [ -z "$1" ]; then
  echo "Error: Environment not specified"
  echo "Usage: ./scheduled-backup.sh <environment> [backup-type]"
  echo "Environment: dev, qa, prod"
  echo "Backup types: daily (default), weekly, monthly"
  exit 1
fi

# Set environment variables
ENV="$1"
BACKUP_TYPE="${2:-daily}"  # Default to daily if not provided
CRM_BASE="/home/ubuntu/PharmacyHub"
ENV_DIR="$CRM_BASE/$ENV"
BACKUP_DIR="$ENV_DIR/data/backups/postgres"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DB_NAME="pharmacyhub_$ENV"
CONTAINER_NAME="pharmacyhub-postgres-$ENV"
CURRENT_DATE=$(date +"%Y-%m-%d")
CURRENT_DAY=$(date +"%d")
CURRENT_MONTH=$(date +"%m")
CURRENT_YEAR=$(date +"%Y")
CURRENT_DOW=$(date +"%u")  # Day of week (1-7), 1 is Monday

# Validate environment
if [ "$ENV" != "dev" ] && [ "$ENV" != "qa" ] && [ "$ENV" != "prod" ]; then
  echo "Error: Invalid environment. Must be one of: dev, qa, prod"
  exit 1
fi

# Ensure backup directories exist
mkdir -p "$BACKUP_DIR/daily"
mkdir -p "$BACKUP_DIR/weekly"
mkdir -p "$BACKUP_DIR/monthly"
mkdir -p "$BACKUP_DIR/yearly"

# Validate backup type
if [ "$BACKUP_TYPE" != "daily" ] && [ "$BACKUP_TYPE" != "weekly" ] && [ "$BACKUP_TYPE" != "monthly" ] && [ "$BACKUP_TYPE" != "yearly" ]; then
  echo "Error: Invalid backup type. Must be one of: daily, weekly, monthly, yearly"
  exit 1
fi

echo "Starting $BACKUP_TYPE backup for $ENV environment..."

# Check if container is running
if ! docker ps | grep -q "$CONTAINER_NAME"; then
  echo "Error: Database container '$CONTAINER_NAME' is not running."
  exit 1
fi

# Set backup filename based on type
if [ "$BACKUP_TYPE" == "daily" ]; then
  BACKUP_NAME="${DB_NAME}_daily_${CURRENT_DATE}"
  BACKUP_FILE="$BACKUP_DIR/daily/$BACKUP_NAME.sql"
  # Keep 7 days of daily backups
  RETENTION_DAYS=7
elif [ "$BACKUP_TYPE" == "weekly" ]; then
  # Only run weekly backup on Sundays (day 7)
  if [ "$CURRENT_DOW" != "7" ]; then
    echo "Weekly backup only runs on Sundays. Today is day $CURRENT_DOW of the week."
    exit 0
  fi
  WEEK_NUM=$(date +"%V")
  BACKUP_NAME="${DB_NAME}_weekly_${CURRENT_YEAR}_week${WEEK_NUM}"
  BACKUP_FILE="$BACKUP_DIR/weekly/$BACKUP_NAME.sql"
  # Keep 5 weeks of weekly backups
  RETENTION_DAYS=35
elif [ "$BACKUP_TYPE" == "monthly" ]; then
  # Only run monthly backup on the 1st day of the month
  if [ "$CURRENT_DAY" != "01" ]; then
    echo "Monthly backup only runs on the 1st day of the month. Today is day $CURRENT_DAY."
    exit 0
  fi
  MONTH_NAME=$(date +"%B")
  BACKUP_NAME="${DB_NAME}_monthly_${CURRENT_YEAR}_${MONTH_NAME}"
  BACKUP_FILE="$BACKUP_DIR/monthly/$BACKUP_NAME.sql"
  # Keep 12 months of monthly backups
  RETENTION_DAYS=365
elif [ "$BACKUP_TYPE" == "yearly" ]; then
  # Only run yearly backup on January 1st
  if [ "$CURRENT_MONTH" != "01" ] || [ "$CURRENT_DAY" != "01" ]; then
    echo "Yearly backup only runs on January 1st. Today is $CURRENT_MONTH-$CURRENT_DAY."
    exit 0
  fi
  BACKUP_NAME="${DB_NAME}_yearly_${CURRENT_YEAR}"
  BACKUP_FILE="$BACKUP_DIR/yearly/$BACKUP_NAME.sql"
  # Keep 5 years of yearly backups
  RETENTION_DAYS=1825
fi

echo "Creating $BACKUP_TYPE backup: $BACKUP_NAME"

# Create the backup
docker exec "$CONTAINER_NAME" pg_dump -U postgres "$DB_NAME" > "$BACKUP_FILE"

# Check if backup was successful
if [ $? -eq 0 ] && [ -s "$BACKUP_FILE" ]; then
  echo "Backup created successfully."
  # Compress backup
  gzip "$BACKUP_FILE"
  echo "Backup compressed: ${BACKUP_FILE}.gz"
  
  # Clean up old backups based on retention policy
  echo "Cleaning up old backups (retaining for $RETENTION_DAYS days)..."
  
  if [ "$BACKUP_TYPE" == "daily" ]; then
    find "$BACKUP_DIR/daily" -name "${DB_NAME}_daily_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
  elif [ "$BACKUP_TYPE" == "weekly" ]; then
    find "$BACKUP_DIR/weekly" -name "${DB_NAME}_weekly_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
  elif [ "$BACKUP_TYPE" == "monthly" ]; then
    find "$BACKUP_DIR/monthly" -name "${DB_NAME}_monthly_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
  elif [ "$BACKUP_TYPE" == "yearly" ]; then
    find "$BACKUP_DIR/yearly" -name "${DB_NAME}_yearly_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
  fi
  
  echo "$BACKUP_TYPE backup completed successfully!"
else
  echo "Error: Backup failed."
  rm -f "$BACKUP_FILE"  # Remove failed backup file
  exit 1
fi

# Display stats
BACKUP_COUNT=$(find "$BACKUP_DIR/$BACKUP_TYPE" -name "${DB_NAME}_${BACKUP_TYPE}_*.sql.gz" | wc -l)
echo "Current $BACKUP_TYPE backup count: $BACKUP_COUNT"
echo "Total backup size for $BACKUP_TYPE:"
du -sh "$BACKUP_DIR/$BACKUP_TYPE"
