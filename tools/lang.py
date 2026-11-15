#!/usr/bin/env python3
"""Textes de Starchitect (FR + EN) -> fichiers de langue + nombre de variantes (Variants.java).

Usage : python3 tools/lang.py
Une entrée « liste » produit des clés numérotées (clé.0, clé.1…) : le critique en tire une au hasard.
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'starchitect', 'lang')
VARIANTS = os.path.join(ROOT, 'src', 'main', 'java', 'com', 'starchitect', 'review', 'Variants.java')

T = {}


def one(key, fr, en):
    T[key] = (fr, en)


def many(key, pairs):
    for i, (fr, en) in enumerate(pairs):
        T[f'{key}.{i}'] = (fr, en)


# ───────────────────────── Interface ─────────────────────────
one('key.categories.starchitect', 'Starchitect', 'Starchitect')
one('key.starchitect.invite', 'Inviter le critique', 'Invite the critic')
one('key.starchitect.guide', 'Ouvrir le guide', 'Open the guide')

one('starchitect.paper', 'LE PARPAING ENCHAÎNÉ', 'THE DAILY BRICK')
one('starchitect.paper.sub', 'Rubrique architecture · Jour %1$s · Critique n° %2$s',
    'Architecture column · Day %1$s · Review #%2$s')
one('starchitect.paper.motto', '« Nul n\'est censé ignorer la truelle. »', '"No one is above the trowel."')
one('starchitect.paper.sheet', 'LA FICHE', 'SCORECARD')
one('starchitect.paper.score', 'Note : %1$s/100', 'Score: %1$s/100')
one('starchitect.paper.by', 'par %s', 'by %s')
one('starchitect.paper.scroll', 'molette : lire la suite', 'scroll to read on')
one('starchitect.paper.visits', '%s visites', '%s visits')
one('starchitect.stars.0', 'Passez votre chemin', 'Keep walking')
one('starchitect.stars.1', 'Mérite le détour à pied', 'Worth a short walk')
one('starchitect.stars.2', 'Mérite un détour en minecart', 'Worth a minecart detour')
one('starchitect.stars.3', 'Vaut le voyage en élytres', 'Worth an elytra trip')
one('starchitect.stars.dirt', 'Cabane en terre d\'or', 'The Golden Dirt Hut')
one('starchitect.button.guide', 'Le guide', 'The guide')
one('starchitect.button.close', 'Fermer', 'Close')
one('starchitect.button.back', 'Retour', 'Back')
one('starchitect.nothing', 'rien', 'nothing')

one('starchitect.guide.title', 'Le Guide Starchitect', 'The Starchitect Guide')
one('starchitect.guide.summary', '%1$s construction(s) critiquée(s) · %2$s ★ au total',
    '%1$s build(s) reviewed · %2$s ★ in total')
one('starchitect.guide.rank', 'Votre titre : %s', 'Your title: %s')
one('starchitect.guide.next', 'Prochain titre à %s ★', 'Next title at %s ★')
one('starchitect.guide.empty', 'Aucune critique pour l\'instant.', 'No reviews yet.')
one('starchitect.guide.empty2', 'Approchez-vous d\'une construction et appuyez sur %s.',
    'Stand near a build and press %s.')
one('starchitect.guide.entry', 'jour %1$s · %2$s · X %3$s Z %4$s', 'day %1$s · %2$s · X %3$s Z %4$s')
one('starchitect.guide.open', 'Cliquez pour relire la critique', 'Click to read the review')
for i, (fr, en) in enumerate([
    ('Apprenti maçon', 'Apprentice mason'),
    ('Bâtisseur du dimanche', 'Weekend builder'),
    ('Maître d\'œuvre', 'Master builder'),
    ('Architecte reconnu', 'Renowned architect'),
    ('Starchitecte', 'Starchitect'),
    ('Légende du parpaing', 'Legend of the brick'),
]):
    one(f'starchitect.rank.{i}', fr, en)

one('starchitect.crit.palette', 'Palette', 'Palette')
one('starchitect.crit.detail', 'Relief', 'Depth')
one('starchitect.crit.light', 'Éclairage', 'Lighting')
one('starchitect.crit.windows', 'Fenêtres', 'Windows')
one('starchitect.crit.roof', 'Toiture', 'Roof')
one('starchitect.crit.symmetry', 'Symétrie', 'Symmetry')
one('starchitect.crit.comfort', 'Aménagement', 'Interior')
one('starchitect.crit.garden', 'Jardin', 'Garden')

# ───────────────────────── Critiques ─────────────────────────
one('starchitect.critic.augustin', 'Augustin de Saint-Parpaing', 'Sir Augustin Cornerstone')
one('starchitect.critic.augustin.title', 'Critique en chef depuis 1709', 'Chief critic since 1709')
one('starchitect.critic.brigitte', 'Brigitte Béton', 'Brigitte Concrete')
one('starchitect.critic.brigitte.title', 'Correspondante brutalisme', 'Brutalism correspondent')
one('starchitect.critic.gaston', 'Gaston Lagrange', 'Old Gaston Barnwood')
one('starchitect.critic.gaston.title', 'Chroniqueur rural', 'Countryside columnist')
one('starchitect.critic.zoe', 'Zoé Néon', 'Zoé Neon')
one('starchitect.critic.zoe.title', 'Design & tendances', 'Design & trends')

# ───────────────────────── Visite ─────────────────────────
one('starchitect.hud.arriving', 'Arrivée dans %2$s s…', 'Arriving in %2$s s…')
one('starchitect.hud.arriving2', '%s pour l\'accueillir tout de suite', '%s to welcome them right away')
one('starchitect.hud.inspecting', 'Inspection de votre construction…', 'Inspecting your build…')
one('starchitect.hud.writing', 'Rédaction de la critique…', 'Writing the review…')
many('starchitect.think', [
    ('Hmm.', 'Hmm.'),
    ('Tiens, tiens…', 'Well, well…'),
    ('Qui a eu cette idée ?', 'Whose idea was this?'),
    ('Je note, je note.', 'Noted. Noted.'),
    ('Ce bloc-là m\'interpelle.', 'That block speaks to me.'),
    ('Audacieux.', 'Bold.'),
    ('Hmm… courageux.', 'Hmm… brave.'),
    ('J\'ai déjà vu ça quelque part.', 'I\'ve seen this somewhere before.'),
    ('Mon monocle s\'embue.', 'My monocle is fogging up.'),
    ('Intéressant. Pas dans le bon sens.', 'Interesting. Not in a good way.'),
    ('On sent l\'inspiration. De qui ?', 'I sense inspiration. From whom?'),
    ('Ah ! Un escalier.', 'Ah! A stair.'),
    ('Respirons.', 'Deep breaths.'),
    ('Je devine une certaine… intention.', 'I detect a certain… intention.'),
    ('Ça penche, non ?', 'Is it leaning?'),
    ('Ne rien toucher. Ne rien toucher.', 'Don\'t touch anything. Don\'t touch anything.'),
])
many('starchitect.msg.nothing', [
    ('%s cherche une construction autour de vous, ne trouve que du paysage, et repart en soupirant.',
     '%s looks around for a build, finds only scenery, and leaves with a sigh.'),
    ('%s a fait le tour du coin : pas l\'ombre d\'une construction. La nature, elle, se passe de critique.',
     '%s walked the whole area: not a build in sight. Nature, at least, needs no review.'),
])
one('starchitect.msg.cancel', 'La visite est annulée : vous êtes parti trop loin.', 'The visit is off: you wandered too far away.')
one('starchitect.msg.busy', 'Un critique est déjà en visite.', 'A critic is already visiting.')
one('starchitect.msg.published', 'Critique publiée : %1$s — %2$s', 'Review published: %1$s — %2$s')
one('starchitect.msg.rumor', 'On murmure qu\'un critique rôde dans les parages…', 'Rumour has it a critic is lurking nearby…')
one('starchitect.cmd.critic', 'Prochain critique : %s', 'Next critic: %s')
one('starchitect.cmd.critic.random', 'au hasard', 'random')
one('starchitect.cmd.unknown', 'Critique inconnu : %s', 'Unknown critic: %s')
one('starchitect.cmd.reset', 'Le guide de ce monde a été effacé.', 'This world\'s guide has been erased.')
one('starchitect.cmd.forget', 'Critique retirée du guide : %s', 'Review removed from the guide: %s')
one('starchitect.cmd.none', 'Aucune critique à moins de 32 blocs.', 'No review within 32 blocks.')
one('starchitect.cmd.rename', 'Construction renommée : %s', 'Build renamed: %s')

one('starchitect.time.morning', 'au petit matin', 'at dawn')
one('starchitect.time.day', 'en plein jour', 'in broad daylight')
one('starchitect.time.evening', 'au crépuscule', 'at dusk')
one('starchitect.time.night', 'en pleine nuit', 'in the dead of night')
one('starchitect.weather.clear', 'sous un ciel limpide', 'under a clear sky')
one('starchitect.weather.rain', 'sous une pluie battante', 'in the pouring rain')
one('starchitect.weather.thunder', 'sous l\'orage', 'in a thunderstorm')

# ───────────────────────── Noms des constructions ─────────────────────────
for t, fr, en in [
    ('shack', 'Le Cabanon %s', 'The %s Shack'),
    ('cottage', 'La Chaumière %s', 'The %s Cottage'),
    ('house', 'La Maison %s', 'The %s House'),
    ('villa', 'La Villa %s', 'The %s Villa'),
    ('manor', 'Le Manoir %s', 'The %s Manor'),
    ('palace', 'Le Palais %s', 'The %s Palace'),
    ('tower', 'La Tour %s', 'The %s Tower'),
    ('bunker', 'Le Bunker %s', 'The %s Bunker'),
    ('hut', 'La Cabane %s', 'The %s Hut'),
    ('greenhouse', 'La Serre %s', 'The %s Greenhouse'),
]:
    one(f'starchitect.name.{t}', fr, en)

# famille : (complément du nom, nom avec article) en FR ; (adjectif, nom) en EN
MATS = [
    ('oak', 'de chêne', 'le chêne', 'Oak', 'oak'),
    ('dark_oak', 'de chêne noir', 'le chêne noir', 'Dark Oak', 'dark oak'),
    ('pale_oak', 'de chêne pâle', 'le chêne pâle', 'Pale Oak', 'pale oak'),
    ('spruce', 'de sapin', 'le sapin', 'Spruce', 'spruce'),
    ('birch', 'de bouleau', 'le bouleau', 'Birch', 'birch'),
    ('jungle', 'd\'acajou', 'l\'acajou', 'Jungle', 'jungle wood'),
    ('acacia', 'd\'acacia', 'l\'acacia', 'Acacia', 'acacia'),
    ('mangrove', 'de palétuvier', 'le palétuvier', 'Mangrove', 'mangrove'),
    ('cherry', 'de cerisier', 'le cerisier', 'Cherry', 'cherry wood'),
    ('bamboo', 'de bambou', 'le bambou', 'Bamboo', 'bamboo'),
    ('crimson', 'carmin', 'le bois carmin', 'Crimson', 'crimson wood'),
    ('warped', 'biscornue', 'le bois biscornu', 'Warped', 'warped wood'),
    ('deepslate', 'd\'ardoise', 'l\'ardoise', 'Slate', 'deepslate'),
    ('blackstone', 'de roche noire', 'la roche noire', 'Blackstone', 'blackstone'),
    ('mossy', 'de mousse', 'la pierre moussue', 'Mossy', 'mossy stone'),
    ('cobble', 'de pavés', 'les pavés', 'Cobblestone', 'cobblestone'),
    ('nether', 'du Nether', 'la brique du Nether', 'Nether', 'nether brick'),
    ('mud', 'de torchis', 'le torchis', 'Mud', 'mud brick'),
    ('sandstone', 'de grès', 'le grès', 'Sandstone', 'sandstone'),
    ('stone', 'de pierre', 'la pierre', 'Stone', 'stone'),
    ('brick', 'de brique', 'la brique', 'Brick', 'brick'),
    ('quartz', 'de quartz', 'le quartz', 'Quartz', 'quartz'),
    ('concrete', 'de béton', 'le béton', 'Concrete', 'concrete'),
    ('terracotta', 'de terre cuite', 'la terre cuite', 'Terracotta', 'terracotta'),
    ('glass', 'de verre', 'le verre', 'Glass', 'glass'),
    ('wool', 'de laine', 'la laine', 'Wool', 'wool'),
    ('dirt', 'en terre', 'la terre', 'Dirt', 'dirt'),
    ('prismarine', 'de prismarine', 'la prismarine', 'Prismarine', 'prismarine'),
    ('copper', 'de cuivre', 'le cuivre', 'Copper', 'copper'),
    ('purpur', 'de purpur', 'le purpur', 'Purpur', 'purpur'),
    ('obsidian', 'd\'obsidienne', 'l\'obsidienne', 'Obsidian', 'obsidian'),
    ('iron', 'de fer', 'le fer', 'Iron', 'iron'),
    ('gold', 'd\'or', 'l\'or', 'Golden', 'gold'),
    ('diamond', 'de diamant', 'le diamant', 'Diamond', 'diamond'),
    ('emerald', 'd\'émeraude', 'l\'émeraude', 'Emerald', 'emerald'),
    ('netherite', 'de netherite', 'la netherite', 'Netherite', 'netherite'),
    ('amethyst', 'd\'améthyste', 'l\'améthyste', 'Amethyst', 'amethyst'),
    ('end', 'de l\'End', 'la pierre de l\'End', 'End Stone', 'end stone'),
    ('resin', 'de résine', 'la résine', 'Resin', 'resin'),
    ('mystery', 'mystère', 'ce matériau mystère', 'Mystery', 'that mystery material'),
]
for fam, fr_of, fr_noun, en_adj, en_noun in MATS:
    one(f'starchitect.mat.{fam}', fr_of, en_adj)
    one(f'starchitect.matn.{fam}', fr_noun, en_noun)

# ───────────────────────── La critique ─────────────────────────
many('starchitect.headline.0', [
    ('%s : on a connu des grottes plus accueillantes', '%s: we\'ve seen cosier caves'),
    ('%s : le néant a trouvé son architecte', '%s: the void finally hired an architect'),
    ('%s, ou l\'art de gâcher des blocs', '%s, or the art of wasting blocks'),
])
many('starchitect.headline.1', [
    ('%s : du potentiel, de la bonne volonté… et du travail', '%s: potential, goodwill… and homework'),
    ('%s : honnête, sans plus', '%s: honest, nothing more'),
    ('%s : une étoile, et de justesse', '%s: one star, by a whisker'),
])
many('starchitect.headline.2', [
    ('%s : une très belle surprise', '%s: a lovely surprise'),
    ('%s : on y passerait bien la nuit (sans monstres)', '%s: we\'d happily spend the night (no mobs)'),
    ('%s : l\'élégance discrète', '%s: quiet elegance'),
])
many('starchitect.headline.3', [
    ('%s : un chef-d\'œuvre, tout simplement', '%s: simply a masterpiece'),
    ('%s : les villageois en pleurent', '%s: the villagers are in tears'),
    ('%s : trois étoiles et un monocle tombé', '%s: three stars and a dropped monocle'),
])
many('starchitect.headline.dirt', [
    ('%s : la terre, encore la terre, toujours la terre', '%s: dirt, more dirt, nothing but dirt'),
    ('%s : même l\'âge de pierre était en pierre', '%s: even the Stone Age was made of stone'),
])

# %1$s = moment, %2$s = météo, %3$s = nom de la construction
many('starchitect.intro.augustin', [
    ('Je suis arrivé %1$s, %2$s, le monocle bien vissé, devant %3$s.',
     'I arrived %1$s, %2$s, monocle firmly in place, before %3$s.'),
    ('C\'est %1$s, %2$s, que j\'ai poussé la porte de « %3$s », mes gants de critique enfilés.',
     'It was %1$s, %2$s, that I stepped into %3$s, critic\'s gloves on.'),
    ('On m\'avait parlé de « %3$s ». J\'y suis allé %1$s, %2$s, sans prévenir, comme toujours.',
     'I had heard of %3$s. I went %1$s, %2$s, unannounced, as always.'),
])
many('starchitect.intro.brigitte', [
    ('Visite de « %3$s » %1$s, %2$s. J\'avais apporté mon niveau à bulle.',
     'Inspected %3$s %1$s, %2$s. I brought my spirit level.'),
    ('%3$s. Inspection %1$s, %2$s. Je ne suis pas venue pour être charmée.',
     '%3$s. Inspection %1$s, %2$s. I did not come here to be charmed.'),
    ('Le béton ne ment jamais. Les bâtisseurs, si. Rendez-vous devant « %3$s », %1$s, %2$s.',
     'Concrete never lies. Builders do. I met %3$s %1$s, %2$s.'),
])
many('starchitect.intro.gaston', [
    ('Je suis passé devant « %3$s » %1$s, %2$s, avec mes bottes et mon panier.',
     'I stopped by %3$s %1$s, %2$s, boots on and basket in hand.'),
    ('Ah, « %3$s » ! J\'y suis allé %1$s, %2$s, en suivant l\'odeur du feu de bois.',
     'Ah, %3$s! I went %1$s, %2$s, following the smell of a wood fire.'),
    ('Rien ne vaut une visite %1$s, %2$s. Direction « %3$s », à travers champs.',
     'Nothing beats a visit %1$s, %2$s. Off to %3$s, across the fields.'),
])
many('starchitect.intro.zoe', [
    ('Visite de « %3$s » %1$s, %2$s. Mes abonnés attendaient ça.',
     'Visited %3$s %1$s, %2$s. My followers have been waiting for this.'),
    ('%3$s, %1$s, %2$s : j\'avais sorti mon appareil photo avant même d\'entrer.',
     '%3$s, %1$s, %2$s: my camera was out before I even got in.'),
    ('On me promettait du design. Verdict devant « %3$s », %1$s, %2$s.',
     'I was promised design. Verdict at %3$s, %1$s, %2$s.'),
])
# %1$s = étoiles d'avant, %2$s = étoiles maintenant, %3$s = numéro de la visite
many('starchitect.revisit.up', [
    ('C\'est ma visite n° %3$s, et quel progrès : on passe de %1$s à %2$s étoile(s). Quelqu\'un a écouté.',
     'This is visit #%3$s, and what progress: from %1$s to %2$s star(s). Someone listened.'),
    ('Pour cette visite n° %3$s, j\'ai failli ne pas reconnaître les lieux. En bien.',
     'On visit #%3$s, I barely recognised the place. In a good way.'),
])
many('starchitect.revisit.same', [
    ('Visite n° %3$s. Des changements, oui. Des progrès… c\'est une autre histoire.',
     'Visit #%3$s. Changes, yes. Progress… that\'s another story.'),
    ('Je reviens, je regarde, je soupire : toujours %2$s étoile(s).',
     'I come back, I look, I sigh: still %2$s star(s).'),
])
many('starchitect.revisit.down', [
    ('Visite n° %3$s, et je regrette déjà la précédente : on tombe de %1$s à %2$s étoile(s).',
     'Visit #%3$s, and I already miss the last one: down from %1$s to %2$s star(s).'),
    ('Qu\'est-il arrivé ici ? La dernière fois, c\'était %1$s étoile(s).',
     'What happened here? Last time it was %1$s star(s).'),
])

C = {}
# Palette : %1$s = nombre de matériaux, %2$s = bloc dominant, %3$s = part du bloc dominant
C['palette'] = [
    [('La palette ? %2$s, et encore %2$s : %3$s %% de la façade. L\'œil s\'endort avant d\'arriver à la porte.',
      'The palette? %2$s, then more %2$s: %3$s%% of the facade. The eye falls asleep before reaching the door.'),
     ('%1$s matériau(x) en tout et pour tout. À ce niveau, ce n\'est plus une palette, c\'est un uniforme.',
      '%1$s material(s) in total. At this point it\'s not a palette, it\'s a uniform.')],
    [('Avec %1$s matériaux, la palette se cherche encore ; « %2$s » écrase un peu le reste.',
      'With %1$s materials, the palette is still finding itself; %2$s rather crushes the rest.'),
     ('Les matériaux se parlent poliment sans vraiment s\'entendre. « %2$s » mène la conversation.',
      'The materials chat politely without really getting along. %2$s leads the conversation.')],
    [('Belle palette : %1$s matériaux qui jouent ensemble, « %2$s » en chef d\'orchestre.',
      'A fine palette: %1$s materials playing together, with %2$s conducting.'),
     ('Les matériaux sont choisis avec goût, et « %2$s » sait rester à sa place.',
      'Tasteful choice of materials, and %2$s knows its place.')],
    [('La palette est une leçon : %1$s matériaux en parfait accord. J\'ai pris des notes.',
      'The palette is a masterclass: %1$s materials in perfect harmony. I took notes.'),
     ('Chaque bloc semble avoir passé un entretien d\'embauche. « %2$s », notamment, est irréprochable.',
      'Every block seems to have passed a job interview. %2$s, in particular, is flawless.')],
]
# Relief : %1$s = part de blocs de relief
C['detail'] = [
    [('Des murs plats comme une crêpe : à peine %1$s %% de relief. Un escalier, une dalle, une trappe… n\'importe quoi !',
      'Walls as flat as a pancake: barely %1$s%% depth. A stair, a slab, a trapdoor… anything!'),
     ('Aucun relief. La façade a la profondeur d\'un panneau publicitaire.',
      'No depth whatsoever. The facade has all the relief of a billboard.')],
    [('Quelques détails (%1$s %% de relief) tentent de casser la monotonie. Ils sont en sous-effectif.',
      'A few details (%1$s%% depth) try to break the monotony. They are understaffed.'),
     ('Le relief est timide, comme s\'il demandait la permission d\'exister.',
      'The depth is shy, as if asking permission to exist.')],
    [('Un vrai travail de relief : murets, dalles et escaliers donnent du caractère à la façade.',
      'Real work on depth: walls, slabs and stairs give the facade character.'),
     ('La façade respire grâce à %1$s %% de blocs de relief bien placés.',
      'The facade breathes thanks to %1$s%% of well-placed detail blocks.')],
    [('Le relief est sculpté avec une précision d\'orfèvre. Chaque ombre est à sa place.',
      'The depth is carved with a jeweller\'s precision. Every shadow is where it belongs.'),
     ('Escaliers, dalles, murets : %1$s %% de détails, et pas un de trop. Du grand art.',
      'Stairs, slabs, walls: %1$s%% detail, and not one too many. High art.')],
]
# Éclairage : %1$s = coins dans le noir, %2$s = sources de lumière
C['light'] = [
    [('L\'éclairage ? %1$s recoins plongés dans le noir complet. Les zombies ont déjà signé le bail.',
      'Lighting? %1$s spots in pitch darkness. The zombies have already signed the lease.'),
     ('Il fait si sombre à l\'intérieur que j\'ai rédigé cette critique à tâtons.',
      'It\'s so dark inside I wrote this review by touch.')],
    [('%2$s source(s) de lumière, c\'est un début ; il reste %1$s coins où un creeper pourrait faire la sieste.',
      '%2$s light source(s) is a start; there are still %1$s corners where a creeper could nap.'),
     ('Éclairage approximatif : on y voit, mais on préfère ne pas regarder de trop près.',
      'Approximate lighting: you can see, but you\'d rather not look too closely.')],
    [('Un éclairage soigné, chaleureux, qui donne envie d\'entrer.',
      'Careful, warm lighting that invites you in.'),
     ('%2$s sources de lumière bien réparties : pas un monstre ne s\'y risquerait.',
      '%2$s well-placed light sources: no monster would dare.')],
    [('La lumière est mise en scène comme au théâtre. Pas une ombre de trop, pas un monstre en vue.',
      'The light is staged like a theatre. Not one shadow too many, not a monster in sight.'),
     ('L\'éclairage est d\'une justesse rare. Même ma chandelle de voyage s\'est sentie inutile.',
      'Lighting this right is rare. Even my travel candle felt useless.')],
]
# Fenêtres : %1$s = part de vitrage, %2$s = blocs de verre
C['windows'] = [
    [('Des fenêtres ? Aucune, ou presque. On se croirait dans un coffre.',
      'Windows? None, or nearly. It feels like living in a chest.'),
     ('Sans fenêtres, une maison n\'est qu\'un placard avec des ambitions.',
      'Without windows, a house is just a cupboard with ambitions.')],
    [('%2$s bloc(s) de verre : la lumière entre, mais sur la pointe des pieds.',
      '%2$s glass block(s): the light gets in, but on tiptoe.'),
     ('Les fenêtres sont là, un peu perdues dans la façade, comme des invités qui ne connaissent personne.',
      'The windows are there, a little lost in the facade, like guests who don\'t know anyone.')],
    [('De belles ouvertures (%1$s %% de vitrage) : la façade a du regard.',
      'Lovely openings (%1$s%% glazing): the facade has a gaze.'),
     ('Fenêtres bien proportionnées : on voit dehors, et c\'est déjà beaucoup.',
      'Well-proportioned windows: you can see outside, which is already a lot.')],
    [('Les fenêtres sont parfaitement dosées. La lumière y entre comme une invitée de marque.',
      'The windows are perfectly measured. Light enters like a guest of honour.'),
     ('%1$s %% de vitrage, idéalement placé. Je suis resté une heure à regarder le paysage.',
      '%1$s%% glazing, ideally placed. I spent an hour just looking at the view.')],
]
# Toiture : %1$s = part de toit en pente
C['roof'] = [
    [('Le toit, ce grand absent. Ou alors une dalle plate posée là comme un couvercle de boîte à chaussures.',
      'The roof, conspicuously absent. Or a flat lid plonked on top like a shoebox.'),
     ('Toit plat, ambitions plates. La pluie, elle, est ravie.',
      'Flat roof, flat ambitions. The rain, however, is delighted.')],
    [('La toiture hésite entre la pente et le plat (%1$s %% en pente). Il faudra choisir.',
      'The roof can\'t decide between sloped and flat (%1$s%% sloped). Pick one.'),
     ('Un toit correct, sans plus : il protège, il ne raconte rien.',
      'A decent roof, nothing more: it protects, it says nothing.')],
    [('Une belle toiture en pente, qui donne enfin une silhouette à l\'ensemble.',
      'A fine sloped roof that finally gives the whole thing a silhouette.'),
     ('Le toit est bien dessiné ; on le reconnaîtrait de loin.',
      'The roof is well drawn; you\'d recognise it from afar.')],
    [('Le toit est une merveille : des pentes justes, un faîtage net. J\'en ai ôté mon chapeau.',
      'The roof is a marvel: true slopes, a crisp ridge. I took my hat off.'),
     ('Une toiture digne d\'une carte postale. %1$s %% de pentes, zéro faute.',
      'A postcard-worthy roof. %1$s%% slopes, zero mistakes.')],
]
# Symétrie : %1$s = part symétrique
C['symmetry'] = [
    [('La symétrie a été bannie des lieux ; on ne sait jamais quel côté regarder.',
      'Symmetry has been banished; you never know which side to look at.'),
     ('Chaque côté semble avoir été construit par quelqu\'un qui n\'a jamais vu l\'autre.',
      'Each side seems to have been built by someone who never met the other.')],
    [('Une symétrie à %1$s %% : l\'intention est là, la règle un peu moins.',
      '%1$s%% symmetry: the intent is there, the ruler less so.'),
     ('L\'ensemble se tient à peu près droit, comme un villageois après la fête.',
      'It stands more or less upright, like a villager after a party.')],
    [('Une composition équilibrée, symétrique à %1$s %%, sans tomber dans le scolaire.',
      'A balanced composition, %1$s%% symmetrical, without being stiff.'),
     ('Les volumes se répondent avec élégance.',
      'The volumes answer each other elegantly.')],
    [('Symétrie à %1$s %%. J\'ai plié le plan en deux : tout tombe pile.',
      '%1$s%% symmetry. I folded the plan in half: everything lines up.'),
     ('Un équilibre parfait. On pourrait poser un miroir au milieu sans rien remarquer.',
      'Perfect balance. You could put a mirror in the middle and not notice.')],
]
# Aménagement : %1$s = sortes de mobilier, %2$s = un meuble
C['comfort'] = [
    [('L\'intérieur est vide. Pas un lit, pas un coffre, pas même une chaise. On y dort debout ?',
      'The interior is empty. Not a bed, not a chest, not even a chair. Do you sleep standing up?'),
     ('Aménagement : néant. L\'écho de mes pas a été mon seul compagnon.',
      'Interior: nothing. The echo of my footsteps was my only company.')],
    [('%1$s sorte(s) de mobilier : c\'est vivable, sans plus. Il faudra autre chose que « %2$s » pour en faire un foyer.',
      '%1$s kind(s) of furniture: livable, barely. It\'ll take more than "%2$s" to make it a home.'),
     ('L\'aménagement est spartiate. Fonctionnel, disons.',
      'The interior is spartan. Functional, let\'s say.')],
    [('Un intérieur bien pensé : %1$s sortes de mobilier, et l\'on s\'y sent chez soi.',
      'A thoughtful interior: %1$s kinds of furniture, and it feels like home.'),
     ('On a envie de poser ses affaires et de rester. « %2$s » en tête, tout est à sa place.',
      'You want to drop your bags and stay. Starting with "%2$s", everything is in its place.')],
    [('L\'aménagement est somptueux : %1$s sortes de mobilier, chaque recoin a son histoire.',
      'The interior is sumptuous: %1$s kinds of furniture, every corner tells a story.'),
     ('Un intérieur digne d\'un magazine. Je me suis assis, et j\'ai failli ne jamais repartir.',
      'A magazine-worthy interior. I sat down and nearly never left.')],
]
# Jardin : %1$s = plantes
C['garden'] = [
    [('Pas une fleur, pas une haie. Le jardin, c\'est la pelouse… et encore.',
      'Not a flower, not a hedge. The garden is the lawn… barely.'),
     ('Autour, rien ne pousse. Même les abeilles ont fait demi-tour.',
      'Nothing grows around it. Even the bees turned back.')],
    [('Quelques plantes (%1$s) font de leur mieux pour égayer les abords.',
      'A few plants (%1$s) do their best to brighten the surroundings.'),
     ('Le jardin existe, timidement. Une fleur de plus ne ferait pas de mal.',
      'The garden exists, timidly. One more flower wouldn\'t hurt.')],
    [('Un joli jardin : %1$s plantes qui adoucissent la silhouette de l\'ensemble.',
      'A pretty garden: %1$s plants that soften the silhouette.'),
     ('Les abords sont soignés, fleuris, accueillants.',
      'The surroundings are tidy, flowery, welcoming.')],
    [('Le jardin est un poème : %1$s plantes, et pas une mauvaise herbe.',
      'The garden is a poem: %1$s plants, and not a single weed.'),
     ('J\'ai passé plus de temps dans le jardin que dans la maison, et je ne le regrette pas.',
      'I spent more time in the garden than in the house, and I don\'t regret it.')],
]
for crit, tiers in C.items():
    for tier, pairs in enumerate(tiers):
        many(f'starchitect.c.{crit}.{tier}', pairs)

# Goûts : %1$s = matériau (FR : avec son article)
many('starchitect.love.augustin', [
    ('Et puis il y a %1$s : noble, intemporel, exactement ce que j\'aime.',
     'And then there\'s the %1$s: noble, timeless, exactly my taste.'),
    ('J\'ai noté %1$s avec un sourire, chose rare.', 'I noted the %1$s with a smile, which is rare.'),
])
many('starchitect.hate.augustin', [
    ('Hélas, %1$s partout. Je n\'ai rien contre, sauf tout.',
     'Alas, %1$s everywhere. I have nothing against it, except everything.'),
    ('Que fait %1$s ici ? Ce matériau n\'a rien à faire dans une demeure qui se respecte.',
     'What is the %1$s doing here? It has no place in a self-respecting home.'),
])
many('starchitect.love.brigitte', [
    ('Côté matériaux, %1$s, brut et assumé : enfin du courage.',
     'Material-wise, %1$s, raw and unashamed: finally some courage.'),
    ('J\'ai salué %1$s, employé sans fioritures.', 'I saluted the %1$s, used without frills.'),
])
many('starchitect.hate.brigitte', [
    ('Côté matériaux, %1$s domine. On se croirait dans un chalet pour touristes.',
     'Material-wise, %1$s dominates. It feels like a tourist chalet.'),
    ('Il y a là %1$s en excès. Je préfère les matériaux qui ne craquent pas.',
     'There\'s an excess of %1$s. I prefer materials that don\'t creak.'),
])
many('starchitect.love.gaston', [
    ('Et puis %1$s ! Ça sent bon la campagne, j\'en ai les larmes aux yeux.',
     'And then the %1$s! It smells of the countryside, I\'m tearing up.'),
    ('J\'aime %1$s ici : ça respire l\'authentique.', 'I love the %1$s here: it feels authentic.'),
])
many('starchitect.hate.gaston', [
    ('Hélas, %1$s, si froid… On dirait une gare.', 'Alas, the %1$s, so cold… It looks like a train station.'),
    ('Pourquoi %1$s ? On n\'est pas en ville, ici !', 'Why %1$s? We\'re not in the city!'),
])
many('starchitect.love.zoe', [
    ('Mention spéciale pour %1$s : très tendance, très photogénique.',
     'Special mention for the %1$s: very on-trend, very photogenic.'),
    ('J\'adore %1$s ici. Ça va faire le tour des réseaux.', 'I adore the %1$s here. It\'ll go viral.'),
])
many('starchitect.hate.zoe', [
    ('Par contre, %1$s… C\'est tellement l\'an dernier.', 'The %1$s, however… so last year.'),
    ('Et %1$s ? Sérieusement ? Même mes grands-parents sont passés à autre chose.',
     'And the %1$s? Seriously? Even my grandparents have moved on.'),
])

many('starchitect.remark.dirt', [
    ('Une cabane en terre. En terre. Le jury m\'autorise à décerner la Cabane en terre d\'or, une distinction qui n\'honore personne.',
     'A dirt hut. Dirt. The jury allows me to award the Golden Dirt Hut, an honour that honours no one.'),
    ('J\'ai vu des terriers de lapin plus ambitieux. Les lapins, au moins, creusent.',
     'I\'ve seen more ambitious rabbit holes. At least rabbits dig.'),
])
many('starchitect.remark.nodoor', [
    ('Détail amusant : il n\'y a pas de porte. Je suis entré comment, au juste ?',
     'Fun detail: there is no door. How did I even get in?'),
    ('Pas de porte. Pratique pour les courants d\'air, moins pour les zombies.',
     'No door. Great for drafts, less so for zombies.'),
])
many('starchitect.remark.mobs', [
    ('J\'ai compté %1$s endroits assez sombres pour qu\'un monstre y apparaisse. Je suis reparti en courant.',
     'I counted %1$s spots dark enough to spawn a monster. I left running.'),
    ('Avec %1$s coins dans le noir, ce n\'est plus une maison, c\'est un élevage de monstres.',
     'With %1$s pitch-dark corners, this isn\'t a house, it\'s a mob farm.'),
])
many('starchitect.remark.noroof', [
    ('Aucune pièce n\'est couverte. Maison ou sculpture ? Je dirai sculpture, par charité.',
     'Not a single covered room. Is it a house or a sculpture? I\'ll say sculpture, out of kindness.'),
    ('Rien n\'est couvert : au premier orage, tout le monde dehors… ce qui ne change rien.',
     'Nothing is covered: at the first storm, everyone out… which changes nothing.'),
])
many('starchitect.remark.cat', [
    ('Un chat habite ici. Le chat, lui, mérite trois étoiles.', 'A cat lives here. The cat, at least, deserves three stars.'),
    ('J\'ai été accueilli par un chat, qui m\'a jugé plus sévèrement que je ne juge la maison.',
     'I was greeted by a cat, who judged me harder than I\'m judging the house.'),
])
many('starchitect.remark.dog', [
    ('Un chien fidèle garde la maison. Il a reniflé mon carnet avec méfiance.',
     'A loyal dog guards the house. It sniffed my notebook suspiciously.'),
    ('Le chien de la maison m\'a fait la fête. Je lui accorde un point de plus.',
     'The house dog gave me a warm welcome. One extra point for him.'),
])
many('starchitect.remark.parrot', [
    ('Un perroquet a répété mes notes à voix haute. J\'ai dû les adoucir.',
     'A parrot repeated my notes out loud. I had to soften them.'),
    ('Le perroquet de la maison m\'a traité de « crrritique ». Il n\'a pas tort.',
     'The house parrot called me a "crrritic". Fair enough.'),
])
many('starchitect.remark.villager', [
    ('Des villageois rôdent autour. Ils semblent satisfaits, mais ils sont satisfaits de tout.',
     'Villagers are wandering around. They seem satisfied, but they\'re satisfied with anything.'),
    ('Un villageois m\'a proposé trois émeraudes contre une bonne note. Refusé.',
     'A villager offered me three emeralds for a good review. Declined.'),
])
many('starchitect.remark.golem', [
    ('Un golem de fer veille sur les lieux. Je me suis tenu à carreau.', 'An iron golem watches over the place. I behaved.'),
    ('La présence d\'un golem de fer rend toute critique plus… diplomatique.',
     'The presence of an iron golem makes any review more… diplomatic.'),
])
many('starchitect.remark.horse', [
    ('Une monture attend devant la porte : le sens de l\'accueil, enfin.', 'A steed waits by the door: finally, a sense of hospitality.'),
    ('Il y a même un cheval. Le parking est bien conçu.', 'There\'s even a horse. The parking is well designed.'),
])
many('starchitect.remark.rainbow', [
    ('%1$s couleurs différentes ! J\'ai cru entrer dans une boîte de crayons.',
     '%1$s different colours! I thought I\'d walked into a box of crayons.'),
    ('La façade compte %1$s couleurs. Mes yeux demandent un arrêt maladie.',
     'The facade has %1$s colours. My eyes are filing for sick leave.'),
])
many('starchitect.remark.cobble', [
    ('La pierre lisse existe, vous savez. Le four aussi. Les deux ensemble font des miracles.',
     'Smooth stone exists, you know. So does the furnace. Together they work wonders.'),
    ('Tant de pavés… On sent l\'abri de la première nuit, resté là pour toujours.',
     'So much cobblestone… This is a first-night shelter that never moved on.'),
])
many('starchitect.remark.greenhouse', [
    ('Plus de verre que de murs : on vit ici comme dans un aquarium.', 'More glass than wall: living here is like living in an aquarium.'),
    ('Une maison de verre. Courageux, côté vie privée.', 'A glass house. Brave, privacy-wise.'),
])
many('starchitect.remark.tower', [
    ('%1$s blocs de haut ! Le vertige fait partie de l\'expérience.', '%1$s blocks tall! Vertigo is part of the experience.'),
    ('Une tour de %1$s blocs : on y monte, on en redescend, on y repense.', 'A %1$s-block tower: you go up, you come down, you think about it.'),
])
many('starchitect.remark.tiny', [
    ('%1$s blocs en tout. Petit, mais je ne suis pas là pour mesurer. Enfin, si.',
     '%1$s blocks in all. Small, but I\'m not here to measure. Well, I am.'),
    ('C\'est minuscule (%1$s blocs). Charmant ou paresseux : je n\'ai pas tranché.',
     'It\'s tiny (%1$s blocks). Charming or lazy: I haven\'t decided.'),
])
many('starchitect.remark.giant', [
    ('%1$s blocs ! J\'ai dû prendre un minecart pour aller du salon à la cuisine.',
     '%1$s blocks! I needed a minecart to get from the living room to the kitchen.'),
    ('Un édifice de %1$s blocs : il m\'a fallu deux jours pour en faire le tour.',
     'A %1$s-block building: it took me two days to walk around it.'),
])
many('starchitect.remark.bling', [
    ('Des blocs précieux en façade. Le bon goût n\'a pas de prix ; visiblement, son contraire non plus.',
     'Precious blocks on the facade. Good taste is priceless; apparently the opposite isn\'t.'),
    ('Or, diamant, émeraude… On a confondu maison et coffre-fort.', 'Gold, diamond, emerald… Someone confused a house with a vault.'),
])

# %1$s = nom de la construction, %2$s = étoiles
many('starchitect.verdict.0', [
    ('Verdict : pas d\'étoile pour « %1$s ». Mais il n\'est jamais trop tard pour apprendre.',
     'Verdict: no star for %1$s. But it\'s never too late to learn.'),
    ('Pas d\'étoile. Je reviendrai… si l\'on m\'y oblige.', 'No star. I\'ll come back… if forced to.'),
])
many('starchitect.verdict.1', [
    ('Verdict : une étoile. « %1$s » mérite le détour, à pied et sans se presser.',
     'Verdict: one star. %1$s is worth a short walk, at a leisurely pace.'),
    ('Une étoile, méritée. Le chantier continue, et je suis curieux de la suite.',
     'One star, earned. The work continues, and I\'m curious what comes next.'),
])
many('starchitect.verdict.2', [
    ('Verdict : deux étoiles. « %1$s » mérite un détour en minecart.', 'Verdict: two stars. %1$s is worth a minecart detour.'),
    ('Deux étoiles, avec les félicitations du jury. Il manque peu de chose pour la consécration.',
     'Two stars, with the jury\'s compliments. Very little stands between this and glory.'),
])
many('starchitect.verdict.3', [
    ('Verdict : trois étoiles. « %1$s » vaut le voyage en élytres.', 'Verdict: three stars. %1$s is worth an elytra trip.'),
    ('Trois étoiles. Je range mon monocle : il n\'y a plus rien à critiquer.',
     'Three stars. I\'m putting my monocle away: there\'s nothing left to criticise.'),
])
many('starchitect.verdict.dirt', [
    ('Verdict : la Cabane en terre d\'or. Félicitations ? Non.', 'Verdict: the Golden Dirt Hut. Congratulations? No.'),
    ('Verdict : aucune étoile, mais une pelle d\'honneur, pour tout reconstruire.',
     'Verdict: no stars, but an honorary shovel, to rebuild everything.'),
])

# ───────────────────────── 1.1 : conseils, photo, mètre de chantier ─────────────────────────
one('starchitect.tips', 'LES CONSEILS DU CRITIQUE', 'THE CRITIC\'S ADVICE')
one('starchitect.tip.none', 'Rien à redire. Surtout, ne touchez plus à rien !', 'Nothing to add. Above all, don\'t touch a thing!')
one('starchitect.tip.dirt', 'Remplacez la terre : des planches, de la pierre taillée, n\'importe quoi d\'autre.',
    'Replace the dirt: planks, stone bricks, anything else.')
one('starchitect.tip.palette.few', 'Ajoutez deux ou trois matériaux pour accompagner « %1$s » : soubassement en pierre, poutres, encadrements.',
    'Add two or three materials to go with %1$s: a stone base, beams, window frames.')
one('starchitect.tip.palette.many', '%1$s matériaux, c\'est trop : gardez-en six ou sept qui vont ensemble.',
    '%1$s materials is too many: keep six or seven that go together.')
one('starchitect.tip.palette.dominant', '« %1$s » fait %2$s %% de la construction : variez avec un autre matériau pour les angles et les bordures.',
    '%1$s makes up %2$s%% of the build: break it up with another material for corners and edges.')
one('starchitect.tip.detail', 'Habillez la façade : environ %1$s escaliers, dalles, murets ou trappes de plus (corniches, rebords, poutres).',
    'Dress up the facade: about %1$s more stairs, slabs, walls or trapdoors (cornices, sills, beams).')
one('starchitect.tip.light', 'Éclairez les %1$s coins restés dans le noir (bouton « Montrer » : ils sont signalés en rouge).',
    'Light up the %1$s pitch-dark spots (the "Show me" button marks them in red).')
one('starchitect.tip.light.none', 'Ajoutez quelques lanternes ou torches : une maison sans lumière n\'accueille personne.',
    'Add a few lanterns or torches: a house with no light welcomes no one.')
one('starchitect.tip.windows', 'Percez environ %1$s fenêtres de plus (vitres ou panneaux de verre).', 'Add about %1$s more windows (glass or glass panes).')
one('starchitect.tip.windows.many', 'Trop de verre : remplacez quelques vitres par des murs pleins.', 'Too much glass: swap a few panes for solid wall.')
one('starchitect.tip.roof.none', 'Couvrez la construction : un toit en escaliers, en pente des deux côtés.', 'Put a roof on it: stairs, sloping down on both sides.')
one('starchitect.tip.roof', 'Remplacez le toit plat par des escaliers en pente, avec une rangée de dalles au faîte.',
    'Swap the flat roof for sloping stairs, with a row of slabs on the ridge.')
one('starchitect.tip.symmetry', 'Équilibrez les deux moitiés de la façade : mêmes fenêtres, même largeur de chaque côté.',
    'Balance both halves of the facade: same windows, same width on each side.')
one('starchitect.tip.comfort', 'Meublez l\'intérieur (%1$s sorte(s) de meubles) : lit, coffre, établi, bibliothèque, tableau, pot de fleurs…',
    'Furnish the inside (%1$s kind(s) of furniture): bed, chest, crafting table, bookshelf, painting, flower pot…')
one('starchitect.tip.garden', 'Plantez autour (%1$s plante(s)) : fleurs, haies de feuilles, potager, chemin.',
    'Plant things around it (%1$s plant(s)): flowers, leaf hedges, a vegetable patch, a path.')
one('starchitect.button.show', 'Montrer', 'Show me')
one('starchitect.show.started', '%s coins sombres signalés en rouge pendant 30 secondes.', '%s dark spots marked in red for 30 seconds.')
one('starchitect.show.none', 'Aucun coin sombre à montrer : tout est éclairé.', 'No dark spot to show: everything is lit.')
one('starchitect.photo.caption', 'Photo : notre envoyé spécial', 'Photo: our special correspondent')
one('starchitect.photo.none', 'Pas de photo : le photographe était en grève', 'No photo: the photographer was on strike')
one('key.starchitect.gauge', 'Mètre de chantier', 'Building gauge')
one('starchitect.gauge.title', 'MÈTRE DE CHANTIER', 'BUILDING GAUGE')
one('starchitect.gauge.estimate', 'Estimation : %s/100', 'Estimate: %s/100')
one('starchitect.gauge.weak', 'Point faible : %s', 'Weak spot: %s')
one('starchitect.gauge.none', 'Aucune construction ici', 'No build around here')
one('starchitect.gauge.measuring', 'Mesure en cours…', 'Measuring…')
one('starchitect.gauge.on', 'Mètre de chantier activé (%s pour le ranger).', 'Building gauge on (%s to put it away).')
one('starchitect.gauge.off', 'Mètre de chantier rangé.', 'Building gauge put away.')

# ───────────────────────── 1.2 : styles, avant/après, distinctions, impression ─────────────────────────
STYLES = [
    ('prehistoric', 'Préhistorique', 'Prehistoric', '', ''),
    ('infernal', 'Gothique infernal', 'Infernal Gothic',
     'Le style ? Gothique infernal : on sent la chaleur du Nether jusque dans les murs.',
     'The style? Infernal Gothic: you can feel the Nether\'s heat in the very walls.'),
    ('japanese', 'Japonais', 'Japanese',
     'Le style japonais est assumé : bois clair, lignes nettes, une certaine idée de la sérénité.',
     'The Japanese style is fully embraced: pale wood, crisp lines, a certain idea of serenity.'),
    ('desert', 'Désertique', 'Desert',
     'Un style désertique, tout en grès et en terre cuite : on cherche le chameau du regard.',
     'A desert style, all sandstone and terracotta: my eyes searched for the camel.'),
    ('brutalist', 'Brutaliste', 'Brutalist',
     'Style brutaliste : du béton, de la pierre, et pas une concession à la joliesse.',
     'Brutalist style: concrete, stone, and not a single concession to prettiness.'),
    ('modern', 'Moderne', 'Modern',
     'Un style résolument moderne : béton clair, quartz et grandes baies vitrées.',
     'A resolutely modern style: pale concrete, quartz and large bay windows.'),
    ('industrial', 'Industriel', 'Industrial',
     'Style industriel : le fer et le cuivre donnent à l\'ensemble des airs d\'usine à vapeur.',
     'Industrial style: iron and copper give the whole thing a steam-factory air.'),
    ('whimsical', 'Féerique', 'Whimsical',
     'Le style est féerique, presque sorti d\'un conte : couleurs vives et matériaux précieux.',
     'The style is whimsical, straight out of a fairy tale: bright colours and precious materials.'),
    ('nordic', 'Nordique', 'Nordic',
     'Un style nordique : sapin sombre, soubassement de pierre et toit pentu pour la neige.',
     'A Nordic style: dark spruce, a stone base and a steep roof for the snow.'),
    ('medieval', 'Médiéval', 'Medieval',
     'Le style est médiéval : la pierre en bas, le bois en haut, comme au bon vieux temps des châteaux.',
     'The style is medieval: stone below, timber above, like in the good old days of castles.'),
    ('tropical', 'Tropical', 'Tropical',
     'Un style tropical : bois de jungle et d\'acacia, on entend presque les perroquets.',
     'A tropical style: jungle and acacia wood, you can almost hear the parrots.'),
    ('rustic', 'Rustique', 'Rustic',
     'Style rustique : du bois, du bois et encore du bois, comme un chalet de montagne.',
     'Rustic style: wood, wood and more wood, like a mountain chalet.'),
    ('classic', 'Classique', 'Classic',
     'Un style classique, tout en pierre : sobre, solide, intemporel.',
     'A classic style, all in stone: sober, solid, timeless.'),
    ('eclectic', 'Éclectique', 'Eclectic',
     'Le style ? Éclectique, disons. Chaque mur semble venir d\'une époque différente.',
     'The style? Eclectic, let\'s say. Each wall seems to come from a different era.'),
]
for sid, fr, en, lfr, len_ in STYLES:
    one(f'starchitect.style.{sid}', fr, en)
    one(f'starchitect.stylen.{sid}', fr.lower(), en)
    if lfr:
        many(f'starchitect.styleline.{sid}', [(lfr, len_)])
many('starchitect.stylelove.augustin', [('Le style %1$s, enfin ! Voilà qui me réconcilie avec mon époque.',
                                          'The %1$s style, at last! It reconciles me with my era.')])
many('starchitect.stylelove.brigitte', [('Le style %1$s : exactement ce que je défends depuis trente ans.',
                                          'The %1$s style: exactly what I have championed for thirty years.')])
many('starchitect.stylelove.gaston', [('Ah, le style %1$s… Ça me rappelle la ferme de mon grand-père.',
                                        'Ah, the %1$s style… It reminds me of my grandfather\'s farm.')])
many('starchitect.stylelove.zoe', [('Le style %1$s, c\'est tout moi. Je valide à cent pour cent.',
                                     'The %1$s style is so me. One hundred percent approved.')])
one('starchitect.paper.style', 'Style : %s', 'Style: %s')
one('starchitect.photo.before', 'AVANT', 'BEFORE')
one('starchitect.photo.after', 'APRÈS', 'AFTER')
one('starchitect.photo.compare', 'Avant / après : %1$s → %2$s points', 'Before / after: %1$s → %2$s points')
one('starchitect.button.print', 'Imprimer', 'Print')
one('starchitect.print.done', 'La une est imprimée : %s', 'Front page printed: %s')

one('starchitect.awards', 'DISTINCTIONS', 'DISTINCTIONS')
one('starchitect.awards.count', '%1$s / %2$s', '%1$s / %2$s')
one('starchitect.award.toast', 'Distinction obtenue !', 'Distinction earned!')
one('starchitect.award.locked', 'À débloquer', 'Locked')
AWARDS = [
    ('first_review', 'Premier article', 'First article', 'Faire critiquer une construction.', 'Have a build reviewed.'),
    ('first_star', 'Première étoile', 'First star', 'Obtenir une étoile.', 'Earn a star.'),
    ('three_stars', 'Trois étoiles', 'Three stars', 'Obtenir trois étoiles.', 'Earn three stars.'),
    ('perfect', 'Copie parfaite', 'Flawless', 'Obtenir 100/100.', 'Score 100/100.'),
    ('dirt_hut', 'Cabane en terre d\'or', 'Golden Dirt Hut', 'Faire critiquer une cabane en terre.', 'Have a dirt hut reviewed.'),
    ('comeback', 'Le grand retour', 'The comeback', 'Gagner des étoiles lors d\'une nouvelle visite.', 'Gain stars on a return visit.'),
    ('all_critics', 'Tout le jury', 'The full jury', 'Être critiqué par les quatre critiques.', 'Be reviewed by all four critics.'),
    ('styles', 'Touche-à-tout', 'Jack of all styles', 'Quatre styles différents dans le guide.', 'Four different styles in the guide.'),
    ('collection', 'Promoteur immobilier', 'Property developer', 'Cinq constructions dans le guide.', 'Five builds in the guide.'),
    ('night_owl', 'Visite nocturne', 'Night visit', 'Recevoir un critique en pleine nuit.', 'Host a critic in the dead of night.'),
]
for aid, fr, en, dfr, den in AWARDS:
    one(f'starchitect.award.{aid}', fr, en)
    one(f'starchitect.award.{aid}.desc', dfr, den)


def main():
    os.makedirs(LANG, exist_ok=True)
    fr = {k: v[0] for k, v in sorted(T.items())}
    en = {k: v[1] for k, v in sorted(T.items())}
    for name, data in (('fr_fr', fr), ('en_us', en)):
        with open(os.path.join(LANG, name + '.json'), 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write('\n')
    counts = {}
    for k in T:
        head, _, tail = k.rpartition('.')
        if tail.isdigit():
            counts[head] = max(counts.get(head, 0), int(tail) + 1)
    lines = ',\n'.join(f'        Map.entry("{k}", {v})' for k, v in sorted(counts.items()))
    java = f'''package com.starchitect.review;

import java.util.Map;

/** Nombre de variantes de chaque phrase. Fichier généré par tools/lang.py : ne pas modifier à la main. */
public final class Variants {{

    private Variants() {{}}

    private static final Map<String, Integer> COUNTS = Map.ofEntries(
{lines}
    );

    public static int count(String prefix) {{
        return COUNTS.getOrDefault(prefix, 0);
    }}
}}
'''
    with open(VARIANTS, 'w', encoding='utf-8') as f:
        f.write(java)
    print(f'{len(T)} clés, {len(counts)} groupes de variantes')


if __name__ == '__main__':
    main()
