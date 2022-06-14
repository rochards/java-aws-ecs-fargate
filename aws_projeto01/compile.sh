#!/bin/bash

echo "==== Compilando projeto Java ===="
mvn clean package

echo ""
echo "==== Executando Dockerfile ===="
docker build -t rochards/java-app-aws-projeto01:latest .