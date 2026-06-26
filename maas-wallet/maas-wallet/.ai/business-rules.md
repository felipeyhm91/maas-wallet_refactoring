# Regras de Negócio e Domínio — MaaS Wallet

Este documento descreve as regras de negócio, o modelo de domínio conceitual, a matriz de acessos e os fluxos lógicos da plataforma MaaS Wallet. Estas regras são prescritivas e devem ser implementadas sem desvios.

---

## 1. Glossário de Entidades e Atributos

### 1.1. Usuário (User)
* **Definição**: Pessoa física ou jurídica cadastrada na plataforma.
* **Atributos de Negócio**: Identificador único, Nome completo/Razão social, Documento (CPF/CNPJ), E-mail, Senha (criptografada), Tipo de Usuário e Status do Usuário.
* **Tipos de Usuários**:
  * **Passageiro**: Usuário final que consome serviços de mobilidade.
  * **Administrador MaaS**: Funcionário interno da plataforma com plenos poderes operacionais.
  * **Parceiro de Mobilidade**: Representante de uma operadora de transporte externa.
  * **Operador de Atendimento**: Analista de suporte da central de atendimento.
  * **Gestor Corporativo**: Representante de uma empresa que distribui créditos para funcionários.
* **Status**: `Ativo` (uso normal), `Bloqueado` (impedido de transacionar por razões de segurança), `Inativo` (exclusão lógica).

### 1.2. Carteira (Wallet)
* **Definição**: Repositório de créditos virtuais de mobilidade atrelado a um Usuário.
* **Atributos de Negócio**: Saldo Disponível, Saldo de Cashback, Status da Carteira.
* **Status**: `Ativa`, `Bloqueada` (transações de débito suspensas).

### 1.3. Transação (Transaction)
* **Definição**: Qualquer lançamento financeiro na carteira de um usuário que altere seu saldo de forma permanente ou temporária.
* **Tipos de Transação**:
  * **Recarga (Recharge)**: Crédito de saldo via Pix, cartão de crédito ou voucher corporativo.
  * **Débito (Debit)**: Pagamento de uma viagem ou reserva de transporte.
  * **Cashback**: Crédito de recompensa ganho por uma viagem realizada.
  * **Estorno (Refund)**: Devolução parcial ou total de um débito devido a cancelamento.
  * **Ajuste (Adjustment)**: Correção manual de saldo feita por um Administrador MaaS após auditoria.

### 1.4. Parceiro de Mobilidade (Mobility Partner)
* **Definição**: Empresa externa de transporte integrada (ex.: SPTrans, Uber, 99, operadoras de aluguel de bicicletas ou patinetes).
* **Atributos de Negócio**: ID do Parceiro, Nome Fantasia, Status da Integração (Ativo/Inativo), Tipos de Modais Suportados, Chave de API de Integração.

### 1.5. Viagem (Trip)
* **Definição**: Registro de um deslocamento multimodal planejado, reservado ou realizado pelo Passageiro.
* **Status da Viagem**:
  * `Cotada (Quoted)`: Rota calculada com preço estimado.
  * `Reservada (Reserved)`: Saldo garantido/reservado em carteira; aguardando início.
  * `Em Andamento (In Progress)`: Viagem iniciada pelo passageiro no modal do parceiro.
  * `Concluída (Completed)`: Viagem finalizada; débito consolidado e cashback liberado.
  * `Cancelada (Cancelled)`: Viagem abortada; saldo reservado deve ser estornado ao usuário.

### 1.6. Campanha de Cashback (Cashback Campaign)
* **Definição**: Regra promocional criada pela administração da plataforma para incentivar o uso de modais específicos em determinados períodos.
* **Atributos de Negócio**: Nome da Campanha, Data/Hora de Início, Data/Hora de Fim, Percentual de Cashback (ex.: 5%), Modais Elegíveis, Teto de Cashback por Usuário, Teto Total da Campanha.

---

## 2. Controle de Acesso e Matriz de Permissões (RBAC)

A segurança deve impor o princípio do menor privilégio. A tabela abaixo mapeia as permissões operacionais:

