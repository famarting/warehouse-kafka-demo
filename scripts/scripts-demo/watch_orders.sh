#!/bin/bash

curl -i http://$(oc get route warehouse-service --template='{{ .spec.host }}')/warehouse/orders