package dev.xdark.jpreprocessor.parser;

public final class DefaultTokenizer implements Tokenizer {
    private final StringBuilder sb = new StringBuilder(512);
    private final StringReader reader;
    private final Tokens tokens;
    private TokenKind tk;
    private int radix;
    private boolean hasEscapeSequences;

    public DefaultTokenizer(StringReader reader, Tokens tokens) {
        this.reader = reader;
        this.tokens = tokens;
    }

    @Override
    public Token readToken() {
        reset();

        StringReader reader = this.reader;
        int pos;
        loop:
        while (true) {
            pos = reader.position();
            switch (reader.get()) {
                case ' ':
                case '\t':
                case '\f':
                    reader.skipWhitespace();
                    continue;
                case '\n':
                    reader.next();
                    continue;
                case '\r':
                    reader.next();
                    reader.accept('\n');
                    continue;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '$':
                case '_':
                    scanIdent();
                    break loop;

                case '0':
                    reader.next();

                    if (reader.acceptOneOf('x', 'X')) {
                        skipIllegalUnderscores();
                        scanNumber(pos, 16);
                    } else if (reader.acceptOneOf('b', 'B')) {
                        skipIllegalUnderscores();
                        scanNumber(pos, 2);
                    } else {
                        append('0');

                        if (reader.is('_')) {
                            reader.skip('_');

                            if (reader.digit(10) < 0) {
                                throw new IllegalStateException("Illegal underscore");
                            }
                        }

                        scanNumber(pos, 8);
                    }
                    break loop;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    scanNumber(pos, 10);
                    break loop;
                case '.':
                    if (reader.accept("...")) {
                        append("...");
                        tk = JavaTokenKind.ELLIPSIS;
                    } else {
                        reader.next();
                        if (reader.accept('.')) {
                            throw new IllegalStateException("Invalid dot");
                        } else if (reader.digit(10) >= 0) {
                            append('.');
                            scanFractionAndSuffix(pos);
                        } else {
                            tk = JavaTokenKind.DOT;
                        }
                    }
                    break loop;

                case ',':
                    reader.next();
                    tk = JavaTokenKind.COMMA;
                    break loop;
                case ';':
                    reader.next();
                    tk = JavaTokenKind.SEMI;
                    break loop;
                case '(':
                    reader.next();
                    tk = JavaTokenKind.LPAREN;
                    break loop;
                case ')':
                    reader.next();
                    tk = JavaTokenKind.RPAREN;
                    break loop;
                case '[':
                    reader.next();
                    tk = JavaTokenKind.LBRACKET;
                    break loop;
                case ']':
                    reader.next();
                    tk = JavaTokenKind.RBRACKET;
                    break loop;
                case '{':
                    reader.next();
                    tk = JavaTokenKind.LBRACE;
                    break loop;
                case '}':
                    reader.next();
                    tk = JavaTokenKind.RBRACE;
                    break loop;
                case '/':
                    if (reader.accept("//")) {
                        append("//");
                        tk = JavaTokenKind.SLASHSLASH;
                    } else if (reader.accept("/*")) {
                        boolean isEmpty = reader.accept('*') && reader.is('/');
                        if (!isEmpty) {
                            while (reader.canRead()) {
                                if (reader.accept('*') && reader.is('/')) {
                                    break;
                                } else {
                                    reader.next();
                                }
                            }
                        }
                        if (reader.accept('/')) {
                            tk = JavaTokenKind.COMMENT;
                        } else {
                            throw new IllegalStateException("Unclosed comment");
                        }
                    } else {
                        reader.next();
                        tk = JavaTokenKind.SLASH;
                    }
                    break loop;
                case '\\':
                    reader.next();
                    tk = JavaTokenKind.BACKSLASH;
                    break loop;
                case '!':
                    reader.next();
                    tk = JavaTokenKind.EXCLAMATION;
                    break loop;
                case '\'':
                    reader.next();

                    if (reader.accept('\'')) {
                        throw new IllegalStateException("Empty character");
                    } else {
                        if (isEOLN()) {
                            throw new IllegalStateException("Illegal line ending");
                        }

                        scanLitChar();

                        if (reader.accept('\'')) {
                            tk = JavaTokenKind.CHAR_VALUE;
                        } else {
                            throw new IllegalStateException("character not closed");
                        }
                    }
                    break loop;

                case '\"':
                    scanString();
                    break loop;
                case ':':
                    reader.next();
                    tk = JavaTokenKind.COLUMN;
                    break loop;
                case '#':
                    reader.next();
                    tk = JavaTokenKind.HASHTAG;
                    break loop;
                case '`':
                    reader.next();
                    tk = JavaTokenKind.BACKQUOTE;
                    break loop;
            }
            if (isSpecial(reader.get())) {
                scanOperator();
            } else {
                boolean isJavaIdentifierStart;

                if (reader.isASCII()) {
                    isJavaIdentifierStart = false;
                } else {
                    isJavaIdentifierStart = reader.isSurrogate()
                            ? Character.isJavaIdentifierStart(reader.getCodepoint())
                            : Character.isJavaIdentifierStart(reader.get());
                }

                if (isJavaIdentifierStart) {
                    scanIdent();
                } else if (reader.digit(10) >= 0) {
                    scanNumber(pos, 10);
                } else if (reader.is((char) 0x1A) || !reader.canRead()) {
                    tk = JavaTokenKind.EOF;
                    pos = reader.position();
                } else {
                    String arg;

                    if (reader.isSurrogate()) {
                        int codePoint = reader.getCodepoint();
                        char hi = Character.highSurrogate(codePoint);
                        char lo = Character.lowSurrogate(codePoint);
                        arg = String.format("\\u%04x\\u%04x", (int) hi, (int) lo);
                    } else {
                        char ch = reader.get();
                        arg = (32 < ch && ch < 127) ? String.format("%s", ch) :
                                String.format("\\u%04x", (int) ch);
                    }

                    throw new IllegalStateException("Illegal character " + arg);
                }
            }
            break;
        }
        int endPos = reader.position();
        if (tk.tag() == JavaTokenKindTag.DEFAULT) {
            return new Token(tk, pos, endPos);
        } else {
            String string = sb.toString();

            if (hasEscapeSequences) {
                string = EscapeTranslator.translate(string);
            }

            if (tk == JavaTokenKind.TEXT_VALUE) {
                return new TextToken(tk, pos, endPos, string);
            } else {
                return new NumericToken(tk, pos, endPos, string, radix);
            }
        }
    }

