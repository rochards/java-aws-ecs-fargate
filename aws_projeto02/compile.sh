#!/bin/bash

echo "==== Compilando projeto Java ===="
mvn clean package

echo ""
echo "==== Executando Dockerfile ===="
docker build -t rochards/java-app-aws-projeto02:2.1.0 .