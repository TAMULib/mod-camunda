#!/usr/bin/env bash

echo "Deploying TestProcess1"
curl -X POST -H "X-Okapi-Tenant: tamu" -F "tenant-id=tamu" -F "deployment-name=TestProcess1" -F "deployment-source=process application" -F "data=@../src/main/resources/workflows/TestProcess1.bpmn" http://localhost:9000/camunda/deployment/create
echo

echo "Deploying TestProcess2"
curl -X POST -H "X-Okapi-Tenant: tamu" -F "tenant-id=tamu" -F "deployment-name=TestProcess2" -F "deployment-source=process application" -F "data=@../src/main/resources/workflows/TestProcess2.bpmn" http://localhost:9000/camunda/deployment/create
echo

echo "Deploying TestMasterProcess"
curl -X POST -H "X-Okapi-Tenant: tamu" -F "tenant-id=tamu" -F "deployment-name=TestMasterProcess" -F "deployment-source=process application" -F "data=@../src/main/resources/workflows/TestMasterProcess.bpmn" http://localhost:9000/camunda/deployment/create
echo

echo "Deploying Decision1"
curl -X POST -H "X-Okapi-Tenant: tamu" -F "tenant-id=tamu" -F "deployment-name=Decision1" -F "deployment-source=decision model" -F "data=@../src/main/resources/decisions/Decision1.dmn" http://localhost:9000/camunda/deployment/create
echo
