package ma.emsi.yassine.atiki;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

public class Test4 {
    public static void main(String[] args) {
        ChatModel modele = Gemini.creerModele(true);
        ContentRetriever cours = RagNaif.creerRecuperateur(
                RagNaif.SUPPORT_RAG, new AllMiniLmL6V2EmbeddingModel());
        ContentRetriever web = creerRecuperateurWeb();

        QueryRouter routeur = new DefaultQueryRouter(cours, web);
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
