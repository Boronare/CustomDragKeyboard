package kr.ac.cau.team135.customdragkeyboard;


import java.text.Normalizer;

interface LanguageHandler {

    StringBuilder handle(String s, StringBuilder sb);
}

class KoreanHandler implements LanguageHandler {
   private static final String[] nfcNfdCho = {"ᄀ", "ᄁ", "", "ᄂ", "", "", "ᄃ", "ᄄ", "ᄅ", "", "", "", "", "", "", "", "ᄆ", "ᄇ", "ᄈ", "", "ᄉ", "ᄊ", "ᄋ", "ᄌ", "ᄍ", "ᄎ", "ᄏ", "ᄐ", "ᄑ", "ᄒ"};
    private static final String[] nfcNfdMo = {"ᅡ", "ᅢ", "ᅣ", "ᅤ", "ᅥ", "ᅦ", "ᅧ", "ᅨ", "ᅩ", "ᅪ", "ᅫ", "ᅬ", "ᅭ", "ᅮ", "ᅯ", "ᅰ", "ᅱ", "ᅲ", "ᅳ", "ᅴ", "ᅵ"};
    private static final String[] nfcNfcJong = {"ᆨ", "ᆩ", "ᆪ", "ᆫ", "ᆬ", "ᆭ", "ᆮ", "", "ᆯ", "ᆰ", "ᆱ", "ᆲ", "ᆳ", "ᆴ", "ᆵ", "ᆶ", "ᆷ", "ᆸ", "", "ᆹ", "ᆺ", "ᆻ", "ᆼ", "ᆽ", "", "ᆾ", "ᆿ", "ᇀ", "ᇁ", "ᇂ"};
    private static final String[] leftJong = {"", "", "ᆨ", "", "ᆫ", "ᆫ", "", "", "ᆯ", "ᆯ", "ᆯ", "ᆯ", "ᆯ", "ᆯ", "ᆯ", "", "", "ᆸ", "", "", "", "", "", "", "", "", "", "", ""};
    private static final String[] jongCho = {"ᄀ", "ᄁ", "ᄉ", "ᄂ", "ᄌ", "ᄒ", "ᄃ", "ᄅ", "ᄀ", "ᄆ", "ᄇ", "ᄉ", "ᄐ", "ᄑ", "ᄒ", "ᄆ", "ᄇ", "ᄉ", "ᄉ", "ᄊ", "ᄋ", "ᄌ", "ᄎ", "ᄏ", "ᄐ", "ᄑ", "ᄒ"};
    //초성 0ㄱ 1ㄲ 2ㄴ 3ㄷ 4ㄸ 5ㄹ 6ㅁ 7ㅂ 8ㅃ 9ㅅ 10ㅆ 11ㅇ 12ㅈ 13ㅉ 14ㅊ 15ㅋ 16ㅌ 17ㅍ 18ㅎ
    //모음 0ㅏ 1ㅐ 2ㅑ 3ㅒ 4ㅓ 5ㅔ 6ㅕ 7ㅖ 8ㅗ 9ㅘ 10ㅙ 11ㅚ 12ㅛ 13ㅜ 14ㅝ 15ㅞ 16ㅟ 17ㅠ 18ㅡ 19ㅢ 20ㅣ;
    //종성 1ㄱ 2ㄲ 3ㄳ 4ㄴ 5ㄵ 6ㄶ 7ㄷ 8ㄹ 9ㄺ 10ㄻ11ㄼ 12ㄽ 13ㄾ 14ㄿ 15ㅀ 16ㅁ 17ㅂ 18ㅄ 19ㅅ 20ㅆ 21ㅇ 22ㅈ 23ㅊ 24ㅋ 25ㅌ 26ㅍ 27ㅎ

    KoreanHandler() {
        super();
    }

