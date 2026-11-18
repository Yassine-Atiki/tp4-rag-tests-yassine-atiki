package ma.emsi.yassine.atiki;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Test4 {
    public static void main(String[] args) {
        ChatModel modele = Gemini.creerModele(true);
        ContentRetriever cours = RagNaif.creerRecuperateur(
                RagNaif.SUPPORT_RAG, new AllMiniLmL6V2EmbeddingModel());
        ContentRetriever web = creerRecuperateurWeb();

        // Routage initial du Test 5.
        // QueryRouter routeur = new DefaultQueryRouter(cours, web);
        Map<ContentRetriever, String> descriptions = new LinkedHashMap<>();
        descriptions.put(cours, "Support de cours sur le RAG, le fine-tuning et LangChain4j.");
        descriptions.put(web, "Recherche sur le Web pour l'actualité ou les sujets hors du cours.");

        PromptTemplate choix = PromptTemplate.from("""
                Choisis une seule source pour la question.
                Choisis le PDF si la question porte sur le cours de RAG, de fine-tuning ou de LangChain4j.
                Choisis le Web si la question porte sur un événement récent ou un autre sujet.
                {{options}}

                Question : {{query}}
                Réponds uniquement par le numéro de la source choisie.
                """);
        LanguageModelQueryRouter routeurGemini = LanguageModelQueryRouter.builder()
                .chatModel(modele)
                .retrieverToDescription(descriptions)
                .promptTemplate(choix)
                .build();
        QueryRouter routeur = question -> {
            Collection<ContentRetriever> sources = routeurGemini.route(question);
            if (sources.size() != 1) {
                throw new IllegalStateException("Gemini doit choisir une seule source.");
            }
            System.out.println(sources.contains(cours) ? "[Routage] PDF" : "[Routage] Web");
            return sources;
        };
        RetrievalAugmentor augmentation = DefaultRetrievalAugmentor.builder()
                .queryRouter(routeur)
                .build();
        Assistant assistant = RagNaif.nouvelAssistant(modele)
                .retrievalAugmentor(augmentation)
                .build();

        Conversation.lancer(assistant);
    }

    private static ContentRetriever creerRecuperateurWeb() {
        String cle = System.getenv("TAVILY_API_KEY");
        if (cle == null || cle.isBlank()) {
            // Le routeur du bonus peut encore utiliser le PDF seul.
            return question -> {
                throw new IllegalStateException("La variable d'environnement TAVILY_API_KEY est absente.");
            };
        }

        TavilyWebSearchEngine moteur = TavilyWebSearchEngine.builder()
                .apiKey(cle)
                .logRequests(false)
                .logResponses(false)
                .build();
        return WebSearchContentRetriever.builder()
                .webSearchEngine(moteur)
                .build();
    }
}
