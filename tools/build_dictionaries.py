#!/usr/bin/env python3
"""단어 추천용 사전(app/src/main/assets/dict/*.tsv)을 만든다.

데이터: wordfreq (https://github.com/rspeer/wordfreq), 데이터 라이선스 CC-BY-SA 4.0.
    pip install wordfreq==3.1.1
    python3 tools/build_dictionaries.py

출력 형식: 한 줄에 "단어<TAB>빈도", 빈도는 Zipf 값 x 100 (정수, 클수록 자주 쓰임).
"""
import math
import os
import re

import wordfreq

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "dict")
HANGUL = re.compile(r"^[가-힣]+$")
LATIN = re.compile(r"^[a-z][a-z']*$")


def build(lang, size, accept):
    # 토큰화를 거치지 않는 원본 빈도표를 쓴다 (한국어 형태소 분석기 MeCab 없이 동작)
    freqs = wordfreq.get_frequency_dict(lang, wordlist="best")
    rows = []
    for word in wordfreq.top_n_list(lang, size):
        if accept(word):
            zipf = math.log10(freqs[word] * 1e9)
            rows.append((word, round(zipf * 100)))
    path = os.path.join(OUT, f"{lang}.tsv")
    with open(path, "w", encoding="utf-8") as f:
        for word, freq in rows:
            f.write(f"{word}\t{freq}\n")
    print(f"{path}: {len(rows)} words")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    # 한 글자 단어는 직접 치는 편이 빠르므로 추천하지 않는다
    build("ko", 60000, lambda w: HANGUL.match(w) and len(w) >= 2)
    build("en", 60000, lambda w: LATIN.match(w) and len(w) >= 2)
