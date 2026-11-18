package ma.emsi.yassine.atiki;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.List;

public class RagNaif {
    static final String SUPPORT_RAG = "rag.pdf";

    public static void main(String[] args) {
        ChatModel modele = Gemini.creerModele(true);
        EmbeddingModel modeleEmbeddings = new AllMiniLmL6V2EmbeddingModel();
        ContentRetriever recuperateur = creerRecuperateur(SUPPORT_RAG, modeleEmbeddings);
        Assistant assistant = nouvelAssistant(modele).contentRetriever(recuperateur).build();

        Conversation.lancer(assistant);
    }

    static ContentRetriever creerRecuperateur(String ressource, EmbeddingModel modeleEmbeddings) {
        // Ingestion du document et calcul des embeddings.
        DocumentParser parseur = new ApacheTikaDocumentParser();
        Document document = ClassPathDocumentLoader.loadDocument(ressource, parseur);
        DocumentSplitter decoupage = DocumentSplitters.recursive(600, 60);
        List<TextSegment> segments = decoupage.split(document);
        List<Embedding> embeddings = modeleEmbeddings.embedAll(segments).content();
        InMemoryEmbeddingStore<TextSegment> magasin = new InMemoryEmbeddingStore<>();
        magasin.addAll(embeddings, segments);

        // Récupération des segments les plus proches de la question.
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(magasin)
                .embeddingModel(modeleEmbeddings)
                .maxResults(2)
                .minScore(0.5)
                .build();
    }

    static AiServices<Assistant> nouvelAssistant(ChatModel modele) {
        return AiServices.builder(Assistant.class)
                .chatModel(modele)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10));
    }
}
