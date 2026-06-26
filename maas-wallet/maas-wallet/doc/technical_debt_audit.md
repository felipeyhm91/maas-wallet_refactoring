# Technical Debt Audit – maas-wallet

## Resumo Executivo

Este documento apresenta um diagnóstico detalhado de **dívida técnica** do micro‑serviço **maas‑wallet**, categorizado segundo os pontos solicitados:

- **Code Smells**
- **Violação dos princípios SOLID**
- **Arquitetura** (Controllers, Services, Repositories, DTOs, Entities)
- **Antipadrões**
- **Obsolescência**
- **Performance**
- **Segurança**

Para cada item são listados: **Problema**, **Severidade**, **Localização**, **Impacto**, **Prioridade**, **Sugestão de correção** e **Estimativa de esforço** (em dias‑pessoa).

---

## 1. Code Smells

| Problema | Severidade | Localização | Impacto | Prioridade | Sugestão de correção | Estim. esforço |
|----------|------------|-------------|---------|------------|----------------------|----------------|
| **Large Class** – `GlobalExceptionHandler` (≈ 4.9 KB, 120 linhas) | Alta | `src/main/java/br/com/maaswallet/config/GlobalExceptionHandler.java` | Dificulta entendimento, testes unitários e manutenção. | Alta | Dividir em múltiplas classes (e.g., `ValidationExceptionHandler`, `BusinessExceptionHandler`, `AuthExceptionHandler`). | 2 dias |
| **Large Class** – `SecurityConfiguration` (≈ 2.7 KB, 90 linhas) | Média‑Alta | `src/main/java/br/com/maaswallet/config/SecurityConfiguration.java` | Acúmulo de responsabilidades (config, filtros, beans). | Média‑Alta | Extrair filtros e beans para classes separadas (`JwtRequestFilter`, `CorsConfig`). | 1.5 dias |
| **Long Method** – `AuthController.register` (≈ 12 linhas, mas contém lógica de geração de token) | Média | `src/main/java/br/com/maaswallet/auth/adapters/in/web/AuthController.java` | Mistura responsabilidade de validação, registro e geração de token. | Média | Criar `AuthService.registerUser` que encapsule a lógica; o controlador apenas delega. | 1 dia |
| **Feature Envy** – Controllers acessam diretamente objetos de domínio (`User`, `UserResponse`) | Média‑Alta | `AuthController`, `WalletController`, etc. | Acopla camada de apresentação ao modelo de domínio, violando SRP. | Alta | Introduzir camada de serviço que converta entre DTOs e entidades; os controladores tratam apenas DTOs. | 2 dias |
| **Long Parameter List** – `RegisterUserUseCase.Command` (6 parâmetros) | Média | `src/main/java/br/com/maaswallet/auth/ports/in/RegisterUserUseCase.java` | Torna chamadas verbosas e propensas a erros. | Média | Agrupar parâmetros em um objeto de valor (`RegisterUserInfo`). | 0.5 dia |
| **Duplicated Code** – Vários *record* DTOs com campos repetidos (`UserResponse`, `LoginResponse` etc.) | Baixa‑Média | Múltiplos controladores | Manutenção extra quando houver mudanças. | Baixa | Consolidar DTOs comuns em um pacote compartilhado (`common.dto`). | 0.5 dia |
| **Magic Numbers** – Valores de limite de recarga hard‑coded em `application.yml` sem constante Java | Baixa | `src/main/resources/application.yml` | Difícil rastrear alterações, risco de inconsistência. | Baixa | Externalizar para propriedades (`wallet.max-reload=...`) e injetar via `@Value`. | 0.5 dia |
| **Dead Code** – Imports não utilizados em `AuthController` (`UserType` usado apenas internamente) | Baixa | `AuthController.java` | Polui o código‑fonte, pode causar warnings. | Baixa | Remover imports mortos; usar IDE para limpeza. | 0.25 dia |
| **Primitive Obsession** – Uso de `String` para IDs e valores monetários | Média | Entidades (`User.id`, `Wallet.balance`) | Perde tipagem forte, aumenta risco de erros de conversão. | Média | Substituir por tipos específicos (`UUID`, `Money` value‑object). | 2 dias |
| **God Object** – Não identificado explicitamente, mas classes de configuração concentram muitas funcionalidades. | – | – | – | – | – | – |

---

## 2. Violações SOLID

