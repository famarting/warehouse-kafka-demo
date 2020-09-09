#!/bin/bash
QUANTITY=${1:-5}

curl -i -d '{"item-id":"123456", "quantity":'"$QUANTITY"'}' -H "Content-Type: application/json" -X POST http://$(oc get route warehouse-service --template='{{ .spec.host }}')/warehouse/stocks
echo