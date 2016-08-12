package com.xzh.picturesmanager.view;

import android.app.Dialog;
import android.content.Context;

public abstract class BaseDialog extends Dialog {
    public BaseDialog(Context context) {
        super(context);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setText(String text) {
    }
}
