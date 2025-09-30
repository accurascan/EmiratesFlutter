package com.accurascan.accuraemirates;

import android.content.Context;

import com.docrecog.scan.CameraActivity;

import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.common.BinaryMessenger;

public class FlutterUnityViewFactory extends PlatformViewFactory {
    private final Context mContext;
    private final BinaryMessenger mBinaryMessenger;
    public FlutterUnityViewFactory(Context context, BinaryMessenger binaryMessenger) {
        super(StandardMessageCodec.INSTANCE);
        mContext = context;
        mBinaryMessenger = binaryMessenger;
    }

    @Override
    public PlatformView create(Context context, int i, Object args) {
        return new CameraActivity(context, mContext, i, mBinaryMessenger);
    }
}