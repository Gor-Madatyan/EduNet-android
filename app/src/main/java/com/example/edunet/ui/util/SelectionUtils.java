package com.example.edunet.ui.util;

import android.view.View;

import com.example.edunet.R;

public final class SelectionUtils {
    private SelectionUtils(){}

    public static void bind(View view, boolean select){
        view.findViewById(R.id.selected).setVisibility(select ? View.VISIBLE : View.INVISIBLE);
    }
}
