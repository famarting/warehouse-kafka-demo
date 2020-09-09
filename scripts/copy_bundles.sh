#!/bin/bash

SERVICES=$@

for svc in ${SERVICES};
do
    echo ${svc}
    cp -r ${svc}/deployment_bundle/. deployment_bundle/
done