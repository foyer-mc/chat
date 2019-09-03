/**
 * Copyright 2019 the Foyer developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cool.foyer.chat;

import java.lang.FunctionalInterface;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.function.Consumer;
import java.util.Set;
import java.util.HashSet;

import lombok.*;

import net.md_5.bungee.api.ChatColor;

public final class Markup {

    @FunctionalInterface
    private interface StateFn {
         public StateFn run();
    }

    @RequiredArgsConstructor
    @NoArgsConstructor(force = true)
    private static enum TokenKind {
        TEXT,
        BOLD('l'),
        ITALIC('o'),
        UNDERLINE('n'),
        STRIKETHROUGH('m'),
        SPOILER('k'),
        EOF;

        public final char code;
    }

    private static class Token {
        public TokenKind kind;
        public int start, end;
    }

    private static class RingBuff {

        private Token[] elems;
        private int start = 0;
        private int end = 0;

        public RingBuff(int cap) {
            elems = new Token[cap];
            for (int i = 0; i < elems.length; ++i) {
                elems[i] = new Token();
            }
        }

        private Token at(int i) {
            return elems[i % elems.length];
        }

        public boolean empty() {
            return start == end;
        }

        public Token push() {
            if (end - start == elems.length) {
                throw new IllegalStateException("ring buffer full.");
            }
            return at(end++);
        }

        public Token pop() {
            if (empty()) {
                return null;
            }
            return at(start++);
        }

    }

    private static class Lex {

        private String input;
        private int start, pos;
        private RingBuff tokens = new RingBuff(2);
        private StateFn state;

        public Lex(String input) {
            this.input = input;
            this.state = this::lex;
        }

        public Token next() {
            while (tokens.empty()) {
                state = state.run();
            }
            return tokens.pop();
        }

        private char read() {
            if (pos >= input.length()) {
                return 0;
            }
            char c = input.charAt(pos);
            ++pos;
            return c;
        }

        private void emit(TokenKind token) {
            var tok = tokens.push();
            tok.kind = token;
            tok.start = start;
            tok.end = pos;
            start = pos;
        }

        private StateFn lex() {
        loop:
            for (;;) {
                TokenKind tok = null;
                if (input.startsWith("**", pos)) {
                    tok = TokenKind.BOLD;
                } else if (input.startsWith("__", pos)) {
                    tok = TokenKind.UNDERLINE;
                } else if (input.startsWith("||", pos)) {
                    tok = TokenKind.SPOILER;
                } else if (input.startsWith("~~", pos)) {
                    tok = TokenKind.STRIKETHROUGH;
                }

                if (tok != null) {
                    if (pos > start) {
                        emit(TokenKind.TEXT);
                    }
                    pos += 2;
                    emit(tok);
                    return this::lex;
                }

                char c = read();
                switch (c) {
                    case 0: // EOF
                        break loop;
                    case '*':
                    case '_':
                        if (pos > start) {
                            --pos;
                            emit(TokenKind.TEXT);
                            ++pos;
                        }
                        emit(TokenKind.ITALIC);
                        return this::lex;
                }
            }

            if (pos > start) {
                emit(TokenKind.TEXT);
            }
            emit(TokenKind.EOF);
            return this::lex;
        }
    }

    private static void appendCodes(StringBuilder sb, Set<TokenKind> mods) {
        sb.append(ChatColor.COLOR_CHAR);
        sb.append('r');
        for (var mod : mods) {
            sb.append(ChatColor.COLOR_CHAR);
            sb.append(mod.code);
        }
    }

    public static String format(String s) {
        var buf = new StringBuilder((int) (s.length() * 1.1));
        var lexer = new Lex(s);
        var modifiers = new HashSet<TokenKind>();
        var modsChanged = false;

        for (var tok = lexer.next(); tok.kind != TokenKind.EOF; tok = lexer.next()) {
            switch (tok.kind) {
                case TEXT:
                    if (modsChanged) {
                        appendCodes(buf, modifiers);
                        modsChanged = false;
                    }
                    buf.append(s, tok.start, tok.end);
                    break;
                case ITALIC:
                case BOLD:
                case UNDERLINE:
                case STRIKETHROUGH:
                case SPOILER:
                    if (!modifiers.remove(tok.kind)) {
                        modifiers.add(tok.kind);
                    }
                    modsChanged = true;
                    break;
            }
        }

        return buf.toString();
    }

}
