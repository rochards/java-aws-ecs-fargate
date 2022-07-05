

Inicializar um projeto cdk com java: `cdk init app --language java`. Obs.: o nome da pasta sob a qual o comando acima é executado, determina o nome da aplicação criada.


### Recursos criados no curso

* **Amazon VPC**: ou também *Amazon Virtual Private Cloud*, permite criar uma rede virtual onde os componentes dentro dela podem ficar isolados e protegidos de acessos externos. No entanto, portas e protocolos podem ser abertos para a internet por meio de regras de segurança;

* **Amazon ECS + Fargate**: o *Elastic Container Service* é um serviço de orquestração de *containers*. O *Fargate* vem para para nos livrar da preocupação de gerenciar máquinas EC2 para o funcionamento do ECS;

* **Aplication Load Balancer**: recurso que permite dividir as requisições entre as instâncias das aplicações. Também foi configurado um **_target group_** para monitorar a saúde das instâncias em execução, assim o *load balancer* pode decidir parar de enviar requesições para instâncias "não saudáveis".


### Alguns conceitos

* **CloudFormation**: serviço que permite modelar, provisionar e gerenciar recursos da AWS e de terceiros ao tratar infraestrutura como código, do inglês *Infrastructure as Code* (IaC). Você submete um *template* (arquivo .json ou .yaml) ao CloudFormation

* **Task definition**: é a definição (receita) de como a nossa aplicação vai ser executada a partir da imagem Docker, definindo recursos computacionais como CPU e memória, configurações de variáveis de ambiente, etc;

* **Service**: tem a função de garantir execução das *tasks* mantendo a quantidade correta de instâncias em execução. Também controla o processo de *deployment* de novas versões da *task definition*;

* **Auto Scaling**: permite aumentar ou diminuir o número de instâncias da aplicação em execução de acordo com alguma métrica definida, como utilização de CPU;

* **IAM Policy**: *policy* é a forma existente na AWS para gerenciar permissões nas contas. Ex.: definir quais usuários tem acesso de escrita em um determinado *bucket*. As *policies* podem ser anexadas a usuários, grupo de usuários e *roles*. As *policies* são documentos no formato JSON;

* **IAM Role**: é um conjunto de *policies*.

### Como executar a aplicação aws_projeto01 na máquina local com o docker

- Execute o arquivo `compile.sh` que está na raiz do projeto;
- Execute o arquivo `run-container.sh` também presente na raiz do projeto.

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


### Fontes:
- https://docs.aws.amazon.com/vpc/latest/userguide/configure-subnets.html
- https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html
- https://docs.aws.amazon.com/vpc/latest/userguide/vpc-nat-gateway.html
- https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Internet_Gateway.html