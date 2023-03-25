package dev.xdark.jpreprocessor.parser;

public interface StringReader {

    CharSequence text();

    int length();

    boolean canRead();

    int position();

    void position(int position);

    void reset(int pos);

    char get();

    int getCodepoint();

    char peek(int offset);

    boolean isSurrogate();

    boolean isASCII();

    char next();

    boolean is(char ch);

    int digit(int digitRadix);

    boolean accept(String string);

    default boolean inRange(char lo, char hi) {
        char character = get();
        return lo <= character && character <= hi;
    }

    default boolean isOneOf(char ch1, char ch2) {
        return is(ch1) || is(ch2);
    }

    default boolean isOneOf(char ch1, char ch2, char ch3) {
        return is(ch1) || is(ch2) || is(ch3);
    }

    default boolean isOneOf(char ch1, char ch2, char ch3, char ch4, char ch5, char ch6) {
        return is(ch1) || is(ch2) || is(ch3) || is(ch4) || is(ch5) || is(ch6);
    }

    default boolean accept(char ch) {
        if (is(ch)) {
            next();
            return true;
        }

        return false;
    }

    default boolean acceptOneOf(char ch1, char ch2) {
        if (isOneOf(ch1, ch2)) {
            next();
            return true;
        }

        return false;
    }

    default boolean acceptOneOf(char ch1, char ch2, char ch3) {
        if (isOneOf(ch1, ch2, ch3)) {
            next();
            return true;
        }

        return false;
    }

    default void skip(char ch) {
        while (accept(ch)) ;
    }

    default void skipWhitespace() {
        while (acceptOneOf(' ', '\t', '\f')) ;
    }

    default void skipLine() {
        while (canRead()) {
            if (isOneOf('\r', '\n')) {
                break;
            }
            next();
        }
    }

    default void expect(char c) {
        if (!accept(c)) {
            throw new IllegalStateException(c + " expected");
        }
        next();
    }

    static StringReader of(CharSequence cs) {
        return new ImmutableStringReader(cs);
    }
}
