# Modernização de Sistemas Legados com Agentes de IA

---------------------------------------------------------------------------------------
# Prompt 1 - Diagnóstico de Dívida Técnica, Descoberta da Arquitetura 

``` text
Atue como um Enterprise Software Architect, Software Modernization Specialist e Tech Lead Sênior.
Analise todo o repositório cnsiderando a arquitetura, código-fonte, dependências, banco de dados e integrações.

## Arquitetura, Identifique:
- Arquitetura atual utilizada
- Estilo arquitetural
- Componentes principais da solução
- Dependências internas e externas
- Tecnologias utilizadas
- Integrações
- Bancos de dados
- Fluxos críticos de negócio
- Fluxo de chamadas
- Camadas existentes
- Dependências entre módulos
- Componentes principais
- Diagrama textual da arquitetura

## Tecnologias
- Liste todas as tecnologias encontradas.
- versão
- finalidade
- dependências
- nível de obsolescência

## Módulos, Liste todos os módulos existentes.Para cada módulo informe:
- responsabilidade
- dependências
- acoplamento
- complexidade

Monte um relatório executivo contendo:
1. Visão Geral da Arquitetura
2. Mapa de Componentes
3. Mapa de Dependências
4. Fluxo de Dados
5. Principais Gargalos
6. Principais Riscos

Importante: Não altere nenhum código.

```

---------------------------------------------------------------------------------------

# Prompt 2 - Auditoria de Dívida Técnica

``` text
Atue como um Enterprise Software Architect, Software Modernization Specialist e Tech Lead Sênior.
Analise todo o repositório cnsiderando a arquitetura, código-fonte, dependências, banco de dados e integrações.

Realize uma Auditoria Técnica completa e identifique: 

## Code Smells
Long Method
Large Class
God Object
Duplicated Code
Feature Envy
Magic Numbers
Dead Code
Long Parameter List
Primitive Obsession

## Violações SOLID
SRP
OCP
LSP
ISP
DIP

Explique:

- Localização
- Impacto
- Como corrigir

## Arquitetura

Controllers
Services
Repositories
DTOs
Entities

## Antipatterns

God Class
Anemic Domain
Transaction Script
Spaghetti Code
Blob

## Obsolescência

APIs deprecated
Java legado
Loops substituíveis por Streams
Records
Pattern Matching
Virtual Threads

## Performance
N+1
Consultas lentas
Cache
Objetos desnecessários

## Segurança
OWASP Top 10
SQL Injection
XSS
CSRF
Secrets Hardcoded

Gere um relatório contendo:

Problema
Severidade
Localização
Impacto
Prioridade
Sugestão de correção
Estimativa de esforço

Importante: Não altere nenhum código.
```

------------------------------------------------------------------------

# Prompt 3 - Auditoria da Suíte de Testes

``` text
Atue como QA Architect e Analise toda a suíte de testes.
Identifique:

- Cobertura
- Classes sem testes
- Métodos críticos
- Testes frágeis
- Duplicação
- Dependências externas

Avalie:

JUnit
Mockito
TestContainers
WireMock
SpringBootTest
MockMvc
RestAssured

Informe:

Cobertura estimada
Riscos
Prioridades
Plano de melhoria

Não implemente testes.
```

------------------------------------------------------------------------

# Prompt 4 - Geração da Suíte de Testes

``` text
Atue como QA Automation Engineer e Implemente testes utilizando:

- AAA
- JUnit 5
- Mockito
- TestContainers
- WireMock

Criar testes para:

Controllers
Services
Repositories
Validações
Casos de erro
Casos extremos
Casos felizes

Meta: Cobertura mínima de 90%.

Ao final informe:

- Cobertura adicionada
- Classes testadas
- Casos pendentes
```

------------------------------------------------------------------------

# Prompt 5 - Plano de Modernização

``` text
Atue como Enterprise Architect e Elabore um plano completo de modernização.
Para cada melhoria informe:

Problema
Objetivo
Benefício
Impacto
Risco
Esforço
Prioridade
Dependências

Considere:
Java moderno
Spring Boot atualizado
Spring Security
Records
Streams
Pattern Matching
Switch Expressions
Virtual Threads
OpenAPI
Observabilidade
CI/CD
OpenRewrite
DDD
Arquitetura Hexagonal
Clean Architecture

Monte um roadmap em ordem de execução.
```

------------------------------------------------------------------------

# Prompt 6 - Refatoração

``` text
Atue como Desenvolvedor Sênior especializado em Refactoring.
Refatore o código seguindo:

SOLID
Clean Code
DRY
KISS
YAGNI
Tell Don't Ask
Law of Demeter

- Não altere comportamento funcional.
- Ao final informe obrigatoriamente:
  1- Problema encontrado
  2- Técnica aplicada
  3 - Benefícios
  4 - Impacto
  5 - Riscos
Antes x Depois
Melhorias futuras
```

-------------------------------------------------------------------------------------------------------------------

# Melhorias de refatoração adicionais 

Atue como um Enterprise Software Architect, Software Modernization Specialist e Tech Lead Sênior.

Execute: 
- Testes de integração	- Verificar fluxo completo (controller → service → repository) e geração de JWT.	Média

Centralizar versões em propriedades - Criar propriedades <spring-cloud.version>, <resilience4j.version>, <kafka.version> no pom.xml para facilitar upgrades futuros.	Média]

- Revisão de mensagens de exceção	Garantir que mensagens de erro não exponham detalhes internos (ex.: “Documento já cadastrado”).	Baixa

- Documentar bean AuthService - Atualizar diagramas de arquitetura e README para refletir novo fluxo.


-------------------------------------------------------------------------------------------------------------------


# Prompt 8 - Relatório de Modernização - Auditoria Pós-Refatoração

``` text
Atue como Software Quality Architect e Compare o sistema antes e depois.
Gere um relatório em Markdown contendo:

Avalie:

Arquitetura
Complexidade
Acoplamento
Coesão
Performance
Segurança
Cobertura de testes
Manutenibilidade

# Plano de Refatoração
Para cada problema:
 - Problema
 - Técnica aplicada
 - Prompt utilizado
 - Arquivos alterados

Informe:
 - Benefícios
 - Melhorias
 - Pendências
 - Riscos
 - Nota inicial
 - Nota final
```

# Diagnóstico

- Code Smells
- Violações SOLID
- Problemas arquiteturais
- Obsolescência

# Testes
 - Cobertura antes
 - Cobertura depois

# Resultado Final

Arquitetura
Performance
Segurança
Qualidade
Manutenibilidade
```