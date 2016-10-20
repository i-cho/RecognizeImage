package jp.co.eforce.smarteyeglass.recognizeimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;

import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.sony.smarteyeglass.SmartEyeglassControl;
import com.sony.smarteyeglass.extension.util.CameraEvent;
import com.sony.smarteyeglass.extension.util.ControlCameraException;
import com.sony.smarteyeglass.extension.util.SmartEyeglassControlUtils;
import com.sony.smarteyeglass.extension.util.SmartEyeglassEventListener;
import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

import java.io.File;

import static com.sony.smarteyeglass.SmartEyeglassControl.Intents.POWER_MODE_HIGH;

/**
 * Created by cho on 2016/10/14.
 */

public final class RecognizeImageControl extends ControlExtension {

    /** Uses SmartEyeglass API version*/
    private static final int SMARTEYEGLASS_API_VERSION = 3;
    private final int width;
    private final int height;
    private final int drawX;
    private final int drawY;

    /** The application context. */
    private final Context context;

    /**
     *  Instance of the Control Utility class.
     */
    private final SmartEyeglassControlUtils utils;
    private boolean cameraStarted = false;
    private int jpegQuality = SmartEyeglassControl.Intents.CAMERA_JPEG_QUALITY_STANDARD;
    private int resolution = SmartEyeglassControl.Intents.CAMERA_RESOLUTION_VGA;
    private int recordingMode = SmartEyeglassControl.Intents.CAMERA_MODE_STILL;

    // The image selected to detect.
    private Bitmap mBitmap;

    private VisionServiceClient client;

    /**
     * for debugging
     */
    private boolean saveToSdcard = true;
    private int saveFileIndex;
    private String saveFilePrefix;
    private File saveFolder;


    /**
     * Creates an instance of this control class.
     *
     * @param context
     *            The application context.
     * @param hostAppPackageName
     *            Package name of host application.
     */
    public RecognizeImageControl(final Context context,
                                  final String hostAppPackageName) {
        super(context, hostAppPackageName);
        this.context = context;

        if (client == null){
            client = new VisionServiceRestClient(context.getString(R.string.subscription_key));
        }

        // Initialize listener for camera events
        SmartEyeglassEventListener listener = new SmartEyeglassEventListener() {
            // When camera operation has succeeded
            // handle result according to current recording mode
            @Override
            public void onCameraReceived(final CameraEvent event) {
                Log.d(Constants.LOG_TAG, "Stream Event coming: " + event.toString());
                cameraEventOperation(event);

                // TODO: next describing...
            }

            // Called when camera operation has failed
            // We just log the error
            @Override
            public void onCameraErrorReceived(final int error) {
                Log.d(Constants.LOG_TAG, "onCameraErrorReceived: " + error);
            }

            // When camera is set to record image to a file,
            // log the operation and clean up
            @Override
            public void onCameraReceivedFile(final String filePath) {
                Log.d(Constants.LOG_TAG, "onCameraReceivedFile: " + filePath);
            }
        };

        utils = new SmartEyeglassControlUtils(hostAppPackageName, listener);
        utils.setRequiredApiVersion(SMARTEYEGLASS_API_VERSION);
        utils.activate(context);

        // Set the camera mode to match the setup
        utils.setCameraMode(jpegQuality, resolution, recordingMode);

        width = context.getResources().getDimensionPixelSize(R.dimen.preview_image_width);
        height = context.getResources().getDimensionPixelSize(R.dimen.preview_image_height);

        drawX = context.getResources().getInteger(R.integer.preview_image_left);
        drawY = context.getResources().getInteger(R.integer.preview_image_top);

        saveFolder = new File(Environment.getExternalStorageDirectory(), "eforce");
        if (!saveFolder.exists()) {
            saveFolder.mkdirs();
        }

        // Switch WLAN mode ON to achieve higher speed on live stream
        //utils.setPowerMode(SmartEyeglassControl.Intents.POWER_MODE_HIGH);
    }

