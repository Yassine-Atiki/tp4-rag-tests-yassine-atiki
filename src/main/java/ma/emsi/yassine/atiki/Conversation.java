package ma.emsi.yassine.atiki;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

final class Conversation {
    private Conversation() {
    }

    static void lancer(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("Posez vos questions et tapez fin pour terminer.");
            while (scanner.hasNextLine()) {
                String question = scanner.nextLine().strip();
                if (question.isBlank()) {
                    continue;
                }
                if ("fin".equalsIgnoreCase(question)) {
                    System.out.println("[Fin] Conversation terminée.");
                    return;
                }
                System.out.println("Question : " + question);
                System.out.println("Gemini : " + assistant.chat(question));
            }
        }
    }
}