    public StringBuilder handle(String new_s, StringBuilder sb) {
        String s = Normalizer.normalize(sb.toString(), Normalizer.Form.NFD);
        sb.replace(0, sb.length(), s);
        int ns = new_s.codePointAt(0);

        if (s.length() == 0) {
            sb.append(new_s);
            return sb;
        }
        int cd = s.codePointBefore(s.length());
        if (ns >= 'ㄱ' && ns <= 'ㅎ') {
            if (cd >= 'ᅡ' && cd <= 'ᅵ') {//첫가끝 모음
                if (nfcNfcJong[ns - 'ㄱ'].length() == 0) sb.append(new_s);
                else sb.append(nfcNfcJong[ns - 'ㄱ']);
                return sb;
            } else if (cd >= 'ᄀ' && cd <= 'ᇂ') {//첫가끝 종성
                switch (cd) {
                    case 'ᆨ':
                        if (ns == 'ㅅ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆪ");
                            return sb;
                        }
                        break;
                    case 'ᆫ':
                        if (ns == 'ㅈ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆬ");
                            return sb;
                        } else if (ns == 'ㅎ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆭ");
                            return sb;
                        }
                        break;
                    case 'ᆯ':
                        if (ns == 'ㄱ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆰ");
                            return sb;
                        } else if (ns == 'ㅁ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆱ");
                            return sb;
                        } else if (ns == 'ㅂ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆲ");
                            return sb;
                        } else if (ns == 'ㅅ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆳ");
                            return sb;
                        } else if (ns == 'ㅌ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆴ");
                            return sb;
                        } else if (ns == 'ㅍ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆵ");
                            return sb;
                        } else if (ns == 'ㅎ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆶ");
                            return sb;
                        }
                        break;
                    case 'ᆸ':
                        if (ns == 'ㅅ') {
                            sb.replace(sb.length() - 1, sb.length(), "ᆹ");
                            return sb;
                        }
                }
            }
            sb.append(new_s);
            return sb;
        }
        else if (ns >= 'ㅏ' && ns <= 'ㅣ') {
            if (cd >= 'ㄱ' && cd <= 'ㅎ') {
                sb.replace(sb.length() - 1, sb.length(), nfcNfdCho[cd - 'ㄱ']);
                sb.append(nfcNfdMo[ns - 'ㅏ']);
                return sb;
            } else if (cd >= 'ᄀ' && cd <= 'ᄒ') {
                sb.append(nfcNfdMo[ns - 'ㅏ']);
                return sb;
            } else if (cd >= 'ㅏ' && cd <= 'ㅣ') {
                switch (cd) {
                    case 'ㅗ'://단모음 ㅗ
                        if (new_s.equals("ㅏ")) sb.replace(sb.length() - 1, sb.length(), "ㅘ");
                        else if (new_s.equals("ㅐ")) sb.replace(sb.length() - 1, sb.length(), "ㅙ");
                        else if (new_s.equals("ㅣ")) sb.replace(sb.length() - 1, sb.length(), "ㅚ");
                        else sb.append(new_s);
                        return sb;
                    case 'ㅜ'://단모음 ㅜ
                        if (new_s.equals("ㅓ")) sb.replace(sb.length() - 1, sb.length(), "ㅝ");
                        else if (new_s.equals("ㅔ")) sb.replace(sb.length() - 1, sb.length(), "ㅞ");
                        else if (new_s.equals("ㅣ")) sb.replace(sb.length() - 1, sb.length(), "ㅟ");
                        else sb.append(new_s);
                        return sb;
                    case 'ㅡ'://단모음 ㅡ
                        if (new_s.equals("ㅣ")) sb.replace(sb.length() - 1, sb.length(), "ㅢ");
                        else sb.append(new_s);
                        return sb;
                    default:
                        sb.append(new_s);
                        return sb;
                }
            } else if (cd >= 'ᅡ' && cd <= 'ᅵ') {
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
                    default:
                        sb.append(new_s);
                        return sb;
                }
            } else if (cd >= 'ᆨ' && cd <= 'ᇂ') {
                sb.replace(sb.length() - 1, sb.length(), leftJong[cd - 'ᆨ']);
                sb.append(jongCho[cd - 'ᆨ'] + nfcNfdMo[ns - 'ㅏ']);
                return sb;
            }
        }
        else if(ns=='゛'||ns=='゜'||ns=='小'){
            if(ns=='゛'){
                if(cd>='か'&&cd<='ち'&&cd%2==1){ sb.delete(sb.length()-1,sb.length());sb.append((char)(cd+1));return sb;}
                switch(cd){
                    case 'つ':
                    case 'て':
                    case 'と':
                    case 'は':
                    case 'ひ':
                    case 'ふ':
                    case 'へ':
                    case 'ほ': sb.delete(sb.length()-1,sb.length());sb.append((char)(cd+1));return sb;
                    case 'う':sb.replace(sb.length()-1,sb.length(),"ゔ"); return sb;
                    default:return sb;
                }
            }else if(ns=='゜'){
                switch(cd){
                    case 'は':
                    case 'ひ':
                    case 'ふ':
                    case 'へ':
                    case 'ほ':sb.delete(sb.length()-1,sb.length());sb.append((char)(cd+2));return sb;
                    default:return sb;
                }
            }
            else{
                switch(cd) {
                    case 'あ':
                    case 'い':
                    case 'う':
                    case 'え':
                    case 'お':
                    case 'つ':
                    case 'や':
                    case 'ゆ':
                    case 'よ':
                    case 'わ':sb.delete(sb.length()-1,sb.length());sb.append((char)(cd-1));return sb;
                    case 'か':sb.replace(sb.length()-1,sb.length(),"ゕ"); return sb;
                    case 'け':sb.replace(sb.length()-1,sb.length(),"ゖ"); return sb;
                    default:return sb;
                }
            }
        }
        return sb;
    }
}
