package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class PPPEApplication extends Application {

    public static final String EXPORT_PATH = "/PhoneProfilesPlusExtender";

    @Override
    public void onCreate() {
        super.onCreate();

        if (checkAppReplacingState())
            return;

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(getApplicationContext(), crashlyticsKit);
        // Crashlytics.logException(exception); -- this log will be associated with crash log.

        try {
            Crashlytics.setBool("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

        //if (BuildConfig.DEBUG) {
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            actualVersionCode = pInfo.versionCode;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(/*getApplicationContext(), */actualVersionCode));
        //}

    }

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    @SuppressLint("LongLogTag")
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            Log.w("PPPEApplication.onCreate", "app is replacing...kill");
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return false;
    }

}
