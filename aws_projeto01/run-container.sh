#!/usr/bin/zsh
echo "==== Certifique-se de ja ter executado o arquivo compile.sh ===="
echo "==== Executando container ===="
docker run --name java-app-aws-projeto01 -p 8080:8080 -d rochards/java-app-aws-projeto01:1.0.0