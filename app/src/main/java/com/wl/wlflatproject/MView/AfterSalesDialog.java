package com.wl.wlflatproject.MView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.wl.wlflatproject.MUtils.DpUtils;
import com.wl.wlflatproject.R;

public class AfterSalesDialog extends Dialog {

    private Bitmap towCode;
    public AfterSalesDialog(Context context) {
        super(context);
        init();
    }

    private void init() {
        setContentView(R.layout.after_code_layout);
    }

}
