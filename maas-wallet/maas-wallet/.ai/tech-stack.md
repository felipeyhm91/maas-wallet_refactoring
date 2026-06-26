# Stack Tecnológica Aprovada — MaaS Wallet

Este documento define de forma prescritiva a stack de tecnologia, frameworks, bibliotecas permitidas, versões mínimas e bibliotecas proibidas para o desenvolvimento do backend da plataforma MaaS Wallet. Nenhuma tecnologia fora desta lista pode ser introduzida sem a aprovação do Comitê de Arquitetura.

---

## 1. Tecnologias Core

| Tecnologia | Versão Aprovada | Finalidade | Raciocínio Técnico |
| :--- | :--- | :--- | :--- |
| **Java** | 21 (LTS) | Linguagem de programação | Permite o uso de **Virtual Threads** (Project Loom) para processamento eficiente e simultâneo de requisições I/O-bound (cotações de múltiplos parceiros concorrentemente). |
| **Spring Boot** | 3.3.x | Framework base do sistema | Versão estável com suporte nativo a Java 21, Spring Framework 6 e Jakarta EE. |
| **Maven** | 3.9.x | Gerenciador de dependências e build | Ferramenta padrão para ciclo de vida de compilação e empacotamento do monolito modular. |

---

## 2. Banco de Dados e Persistência

### 2.1. Persistência Principal
* **PostgreSQL 16**: Utilizado para todas as transações, dados cadastrais e o Ledger financeiro.
* **Driver JDBC**: `org.postgresql:postgresql` (versão gerenciada pelo Spring Boot Parent).
* **Flyway 10.x**: Utilizado obrigatoriamente para migrações e controle de versão de schemas lógicos do banco. Migrações manuais via SQL direto no banco de dados de produção são estritamente proibidas.

### 2.2. Cache e Controle de Estado Efêmero
* **Redis 7.x**: Utilizado para cache de cotações de rotas (TTL curto), armazenamento de tokens expirados (blacklist), controle de concorrência e chaves de idempotência de transações.
* **Biblioteca Cliente**: Lettuce (inclusa em `spring-boot-starter-data-redis`).

---

## 3. Bibliotecas e Frameworks Secundários Permitidos

### 3.1. Integração e Clientes HTTP
* **Spring RestClient**: Escolha padrão para chamadas síncronas bloqueantes a APIs externas (aproveitando a eficiência de Virtual Threads).
* **Spring WebClient**: Permitido para integrações assíncronas reativas.

### 3.2. Mapeamento e Utilitários de Código
* **MapStruct 1.5.x**: Obrigatório para mapeamento bidirecional de dados (DTO para Entidade de Domínio; Entidade de Domínio para Entidade JPA).
* **Lombok 1.18.x**: Permitido para reduzir boilerplate.
  * *Uso obrigatório*: `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Builder` e `@Slf4j`.
  * *Uso proibido*: `@Data` (gera `hashCode` e `equals` instáveis em relacionamentos JPA lazy loading).

### 3.3. API e Documentação
* **Springdoc OpenAPI 2.x**: Geração automatizada de documentação Swagger com base no padrão OpenAPI 3.0 (`org.springdoc:springdoc-openapi-starter-webmvc-ui`).

### 3.4. Segurança e Criptografia
* **Spring Security 6.x**: Controle de autenticação e RBAC.
* **Auth0 Java JWT 4.4.x**: Criação, assinatura e decodificação local de JSON Web Tokens (JWT).
* **BCrypt**: Algoritmo obrigatório para hash de senhas de usuários (`BCryptPasswordEncoder` com custo de força de trabalho definido em `12`).

---

## 4. Stack de Testes

| Biblioteca | Versão Aprovada | Finalidade |
| :--- | :--- | :--- |
| **JUnit 5 (Jupiter)** | 5.10.x | Engine principal de execução de testes unitários e de integração. |
| **Mockito 5.x** | 5.11.x | Criação de mocks e stubs para isolamento de lógica em testes unitários. |
| **AssertJ 3.x** | 3.25.x | Escrita de asserções fluídas, legíveis e fortemente tipadas. |
| **Testcontainers 1.19.x** | 1.19.x | Inicialização de containers reais PostgreSQL e Redis durante a fase de testes de integração (`mvn verify`). |
| **ArchUnit 1.2.x** | 1.2.x | Testes automatizados para validação do isolamento da arquitetura hexagonal (ex.: garantir que a camada `domain` não conhece a camada `adapters`). |

---

## 5. Diretrizes e Tecnologias Proibidas

* **PROIBIDO Spring Data REST**: Toda API REST deve ter seus controladores escritos e validados manualmente para garantir conformidade estrita com o padrão de rotas definido nas regras de negócio.
* **PROIBIDO Bancos de Dados NoSQL para Transações**: Toda a persistência da carteira e do ledger deve estar sob a governança ACID do PostgreSQL. Redis só pode ser usado para dados que podem ser perdidos e recalculados sem prejuízo financeiro.
* **PROIBIDO java.util.Date e java.util.Calendar**: Use estritamente as classes da API de tempo moderna do Java (`java.time.Instant`, `java.time.OffsetDateTime`, `java.time.LocalDate`). Timestamps gravados no banco de dados devem utilizar a precisão UTC.
* **PROIBIDO Injeção de Atributos com `@Autowired`**: Conforme o documento de padrões, toda dependência deve ser injetada via construtor do Java.
* **PROIBIDO Compartilhamento de Dependências Externas na Camada de Domínio**: O domínio não pode importar nenhuma classe que comece com `org.springframework.*`, `jakarta.persistence.*`, ou dependências de mapeamento como MapStruct e Jackson. O domínio deve ser Java puro.
