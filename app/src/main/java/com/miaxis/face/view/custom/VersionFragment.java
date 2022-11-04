package com.miaxis.face.view.custom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miaxis.face.R;
import com.miaxis.face.manager.FaceManager;
import com.miaxis.face.util.MyUtil;
import com.miaxis.face.view.fragment.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class VersionFragment extends Fragment {

    @BindView(R.id.app_ver)
    TextView app;
    @BindView(R.id.algorithm_ver)
    TextView algorithm;

    private Unbinder bind;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_version,container,false);
        bind = ButterKnife.bind(this, view);
        app.setText(MyUtil.getCurVersion(getActivity()));
        algorithm.setText(FaceManager.getInstance().faceVersion());
        return view;
    }
}
