package com.example.emosafe.models;

public class scanhis {
    public scanhis(String res, String date) {
        this.res = res;
        this.date = date;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    String res,date;

}
