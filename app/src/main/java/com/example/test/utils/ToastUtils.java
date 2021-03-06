package com.example.test.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void showLongToast(Context context, CharSequence llw) {
        Toast.makeText(context.getApplicationContext(), llw, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Context context, CharSequence llw) {
        Toast.makeText(context.getApplicationContext(), llw, Toast.LENGTH_SHORT).show();
    }
}
