# ⭐ Starchitect — le guide étoilé de vos constructions (Minecraft Forge / NeoForge)

> *Le critique est arrivé au crépuscule, sous une pluie battante, le monocle bien vissé.*

Un critique d'architecture snob vient visiter vos constructions. Il inspecte tout (palette de blocs, relief, éclairage, fenêtres, toiture, symétrie, aménagement, jardin), puis publie sa critique dans **Le Parpaing enchaîné** : un vrai article de journal, avec un gros titre, une note sur 100 et **0 à 3 étoiles**.

Toutes vos critiques sont rassemblées dans **le Guide Starchitect**, avec un titre qui évolue au fil des étoiles (d'*Apprenti maçon* à *Légende du parpaing*).

**Mod 100 % client** : il fonctionne en solo et sur n'importe quel serveur, même vanilla. Il ne modifie rien dans le monde.

## 📦 Installation

| Minecraft | Loader | Jar |
|---|---|---|
| 1.21 – 1.21.1 | Forge 51+ | `starchitect-<version>-forge-1.21.1.jar` |
| 1.21 – 1.21.1 | NeoForge 21.0 / 21.1 | `starchitect-<version>-neoforge-1.21.1.jar` |
| 1.20.1 | Forge 47 | `starchitect-<version>-forge-1.20.1.jar` |

Copiez le jar dans le dossier `mods/` (côté client uniquement). Les jars sont dans les [Releases](https://github.com/Loup0007A/starchitect/releases), sur CurseForge et dans [`releases/`](releases/).

## 🧐 Comment ça marche

- Placez-vous près d'une construction et appuyez sur **J** : le critique arrive, inspecte les lieux pendant quelques secondes (des étincelles parcourent les blocs examinés, il marmonne ses impressions)… puis le journal s'ouvre.
- **Visites surprises** : toutes les 20 minutes (réglable), si vous êtes près d'une construction nouvelle ou modifiée, un critique s'annonce. Vous avez 10 secondes pour l'accueillir (ou partir en courant).
- Revenez avec une construction améliorée : le critique s'en souvient (« on passe de 1 à 2 étoiles, quelqu'un a écouté »).
- **H** ouvre le guide ; cliquez sur une critique pour la relire.
- **Photo de presse** (1.1) : à son arrivée, le critique photographie la construction (flash !). La photo, développée en sépia avec du grain, illustre l'article et la fiche du guide.
- **Les conseils du critique** (1.1) : chaque critique se termine par trois conseils concrets. Le bouton **Montrer** signale en rouge, pendant 30 secondes, chaque coin resté dans le noir.
- **Styles** (1.2) : le critique reconnaît le style de la construction (médiéval, nordique, japonais, moderne, brutaliste, désertique, rustique…). Chaque critique a ses styles préférés.
- **Avant / après** (1.2) : lors d'une nouvelle visite, le journal montre l'ancienne photo à côté de la nouvelle.
- **Distinctions** (1.2) : 10 distinctions à obtenir (première étoile, copie parfaite, Cabane en terre d'or, le grand retour, tout le jury…), annoncées par une notification et rangées sur l'étagère du guide.
- **Imprimer** (1.2) : le bouton enregistre la une dans le dossier des captures d'écran, prête à partager.
- **Mètre de chantier** (1.1, touche **G**) : une estimation en direct de la note de la construction où vous êtes, avec ses 8 rubriques et son point faible, mise à jour pendant que vous bâtissez.

### Les 8 rubriques

| Rubrique | Ce que le critique regarde |
|---|---|
| Palette | Nombre de matériaux, et si l'un d'eux écrase tous les autres |
| Relief | Escaliers, dalles, murets, barrières, trappes : la profondeur de la façade |
| Éclairage | Les coins de l'intérieur plongés dans le noir complet (où les monstres apparaissent) |
| Fenêtres | La part de verre dans les murs |
| Toiture | Un toit, et s'il est en pente (escaliers, dalles) ou plat |
| Symétrie | La construction pliée en deux, selon un axe ou l'autre |
| Aménagement | Les sortes de mobilier : lit, coffres, établi, bibliothèques, tableaux, cadres… |
| Jardin | Fleurs, haies (feuilles posées), cultures, pots, chemins autour de la maison |

S'y ajoutent les goûts du critique, et quelques remarques : la cabane en terre (récompensée par la **Cabane en terre d'or**), l'absence de porte, les arcs-en-ciel de laine, le chat de la maison…

### Les critiques

| Critique | Style |
|---|---|
| **Augustin de Saint-Parpaing** | Classique et snob. Adore la pierre, le quartz, la symétrie. Méprise les pavés et la laine. |
| **Brigitte Béton** | Brutaliste. Le béton ne ment jamais. Le bois, si. |
| **Gaston Lagrange** | Rustique. Bois, jardin, feu de cheminée. Le béton lui donne froid. |
| **Zoé Néon** | Design et tendances. Verre, lumière, quartz. Les pavés, c'est tellement l'an dernier. |

## ⌨️ Commandes (client)

`/starchitect review [critique]` · `/starchitect guide` · `/starchitect read` (relire la critique la plus proche) · `/starchitect show` (montrer ses coins sombres) · `/starchitect gauge` · `/starchitect critic <random|augustin|brigitte|gaston|zoe>` · `/starchitect rename <nom>` · `/starchitect forget` · `/starchitect reset`

## ⚙️ Configuration

`config/starchitect-client.toml` : `autoVisits`, `visitIntervalMinutes` (20), `announceSeconds` (10), `critic` (random), `scanRadius` (20), `minBlocks` (25), `particles`, `sounds`.

Le guide est enregistré dans `.minecraft/starchitect/<monde ou serveur>.json`, les photos dans `.minecraft/starchitect/photos/`.

## 🔧 Développement

- Java 21, Forge 1.21.1-52.0.7, mappings Parchment : c'est la variante de référence (`src/main`). Les autres cibles (NeoForge 1.21.1, Forge 1.20.1) sont fabriquées par `tools/port.py` (voir `targets/`).
- L'analyse et la rédaction des critiques sont du Java pur (`analysis/`, `review/`) : `bash tools/offline.sh [fr|en]` construit les maisons de test, les fait critiquer et affiche les articles, sans Minecraft.
- Textes FR + EN : `tools/lang.py` (génère aussi `review/Variants.java`). Logo : `tools/logo.py`. Textures de l'interface (papier, sceaux, étoiles, ornements) : `tools/textures.py`.
- **CI GitHub Actions** : un jar par cible, puis pour chaque cible un vrai serveur (qui bâtit une maison et une cabane en terre) et un vrai client qui les fait critiquer et photographie chaque écran (branche `ci-logs`). Ensuite : release GitHub, copies dans `releases/` et envoi sur **CurseForge** (voir [`curseforge/SETUP.md`](curseforge/SETUP.md)).

Licence MIT.
