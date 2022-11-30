package com.wl.wlflatproject.Bean;

import java.io.Serializable;

/**
 * @Project: wlzn-aigang-android
 * @Package: com.szwl.device.bean
 * @Author: HSL
 * @Time: 2022/11/17 15:29
 * @E-mail: xxx@163.com
 * @Description: 这个人太懒，没留下什么踪迹~
 */
public class WJATokenBean implements Serializable {

    private int code;
    private String msg;
    private String note;
    private DataDTO data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public static class DataDTO {
        private String token;
        private int onlineStatus;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getOnlineStatus() {
            return onlineStatus;
        }

        public void setOnlineStatus(int onlineStatus) {
            this.onlineStatus = onlineStatus;
        }
    }
}
