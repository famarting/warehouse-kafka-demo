#!/bin/bash
NUM_OF_ORDERS=${1:-10}
QUANTITY_PER_ORDER=${2:-1}

for i in $(seq 1 $NUM_OF_ORDERS)
do
    curl -i -d '{"itemId":"123456", "quantity":'"$QUANTITY_PER_ORDER"'}' -H "Content-Type: application/json" -X POST http://$(oc get route warehouse-service --template='{{ .spec.host }}')/orders
    echo
done