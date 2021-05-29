package NN;

import dataDTO.RSSI;
import java.util.ArrayList;
import java.util.HashMap;
import WKNN.WKNN;
import dataDTO.XYaxis;

public class test {
    public static void main(String[] args) {
        RSSI rssi_test = new RSSI();
        rssi_test.setAP1RSSI(-72);
        rssi_test.setAP2RSSI(-69);
        rssi_test.setAP3RSSI(-63);
        rssi_test.setAP1MAC("6c:b7:49:55:81:85");
        rssi_test.setAP2MAC("a0:57:e3:c0:f2:71");
        rssi_test.setAP3MAC("82:4e:99:16:98:7e");
        XYaxis XY_test = WKNN.Match_WKNN(rssi_test);
        System.out.println("\n定位信息在坐标轴上为：");
        String xy = XY_test.toString();
        System.out.println(xy);
    }
}
