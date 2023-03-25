package dev.xdark.jpreprocessor.parser;

public final class ImmutableStringReader implements StringReader {

    private final CharSequence cs;
    private int position;
    private int width;
    private char character;
    private int codepoint;
    private boolean wasBackslash;
    private boolean wasUnicodeEscape;

    public ImmutableStringReader(CharSequence cs) {
        this.cs = cs;
        nextCodePoint();
    }

    @Override
    public CharSequence text() {
        return cs;
    }

    @Override
    public int length() {
        return cs.length();
    }

    @Override
    public boolean canRead() {
        return position < cs.length();
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public void position(int position) {
        this.position = position;
    }

    @Override
    public void reset(int pos) {
        position = pos;
        width = 0;
        wasBackslash = false;
        wasUnicodeEscape = false;
        nextCodePoint();
    }

    @Override
    public char get() {
        return character;
    }

    @Override
    public int getCodepoint() {
        return codepoint;
    }

    @Override
    public char peek(int offset) {
        return cs.charAt(position + offset);
    }

    @Override
    public boolean isSurrogate() {
        return 0xFFFF < codepoint;
    }

    @Override
    public boolean isASCII() {
        return character <= 0x7F;
    }

    @Override
    public char next() {
        nextCodePoint();
        return character;
    }

    @Override
    public boolean is(char ch) {
        return character == ch;
    }

    @Override
    public int digit(int digitRadix) {
        int result;

        if (inRange('0', '9')) {
            result = character - '0';

            return result < digitRadix ? result : -1;
        }

        result = isSurrogate() ? Character.digit(codepoint, digitRadix) :
                Character.digit(character, digitRadix);

        if (result >= 0 && !isASCII()) {
            throw new IllegalStateException("Illegal ASCII digit " + result);
        }

        return result;
    }

    @Override
    public boolean accept(String s) {
        if (s.isEmpty() || !is(s.charAt(0))) {
            return false;
        }

        int savedPosition = position;
        nextCodePoint();

        for (int i = 1; i < s.length(); i++) {
            if (!is(s.charAt(i))) {
                reset(savedPosition);
                return false;
            }

            nextCodePoint();
        }

        return true;
    }

    private void nextCodeUnit() {
        int index = position + width;

        if (cs.length() <= index) {
            character = 0x1A;
        } else {
            character = cs.charAt(index);
            width++;
        }
    }

    private void nextUnicodeInputCharacter() {
        position += width;
        width = 0;

        nextCodeUnit();

        if (character == '\\' && (!wasBackslash || wasUnicodeEscape)) {
            int start = position + width;

            width = 1;

            CharSequence cs = this.cs;
            int index;
            for (index = start; index < cs.length(); index++) {
                if (cs.charAt(index) != 'u') {
                    break;
                }
            }

            if (index == start) {
                wasUnicodeEscape = false;
                wasBackslash ^= true;
            } else {
                int code = 0;
                for (int i = 0; i < 4; i++) {
                    int digit = index < cs.length() ? Character.digit(cs.charAt(index), 16) : -1;
                    code = code << 4 | digit;

                    if (code < 0) {
                        break;
                    }

                    index++;
                }
                width = index - position;
                if (code >= 0) {
                    character = (char) code;
                    wasUnicodeEscape = true;
                    wasBackslash = character == '\\' && !wasBackslash;
                } else {
                    nextUnicodeInputCharacter();
                }
            }
        } else {
            wasBackslash = false;
            wasUnicodeEscape = false;
        }

        codepoint = character;
    }

    private void nextCodePoint() {
        nextUnicodeInputCharacter();

        if (isASCII() || !Character.isHighSurrogate(character)) {
            return;
        }

        char hi = character;
        int savePosition = position;
        int saveWidth = width;

        nextUnicodeInputCharacter();
        char lo = character;

        if (Character.isLowSurrogate(lo)) {
            position = savePosition;
            width += saveWidth;
            codepoint = Character.toCodePoint(hi, lo);
        } else {
            position = savePosition;
            width = saveWidth;
            character = hi;
            codepoint = hi;
        }
    }
}