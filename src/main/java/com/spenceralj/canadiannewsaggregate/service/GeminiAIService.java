package com.spenceralj.canadiannewsaggregate.service;

import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.SilentJavaScriptErrorListener;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiAIService {
    private final ChatClient client;

    public GeminiAIService(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder.build();
    }

    public NewsArticle.Analysis analyze(NewsArticle article) {
        String prompt = """
                You are an expert Canadian public policy and legislative analyst.
                Analyze the following Canadian news item or government press release.

                Article Details:
                - Source: {source}
                - Title: {title}
                - RSS Description: {description}
                - Full Article Content: {raw_content}

                Tasks:
                1. RELEVANCE CHECK (`isRelevant`):
                   - Set `isRelevant = true` IF the article represents:
                     (a) Major infrastructure, transit lines, highway expansions, rail/subway/bus rapid transit, energy generation/grid projects, or major hospital builds ($1M+ capital scale, groundbreakings, contract awards, or completions).
                     (b) Significant government funding programs, grants, industrial investments, or economic support packages ($1M+ or broad strategic scope).
                     (c) Substantial housing initiatives, zoning changes, or housing-enabling infrastructure funds.
                     (d) New legislation, bills, statutory amendments, regulations, tax policy changes, provincial budgets, or governance decisions.
                     (e) International/cross-border trade actions, tariffs, export rules, interprovincial trade agreements, or retaliatory trade measures.
                     (f) Major regulatory decisions, public contracting bans (e.g. RENA), or major tax fraud judgements exceeding $1M.
                     (g) Provincial disaster relief declarations or emergency wildfire assistance funds.
                     (h) Formal provincial/federal party leadership race entries, cabinet appointments/resignations, or legislature prorogations.
                   - Set `isRelevant = false` IF the article represents:
                     (a) Media advisories, press conference scheduling notices, or photo-op announcements (e.g., "Minister to Hold a Press Conference").
                     (b) Punditry, op-eds, academic opinions, think-tank commentaries, or union grievances where NO government policy, bill, or budget has been enacted.
                     (c) Foreign domestic news (e.g., US domestic corporate lawsuits, US domestic regulations) that does not directly alter Canadian statutes or trade.
                     (d) Routine hyper-local municipal maintenance or traffic alerts (e.g., lane closures, local road repaving, traffic pattern shifts, speed radar alerts).
                     (e) Single-incident criminal arrests, court trials, homicides, accidents, or police blotters without systemic legislative/policy changes.
                     (f) Ceremonial awards, medals, sports tournaments, pageants, or awareness slogans.
                     (g) Routine civil service HR surveys, internal staff statistics, or routine crown corporation executive resignations.
                     (h) Minor poaching, fishing, or individual traffic tickets.

                2. FACT & DATA-DENSE SUMMARY (`tldr`):
                   - If `isRelevant = true`, write a detailed, highly factual 3 to 5 sentence summary in English extracting the concrete facts from the article (if the article is in French, translate and synthesize the facts into English).
                   - MANDATORY RULES:
                     (a) NEVER use vague generalities like "a food manufacturer", "a company", "a transit project", or "a local municipality". ALWAYS name the exact corporations, agencies, facilities, and organizations involved (e.g., "Maple Lodge Farms Ltd.", "Metrolinx", "Vale Canada").
                     (b) Detail the EXACT physical, operational, or policy cause (e.g., "a hose carrying carbon dioxide ruptured on a vacuum blender, releasing 16,072 pounds of gas into the deli area", NOT "an injury occurred").
                     (c) Include ALL specific numbers, figures, and dollar amounts (e.g., exact fine amounts, 25% victim fine surcharges, total square footage, units built, megawatts generated, kilometers widened).
                     (d) Cite specific legislation, section numbers, or funds (e.g., "Section 25(2)(h) of the Occupational Health and Safety Act", "Housing-Enabling Water Systems Fund").
                     (e) Include the outcome or corrective action (e.g., "installed permanent CO2 sensors", "construction begins Q2 2027").
                   - If `isRelevant = false`, return an empty string "".

                3. SECTOR & JURISDICTION TAGS (`tags`):
                   - If `isRelevant = true`, select 1 to 6 tags (maximum of 6, not a requirement to pick 6) STRICTLY from this approved list (do not create custom tags or use ampersands):
                     - Jurisdiction Level: ["National", "Ontario", "Quebec", "British Columbia",
                                            "Alberta", "Manitoba", "Saskatchewan", "Nova Scotia",
                                            "New Brunswick", "Newfoundland and Labrador",
                                            "Prince Edward Island", "Municipal"]
                       (Note: "National" covers federal affairs, nationwide initiatives, and northern territories: Yukon, Northwest Territories, Nunavut)
                     - Policy Domain: ["Housing", "Affordability", "Economy", "Taxation", "Finance",
                                       "Trade", "Labour", "Healthcare", "Seniors", "Education",
                                       "Social Services", "Indigenous", "Culture", "Tourism", "Infrastructure",
                                       "Transportation", "Transit", "Industry", "Technology", "Energy",
                                       "Environment", "Climate", "Resources", "Mining", "Agriculture",
                                       "Fisheries", "Emergency", "Justice", "Public Safety", "Governance",
                                       "Immigration", "Defence"]
                   - Always include at least one jurisdiction tag (e.g. "National", "Ontario", etc.) along with 1 to 5 applicable policy domain tags.
                   - If `isRelevant = false`, return an empty list [].
                """;

        String content = "";

        try (final WebClient webClient = new WebClient()) {

            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setDownloadImages(false);
            webClient.getOptions().setCssEnabled(false);

            webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
            webClient.setIncorrectnessListener((message, origin) -> {});
            webClient.setCssErrorHandler(new SilentCssErrorHandler());


            HtmlPage page = webClient.getPage(article.link());
            webClient.waitForBackgroundJavaScript(2000);

            content = page.asNormalizedText();
        } catch (Exception e) {
            log.error("Unable to fetch page, defaulting to rss description: {}", e.getMessage());
        }

        final String rawContent = content.substring(0, Math.min(content.length(), 15000));

        return client.prompt()
                .user(u -> u.text(prompt)
                        .param("source", article.source().name())
                        .param("title", article.title())
                        .param("description", article.description())
                        .param("raw_content", rawContent))
                .call()
                .entity(NewsArticle.Analysis.class);
    }
}
