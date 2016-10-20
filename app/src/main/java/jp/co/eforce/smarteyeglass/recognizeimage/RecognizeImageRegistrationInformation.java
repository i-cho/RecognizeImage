package jp.co.eforce.smarteyeglass.recognizeimage;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.aef.registration.Registration.ExtensionColumns;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensor;


/**
 * Created by cho on 2016/10/13.
 */

public final class RecognizeImageRegistrationInformation
        extends RegistrationInformation {

    /** The application context. */
    private final Context context;

    /** Uses control API version*/
    private static final int CONTROL_API_VERSION = 4;

    /**
     * Creates sensor registration object.
     *
     * @param context
     *            The context.
     */
    protected RecognizeImageRegistrationInformation(final Context context) {
        this.context = context;
    }

    @Override
    public int getRequiredControlApiVersion() {

        return CONTROL_API_VERSION;
    }

    @Override
    public int getTargetControlApiVersion() {

        return CONTROL_API_VERSION;
    }

    @Override
    public int getRequiredSensorApiVersion() {
        return RegistrationInformation.API_NOT_REQUIRED;
     }

    @Override
    public int getRequiredNotificationApiVersion() {

        return RegistrationInformation.API_NOT_REQUIRED;
    }

    @Override
    public int getRequiredWidgetApiVersion() {
        //return CONTROL_API_VERSION;
        return RegistrationInformation.API_NOT_REQUIRED;
    }

    @Override
    public ContentValues getExtensionRegistrationConfiguration() {
        Log.d(Constants.LOG_TAG, "RegistrationInformation: getExtensionRegistrationConfiguration");

        String iconHostApp =
                ExtensionUtils.getUriString(context, R.drawable.icon);
        String iconExtension =
                ExtensionUtils.getUriString(context, R.drawable.icon_extension);
        String iconExtension48 =
                ExtensionUtils.getUriString(context, R.drawable.icon_extension48);

        ContentValues values = new ContentValues();
        values.put(ExtensionColumns.CONFIGURATION_ACTIVITY,
                RecognizeImagePreferenceActivity.class.getName());
        values.put(ExtensionColumns.CONFIGURATION_TEXT,
                context.getString(R.string.configuration_text));
        values.put(ExtensionColumns.NAME,
                context.getString(R.string.extension_name));
        values.put(ExtensionColumns.EXTENSION_KEY, Constants.EXTENSION_KEY);
        values.put(ExtensionColumns.HOST_APP_ICON_URI, iconHostApp);
        values.put(ExtensionColumns.EXTENSION_ICON_URI, iconExtension);
        values.put(ExtensionColumns.EXTENSION_48PX_ICON_URI, iconExtension48);
        values.put(ExtensionColumns.NOTIFICATION_API_VERSION,
                getRequiredNotificationApiVersion());
        values.put(ExtensionColumns.PACKAGE_NAME, context.getPackageName());

        return values;
    }

    @Override
    public boolean isDisplaySizeSupported(final int width, final int height) {
        ScreenSize size = new ScreenSize(context);
        return (size.equals(width, height));
    }
}
