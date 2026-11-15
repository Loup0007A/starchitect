package com.starchitect.analysis;

/**
 * Ce que le critique voit dans un bloc. Code pur (aucune classe de Minecraft) : la classification
 * se fait sur l'identifiant du bloc et quelques propriétés, ce qui marche aussi avec les blocs des
 * autres mods et se teste hors du jeu (voir tools/OfflineCheck.java).
 */
public final class Kind {

    private Kind() {}

    public static final byte AIR = 0;
    /** Terrain : pierre, terre, sable, minerais… */
    public static final byte NATURAL = 1;
    /** Naturel mais traversable : herbes, eau, neige, lianes… */
    public static final byte SOFT = 2;
    /** Feuilles naturelles (servent à reconnaître les troncs d'arbre). */
    public static final byte LEAF = 3;
    /** Bloc construit plein (planches, briques, béton, laine…). */
    public static final byte SOLID = 4;
    /** Relief : murets, barrières, trappes, barreaux… */
    public static final byte SHAPE = 5;
    /** Escaliers et dalles (relief, et surtout : toitures). */
    public static final byte STAIR = 6;
    public static final byte GLASS = 7;
    public static final byte LIGHT = 8;
    public static final byte FURNITURE = 9;
    /** Jardin : fleurs, haies (feuilles posées), cultures, pots, chemins… */
    public static final byte PLANT = 10;
    public static final byte DOOR = 11;
    /** Terre : naturelle ou posée par le joueur (décidé par l'analyse, selon la forme). */
    public static final byte DIRT = 12;
    /** Tronc vertical : arbre ou pilier (décidé par l'analyse, selon les voisins). */
    public static final byte LOG = 13;
    /** Terre posée (cabane en terre…). Produit par l'analyse uniquement. */
    public static final byte PLACED_DIRT = 14;

    /** Fait partie d'une construction. */
    public static boolean built(byte k) {
        return k == SOLID || k == SHAPE || k == STAIR || k == GLASS || k == LIGHT || k == FURNITURE
            || k == DOOR || k == PLACED_DIRT;
    }

    /** Arrête un regard / un courant d'air : murs, terrain. */
    public static boolean wall(byte k) {
        return built(k) || k == NATURAL || k == DIRT || k == LOG;
    }

    /** Bloc « de structure » : ceux qui comptent pour la palette et les proportions. */
    public static boolean structural(byte k) {
        return k == SOLID || k == SHAPE || k == STAIR || k == GLASS || k == DOOR || k == PLACED_DIRT;
    }

    private static final String[] NATURAL_EXACT = {
        "stone", "granite", "diorite", "andesite", "deepslate", "tuff", "calcite", "dripstone_block", "bedrock",
        "sand", "red_sand", "gravel", "clay", "sandstone", "red_sandstone", "terracotta", "snow_block", "ice",
        "packed_ice", "blue_ice", "netherrack", "soul_sand", "soul_soil", "basalt", "blackstone", "magma_block",
        "end_stone", "obsidian", "crying_obsidian", "moss_block", "mud", "mycelium", "podzol", "amethyst_block",
        "budding_amethyst", "smooth_basalt", "powder_snow", "sculk", "sculk_vein", "sculk_catalyst",
        "sculk_sensor", "sculk_shrieker", "nether_wart_block", "warped_wart_block",
        "crimson_nylium", "warped_nylium", "suspicious_sand", "suspicious_gravel", "mushroom_stem",
        "brown_mushroom_block", "red_mushroom_block", "pale_moss_block"
    };

    private static final String[] SOFT_EXACT = {
        "water", "lava", "bubble_column", "short_grass", "grass", "tall_grass", "fern", "large_fern", "dead_bush",
        "seagrass", "tall_seagrass", "kelp", "kelp_plant", "vine", "glow_lichen", "snow", "sugar_cane", "cactus",
        "bamboo", "bamboo_sapling", "brown_mushroom", "red_mushroom", "pumpkin", "melon", "sweet_berry_bush",
        "cave_vines", "cave_vines_plant", "hanging_roots", "spore_blossom", "big_dripleaf", "big_dripleaf_stem",
        "small_dripleaf", "pointed_dripstone", "moss_carpet", "lily_pad", "sea_pickle", "fire", "soul_fire",
        "cobweb", "crimson_roots", "warped_roots", "nether_sprouts", "weeping_vines", "weeping_vines_plant",
        "twisting_vines", "twisting_vines_plant", "crimson_fungus", "warped_fungus", "frogspawn", "bee_nest",
        "azalea", "flowering_azalea", "pale_hanging_moss", "pale_moss_carpet", "chorus_plant", "chorus_flower"
    };

