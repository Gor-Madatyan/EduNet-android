package com.example.edunet.ui.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.edunet.R;
import com.example.edunet.common.util.UriUtils;
import com.example.edunet.data.service.model.Entity;

public final class EntityUtils {
    private EntityUtils(){}

    public static void bindNameAvatarElement(Entity entity, View view){
        TextView name = view.findViewById(R.id.name);
        ImageView avatar = view.findViewById(R.id.avatar);

        name.setText(entity.getName());
        ImageLoadingUtils.loadAvatar(
                view,
                UriUtils.safeParse(entity.getAvatar()),
                entity.requireDefaultAvatar(),
                avatar);
    }
}
