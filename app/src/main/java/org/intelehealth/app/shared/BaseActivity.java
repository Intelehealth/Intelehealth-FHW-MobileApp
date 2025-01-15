package org.intelehealth.app.shared;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.lifecycle.ViewModelProvider;

import com.github.ajalt.timberkt.Timber;
import com.google.gson.Gson;

import org.intelehealth.app.ayu.visit.vital.CoroutineProvider;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.ui.language.activity.LanguageActivity;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.IDAChatActivity;
import org.intelehealth.app.webrtc.notification.AppNotification;
import org.intelehealth.config.presenter.feature.data.FeatureActiveStatusRepository;
import org.intelehealth.config.presenter.feature.factory.FeatureActiveStatusViewModelFactory;
import org.intelehealth.config.presenter.feature.viewmodel.FeatureActiveStatusViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 03-06-2023 - 19:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class BaseActivity extends LanguageActivity implements SocketManager.NotificationListener {
    private static final String TAG = "BaseActivity";
    private FeatureActiveStatus featureActiveStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketManager.getInstance().setNotificationListener(this);
        loadFeatureActiveStatus();
    }

    /**
     * This method will load the active/deactivate status of overall application feature
     * like appointment = false, vital = false means as per this status we need to hide/show
     * the specific feature from the app
     */
    private void loadFeatureActiveStatus() {
        FeatureActiveStatusRepository repository = new FeatureActiveStatusRepository(ConfigDatabase.getInstance(this).featureActiveStatusDao());
        FeatureActiveStatusViewModelFactory factory = new FeatureActiveStatusViewModelFactory(repository);
        FeatureActiveStatusViewModel featureActiveStatusViewModel = new ViewModelProvider(this, factory).get(FeatureActiveStatusViewModel.class);
        featureActiveStatusViewModel.fetchFeaturesActiveStatus().observe(this, featureActiveStatus -> {
            if (featureActiveStatus != null) onFeatureActiveStatusLoaded(featureActiveStatus);
        });
    }

    /**
     * method to handle feature active/deactivate from suspended function
     * added this to disable "active/inactive" feature whenever user entering their data
     * currently using only from patient registration
     * <p>
     * not calling from BaseActivity onCreate
     * if need to use this on any other activity
     * then just call from newly created activity
     */
    public void loadFeatureActiveStatusFromSuspended() {
        FeatureActiveStatusRepository repository = new FeatureActiveStatusRepository(ConfigDatabase.getInstance(this).featureActiveStatusDao());
        FeatureActiveStatusViewModelFactory factory = new FeatureActiveStatusViewModelFactory(repository);
        FeatureActiveStatusViewModel featureActiveStatusViewModel = new ViewModelProvider(this, factory).get(FeatureActiveStatusViewModel.class);

        //normally we are not able to access coroutine from JAVA
        //hence using Coroutine Provider to access coroutineScope
        CoroutineProvider.useFeatureActiveStatusScope(
                LifecycleOwnerKt.getLifecycleScope(this),
                featureActiveStatusViewModel,
                data -> {
                    onFeatureActiveStatusLoadedFromSuspended((FeatureActiveStatus) data);
                }
        );
    }


    @Override
    public void showNotification(@NonNull ChatMessage chatMessage) {
        if (featureActiveStatus != null && featureActiveStatus.getChatSection()) {
            RtcArgs args = new RtcArgs();
            args.setPatientName(chatMessage.getPatientName());
            args.setPatientId(chatMessage.getPatientId());
            args.setVisitId(chatMessage.getVisitId());
            args.setNurseId(chatMessage.getToUser());
            args.setDoctorUuid(chatMessage.getFromUser());
            try {
                String title = new ProviderDAO().getProviderName(args.getDoctorUuid(), ProviderDTO.Columns.USER_UUID.value);
                new AppNotification.Builder(this)
                        .title(title)
                        .body(chatMessage.getMessage())
                        .pendingIntent(IDAChatActivity.getPendingIntent(this, args))
                        .send();

                saveChatInfoLog(args.getVisitId(), args.getDoctorUuid());
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveChatInfoLog(String visitId, String doctorId) throws DAOException {
        RTCConnectionDTO rtcDto = new RTCConnectionDTO();
        rtcDto.setUuid(UUID.randomUUID().toString());
        rtcDto.setVisitUUID(visitId);
        rtcDto.setConnectionInfo(doctorId);
        new RTCConnectionDAO().insert(rtcDto);
    }

    @Override
    public void saveTheDoctor(@NonNull ChatMessage chatMessage) {
        try {
            saveChatInfoLog(chatMessage.getVisitId(), chatMessage.getFromUser());
        } catch (DAOException e) {
            Timber.tag(TAG).e(e.getThwStack(), "saveTheDoctor: ");
        }
    }

    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        featureActiveStatus = activeStatus;
        Timber.tag(TAG).d("Active feature status=>%s", new Gson().toJson(activeStatus));
    }

    protected void onFeatureActiveStatusLoadedFromSuspended(FeatureActiveStatus activeStatus) {
        featureActiveStatus = activeStatus;
        Timber.tag(TAG).d("Active feature status=>%s", new Gson().toJson(activeStatus));
    }
}
