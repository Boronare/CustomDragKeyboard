package org.dyndns.fules.ck;

import java.text.Normalizer;


interface LanguageHandler {

    StringBuilder handle(String s, StringBuilder sb);
    boolean deletable(StringBuilder sb);
    void delete(StringBuilder sb);
}

class DummyHandler implements LanguageHandler{
    public StringBuilder handle(String s,StringBuilder sb){
    return sb;}
    public boolean deletable(StringBuilder sb){
        return false;
    }
    public void delete(StringBuilder sb){
    }
}
class KoreanHandler implements LanguageHandler {
    private static final String[] cho =  {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    private static final String[] moum = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
    private static final String[] jong = {"", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    //초성 0ㄱ 1ㄲ 2ㄴ 3ㄷ 4ㄸ 5ㄹ 6ㅁ 7ㅂ 8ㅃ 9ㅅ 10ㅆ 11ㅇ 12ㅈ 13ㅉ 14ㅊ 15ㅋ 16ㅌ 17ㅍ 18ㅎ
    //모음 0ㅏ 1ㅐ 2ㅑ 3ㅒ 4ㅓ 5ㅔ 6ㅕ 7ㅖ 8ㅗ 9ㅘ 10ㅙ 11ㅚ 12ㅛ 13ㅜ 14ㅝ 15ㅞ 16ㅟ 17ㅠ 18ㅡ 19ㅢ 20ㅣ;
    //종성 1ㄱ 2ㄲ 3ㄳ 4ㄴ 5ㄵ 6ㄶ 7ㄷ 8ㄹ 9ㄺ 10ㄻ11ㄼ 12ㄽ 13ㄾ 14ㄿ 15ㅀ 16ㅁ 17ㅂ 18ㅄ 19ㅅ 20ㅆ 21ㅇ 22ㅈ 23ㅊ 24ㅋ 25ㅌ 26ㅍ 27ㅎ

    private static final int[] jongCompPrev = {1,  4,  4, 8, 8,   8,  8,  8,  8,  8, 17, 19};  					//{8,8,8,13,13,13,18}; // 모음조합 기존상태 인덱스
    private static final int[] jongCompNext =  {9, 12, 18, 1,  7,  7,  9, 16, 17, 18,  9,  9};					//{0,1,20,4,5,20,20}; // 모음조합 다음상태 인덱스
    private static final int[] jongCompRes =  {3,  5,  6, 9, 10, 11, 12, 13, 14, 15, 18, 20};					//{9,10,11,14,15,16,19}; // 모음조합 결과 인덱스

    private static final int[] moumCompPrev = {8,8,8,13,13,13,18};//모음조합 기존상태 인덱스
    private static final int[] moumCompNext = {0,1,20,4,5,20,20};//모음조합 다음상태 인덱스
    private static final int[] moumCompRes = { 9,10,11,14,15,16,19};//모음조합 결과 인덱스


    KoreanHandler() {
        super();
    }
    public boolean deletable(StringBuilder sb){
        return false;
    }
    public void delete(StringBuilder sb){
    }
    public StringBuilder handle(String new_s, StringBuilder sb) {
        String s = sb.toString();

        if (!isHangul(new_s)) { return sb; }

        if (isConsonan(s))
        {
            // System.out.println("자음");
            if (isConsonan(new_s)) { sb.append(new_s); }
            else if (isCollection(new_s)) {
                StringBuilder temp_sb = new StringBuilder();
                temp_sb.append(sb);
                temp_sb.append(new_s);

                String nfkd = Normalizer.normalize(temp_sb.toString(), Normalizer.Form.NFKD);

                sb.deleteCharAt(sb.length() - 1);
                sb.append(nfkd);
            } else {
                sb.append(new_s); /* error */
            }
        }
        else if (isCollection(s))
        {
            // System.out.println("모");
            if (isConsonan(new_s)) { sb.append(new_s); }
            else if (isCollection(new_s)) { sb.append(new_s); }
            else {
                    /* error */
            }
        }
        else if(s.length()>0)
        {
            if (!isHangul(s)) { sb.append(new_s); }

            int temp = s.codePointAt(s.length()-1) - 0xAC00;

            int a = temp / (21 * 28);
            int b = (temp % (21 * 28)) / 28;
            int c = temp % 28;

            int cho_value = 0;
            int moum_value = 0;
            int jong_value = 0;

            if (c != 0 ) {
                    /* 받침이 있다.*/
                if (isConsonan(new_s)) {

                    for (int i = 0; i < cho.length; i++) {
                        if (cho[i].compareTo(new_s) == 0) { cho_value = i; }
                    }

                    jong_value = c;

                    for (int i = 0; i < jongCompPrev.length; i++) {
                        if (jongCompPrev[i] == jong_value) {
                            for (int j = 0; j < jongCompNext.length; j++) {
                                if (jongCompNext[j] == cho_value && i == j) {
                                        /* 조합 가능 하다.*/

                                    int res = calculateCodePointValue(a, b, jongCompRes[j]);
                                    sb.deleteCharAt(sb.length() - 1);
                                    sb.append((char) res);

                                    return sb;
                                }
                            }
                        }
                    }

                    sb.append(new_s);
                }
                else if (isCollection(new_s)) {
                        /* 기존 단어에서 받침을 제거하고 */

                    int res = calculateCodePointValue(a, b, 0);
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append((char) res);

                        /* 그 부분을 가져와 모음과 결합한다. */
                    for (int i = 0; i < moum.length; i++) {
                        if (moum[i].compareTo(new_s) == 0) { moum_value = i; }
                    }

                    for (int i = 0; i < cho.length; i++) {
                        if (cho[i].compareTo(jong[c]) == 0) { cho_value = i; }
                    }

                    res = calculateCodePointValue(cho_value, moum_value, 0);
                    sb.append((char) res);
                }
                else {
                        /* error */
                }
            } else {
                    /* 받침이 없다. */
                if (isConsonan(new_s)) {
                    for (int i = 0; i < jong.length; i++) {
                        if (jong[i].compareTo(new_s) == 0) { jong_value = i; }
                    }

                    int res = calculateCodePointValue(a, b, jong_value);
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append((char) res);
                }
                else if (isCollection(new_s)) { sb.append(new_s); }
                else {
                        /* error */
                }
            }
        }
        else{
            sb.append(new_s);
        }
        return sb;
    }

    private boolean isHangul(String string) {
        if(string.length()==0) return false;
        char c = string.charAt(string.length() - 1);
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);

        if (Character.UnicodeBlock.HANGUL_SYLLABLES.equals( unicodeBlock ) ||
                Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.equals( unicodeBlock ) ||
                Character.UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock))
        {
            return true;
        }

        return false;
    }

    private boolean isConsonan(String string) {
        if(string.length()==0) return false;
        String s = String.format("U+%04X", string.codePointAt(string.length() - 1));

        if (0 <= s.compareTo("U+3130") && s.compareTo("U+314E") <= 0) { return true; }
        else { return false; }
    }

    private boolean isCollection(String string) {
        if(string.length()==0) return false;
        String s = String.format("U+%04X", string.codePointAt(string.length() - 1));

        if (0 <= s.compareTo("U+314F") && s.compareTo("U+3163") <= 0) { return true; }
        else { return false; }
    }

    private int calculateCodePointValue(int a, int b, int c) {
        return (0xAC00 + ((a * 21) + b) * 28 + c);
    }
}
