package com.accurascan.accuraemirates;

import androidx.annotation.NonNull;

import android.app.Activity;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.platform.PlatformViewRegistry;

/** AccuraemiratesPlugin */
public class AccuraemiratesPlugin implements FlutterPlugin, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  public FlutterPluginBinding flutterPluginBinding;
  public static Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding;

    PlatformViewRegistry registry = flutterPluginBinding.getPlatformViewRegistry();
    registry.registerViewFactory("scan_preview", new FlutterUnityViewFactory(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger()));
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    flutterPluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    // Activity is available
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // Activity is being destroyed for config changes
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    // Activity is being recreated after config changes
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    // Activity is being destroyed
    activity = null;
  }
}
