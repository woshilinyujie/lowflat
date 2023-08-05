package com.wl.wlflatproject.Presenter;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.wl.wlflatproject.Activity.MainActivity;
import com.wl.wlflatproject.Bean.TimeBean;
import com.wl.wlflatproject.Bean.WJAParamBean;
import com.wl.wlflatproject.Bean.WJATokenBean;
import com.wl.wlflatproject.MUtils.GsonUtils;
import com.wl.wlflatproject.MUtils.StringCallBack;
import com.wl.wlflatproject.MUtils.Utils;
import com.wl.wlflatproject.MView.WJAVideoView;
import com.wl.wlflatproject.MView.WaitDialogTime1;
import com.wl.wlflatproject.R;
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
    private MainActivity context;
    private boolean isGetToken = false;
    public boolean isPlaying = false;
    public boolean isPlaying1 = false;//防止延迟结束全屏问题bug的变量
    private ImageView bg;
    private ConstraintLayout mFunVideoView;
    private TextView time;
    private ImageView codeBt;
    private WaitDialogTime1 mWaitDlg1;
//        private String baseUrl="http://ums-test.wonlycloud.com:10301/";
    private String baseUrl="https://ums-ag.wonlycloud.com:10301/";
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("监测---：","设置状态");
            isPlaying=false;
        }
    };


    public void initCamera(
            WJAVideoView videoPlayView,
            String mDeviceUid,
            String mVideoUid,
            Application application,
            MainActivity context,
            ImageView bg, ConstraintLayout mFunVideoView, TextView time,ImageView codeBt
    ) {
        this.bg=bg;
        this.mFunVideoView=mFunVideoView;
        this.time=time;
        this.codeBt=codeBt;
        this.context = context;
        this.mDeviceUid = mDeviceUid;
        this.mVideoUid = mVideoUid;
        this.videoPlayView = videoPlayView;
        this.application = application;
        mFunVideoView.post(() -> {
            int width = mFunVideoView.getMeasuredWidth();
            int height = mFunVideoView.getMeasuredHeight();
            videoPlayView.setAspectRatio(width, height);
            ViewGroup.LayoutParams params = videoPlayView.getLayoutParams();
            params.width = height;
            params.height = width;
            videoPlayView.setLayoutParams(params);
//            videoPlayView.invalidate();
            videoPlayView.setRotation(-90f);
        });
        videoPlayView.setZoom(false);
        videoPlayView.setOnVideoPlayViewListener(this);
        videoPlayView.setOnVideoPlayViewClick(this);
        initAudio(videoPlayView.getContext());
        mFunVideoView.setVisibility(View.GONE);
    }

    public String getVideoId() {
        return mVideoUid;
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
        Log.e("监测---：","访问token接口");
        if (mWaitDlg1 == null)
            mWaitDlg1 = new WaitDialogTime1(context, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg1.show();
        mWaitDlg1.setWaitText("连接设备中");
        JSONObject data = new JSONObject();
        try {
            data.put("devId", mDeviceUid);
            data.put("ipcSn", mVideoUid);
            data.put("refresh", refreshToken);
            Utils.setSignJson(data, application);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String path = baseUrl+"api/aigang/wanjiaan/getCameraPullFlowToken";
        OkGo.<String>post(path).upJson(data.toString()).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
//                mWaitDlg1.setWaitText("访问获取token成功");
                Log.e("监测---：","访问获取token成功");
                String s = response.body();
                Gson gson = new Gson();
                WJATokenBean wjaTokenBean = gson.fromJson(s, WJATokenBean.class);
                if(wjaTokenBean.getData()==null||wjaTokenBean.getData().getToken()==null){
                    if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
                        mWaitDlg1.dismiss();
                    Toast.makeText(context, wjaTokenBean.getMsg(), Toast.LENGTH_SHORT).show();
                }else{
                    WJANetCtrl.getInstance().setToken(wjaTokenBean.getData().getToken());
                    if (wjaTokenBean.getData().getOnlineStatus() == 1) {
//                        mWaitDlg1.setWaitText("摄像头在线 开始link");
                        Log.e("监测---：","摄像头在线 开始link");
                        setResolutionRatio(wjaTokenBean.getData().getToken());//
                        startLink(true);
                    } else {
                        Log.e("监测---：","摄像头在线 开始唤醒");
                        wakeUpCamera();
                    }
                }
            }

            @Override
            public void onError(Response<String> response) {
                Toast.makeText(context, "获取wja token接口失败", Toast.LENGTH_SHORT).show();
                if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
                    mWaitDlg1.dismiss();
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

        String path = baseUrl+"api/aigang/wanjiaan/wakeUpCameraParameters";
        OkGo.<String>post(path).upJson(data.toString()).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                String s = response.body();
                Gson gson = new Gson();
                WJAParamBean wjaParamBean = gson.fromJson(s, WJAParamBean.class);
                NetApiManager.getInstance().initNetApiManager(wjaParamBean.getData().getUserToken(), wjaParamBean.getData().getUserTopic());
                NetApiManager.getInstance().setOnMqttArrivedListener(new OnMqttArrivedListener() {
                    @Override
                    public void onSystemNotify(String s) {

                    }

                    @Override
                    public void onOnlineStatusNotify(String deviceId, String status) {
//                        mWaitDlg1.setWaitText("唤醒成功开始link");
                        Log.e("监测---：","唤醒成功开始link");
                        if ("1".equals(status) && deviceId.equals(mVideoUid)) {
                            // 开始维活
//                            handler.sendEmptyMessageDelayed(0, 5000);
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
                        if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
                            mWaitDlg1.dismiss();
                        Toast.makeText(context, "摄像头唤醒失败", Toast.LENGTH_SHORT).show();
                        Log.e("监测---：","摄像头唤醒失败");
                    }
                });
            }

            @Override
            public void onError(Response<String> response) {
                Toast.makeText(context, "唤醒接口调用失败", Toast.LENGTH_SHORT).show();
                Log.e("监测---：","唤醒接口调用失败");
                if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
                    mWaitDlg1.dismiss();
            }
        });
    }



    //设置分辨率
    private void setResolutionRatio(String token) {
//        mWaitDlg1.setWaitText("设置分辨率");
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
            data.put("ipcSn", mVideoUid);
            data.put("videoQuality", 0);
            Utils.setSignJson(data, application);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String path = baseUrl+"api/aigang/wanjiaan/setVideoQualidy";
        OkGo.<String>post(path).upJson(data.toString()).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
//                mWaitDlg1.setWaitText("设置分辨率成功");

            }

            @Override
            public void onError(Response<String> response) {
//                Toast.makeText(context, "设置分辨率接口调用失败", Toast.LENGTH_SHORT).show();
//                if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
//                    mWaitDlg1.dismiss();
            }
        });
    }




    public void getSystemTime() {
        String path = "https://ums-ag.wonlycloud.com:10301/api/aigang/getTimeStamp";
        OkGo.<String>post(path).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                TimeBean bean=GsonUtils.GsonToBean(response.body(), TimeBean.class);
                SystemClock.setCurrentTimeMillis(bean.getData().getMillisecond());
            }

            @Override
            public void onError(Response<String> response) {
            }
        });
    }








    public int linkCount = 0;
    private boolean isLinkSuccess = false;
    private LinkInfo mLinkInfo;

    private void startLink(boolean isUpdateLink) {
        linkCount++;
        MediaControl.getInstance().getLinkHandler(
                mVideoUid,
                WJANetCtrl.getInstance().getWJAToken(),
                isUpdateLink,
                new ValueCallBack<LinkInfo>() {
                    @Override
                    public void success(LinkInfo linkInfo) {
                        Log.e("监测---：","link成功 开始拉流");
//                        mWaitDlg1.setWaitText("link成功 开始拉流");
                        isLinkSuccess = true;
                        mLinkInfo = linkInfo;
                        startMonitor();
                        context.handler.removeMessages(1);
                        context.handler.sendEmptyMessageDelayed(1,120000);
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
                        }else if (p0 == 0x4001) {
                            MediaControl.getInstance().setIsShowLog(true);
                            MediaControl.getInstance().initialize(context);
                            NetApiManager.getInstance().mqttDisconnect();
                            NetApiManager.getInstance().reConMQ();
                            Log.d("hsl666", "initAVLib: ---->万佳安初始化");
                        }
//                        mWaitDlg1.setWaitText("link失败 link次数："+linkCount+"失败码："+p0);
                        if (linkCount > 5) {
                            Toast.makeText(context, errorStr, Toast.LENGTH_SHORT).show();
                            isLinkSuccess = false;
                            //彻底失败
                            if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
                                mWaitDlg1.dismiss();
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
            Log.e("监测---：","调用关闭视频API");
        }
    }

    public void setDevid(String id){
        mDeviceUid=id;
    }
    public void setVideoid(String id){
        mVideoUid=id;
    }
    /**
     * 注销实时预览
     */
    public void destroyMonitor() {
        Log.e("监测---：","调用关闭视频");
        isPlaying1=false;
        if (null != mFunVideoView) {
            mFunVideoView.setVisibility(View.GONE);
            bg.setBackgroundResource(R.drawable.bg1);
            time.setVisibility(View.VISIBLE);
            codeBt.setVisibility(View.VISIBLE);
            context.setScreen();
            Log.e("监测---：","控件隐藏");
        }
        stopMonitor();
        if(!TextUtils.isEmpty(mVideoUid)) {
            MediaControl.getInstance().destroyLink(mVideoUid);
        }
        handler.sendEmptyMessageDelayed(0,2000);
    }

    @Override
    public void onHideLoading() {
        if(mWaitDlg1!=null&&mWaitDlg1.isShowing())
            mWaitDlg1.dismiss();
        if(isPlaying)
            return;
        if (!context.isFull){
            context.setFullScreen();
            if(mFunVideoView.getVisibility()!=View.VISIBLE)
            mFunVideoView.setVisibility(View.VISIBLE);
            videoPlayView.setIsOpenAudio(false);
            if(time.getVisibility()!=View.GONE){
                time.setVisibility(View.GONE);
                codeBt.setVisibility(View.GONE);
            }
            isPlaying = true;
            isPlaying1=true;
        }
    }

    @Override
    public void onLoading() {

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
    private boolean fullScreen = false;
    public void setScreen(Boolean b) {
        this.fullScreen = b;
    }
}
