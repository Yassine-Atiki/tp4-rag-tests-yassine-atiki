package ma.emsi.yassine.atiki;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;

import java.util.List;
import java.util.Map;

public class Test3 {
    public static void main(String[] args) {
        ChatModel modele = Gemini.creerModele(true);
        ContentRetriever cours = RagNaif.creerRecuperateur(
                RagNaif.SUPPORT_RAG, new AllMiniLmL6V2EmbeddingModel());
        PromptTemplate choix = PromptTemplate.from("""
                Détermine si le support de cours sur le RAG et le fine-tuning aide à répondre.
                Si la question demande la définition ou l'utilisation du RAG ou du fine-tuning, réponds « oui ».
                Une salutation seule appelle « non ». Pour un lien incertain avec le cours, réponds « peut-être ».
                Exemple : « Qu'est-ce que le fine-tuning ? » donne « oui ».

                Question à classer : {{question}}
                Réponds par un seul choix parmi « oui », « non » et « peut-être ».
                """);

        class QueryRouterPourEviterRag implements QueryRouter {
            @Override
            public List<ContentRetriever> route(Query query) {
                String prompt = choix.apply(Map.of("question", query.text())).text();
                String reponse = modele.chat(prompt).strip();
                if (reponse.matches("(?i)non[.!?]?")) {
                    System.out.println("[Routage] Aucun document.");
                    return List.of();
                }
                System.out.println("[Routage] Support de cours.");
                return List.of(cours);
            }
        }

        RetrievalAugmentor augmentation = DefaultRetrievalAugmentor.builder()
                .queryRouter(new QueryRouterPourEviterRag())
                .build();
        Assistant assistant = RagNaif.nouvelAssistant(modele)
                .retrievalAugmentor(augmentation)
                .build();

        Conversation.lancer(assistant);
    }
}
