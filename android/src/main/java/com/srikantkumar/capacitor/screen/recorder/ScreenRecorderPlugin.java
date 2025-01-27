package com.srikantkumar.capacitor.screen.recorder;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.ActivityCallback;

import android.Manifest;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;
import android.util.Log;
import android.content.ContentValues;
import android.content.ContentResolver;


import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// import javax.swing.text.View;

import androidx.activity.result.ActivityResult;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.hbisoft.hbrecorder.HBRecorderCodecInfo;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.hbisoft.hbrecorder.Constants.MAX_FILE_SIZE_REACHED_ERROR;
import static com.hbisoft.hbrecorder.Constants.SETTINGS_ERROR;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;


@CapacitorPlugin(
        name = "ScreenRecorder",
        permissions = {
                @Permission(
                        alias = "notification",
                        strings = {Manifest.permission.POST_NOTIFICATIONS}
                ),
                @Permission(
                        alias = "audio",
                        strings = {Manifest.permission.RECORD_AUDIO}
                ), @Permission(
                alias = "foreground",
                strings = {Manifest.permission.FOREGROUND_SERVICE}
        ),
                @Permission(
                        alias = "storage",
                        strings = {
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }
                )
        }
)
public class ScreenRecorderPlugin extends Plugin implements HBRecorderListener {

    //Permissions
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_POST_NOTIFICATIONS = 33;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private boolean hasPermissions = false;

    //Declare HBRecorder
    private HBRecorder hbRecorder;

