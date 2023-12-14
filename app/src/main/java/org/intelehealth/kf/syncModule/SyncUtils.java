package org.intelehealth.kf.syncModule;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.work.WorkManager;

import org.intelehealth.kf.R;
import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.app.IntelehealthApplication;
import org.intelehealth.kf.appointment.sync.AppointmentSync;
import org.intelehealth.kf.database.dao.ImagesPushDAO;
import org.intelehealth.kf.database.dao.SyncDAO;
import org.intelehealth.kf.utilities.Logger;
import org.intelehealth.kf.utilities.NetworkConnection;
import org.intelehealth.kf.utilities.NotificationUtils;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    /**
     * This method will be responsible for initial sync/setup
     *
     * @param fromActivity
     */
    public void initialSync(String fromActivity) {

        SyncDAO syncDAO = new SyncDAO();
        Logger.logD(TAG, "Pull Started");
        syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity);
        Logger.logD(TAG, "Pull ended");
        // sync data
        AppointmentSync.getAppointments(IntelehealthApplication.getAppContext());
    }

    public void syncBackground() {
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

        syncDAO.pushDataApi();
        syncDAO.pullData_Background(IntelehealthApplication.getAppContext()); //only this new function duplicate


        //  imagesPushDAO.patientProfileImagesPush();
        //ui2.0
        imagesPushDAO.loggedInUserProfileImagesPush();

//        imagesPushDAO.obsImagesPush();

        /*
         * Looper.getMainLooper is used in background sync since the sync_background()
         * is called from the syncWorkManager.java class which executes the sync on the
         * worker thread (non-ui thread) and the image push is executing on the
         * ui thread.
         */
        final Handler handler_background = new Handler(Looper.getMainLooper());
        handler_background.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppointmentSync.getAppointments(IntelehealthApplication.getAppContext());
                Logger.logD(TAG, "Background Image Push Started");
                imagesPushDAO.obsImagesPush();
                Logger.logD(TAG, "Background Image Pull ended");
            }
        }, 4000);

        imagesPushDAO.deleteObsImage();

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.clearAllNotifications(IntelehealthApplication.getAppContext());

        //Background Sync Fixes : Chaining of request in place of running background service
        WorkManager.getInstance()
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

    }

    public boolean syncForeground(String fromActivity) {
        boolean isSynced = false;
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        Logger.logD(TAG, "Push Started");
        isSynced = syncDAO.pushDataApi();
        Logger.logD(TAG, "Push ended");


//        need to add delay for pulling the obs correctly
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Pull Started");
                syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity);
                AppointmentSync.getAppointments(IntelehealthApplication.getAppContext());
                Logger.logD(TAG, "Pull ended");
            }
        }, 4000);

        imagesPushDAO.patientProfileImagesPush();
        //ui2.0
        imagesPushDAO.loggedInUserProfileImagesPush();
//        imagesPushDAO.obsImagesPush();

        /*
         * Handler is added for pushing image in sync foreground
         * to fix the issue of Phy exam and additional images not showing up sometimes
         * on the webapp (doctor portal).
         * */
        final Handler handler_foreground = new Handler();
        handler_foreground.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Image Push Started");
                imagesPushDAO.obsImagesPush();
                Logger.logD(TAG, "Image Pull ended");
            }
        }, 3000);

        imagesPushDAO.deleteObsImage();


        WorkManager.getInstance()
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

        /*Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);*/

        return isSynced;
    }

    /**
     * Clicking on this btn will start Sync.
     *
     * @param view Refresh button view.
     */
    public static boolean syncNow(Context context, View view, ObjectAnimator syncAnimator) {
        boolean isSynced = false;

        syncAnimator = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setInterpolator(new LinearInterpolator());

        if (NetworkConnection.isOnline(context)) {
            //Toast.makeText(context, context.getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
            view.clearAnimation();
            syncAnimator.start();
            new SyncUtils().syncBackground();

            isSynced = true;
            new Handler(Looper.getMainLooper())
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, context.getString(R.string.successfully_synced), Toast.LENGTH_SHORT).show();
                        }
                    }, 1200);

        } else {
            isSynced = false;
            Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
        }

        return isSynced;
    }

}
