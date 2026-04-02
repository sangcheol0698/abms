package kr.co.abacus.abms.adapter.observability;

final class PathNormalizer {

    private static final String UUID_PATTERN = "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final String NUMBER_PATTERN = "/\\d+";

    private PathNormalizer() {
    }

    static String normalize(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "/";
        }
        return rawPath
                .replaceAll(UUID_PATTERN, "/{id}")
                .replaceAll(NUMBER_PATTERN, "/{id}");
    }
}
