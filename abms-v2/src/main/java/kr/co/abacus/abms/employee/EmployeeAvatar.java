package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 프로필 아바타 프리셋. 이니셜 배경 그라디언트 색상을 가진다.
 */
public enum EmployeeAvatar implements Labeled {

    SKY_GLOW("Sky Glow", "#38bdf8", "#6366f1"),
    SUNSET_BREEZE("Sunset Breeze", "#fb923c", "#ec4899"),
    CORAL_SPARK("Coral Spark", "#f87171", "#f59e0b"),
    FOREST_MINT("Forest Mint", "#34d399", "#059669"),
    LAVENDER_MOON("Lavender Moon", "#c084fc", "#818cf8"),
    COBALT_WAVE("Cobalt Wave", "#3b82f6", "#1e3a8a"),
    ORANGE_BURST("Orange Burst", "#f97316", "#ea580c"),
    SAGE_GUARD("Sage Guard", "#84cc16", "#4d7c0f"),
    BLOSSOM_SMILE("Blossom Smile", "#f472b6", "#db2777"),
    MIDNIGHT_WINK("Midnight Wink", "#475569", "#0f172a"),
    AQUA_SPLASH("Aqua Splash", "#22d3ee", "#0891b2"),
    GOLDEN_RAY("Golden Ray", "#facc15", "#ca8a04");

    private final String label;
    private final String from;
    private final String to;

    EmployeeAvatar(String label, String from, String to) {
        this.label = label;
        this.from = from;
        this.to = to;
    }

    @Override
    public String label() {
        return label;
    }

    public String gradient() {
        return "linear-gradient(135deg, " + from + ", " + to + ")";
    }

}
