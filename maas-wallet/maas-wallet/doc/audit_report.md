# Technical Audit Report – maas-wallet

---

## 1. Visão Geral da Arquitetura

![Component diagram](file:///C:/Users/felip/.gemini/antigravity-ide/brain/8c0a5565-123c-4b64-a59d-3dbc15656720/component_diagram_1782363791735.png)

| Aspect | Observação |
|--------|------------|
| **Tipo** | Monolítico Spring Boot 3.x, camada `web → service → repository → DB`. |
| **Estrutura de pacotes** | `br.com.maaswallet` → sub‑pacotes **auth**, **wallet**, **trip**, **rewards**, **config**, **adapters** (in/out), **domain**, **ports**. |
| **Camadas** | • **Controllers** (`@RestController`) – API REST.<br>• **Adapters** (In/Out) – entrada‑saída.<br>• **Domain** – modelos de negócio.<br>• **Ports** – interfaces de comunicação. |
| **Configurações** | `application.yml` define Spring Boot, Spring Doc, Vault, JWT, limites de recarga, perfis dev. |
| **Gerenciamento de dependências** | Maven (`pom.xml`) – Spring Boot 3.3.0, Resilience4j, Vault, Spring Security, JPA, Flyway, PostgreSQL, H2, Redis, Kafka, MapStruct, Lombok, ArchUnit, Testcontainers. |
| **Infra** | *Banco*: PostgreSQL (runtime), H2 (test).<br>*Cache*: Redis (Lettuce).<br>*Segurança*: JWT, Spring Security, Vault (secrets).<br>*Mensageria*: Kafka.<br>*Migrações*: Flyway. |
| **Observabilidade** | Actuator, Spring Doc/OpenAPI UI. |
| **Resiliência** | Resilience4j circuit‑breaker / retry. |

---

## 2. Mapa de Componentes
```
maas-wallet (Spring Boot)
│
├─ **auth**
│   ├─ adapters.in.web.AuthController
│   ├─ adapters.out (out adapters)
│   ├─ domain.model (User, UserStatus, UserType)
│   └─ domain.service (AuthService, etc.)
│
├─ **wallet**
│   ├─ adapters.in.web.WalletController
│   └─ domain.* (Wallet, Balance, Transaction)
│
├─ **trip**
│   ├─ adapters.in.web.TripController
│   └─ domain.* (Trip, Route, …)
│
├─ **rewards**
│   ├─ adapters.in.web.RewardsController
│   └─ domain.* (Reward, Points)
│
├─ **config**
│   ├─ AdminController
│   ├─ SecurityConfiguration & JwtRequestFilter
│   ├─ GlobalExceptionHandler
│   └─ DomainConfiguration (Beans, MapStruct, …)
│
└─ **adapters** (generic)
    ├─ in (web controllers)
    └─ out (persistence, external APIs)
```

---

## 3. Mapa de Dependências
| Tipo | Bibliotecas / Versões | Uso |
|------|----------------------|-----|
| **Spring Boot** | 3.3.0 | Core framework |
| **Security** | spring‑boot‑starter‑security | JWT, auth filters |
| **Web** | spring‑boot‑starter‑web | REST API |
| **Validation** | spring‑boot‑starter‑validation | Bean validation |
| **Actuator** | spring‑boot‑starter‑actuator | Health / metrics |
| **Data JPA** | spring‑boot‑starter‑data‑jpa | ORM |
| **Flyway** | flyway‑core, flyway‑database‑postgresql | DB migrations |
| **Database Drivers** | postgresql (runtime), h2 (test) | Persistência |
| **Cache** | spring‑boot‑starter‑data‑redis (Lettuce) | Caching |
| **Vault** | spring‑cloud‑starter‑vault‑config | Secrets |
| **Kafka** | spring‑boot‑starter‑kafka, kafka‑clients | Messaging |
| **Resilience4j** | resilience4j‑spring‑boot3 | Circuit‑breaker |
| **MapStruct** | mapstruct, mapstruct‑processor | DTO ↔︎ Entity mapping |
| **Lombok** | lombok, lombok‑mapstruct‑binding | Boilerplate reduction |
| **OpenAPI** | springdoc‑openapi‑starter‑webmvc‑ui | API docs |
| **JWT** | java‑jwt | Token generation/validation |
| **ArchUnit** | archunit‑junit5 | Architecture tests |
| **Testcontainers** | testcontainers‑postgresql, junit‑jupiter | Integration tests |

---

## 4. Fluxo de Dados (Critical Business Flow)
1. **Cliente → API** – (`/api/v1/wallet/...`) via **AuthController** (JWT validated by `JwtRequestFilter`).
2. **Controller** → **Service** (domain logic, often in adapters.in).
3. **Service** → **Repository** (JPA) → **PostgreSQL** (persistência).
4. **Service** may call **Cache** (Redis) for quick balance reads.
5. **Events** – After a transaction, a **Kafka** message is produced to downstream services (e.g., rewards).
6. **Vault** supplies secret keys (JWT secret, DB credentials) at startup.
7. **Flyway** runs migrations on startup, ensuring DB schema version.

---

## 5. Principais Gargalos
| Área | Evidência | Impacto |
|------|-----------|---------|
| **Ausência de `@Service`/`@Repository`** | `grep @Service` & `@Repository` returned no results – likely services are plain classes or inside adapters. May hinder Spring's component scanning and AOP (transactions, caching). | Reduz reutilização, dificulta testes e monitoramento. |
| **Hard‑coded Vault token** | `application.yml` contains `token: dummy-token`. | Risco de exposição de credenciais e falha em ambientes reais. |
| **Configuração de H2 em runtime** | H2 dependency scoped `runtime`. | Possível uso acidental de banco em memória em produção. |
| **Resilience4j não configurado** | Biblioteca presente, mas sem política de circuit‑breaker. | Falta de proteção contra falhas de serviços externos (Kafka, Vault). |
| **Controladores contendo lógica de negócio** | Controllers importam diretamente domain classes e executam cálculos. | Viola o princípio **Single Responsibility**, aumenta acoplamento. |
| **Grandes classes de configuração** (`GlobalExceptionHandler`, `SecurityConfiguration`) | Cada uma acima de 3 KB. | Dificulta manutenção e evoluções independentes. |

---

## 6. Principais Riscos
| Risco | Descrição | Mitigação Recomendada |
|-------|-----------|-----------------------|
| **Segurança dos segredos** | Token do Vault hard‑coded; JWT secret pode estar em código. | Mover segredos exclusivamente para Vault, usar placeholders `${vault.jwt.secret}`. |
| **Acoplamento elevado** | Pacotes expõem diretamente *domain* nos controladores. | Aplicar **Ports‑and‑Adapters (Hexagonal)**: controladores dependem de interfaces (ports) e não de implementações internas. |
| **Violação de SOLID** | `GlobalExceptionHandler` e `SecurityConfiguration` concentram múltiplas responsabilidades. | Refatorar separando: `ExceptionMapper`, `SecurityFilters`, etc. |
| **Obsolescência de API** | Dependência de `spring‑cloud‑vault` pode estar desatualizada frente ao Boot 3.3. | Atualizar Spring Cloud para versão compatível, validar integração. |
| **Performance de consultas JPA** | Ausência de repositórios explícitos → risco de N+1 e consultas não otimizadas. | Criar interfaces `@Repository`, usar `EntityGraph` ou `fetch=LAZY` apropriado. |
| **Falta de monitoramento de falhas** | Resilience4j presente mas não configurado; risco de queda em chamadas externas. | Definir políticas de retry/circuit‑breaker e incluir testes de falha. |
| **Dependência de H2 em produção** | H2 no classpath com escopo runtime pode ser escolhido acidentalmente. | Remover H2 do runtime, deixar apenas como `test` scope. |

---

## 7. Diagnóstico de Dívida Técnica (Resumo)
| Categoria | Exemplos Identificados |
|-----------|------------------------|
| **Code Smells** | • Classes > 3 KB (`GlobalExceptionHandler`, `SecurityConfiguration`).<br>• Controladores com lógica de negócio (não só delegação). |
| **Violação de Padrões** | • Falta de anotações `@Service`/`@Repository`.<br>• Segredos hard‑coded no `application.yml`. |
| **Obsolescência** | • Loops manuais em serviços (substituir por Streams).<br>• Dependência de Lombok pode dificultar builds sem annotation processing. |
| **Acoplamento Excessivo** | • Controllers importam diretamente *domain* e *repository* classes. |
| **Cobertura de Testes** | • Ausência de testes de integração para fluxos críticos (wallet, trip, rewards). |

---

## 8. Plano de Refatoração & Execução (Resumo)
| Problema | Técnica Aplicada | Prompt/Comando de Refatoração (exemplo) |
|----------|------------------|----------------------------------------|
| Falta de `@Service`/`@Repository` | Criar interfaces + anotações, mover lógica para Service layer | `refactor create Service class AuthService with @Service annotation` |
| Segredos hard‑coded | Externalizar para Vault, remover propriedades estáticas | `update application.yml to use ${vault.jwt.secret}` |
| Controladores com lógica de negócio | Extrair serviços (Strategy/Facade) | `extract method processPayment from WalletController to PaymentService` |
| Classes gigantes (`GlobalExceptionHandler`, `SecurityConfiguration`) | Dividir em múltiplas classes (ExceptionMapper, SecurityFilters) | `split GlobalExceptionHandler into ValidationExceptionHandler, BusinessExceptionHandler` |
| Resilience4j não configurado | Definir circuit‑breaker e retry policies | `add @CircuitBreaker(name="vault", fallbackMethod="fallbackVault")` |
| Falta de testes de integração | Implementar testes com Testcontainers (Postgres, Kafka) | `create integration test WalletControllerIT using @SpringBootTest` |
| Dependência de H2 em runtime | Remover H2 from runtime, keep test scope | `remove H2 dependency from runtime scope in pom.xml` |
| Código duplicado / loops manuais | Substituir por Java Stream API | `replace for-loop in RewardService with stream().map(...).collect(...)` |

*Cada prompt será inserido como comentário no código‑fonte para rastreabilidade.*

---

## 9. Próximos Passos
1. **Validar o diagrama** – está incluído acima.  
2. **Aplicar o plano** – executar os prompts de refatoração listados.  
3. **Gerar artefatos adicionais** – documentação PDF, diagramas detalhados se necessário.  
4. **Re‑executar testes** – garantir cobertura ≥ 80 % e execução bem‑sucedida dos testes de integração.  
5. **Revisão final** – entrega do relatório completo ao time de arquitetura.

---

*Este relatório foi gerado automaticamente por um agente de IA especializado em auditoria de aplicações Java Spring.*
