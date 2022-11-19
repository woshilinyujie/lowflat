package com.wl.wlflatproject.Presenter;

import com.worthcloud.avlib.bean.NetEntity;
import com.worthcloud.avlib.net.HttpUtils;
import com.worthcloud.avlib.utils.AsyncTaskUtils;
import com.worthcloud.avlib.utils.ResultUtils;

import java.util.Map;

/**
 * 网络控制器
 *
 * @author DZS dzsdevelop@163.com
 * @version V1.0
 * @date 2017/10/23
 */
public class WJANetCtrl extends BaseCtrl {
    private static WJANetCtrl netCtrl;

    private WJANetCtrl() {
    }

    /*单利模式*/
    public static WJANetCtrl getInstance() {
        if (netCtrl == null) netCtrl = new WJANetCtrl();
        return netCtrl;
    }

    /**
     * 获取TOKEN
     */
    public void getToken() {
        Map<String, Object> map = createCommonMap();
        map.put("expires", "72000");
        map.put("access_key", ACCESS_KEY);
        map.put("secret_key", SECRET_KEY);
        getData(createLibEntity("GetToken", GET_TOKEN, HttpUtils.HttpRequestMethod.GET, map), netEntity -> commonNetResult(netEntity, new Operate<NetEntity>() {
            @Override
            public void Success(NetEntity netEntity) {
                TOKEN = ResultUtils.getStringFromResult(netEntity.getResultMap(), KEY_TOKEN);
            }

            @Override
            public void Fail(int code, String msg) {

            }
        }));
    }

    public void setToken(String token) {
        TOKEN = token;
    }

    public String getWJAToken() {
        return TOKEN;
    }

    /**
     * 获取UserTOKEN
     */
    public void getUserToken(String userId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("expires", "72000");
        map.put("access_key", ACCESS_KEY);
        map.put("secret_key", SECRET_KEY);
        map.put("user_id", userId);
        getData(createLibEntity("GetToken", GET_TOKEN, HttpUtils.HttpRequestMethod.GET, map), netEntity -> commonNetResult(netEntity, operate));
    }

    /**
     * 获取UserTOKEN
     */
    public void getUserTopic(String userId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("user_id", userId);
        getData(createLibEntity("GET_TOPIC", GET_TOPIC, HttpUtils.HttpRequestMethod.GET, map), netEntity -> commonNetResult(netEntity, operate));
    }

    /**
     * 注册用户
     *
     * @param userId 用户ID
     */
    public void registerUser(String userId) {
        Map<String, Object> map = createCommonMap();
        map.put("user_id", userId);
        getData(createLibEntity("RegisterUser", REGISTER_USER, HttpUtils.HttpRequestMethod.POST, map), (AsyncTaskUtils.OnNetReturnListener) netEntity -> commonNetResult(netEntity, new Operate<NetEntity>() {
            @Override
            public void Success(NetEntity netEntity) {

            }

            @Override
            public void Fail(int code, String msg) {

            }
        }));
    }

    /**
     * 获取设备列表
     *
     * @param userId user_id
     */
    public void getDeviceList(String userId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("user_id", userId);
        map.put("page", "1");
        map.put("number", "40");
        getData(createLibEntity("GetDeviceList", GET_DEVICE_LIST, HttpUtils.HttpRequestMethod.GET, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }

    /**
     * 查询待绑定设备
     *
     * @param userId  用户ID
     * @param operate 回调
     */
    public void checkBindDevice(String userId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("user_id", userId);
        getData(createLibEntity("checkBindDevice", CHECK_BIND, HttpUtils.HttpRequestMethod.GET, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }

    /**
     * 添加设备
     *
     * @param userId  用户ID
     * @param uuId    设备ID
     * @param operate 回调
     */
    public void addDevice(String userId, String uuId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("user_id", userId);
        map.put("device_id", uuId);
        getData(createLibEntity("AddDevice", ADD_DEVICE, HttpUtils.HttpRequestMethod.POST, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }

    /**
     * 删除设备
     *
     * @param userId  用户ID
     * @param uuId    设备ID
     * @param operate 回调
     */
    public void deleteDevice(String userId, String uuId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("user_id", userId);
        map.put("device_id", uuId);
        getData(createLibEntity("DeleteDevice", DEL_DEVICE, HttpUtils.HttpRequestMethod.POST, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }

    /**
     * 设置推流地址
     *
     * @param pushUrl 推流地址
     * @param uuId    设备ID
     * @param operate 回调
     */
    public void setPushURL(String pushUrl, String uuId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("device_id", uuId);
        map.put("push_url", pushUrl);
        getData(createLibEntity("SET_PUSH_URL", SET_PUSH_URL, HttpUtils.HttpRequestMethod.GET, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }

    public void getCloudVideoTS(String start_time, String uuId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("device_id", uuId);
        map.put("start_time", start_time);
        getData(createLibEntity("GET_DEVICE_CLOUD_VIDEO_TS", GET_DEVICE_CLOUD_VIDEO_TS, HttpUtils.HttpRequestMethod.GET, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }

    public void setPushStop(String uuId, Operate<NetEntity> operate) {
        Map<String, Object> map = createCommonMap();
        map.put("device_id", uuId);
        getData(createLibEntity("SET_PUSH_STOP", GET_DEVICE_CLOUD_VIDEO_TS, HttpUtils.HttpRequestMethod.GET, map), (AsyncTaskUtils.OnNetReturnListener) libEntity -> commonNetResult(libEntity, operate));
    }
}
