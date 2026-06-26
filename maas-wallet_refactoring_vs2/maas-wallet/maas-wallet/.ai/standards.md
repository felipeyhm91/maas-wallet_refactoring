# Convenções e Padrões de Código — MaaS Wallet

Este documento estabelece as regras e padrões de codificação obrigatórios para o backend Java da plataforma MaaS Wallet. Todo desenvolvedor e agente de IA deve seguir rigorosamente as definições abaixo para manter a consistência, segurança e qualidade do código.

---

## 1. Convenções de Nomeação

### 1.1. Linguagem e Escrita
* **Língua**: Todo o código (nomes de classes, métodos, variáveis, comentários e documentação) deve ser escrito em **inglês**.
* **Termos de Domínio**: Devem seguir o glossário de negócios oficial (ex.: `Wallet`, `Ledger`, `Transaction`, `Trip`, `Partner`, `CashbackCampaign`).

### 1.2. Padrões de Caixa (Casing)
* **Classes, Interfaces, Enums e Records**: `PascalCase` (ex.: `WalletService`, `TransactionType`, `RouteResponse`).
* **Métodos e Variáveis**: `camelCase` (ex.: `calculateCashback()`, `availableBalance`).
* **Constantes (`public static final`)**: `UPPER_SNAKE_CASE` (ex.: `MAXIMUM_RECHARGE_LIMIT`).
* **Pacotes**: Apenas letras minúsculas, sem caracteres especiais ou underscores (ex.: `br.com.maaswallet.wallet.domain`).

### 1.3. Sufixos de Classes
* **DTOs de Entrada (Request)**: `*Request` (ex.: `RechargeRequest`).
* **DTOs de Saída (Response)**: `*Response` (ex.: `TransactionResponse`).
* **Entidades JPA**: `*Entity` (ex.: `WalletEntity` - para separar do modelo de domínio puro).
* **Portas de Entrada (Use Cases)**: Interface `*UseCase` (ex.: `CreateTripUseCase`).
* **Portas de Saída (SPIs)**: Interface `*Port` (ex.: `WalletRepositoryPort`).
* **Adaptadores**: `*Adapter` (ex.: `PostgresWalletRepositoryAdapter`).

---

## 2. Estrutura de Pacotes (Arquitetura Hexagonal)

Cada domínio/módulo deve seguir a separação clássica de Portas e Adaptadores para garantir o desacoplamento do framework (Spring Boot):

```
br.com.maaswallet.[modulo]
├── domain/                      # Regras de negócio puras (sem dependências do Spring)
│   ├── model/                   # Entidades de domínio puras, Value Objects e Enums
│   └── exception/               # Exceções exclusivas do domínio
├── ports/                       # Portas de comunicação
│   ├── in/                      # Interfaces de Casos de Uso (Usecases/Input Ports)
│   └── out/                     # Interfaces de SPIs (Output Ports, Repositories, Clientes HTTP)
└── adapters/                    # Implementações concretas de tecnologia
    ├── in/                      # Controladores REST, Webhooks, listeners de mensageria
    │   └── web/                 # RestControllers, DTOs de Request/Response
    └── out/                     # Banco de dados, integrações de parceiros
        ├── persistence/         # Repositories JPA, Entidades JPA, Mapeadores
        └── integration/         # Clientes Feign/WebClient para Uber, SPTrans, etc.
```

---

## 3. Diretrizes de Design de Código Java

### 3.1. Java 21 e Imutabilidade
* **Uso de Records**: DTOs de API, payloads de eventos e Value Objects do domínio devem ser implementados como `record`.
* **Modificador `final`**: Todas as variáveis locais e parâmetros de métodos devem ser declarados como `final`.
* **Coleções Imutáveis**: Retorne coleções envelopadas em `List.copyOf()`, `Set.copyOf()` ou `Collections.unmodifiableList()` para evitar efeitos colaterais de mutabilidade.

### 3.2. Injeção de Dependências
* **Constructor Injection**: É obrigatório o uso de injeção por construtor.
* **Proibição do `@Autowired` em atributos**: Nunca utilize `@Autowired` em campos de classe.
* **Uso do Lombok**: Utilize `@RequiredArgsConstructor` do Lombok em adaptadores/serviços para gerar o construtor automaticamente.

```java
// Exemplo Correto
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
}
```

### 3.3. Tratamento de Exceções
* **Exceções de Domínio**: Devem herdar de `RuntimeException` (exceções não-checadas) e não devem conter metadados HTTP.
* **Mapeamento REST**: O adaptador de entrada REST deve capturar as exceções via `@RestControllerAdvice` e convertê-las em respostas estruturadas de acordo com a **RFC 7807 (Problem Details)**.
* **Logs**: Toda exceção mapeada para erro 5xx deve ser logada com nível `ERROR` e stack trace completo. Erros de validação (4xx) devem ser logados em nível `WARN` ou `INFO` sem stack trace.

### 3.4. Validação de Dados
* Use Bean Validation (`jakarta.validation.constraints.*`) nos DTOs de entrada do Controller (ex.: `@NotNull`, `@NotBlank`, `@Positive`).
* O Controller deve usar a anotação `@Valid` para disparar a validação antes da chamada ao caso de uso.

---

## 4. Padrões de Testes

### 4.1. Tipos de Testes e Ferramentas
* **Testes Unitários**: Obrigatórios para toda lógica de negócio (`domain/model` e `ports/in` use cases). Usar **JUnit 5**, **Mockito** e **AssertJ**.
* **Testes de Integração**: Obrigatórios para validar adaptadores de persistência e integrações externas. Usar **Spring Boot Test** com **Testcontainers** para instanciar PostgreSQL e Redis reais em containers Docker.

### 4.2. Estrutura e Nomeação de Testes
* O nome da classe de teste deve corresponder à classe sob teste com o sufixo `Test` (ex.: `WalletServiceTest`).
* Padrão de nome de método de teste: `should[Comportamento]When[Condicao]`.
* Estrutura interna: Padrão **AAA (Arrange, Act, Assert)** ou **BDD (Given, When, Then)** com separação visual por quebra de linha.

```java
@Test
void shouldDebitBalanceWhenSufficientFundsExist() {
    // Given (Arrange)
    final var wallet = createWalletWithBalance(BigDecimal.valueOf(100.00));
    final var debitAmount = BigDecimal.valueOf(30.00);
    
    // When (Act)
    wallet.debit(debitAmount);
    
    // Then (Assert)
    assertThat(wallet.getAvailableBalance()).isEqualByComparingTo(BigDecimal.valueOf(70.00));
}
```

### 4.3. Cobertura Mínima
* A cobertura de código mínima exigida para build em produção é de **80% de linhas de código**, medida através da ferramenta **JaCoCo**.
* Classes de configuração de frameworks, DTOs e entidades JPA puras podem ser excluídas da métrica do JaCoCo.

---

## 5. Qualidade e Análise Estática

* **Checkstyle**: Seguir as regras do Google Java Style Guide.
* **Lombok**: Permitido apenas para redução de boilerplate em DTOs, Entidades JPA e classes de serviço (ex.: `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Builder`). Proibido o uso indiscriminado de `@Data` em entidades JPA devido a problemas com `hashCode()` e `equals()` cíclicos gerados pelo Hibernate.
* **MapStruct**: Obrigatório para mapeamento entre DTOs, Modelos de Domínio e Entidades JPA. Proibido mapeamento manual repetitivo.