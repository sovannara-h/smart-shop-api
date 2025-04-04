#!/bin/bash
# start-vault.sh

echo "Starting Vault in development mode..."

# Check if Vault is already running
if pgrep vault > /dev/null; then
    echo "Vault is already running. Stopping previous instance..."
    pkill vault
    sleep 2
fi

# Start Vault in the background
vault server -dev > vault.log 2>&1 &
VAULT_PID=$!

# Wait for Vault to start
sleep 3

# Extract the root token from the log
ROOT_TOKEN=$(grep "Root Token:" vault.log | cut -d' ' -f3)
echo "Root Token: $ROOT_TOKEN"

# Configure environment variables for the following commands
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN="$ROOT_TOKEN"

# Create or update the .env file
echo "VAULT_ADDR=http://127.0.0.1:8200" > .env
echo "VAULT_DEV_TOKEN=$ROOT_TOKEN" >> .env

# Check if Vault is accessible
vault status

# Create policy for the application
echo "Creating application policy..."
vault policy write spring-app - <<EOF
path "secret/jwt" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
path "secret/data/jwt" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

# Create a token for the application with this policy
echo "Creating application token..."
APP_TOKEN=$(vault token create -policy=spring-app -display-name=spring-dev -format=json | jq -r '.auth.client_token')
echo "Application Token: $APP_TOKEN"

# Update .env file with application token
sed -i '' "s/VAULT_DEV_TOKEN=.*/VAULT_DEV_TOKEN=$APP_TOKEN/" .env

# Create or update JWT secret
echo "Creating JWT secret..."
vault kv put secret/jwt current="jwt-secret-$(date +%s)"

echo "Vault started and configured successfully!"
echo "PID: $VAULT_PID - Use 'kill $VAULT_PID' to stop Vault"