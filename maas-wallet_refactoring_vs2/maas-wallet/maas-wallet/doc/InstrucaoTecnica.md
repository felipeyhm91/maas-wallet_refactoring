# Roteiro para Execução Local

O sistema está atualmente em execução em segundo plano, mas se precisar reiniciá-lo, siga os passos abaixo no PowerShell a partir da pasta 
maas-wallet:

# Execute o script automatizado que configura o JDK 21 local e inicia o Maven:
./run.ps1

O servidor estará disponível em:

http://localhost:8080/index.html
Dashboard Interativo: http://localhost:8080/
Swagger UI (Documentação): http://localhost:8080/swagger-ui/index.html
H2 Database Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:maasdb, User: sa, Senha em branco/sa).


Verificação Manual
Iniciar o Spring Boot localmente (mvn spring-boot:run ou ./run.ps1).
Acessar a documentação da API Swagger em: http://localhost:8080/swagger-ui/index.html para certificar que todos os endpoints estão listados e documentados.
Acessar o dashboard interativo em: http://localhost:8080/index.html.
Realizar o fluxo completo no dashboard:
Cadastrar um novo passageiro.
Efetuar login e obter o token JWT.
Visualizar saldo inicial (R$ 0,00).
Efetuar uma recarga Pix simulada (ex.: R$ 100,00).
Buscar uma rota multimodal entre coordenadas (ex.: Origem: Av. Paulista, Destino: Parque Ibirapuera).
Selecionar uma opção de transporte, gerar cotação, reservar a viagem, e confirmar seu uso via simulador de webhook do parceiro.
Verificar se o débito ocorreu no ledger, o extrato foi atualizado e o cashback foi creditado.