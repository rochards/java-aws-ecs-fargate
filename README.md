

Inicializar um projeto cdk com java: `cdk init app --language java`. Obs.: o nome da pasta sob a qual o comando acima é executado, determina o nome da aplicação criada.


### Recursos criados no curso

* **Amazon VPC**: ou também *Amazon Virtual Private Cloud*, permite criar uma rede virtual onde os componentes dentro dela podem ficar isolados e protegidos de acessos externos. No entanto, portas e protocolos podem ser abertos para a internet por meio de regras de segurança;

* **Amazon ECS + Fargate**: o *Elastic Container Service* é um serviço de orquestração de *containers*. O Fargate vem para para nos livrar da preocupação de gerenciar máquinas EC2 para o funcionamento do ECS.

* **Aplication Load Balancer**: recurso que permite dividir as requisições entre as instâncias das aplicações. Também foi configurado um **_target group_** para monitorar a saúde das instâncias em execução, assim o *load balancer* pode decidir parar de enviar requesições para instâncias "não saudáveis"


### Alguns conceitos

* **Task definition**: é a definição de como a nossa aplicação vai ser executada a partir da imagem Docker, definindo recursos computacionais como CPU e memória, configurações de variáveis de ambiente, etc;

* **Service**: tem a função de garantir execução das *tasks* mantendo a quantidade correta de instâncias em execução. Também controla o processo de *deployment* de novas versões da definição da *task definition*;

* **Auto Scaling**: permite aumentar ou diminuir o número de instâncias da aplicação em execução de acordo com alguma métrica definida, como utilização de CPU.