package com.starchitect.analysis;

/**
 * Le style architectural d'une construction, deviné d'après ses matériaux et sa forme. Code pur.
 * L'ordre compte : le premier style qui correspond l'emporte.
 */
public enum Style {
    PREHISTORIC, INFERNAL, JAPANESE, DESERT, BRUTALIST, MODERN, INDUSTRIAL, WHIMSICAL, NORDIC, MEDIEVAL, TROPICAL,
    RUSTIC, CLASSIC, ECLECTIC;

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static Style byId(String id) {
        for (Style s : values()) if (s.id().equals(id)) return s;
        return null;
    }

    private static double wood(Survey s) {
        double w = 0;
        for (String f : new String[]{"oak", "dark_oak", "pale_oak", "spruce", "birch", "jungle", "acacia", "mangrove", "cherry",
            "bamboo", "crimson", "warped"}) {
            w += s.share(f);
        }
        return w;
    }

    public static Style detect(Survey s) {
        if (s.empty()) return ECLECTIC;
        if (s.dirtRatio() > 0.3) return PREHISTORIC;
        double wood = wood(s);
        double stone = s.share("stone") + s.share("cobble") + s.share("mossy") + s.share("deepslate") + s.share("brick");
        if (s.share("nether") + s.share("blackstone") + s.share("crimson") + s.share("warped") >= 0.3) return INFERNAL;
        if (s.share("cherry") + s.share("bamboo") >= 0.15) return JAPANESE;
        if (s.share("sandstone") + s.share("terracotta") + s.share("mud") >= 0.35) return DESERT;
        double concrete = s.share("concrete"), quartz = s.share("quartz");
        if (concrete + s.share("stone") + s.share("deepslate") + s.share("tuff") >= 0.6 && s.glassRatio() < 0.1 && wood < 0.1) {
            return BRUTALIST;
        }
        if (concrete + quartz >= 0.25 && s.glassRatio() >= 0.12) return MODERN;
        if (s.share("iron") + s.share("copper") >= 0.15) return INDUSTRIAL;
        if (s.share("amethyst") + s.share("purpur") + s.share("prismarine") >= 0.15 || s.dyeColors >= 5) return WHIMSICAL;
        if (s.share("spruce") + s.share("dark_oak") >= 0.3 && stone >= 0.1 && s.slopedRatio() >= 0.5) return NORDIC;
        if (wood >= 0.15 && stone >= 0.15) return MEDIEVAL;
        if (s.share("jungle") + s.share("acacia") + s.share("bamboo") >= 0.3) return TROPICAL;
        if (wood >= 0.6) return RUSTIC;
        if (stone + quartz >= 0.5) return CLASSIC;
        return ECLECTIC;
    }
}
