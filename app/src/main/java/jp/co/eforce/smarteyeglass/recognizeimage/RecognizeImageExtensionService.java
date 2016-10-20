package jp.co.eforce.smarteyeglass.recognizeimage;

import android.util.Log;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * Created by cho on 2016/10/14.
 */

public final class RecognizeImageExtensionService extends ExtensionService {
    /** Creates a new instance. */
    public RecognizeImageExtensionService() {

        super(Constants.EXTENSION_KEY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.LOG_TAG, "RecognizeImageExtensionService : onCreate");
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
         return new RecognizeImageRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    @Override
    public ControlExtension createControlExtension(
            final String hostAppPackageName) {
        Log.d(Constants.LOG_TAG, "Service: createControlExtension");
        boolean isApiSupported = DeviceInfoHelper
                .isSmartEyeglassScreenSupported(this, hostAppPackageName);
        Log.d(Constants.LOG_TAG, "Service: isApiSupported " + isApiSupported);
        if (isApiSupported) {
            return new RecognizeImageControl(this, hostAppPackageName);
        } else {
            Log.d(Constants.LOG_TAG, "Service: not supported, exiting");
            throw new IllegalArgumentException(
                    "No control for: " + hostAppPackageName);
        }
    }

}
