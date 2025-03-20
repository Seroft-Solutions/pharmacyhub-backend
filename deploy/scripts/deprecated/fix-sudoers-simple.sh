#!/bin/bash
set -e

echo "Creating a basic sudoers file with simplified syntax..."

# Try multiple syntax variations until one works
for SYNTAX in {1..4}; do
  echo "Trying syntax variation $SYNTAX..."
  
  if [ "$SYNTAX" -eq 1 ]; then
    # Variation 1: Basic commands without arguments
    cat > /tmp/sudoers.test << EOF
root ALL=(ALL) NOPASSWD: /usr/bin/chown
root ALL=(ALL) NOPASSWD: /usr/bin/chmod
EOF
  elif [ "$SYNTAX" -eq 2 ]; then
    # Variation 2: With spaces around equals sign
    cat > /tmp/sudoers.test << EOF
root ALL = (ALL) NOPASSWD: /usr/bin/chown
root ALL = (ALL) NOPASSWD: /usr/bin/chmod
EOF
  elif [ "$SYNTAX" -eq 3 ]; then
    # Variation 3: Without parentheses
    cat > /tmp/sudoers.test << EOF
root ALL = ALL NOPASSWD: /usr/bin/chown
root ALL = ALL NOPASSWD: /usr/bin/chmod
EOF
  elif [ "$SYNTAX" -eq 4 ]; then
    # Variation 4: Using Cmnd_Alias
    cat > /tmp/sudoers.test << EOF
Cmnd_Alias POSTGRES_COMMANDS = /usr/bin/chown -R 999:999 /opt/PharmacyHub/dev/data/postgres, /usr/bin/chown -R 999:999 /opt/PharmacyHub/qa/data/postgres, /usr/bin/chown -R 999:999 /opt/PharmacyHub/prod/data/postgres, /usr/bin/chmod 750 /opt/PharmacyHub/dev/data/postgres, /usr/bin/chmod 750 /opt/PharmacyHub/qa/data/postgres, /usr/bin/chmod 750 /opt/PharmacyHub/prod/data/postgres
root ALL=(ALL) NOPASSWD: POSTGRES_COMMANDS
EOF
  fi
  
  # Ensure file ends with newline
  echo "" >> /tmp/sudoers.test
  
  # Validate the syntax
  if visudo -cf /tmp/sudoers.test; then
    echo "Syntax variation $SYNTAX works!"
    
    # If we found a working syntax, create the actual file
    if [ "$SYNTAX" -eq 1 ]; then
      # For variation 1, expand to full commands now that we know basic syntax works
      cat > /tmp/pharmacyhub-postgres-sudoers << EOF
root ALL=(ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/dev/data/postgres
root ALL=(ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/qa/data/postgres
root ALL=(ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/prod/data/postgres
root ALL=(ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/dev/data/postgres
root ALL=(ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/qa/data/postgres
root ALL=(ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/prod/data/postgres

EOF
    elif [ "$SYNTAX" -eq 2 ]; then
      cat > /tmp/pharmacyhub-postgres-sudoers << EOF
root ALL = (ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/dev/data/postgres
root ALL = (ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/qa/data/postgres
root ALL = (ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/prod/data/postgres
root ALL = (ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/dev/data/postgres
root ALL = (ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/qa/data/postgres
root ALL = (ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/prod/data/postgres

EOF
    elif [ "$SYNTAX" -eq 3 ]; then
      cat > /tmp/pharmacyhub-postgres-sudoers << EOF
root ALL = ALL NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/dev/data/postgres
root ALL = ALL NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/qa/data/postgres
root ALL = ALL NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/prod/data/postgres
root ALL = ALL NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/dev/data/postgres
root ALL = ALL NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/qa/data/postgres
root ALL = ALL NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/prod/data/postgres

EOF
    elif [ "$SYNTAX" -eq 4 ]; then
      cat > /tmp/pharmacyhub-postgres-sudoers << EOF
Cmnd_Alias POSTGRES_COMMANDS = /usr/bin/chown -R 999:999 /opt/PharmacyHub/dev/data/postgres, /usr/bin/chown -R 999:999 /opt/PharmacyHub/qa/data/postgres, /usr/bin/chown -R 999:999 /opt/PharmacyHub/prod/data/postgres, /usr/bin/chmod 750 /opt/PharmacyHub/dev/data/postgres, /usr/bin/chmod 750 /opt/PharmacyHub/qa/data/postgres, /usr/bin/chmod 750 /opt/PharmacyHub/prod/data/postgres
root ALL=(ALL) NOPASSWD: POSTGRES_COMMANDS

EOF
    fi
    
    # Validate the expanded file
    if visudo -cf /tmp/pharmacyhub-postgres-sudoers; then
      echo "Full sudoers file validated successfully!"
      
      # Back up existing file if needed
      if [ -f /etc/sudoers.d/pharmacyhub-postgres ]; then
        cp /etc/sudoers.d/pharmacyhub-postgres /etc/sudoers.d/pharmacyhub-postgres.bak
      fi
      
      # Install the new file
      cp /tmp/pharmacyhub-postgres-sudoers /etc/sudoers.d/pharmacyhub-postgres
      chmod 440 /etc/sudoers.d/pharmacyhub-postgres
      
      echo "Updated sudoers file installed successfully:"
      cat /etc/sudoers.d/pharmacyhub-postgres
      break
    else
      echo "Full file validation failed, trying next syntax..."
    fi
  else
    echo "Syntax variation $SYNTAX failed, trying next..."
  fi
done

# Clean up
rm -f /tmp/sudoers.test
rm -f /tmp/pharmacyhub-postgres-sudoers

# Check if we fixed it
if [ -f /etc/sudoers.d/pharmacyhub-postgres ]; then
  if visudo -cf /etc/sudoers.d/pharmacyhub-postgres; then
    echo "SUCCESS: Final sudoers file is valid!"
  else
    echo "ERROR: Final sudoers file is still invalid!"
  fi
else
  echo "ERROR: No sudoers file was created!"
fi
