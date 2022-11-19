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

    private String token;

    /**
     * 0:离线 1: 在线  2：休眠
     */
    private int onlineStatus = 0;

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
