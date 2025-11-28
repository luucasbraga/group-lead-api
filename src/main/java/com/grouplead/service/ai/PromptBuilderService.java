package com.grouplead.service.ai;

import com.grouplead.domain.entity.Sprint;
import com.grouplead.domain.entity.User;
import com.grouplead.domain.enums.InsightType;
import com.grouplead.domain.enums.PeriodType;
import com.grouplead.repository.DeveloperRepository;
import com.grouplead.repository.SprintRepository;
import com.grouplead.repository.TicketRepository;
import com.grouplead.service.collector.JiraCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptBuilderService {

    private final SprintRepository sprintRepository;
    private final TicketRepository ticketRepository;
    private final DeveloperRepository developerRepository;
    private final JiraCollectorService jiraCollectorService;

    public String buildPromptForInsight(InsightType type, String targetId, PeriodType period) {
        return switch (type) {
            case SPRINT_SUMMARY -> buildSprintSummaryPrompt(targetId);
            case DELIVERY_PREDICTION -> buildPredictionPrompt(targetId);
            case DEVELOPER_ANALYSIS -> buildDeveloperAnalysisPrompt(Long.parseLong(targetId), period);
            case BURNOUT_DETECTION -> buildBurnoutDetectionPrompt(Long.parseLong(targetId));
            case ANOMALY_DETECTION -> buildAnomalyDetectionPrompt();
            case TEAM_HEALTH -> buildTeamHealthPrompt(Long.parseLong(targetId));
            case CODE_QUALITY -> buildCodeQualityPrompt();
            case INFRASTRUCTURE_HEALTH -> buildInfrastructureHealthPrompt();
        };
    }

    public String buildSprintSummaryPrompt(String sprintId) {
        Sprint sprint = sprintRepository.findByExternalId(sprintId).orElse(null);
        if (sprint == null) {
            return "Sprint not found. Please provide a valid sprint ID.";
        }

        var metrics = jiraCollectorService.calculateSprintMetrics(sprintId);

        return """
            Você é um analista de engenharia de software especializado em métricas de produtividade.

            Analise os dados da sprint abaixo e gere um resumo executivo em português.

            ## Dados da Sprint
            - Nome: %s
            - Período: %s a %s
            - Dias restantes: %d
            - Tickets planejados: %d (Total: %d story points)
            - Tickets concluídos: %d (Total: %d story points)
            - Tickets em progresso: %d
            - Tickets bloqueados: %d
            - Taxa de conclusão: %.1f%%

            ## Instruções
            Gere um resumo executivo contendo:
            1. Resumo geral das entregas (2-3 parágrafos)
            2. Pontos positivos (máximo 3)
            3. Pontos de atenção (máximo 3)
            4. Recomendações para próxima sprint (máximo 3)

            Seja objetivo e direto. Use dados concretos para embasar as análises.
            Responda em formato Markdown.
            """.formatted(
                metrics.sprintName(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                metrics.daysRemaining(),
                metrics.totalTickets(),
                metrics.totalStoryPoints(),
                metrics.completedTickets(),
                metrics.completedStoryPoints(),
                metrics.inProgressTickets(),
                metrics.blockedTickets(),
                metrics.completionRate()
        );
    }

    public String buildPredictionPrompt(String sprintId) {
        Sprint sprint = sprintRepository.findByExternalId(sprintId).orElse(null);
        if (sprint == null) {
            return "Sprint not found.";
        }

        var metrics = jiraCollectorService.calculateSprintMetrics(sprintId);

        return """
            Você é um especialista em gestão de projetos ágeis com foco em previsibilidade.

            Com base nos dados atuais, preveja a probabilidade de conclusão da sprint no prazo.

            ## Estado Atual da Sprint
            - Nome: %s
            - Dias restantes: %d
            - Story points restantes: %d
            - Tickets em andamento: %d
            - Tickets bloqueados: %d
            - Taxa de conclusão atual: %.1f%%

            ## Instruções
            Responda em JSON com a seguinte estrutura:
            {
                "predicted_completion_date": "YYYY-MM-DD",
                "confidence_score": 0.0 a 1.0,
                "risk_level": "LOW" | "MEDIUM" | "HIGH" | "CRITICAL",
                "completion_probability": 0.0 a 1.0,
                "risk_factors": ["lista de fatores de risco"],
                "recommendations": ["lista de recomendações"],
                "scenario_analysis": {
                    "optimistic": {"date": "YYYY-MM-DD", "probability": 0.0},
                    "realistic": {"date": "YYYY-MM-DD", "probability": 0.0},
                    "pessimistic": {"date": "YYYY-MM-DD", "probability": 0.0}
                }
            }

            Seja preciso e realista nas previsões.
            """.formatted(
                metrics.sprintName(),
                metrics.daysRemaining(),
                metrics.totalStoryPoints() - metrics.completedStoryPoints(),
                metrics.inProgressTickets(),
                metrics.blockedTickets(),
                metrics.completionRate()
        );
    }

    public String buildDeveloperAnalysisPrompt(Long developerId, PeriodType period) {
        var developer = developerRepository.findById(developerId).orElse(null);
        if (developer == null) {
            return "Developer not found.";
        }

        var tickets = ticketRepository.findByDeveloperId(developerId);

        return """
            Você é um especialista em gestão de times de desenvolvimento.

            Analise os dados do desenvolvedor abaixo e gere insights para uma reunião de 1:1.

            ## Dados do Desenvolvedor
            - Nome: %s
            - Período analisado: %s
            - Tickets atribuídos: %d
            - Tickets concluídos: %d

            ## Instruções
            Gere insights incluindo:
            1. Análise de produtividade
            2. Pontos fortes identificados
            3. Oportunidades de crescimento
            4. Pontos sugeridos para discussão no 1:1

            Seja construtivo e focado no desenvolvimento profissional.
            Responda em português.
            """.formatted(
                developer.getName(),
                period.name(),
                tickets.size(),
                tickets.stream().filter(t -> t.isCompleted()).count()
        );
    }

    public String buildBurnoutDetectionPrompt(Long developerId) {
        return """
            Analise indicadores de burnout para o desenvolvedor e identifique sinais de sobrecarga.

            Considere fatores como:
            - Commits fora do horário comercial
            - Aumento súbito de carga de trabalho
            - Queda na qualidade do código
            - Tickets arrastando por muito tempo

            Retorne uma análise com score de risco (0-1) e recomendações.
            """;
    }

    public String buildAnomalyDetectionPrompt() {
        return """
            Analise as métricas de infraestrutura e identifique anomalias.

            Busque por:
            - Picos de latência incomuns
            - Aumento na taxa de erro
            - Uso excessivo de recursos
            - Padrões de comportamento anômalos

            Retorne anomalias detectadas com análise de causa e sugestões de ação.
            """;
    }

    public String buildTeamHealthPrompt(Long teamId) {
        return """
            Analise a saúde geral do time de desenvolvimento.

            Considere:
            - Velocidade do time
            - Distribuição de carga de trabalho
            - Qualidade das entregas
            - Colaboração (code reviews, pair programming)

            Retorne um relatório de saúde do time com recomendações.
            """;
    }

    public String buildCodeQualityPrompt() {
        return """
            Analise métricas de qualidade de código do projeto.

            Considere:
            - Cobertura de testes
            - Bugs encontrados em produção
            - Tempo médio de code review
            - Débito técnico

            Retorne análise de qualidade com sugestões de melhoria.
            """;
    }

    public String buildInfrastructureHealthPrompt() {
        return """
            Analise a saúde da infraestrutura.

            Métricas a considerar:
            - Uptime dos serviços
            - Latência P50, P95, P99
            - Taxa de erros
            - Uso de recursos (CPU, memória)

            Retorne status de saúde e alertas necessários.
            """;
    }

    public String buildChatSystemPrompt(User user) {
        return """
            Você é o assistente de IA do Group Lead, um sistema de acompanhamento de times de desenvolvimento.

            ## Contexto do Usuário
            - Nome: %s
            - Papel: %s

            ## Instruções
            1. Responda perguntas sobre métricas do time e infraestrutura
            2. Seja conciso e direto
            3. Use dados concretos sempre que possível
            4. Sugira ações quando apropriado
            5. Se não tiver dados suficientes, informe
            6. Responda sempre em português

            Você tem acesso aos dados do time. Responda as perguntas do usuário de forma útil e informativa.
            """.formatted(
                user.getFullName(),
                user.getRole().name()
        );
    }
}
