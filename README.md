# Group Lead API

<div align="center">

**Sistema de Acompanhamento de Time e Infraestrutura**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

</div>

---

## 📑 Índice

- [Visão Geral](#-visão-geral)
- [Características Principais](#-características-principais)
- [Arquitetura do Sistema](#-arquitetura-do-sistema)
- [Stack Tecnológica](#-stack-tecnológica)
- [Quick Start](#-quick-start)
- [Casos de Uso](#-casos-de-uso)
- [Segurança](#-segurança)
- [Monitoramento](#-monitoramento)
- [Suporte](#-suporte)
- [Roadmap](#-roadmap)

---

## 🎯 Visão Geral

### Descrição

O **Group Lead API** é uma plataforma completa de monitoramento e análise para líderes de engenharia que precisam acompanhar o desempenho de times de desenvolvimento, a saúde da infraestrutura e obter insights preditivos alimentados por IA.

### Problema que Resolve

Líderes técnicos enfrentam desafios constantes:

- **Falta de Visibilidade**: Dificuldade em acompanhar métricas de time em tempo real
- **Dados Fragmentados**: Informações espalhadas entre JIRA, GitLab, AWS e outras ferramentas
- **Detecção Tardia de Problemas**: Identificação de burnout, degradação de performance e incidentes apenas após o impacto
- **Análise Manual Demorada**: Horas gastas consolidando dados para relatórios de sprint
- **Reatividade**: Falta de previsões e alertas proativos sobre problemas iminentes

### Solução Proposta

Uma API REST unificada que:

✅ **Centraliza** dados de múltiplas fontes (JIRA, GitLab, AWS CloudWatch)
✅ **Automatiza** coleta de métricas e geração de relatórios
✅ **Analisa** padrões de trabalho e detecta anomalias
✅ **Prediz** problemas antes que impactem a equipe
✅ **Alerta** sobre riscos de burnout, degradação de infraestrutura e queda de velocidade
✅ **Visualiza** métricas DORA, custos AWS e performance de sprints em dashboards

---

## ✨ Características Principais

### 🏃 Gestão de Sprints e Times

- **Acompanhamento de Sprints**: Criação, planejamento e conclusão de sprints com sincronização JIRA
- **Métricas de Velocidade**: Cálculo automático de story points, burndown e velocity trends
- **Gestão de Times**: Organização de desenvolvedores com mapeamento entre sistemas externos
- **Rastreamento de Tickets**: Sincronização automática de issues do JIRA a cada 15 minutos

### 📊 Métricas DORA

Implementação completa das métricas DevOps Research and Assessment:

- **Deployment Frequency**: Frequência de deploys por período
- **Lead Time for Changes**: Tempo médio entre commit e produção
- **Mean Time to Recovery (MTTR)**: Tempo médio de recuperação de incidentes
- **Change Failure Rate**: Taxa de falha em deployments

### 🤖 Insights Alimentados por IA (Claude)

- **Chat Interativo**: Assistente IA com contexto do time e histórico de conversas
- **Previsões de Sprint**: Predição de entrega baseada em dados históricos
- **Sumários Automatizados**: Relatórios de sprint gerados semanalmente
- **Análise de Desenvolvedores**: Insights individuais de produtividade e bem-estar
- **Detecção de Anomalias**: Identificação automática de padrões anormais em infraestrutura
- **Alerta de Burnout**: Análise de commits fora do horário e trabalho em finais de semana

### 🚨 Sistema de Alertas Inteligentes

| Tipo de Alerta | Gatilho | Severidade |
|----------------|---------|------------|
| **Queda de Velocidade** | Redução >20% na velocity do time | WARNING/CRITICAL |
| **Risco de Burnout** | >50% commits fora do horário | CRITICAL |
| **CPU Elevada** | Utilização >80% | HIGH |
| **Erros em Produção** | Taxa de erro >1% | CRITICAL |
| **Latência Alta** | P99 >500ms | HIGH |

### 🏗️ Monitoramento de Infraestrutura AWS

- **EC2**: CPU, rede, I/O de disco
- **RDS**: CPU, conexões, memória, armazenamento
- **ECS**: Utilização de CPU e memória de containers
- **Lambda**: Invocações, duração, erros, throttles
- **ALB**: Contagem de requisições, tempo de resposta, status HTTP
- **Análise de Custos**: Exploração de custos AWS com forecasting

### 📈 Dashboards Personalizados

- **Visão Geral**: Métricas consolidadas com filtros por período
- **Dashboard por Time**: Visualização específica com seleção de período
- **Métricas de Infraestrutura**: Visualização em tempo real dos recursos AWS
- **Análise de Incidentes**: MTTR, severidade e timeline de incidentes

---

## 🏛️ Arquitetura do Sistema

### Diagrama de Camadas

```
┌──────────────────────────────────────────────────────────┐
│                   REST API Layer                          │
│  Controllers: Auth | Teams | Developers | Sprints        │
│              Dashboard | Incidents | Alerts | AI          │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────┴─────────────────────────────────┐
│                 Business Logic Layer                      │
│  ┌────────────┬───────────────┬─────────────────────┐   │
│  │   Core     │  Collectors   │   AI Services       │   │
│  │  Services  │  (JIRA, Git,  │  (Predictions,      │   │
│  │            │   AWS)        │   Anomalies)        │   │
│  └────────────┴───────────────┴─────────────────────┘   │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────┴─────────────────────────────────┐
│              Data Access Layer (Repositories)             │
│  JPA Repositories: User | Team | Sprint | Incident | ...  │
└─────┬──────────────────────────────────────────┬─────────┘
      │                                          │
┌─────▼──────┐                         ┌────────▼─────────┐
│ PostgreSQL │                         │  External APIs   │
│   + Redis  │                         │  JIRA | GitLab   │
│   Cache    │                         │  AWS | Claude AI │
└────────────┘                         └──────────────────┘
```

### Padrões Arquiteturais

- **Arquitetura em Camadas**: Separação clara entre Controllers, Services e Repositories
- **Repository Pattern**: Abstração da camada de dados com Spring Data JPA
- **DTO Pattern**: Separação entre entidades de domínio e objetos de transferência
- **Circuit Breaker Pattern**: Resiliência nas integrações externas
- **Cache-Aside Pattern**: Cache Redis para insights de IA e métricas frequentes

### Fluxo de Dados

```mermaid
graph LR
    A[Cliente] --> B[Controller]
    B --> C[Service]
    C --> D[Repository]
    D --> E[PostgreSQL]
    C --> F[External APIs]
    C --> G[Redis Cache]
    H[Scheduler] --> C
```

### Integrações Externas

| Serviço | Propósito | Frequência de Coleta |
|---------|-----------|----------------------|
| **JIRA** | Issues, sprints, story points | A cada 15 minutos |
| **GitLab** | Commits, merge requests | A cada 10 minutos |
| **AWS CloudWatch** | Métricas de infraestrutura | A cada 5 minutos |
| **AWS Cost Explorer** | Análise de custos | Diariamente (6h) |
| **Claude AI** | Insights e previsões | Sob demanda + agendado |

---

## 🛠️ Stack Tecnológica

### Backend

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Java** | 21 | Linguagem de programação |
| **Spring Boot** | 3.2.0 | Framework principal |
| **Spring Security** | 6.x | Autenticação e autorização |
| **Spring Data JPA** | 3.x | Camada de persistência |
| **Spring WebFlux** | 6.x | Cliente HTTP assíncrono |

### Banco de Dados

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **PostgreSQL** | 16 | Banco de dados relacional principal |
| **Redis** | 7 | Cache distribuído e sessões |
| **Flyway** | Latest | Controle de versão do schema |

### Integrações e SDKs

| SDK/API | Versão | Uso |
|---------|--------|-----|
| **AWS SDK** | 2.21.0 | CloudWatch, Cost Explorer |
| **JJWT** | 0.12.3 | Tokens JWT |
| **Anthropic Claude API** | - | IA generativa |

### Ferramentas de Desenvolvimento

| Ferramenta | Propósito |
|------------|-----------|
| **Lombok** | Redução de boilerplate |
| **MapStruct** | Mapeamento objeto-objeto |
| **SpringDoc OpenAPI** | Documentação Swagger/OpenAPI |
| **Testcontainers** | Testes de integração |

### DevOps & Deployment

| Tecnologia | Uso |
|------------|-----|
| **Docker** | Containerização |
| **Docker Compose** | Orquestração local |
| **Kubernetes** | Orquestração em produção |
| **Maven** | Build e gerenciamento de dependências |
| **Prometheus** | Métricas e observabilidade |

---

## 🚀 Quick Start

### Pré-requisitos

- **Java 21** ou superior
- **Docker** e **Docker Compose**
- **Maven 3.8+**
- Credenciais para: JIRA, GitLab, AWS, Claude AI

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/group-lead-api.git
cd group-lead-api
```

### 2. Configure as Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=grouplead
DB_USER=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=seu-secret-super-seguro-aqui-minimo-256-bits
JWT_EXPIRATION=86400000

# JIRA Integration
JIRA_BASE_URL=https://sua-empresa.atlassian.net
JIRA_USERNAME=seu-email@empresa.com
JIRA_API_TOKEN=seu-token-jira

# GitLab Integration
GITLAB_BASE_URL=https://gitlab.com
GITLAB_TOKEN=seu-token-gitlab

# AWS Credentials
AWS_ACCESS_KEY_ID=sua-access-key
AWS_SECRET_ACCESS_KEY=sua-secret-key
AWS_REGION=us-east-1

# Claude AI
CLAUDE_API_KEY=sk-ant-api03-sua-chave-aqui
CLAUDE_MODEL=claude-sonnet-4-20250514
```

### 3. Suba a Infraestrutura com Docker Compose

```bash
docker-compose up -d postgres redis
```

### 4. Execute as Migrações

```bash
./mvnw flyway:migrate
```

### 5. Compile e Execute a Aplicação

```bash
# Compilar
./mvnw clean package

# Executar
./mvnw spring-boot:run
```

Ou usando Docker:

```bash
docker-compose up --build
```

### 6. Acesse a Documentação da API

Abra o navegador em:

```
http://localhost:8080/swagger-ui.html
```

### 7. Crie seu Primeiro Usuário

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@empresa.com",
    "password": "SenhaSegura123!",
    "role": "ADMIN"
  }'
```

### 8. Faça Login e Obtenha o Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "SenhaSegura123!"
  }'
```

### 9. Use o Token nas Requisições

```bash
export TOKEN="seu-jwt-token-aqui"

curl -X GET http://localhost:8080/teams \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💼 Casos de Uso

### 1. Monitoramento de Sprint em Tempo Real

**Persona**: Tech Lead de time ágil

**Cenário**: Acompanhar progresso do sprint atual

```bash
# Buscar sprint atual
GET /sprints/current?teamId=1

# Ver métricas do sprint
GET /sprints/{sprintId}/metrics

# Obter previsão de entrega com IA
POST /ai/sprint-predictions/{sprintId}
```

**Benefício**: Visibilidade instantânea de velocity, burndown e previsão de entrega.

---

### 2. Detecção de Risco de Burnout

**Persona**: Engineering Manager

**Cenário**: Identificar desenvolvedores em risco de burnout

```bash
# Verificar alertas de burnout
GET /alerts?type=BURNOUT_RISK&status=UNRESOLVED

# Análise detalhada de um desenvolvedor
POST /ai/developer-insights/{developerId}

# Ver métricas individuais
GET /metrics/developer/{developerId}
```

**Benefício**: Intervenção proativa antes de impactos na saúde e produtividade.

---

### 3. Análise de Custos AWS

**Persona**: DevOps Engineer

**Cenário**: Otimizar custos de infraestrutura

```bash
# Coletar métricas de custo atuais
POST /infrastructure/cost-metrics/{teamId}

# Ver previsão de custos
POST /infrastructure/forecast/{teamId}?days=30

# Identificar recursos mais caros
GET /infrastructure/top-costs/{teamId}
```

**Benefício**: Identificação rápida de recursos que aumentam custos desnecessariamente.

---

### 4. Resposta a Incidentes

**Persona**: SRE / On-call Engineer

**Cenário**: Gerenciar incidente de produção

```bash
# Criar incidente
POST /incidents
{
  "title": "High CPU in Production DB",
  "severity": "CRITICAL",
  "teamId": 1
}

# Atualizar status
PUT /incidents/{id}/status
{
  "status": "INVESTIGATING"
}

# Adicionar evento na timeline
POST /incidents/{id}/timeline
{
  "description": "Identified slow query in logs"
}

# Resolver incidente
POST /incidents/{id}/resolve
{
  "rootCause": "Missing index on users.email"
}
```

**Benefício**: Rastreamento completo do incidente com MTTR automático.

---

### 5. Dashboard Executivo

**Persona**: VP of Engineering

**Cenário**: Visão geral de todos os times

```bash
# Dashboard consolidado
GET /dashboard/summary?startDate=2024-01-01&endDate=2024-12-31

# Métricas DORA de um time
GET /dashboard/team/{teamId}/dora?period=LAST_QUARTER

# Anomalias detectadas por IA
POST /ai/anomaly-detection/{teamId}
```

**Benefício**: Visão estratégica com dados consolidados e insights acionáveis.

---

## 🔒 Segurança

### Autenticação e Autorização

- **JWT (JSON Web Tokens)**: Autenticação stateless com expiração configurável
- **BCrypt**: Hash de senhas com salt automático
- **Spring Security**: Framework de segurança enterprise-grade
- **Role-Based Access Control (RBAC)**:
  - `ADMIN`: Acesso completo
  - `TECH_LEAD`: Gerenciamento de times e sprints
  - `USER`: Visualização e consultas

### Endpoints Protegidos

| Endpoint | Roles Permitidas |
|----------|------------------|
| `/auth/**` | Público |
| `/teams/**` | ADMIN, TECH_LEAD |
| `/developers/**` | ADMIN, TECH_LEAD |
| `/sprints/**` | ADMIN, TECH_LEAD, USER |
| `/dashboard/**` | ADMIN, TECH_LEAD, USER |
| `/incidents/**` | ADMIN, TECH_LEAD |
| `/ai/**` | ADMIN, TECH_LEAD |

### Boas Práticas Implementadas

✅ **Secrets Management**: Variáveis de ambiente para credenciais
✅ **HTTPS Only**: Configuração para redirecionar HTTP → HTTPS
✅ **CORS Configurado**: Whitelist de origens permitidas
✅ **Rate Limiting**: Proteção contra abuso de API
✅ **Input Validation**: Bean Validation em todos os DTOs
✅ **SQL Injection Prevention**: JPA com prepared statements
✅ **Container Security**: Non-root user em Docker

### Kubernetes Security

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
```

---

## 📡 Monitoramento

### Health Checks

A aplicação expõe endpoints de health check:

```bash
# Liveness probe (aplicação está viva?)
GET /actuator/health/liveness

# Readiness probe (aplicação está pronta?)
GET /actuator/health/readiness

# Health geral
GET /actuator/health
```

### Métricas Prometheus

Métricas disponíveis em `/actuator/prometheus`:

- **JVM**: Heap, GC, threads, CPU
- **HTTP**: Requests, latência, status codes
- **Database**: Connection pool, queries
- **Cache**: Hit rate, evictions
- **Custom Metrics**: Alertas, incidentes, deploys

### Logs Estruturados

Logs em formato JSON para ingestão em ferramentas como ELK Stack:

```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger": "com.grouplead.api.service.AlertService",
  "message": "Alert created",
  "context": {
    "alertId": 123,
    "type": "VELOCITY_DROP",
    "severity": "CRITICAL",
    "teamId": 1
  }
}
```

### Dashboards Recomendados

#### Grafana Dashboard: Team Velocity

- Story points por sprint (últimos 12 sprints)
- Velocity trend line
- Alertas de queda de velocidade
- Previsões de IA

#### Grafana Dashboard: Infrastructure

- CPU/Memory utilization por recurso AWS
- Request rate e latência P50/P95/P99
- Error rate e HTTP status codes
- Custos AWS por serviço

#### Grafana Dashboard: Incidents

- MTTR por severidade
- Incidentes abertos vs resolvidos
- Time to detect
- Incidentes por time

---

## 🆘 Suporte

### Documentação

- **API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Wiki do Projeto**: [GitHub Wiki](https://github.com/seu-usuario/group-lead-api/wiki)

### Reportar Problemas

Encontrou um bug? Abra uma issue:

1. Acesse [GitHub Issues](https://github.com/seu-usuario/group-lead-api/issues)
2. Clique em "New Issue"
3. Selecione o template apropriado:
   - 🐛 Bug Report
   - ✨ Feature Request
   - 📚 Documentation
   - ❓ Question

### Comunidade

- **Discussões**: [GitHub Discussions](https://github.com/seu-usuario/group-lead-api/discussions)
- **Slack**: [#group-lead-api](https://seu-workspace.slack.com/archives/group-lead-api)
- **Email**: support@grouplead.com

### SLA de Resposta

| Severidade | Tempo de Primeira Resposta |
|------------|---------------------------|
| Crítico (P0) | 2 horas |
| Alto (P1) | 1 dia útil |
| Médio (P2) | 3 dias úteis |
| Baixo (P3) | 1 semana |

---

## 🗺️ Roadmap

### ✅ V1.0 - MVP (Concluído)

- [x] Autenticação JWT
- [x] Gestão de times e desenvolvedores
- [x] Sincronização JIRA e GitLab
- [x] Métricas básicas de sprint
- [x] Monitoramento AWS CloudWatch
- [x] Integração Claude AI

### 🚧 V1.1 - Alertas Avançados (Em Desenvolvimento)

- [x] Sistema de alertas
- [x] Detecção de burnout
- [x] Anomalias de infraestrutura
- [ ] Integração com PagerDuty
- [ ] Webhooks para alertas
- [ ] Canal Slack para notificações

### 📋 V1.2 - Análise Preditiva (Q2 2025)

- [ ] Previsão de riscos de deployment
- [ ] Recomendações automáticas de alocação
- [ ] Predição de custos AWS com ML
- [ ] Identificação de code smells com IA
- [ ] Sugestões de refatoração

### 🎯 V2.0 - Multi-Tenant (Q3 2025)

- [ ] Arquitetura multi-tenant
- [ ] Isolamento de dados por organização
- [ ] White-label UI
- [ ] Planos e billing
- [ ] Self-service onboarding

### 🌟 V2.1 - Integrações Adicionais (Q4 2025)

- [ ] GitHub integration
- [ ] Azure DevOps integration
- [ ] Datadog integration
- [ ] New Relic integration
- [ ] CircleCI/Jenkins webhooks

### 🔮 V3.0 - Automação Inteligente (2026)

- [ ] Auto-remediation de incidentes simples
- [ ] Chatbot IA para operações
- [ ] Rebalanceamento automático de carga
- [ ] Policy-as-Code para alerts
- [ ] Insights preditivos em tempo real

---

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor, leia nosso [CONTRIBUTING.md](CONTRIBUTING.md) para detalhes sobre nosso código de conduta e processo de submissão de pull requests.

---

## 👥 Autores

- **Equipe Group Lead** - *Trabalho inicial* - [GitHub](https://github.com/seu-usuario)

---

## 🙏 Agradecimentos

- Spring Boot team pela excelente documentação
- Anthropic pelo Claude API
- Comunidade open source

---

<div align="center">

**[⬆ Voltar ao topo](#group-lead-api)**

Feito com ❤️ para líderes de engenharia

</div>
