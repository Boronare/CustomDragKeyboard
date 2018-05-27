package org.dyndns.fules.ck;


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
    //private static final String[] cho =  {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    private static final String[] nfcNfdCho =  {"ᄀ", "ᄁ", "","ᄂ", "","","ᄃ", "ᄄ", "ᄅ","","","","","","","", "ᄆ", "ᄇ" ,"ᄈ","", "ᄉ", "ᄊ", "ᄋ", "ᄌ", "ᄍ", "ᄎ", "ᄏ", "ᄐ", "ᄑ", "ᄒ" };
    private static final String[] nfcNfdMo = {"ᅡ" ,"ᅢ" ,"ᅣ" ,"ᅤ" ,"ᅥ" ,"ᅦ" ,"ᅧ" ,"ᅨ" ,"ᅩ" ,"ᅪ" ,"ᅫ","ᅬ", "ᅭ" ,"ᅮ" ,"ᅯ","ᅰ" ,"ᅱ" ,"ᅲ", "ᅳ", "ᅴ", "ᅵ" };
    private static final String[] nfcNfcJong={"ᆨ", "ᆩ" ,"ᆪ", "ᆫ" ,"ᆬ", "ᆭ", "ᆮ","" ,"ᆯ", "ᆰ" ,"ᆱ", "ᆲ" ,"ᆳ","ᆴ", "ᆵ","ᆶ" ,"ᆷ" ,"ᆸ","" ,"ᆹ", "ᆺ","ᆻ" ,"ᆼ" ,"ᆽ" ,"","ᆾ" ,"ᆿ","ᇀ","ᇁ","ᇂ"};
    private static final String[] leftJong = {"","","ᆨ","","ᆫ","ᆫ","","","","ᆯ","ᆯ","ᆯ","ᆯ","ᆯ","ᆯ","ᆯ","","","","ᆸ","","","","","","","","",""};
    private static final String[] jongCho = {"ᄀ", "ᄁ", "ᄉ","ᄂ", "ᄌ","ᄒ","ᄃ", "ᄅ","ᄀ","ᄆ","ᄇ","ᄉ","ᄐ","ᄑ","ᄒ", "ᄆ", "ᄇ" ,"ᄉ", "ᄉ", "ᄊ", "ᄋ", "ᄌ", "ᄎ", "ᄏ", "ᄐ", "ᄑ", "ᄒ"};
    /*private static final String[] moum = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
    private static final String[] jong = {"", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    //초성 0ㄱ 1ㄲ 2ㄴ 3ㄷ 4ㄸ 5ㄹ 6ㅁ 7ㅂ 8ㅃ 9ㅅ 10ㅆ 11ㅇ 12ㅈ 13ㅉ 14ㅊ 15ㅋ 16ㅌ 17ㅍ 18ㅎ
    //모음 0ㅏ 1ㅐ 2ㅑ 3ㅒ 4ㅓ 5ㅔ 6ㅕ 7ㅖ 8ㅗ 9ㅘ 10ㅙ 11ㅚ 12ㅛ 13ㅜ 14ㅝ 15ㅞ 16ㅟ 17ㅠ 18ㅡ 19ㅢ 20ㅣ;
    //종성 1ㄱ 2ㄲ 3ㄳ 4ㄴ 5ㄵ 6ㄶ 7ㄷ 8ㄹ 9ㄺ 10ㄻ11ㄼ 12ㄽ 13ㄾ 14ㄿ 15ㅀ 16ㅁ 17ㅂ 18ㅄ 19ㅅ 20ㅆ 21ㅇ 22ㅈ 23ㅊ 24ㅋ 25ㅌ 26ㅍ 27ㅎ

    private static final int[] jongCompPrev = {1,  4,  4, 8, 8,   8,  8,  8,  8,  8, 17, 19};  					//{8,8,8,13,13,13,18}; // 모음조합 기존상태 인덱스
    private static final int[] jongCompNext =  {9, 12, 18, 1,  7,  7,  9, 16, 17, 18,  9,  9};					//{0,1,20,4,5,20,20}; // 모음조합 다음상태 인덱스
    private static final int[] jongCompRes =  {3,  5,  6, 9, 10, 11, 12, 13, 14, 15, 18, 20};					//{9,10,11,14,15,16,19}; // 모음조합 결과 인덱스

    private static final int[] moumCompPrev = {8,8,8,13,13,13,18};//모음조합 기존상태 인덱스
    private static final int[] moumCompNext = {0,1,20,4,5,20,20};//모음조합 다음상태 인덱스
    private static final int[] moumCompRes = { 9,10,11,14,15,16,19};//모음조합 결과 인덱스*/

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
        int ns=new_s.codePointAt(0);

        if(s.length()==0){
            sb.append(new_s);
            return sb;
        }
        int cd=s.codePointBefore(s.length());
        if(ns>='ㄱ'&&ns<='ㅎ'){
            if(cd>='ᅡ'&&cd<='ᅵ') {//첫가끝 모음
                sb.append(nfcNfcJong[ns - 'ㄱ']);
                return sb;
            }else if(cd>='ᄀ'&&cd<='ᇂ') {//첫가끝 종성
                switch(cd){
                    case 'ᆨ':if(ns=='ㅅ'){sb.replace(sb.length()-1,sb.length(),"ᆪ"); return sb;}break;
                    case 'ᆫ':if(ns=='ㅈ'){sb.replace(sb.length()-1,sb.length(),"ᆬ"); return sb;}
                        else if(ns=='ㅎ'){sb.replace(sb.length()-1,sb.length(),"ᆭ"); return sb;}break;
                    case 'ᆯ': if(ns=='ㄱ'){sb.replace(sb.length()-1,sb.length(),"ᆰ"); return sb;}
                        else if(ns=='ㅁ'){sb.replace(sb.length()-1,sb.length(),"ᆱ"); return sb;}
                        else if(ns=='ㅂ'){sb.replace(sb.length()-1,sb.length(),"ᆲ"); return sb;}
                        else if(ns=='ㅅ'){sb.replace(sb.length()-1,sb.length(),"ᆳ"); return sb;}
                        else if(ns=='ㅌ'){sb.replace(sb.length()-1,sb.length(),"ᆴ"); return sb;}
                        else if(ns=='ㅍ'){sb.replace(sb.length()-1,sb.length(),"ᆵ"); return sb;}
                        else if(ns=='ㅎ'){sb.replace(sb.length()-1,sb.length(),"ᆶ"); return sb;}break;
                    case 'ᆸ': if(ns=='ㅅ'){sb.replace(sb.length()-1,sb.length(),"ᆹ"); return sb;}
                }
            }
            sb.append(new_s);
            return sb;
        }
        if(ns>='ㅏ'&&ns<='ㅣ'){
            if(cd>='ㄱ'&&cd<='ㅎ'){
                sb.replace(sb.length()-1,sb.length(),nfcNfdCho[cd-'ㄱ']);
                sb.append(nfcNfdMo[ns-'ㅏ']);
                return sb;
            }
            else if(cd>='ᄀ'&&cd<='ᄒ'){
                sb.append(nfcNfdMo[ns-'ㅏ']);
                return sb;
            }
            else if(cd>='ㅏ'&&cd<='ㅣ'){
                switch(cd){
                    case 'ㅗ'://단모음 ㅗ
                        if(new_s.equals("ㅏ"))sb.replace(sb.length()-1,sb.length(),"ㅘ");
                        else if(new_s.equals("ㅐ"))sb.replace(sb.length()-1,sb.length(),"ㅙ");
                        else if(new_s.equals("ㅣ"))sb.replace(sb.length()-1,sb.length(),"ㅚ");
                        else sb.append(new_s);
                        return sb;
                    case 'ㅜ'://단모음 ㅜ
                        if(new_s.equals("ㅓ"))sb.replace(sb.length()-1,sb.length(),"ㅝ");
                        else if(new_s.equals("ㅔ"))sb.replace(sb.length()-1,sb.length(),"ㅞ");
                        else if(new_s.equals("ㅣ"))sb.replace(sb.length()-1,sb.length(),"ㅟ");
                        else sb.append(new_s);
                        return sb;
                    case 'ㅡ'://단모음 ㅡ
                        if(new_s.equals("ㅣ"))sb.replace(sb.length()-1,sb.length(),"ㅢ");
                        else sb.append(new_s);
                        return sb;
                    default: sb.append(new_s);
                    return sb;
                }
            }
            else if(cd>='ᅡ'&&cd<='ᅵ') {
                switch (cd) {
                    case 'ᅩ'://첫가끝 ㅗ
                        if (new_s.equals("ㅏ")) sb.replace(sb.length() - 1, sb.length(), "ᅪ");
                        else if (new_s.equals("ㅐ")) sb.replace(sb.length() - 1, sb.length(), "ᅫ");
                        else if (new_s.equals("ㅣ")) sb.replace(sb.length() - 1, sb.length(), "ᅬ");
                        else sb.append(new_s);
                        return sb;
                    case 'ᅮ'://첫가끝 ㅜ
                        if (new_s.equals("ㅓ")) sb.replace(sb.length() - 1, sb.length(), "ᅯ");
                        else if (new_s.equals("ㅔ")) sb.replace(sb.length() - 1, sb.length(), "ᅰ");
                        else if (new_s.equals("ㅣ")) sb.replace(sb.length() - 1, sb.length(), "ᅱ");
                        else sb.append(new_s);
                        return sb;
                    case 'ᅳ'://첫가끝 ㅡ
                        if (new_s.equals("ㅣ")) sb.replace(sb.length() - 1, sb.length(), "ᅴ");
                        else sb.append(new_s);
                        return sb;
                }
            }
            else if(cd>='ᆨ'&&cd<='ᇂ'){
                sb.replace(sb.length()-1,sb.length(), leftJong[cd-'ᆨ']);
                sb.append(jongCho[cd-'ᆨ']+nfcNfdMo[ns-'ㅏ']);
                return sb;
            }
        }
        /*if (isConsonan(s))
        {
            // System.out.println("자음");
            if (isConsonan(new_s)) { sb.append(new_s); }
            else if (isVowel(new_s)) {
                StringBuilder temp_sb = new StringBuilder();
                temp_sb.append(sb);
                temp_sb.append(new_s);

                String nfkd = Normalizer.normalize(temp_sb.toString(), Normalizer.Form.NFKD);

                sb.deleteCharAt(sb.length() - 1);
                sb.append(nfkd);
            } else {
                sb.append(new_s);
            }
        }
        else if (isVowel(s))
        {
            // System.out.println("모");
            if (isConsonan(new_s)) { sb.append(new_s); }
            else if (isVowel(new_s)) { sb.append(new_s); }
            else {
            }
        }
        else if(s.length()>0)
        {
            if (!isHangul(s)) { sb.append(new_s); }

            int temp = s.codePointAt(s.length()-1) - 0xAC00;

            int a = temp / (21 * 28);
            int b = (temp % (21 * 28)) / 28;
            int c = temp % 28;
                Log.i("Handler","c="+c+" temp="+temp+"s="+s);
            int cho_value = 0;
            int moum_value = 0;
            int jong_value = 0;

            if (c != 0 ) {
                    // 받침이 있다.
                if (isConsonan(new_s)) {

                    for (int i = 0; i < cho.length; i++) {
                        if (cho[i].compareTo(new_s) == 0) { cho_value = i; }
                    }

                    jong_value = c;

                    for (int i = 0; i < jongCompPrev.length; i++) {
                        if (jongCompPrev[i] == jong_value) {
                            for (int j = 0; j < jongCompNext.length; j++) {
                                if (jongCompNext[j] == cho_value && i == j) {
                                        // 조합 가능 하다.

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
                else if (isVowel(new_s)) {
                        // 기존 단어에서 받침을 제거하고

                    int res = calculateCodePointValue(a, b, 0);
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append((char) res);

                        // 그 부분을 가져와 모음과 결합한다.
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
                       // error
                }
            } else {
                    // 받침이 없다.
                if (isConsonan(new_s)) {
                    for (int i = 0; i < jong.length; i++) {
                        if (jong[i].compareTo(new_s) == 0) { jong_value = i; }
                    }

                    int res = calculateCodePointValue(a, b, jong_value);
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append((char) res);
                }
                else if (isVowel(new_s)) { sb.append(new_s); }
                else {
                        // error
                }
            }
        }*/
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

    private boolean isVowel(String string) {
        if(string.length()==0) return false;
        String s = String.format("U+%04X", string.codePointAt(string.length() - 1));

        if (0 <= s.compareTo("U+314F") && s.compareTo("U+3163") <= 0) { return true; }
        else { return false; }
    }

    private int calculateCodePointValue(int a, int b, int c) {
        return (0xAC00 + ((a * 21) + b) * 28 + c);
    }
}
