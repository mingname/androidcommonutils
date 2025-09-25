package com.comm.library.utils.base;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;
    protected Context mContext; // 添加全局 Context
    private static final String TAG = "BaseActivity";
    private static long lastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this; // 初始化全局 Context
        binding = initViewBinding();
        if (binding != null) {
            setContentView(binding.getRoot());
        }

        AppManager.getInstance().addActivity(this);
        Log.d(TAG, this.getClass().getSimpleName() + " created.");

        initView();
        initData();
        initListener();

        // 让内容延伸到状态栏区域
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        );
//
//        // 状态栏设置为透明
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    // 子类实现初始化View
    protected abstract void initView();

    // 子类实现数据处理
    protected abstract void initData();
    // 子类实现监听
    protected abstract void initListener();

    // 初始化 ViewBinding
    @SuppressWarnings("unchecked")
    private VB initViewBinding() {
        try {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                Class<VB> clazz = (Class<VB>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
                Method inflate = clazz.getMethod("inflate", android.view.LayoutInflater.class);
                return (VB) inflate.invoke(null, getLayoutInflater());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 简单跳转
    protected void goTo(Class<? extends Activity> cls) {
        if (!isFastClick()) {
            Intent intent = new Intent(this, cls);
            startActivity(intent);
        }
    }

    // 携带数据跳转
    protected void goTo(Class<? extends Activity> cls, Bundle bundle) {
        if (!isFastClick()) {
            Intent intent = new Intent(this, cls);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    protected void goTo(Class<? extends Activity> cls, Bundle bundle, boolean finishCurrent) {
        if (!isFastClick()) {
            Intent intent = new Intent(this, cls);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            startActivity(intent);
            if (finishCurrent) {
                finish();
            }
        }
    }

    // 防重复点击
    private boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 600) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }


    // 添加通用点击绑定方法
    protected void setClickListeners(View.OnClickListener listener, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(listener);
            }
        }
    }

    @Override
    protected void onDestroy() {
        AppManager.getInstance().removeActivity(this);
        Log.d(TAG, this.getClass().getSimpleName() + " destroyed.");
        super.onDestroy();
    }
}


