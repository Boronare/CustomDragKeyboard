package org.dyndns.fules.ck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class KbdModelSelector implements Serializable {
    final long serialVersionUID= 2L;
    List<String> kbdSerialList = new ArrayList<>();

    //selectActivity에서 settingActivity로 kbdmodel 이름 값을 전달
    //전달 받은 kbdmodel 불러오기. 없으면 새로 생성한 것이므로 기본값 생성

    //기본 초기화
    KbdModelSelector(){
        //KbdModel kbdModel = new KbdModel();
        this.kbdSerialList.add("userKbdModel" + this.kbdSerialList.size());
    }
    public void add(String data){
        kbdSerialList.add(data);

    }


}
