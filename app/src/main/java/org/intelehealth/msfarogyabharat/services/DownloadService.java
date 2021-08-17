package org.intelehealth.msfarogyabharat.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.dao.ImagesDAO;
import org.intelehealth.msfarogyabharat.database.dao.ObsDAO;
import org.intelehealth.msfarogyabharat.models.download.Download;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DownloadService extends IntentService {
    static String TAG = DownloadService.class.getSimpleName();
    UrlModifiers urlModifiers = new UrlModifiers();
    ObsDAO obsDAO = new ObsDAO();
    SessionManager sessionManager = null;
    private String encounterAdultIntials;
    private int totalFileSize;
    public String baseDir = "";
    public String ImageType = "";

    public DownloadService() {
        super("Download Service");
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            ImageType = intent.getStringExtra("ImageType");
        }
//        AppConstants.notificationUtils.showNotificationProgress("Download", "Downloading File", 4, IntelehealthApplication.getAppContext(), 0);

        baseDir = AppConstants.IMAGE_PATH;
        initDownload(ImageType);

    }

    private void initDownload(String ImageType) {

        String url = "";
        List<String> imageObsList = new ArrayList<>();
        imageObsList = obsDAO.getImageStrings(ImageType, encounterAdultIntials);

        if (imageObsList.size() == 0) {
//            AppConstants.notificationUtils.DownloadDone("Download", "No Images to Download", 4, IntelehealthApplication.getAppContext());
        }
        for (int i = 0; i < imageObsList.size(); i++) {
           // List<String> image_value = new ArrayList<>();
            String image_value = obsDAO.getImageStrings_value(imageObsList.get(i), ImageType, encounterAdultIntials);

            url = urlModifiers.obsImageUrl(imageObsList.get(i));
            Observable<ResponseBody> downloadobs = AppConstants.apiInterface.OBS_IMAGE_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
            int finalI1 = i;
            List<String> finalImageObsList1 = imageObsList;
            String final_ObsImage_Value = image_value; //TODO: either get value of image_value or get data from responseBody...Better is responseBody...
            downloadobs.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ResponseBody>() {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            //TODO: responseBody should send us the filename so that we can pass it to the Download class
                            // where uuid and filename will be sent. Instead of uuid we need value i.e. Filename of that image...
                            // or else if no data in responseBody than either the PULL obs data should return image_value data
                            // from the obs table...
                            try {
                              //  downloadFile(responseBody, finalImageObsList1.get(finalI1));
                                downloadFile(responseBody, final_ObsImage_Value);
                            } catch (IOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "onerror" + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "oncomplete");

                        }
                    });
        }

    }

    private void downloadFile(ResponseBody body, String imageuuid) throws IOException {
        String imagepath = imageuuid + ".jpg";
        int count;
        byte[] data = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(baseDir + imagepath);
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendNotification(download);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete();
        output.flush();
        output.close();
        bis.close();

        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.updateObs(imageuuid);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    private void sendNotification(Download download) {

        sendIntent(download);
    }

    private void sendIntent(Download download) {

        Intent intent = new Intent(AppConstants.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);


        Intent i = new Intent();
        i.setAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        sendBroadcast(i);


    }


    private void onDownloadComplete() {

        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

//        AppConstants.notificationUtils.showNotificationProgress("Download", "File Downloaded", 4, IntelehealthApplication.getAppContext(), 100);


    }


}