    @Override
    public StringReader source() {
        return reader;
    }

    @Override
    public void reset() {
        sb.setLength(0);
        radix = 0;
        hasEscapeSequences = false;
    }

    private void append(char ch) {
        sb.append(ch);
    }

    private void appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
    }

    private void append() {
        StringReader reader = this.reader;
        if (reader.isSurrogate()) {
            appendCodePoint(reader.getCodepoint());
        } else {
            append(reader.get());
        }
    }

    private void append(String string) {
        sb.append(string);
    }

    private char appendThenNext() {
        append();

        return reader.next();
    }

    private boolean acceptOneOfThenAppend(char ch1, char ch2) {
        StringReader reader = this.reader;
        if (reader.isOneOf(ch1, ch2)) {
            append(reader.get());
            reader.next();

            return true;
        }

        return false;
    }

    private boolean acceptThenAppend(char ch) {
        StringReader reader = this.reader;
        if (reader.is(ch)) {
            append(reader.get());
            reader.next();

            return true;
        }

        return false;
    }

    private boolean isEOLN() {
        return reader.isOneOf('\n', '\r');
    }

    private void scanIdent() {
        appendThenNext();

        StringReader reader = this.reader;
        do {
            switch (reader.get()) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '$':
                case '_':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;

                case '\u0000':
                case '\u0001':
                case '\u0002':
                case '\u0003':
                case '\u0004':
                case '\u0005':
                case '\u0006':
                case '\u0007':
                case '\u0008':
                case '\u000E':
                case '\u000F':
                case '\u0010':
                case '\u0011':
                case '\u0012':
                case '\u0013':
                case '\u0014':
                case '\u0015':
                case '\u0016':
                case '\u0017':
                case '\u0018':
                case '\u0019':
                case '\u001B':
                case '\u007F':
                    reader.next();
                    continue;

                case '\u001A':
                    if (reader.canRead()) {
                        reader.next();
                        continue;
                    }

                    checkIdent();
                    return;

                default:
                    boolean isJavaIdentifierPart;

                    if (reader.isASCII()) {
                        isJavaIdentifierPart = false;
                    } else {
                        if (Character.isIdentifierIgnorable(reader.get())) {
                            reader.next();
                            continue;
                        }

                        isJavaIdentifierPart = reader.isSurrogate()
                                ? Character.isJavaIdentifierPart(reader.getCodepoint())
                                : Character.isJavaIdentifierPart(reader.get());
                    }

                    if (!isJavaIdentifierPart) {
                        checkIdent();
                        return;
                    }
            }

            appendThenNext();
        } while (true);
    }

    private void checkIdent() {
        tk = tokens.lookup(sb.toString());
    }

    private void skipIllegalUnderscores() {
        if (reader.is('_')) {
            throw new IllegalStateException("Illegal underscore");
            //skip('_');
        }
    }

    private void scanDigits(int digitRadix) {
        StringReader reader = this.reader;
        int leadingUnderscorePos = reader.is('_') ? reader.position() : -1;
        int trailingUnderscorePos;

        do {
            if (!reader.is('_')) {
                append();
                trailingUnderscorePos = -1;
            } else {
                trailingUnderscorePos = reader.position();
            }

            reader.next();
        } while (reader.digit(digitRadix) >= 0 || reader.is('_'));

        if (leadingUnderscorePos != -1 || trailingUnderscorePos != -1) {
            throw new IllegalStateException("Illegal underscore");
        }
    }

    private void scanFraction(int pos) {
        skipIllegalUnderscores();

        if (reader.digit(10) >= 0) {
            scanDigits(10);
        }

        // int index = sb.length();

        if (acceptOneOfThenAppend('e', 'E')) {
            skipIllegalUnderscores();
            acceptOneOfThenAppend('+', '-');
            skipIllegalUnderscores();

            if (reader.digit(10) >= 0) {
                scanDigits(10);
                return;
            }

            throw new IllegalStateException("Malformed float point number");
            //lexError(pos, CompilerProperties.Errors.MalformedFpLit);
            //sb.setLength(index);
        }
    }

    private void scanFractionAndSuffix(int pos) {
        radix = 10;
        scanFraction(pos);

        if (acceptOneOfThenAppend('f', 'F')) {
            tk = JavaTokenKind.FLOAT_VALUE;
        } else {
            acceptOneOfThenAppend('d', 'D');
            tk = JavaTokenKind.DOUBLE_VALUE;
        }
    }

    private void scanHexExponentAndSuffix(int pos) {
        if (acceptOneOfThenAppend('p', 'P')) {
            skipIllegalUnderscores();
            acceptOneOfThenAppend('+', '-');
            skipIllegalUnderscores();

            if (reader.digit(10) >= 0) {
                scanDigits(10);
            } else {
                throw new IllegalStateException("Invalid float point number");
            }
        } else {
            throw new IllegalStateException("Invalid float point number");
        }

        if (acceptOneOfThenAppend('f', 'F')) {
            tk = JavaTokenKind.FLOAT_VALUE;
        } else {
            acceptOneOfThenAppend('d', 'D');
            tk = JavaTokenKind.DOUBLE_VALUE;
        }
        radix = 16;
    }

    private void scanHexFractionAndSuffix(int pos, boolean seendigit) {
        radix = 16;
        appendThenNext();
        skipIllegalUnderscores();

        if (reader.digit(16) >= 0) {
            seendigit = true;
            scanDigits(16);
        }

        if (!seendigit)
            throw new IllegalStateException("Invalid hex number");
        else
            scanHexExponentAndSuffix(pos);
    }

    private void scanNumber(int pos, int radix) {
        this.radix = radix;
        StringReader reader = this.reader;
        int digitRadix = (radix == 8 ? 10 : radix);
        int firstDigit = reader.digit(Math.max(10, digitRadix));
        boolean seendigit = firstDigit >= 0;
        boolean seenValidDigit = firstDigit >= 0 && firstDigit < digitRadix;

        if (seendigit) {
            scanDigits(digitRadix);
        }

        if (radix == 16 && reader.is('.')) {
            scanHexFractionAndSuffix(pos, seendigit);
        } else if (seendigit && radix == 16 && reader.isOneOf('p', 'P')) {
            scanHexExponentAndSuffix(pos);
        } else if (digitRadix == 10 && reader.is('.')) {
            appendThenNext();
            scanFractionAndSuffix(pos);
        } else if (digitRadix == 10 && reader.isOneOf('e', 'E', 'f', 'F', 'd', 'D')) {
            scanFractionAndSuffix(pos);
        } else {
            if (!seenValidDigit) {
                switch (radix) {
                    case 2:
                        //lexError(pos, CompilerProperties.Errors.InvalidBinaryNumber);
                        throw new IllegalStateException("Invalid binary text");
                        //break;
                    case 16:
                        //lexError(pos, CompilerProperties.Errors.InvalidHexNumber);
                        throw new IllegalStateException("Invalid hex text");
                        //break;
                }
            }
            if (radix == 8) {
                sb.setLength(0);
                reader.reset(pos);
                scanDigits(8);
            }

            if (reader.acceptOneOf('l', 'L')) {
                tk = JavaTokenKind.LONG_VALUE;
            } else {
                tk = JavaTokenKind.INT_VALUE;
            }
        }
    }

    private void scanLitChar() {
        if (acceptThenAppend('\\')) {
            hasEscapeSequences = true;

            switch (reader.get()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    char leadch = reader.get();
                    appendThenNext();

                    if (reader.inRange('0', '7')) {
                        appendThenNext();

                        if (leadch <= '3' && reader.inRange('0', '7')) {
                            appendThenNext();
                        }
                    }
                    break;

                case 'b':
                case 't':
                case 'n':
                case 'f':
                case 'r':
                case '\'':
                case '\"':
                case '\\':
                    appendThenNext();
                    break;

                case '\n':
                case '\r':
                    throw new IllegalStateException("Bad escape character");

                default:
                    throw new IllegalStateException("Bad escape character");
            }
        } else {
            appendThenNext();
        }
    }

    private void scanString() {
        tk = JavaTokenKind.TEXT_VALUE;
        StringReader reader = this.reader;
        reader.next();

        while (reader.canRead()) {
            if (reader.accept('\"')) {
                return;
            }

            if (isEOLN()) {
                break;
            } else {
                scanLitChar();
            }
        }
        throw new IllegalStateException("Unclosed block");
    }

    private boolean isSpecial(char ch) {
        switch (ch) {
            case '!':
            case '%':
            case '&':
            case '*':
            case '?':
            case '+':
            case '-':
            case ':':
            case '<':
            case '=':
            case '>':
            case '^':
            case '|':
            case '~':
            case '@':
                return true;

            default:
                return false;
        }
    }

    private void scanOperator() {
        StringReader reader = this.reader;
        while (true) {
            append();
            TokenKind kind = tokens.lookup(sb.toString());

            if (kind == JavaTokenKind.IDENTIFIER) {
                sb.setLength(sb.length() - 1);
                break;
            }

            tk = kind;
            reader.next();

            if (!isSpecial(reader.get())) {
                break;
            }
        }
    }
}
