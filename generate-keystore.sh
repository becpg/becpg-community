#!/usr/bin/env bash

set -e

# ---- Check arguments ----
if [ -z "$1" ]; then
  echo "Usage: $0 <becpg_version>"
  echo "Example: $0 becpg_25_3_0"
  exit 1
fi

# ---- Parameters ----
BECPG_VERSION="$1"
CONTAINER="${BECPG_VERSION}-becpg-1"

# ---- Configuration ----
KEY_ALIAS="becpg-sign"
KEYSTORE_PATH="/usr/local/tomcat/shared/classes/alfresco/extension/keystore"
KEYSTORE_FILE="${KEYSTORE_PATH}/keystore"
STORE_PASS="REDACTED"
KEY_PASS="REDACTED"
VALIDITY_DAYS=365

DNAME="CN=Valentin Ledev, OU=dev, O=beCPG, L=Paris, ST=France, C=FR"

# ---- Create keystore directory with correct permissions ----
echo "Creating keystore directory with proper permissions in container ${CONTAINER}..."
docker exec -u root "${CONTAINER}" bash -c "
  mkdir -p ${KEYSTORE_PATH} &&
  chown -R alfresco:alfresco ${KEYSTORE_PATH} &&
  chmod -R 750 ${KEYSTORE_PATH}
"

# ---- Generate keypair ----
echo "Generating keystore and keypair..."
docker exec -u alfresco "${CONTAINER}" bash -c "
  keytool -genkeypair \
    -alias ${KEY_ALIAS} \
    -validity ${VALIDITY_DAYS} \
    -keyalg RSA \
    -keysize 2048 \
    -keypass ${KEY_PASS} \
    -storetype JCEKS \
    -keystore ${KEYSTORE_FILE} \
    -storepass ${STORE_PASS} \
    -dname \"${DNAME}\"
"

echo "Keystore successfully generated at:"
echo "  ${KEYSTORE_FILE}"
