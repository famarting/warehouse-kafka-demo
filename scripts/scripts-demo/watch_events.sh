#!/bin/bash

curl http://$(oc get route warehouse-service --template='{{ .spec.host }}')/warehouse/events