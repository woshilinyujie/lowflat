package com.wl.wlflatproject.Presenter;

import android.os.AsyncTask;

import com.worthcloud.avlib.bean.NetEntity;
import com.worthcloud.avlib.bean.NetResultType;
import com.worthcloud.avlib.net.HttpUtils;
import com.worthcloud.avlib.utils.AsyncTaskUtils;
import com.worthcloud.avlib.utils.ResultUtils;

import java.util.HashMap;
import java.util.Map;

public class BaseCtrl {
    protected final static String KEY_CODE = "code";
    protected final static String KEY_MESSAGE = "message";
    protected final static String KEY_DATA = "data";
    protected final static String KEY_TOKEN = "token";
    protected final static String KEY_DEVICE_ID = "device_id";
    protected final static String SERVER = "https://iot.worthcloud.net";
    protected static final String APP_ID = "2012";
    protected final static String ACCESS_KEY = "640e246f8e2ef6aabe53ca5f8ffe9f74";
    protected final static String SECRET_KEY = "76dc611d6ebaafc66cc0879c71b5db5c";

    protected String TOKEN = "";

    protected void getData(NetEntity netEntity, AsyncTaskUtils.OnNetReturnListener... listeners) {
        new AsyncTaskUtils(listeners).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, netEntity);
    }

    /*检查状态*/
    protected boolean checkStatus(NetEntity netEntity) {
        if (netEntity == null || netEntity.getResultMap() == null) {
            return false;
        } else {
            return "0".equals(ResultUtils.getStringFromResult(netEntity.getResultMap(), KEY_CODE));
        }
    }

    /*公共处理*/
    public void commonNetResult(NetEntity libEntity, Operate<NetEntity> operate) {
        if (libEntity.getNetResultType() == NetResultType.NET_CONNECT_SUCCESS) {
            if (checkStatus(libEntity)) {
                operate.Success(libEntity);
            } else {
                operate.Fail(1, ResultUtils.getStringFromResult(libEntity.getResultMap(), KEY_MESSAGE));
            }
        } else {
            operate.Fail(0, libEntity.getNetResultType().getMessage());
        }
    }

    /*创建公共参数*/
    protected Map<String, Object> createCommonMap() {
        return new HashMap<>();
    }

    /*创建公共头部信息*/
    protected Map<String, Object> createHeadMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("Authorization", TOKEN);
        return map;
    }

    /*创建公共参数*/
    protected NetEntity createLibEntity(String taskId, String url, HttpUtils.HttpRequestMethod httpRequestMethod, Map<String, Object> map) {
        NetEntity libEntity = new NetEntity();
        libEntity.setTaskId(taskId);
        libEntity.setRequestHead(createHeadMap());
        libEntity.setRequestMethod(httpRequestMethod);
        libEntity.setUrl(url);
        libEntity.setRequestParameter(map);
        return libEntity;
    }

    public interface Operate<T> {
        void Success(T t);

        void Fail(int code, String msg);
    }

    protected final static String HOST = SERVER + "/api/v2";
    //注册TOKEN
    protected final static String GET_TOKEN = HOST + "/auth/get_token";
    //注册用户
    protected final static String REGISTER_USER = HOST + "/user/register";
    //查询待绑定的设备
    protected final static String CHECK_BIND = HOST + "/device/bind_status";
    //添加设备
    protected final static String ADD_DEVICE = HOST + "/device/bind";
    //获取设备列表
    protected final static String GET_DEVICE_LIST = HOST + "/device/list";
    //删除设备
    protected final static String DEL_DEVICE = HOST + "/device/unbind";
    //信令设置
    protected final static String CTRL_DEVICE = HOST + "/command/send";
    //信令获取
    protected final static String GET_DEVICE_INFO = HOST + "/command/get_status_current";
    //检查是否有最新固件
    protected final static String CHECK_NEW_FIRMWARE = HOST + "/firmware/check_upgrade";
    //升级固件
    protected final static String UPDATE_DEVICE = HOST + "/firmware/upgrade";
    //获取设备云回放
    protected final static String GET_DEVICE_CLOUD_VIDEO = HOST + "/cloud/playlist";
    //获取设备TS云回放
    protected final static String GET_DEVICE_CLOUD_VIDEO_TS = HOST + "/cloud/playlist_ts";
    //设置自定义推流地址
    protected final static String SET_PUSH_URL = HOST + "/custom/open";
    //关闭自定义推流
    protected final static String SET_PUSH_STOP = HOST + "/custom/close";

    protected final static String GET_TOPIC = HOST + "/user/topic";
}
