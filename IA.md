# Utilisation de l’IA — TP4 RAG, quelques tests

**Yassine Atiki — EMSI Casablanca, groupe G2**

Pour cette version du TP4, j’ai demandé à Codex d’implémenter et d’exécuter les cinq tests console, puis de relever les résultats utiles à la remise. Gemini est le modèle utilisé par le programme pour répondre aux questions, reformuler les requêtes et choisir les sources.

Le premier échange reprend un extrait de ma demande initiale. Les quatre questions suivantes ont été rédigées après les essais pour expliquer ce que montrent les journaux.

## Test 2 — Comprendre les différences entre les réponses

**Prompt — extrait de ma demande initiale**

> Pour chaque version, conserve la réponse réelle à la deuxième question et le message augmenté réellement envoyé à Gemini. Explique la différence en t’appuyant sur les passages récupérés et les logs.

**Réponse de Codex**

Pour la relance « Dans le support de cours fourni, à quoi c’est comparé ? », la recherche naïve a remonté un passage sur un exemple de résiliation. L’historique du premier échange figurait aussi dans la requête finale et a pu aider Gemini à évoquer le RAG, le prompt engineering et les alternatives au fine-tuning. Avec la transformation, la recherche a retrouvé des passages sur les options et les risques du fine-tuning. La réponse finale s’est concentrée sur les alternatives au fine-tuning complet. Dans cet essai, les passages sont devenus plus ciblés et la réponse a couvert moins de comparaisons.

## Test 2 — Repérer les deux appels dans le journal

**Prompt d’analyse**

> Où est-ce que je vois la reformulation et où est-ce que je vois la question augmentée pour la seconde relance ?

**Réponse de Codex**

Dans `05-transformation-apres.log`, la première réponse de Gemini après la relance contient la reformulation. Elle nomme le fine-tuning, le RAG et les options alternatives. Cette formulation sert à interroger le magasin d’embeddings. La requête suivante contient la relance d’origine et les passages récupérés, sous le marqueur `Answer using the following information:`. C’est cet appel qui produit la réponse affichée à l’utilisateur.

## Test 3 — Vérifier la source réellement utilisée

**Prompt d’analyse**

> Le routeur renvoie `2` pour la question sur le sentier. Quelle ligne montre que la fiche de randonnée est bien arrivée dans la requête finale ?

**Réponse de Codex**

Dans `06-routage-deux-sources.log`, le prompt de sélection associe `1` au support de cours et `2` à la fiche du sentier des Cèdres. Après le choix `2`, le dernier message utilisateur de la requête suivante contient le passage sur les balises bleues. Le choix du routeur établit la source sélectionnée. Le passage ajouté au dernier message établit le contenu récupéré pour cette question. Les messages précédents peuvent encore contenir les passages d’un autre échange.

## Test 4 — Diagnostiquer une mauvaise décision de routage

**Prompt d’analyse**

> Pourquoi le premier essai avec le routeur personnalisé échoue-t-il pour la question sur le fine-tuning, et comment voit-on que l’ajustement fonctionne ?

**Réponse de Codex**

Au premier essai, Gemini a répondu `non` à la salutation et aussi à la question sur le fine-tuning. Le second choix a écarté le PDF alors que la question portait sur le cours. Après ajout d’un critère explicite dans le prompt, le journal `08b-routage-pas-de-rag-valide.log` montre `non` puis `[Routage] Aucun document.` pour « Bonjour ». Pour la question sur le fine-tuning, il montre `oui`, `[Routage] Support de cours.` et des passages ajoutés à la requête suivante.

## Test 5 — Déterminer ce que les essais prouvent pour le bonus

**Prompt d’analyse**

> Le journal affiche `[Routage] Web` pour la question d’actualité. Qu’est-ce que je peux conclure de cet essai et que faut-il encore vérifier avec Tavily ?

**Réponse de Codex**

Le journal montre que Gemini a sélectionné la route Web. La recherche s’est arrêtée faute de `TAVILY_API_KEY`. La question sur le cours a déclenché la route PDF et a reçu une réponse avec Gemini. Un essai séparé avec le PDF seul a montré l’ajout de passages hors sujet pour la même question d’actualité. Pour vérifier le bonus de bout en bout, il reste à rejouer cette question avec Tavily dans les deux versions et à comparer les passages ajoutés dans chaque requête finale.