| Recurso / Ação | Passageiro | Administrador MaaS | Parceiro de Mobilidade | Operador de Atendimento | Gestor Corporativo |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Consultar própria conta/saldo** | Sim | Sim | Não | Sim | Não |
| **Solicitar recarga própria** | Sim | Não | Não | Não | Não |
| **Pesquisar e cotar rotas** | Sim | Não | Não | Não | Não |
| **Criar/pagar viagem** | Sim | Não | Não | Não | Não |
| **Cadastrar parceiros / modais** | Não | Sim | Não | Não | Não |
| **Criar campanhas de cashback** | Não | Sim | Não | Não | Não |
| **Ajustar saldo manualmente** | Não | Sim (Requer aprovação)* | Não | Não | Não |
| **Consultar transações gerais** | Não | Sim | Não | Sim | Não |
| **Consultar transações próprias** | Sim | Não | Sim (apenas do seu ID) | Não | Não |
| **Distribuir vouchers a funcionários**| Não | Não | Não | Não | Sim |
| **Visualizar relatórios (LGPD)** | Não | Sim (Auditoria) | Não | Não | Sim (Dados agregados) |

> [!IMPORTANT]
> * **Ajuste Financeiro Manual**: Qualquer inserção manual de saldo (Ajuste) feita por um administrador exige obrigatoriamente a aprovação (duplo fator / aprovação de outro usuário admin) registrada em logs para evitar fraudes internas.

---

## 3. Regras da Carteira e do Livro-Razão (Ledger)

### 3.1. Imutabilidade Absoluta
* Nenhuma linha da tabela `ledger_entry` pode ser atualizada (`UPDATE`) ou deletada (`DELETE`). O banco de dados deve ter permissões restritas a `SELECT` e `INSERT` para esta tabela.
* Correções de valores incorretos exigem o lançamento de uma transação de **Ajuste** (crédito ou débito) ou **Estorno**, mantendo a trilha histórica intocada.

### 3.2. Regra de Saldo Consolidado
* O saldo exibido ao usuário em tela é calculado através da seguinte fórmula lógica:
  
  $$\text{Saldo Disponível} = \sum(\text{Transações Confirmadas}) + \sum(\text{Transações Pendentes}) - \sum(\text{Reservas Bloqueadas})$$
  
* Lançamentos pendentes (como recargas de boleto aguardando compensação) não devem ser computados como saldo utilizável imediato.

### 3.3. Rastreabilidade de Estorno
* Um estorno só pode ser gerado caso esteja associado ao identificador da transação de débito original.
* O valor acumulado de estornos para uma transação de débito nunca pode exceder o valor total do débito original.

---

## 4. Regras do Motor de Cashback

* **Cálculo da Recompensa**: O cálculo do cashback é feito durante a cotação da rota com base nas campanhas ativas. Se o usuário for elegível a mais de uma campanha, aplica-se a regra de melhor benefício para o usuário (a de maior percentual), respeitando o teto individual.
* **Provisão de Cashback**: No momento da reserva da viagem, o cashback projetado fica no estado `Pendente` e não pode ser gasto pelo usuário.
* **Liberação do Crédito**: O cashback só é efetivamente convertido em `Saldo Disponível` após o recebimento e processamento da confirmação de uso da viagem enviado pelo parceiro.
* **Cancelamento**: Caso a viagem seja cancelada, o cashback provisionado (pendente) associado àquela viagem é excluído.

---

## 5. Integrações, Idempotência e Webhooks

* **Saldo Mínimo para Viagens**: Um passageiro não pode iniciar uma viagem ou fazer uma reserva se o seu `Saldo Disponível` for inferior ao valor cotado da viagem, **exceto** se o parceiro de mobilidade aceitar cobrança externa direta (ex.: o usuário configurou pagamento direto com cartão de crédito via app do parceiro).
* **Idempotência Obrigatória**: O sistema deve rejeitar requisições de webhooks duplicadas. A idempotência deve ser validada usando a chave única contendo o ID do parceiro e o ID da transação da viagem no parceiro. Se um webhook com a mesma chave for recebido novamente, o sistema deve retornar HTTP 200 OK sem reprocessar as alterações de saldo.
* **Privacidade de Dados (LGPD)**:
  * Parceiros de mobilidade não recebem dados pessoais dos usuários da carteira (como CPF ou nome completo). As requisições de cotação e reserva devem usar tokens de identificação temporários e anônimos.
  * Relatórios disponibilizados a Gestores Corporativos devem agregar dados de uso (ex.: "R$ 500 gastos em Uber em Maio"), sendo expressamente proibido listar detalhes de rotas individuais (origem, destino, horários) de funcionários específicos.
