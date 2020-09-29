#!/bin/bash

if [ -f "/cachain/ca.pem" ]; then
    keytool -importcert -keystore /etc/pki/java/cacerts -alias rh-ca -storepass changeit -file /cachain/ca.pem -noprompt
fi

./deployments/run-java.sh