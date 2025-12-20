package kr.co.abacus.abms.domain.employee;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeAvatar {

    SKY_GLOW("Sky Glow"),
    SUNSET_BREEZE("Sunset Breeze"),
    CORAL_SPARK("Coral Spark"),
    FOREST_MINT("Forest Mint"),
    LAVENDER_MOON("Lavender Moon"),
    COBALT_WAVE("Cobalt Wave"),
    ORANGE_BURST("Orange Burst"),
    SAGE_GUARD("Sage Guard"),
    BLOSSOM_SMILE("Blossom Smile"),
    MIDNIGHT_WINK("Midnight Wink"),
    AQUA_SPLASH("Aqua Splash"),
    GOLDEN_RAY("Golden Ray");

    private final String description;
}