    // When app becomes visible, set up camera mode choices
    // and instruct user to begin camera operation
    @Override
    public void onResume() {
        Log.d(Constants.LOG_TAG, "Control onResume");
        // Note: Setting the screen to be always on will drain the accessory
        // battery. It is done here solely for demonstration purposes.
        setScreenState(Control.Intents.SCREEN_STATE_ON);

        Time now = new Time();
        now.setToNow();
        saveFilePrefix = "smarteyeglass_" + now.format2445() + "_";
        saveFileIndex = 0;

        // Set the camera mode to match the setup
        utils.setCameraMode(jpegQuality, resolution, recordingMode);

        cameraStarted = false;

        updateLayout();
        setResultText(R.string.usage);

        super.onResume();
    }

    // Clean up any open files and reset mode when app is paused.
    @Override
    public void onPause() {
        // Stop camera.
        if (cameraStarted) {
            Log.d(Constants.LOG_TAG, "onPause() : stopCamera");
            cleanupCamera();
        }
    }

    // Clean up data structures on termination.
    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, "Control onDestroy");
        utils.stopCamera();
        utils.deactivate();
    }

    /**
     * Respond to tap on touch pad by triggering camera capture
     */
    @Override
    public void onTouch(final ControlTouchEvent event) {
        super.onTouch(event);

        if (event.getAction() == Control.TapActions.SINGLE_TAP) {
            if (!cameraStarted) {
                initializeCamera();
            }
            // Call for camera capture for Still recording modes.
            utils.requestCameraCapture();
        }
    }

    /**
     * Call the startCamera, and start video recording or shooting.
     */
    private void initializeCamera() {
        try {
            // Start camera without filepath for other recording modes
            Log.d(Constants.LOG_TAG, "startCamera ");
            utils.startCamera();
        } catch (ControlCameraException e) {
            Log.d(Constants.LOG_TAG, "Failed to register listener", e);
        }
        Log.d(Constants.LOG_TAG, "onResume: Registered listener");

        cameraStarted = true;
    }

    /**
     * Call the stopCamera, and stop video recording or shooting.
     */
    private void cleanupCamera() {
        utils.stopCamera();
        cameraStarted = false;
    }

    /**
     * Received camera event and operation each event.
     *
     * @param event
     */
    private void cameraEventOperation(CameraEvent event) {
        if (event.getErrorStatus() != 0) {
            Log.d(Constants.LOG_TAG, "error code = " + event.getErrorStatus());
            return;
        }

        if(event.getIndex() != 0){
            Log.d(Constants.LOG_TAG, "not oparate this event");
            return;
        }

        Bitmap bitmap = null;
        byte[] data = null;

        if ((event.getData() != null) && ((event.getData().length) > 0)) {
            // JPEG/YUV422
            data = event.getData();
            // jpeg to bitmap.
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        if (bitmap == null) {
            Log.d(Constants.LOG_TAG, "bitmap == null");
            return;
        }

        if (saveToSdcard == true) {
            String fileName = saveFilePrefix + String.format("%04d", saveFileIndex) + ".jpg";
            new SavePhotoTask(saveFolder, fileName).execute(data);
            saveFileIndex++;
        }

        if (recordingMode == SmartEyeglassControl.Intents.CAMERA_MODE_STILL) {
            Bitmap baseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            //bitmap[3] = Bitmap.createScaledBitmap(bitmap[0], 80, 48, true);
            baseBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
            Canvas canvas = new Canvas(baseBitmap);

            // 描画元の矩形
            Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            // 描画先の矩形
            Rect destRect = new Rect(0, 0, width, height);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            // TODO: これで元画像縮小される?
            canvas.drawBitmap(bitmap, srcRect, destRect, paint);

            // TODO: update message.
            setResultText(R.string.describe);

            //utils.showBitmap(baseBitmap);
            utils.showBitmap(baseBitmap, drawX, drawY);

            return;
        }

    }

    /**
     *  Update the display with the dynamic message text.
     */
    private void updateLayout() {
        showLayout(R.layout.layout_control, null);
    }

    /**
     *  Update the display with the dynamic message text.
     */
    private void setResultText(int idRes) {
        String text = context.getString(idRes);
        sendText(R.id.textResult, text);
    }

    private void setResultText(String text) {
        sendText(R.id.textResult, text);
    }
}
