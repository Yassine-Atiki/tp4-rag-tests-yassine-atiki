package ma.emsi.yassine.atiki;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;

public class TestTransformation {
    public static void main(String[] args) {
        ChatModel modele = Gemini.creerModele(true);
        ContentRetriever recuperateur = RagNaif.creerRecuperateur(
                RagNaif.SUPPORT_RAG, new AllMiniLmL6V2EmbeddingModel());

        CompressingQueryTransformer transformation = CompressingQueryTransformer.builder()
                .chatModel(modele)
                .build();
        RetrievalAugmentor augmentation = DefaultRetrievalAugmentor.builder()
                .queryTransformer(transformation)
                .contentRetriever(recuperateur)
                .build();
        Assistant assistant = RagNaif.nouvelAssistant(modele)
                .retrievalAugmentor(augmentation)
                .build();

        Conversation.lancer(assistant);
    }
}
