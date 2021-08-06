package com.example.test.aqi;

public class AirNowStation {
    public String aqi;
    public String qlty;
    public String main;
    public String pm25;
    public String pm10;
    public String no2;
    public String so2;
    public String co;
    public String o3;
    public String pub_time;

    @Override
    public String toString() {
        return "AirNowStation{" +
                "aqi='" + aqi + '\'' +
                ", qlty='" + qlty + '\'' +
                ", main='" + main + '\'' +
                ", pm25='" + pm25 + '\'' +
                ", pm10='" + pm10 + '\'' +
                ", no2='" + no2 + '\'' +
                ", so2='" + so2 + '\'' +
                ", co='" + co + '\'' +
                ", o3='" + o3 + '\'' +
                ", pub_time='" + pub_time + '\'' +
                '}';
    }
}
