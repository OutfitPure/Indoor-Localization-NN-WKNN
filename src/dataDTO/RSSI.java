package dataDTO;
import java.sql.*;
import java.util.Objects;

public class RSSI {
    private int number; //编号
    private int frequency;  //频率

    private String AP1MAC;
    private int AP1RSSI;
    private String AP2MAC;
    private int AP2RSSI;
    private String AP3MAC;
    private int AP3RSSI;

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getAP1MAC() {
        return AP1MAC;
    }
    public void setAP1MAC(String AP1MAC) {
        this.AP1MAC = AP1MAC;
    }

    public int getAP1RSSI() {
        return AP1RSSI;
    }

    public void setAP1RSSI(int AP1RSSI) {
        this.AP1RSSI = AP1RSSI;
    }

    public String getAP2MAC() {
        return AP2MAC;
    }
    public void setAP2MAC(String AP2MAC) {
        this.AP2MAC = AP2MAC;
    }

    public int getAP2RSSI() {
        return AP2RSSI;
    }
    public void setAP2RSSI(int AP2RSSI) {
        this.AP2RSSI = AP2RSSI;
    }

    public String getAP3MAC() {
        return AP3MAC;
    }
    public void setAP3MAC(String AP3MAC) {
        this.AP3MAC = AP3MAC;
    }

    public int getAP3RSSI() {
        return AP3RSSI;
    }
    public void setAP3RSSI(int AP3RSSI) {
        this.AP3RSSI = AP3RSSI;
    }

    @Override
    public String toString() {
        return "RSSI{" +
                "number=" + number +
                '}';
    }

    //(Location information,Positioning frequency,MAC AP1,RSSI AP1,MAC AP2,RSSI AP2,MAC AP3,RSSI AP3)
}
