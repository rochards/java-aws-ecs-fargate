

Este repositório é o resultado do curso [Criando microsserviços em Java com AWS ECS Fargate e AWS CDK](https://www.udemy.com/course/aws-ecs-fargate-java/) que realizei.

Inicializar um projeto cdk com java: `cdk init app --language java`. Obs.: o nome da pasta sob a qual o comando acima é executado, determina o nome da aplicação criada.


### Recursos/Serviços utilizados no curso

* **Amazon VPC**: ou também *Amazon Virtual Private Cloud*, permite criar uma rede virtual onde os componentes dentro dela podem ficar isolados e protegidos de acessos externos. No entanto, portas e protocolos podem ser abertos para a internet por meio de regras de segurança;

* **Amazon ECS + Fargate**: o *Elastic Container Service* é um serviço de orquestração de *containers*. O *Fargate* vem para para nos livrar da preocupação de gerenciar máquinas EC2 para o funcionamento do ECS;

* **Aplication Load Balancer**: recurso que permite dividir as requisições entre as instâncias das aplicações. Também foi configurado um **_target group_** para monitorar a saúde das instâncias em execução, assim o *load balancer* pode decidir parar de enviar requesições para instâncias "não saudáveis";

* **SNS**: o *Simple Notification Service* é um serviço de mensagens totalmente gerenciado pela AWS. Um *publisher* ou publicador, envia suas mensagens para o SNS que posteriormente serão consumidas por um ou mais *subscribers* ou assinantes;

* **SQS**: o *Simple Queue Service* é ums serviço de fila totalmente gerenciado pela AWS. Um *publisher* envia mensagens para a fila e um *consumer* precisa ficar consultando essa fila para verificar a existência de novas mensagens. O SQS suporta dois tipos de filas: [standard](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/standard-queues.html) e [FIFO](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues.html). Neste projeto utilizamos a *standard*. Em sistemas em que a duplicidade de mensagens seria um problema, a FIFO resolveria essa questão;

* **RDS**: 

* **DynamoDB**: é um serviço de banco de dados NoSQL altamente escalável gerenciado pela AWS. Uma **tabela** no DynamoDB é uma coleção de itens, cada **item** por sua vez é uma coleção de **atributos**. Os dados na tabela podem ser unicamente identificado utilizando a combinação de uma *partition key* e uma *sort key*, essa última é opcional;

* **S3**: o *Simple Storage Service* é um serviço totalmente gerenciado pela AWS para armazenamento de arquivos. O S3 armazena dados como objetos dentro de *buckets*. Um *bucket*, por sua vez é formado por um arquivo e metadados (sendo esse opcional) que descrevem esse arquivo.


### Alguns conceitos

* **CloudFormation**: serviço que permite modelar, provisionar e gerenciar recursos da AWS e de terceiros ao tratar infraestrutura como código, do inglês *Infrastructure as Code* (IaC). Você submete um *template* (arquivo .json ou .yaml) ao CloudFormation

* **Task definition**: é a definição (receita) de como a nossa aplicação vai ser executada a partir da imagem Docker, definindo recursos computacionais como CPU e memória, configurações de variáveis de ambiente, etc;

* **Service**: tem a função de garantir execução das *tasks* mantendo a quantidade correta de instâncias em execução. Também controla o processo de *deployment* de novas versões da *task definition*;

* **Auto Scaling**: permite aumentar ou diminuir o número de instâncias da aplicação em execução de acordo com alguma métrica definida, como utilização de CPU;

* **IAM Policy**: *policy* é a forma existente na AWS para gerenciar permissões nas contas. Ex.: definir quais usuários tem acesso de escrita em um determinado *bucket*. As *policies* podem ser anexadas a usuários, grupo de usuários e *roles*. As *policies* são documentos no formato JSON;

* **IAM Role**: é um conjunto de *policies*.

### Como executar a aplicação aws_projeto01 na máquina local com o docker

- Execute o arquivo `compile.sh` que está na raiz do projeto;
  - OBS.: esse arquivo executa os testes do *maven*, então é preciso ter um banco de dados local para não ser lançada uma exceção ao subir a aplicação Spring.
- Execute o arquivo `run-container.sh` também presente na raiz do projeto.
- Na raiz do projeto se encontram as *collections* do Postman para entender quais os endpoints disponíveis na aplicação.

### Como fazer o *deploy* da infraestrutura, descrita na pasta `aws_cdk_infra`, na AWS

- `$ cdk bootstrap`: cria as *roles* com as permissões necessárias para que o CDK faça *deploy* dos recursos;
- `$ cdk deploy nome_stack1 nome_stack2`: faz o *deploy* das *stacks* na sua conta AWS;
- `$ cdk destroy nome_stack1 nome_stack2`: apaga as *stacks* listadas da sua conta AWS;
- `$ cdk list`: lista as *stacks* que existem dentro do seu projeto;
- `$ cdk diff`: mostra a diferença entre a sua *stack* definida pelo seu código local e os recursos criados na AWS.


### O que fui observando durante o curso

- O primeiro comando que deve ser executado é o `$ cdk bootstrap`. Será criada uma *stack* **CDKToolkit** no CloudFormation. Tal *stack* cria várias roles para cdk acessar os recursos de SSM, IAM, ECR. Ex. de *role*: `cdk-hnb123fds-lookup-role-000000000000-sa-east-1`. Os zeros representam o número da sua conta.

- O comando `$ cdk deploy Vpc` com as linhas de código abaixo
    ```java
    Vpc.Builder.create(this, "Vpc01")
        .maxAzs(3)
        .build();
    ``` 
    cria várias recursos por *default*:
    - ***Subnets*** públicas e privadas: uma *subnet* ou sub-rede é uma rede menor dentro da sua rede maior, sendo essa última a VPC. Você utiliza a *subnet* pública para criar recursos que devem ter conexão com a internet, e *subnet* privada para aqueles que não serão conectados à internet;
    - ***Route Tables***: uma *route table* contém um conjunto de regras (rotas) que determinam para onde o tráfego da sua *subnet* ou *gateway* é direcionado;
    - ***NATGateway***: um NAT (Network Addres Translation) gateway na AWS é um serviço que permite instâncias numa *subnet* privada se conectarem com serviços externos à sua VPC, porém esses serviços externos não conseguem iniciar uma conexão com as suas instâncias;
    - ***InternetGateway***: é um componente que permite comunicação entre sua VPC e a internet. Um internet *gateway* permite que recursos, como instâncias EC2, em sua *subnet* pública se conectem à internet. Por outro lado, permite também que suas instâncias recebem conexões da internet, por exemplo, você pode se conectar uma instância EC2 na AWS utilizando seu computador pessoal.

- Quando criar a *stack* do serviço, por exemplo o `Service01`, ao final é apresentado no console o DNS para vc fazer chamadas ao serviço, ex.: `http://Servi-ALB01-XXXXXXXXXX-0000000000.sa-east-1.elb.amazonaws.com`. Basta fazer a chamada passando a porta `8080`;

- Caso esteja executando  o `aws_projeto01` no Intellij IDEA, podemos passar as variáveis de ambiente pela própria IDE para testes locais. Utilizei as seguintes variáveis, que estão definidas no `application.properties` do projeto: `SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/aws_projeto01?createDatabaseIfNotExist=true;SPRING_DATASOURCE_USERNAME=root;SPRING_DATASOURCE_PASSWORD=root`;
- Para subir um MySQL local no Docker, podemos fazer `$ docker run --name localdb -e MYSQL_ROOT_PASSWORD=root -d -p 3306:3306 mysql:5.7`. 
   - OBS.: em versões mais recentes do MySQL há um erro de chave RSA -> https://github.com/metabase/metabase/issues/12545;

- Há umas classes de configurações no projeto aws_projeto01 para que ele seja executado no *localstack*. Faz-se necessário passar a variável de ambiente `SPRING_PROFILES_ACTIVE=local` para o Intellij;
- Para executar o *localstack* no Docker, basta: `$ docker run --rm -p 4566:4566 -p 4571:4571 localstack/localstack -e "SERVICES=sns,sqs,dynamodb,s3"`. Dentro da variável `SERVICES` estão os serviços que o projeto precisa.

### Fontes:
- https://docs.aws.amazon.com/vpc/latest/userguide/configure-subnets.html
- https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html
- https://docs.aws.amazon.com/vpc/latest/userguide/vpc-nat-gateway.html
- https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Internet_Gateway.html