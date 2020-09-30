#!/bin/sh

if [ -f "/var/tmp/ca.pem" ]; then
    keytool -importcert -keystore /etc/ssl/certs/java/cacerts -alias rh-ca -storepass changeit -file /var/tmp/ca.pem -noprompt
fi

exec /deployments/run-java.sh