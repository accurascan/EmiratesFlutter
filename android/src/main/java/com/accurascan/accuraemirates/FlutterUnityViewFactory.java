package com.accurascan.accuraemirates;

// import android.app.Activity;

import android.content.Context;

import com.docrecog.scan.CameraActivity;

import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

// import io.flutter.plugin.common.BinaryMessenger;

public class FlutterUnityViewFactory extends PlatformViewFactory {
    private final PluginRegistry.Registrar mPluginRegistrar;
    // private final BinaryMessenger messenger;
    // private final Activity activity;

    public FlutterUnityViewFactory(PluginRegistry.Registrar registrar) {
        super(StandardMessageCodec.INSTANCE);
        mPluginRegistrar = registrar;
        // this.messenger = messenger;
        // this.activity = activity;
    }

    @Override
    public PlatformView create(Context context, int i, Object args) {

        return new CameraActivity(context, mPluginRegistrar, i);
    }
}