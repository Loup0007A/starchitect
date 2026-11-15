package com.starchitect.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Configuration client : config/starchitect-client.toml */
public final class SConfig {

    private SConfig() {}

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AUTO_VISITS;
    public static final ForgeConfigSpec.IntValue VISIT_INTERVAL;
    public static final ForgeConfigSpec.IntValue ANNOUNCE_SECONDS;
    public static final ForgeConfigSpec.IntValue SCAN_RADIUS;
    public static final ForgeConfigSpec.IntValue MIN_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<String> CRITIC;
    public static final ForgeConfigSpec.BooleanValue PARTICLES;
    public static final ForgeConfigSpec.BooleanValue SOUNDS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Starchitect — configuration client").push("visits");
        AUTO_VISITS = b.comment("Visites surprises : de temps en temps, un critique vient voir la construction près de vous.",
                "Surprise visits: now and then a critic drops by to review the build you are standing in.")
            .define("autoVisits", true);
        VISIT_INTERVAL = b.comment("Minutes entre deux visites surprises. / Minutes between surprise visits.")
            .defineInRange("visitIntervalMinutes", 20, 3, 600);
        ANNOUNCE_SECONDS = b.comment("Secondes d'attente entre l'annonce d'une visite surprise et l'arrivée du critique.",
                "Seconds between the announcement of a surprise visit and the critic's arrival.")
            .defineInRange("announceSeconds", 10, 0, 120);
        CRITIC = b.comment("Critique invité : random, augustin, brigitte, gaston ou zoe. / Which critic comes: random, augustin, brigitte, gaston or zoe.")
            .define("critic", "random");
        b.pop();

        b.push("inspection");
        SCAN_RADIUS = b.comment("Rayon (en blocs) inspecté autour du joueur. / Radius (in blocks) inspected around the player.")
            .defineInRange("scanRadius", 20, 8, 40);
        MIN_BLOCKS = b.comment("Nombre minimal de blocs posés pour qu'une construction mérite une critique.",
                "Minimum number of placed blocks for a build to be worth a review.")
            .defineInRange("minBlocks", 25, 5, 1000);
        PARTICLES = b.comment("Étincelles d'inspection sur les blocs examinés. / Inspection sparkles on examined blocks.")
            .define("particles", true);
        SOUNDS = b.comment("Sons du critique (pages, étoiles…). / Critic sounds (pages, stars…).")
            .define("sounds", true);
        b.pop();

        SPEC = b.build();
    }
}
