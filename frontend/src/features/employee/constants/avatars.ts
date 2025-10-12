import skyGlow from '@/assets/avatars/notion/notion-avatar-sky-glow.svg?url';
import sunsetBreeze from '@/assets/avatars/notion/notion-avatar-sunset-breeze.svg?url';
import coralSpark from '@/assets/avatars/notion/notion-avatar-coral-spark.svg?url';
import forestMint from '@/assets/avatars/notion/notion-avatar-forest-mint.svg?url';
import lavenderMoon from '@/assets/avatars/notion/notion-avatar-lavender-moon.svg?url';
import cobaltWave from '@/assets/avatars/notion/notion-avatar-cobalt-wave.svg?url';
import orangeBurst from '@/assets/avatars/notion/notion-avatar-orange-burst.svg?url';
import sageGuard from '@/assets/avatars/notion/notion-avatar-sage-guard.svg?url';
import blossomSmile from '@/assets/avatars/notion/notion-avatar-blossom-smile.svg?url';
import midnightWink from '@/assets/avatars/notion/notion-avatar-midnight-wink.svg?url';
import aquaSplash from '@/assets/avatars/notion/notion-avatar-aqua-splash.svg?url';
import goldenRay from '@/assets/avatars/notion/notion-avatar-golden-ray.svg?url';

const avatarDefinitions = {
  SKY_GLOW: { label: 'Sky Glow', imageUrl: skyGlow },
  SUNSET_BREEZE: { label: 'Sunset Breeze', imageUrl: sunsetBreeze },
  CORAL_SPARK: { label: 'Coral Spark', imageUrl: coralSpark },
  FOREST_MINT: { label: 'Forest Mint', imageUrl: forestMint },
  LAVENDER_MOON: { label: 'Lavender Moon', imageUrl: lavenderMoon },
  COBALT_WAVE: { label: 'Cobalt Wave', imageUrl: cobaltWave },
  ORANGE_BURST: { label: 'Orange Burst', imageUrl: orangeBurst },
  SAGE_GUARD: { label: 'Sage Guard', imageUrl: sageGuard },
  BLOSSOM_SMILE: { label: 'Blossom Smile', imageUrl: blossomSmile },
  MIDNIGHT_WINK: { label: 'Midnight Wink', imageUrl: midnightWink },
  AQUA_SPLASH: { label: 'Aqua Splash', imageUrl: aquaSplash },
  GOLDEN_RAY: { label: 'Golden Ray', imageUrl: goldenRay },
} as const;

export type EmployeeAvatarCode = keyof typeof avatarDefinitions;

export interface EmployeeAvatarOption {
  code: EmployeeAvatarCode;
  label: string;
  imageUrl: string;
}

export const defaultEmployeeAvatar: EmployeeAvatarCode = 'SKY_GLOW';

export function isEmployeeAvatarCode(value: unknown): value is EmployeeAvatarCode {
  return typeof value === 'string' && value in avatarDefinitions;
}

export function getEmployeeAvatarOption(code: string | null | undefined): EmployeeAvatarOption {
  const normalized = typeof code === 'string' ? code : defaultEmployeeAvatar;
  if (isEmployeeAvatarCode(normalized)) {
    const definition = avatarDefinitions[normalized];
    return { code: normalized, label: definition.label, imageUrl: definition.imageUrl };
  }
  const definition = avatarDefinitions[defaultEmployeeAvatar];
  return {
    code: defaultEmployeeAvatar,
    label: definition.label,
    imageUrl: definition.imageUrl,
  };
}

export function getEmployeeAvatarOptions(): EmployeeAvatarOption[] {
  return (Object.entries(avatarDefinitions) as [EmployeeAvatarCode, { label: string; imageUrl: string }][])
    .map(([code, definition]) => ({
      code,
      label: definition.label,
      imageUrl: definition.imageUrl,
    }));
}
