package com.example.edunet.ui.util;

import android.view.View;
import android.widget.TextView;

import com.example.edunet.R;

public final class TextUtils {
    private TextUtils(){}

    public static void bindHeader(String text, View view){
        TextView textView = view.findViewById(R.id.header);
        textView.setText(text);
    }
}
