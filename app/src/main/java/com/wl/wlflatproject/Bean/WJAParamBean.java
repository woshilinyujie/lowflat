package com.wl.wlflatproject.Bean;

/**
 * @Project: wlzn-aigang-android
 * @Package: com.szwl.device.bean
 * @Author: HSL
 * @Time: 2022/11/17 16:30
 * @E-mail: xxx@163.com
 * @Description: 这个人太懒，没留下什么踪迹~
 */
public class WJAParamBean {

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
        private String uid;
        private String userToken;
        private String userTopic;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            this.userToken = userToken;
        }

        public String getUserTopic() {
            return userTopic;
        }

        public void setUserTopic(String userTopic) {
            this.userTopic = userTopic;
        }
    }
}