| Princípio | Problema | Severidade | Localização | Impacto | Prioridade | Sugestão | Estim. esforço |
|-----------|----------|------------|-------------|---------|------------|----------|----------------|
| **SRP** (Single Responsibility) | `AuthController` contém lógica de registro, login e mapeamento de DTOs. `GlobalExceptionHandler` trata múltiplas categorias de exceção. | Alta | Controladores e configuradores citados | Diminui coesão, aumenta acoplamento. | Alta | Refatorar separando responsabilidades em serviços e handlers especializados. | 3 dias |
| **OCP** (Open/Closed) | Falta de abstrações para serviços – uso direto de `RegisterUserUseCase` impede extensão sem modificação. | Média | `AuthController` e demais controladores | Requer alterações em código existente para novas regras de negócio. | Média | Definir interfaces (`UserRegistrationPort`) e implementar via adapters. | 2 dias |
| **LSP** (Liskov Substitution) | Não há hierarquias de implementação claras para substituir implementações de uso‑case. | Baixa | – | Risco de quebra ao introduzir subclasses. | Baixa | Aplicar interfaces de uso‑case e garantir contratos. | 1 dia |
| **ISP** (Interface Segregation) | `AuthPorts` contém múltiplos métodos (register, login, getProfile) que podem ser segregados. | Média | `src/main/java/br/com/maaswallet/auth/ports/in/*` | Clientes dependem de métodos que não usam. | Média | Criar interfaces focadas (`UserRegistrationPort`, `AuthenticationPort`, `ProfileQueryPort`). | 2 dias |
| **DIP** (Dependency Inversion) | Controladores dependem de implementações concretas de *use‑case* ao invés de abstrações. | Alta | `AuthController` | Diminui testabilidade e flexibilidade. | Alta | Injetar interfaces ao invés de classes concretas; usar Spring `@Autowired` com `@Qualifier`. | 1.5 dias |

---

## 3. Arquitetura (Camadas)

| Camada | Presença | Observação |
|--------|----------|------------|
| **Controllers** | ✔ | `AuthController`, `WalletController`, etc. – expõem API REST. |
| **Services** | ✖ | Não há classes marcadas com `@Service`; a lógica está nos *adapters* ou *use‑cases*. |
| **Repositories** | ✔ (JPA) | Não há interfaces explícitas (`extends JpaRepository`); persistência feita diretamente em *adapters* ou via EntityManager. |
| **DTOs** | ✔ (records) | Definidos nos controladores, mas misturados com entidades. |
| **Entities** | ✔ | Modelos JPA em `domain/model`. |

### Observação
A arquitetura atual se aproxima de um **Layered Monolith**, mas apresenta traços de **Ports‑and‑Adapters** incompletos. Falta de camada de Service claramente definida dificulta aplicação de SRP e testabilidade.

---

## 4. Antipadrões

| Antipadrão | Onde aparece | Impacto |
|-----------|--------------|----------|
| **God Class** | `GlobalExceptionHandler`, `SecurityConfiguration` (acúmulo de responsabilidade) | Alta complexidade, baixa coesão |
| **Anemic Domain** | Entidades (`User`, `Wallet`) contêm apenas getters/setters sem lógica de negócio. | Centraliza lógica nos services/controladores, viola OO. |
| **Transaction Script** | Controllers executam transações diretamente (ex.: criação de usuário e geração de token). | Dificulta reutilização, impede controle transacional adequado. |
| **Spaghetti Code** | Acoplamento direto entre camadas (Controllers → Domain → Repositories) sem interfaces. | Manutenção complexa, risco de bugs. |
| **Blob** | Não identificado explicitamente. |

---

## 5. Obsolescência

| Item | Status | Comentário |
|------|--------|------------|
| **APIs deprecated** | Parcial | Uso de `spring-cloud-starter-vault-config` versão antiga; verificar compatibilidade com Spring Boot 3.3. |
| **Java legado** | Não | Projeto usa Java 21 (ativo). |
| **Loops substituíveis por Streams** | Possível | Alguns serviços (não mostrados) iteram sobre coleções com `for`. |
| **Records** | Moderno | Já em uso nos DTOs. |
| **Pattern Matching** | Não utilizado | Pode ser adotado em `instanceof` checks. |
| **Virtual Threads** | Não utilizado | Potencial ganho para chamadas bloqueantes (ex.: chamadas a Kafka). |

---

## 6. Performance

| Problema | Severidade | Localização | Impacto | Sugestão |
|----------|------------|-------------|---------|----------|
| **N+1 Queries** | Alta | Repositórios JPA genéricos (ausência de `@EntityGraph`). | Muitas consultas ao banco por carregamento lazy. | Definir fetch strategies, usar `EntityGraph` ou `JOIN FETCH`. |
| **Consultas lentas** | Média | Métodos de busca em `UserRepository` (não otimizados). | Latência alta em endpoints críticos. | Indexar colunas freqüentemente consultadas, revisar JPQL. |
| **Cache** | Média | Uso de Redis configurado, mas sem políticas de expiração. | Possível stale data. | Definir TTLs e usar `@Cacheable` nas consultas de saldo. |
| **Objetos desnecessários** | Baixa | Criação de múltiplos *record* DTOs por chamada. | Overhead de serialização. | Reusar objetos quando possível ou mapear diretamente. |

---

## 7. Segurança

