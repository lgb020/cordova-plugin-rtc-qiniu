package cordova.plugin.qnrtc;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.telecom.Call;

import cordova.plugin.qnrtc.activity.RoomActivity;
import cordova.plugin.qnrtc.utils.ToastUtils;

public class QNRtc extends CordovaPlugin {

    public static CordovaInterface _cordova = null;

    public  Context context = null;
    // JS回掉接口对象
    public static CallbackContext cb = null;
    // 权限申请码
    private static final int PERMISSION_REQUEST_CODE = 500;
    // 需要进行检测的权限数组
    protected String[] needPermissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void pluginInitialize() {
        _cordova = this.cordova;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            if (this.isNeedCheckPermissions(needPermissions)) {
                this.checkPermissions(needPermissions);
            }
            startConference(args, callbackContext);
            return true;
        }

        return false;
    }

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    /**
     * 获取定位
     */
    public void startConference(final CordovaArgs args, final CallbackContext callbackContext) {
        String appId;
        String userId;
        String roomName;
        String roomToken;
        JSONObject params;
        cb = callbackContext;

        try {
            params = args.getJSONObject(0);
            appId = params.has("app_id") ? params.getString("app_id") : "";
            userId = params.has("user_id") ? params.getString("user_id") : "";
            roomName = params.has("room_name") ? params.getString("room_name") : "";
            roomToken = params.has("room_token") ? params.getString("room_token") : "";
			Context context = this.cordova.getActivity().getApplicationContext();
			Activity myActivity = this.cordova.getActivity();

			new Thread(new Runnable() {
				@Override
				public void run() {

                    myActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (roomToken == null) {
								ToastUtils.s(myActivity, "empty token");
								return;
							}
							Intent intent = new Intent(myActivity, RoomActivity.class);
							intent.putExtra(RoomActivity.EXTRA_ROOM_ID, roomName.trim());
							intent.putExtra(RoomActivity.EXTRA_ROOM_TOKEN, roomToken);
							intent.putExtra(RoomActivity.EXTRA_USER_ID, userId);
                            myActivity.startActivity(intent);
						}
					});
				}
			}).start();
        } catch (JSONException e) {
            callbackContext.error("参数格式错误");
            return;
        }
    }

    /**
     *  启动应用的设置
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivity(intent);
    }

    /**
     * 检查权限
     */
    private void checkPermissions(String... permissions) {
        try {
            List<String> needRequestPermissionList = findNeedPermissions(permissions);
            if (null != needRequestPermissionList && needRequestPermissionList.size() > 0) {
                String[] array = needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]);
                cordova.requestPermissions(this, PERMISSION_REQUEST_CODE, array);
            }
        } catch (Throwable e) {

        }
    }

    /**
     * 判断是否需要权限校验
     */
    private boolean isNeedCheckPermissions(String... permission) {
        List<String> needRequestPermissionList = findNeedPermissions(permission);
        if (null != needRequestPermissionList && needRequestPermissionList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取需要获取权限的集合
     */
    private  List<String> findNeedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        try {
            for (String perm : permissions) {
                if (!cordova.hasPermission(perm)) {
                    needRequestPermissionList.add(perm);
                }
            }
        } catch (Throwable e) {

        }
        return needRequestPermissionList;
    }

    /**
     * 权限检测回调
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }

    /**
     * 显示提示信息
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限。");

        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     * 检测是否所有的权限都已经授权
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static int getResourceId(String name, String type) {
        Application app = _cordova.getActivity().getApplication();
        String package_name = app.getPackageName();
        Resources resources = app.getResources();
        int ic = resources.getIdentifier(name, type, package_name);
        return ic;
    }
}
