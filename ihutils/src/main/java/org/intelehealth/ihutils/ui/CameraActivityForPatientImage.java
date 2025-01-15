package org.intelehealth.ihutils.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.cameraview.CameraView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ihutils.R;
import org.intelehealth.ihutils.utils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CameraActivityForPatientImage extends CameraActivity {


    public static final int TAKE_IMAGE = 205;
    /**
     * Bundle key used for the {@link String} setting custom Image Name
     * for the file generated
     */
    public static final String SET_IMAGE_NAME = "IMG_NAME";
    /**
     * Bundle key used for the {@link String} setting custom FilePath for
     * storing the file generated
     */
    public static final String SET_IMAGE_PATH = "IMG_PATH";
    /**
     * Bundle key used for the {@link String} showing custom dialog
     * message before starting the camera.
     */
    public static final String SHOW_DIALOG_MESSAGE = "DEFAULT_DLG";
    public static final String SEND_BROADCAST_AFTER_CAPTURE = "SEND_BROADCAST_AFTER_CAPTURE";
    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };
    private static final int[] FLASH_ICONS = {
            R.drawable.utils_ic_flash_auto,
            R.drawable.utils_ic_flash_off,
            R.drawable.ic_flash_on,
    };
    private static final int[] FLASH_TITLES = {
            R.string.util_flash_auto,
            R.string.util_flash_off,
            R.string.util_flash_on,
    };
    private final String TAG = CameraActivityForPatientImage.class.getSimpleName();
    private CameraView mCameraView;

    private int mCurrentFlash;

    private Handler mBackgroundHandler;

    //Pass Custom File Name Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Image Name");
    private String mImageName = null;
    //Pass Dialog Message Using intent.putExtra(CameraActivity.SET_IMAGE_NAME, "Dialog Message");
    private String mDialogMessage = null;
    //Pass Custom File Path Using intent.putExtra(CameraActivity.SET_IMAGE_PATH, "Image Path");
    private String mFilePath = null;
    private boolean fabClickFlag = true;

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), R.string.util_picture_taken, Toast.LENGTH_SHORT)
                    .show();
            //compressImageAndSave(data);
            // check and correct the image rotation
            try {
                Bitmap bitmap = BitmapUtils.rotateImageIfRequired(data);
                compressImageAndSave(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    };


    void compressImageAndSave(Bitmap bitmap) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mImageName == null) {
                    mImageName = "IMG";
                }


                //String filePath = mImagePathRoot + mImageName + ".jpg";

                File file;
                //adding image file to unSavedImages folder
                //so that the newly taken image are not replaced with the existing one
                //we will replace only when patient details save is successful

                File newDir = new File(mImagePathRoot, "unSavedImages");
                if (!newDir.exists()) {
                    boolean isCreated = newDir.mkdir();
                    if (!isCreated) {
                        setResult(RESULT_CANCELED, new Intent());
                        return;
                    }
                }

                file = new File(newDir.getPath(), mImageName + ".jpg");
                String filePath = file.getPath();

                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                    //Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //  Bitmap bitmap = Bitmap.createScaledBitmap(bmp, 600, 800, false);
                    //  bitmap.recycle();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                    bitmap.recycle();


                    Bitmap scaledBitmap = null;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

                    int actualHeight = options.outHeight;
                    int actualWidth = options.outWidth;
                    float maxHeight = 816.0f;
                    float maxWidth = 612.0f;
                    float imgRatio = actualWidth / actualHeight;
                    float maxRatio = maxWidth / maxHeight;

                    if (actualHeight > maxHeight || actualWidth > maxWidth) {
                        if (imgRatio < maxRatio) {
                            imgRatio = maxHeight / actualHeight;
                            actualWidth = (int) (imgRatio * actualWidth);
                            actualHeight = (int) maxHeight;
                        } else if (imgRatio > maxRatio) {
                            imgRatio = maxWidth / actualWidth;
                            actualHeight = (int) (imgRatio * actualHeight);
                            actualWidth = (int) maxWidth;
                        } else {
                            actualHeight = (int) maxHeight;
                            actualWidth = (int) maxWidth;
                        }
                    }

                    options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                    options.inJustDecodeBounds = false;
                    options.inDither = false;
                    options.inTempStorage = new byte[16 * 1024];

                    try {
                        bmp = BitmapFactory.decodeFile(filePath, options);
                    } catch (OutOfMemoryError exception) {
                        exception.printStackTrace();

                    }
                    try {
                        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
                    } catch (OutOfMemoryError exception) {
                        exception.printStackTrace();
                    }

                    float ratioX = actualWidth / (float) options.outWidth;
                    float ratioY = actualHeight / (float) options.outHeight;
                    float middleX = actualWidth / 3.0f;
                    float middleY = actualHeight / 3.0f;

                    Matrix scaleMatrix = new Matrix();
                    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                    Canvas canvas = new Canvas(scaledBitmap);
                    canvas.setMatrix(scaleMatrix);
                    canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 3, middleY - bmp.getHeight() / 3, new Paint(
                            Paint.FILTER_BITMAP_FLAG));

                    ExifInterface exif;
                    try {
                        exif = new ExifInterface(filePath);

                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                        Log.e("EXIF", "Exif: " + orientation);
                        Matrix matrix = new Matrix();
                        if (orientation == 6) {
                            matrix.postRotate(90);
                            Log.e("EXIF", "Exif: " + orientation);
                        } else if (orientation == 3) {
                            matrix.postRotate(180);
                            Log.e("EXIF", "Exif: " + orientation);
                        } else if (orientation == 8) {
                            matrix.postRotate(270);
                            Log.e("EXIF", "Exif: " + orientation);
                        }
                        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                                matrix, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream out = null;
                    String filename = filePath;
                    try {
                        out = new FileOutputStream(file);
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (bmp != null) {
                            bmp.recycle();
                            bmp = null;
                        }
                        if (scaledBitmap != null) {
                            scaledBitmap.recycle();
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("RESULT", file.getAbsolutePath());
                    setResult(RESULT_OK, intent);
                    Log.i(TAG, file.getAbsolutePath());
                    finish();
                } catch (IOException e) {
                    Log.w(TAG, "Cannot write to " + file, e);
                    setResult(RESULT_CANCELED, new Intent());
                    finish();
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            //FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                }

            }
        });
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public String mImagePathRoot = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImagePathRoot = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SET_IMAGE_NAME))
                mImageName = extras.getString(SET_IMAGE_NAME);
            if (extras.containsKey(SHOW_DIALOG_MESSAGE))
                mDialogMessage = extras.getString(SHOW_DIALOG_MESSAGE);
            if (extras.containsKey(SET_IMAGE_PATH))
                mFilePath = extras.getString(SET_IMAGE_PATH);
        }

        setContentView(R.layout.utils_activity_camera);
        mCameraView = findViewById(R.id.utils_camera_surface_CameraView);

        handleBackPress();

        if (mCameraView != null) mCameraView.addCallback(mCallback);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraView != null) mCameraView.stop();
        CameraActivityPermissionsDispatcher.startCameraWithPermissionCheck(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraView != null) mCameraView.stop();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.util_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.switch_flash) {
            if (mCameraView != null) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                item.setTitle(FLASH_TITLES[mCurrentFlash]);
                item.setIcon(FLASH_ICONS[mCurrentFlash]);
                mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void startCamera() {
        if (mDialogMessage != null) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setMessage(mDialogMessage)
                    .setNeutralButton(getString(R.string.util_button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.show();
            //IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
        }
        if (mCameraView != null)
            mCameraView.start();
    }


    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(final PermissionRequest request) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.util_permission_camera_rationale))
                .setPositiveButton(getString(R.string.util_button_allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(getString(R.string.util_button_deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                });
        AlertDialog dialog = builder.show();
        //IntelehealthApplication.setAlertDialogCustomTheme(this, dialog);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        Toast.makeText(this, getString(R.string.util_permission_camera_denied), Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        Toast.makeText(this, getString(R.string.util_permission_camera_never_askagain), Toast.LENGTH_SHORT).show();
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    /**
     * removed onBackPressed function due to deprecation
     * and added this one to handle onBackPressed
     */
    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    public void endCameraSession(View view) {
        finish();
    }

    public void flipCamera(View view) {
        if (mCameraView != null) {
            int facing = mCameraView.getFacing();
            mCameraView.setFacing(facing == CameraView.FACING_FRONT ? CameraView.FACING_BACK : CameraView.FACING_FRONT);
        }
    }

    public void takeImage(View view) {
        if (mCameraView != null && fabClickFlag == true) {
            fabClickFlag = false;
            mCameraView.takePicture();
        }
    }

    public void switchFlash(View view) {
        Log.v(TAG, "switchFlash");
        if (mCameraView != null) {
            mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
            //item.setTitle(FLASH_TITLES[mCurrentFlash]);
            Toast.makeText(this, FLASH_TITLES[mCurrentFlash], Toast.LENGTH_SHORT).show();
            ((ImageView) view).setImageResource(FLASH_ICONS[mCurrentFlash]);
            mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        }
    }
}
