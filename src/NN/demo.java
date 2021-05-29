package NN;

import dataDTO.RSSI;
import java.util.ArrayList;
public class demo {
    private static RSSI[] rssis = new RSSI[20];
    public static void main(String[] args) {
        //初始化一个rssi数组
        for (int counter = 0; counter < rssis.length; counter ++) {
            rssis[counter] = new RSSI();
        }
        for (int counter = 0; counter < rssis.length; counter ++) {
            rssis[counter].setAP1MAC("6c:b7:49:55:81:85");
            rssis[counter].setAP2MAC("a0:57:e3:c0:f2:71");
            rssis[counter].setAP3MAC("82:4e:99:16:98:7e");
        }
        rssis[0].setAP1RSSI(-1);
        rssis[0].setAP2RSSI(-2);
        rssis[0].setAP3RSSI(-3);

        rssis[1].setAP1RSSI(-1);
        rssis[1].setAP2RSSI(-2);
        rssis[1].setAP3RSSI(-3);

        rssis[2].setAP1RSSI(-1);
        rssis[2].setAP2RSSI(-60);
        rssis[2].setAP3RSSI(-2);

        rssis[3].setAP1RSSI(-55);
        rssis[3].setAP2RSSI(-2);
        rssis[3].setAP3RSSI(-95);

        for (int counter = 0;counter < rssis.length; counter ++){
            System.out.println("data"+(counter + 1) +"  MAC                 RSSI");
            System.out.print("       "+rssis[counter].getAP1MAC()+"  ");
            System.out.println(rssis[counter].getAP1RSSI());
            System.out.print("       "+rssis[counter].getAP2MAC()+"  ");
            System.out.println(rssis[counter].getAP2RSSI());
            System.out.print("       "+rssis[counter].getAP3MAC()+"  ");
            System.out.println(rssis[counter].getAP3RSSI());
            System.out.println();
        }
        ArrayList<RSSI> listRSSIin = new ArrayList<>();
        for (int counter = 0; counter < rssis.length;counter++) {
            listRSSIin.add(rssis[counter]);
        }
        //进行筛选
        listRSSIin = NN.Screen(listRSSIin);

        //测试平均
        RSSI averageRSSI = NN.Average(listRSSIin);
        System.out.println("\n信号均值为：\nAP1:"+averageRSSI.getAP1RSSI()+"  AP2:"+averageRSSI.getAP2RSSI()+"  AP3:"+averageRSSI.getAP3RSSI());

        //测试匹配
        String result = NN.Match(averageRSSI);
        System.out.println("----------------------------------------------------------------------\n定位结果:\n当前所在位置为："+ result +"号位置\n----------------------------------------------------------------------");
    }
}