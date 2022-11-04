package com.miaxis.face.view.custom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.miaxis.face.R;
import com.miaxis.face.bean.Config;
import com.miaxis.face.manager.ConfigManager;
import com.miaxis.face.manager.DaoManager;
import com.miaxis.face.manager.FingerManager;
import com.miaxis.face.manager.ToastManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ParameterFragment extends Fragment {

    @BindView(R.id.tv_device_serial)
    TextView tvDeviceSerial;
    @BindView(R.id.et_client_id)
    EditText etClientId;
    @BindView(R.id.s_verify_mode)
    Spinner sVerifyMode;
    @BindView(R.id.rb_gather_on_one)
    RadioButton rbGatherOnOne;
    @BindView(R.id.rb_gather_on_two)
    RadioButton rbGatherOnTwo;
    @BindView(R.id.rb_gather_off)
    RadioButton rbGatherOff;
    @BindView(R.id.rg_gather)
    RadioGroup rgGather;
    @BindView(R.id.rb_liveness_on)
    RadioButton rbLivenessOn;
    @BindView(R.id.rb_liveness_off)
    RadioButton rbLivenessOff;
    @BindView(R.id.rg_liveness)
    RadioGroup rgLiveness;
    @BindView(R.id.et_verify_score)
    EditText etVerifyScore;
    @BindView(R.id.et_quality_score)
    EditText etQualityScore;
    @BindView(R.id.et_liveness_quality_score)
    EditText etLivenessQualityScore;
    @BindView(R.id.et_monitor_interval)
    EditText etMonitorInterval;
    @BindView(R.id.et_org_name)
    EditText etOrgName;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.et_advertise_delay_time)
    EditText etAdvertiseDelayTime;

    private Config config;
    private boolean hasFingerDevice;


    private Unbinder bind;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_parameter,container,false);
        bind = ButterKnife.bind(this, view);
        initData();
        initModeSpinner();
        initView();
        return view;
    }

    private void initData() {
        config = ConfigManager.getInstance().getConfig();
        hasFingerDevice = FingerManager.getInstance().checkHasFingerDevice();
    }

    void initModeSpinner() {
        List<String> verifyModeList = Arrays.asList(getResources().getStringArray(R.array.verifyMode));
        if (!hasFingerDevice) {
            String faceOnly = verifyModeList.get(0);
//            String local = verifyModeList.get(6);
            verifyModeList = new ArrayList<>();
            verifyModeList.add(faceOnly);
//            verifyModeList.add(local);
        }
        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style_display, R.id.tvDisplay, verifyModeList);
        sVerifyMode.setAdapter(myAdapter);
        sVerifyMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (hasFingerDevice) {
                    config.setVerifyMode(position);
                } else {
                    if (position == 1) {
                        config.setVerifyMode(Config.MODE_LOCAL_FEATURE);
                    } else {
                        config.setVerifyMode(Config.MODE_FACE_ONLY);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                config.setVerifyMode(0);
            }
        });
    }

    private void initView() {
        tvDeviceSerial.setText(config.getDeviceSerialNumber());
        etClientId.setText(config.getClientId());
        if (hasFingerDevice) {
            sVerifyMode.setSelection(config.getVerifyMode());
        } else {
            sVerifyMode.setSelection(config.getVerifyMode() / 6);           //无指纹模块时， 验证模式 只有0 或 6
        }
        rbLivenessOn.setChecked(config.getLivenessFlag());
        rbLivenessOff.setChecked(!config.getLivenessFlag());
        rbGatherOnOne.setChecked(config.getGatherFingerFlag() == 0);
        rbGatherOnTwo.setChecked(config.getGatherFingerFlag() == 1);
        rbGatherOff.setChecked(config.getGatherFingerFlag() == 2);
        etVerifyScore.setText(String.valueOf(config.getVerifyScore()));
        etQualityScore.setText(String.valueOf(config.getQualityScore()));
        etLivenessQualityScore.setText(String.valueOf(config.getLivenessQualityScore()));
        etMonitorInterval.setText(String.valueOf(config.getIntervalTime()));
        etOrgName.setText(config.getOrgName());
        etPwd.setText(config.getPassword());
        etAdvertiseDelayTime.setText(String.valueOf(config.getAdvertiseDelayTime()));
    }

    @OnClick(R.id.save)
    void save(){
        if (etPwd.getText().length() != 6) {
            Toast.makeText(getActivity(), "请填写6位数字密码", Toast.LENGTH_SHORT).show();
            return;
        }
        config.setClientId(etClientId.getText().toString());
        config.setLivenessFlag(rbLivenessOn.isChecked());

        config.setVerifyScore(Float.parseFloat(etVerifyScore.getText().toString()));
        config.setQualityScore(Integer.parseInt(etQualityScore.getText().toString()));
        config.setLivenessQualityScore(Integer.parseInt(etLivenessQualityScore.getText().toString()));
        config.setPassword(etPwd.getText().toString());
        config.setIntervalTime(Integer.parseInt(etMonitorInterval.getText().toString()));
        config.setOrgName(etOrgName.getText().toString());
        config.setAdvertiseDelayTime(Integer.parseInt(etAdvertiseDelayTime.getText().toString()));
        if (rbGatherOnOne.isChecked()) {
            config.setGatherFingerFlag(0);
        } else if (rbGatherOnTwo.isChecked()) {
            config.setGatherFingerFlag(1);
        } else if (rbGatherOff.isChecked()) {
            config.setGatherFingerFlag(2);
        }

        ConfigManager.getInstance().saveConfig(config, (result, message) -> {
            if (result) {
                ToastManager.toast("保存设置成功");
            } else {
                ToastManager.toast("保存设置失败");
            }
        });
    }
}

