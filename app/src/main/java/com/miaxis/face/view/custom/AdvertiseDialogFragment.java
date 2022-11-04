package com.miaxis.face.view.custom;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.miaxis.face.R;
import com.miaxis.face.app.GlideImageLoader;
import com.miaxis.face.bean.Advertisement;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.manager.CardManager;
import com.miaxis.face.manager.ToastManager;
import com.miaxis.face.presenter.AdvertisePresenter;
import com.miaxis.face.util.DateUtil;
import com.miaxis.face.view.activity.SettingActivity;
import com.miaxis.face.view.activity.SettingActivity2;
import com.miaxis.face.view.activity.VerifyActivity;
import com.miaxis.face.view.fragment.BaseDialogFragment;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AdvertiseDialogFragment extends BaseDialogFragment {

//    @BindView(R.id.banner)
//    Banner banner;

    @BindView(R.id.out)
    ImageView out;
    @BindView(R.id.in)
    ImageView in;
    @BindView((R.id.tv_date))
    TextView date;
    @BindView((R.id.tv_time))
    TextView time;
    @BindView(R.id.app_name)
    TextView Tittle;


    private Unbinder bind;
    private List<Advertisement> advertisementList;
    private OnViewClickListener listener;
    private Integer mode;
    private AdvertisePresenter advertisePresenter;

    private long firstTime = 0;
    private int mState = 1;         // 记录点击次数

    public static AdvertiseDialogFragment newInstance(OnViewClickListener listener) {
        AdvertiseDialogFragment advertiseDialogFragment = new AdvertiseDialogFragment();
        advertiseDialogFragment.setListener(listener);
//        advertiseDialogFragment.setMode(mode);
        return advertiseDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advertise_dialog, container, false);
        bind = ButterKnife.bind(this, view);
        out.setOnClickListener(v -> listener.onClick("出场"));
        in.setOnClickListener(v->listener.onClick("进场"));
        advertisePresenter = new AdvertisePresenter(inflater.getContext());
        initTimeReceiver();
//        Tittle.setOnLongClickListener(v -> {
//            listener.onFinish();
//            return false;
//        });
        Tittle.setOnClickListener(v->{

            long secondTime = System.currentTimeMillis();
            if ((secondTime - firstTime) > 1500) {
                mState = 1;
            } else {
                mState++;
            }
            firstTime = secondTime;
            Log.e("mState:", "" + mState);
//            if (mState > 3&&7>mState) {
                if (mState==6) {
                    listener.onFinish();
                }
//            }
        });
        hideNavigationBar();
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Holo_Light);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("Activity_Dialog:","onResume");
    }

    private void initTimeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        Objects.requireNonNull(getActivity()).registerReceiver(timeReceiver, filter);
        onTimeEvent();
    }

    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                onTimeEvent();//每一分钟更新时间
            }
        }
    };

    private void onTimeEvent() {
        String d = DateUtil.dateFormat.format(new Date());
        String t = DateUtil.timeFormat.format(new Date());
        time.setText(t);
        date.setText(d);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("Activity_Dialog:","onStop");
//        banner.stopAutoPlay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        advertisePresenter.destroy();
        Objects.requireNonNull(getActivity()).unregisterReceiver(timeReceiver);
        Log.e("Activity_Dialog:","onDestroyView");
        bind.unbind();
    }

    public void setListener(OnViewClickListener listener) {
        this.listener = listener;
    }

//    public void setMode(Integer mode) {
//        this.mode = mode;
//    }

    public interface OnViewClickListener {
        void onClick(String position);

        void onFinish();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        Log.e("Activity_Dialog:","      tag="+tag);
        CardManager.getInstance().getThread().onPause();
        Log.e(TAG,"=====================================");
        Log.e(TAG,"==================show===================");
        getAllThread();
        Log.e(TAG,"=====================================");
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.e("Activity_Dialog:","      dismiss");
        CardManager.getInstance().getThread().onResume();
        Log.e(TAG,"=====================================");
        Log.e(TAG,"==================dismiss===================");
        getAllThread();
        Log.e(TAG,"=====================================");
    }
    String TAG="getAll";
    private void getAllThread() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Log.d(TAG, "线程总数：" + allStackTraces.size());
        for (Map.Entry<Thread, StackTraceElement[]> stackTrace : allStackTraces.entrySet()) {
            Thread thread = (Thread) stackTrace.getKey();
            Log.d(TAG, "线程：" + thread.getName() + ",id=" + thread.getId() + ",state=" + thread.getState());
            StackTraceElement[] stack = (StackTraceElement[]) stackTrace.getValue();
            String strStackTrace = "堆栈：";
            for (StackTraceElement stackTraceElement : stack) {
                strStackTrace += stackTraceElement.toString() + "\n";
            }
        }
    }

}
