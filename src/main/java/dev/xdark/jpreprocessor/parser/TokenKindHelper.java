package dev.xdark.jpreprocessor.parser;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class TokenKindHelper {

    private TokenKindHelper() {
    }

    @SafeVarargs
    static <K extends TokenKind> Map<String, K> collect(K... values) {
        K[] array = (K[]) values.getClass().getComponentType().getEnumConstants();
        return Arrays.stream(array)
                .filter(t -> t.content() != null)
                .collect(Collectors.toMap(TokenKind::content, Function.identity()));
    }
}
