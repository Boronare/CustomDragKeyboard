package org.dyndns.fules.ck;

import android.inputmethodservice.Keyboard;
import android.view.KeyEvent;

import android.view.KeyEvent;

import java.io.Serializable;

import static android.view.KeyEvent.*;
import static org.dyndns.fules.ck.KeySettingActivity.defaultShow;

/**
 * 키보드 설정값 저장용 모델.
 * Created by ZaiC on 2018-03-28.
 */

public class KbdModel implements Serializable {
    //Todo Language에 대한 선언값도 넣어야됨. String? File(사전)?

    final long serialVersionUID= 1L;

    String kbdName;
    int kbdLang;    //  0: 영어   1: 한국어  2: 일본어  3: 중국어
    Row[] row;

    KbdModel(int rows,int cols){
        row = new Row[rows];
        for (int i = 0; i < rows; i++) {
            row[i] = new Row(cols);
        }
    }
    //기본 초기화...
    KbdModel(){
        row = new Row[3];
        for (int i = 0; i < 3; i++) {
            row[i] = new Row(5);
        }
        this.kbdName = "기본 키보드";
        this.kbdLang = 0;

        for (int i = 0; i < 3; i++) {
            this.row[i] = new KbdModel.Row(5);
            for (int j = 0; j < 5; j++) {
                this.row[i].col[j] = new KbdModel.Col();
                for (int k = 0; k < 9; k++) {
                    Dir curdir = this.row[i].col[j].dir[k];
                    if(i>2 || j>4) {  //기본 값 row=3, col=5 이거보다 클 경우 기본 문자로 초기화 ㄴㄴ 공백으로 초기화
                        curdir.show = "";
                        curdir.sValue = "";
                        curdir.iValue = 0;
                        curdir.actType = 1;
                    }
                    else{
                        switch(defaultShow[i][j][k]){
                            case " ":curdir.show="␣";
                                curdir.sValue=" ";
                                curdir.iValue=0;
                                curdir.actType=1;
                                break;
                            case "⏎":curdir.show="⏎";
                            curdir.sValue="";
                            curdir.iValue= KEYCODE_ENTER;
                            curdir.actType=2;
                            break;
                            case "⌫":curdir.show="⌫";
                            curdir.sValue="";
                            curdir.iValue= KEYCODE_DEL;
                            curdir.actType=2;
                            break;
                            default:
                            curdir.show = defaultShow[i][j][k];
                            curdir.sValue = defaultShow[i][j][k];
                            curdir.iValue = 0;
                            curdir.actType = 1;
                        }
                    }
                }
            }
        }

    }

    public static class Row implements Serializable {

        Col[] col;

        Row(int cols){
            col = new Col[cols];
            for(int i=0;i<cols;i++)
                col[i] = new Col();
        }
        Row(){

        }
    }
    public static class Col implements Serializable {
        Dir[] dir = new Dir[9];
        boolean longpress = false;//길게 눌렀을 때 탭 액션 반복실행 여부

        Col(Dir[] dir){
            this.dir = dir;
        }
        Col(){
            for(int i=0;i<9;i++){
                dir[i]=new Dir();
            }
        }
    }
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
        Dir(){
            this.actType = 1;
        }
    }
    public void makeTestValue(){
        row=new Row[3];
        for(int i=0;i<3;i++) {
            row[i] = new Row();
            row[i].col=new Col[5];
            for(int j=0;j<5;j++) {
                row[i].col[j] = new Col();
                for(int k=0;k<9;k++){
                    row[i].col[j].dir[k]=new Dir();
                    row[i].col[j].dir[k].actType=1;
                    row[i].col[j].dir[k].sValue=Character.toString((char)('A'+i+j+k));
                    row[i].col[j].dir[k].show=Character.toString((char)('A'+i+j+k));
                }
            }
        }
        kbdName="TestLayout";
    }
}