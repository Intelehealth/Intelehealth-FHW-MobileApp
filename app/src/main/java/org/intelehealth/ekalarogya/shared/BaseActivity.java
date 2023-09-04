package org.intelehealth.ekalarogya.shared;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ekalarogya.database.dao.ProviderDAO;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.intelehealth.ekalarogya.webrtc.activity.EkalChatActivity;
import org.intelehealth.ekalarogya.webrtc.notification.AppNotification;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

/**
 * Created by Vaghela Mithun R. on 03-06-2023 - 19:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class BaseActivity extends AppCompatActivity implements SocketManager.NotificationListener {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketManager.getInstance().setNotificationListener(this);
    }

    @Override
    public void showNotification(@NonNull ChatMessage chatMessage) {
        RtcArgs args = new RtcArgs();
        args.setPatientName(chatMessage.getPatientName());
        args.setPatientId(chatMessage.getPatientId());
        args.setVisitId(chatMessage.getVisitId());
        args.setNurseId(chatMessage.getToUser());
        args.setDoctorUuid(chatMessage.getFromUser());
        Log.e(TAG, "showNotification: " + args.toJson());
        try {
            String title = new ProviderDAO().getProviderName(args.getDoctorUuid());
            new AppNotification.Builder(this)
                    .title(title)
                    .body(chatMessage.getMessage())
                    .pendingIntent(EkalChatActivity.getPendingIntent(this, args))
                    .send();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }
}