    private static final String[] FLOWERS = {
        "poppy", "dandelion", "allium", "azure_bluet", "blue_orchid", "cornflower", "lily_of_the_valley",
        "oxeye_daisy", "sunflower", "lilac", "rose_bush", "peony", "wither_rose", "torchflower", "pitcher_plant",
        "pink_petals", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "eyeblossom", "open_eyeblossom",
        "closed_eyeblossom"
    };

    private static final String[] FURNITURE_PARTS = {
        "_bed", "chest", "barrel", "crafting_table", "furnace", "smoker", "bookshelf", "lectern", "flower_pot",
        "_carpet", "cauldron", "anvil", "jukebox", "note_block", "_banner", "loom", "cartography_table",
        "fletching_table", "smithing_table", "stonecutter", "grindstone", "brewing_stand", "enchanting_table",
        "composter", "bell", "decorated_pot", "_sign", "_head", "_skull", "cake", "shulker_box", "beehive",
        "crafter", "hopper", "respawn_anchor", "lodestone", "conduit", "beacon", "daylight_detector", "_shelf"
    };

    private static final String[] LIGHT_PARTS = {
        "torch", "lantern", "glowstone", "sea_lantern", "shroomlight", "redstone_lamp", "campfire", "end_rod",
        "candle", "froglight", "jack_o_lantern", "copper_bulb"
    };

    private static final String[] SHAPE_SUFFIXES = {
        "_wall", "_fence", "_fence_gate", "_trapdoor", "_button", "_pressure_plate", "iron_bars", "chain",
        "ladder", "lightning_rod", "scaffolding", "_grate", "lever", "tripwire_hook", "_rail", "rail"
    };

    private static boolean in(String s, String[] list) {
        for (String x : list) if (x.equals(s)) return true;
        return false;
    }

    private static boolean contains(String s, String[] parts) {
        for (String x : parts) if (s.contains(x)) return true;
        return false;
    }

    private static boolean endsWith(String s, String[] parts) {
        for (String x : parts) if (s.endsWith(x)) return true;
        return false;
    }

    /**
     * @param path       chemin de l'identifiant du bloc (« oak_planks », sans l'espace de noms)
     * @param persistent feuilles posées par un joueur (propriété persistent=true)
     * @param axis       'x', 'y', 'z' pour les blocs orientables (troncs), 0 sinon
     * @param emission   lumière émise (0-15)
     */
    public static byte classify(String path, boolean persistent, char axis, int emission) {
        if (path.equals("air") || path.equals("cave_air") || path.equals("void_air") || path.equals("light")
            || path.equals("barrier") || path.equals("structure_void") || path.equals("moving_piston")) {
            return AIR;
        }
        if (path.endsWith("_door")) return DOOR;
        if (path.contains("glass")) return GLASS;
        if (path.startsWith("potted_")) return PLANT;
        if (contains(path, FURNITURE_PARTS) && !path.endsWith("_slab") && !path.endsWith("_stairs")) return FURNITURE;
        if (path.endsWith("_leaves")) return persistent ? PLANT : LEAF;
        if (in(path, FLOWERS) || path.endsWith("_tulip") || path.endsWith("_sapling") || path.endsWith("_propagule")
            || path.equals("farmland") || path.equals("dirt_path") || path.equals("wheat") || path.equals("carrots")
            || path.equals("potatoes") || path.equals("beetroots") || path.equals("hay_block")
            || (path.endsWith("_stem") && (path.startsWith("pumpkin") || path.startsWith("melon") || path.startsWith("attached")))) {
            return PLANT;
        }
        if (contains(path, LIGHT_PARTS) && !path.endsWith("_slab") && !path.endsWith("_stairs")) return LIGHT;
        if (path.endsWith("_ore") || path.startsWith("infested_") || in(path, NATURAL_EXACT)) {
            return NATURAL;
        }
        if (in(path, SOFT_EXACT) || path.endsWith("_coral") || path.endsWith("_coral_fan") || path.endsWith("_coral_wall_fan")
            || path.endsWith("_coral_block") || path.endsWith("_mushroom") || path.endsWith("_roots")) {
            return SOFT;
        }
        if (path.equals("dirt") || path.equals("grass_block") || path.equals("coarse_dirt") || path.equals("rooted_dirt")) {
            return DIRT;
        }
        if (path.endsWith("_log") || path.endsWith("_wood") || path.endsWith("_stem") || path.endsWith("_hyphae")) {
            if (path.startsWith("stripped_") || axis == 'x' || axis == 'z') return SOLID;
            return LOG;
        }
        if (path.endsWith("_stairs") || path.endsWith("_slab")) return STAIR;
        if (endsWith(path, SHAPE_SUFFIXES) || path.equals("iron_bars") || path.equals("chain")) return SHAPE;
        // Un bloc lumineux inconnu (souvent d'un autre mod) : une source de lumière
        if (emission > 0 && !path.contains("magma") && !path.contains("lava")) return LIGHT;
        return SOLID;
    }

