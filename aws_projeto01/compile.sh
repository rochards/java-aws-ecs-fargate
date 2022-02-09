#!/usr/bin/zsh

echo "==== Compilando projeto Java ===="
mvn clean package

echo ""
echo "==== Executando Dockerfile ===="
docker build -t rochards/java-app-aws-projeto01:1.0.0 .