     @PluginMethod
    public void enableFullScreen(PluginCall call) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                );

                activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            });
            call.resolve();
        } else {
            call.reject("Failed to get activity");
        }
    }

    @PluginMethod
    public void disableFullScreen(PluginCall call) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                // Clear full-screen flag
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // Restore the default system UI
                activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE
                );
            });
            call.resolve();
        } else {
            call.reject("Failed to get activity");
        }
    }
    
    @Override
    public void load() {
        //Init HBRecorder
        hbRecorder = new HBRecorder(getContext(), this);

        HBRecorderCodecInfo hbRecorderCodecInfo = new HBRecorderCodecInfo();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int mWidth = hbRecorder.getDefaultWidth();
            int mHeight = hbRecorder.getDefaultHeight();
            String mMimeType = "video/avc";
            int mFPS = 30;
            if (hbRecorderCodecInfo.isMimeTypeSupported(mMimeType)) {
                String defaultVideoEncoder = hbRecorderCodecInfo.getDefaultVideoEncoderName(mMimeType);
                boolean isSizeAndFramerateSupported = hbRecorderCodecInfo.isSizeAndFramerateSupported(mWidth, mHeight, mFPS, mMimeType, ORIENTATION_PORTRAIT);
                Log.e("EXAMPLE", "THIS IS AN EXAMPLE OF HOW TO USE THE (HBRecorderCodecInfo) TO GET CODEC INFO:");
                Log.e("HBRecorderCodecInfo", "defaultVideoEncoder for (" + mMimeType + ") -> " + defaultVideoEncoder);
                Log.e("HBRecorderCodecInfo", "MaxSupportedFrameRate -> " + hbRecorderCodecInfo.getMaxSupportedFrameRate(mWidth, mHeight, mMimeType));
                Log.e("HBRecorderCodecInfo", "MaxSupportedBitrate -> " + hbRecorderCodecInfo.getMaxSupportedBitrate(mMimeType));
                Log.e("HBRecorderCodecInfo", "isSizeAndFramerateSupported @ Width = " + mWidth + " Height = " + mHeight + " FPS = " + mFPS + " -> " + isSizeAndFramerateSupported);
                Log.e("HBRecorderCodecInfo", "isSizeSupported @ Width = " + mWidth + " Height = " + mHeight + " -> " + hbRecorderCodecInfo.isSizeSupported(mWidth, mHeight, mMimeType));
                Log.e("HBRecorderCodecInfo", "Default Video Format = " + hbRecorderCodecInfo.getDefaultVideoFormat());

                HashMap<String, String> supportedVideoMimeTypes = hbRecorderCodecInfo.getSupportedVideoMimeTypes();
                for (Map.Entry<String, String> entry : supportedVideoMimeTypes.entrySet()) {
                    Log.e("HBRecorderCodecInfo", "Supported VIDEO encoders and mime types : " + entry.getKey() + " -> " + entry.getValue());
                }

                HashMap<String, String> supportedAudioMimeTypes = hbRecorderCodecInfo.getSupportedAudioMimeTypes();
                for (Map.Entry<String, String> entry : supportedAudioMimeTypes.entrySet()) {
                    Log.e("HBRecorderCodecInfo", "Supported AUDIO encoders and mime types : " + entry.getKey() + " -> " + entry.getValue());
                }

                ArrayList<String> supportedVideoFormats = hbRecorderCodecInfo.getSupportedVideoFormats();
                for (int j = 0; j < supportedVideoFormats.size(); j++) {
                    Log.e("HBRecorderCodecInfo", "Available Video Formats : " + supportedVideoFormats.get(j));
                }
            } else {
                Log.e("HBRecorderCodecInfo", "MimeType not supported");
            }

        }
    }

    @PluginMethod
    public void start(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //first check if permissions was granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkAndGetNotificationPermission(call);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                checkAndGetAudioPermission(call);
            } else {
                checkAndGetStoragePermission(call);
            }
        } else {
            showLongToast("This library requires API 21>");
        }
    }

    private void checkAndGetNotificationPermission(PluginCall call) {
        if (getPermissionState("notification") != PermissionState.GRANTED) {
            requestPermissionForAlias("notification", call, "notificationCallBack");
        } else {
            checkAndGetAudioPermission(call);
        }
    }

    @PermissionCallback
    private void notificationCallBack(PluginCall call) {
        if (getPermissionState("notification") == PermissionState.GRANTED) {
            checkAndGetAudioPermission(call);
        } else {
            Log.e("Permission", "Notification Not Granted");
            call.reject("Permission Notification is required");
        }
    }

    private void checkAndGetStoragePermission(PluginCall call) {
        if (getPermissionState("storage") != PermissionState.GRANTED) {
            requestPermissionForAlias("storage", call, "storageCallBack");
        } else {
            checkAndGetAudioPermission(call);
        }
    }

    @PermissionCallback
    private void storageCallBack(PluginCall call) {
        if (getPermissionState("storage") == PermissionState.GRANTED) {
            checkAndGetAudioPermission(call);
        } else {
            Log.e("Permission", "Storage Not Granted");
            call.reject("Permission Storage is required");
        }
    }

    private void checkAndGetAudioPermission(PluginCall call) {
        Log.e("Custom-----", String.valueOf(getPermissionState("audio")));
        if (getPermissionState("audio") != PermissionState.GRANTED) {
            if(getPermissionState("audio") == PermissionState.DENIED){
                call.reject("Denied");
            }
            requestPermissionForAlias("audio", call, "audioCallBack");
        } else {
            callRecordingAndGenerateResponse(call);
        }
    }

    @PermissionCallback
    private void audioCallBack(PluginCall call) {
        if (getPermissionState("audio") == PermissionState.GRANTED) {
            callRecordingAndGenerateResponse(call);
        } else {
            Log.e("Permission", "Audio Not Granted");
            call.reject("Permission Audio is required");
        }
    }


    private void callRecordingAndGenerateResponse(PluginCall call) {
        if (hbRecorder.isBusyRecording()) {
            hbRecorder.stopScreenRecording();
        } else {
            startRecordingScreen(call);

            JSObject result = new JSObject();
            result.put("status", true);
            result.put("message", "Record Start Called. Use onRecordingStarted Listener to Get Data");
            call.resolve(result);
        }
    }

    //Check Recorder Is Busy
    @PluginMethod
    public void recorder_status(PluginCall call) {
        boolean status;
        String msg;
        if (hbRecorder.isBusyRecording()) {
            status = true;
            msg = "Recorder is Running";
        } else {
            status = false;
            msg = "Recorder is Not Running";
        }
        JSObject result = new JSObject();
        result.put("status", status);
        result.put("message", msg);
        call.resolve(result);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        if (hbRecorder != null) {
            hbRecorder.stopScreenRecording();
        }
        JSObject result = new JSObject();
        result.put("status", true);
        result.put("message", "Record Stop Called. Use onRecordingComplete Listener to Get Data");
        call.resolve(result);
    }


    @Override
    public void HBRecorderOnStart() {
        // Handle Starting Result
        JSObject result = new JSObject();
        result.put("status", true);
        result.put("message", "Recording Started");
        notifyListeners("onRecordingStarted", result);

    }

    @Override
    public void HBRecorderOnComplete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Update gallery depending on SDK Level
            if (hbRecorder.wasUriSet()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    updateGalleryUri();
                } else {
                    refreshGalleryFile();
                }
            } else {
                refreshGalleryFile();
            }
        }
        JSObject result = new JSObject();
        result.put("status", true);
        result.put("message", "Recording Stopped");
        result.put("file_name", hbRecorder.getFileName());
        Log.e("File Path-------", hbRecorder.getFilePath());
        result.put("file_path", hbRecorder.getFilePath());

        notifyListeners("onRecordingComplete", result);
        //Log.e("File Path",hbRecorder.getFileName());
    }

    @Override
    public void HBRecorderOnError(int errorCode, String errorMessage) {
        JSObject result = new JSObject();
        String msg;
        if (errorCode == SETTINGS_ERROR) {
            msg = "Setting Not Supported";
        } else if (errorCode == MAX_FILE_SIZE_REACHED_ERROR) {
            msg = "Max File Size Reached";
        } else {
            msg = "Unknown Error";
            Log.e("HBRecorderOnError", errorMessage);
        }

        result.put("status", false);
        result.put("message", msg);
        notifyListeners("onRecordingError", result);
    }

    // Called when recording has been paused
    @Override
    public void HBRecorderOnPause() {
        // Called when recording was paused
    }

    // Calld when recording has resumed
    @Override
    public void HBRecorderOnResume() {
        // Called when recording was resumed
    }


    //Get/Set the selected settings
    private void quickSettings() {
        hbRecorder.recordHDVideo(false);
        hbRecorder.isAudioEnabled(true);
        hbRecorder.setVideoBitrate(2500000); // Lower bitrate
        // hbRecorder.setVideoFrameRate(30); // Reduce frame rat
    }

    //Create Folder
    //Only call this on Android 9 and lower (getExternalStoragePublicDirectory is deprecated)
    //This can still be used on Android 10> but you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    private void createFolder() {
        File f1 = new File(Environment.getExternalStorageDirectory(), "Movies/mmGuitar");
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }

    //Check if permissions was granted
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    //Show Toast
    private void showLongToast(final String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }


    //For Android 10> we will pass a Uri to HBRecorder
    //This is not necessary - You can still use getExternalStoragePublicDirectory
    //But then you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    //IT IS IMPORTANT TO SET THE FILE NAME THE SAME AS THE NAME YOU USE FOR TITLE AND DISPLAY_NAME
    ContentResolver resolver;
    ContentValues contentValues;
    Uri mUri;

    private void setOutputPath() {
        String filename = generateFileName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = getContext().getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "mmGuitar");
            contentValues.put(MediaStore.Video.Media.TITLE, filename);
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            //FILE NAME SHOULD BE THE SAME
            hbRecorder.setFileName(filename);
            hbRecorder.setOutputUri(mUri);
            hbRecorder.setOutputPath(Environment.getExternalStorageDirectory() + "/Movies/mmGuitar");
        } else {
            createFolder();
            hbRecorder.setOutputPath(Environment.getExternalStorageDirectory() + "/Movies/mmGuitar");
        }
    }

    //Generate a timestamp to be used as a file name
    private String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate).replace(" ", "");
    }

    private void updateGalleryUri() {
        contentValues.clear();
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        getContext().getContentResolver().update(mUri, contentValues, null, null);
    }

    private void checkAndOpenPermission() {
        Context context = bridge.getContext(); // Get the context from the bridge

        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE; // Replace with the desired permission
        int permissionStatus = ActivityCompat.checkSelfPermission(context, permission);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            Log.d("Permission", "hAI");
        } else {
            // Permission is not granted
            Log.d("Permission", "nAHI hAI");

            // Permission is not granted, open settings
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);

        }
    }

    private void refreshGalleryFile() {
        MediaScannerConnection.scanFile(getContext(),
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private void startRecordingScreen(PluginCall call) {
        Log.d("Start", "startRecordingScreen");
        hbRecorder.enableCustomSettings();
        quickSettings();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        
        //Deprecated Function Open Activity With Result
        //startActivityForResult(call,permissionIntent,SCREEN_RECORD_REQUEST_CODE);

        // Start the Activity for result using the name of the callback method
        startActivityForResult(call, permissionIntent, "startRecordingActivity");
    }

    @ActivityCallback
    private void startRecordingActivity(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == getActivity().RESULT_OK) {
            //Set file path or Uri depending on SDK version
            setOutputPath();
            //Start screen recording
            hbRecorder.startScreenRecording(result.getData(), result.getResultCode());
        }else {
            // Handle when user cancels the permission request
            Log.e("ScreenRecorder", "Screen recording permission denied or cancelled");
            call.reject("User cancelled the screen recording permission request.");
            JSObject obj = new JSObject();
            obj.put("status", false);
            obj.put("message", "User cancelled the screen recording permission request.");
            notifyListeners("onRecordingError", obj);
        }

    }

    ///Deprecated Function Handle Activity With Result
    // @Override
//    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.e("Handle Activity","handleOnActivityResult");
//        super.handleOnActivityResult(requestCode, resultCode, data);
//
//        Log.e("Handle Activity","handleOnActivityResult");
//        Log.e("Handle Activity2", "Integer value: " + requestCode);
//        Log.e("Handle Activity3", "Integer value: " + resultCode);
//
//        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
//
//            if (resultCode == getActivity().RESULT_OK) {
//                // Handle successful result
//                //JSObject result = new JSObject();
//                //result.put("message", "Activity result OK");
//                //notifyListeners("activityResult", result);
//
//                //Set file path or Uri depending on SDK version
//                setOutputPath();
//                //Start screen recording
//                hbRecorder.startScreenRecording(data, resultCode);
//            }
//        }
//    }

}