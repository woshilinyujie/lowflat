package com.wl.wlflatproject.Presenter;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.wl.wlflatproject.Bean.WJAParamBean;
import com.wl.wlflatproject.Bean.WJATokenBean;
import com.wl.wlflatproject.MUtils.StringCallBack;
import com.wl.wlflatproject.MUtils.Utils;
import com.wl.wlflatproject.MView.WJAVideoView;
import com.worthcloud.avlib.basemedia.MediaControl;
import com.worthcloud.avlib.basemedia.NetApiManager;
import com.worthcloud.avlib.bean.AgreementType;
import com.worthcloud.avlib.bean.DeviceBindStatusBean;
import com.worthcloud.avlib.bean.EventMessage;
import com.worthcloud.avlib.bean.LinkInfo;
import com.worthcloud.avlib.bean.RequestStatus;
import com.worthcloud.avlib.listener.DeviceCtrlCallBack;
import com.worthcloud.avlib.listener.OnMqttArrivedListener;
import com.worthcloud.avlib.listener.OnVideoViewListener;
import com.worthcloud.avlib.listener.ValueCallBack;
import com.worthcloud.avlib.utils.AudioAcquisition;
import com.worthcloud.avlib.widget.BasePlayView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WJAPlayPresenter implements OnVideoViewListener,
        BasePlayView.OnVideoPlayViewClick {
    private WJAVideoView videoPlayView;
    private String mDeviceUid;
    private String mVideoUid;
    private Application application;
    private boolean isLinking = false;
    private Context context;
    private boolean isGetToken = false;
   Handler handler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
           keepDeviceLive();
       }
   };






    public void initCamera(
            WJAVideoView videoPlayView,
            RelativeLayout videoContainerCl,
            String mDeviceUid,
            String mVideoUid,
            Application application,
            Context context
    ) {
        this.context = context;
        this.mDeviceUid = mDeviceUid;
        this.mVideoUid = mVideoUid;
        this.videoPlayView = videoPlayView;
        this.application = application;
        videoContainerCl.post(() -> {
            int width = videoContainerCl.getMeasuredWidth();
            int height = videoContainerCl.getMeasuredHeight();
            videoPlayView.setAspectRatio(width, height);
            ViewGroup.LayoutParams params = videoPlayView.getLayoutParams();
            params.width = height;
            params.height = width;
            videoPlayView.invalidate();
            videoPlayView.setRotation(-90f);
        });
        videoPlayView.setOnVideoPlayViewListener(this);
        videoPlayView.setOnVideoPlayViewClick(this);
        initAudio(videoPlayView.getContext());
    }


    private void initAudio(Context context) {
        AudioAcquisition audioAcquisition = new AudioAcquisition(
                context,
                AudioAcquisition.AudioSamplingFrequencyType.Intercom_16,
                AgreementType.P2P
        );
        audioAcquisition.setAudioErrorCallBack(new ValueCallback<Integer>() {
            @Override
            public void onReceiveValue(Integer value) {
                if (value == -1) {
                    // 无录音权限
                }
            }
        });
//        audioAcquisition.setPadCall(true)
    }

    /**
     * 获取万佳安TOKEN
     */
    public void queryWAJToken(boolean refreshToken) {
        if (isGetToken) return;
        isGetToken = true;
        JSONObject data = new JSONObject();
        try {
            data.put("devId", mDeviceUid);
            data.put("ipcSn", mVideoUid);
            data.put("refresh", refreshToken);
            Utils.setSignJson(data, application);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String path = "api/aigang/wanjiaan/getCameraPullFlowToken";
        OkGo.<String>post(path).upJson(data.toString()).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                String s = response.body();
                Gson gson = new Gson();
                WJATokenBean wjaTokenBean = gson.fromJson(s, WJATokenBean.class);
                WJANetCtrl.getInstance().setToken(wjaTokenBean.getToken());
                if (wjaTokenBean.getOnlineStatus() == 1) {
                    startLink(true);
                } else {
                    wakeUpCamera();
                }
                isGetToken = false;
            }

            @Override
            public void onError(Response<String> response) {
                Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                isGetToken = false;
            }
        });
    }


    private void wakeUpCamera() {
        JSONObject data = new JSONObject();
        try {
            data.put("devId", mDeviceUid);
            data.put("ipcSn", mVideoUid);
            Utils.setSignJson(data, application);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String path = "api/aigang/wanjiaan/wakeUpCameraParameters";
        OkGo.<String>post(path).upJson(data.toString()).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                String s = response.body();
                Gson gson = new Gson();
                WJAParamBean wjaParamBean = gson.fromJson(s, WJAParamBean.class);
                NetApiManager.getInstance().initNetApiManager(wjaParamBean.getUserToken(), wjaParamBean.getUserTopic());
                NetApiManager.getInstance().setOnMqttArrivedListener(new OnMqttArrivedListener() {
                    @Override
                    public void onSystemNotify(String s) {

                    }

                    @Override
                    public void onOnlineStatusNotify(String deviceId, String status) {
                        if ("1".equals(status) && deviceId.equals(mVideoUid)) {
                            // 开始维活
                            handler.sendEmptyMessageDelayed(0,5000);
                            startLink(true);
                        }
                    }

                    @Override
                    public void onBindStatusNotify(DeviceBindStatusBean deviceBindStatusBean) {

                    }

                    @Override
                    public void onDeviceResport(String s, String s1) {

                    }

                    @Override
                    public void onMsgArrived(String s, String s1) {

                    }
                });

                NetApiManager.getInstance().wakeUpDevice(mVideoUid, new DeviceCtrlCallBack<Integer>() {
                    @Override
                    public void success(RequestStatus<Integer> requestStatus) {
                        // TODO: 2022/11/17 此处与方法 onOnlineStatusNotify()一样的功能
                    }

                    @Override
                    public void fail(long l, String s) {
                        Toast.makeText(context, "摄像头唤醒失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Response<String> response) {

            }
        });
    }

    /**
     * 维活指令
     */
    private void keepDeviceLive() {
        String orderJson = "{\"keep_wake\":0}";
        NetApiManager.getInstance().setStauteDevice(mVideoUid, orderJson, new DeviceCtrlCallBack<Integer>() {
            @Override
            public void success(RequestStatus<Integer> requestStatus) {
                Log.d("hsl444", "success: 维活成功");
            }

            @Override
            public void fail(long l, String s) {

            }
        });
    }

    private int linkCount = 0;
    private boolean isLinkSuccess = false;
    private LinkInfo mLinkInfo;

    private void startLink(boolean isUpdateLink) {
        if (isLinking) return;
        linkCount++;
        isLinking = true;
        MediaControl.getInstance().getLinkHandler(
                mVideoUid,
                WJANetCtrl.getInstance().getWJAToken(),
                isUpdateLink,
                new ValueCallBack<LinkInfo>() {
                    @Override
                    public void success(LinkInfo linkInfo) {
                        isLinkSuccess = true;
                        mLinkInfo = linkInfo;
                        startMonitor();
                    }

                    @Override
                    public void fail(long p0, String s) {
                        isLinking = false;
                        linkCount++;
                        String errorStr = "";
                        if (p0 == 0x3009) {
                            errorStr = "授权失败";
                        } else if (p0 == 0x3010) {
                            errorStr = "无权限操作此设备";
                            queryWAJToken(true);
                        } else if (p0 == 0x3013) {
                            errorStr = "TOKEN过期或无效";
                            queryWAJToken(true);
                        } else if (p0 == 0x2002) {
                            errorStr = "P2P 穿透失败";
                            startLink(true);
                        } else if (p0 == 0x2008) {
                            errorStr = "P2P错误";
                            queryWAJToken(true);
                        } else if (p0 == 0x2004) {
                            errorStr = "Link失败";
                            wakeUpCamera();
                        }
                        if (linkCount > 5) {
                            Toast.makeText(context, errorStr, Toast.LENGTH_SHORT).show();
                            isLinkSuccess = false;
                            //彻底失败
                        }
                    }
                });
    }

    /**
     * 开始预览
     */
    public void startMonitor() {
        if (isLinkSuccess) {
            if (mLinkInfo != null) {
                videoPlayView.playVideoByP2P(mVideoUid, mLinkInfo.getLinkHandler(), false);
            }
        }
    }
    /**
     * 停止预览
     */
    public void stopMonitor() {
        if (isLinkSuccess) {
            videoPlayView.playVideoStop();
        }
    }
    @Override
    public void onLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onPlayComplete() {

    }

    @Override
    public void onVideoMessage(EventMessage<Object> eventMessage) {

    }

    @Override
    public void onViewClick() {

    }

    @Override
    public void onCustomTouchEvent(MotionEvent motionEvent) {

    }


}
