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
