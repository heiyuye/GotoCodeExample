package com.liucr.gotocode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liucr on 2019/7/24.
 */
public class LocationPlugin {

    private static final int ADD_LOCATION_CLICK_LISTENER = 1;

    private static ExecutorService mShowExecutorPool = Executors.newFixedThreadPool(1);

    private static String appID = "unKnow";

    private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj instanceof View
                    && msg.what == msg.obj.hashCode()) {
                long l = System.currentTimeMillis();
                addLocationClickToView((View) msg.obj);
                Log.d("addLocationClickToView", "time : " + (System.currentTimeMillis() - l));
            }
            return false;
        }
    });

    public void init(Application application) {
        appID = application.getApplicationInfo().processName;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                addLayoutChangeListener(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
//                    addLocationClickToView(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                removeLayoutChangeListener(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            handler.removeMessages(v.hashCode());

            Message message = new Message();
            message.obj = v;
            message.what = v.hashCode();
            handler.sendMessageDelayed(message, 2000);
        }
    };

    private void addLayoutChangeListener(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.removeOnLayoutChangeListener(layoutChangeListener);
        decorView.addOnLayoutChangeListener(layoutChangeListener);

//        if (decorView instanceof ViewGroup) {
//            Log.d("addLayoutChangeListener", decorView.toString());
//            LocationView locationView = new LocationView(activity);
//            locationView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            ((ViewGroup) decorView).addView(locationView);
//        }
    }

    private void removeLayoutChangeListener(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.removeOnLayoutChangeListener(layoutChangeListener);
        handler.removeMessages(decorView.hashCode());
    }

    private void addLocationClickToView(Activity activity) {
        List<View> allViews = getAllViews(activity);
        for (View view : allViews) {

            View.OnClickListener onClickListener = getOnClickListener(view);
            if (onClickListener == null
                    || onClickListener instanceof LocationCodeListener) {
            } else {
                LocationCodeListener locationCodeListener = new LocationCodeListener(onClickListener);
                view.setOnClickListener(locationCodeListener);
            }
        }
    }

    private void addLocationClickToView(View parent) {
        List<View> allViews = getAllChildViews(parent);
        for (View view : allViews) {
            View.OnClickListener onClickListener = getOnClickListener(view);
            if (onClickListener == null
                    || onClickListener instanceof LocationCodeListener) {
            } else {
                LocationCodeListener locationCodeListener = new LocationCodeListener(onClickListener);
                view.setOnClickListener(locationCodeListener);
            }
        }
    }

    //获取 activity中的所有view
    private List<View> getAllViews(Activity act) {
        return getAllChildViews(act.getWindow().getDecorView());
    }

    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                //再次 调用本身（递归）
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }

    private static String getCallName(StackTraceElement st) {
        StringBuilder buf = new StringBuilder();

        if (st == null) {
            buf.append("(Null Stack)");
        } else {
            String fName = st.getFileName();

            if (TextUtils.isEmpty(fName)) {
                buf.append("(Unknown Source)");
            } else {
                buf.append('(');
                buf.append(fName);

                int lineNum = st.getLineNumber();
                if (lineNum >= 0) {
                    buf.append(':');
                    buf.append(lineNum);
                }

                buf.append(')');

                buf.append('.');
                buf.append(st.getMethodName());
                buf.append('(');
                buf.append(')');
            }
        }
        return buf.toString();
    }

    /**
     * 通过反射获取控件当前的点击事件
     */
    private View.OnClickListener getOnClickListener(View view) {
        try {
            //通过反射，获取到PackageManager隐藏的方法getPackageSizeInfo()
            @SuppressLint("PrivateApi") Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
            getListenerInfo.setAccessible(true);

            Object o = getListenerInfo.invoke(view);
            Field field = o.getClass().getDeclaredField("mOnClickListener");
            field.setAccessible(true);

            Object o1 = field.get(o);
            if (o1 instanceof View.OnClickListener) {
                return (View.OnClickListener) o1;
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class LocationCodeListener implements View.OnClickListener {

        private View.OnClickListener mOriginalClickListener;

        public LocationCodeListener(View.OnClickListener mOriginalClickListener) {
            this.mOriginalClickListener = mOriginalClickListener;
        }

        @Override
        public void onClick(View v) {
            mShowExecutorPool.submit(new Runnable() {
                @Override
                public void run() {
                    StackTraceElement[] sts = Looper.getMainLooper().getThread().getStackTrace();
                    StringBuilder log = new StringBuilder();
                    List<StackTraceElement> traceElements = new ArrayList<>();
                    for (StackTraceElement st : sts) {
                        if (st.isNativeMethod()) {
                            continue;
                        }
                        if (st.getClassName().startsWith(appID)) {
                            log.append("|").append(getCallName(st));
                            traceElements.add(st);
                        }
                    }
                    String s = log.toString();
                    if (!TextUtils.isEmpty(s)) {
                        s = s.replaceFirst("\\|", "");
                        Log.d("ClickEvent", traceElements.toString());
                    }
                }
            });
            if (mOriginalClickListener != null) {
                mOriginalClickListener.onClick(v);
            }
        }
    }

//    public class LocationView extends View {
//
//        public LocationView(Context context) {
//            super(context);
//        }
//
//        @Override
//        public boolean dispatchTouchEvent(MotionEvent event) {
//            Log.d("dispatchClickEvent", event.toString());
//
//            mShowExecutorPool.submit(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    StackTraceElement[] sts = Looper.getMainLooper().getThread().getStackTrace();
//                    for (StackTraceElement st : sts) {
//                        if (st.isNativeMethod()) {
//                            continue;
//                        }
////                        if (st.getClassName().startsWith(appID)) {
//                            Log.d("dispatchClickEvent", getCallName(st));
////                        }
//                    }
//                }
//            });
//            return super.dispatchTouchEvent(event);
//        }
//    }
}
