package org.dyndns.fules.ck;

import java.io.Serializable;

/**
 * 키보드 설정값 저장용 모델.
 * Created by ZaiC on 2018-03-28.
 */

public class KbdModel implements Serializable {
    //Todo Language에 대한 선언값도 넣어야됨. String? File(사전)?

    final long serialVersionUID= 1L;

    String kbdName;
    Row[] row;

    public static class Row implements Serializable {

        Col[] col;

        Row(Col[] col){
            this.col = col;
        }

        public void setCol(Col col[]){
            this.col = col;
        }

    }
    public static class Col implements Serializable {
        Dir[] dir = new Dir[9];
        boolean longpress = false;//길게 눌렀을 때 탭 액션 반복실행 여부

        Col(Dir[] dir){
            this.dir = dir;
        }

        public void setDir(Dir dir[]){
            this.dir = dir;
        }

    }
    /*Row row[][];
    public class Row implements Serializable {
        Dir dir[] = new Dir[9];
        boolean longpress = false;//길게 눌렀을 때 탭 액션 반복실행 여부
    }*/
    /**
     * 방향별 행동 및 표시
     * 배열 인덱스는 키패드 방향의 숫자-1 (즉 탭은 가운데 5 -1=4)
     */
    public static class Dir implements Serializable {
        String show;//뷰 상에 노출되는 문자.
        /**
         * 명령어 종류
         * 0:Action List 실행
         * 1:sValue 출력
         * 2:iValue에 해당하는 Keycode 입력
         * 3:iValue에 해당하는 unicode char 입력
         * 4:iValue에 해당하는 KbdModel호출 (-1:최근, -2:이전, -3:다음, -4:sValue의 파일 호출)
         */
        int actType;
        String sValue;//인자 중 String값.
        int iValue;//인자 중 int값.

        Dir(String show, int actType){
            this.show = show;
            this.actType = actType;
        }

        public void setShow(String show){
            this.show = show;
        }

        public void setActType(int actType){
            this.actType = actType;
        }

    }

    public void setRow(Row row[]){
        this.row = row;
    }



    //ex) getcount 비슷한 메소드 필요하면 만들기.
}