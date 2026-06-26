# Promp 01 - Contexto 

Criação do contexto técnico do Projeto (.ai/)

Você está operando em Agent Mode, com permissão para criar arquivos e escrever documentação.
Atue como um Staff Engineer / Tech Lead Java, responsável por preparar o contexto técnico e arquitetural de um projeto que será implementado posteriormente por outro agente.
Pense como se estivesse escrevendo documentação interna para um time real.

Objetivo: Criar uma pasta .ai/ na raiz do projeto contendo documentos claros, prescritivos e não genéricos, que definem. Esses arquivos não são explicativos, eles definem regras que devem ser seguidas. 
 - Padrões de código
 - Decisões arquiteturais
 - Stack tecnológica aprovada
 - Regras de negócio e domínio

Estrutura obrigatória, Criar exatamente a seguinte estrutura:
.ai/
├── standards.md
├── architecture.md
├── tech-stack.md
└── business-rules.md

INSTRUÇÕES DE INICIO:  
- Para preenchimento das "instruções detalhadas", Avalie e Entenda o documento do Projeto, arquivo anexo DocumentaçãoProjeto.pdf
- Ponto importante para o tópico "11. Prompts Utilizados para Criar a Arquitetura", onde consta especificado os Prompts utilizados no Projeto.  

INSTRUÇÕES DETALHADAS: 
.ai/standards.md — Convenções e Padrões de código e estilo
  - Defina regras concretas
  - Descreva de forma detalhada. 

.ai/architecture.md — Arquitetura e Desisões de alto nivel (ADRs) 
  - Descreva De forma detalhada a Arquitetura. 

.ai/tech-stack.md — Stack tecnológica (PRESCRITIVO)
  - Versoes e libs permitidas
  - Defina explicitamente.

.ai/business-rules.md — Lógica de negocio e dominio, Regras de negócio
  - Documente regras claras:
  - Não inclua Codigo
  - Descreva de forma detalhada. 

REGRAS FINAIS 
 - Escreva os documentos pensando em outro agente
 - Seja específico e direto
 - Não use frases genéricas
 - Não antecipe implementação

ENTREGRAVEL
 - Pasta .ai/ criada
 - Quatro arquivos preenchidos conforme especificado
 - Ao final, apresente:
 - Lista dos arquivos criados
 - um resumo objetivo de cada documento