    /**
     * Le matériau d'un bloc, sans sa forme : « oak_stairs », « oak_planks » et « stripped_oak_log »
     * sont tous du chêne (« oak »).
     */
    public static String material(String path) {
        String m = path;
        if (m.startsWith("stripped_")) m = m.substring(9);
        if (m.startsWith("waxed_")) m = m.substring(6);
        String[] suffixes = {"_stairs", "_slab", "_wall", "_fence_gate", "_fence", "_trapdoor", "_door", "_button",
            "_pressure_plate", "_pane", "_planks", "_log", "_wood", "_hyphae", "_stem", "_carpet"};
        boolean again = true;
        while (again) {
            again = false;
            for (String s : suffixes) {
                if (m.endsWith(s) && m.length() > s.length()) {
                    m = m.substring(0, m.length() - s.length());
                    again = true;
                }
            }
        }
        if (m.endsWith("_bricks")) m = m.substring(0, m.length() - 1);
        if (m.equals("bricks")) m = "brick";
        if (m.endsWith("_tiles")) m = m.substring(0, m.length() - 1);
        for (String p : new String[]{"polished_", "cut_", "smooth_", "chiseled_", "cracked_"}) {
            if (m.startsWith(p) && m.length() > p.length()) m = m.substring(p.length());
        }
        return m;
    }

    /** La famille d'un matériau, pour nommer la construction et pour les goûts des critiques. */
    public static String family(String material) {
        String m = material;
        String[][] order = {
            {"dark_oak", "dark_oak"}, {"pale_oak", "pale_oak"}, {"oak", "oak"}, {"spruce", "spruce"},
            {"birch", "birch"}, {"jungle", "jungle"}, {"acacia", "acacia"}, {"mangrove", "mangrove"},
            {"cherry", "cherry"}, {"bamboo", "bamboo"}, {"crimson", "crimson"}, {"warped", "warped"},
            {"deepslate", "deepslate"}, {"blackstone", "blackstone"}, {"mossy", "mossy"},
            {"cobble", "cobble"}, {"nether", "nether"}, {"mud", "mud"}, {"sandstone", "sandstone"},
            {"stone_brick", "stone"}, {"stone", "stone"}, {"andesite", "stone"}, {"diorite", "stone"},
            {"granite", "stone"}, {"tuff", "stone"}, {"calcite", "stone"}, {"brick", "brick"},
            {"quartz", "quartz"}, {"concrete", "concrete"}, {"terracotta", "terracotta"}, {"glass", "glass"},
            {"wool", "wool"}, {"dirt", "dirt"}, {"grass_block", "dirt"}, {"prismarine", "prismarine"},
            {"copper", "copper"}, {"purpur", "purpur"}, {"obsidian", "obsidian"}, {"iron", "iron"},
            {"gold", "gold"}, {"diamond", "diamond"}, {"emerald", "emerald"}, {"netherite", "netherite"},
            {"amethyst", "amethyst"}, {"end_stone", "end"}, {"resin", "resin"}
        };
        for (String[] o : order) if (m.contains(o[0])) return o[1];
        return "mystery";
    }

    /** Couleur d'un bloc teint (laine, béton, terre cuite, verre), ou null. */
    public static String dye(String path) {
        String[] colors = {"light_blue", "light_gray", "white", "orange", "magenta", "yellow", "lime", "pink", "gray",
            "cyan", "purple", "blue", "brown", "green", "red", "black"};
        if (!(path.contains("wool") || path.contains("concrete") || path.contains("terracotta") || path.contains("stained_glass"))) {
            return null;
        }
        for (String c : colors) if (path.startsWith(c + "_")) return c;
        return null;
    }
}
