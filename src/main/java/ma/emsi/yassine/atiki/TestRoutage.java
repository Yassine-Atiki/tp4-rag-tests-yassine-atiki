package ma.emsi.yassine.atiki;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestRoutage {
    public static void main(String[] args) {
        ChatModel modele = Gemini.creerModele(true);
        EmbeddingModel modeleEmbeddings = new AllMiniLmL6V2EmbeddingModel();
        ContentRetriever cours = RagNaif.creerRecuperateur(RagNaif.SUPPORT_RAG, modeleEmbeddings);
        ContentRetriever randonnee = RagNaif.creerRecuperateur("sentier-des-cedres.txt", modeleEmbeddings);

        Map<ContentRetriever, String> descriptions = new LinkedHashMap<>();
        descriptions.put(cours, "Support de cours sur le RAG, le fine-tuning et LangChain4j.");
        descriptions.put(randonnee, "Fiche de randonnée du sentier des Cèdres, son balisage et son itinéraire.");
        LanguageModelQueryRouter routeur = new LanguageModelQueryRouter(modele, descriptions);
        RetrievalAugmentor augmentation = DefaultRetrievalAugmentor.builder()
                .queryRouter(routeur)
                .build();
        Assistant assistant = RagNaif.nouvelAssistant(modele)
                .retrievalAugmentor(augmentation)
                .build();

        Conversation.lancer(assistant);
    }
}
