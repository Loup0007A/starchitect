# Starchitect — consignes pour Claude

- **Toujours pousser directement sur `main`** (pas de branche, pas de PR), à la demande de Loup0007A.
- Avant de pousser : le code doit compiler pour **chaque cible**, et `bash tools/offline.sh` doit afficher `OFFLINE OK`. Ensuite, vérifier la CI : branche `ci-logs`, `main/status.txt` puis `main/<test>/clienttest.log` et `main/<test>/screenshots/`.
- Après chaque push sur `main`, la CI ajoute un commit « Jars compilés … » : `git fetch` + rebase avant le push suivant.
- Même organisation que fate-flip : `src/main` = Forge 1.21–1.21.1 ; autres cibles dans `targets/<cible>/` (`target.json` + `overrides/`), sources produites par `python3 tools/port.py <cible>` (blocs `//#if` / `//#else` / `//#endif`, lignes inactives `//$$ `). `python3 tools/port.py --check` vérifie `src/main`.
- Compiler localement une cible : bibliothèques de la branche `mc-libs-<cible>` (workflow « Export libs »), puis `javac -cp "$(ls lib/*.jar | tr '\n' ':')"` sur `targets/<cible>/build/ported/java` (Java 17 pour forge-1.20.1). Les bibliothèques de fate-flip (`mc-libs-neoforge-1.21.1`, `mc-libs-forge-1.20.1`) conviennent aussi.
- `analysis/` et `review/` (et `debug/TestBuilds`) restent du Java pur, sans classe de Minecraft : c'est ce qui permet `tools/offline.sh`.
- Tout texte passe par `tools/lang.py` (FR + EN), puis `python3 tools/lang.py`. Une nouvelle phrase à variantes = une entrée `many(...)` ; `Variants.java` est généré.
- Nouvelle version = `mod_version` dans `gradle.properties` + section `## x.y.z` en haut de `CHANGELOG.md` (notes GitHub et CurseForge).
- Textures de l'interface générées par `python3 tools/textures.py` (Pillow), dessin commun dans `client/gui/Gfx.java`. Photos de presse : `client/Photos.java` (capture à `RenderLevelStageEvent.Stage.AFTER_LEVEL`, développée en sépia).
- Le mod reste 100 % client : il observe le monde, ne le modifie jamais.