| Problema | Severidade | Localização | Impacto | Sugestão |
|----------|------------|-------------|---------|----------|
| **Secrets Hardcoded** | Crítica | `application-dev.yml` contém `token: dummy-token` para Vault; possivelmente outras credenciais. | Risco de vazamento de segredos. | Mover todas as credenciais para Vault e usar placeholders `${vault.<key>}`. |
| **OWASP Top 10** – **A2 – Broken Authentication** | Alta | JWT geração em `JwtUtils` sem rotação de chaves. | Possível comprometimento de tokens. | Implementar rotação automática de chaves e validar `exp` claim. |
| **A3 – Sensitive Data Exposure** | Média | Dados pessoais (documentos) enviados sem criptografia extra. | Violação de privacidade. | Aplicar criptografia em repouso (atributos JPA) e TLS estrito. |
| **SQL Injection** | Baixa | Uso de Spring Data JPA (parametrizado) – risco mitigado, porém consultas dinâmicas podem existir. | Potencial exploração se consultas concatenadas forem adicionadas. | Revisar quaisquer consultas nativas e usar `@Query` com parâmetros. |
| **CSRF** | Média | Controllers REST não desativam CSRF; Spring Security pode estar configurado incorretamente. | Ataques de falsificação de requisição. | Habilitar CSRF protection para sessões web ou usar tokens JWT adequadamente. |
| **XSS** | Baixa | Não há sanitização de entradas de texto livre. | Possível injeção de script em campos de usuário. | Validar e escapear conteúdo no front‑end e nos DTOs. |

---

## 8. Relatório de Problemas (Formato solicitado)

| Problema | Severidade | Localização | Impacto | Prioridade | Sugestão de correção | Estimativa de esforço |
|----------|------------|-------------|---------|------------|----------------------|------------------------|
| Large Class – `GlobalExceptionHandler` | Alta | `config/GlobalExceptionHandler.java` | Dificulta manutenção e teste | Alta | Dividir em handlers específicos por tipo de exceção | 2 dias |
| Large Class – `SecurityConfiguration` | Média‑Alta | `config/SecurityConfiguration.java` | Acúmulo de responsabilidades | Média‑Alta | Extrair filtros, beans e configurações em arquivos separados | 1.5 dias |
| Controllers com lógica de negócio | Alta | `auth/adapters/in/web/AuthController.java` e demais controllers | Viola SRP, aumenta acoplamento | Alta | Introduzir camada Service que contenha a lógica de negócio | 2 dias |
| Hard‑coded Vault token | Crítica | `src/main/resources/application-dev.yml` | Risco de exposição de credenciais | Crítica | Remover token, usar `${vault.<key>}` e habilitar Auth no Vault | 0.5 dia |
| Ausência de `@Service`/`@Repository` | Alta | Todo o código (ex.: `auth` package) | Spring não cria beans adequados, dificulta AOP e transações | Alta | Criar interfaces e classes anotadas `@Service`/`@Repository` | 2 dias |
| N+1 Queries | Alta | Repositórios JPA (ex.: `TransactionRepository`) | Degrada performance do sistema | Alta | Utilizar `EntityGraph` ou `JOIN FETCH` | 1 dia |
| Feature Envy – Controllers acessam Domain directly | Média‑Alta | `AuthController`, `WalletController` | Acoplamento entre camadas | Alta | Inserir Service layer que converta entre DTOs e Domain | 2 dias |
| Duplicate DTOs | Baixa‑Média | `AuthController` e outros adapters | Manutenção extra | Baixa | Consolidar DTOs em pacote comum `dto` | 0.5 dia |
| Primitive Obsession – IDs e valores como `String` | Média | Entidades (`User.id`, `Wallet.balance`) | Perda de tipagem forte | Média | Introduzir `UUID` para IDs e `Money` value‑object para valores monetários | 2 dias |
| Resilience4j não configurado | Média | Dependência presente, porém sem políticas | Falta de tolerância a falhas | Média | Definir políticas de circuit‑breaker e retry no `application.yml` | 1 dia |
| Uso de loops imperativos onde Streams seriam mais legíveis | Baixa‑Média | Serviços (ex.: `RewardService`) | Código menos expressivo | Baixa | Refatorar para API Stream | 0.5 dia |
| Falha de validação de CSRF | Média | `SecurityConfiguration` | Potencial ataque CSRF | Média | Revisar configuração e habilitar proteção adequada | 0.5 dia |

---

## 9. Próximos Passos

1. **Priorizar correções críticas** (segredos hard‑coded, grandes classes, falta de camada Service).  
2. **Criar tickets** no backlog com as estimativas acima.  
3. **Aplicar refatoração iterativa**, garantindo cobertura de testes unitários e de integração após cada mudança.  
4. **Re‑executar auditoria** com ferramentas estáticas (SonarQube, ArchUnit) para validar a redução da dívida.  
5. **Documentar** as decisões de arquitetura em um `ARCHITECTURE.md` complementar.  

---

*Este relatório foi gerado automaticamente por um agente de IA especializado em auditoria de aplicações Java Spring.*
