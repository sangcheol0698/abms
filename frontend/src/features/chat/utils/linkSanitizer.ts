const INTERNAL_PATH_REGEX = "\\/(employees|departments|projects|parties)\\/\\d+";

/**
 * Normalize hallucinated absolute internal URLs to relative paths.
 * Example: https://yourcompany.com/projects/2 -> /projects/2
 */
export function sanitizeAssistantLinks(content: string): string {
  let sanitized = content;

  // Markdown link target: [text](https://domain.com/projects/2) -> [text](/projects/2)
  const markdownAbsoluteLinkPattern = new RegExp(
    `\\[([^\\]]+)\\]\\((https?:\\/\\/[^\\s)]+)(${INTERNAL_PATH_REGEX})\\)`,
    'gi',
  );
  sanitized = sanitized.replace(markdownAbsoluteLinkPattern, '[$1]($3)');

  // Plain URL: https://domain.com/projects/2 -> /projects/2
  const plainAbsoluteLinkPattern = new RegExp(`https?:\\/\\/[^\\s)]+(${INTERNAL_PATH_REGEX})`, 'gi');
  sanitized = sanitized.replace(plainAbsoluteLinkPattern, '$1');

  return sanitized;
}
