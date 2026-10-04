package kr.co.abacus.abms.common.web;

/**
 * SEED Design 레시피 CSS 클래스 이름 생성기. (@seed-design/css 의 createClassName 규칙과 같다)
 * <p>
 * 예: {@code Seed.button("brandSolid")} →
 * {@code seed-action-button seed-action-button--variant_brandSolid seed-action-button--size_small …}
 */
public final class Seed {

    private Seed() {
    }

    /**
     * Action Button. variant: brandSolid, neutralSolid, neutralWeak, criticalSolid, brandOutline, neutralOutline, ghost
     */
    public static String button(String variant) {
        return button(variant, "small");
    }

    /** size: xsmall, small, medium, large */
    public static String button(String variant, String size) {
        return actionButton(variant, size, "withText");
    }

    public static String iconButton(String variant, String size) {
        return actionButton(variant, size, "iconOnly");
    }

    /** Badge (weak). tone: neutral, brand, informative, positive, warning, critical */
    public static String badge(String tone) {
        return badge(tone, "weak");
    }

    /** variant: weak, solid, outline */
    public static String badge(String tone, String variant) {
        String root = "seed-badge__root";
        return root + " " + root + "--size_large " + root + "--variant_" + variant + " " + root + "--tone_" + tone + "-variant_" + variant;
    }

    private static String actionButton(String variant, String size, String layout) {
        String base = "seed-action-button";
        return base + " " + base + "--variant_" + variant + " " + base + "--size_" + size + " " + base + "--layout_" + layout
                + " " + base + "--size_" + size + "-layout_" + layout;
    }

}
