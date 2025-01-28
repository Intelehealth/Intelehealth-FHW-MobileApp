package org.intelehealth.klivekit.call.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.Timber
import io.livekit.android.LiveKit
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.events.DisconnectReason
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalScreencastVideoTrack
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.util.LoggingLevel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.klivekit.RtcEngine
import org.intelehealth.klivekit.utils.AudioType
import org.intelehealth.klivekit.utils.AudioType.SPEAKER_PHONE
import org.intelehealth.klivekit.utils.extensions.hide
import kotlin.coroutines.coroutineContext

open class CallViewModel(
    private val url: String,
    private val token: String,
    private val application: Application
) : ViewModel() {

    private var audioHandler = AudioSwitchHandler(application)

    val room = RtcEngine.create(application.applicationContext)

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error = mutableError.hide()

    private var localScreencastTrack: LocalScreencastVideoTrack? = null

    // Controls
    private val mutableMicEnabled = MutableLiveData<Map<String, Boolean>>()
    val micEnabled = mutableMicEnabled.hide()

    private val mutableCameraEnabled = MutableLiveData<Map<String, Boolean>>()
    val cameraEnabled = mutableCameraEnabled.hide()

    private val mutableRemoteConnectionQuality = MutableLiveData<ConnectionQuality>()
    val remoteConnectionQuality = mutableRemoteConnectionQuality.hide()

    private val mutableScreencastEnabled = MutableLiveData(false)
    val screenshareEnabled = mutableScreencastEnabled.hide()

    private val mutableLocalCameraMirrorStatus = MutableLiveData(false)
    val localCameraMirrorStatus = mutableLocalCameraMirrorStatus.hide()

    private val mutableIsSpeakingStatus = MutableLiveData<Map<String, Boolean>>()
    val isSpeakingStatus = mutableIsSpeakingStatus.hide()

    // Emits a string whenever a data message is received.
    private val mutableDataReceived = MutableSharedFlow<String>()
    val dataReceived = mutableDataReceived

    // Emits a string whenever a data message is received.
    private val mutableRemoteParticipantIdentity = MutableSharedFlow<String>()
    val remoteParticipantIdentity = mutableRemoteParticipantIdentity

    // Whether other participants are allowed to subscribe to this participant's tracks.
    private val mutablePermissionAllowed = MutableStateFlow(true)
    val permissionAllowed = mutablePermissionAllowed.hide()

    // Whether other participants are allowed to subscribe to this participant's tracks.
    private val mutableLocalVideoTrack = MutableLiveData<VideoTrack?>(null)
    val localVideoTrack = mutableLocalVideoTrack.hide()

    private val mutableRemoteVideoTrack = MutableLiveData<VideoTrack?>(null)
    val remoteVideoTrack = mutableRemoteVideoTrack.hide()

    private val mutableRemoteParticipantDisconnected = MutableLiveData(false)
    val remoteParticipantDisconnected = mutableRemoteParticipantDisconnected.hide()

    private val mutableParticipantConnected = MutableLiveData(false)
    val participantConnected = mutableParticipantConnected.hide()

    private val mutableCallDisconnectedReason = MutableLiveData<DisconnectReason?>()
    val remoteCallDisconnectedReason = mutableCallDisconnectedReason.hide()

    private val mutableCameraPosition = MutableLiveData<CameraPosition>()
    val cameraPosition = mutableCameraPosition.hide()

    var remoteParticipant: RemoteParticipant? = null

    init {
        LiveKit.loggingLevel = LoggingLevel.DEBUG
        viewModelScope.launch {
            collectError()
//            collectEvents()
        }


        // Start a foreground service to keep the call from being interrupted if the
        // app goes into the background.
//        val foregroundServiceIntent = Intent(application, ForegroundService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            application.startForegroundService(foregroundServiceIntent)
//        } else {
//            application.startService(foregroundServiceIntent)
//        }
    }

    private suspend fun collectError() {
        // Collect any errors.
        withContext(coroutineContext) {
            launch { error.collect { Timber.e { "Error detect =>$it" } } }
        }
    }

    private suspend fun collectEvents() {
        room.state.name
        room.events.collect {
            Timber.e { "Room event: $it" }
            when (it) {
                is RoomEvent.FailedToConnect -> mutableError.value = it.error
                is RoomEvent.Disconnected -> onDisconnected(it)
                is RoomEvent.ParticipantConnected -> onParticipantConnected(it)
                is RoomEvent.DataReceived -> onDataReceived(it)
                is RoomEvent.ParticipantDisconnected -> onParticipantDisconnected(it)
                is RoomEvent.TrackSubscribed -> collectTrackStats(it)
                is RoomEvent.ConnectionQualityChanged -> onConnectivityChanged(it)
                is RoomEvent.Reconnecting -> onParticipantReconnecting(it)
                is RoomEvent.Reconnected -> onParticipantReconnected(it)
                is RoomEvent.TrackMuted -> onParticipantMuted(it)
                is RoomEvent.TrackUnmuted -> onParticipantUnmuted(it)
                is RoomEvent.TrackPublished -> onRemoteParticipantTrackPublished(it)
                is RoomEvent.TrackUnpublished -> onRemoteParticipantTrackUnpublished(it)
                is RoomEvent.ActiveSpeakersChanged -> onParticipantSpeakerChanged(it)
                else -> {
                    Timber.e { "Room event: $it" }
                }
            }
        }
    }

    private fun onParticipantConnected(it: RoomEvent.ParticipantConnected) {
        remoteParticipant = it.participant
        remoteParticipant?.let { remote ->
            getVideoTrack(remote)?.let { track -> updateParticipantVideoTrack(track) }
        }
        mutableParticipantConnected.postValue(true)
    }


    private fun onParticipantSpeakerChanged(it: RoomEvent.ActiveSpeakersChanged) {
        it.speakers.firstOrNull()?.let {
            mutableIsSpeakingStatus.postValue(getParticipantStatusMap(it, it.isSpeaking))
        }
    }

    private fun onRemoteParticipantTrackUnpublished(it: RoomEvent.TrackUnpublished) {
//        if (it.participant is RemoteParticipant) {
        mutableCameraEnabled.postValue(getParticipantStatusMap(it.participant, false))
//        }
    }

    private fun onRemoteParticipantTrackPublished(it: RoomEvent.TrackPublished) {
        if (it.participant is RemoteParticipant) {
            remoteParticipant = it.participant as RemoteParticipant
            getVideoTrack(it.participant)?.let {
                updateParticipantVideoTrack(it)
            }
        }
    }

    private fun onParticipantMuted(it: RoomEvent.TrackMuted) {
        if (it.participant is RemoteParticipant) {
            Timber.e { "onParticipantMuted => isMuted : ${it.publication.muted}" }
            if (it.publication.kind == Track.Kind.AUDIO) {
                mutableMicEnabled.postValue(
                    getParticipantStatusMap(
                        it.participant,
                        it.publication.muted
                    )
                )
            } else if (it.publication.kind == Track.Kind.VIDEO) {
                mutableCameraEnabled.postValue(
                    getParticipantStatusMap(
                        it.participant,
                        it.publication.muted
                    )
                )
            }
        }
    }

    private fun onParticipantUnmuted(it: RoomEvent.TrackUnmuted) {
        if (it.participant is RemoteParticipant) {
            Timber.e { "onParticipantUnmuted => isMuted : ${it.publication.muted}" }
            if (it.publication.kind == Track.Kind.AUDIO) {
                mutableMicEnabled.postValue(
                    getParticipantStatusMap(
                        it.participant,
                        it.publication.muted
                    )
                )
            } else if (it.publication.kind == Track.Kind.VIDEO) {
                mutableCameraEnabled.postValue(
                    getParticipantStatusMap(
                        it.participant,
                        it.publication.muted
                    )
                )
            }
        }
    }

    private fun onParticipantReconnected(it: RoomEvent.Reconnected) {
        Timber.e { "onParticipantReconnected =>" + it.room.name }
        remoteParticipant?.let { remote ->
            getVideoTrack(remote)?.let { track ->
                updateParticipantVideoTrack(track)
            }
        }
    }

    private fun onParticipantReconnecting(it: RoomEvent.Reconnecting) {
        Timber.e { "onParticipantReconnecting =>" + it.room.name }
    }

    private fun onConnectivityChanged(it: RoomEvent.ConnectionQualityChanged) {
        if (it.participant is RemoteParticipant)
            mutableRemoteConnectionQuality.postValue(it.quality)
    }

//    private fun manageTrackPublicationOnConnectivityChanged(it: RoomEvent.ConnectionQualityChanged) {
//        viewModelScope.launch {
//            when (it.quality) {
//                ConnectionQuality.POOR -> {
//                    Timber.e { "${it.quality} => Unpublishing" }
//                    room.localParticipant.getTrackPublication(Track.Source.CAMERA)?.let {
//                        room.localParticipant.unpublishTrack(it.track!!, false)
//                    }
//                }
//
//                ConnectionQuality.EXCELLENT,
//                ConnectionQuality.GOOD,
//                ConnectionQuality.UNKNOWN -> {
//                    Timber.e { "${it.quality} => republishTracks" }
//                    room.localParticipant.republishTracks()
//                }
//
//                ConnectionQuality.LOST -> {
//                    Timber.e { "${it.quality} => Lost" }
//                }
//            }
//        }
//    }

    private suspend fun onDataReceived(roomEvent: RoomEvent.DataReceived) {
        val identity = roomEvent.participant?.identity ?: "server"
        val message = roomEvent.data.toString(Charsets.UTF_8)
        mutableDataReceived.emit("$identity: $message")
    }

    private fun onParticipantDisconnected(participantDisconnected: RoomEvent.ParticipantDisconnected) {
        Timber.e { "onParticipantDisconnected => ${participantDisconnected.participant.sid}" }
        Timber.e { "onParticipantDisconnected => ${remoteParticipant?.sid ?: "null"}" }
        remoteParticipant?.let {
            mutableRemoteParticipantDisconnected.postValue(participantDisconnected.participant.sid == it.sid)
        }
    }

    private fun onDisconnected(event: RoomEvent.Disconnected) {
        Timber.e { "onDisconnected => ${event.reason.name}" }
        mutableCallDisconnectedReason.postValue(event.reason)
    }

    private fun collectTrackStats(event: RoomEvent.TrackSubscribed) {
        if (event.track is VideoTrack) updateParticipantVideoTrack(event.track as VideoTrack)
    }

    private fun updateParticipantVideoTrack(videoTrack: VideoTrack) {
        if (videoTrack is LocalVideoTrack) {
            mutableLocalVideoTrack.postValue(videoTrack)
        } else {
            mutableRemoteVideoTrack.postValue(videoTrack)
        }
    }

//    private fun observeSpeaking(participant: Participant) {
//        viewModelScope.launch {
//            participant::isSpeaking.flow.collect { isSpeaking ->
//                mutableIsSpeakingStatus.postValue(getParticipantStatusMap(participant, isSpeaking))
//            }
//        }
//    }

    private fun getParticipantStatusMap(participant: Participant, flag: Boolean) =
        HashMap<String, Boolean>().apply {
            val key = if (participant == room.localParticipant) LOCAL_PARTICIPANT
            else REMOTE_PARTICIPANT
            put(key, flag)
        }

//    private fun getRemoteParticipantIdentity(remoteParticipant: Participant) {
//        viewModelScope.launch {
//            remoteParticipant::identity.flow.collect { identity ->
//                identity?.let { mutableRemoteParticipantIdentity.emit(it.value) }
//            }
//        }
//    }

//    private fun observeRemoteParticipantAudioTrack(remoteParticipant: Participant) {
//        viewModelScope.launch {
//            remoteParticipant::audioTracks.flow
//                .flatMapLatest { tracks ->
//                    val audioTrack = tracks.firstOrNull()?.first
//                    if (audioTrack != null) {
//                        audioTrack::muted.flow
//                    } else {
//                        flowOf(true)
//                    }
//                }
//                .collect { muted ->
//                    mutableMicEnabled.postValue(getParticipantStatusMap(remoteParticipant, muted))
//                }
//        }
//    }

//    private fun checkRemoteParticipantConnectivity(remoteParticipant: Participant) {
//        viewModelScope.launch {
//            remoteParticipant::connectionQuality.flow
//                .collect { quality ->
//                    mutableRemoteConnectionQuality.postValue(quality)
////                    viewBinding.connectionQuality.visibility =
////                        if (quality == ConnectionQuality.POOR) View.VISIBLE else View.INVISIBLE
//                }
//        }
//    }

    private fun getVideoTrack(participant: Participant): VideoTrack? {
        return participant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
    }

//    @SuppressLint("FlowDelegateUsageDetector")
//    private fun updateVideoTrack(participant: Participant) {
//        // observe videoTracks changes.
//        val videoTrackPubFlow = participant::videoTracks.flow
//            .map { participant to it }
//            .flatMapLatest { (participant, videoTracks) ->
//                // Prioritize any screenshare streams.
//                val trackPublication = participant.getTrackPublication(Track.Source.SCREEN_SHARE)
//                    ?: participant.getTrackPublication(Track.Source.CAMERA)
//                    ?: videoTracks.firstOrNull()?.first
//
//                flowOf(trackPublication)
//            }
//
//        viewModelScope.launch {
//            val videoTrackFlow = videoTrackPubFlow
//                .flatMapLatestOrNull { pub -> pub::track.flow }
//
//            // Configure video view with track
//            launch {
//                videoTrackFlow.collectLatest { videoTrack ->
//                    videoTrack?.let {
//                        updateParticipantVideoTrack(it as VideoTrack)
//                    }
//                }
//            }
//
//            // For local participants, mirror camera if using front camera.
//            if (participant == room.localParticipant) {
//                launch {
//                    videoTrackFlow
//                        .flatMapLatestOrNull { track -> (track as LocalVideoTrack)::options.flow }
//                        .collectLatest { options ->
//                            mutableLocalCameraMirrorStatus.postValue(options?.position == CameraPosition.FRONT)
//                            //viewBinding.renderer.setMirror(options?.position == CameraPosition.FRONT)
//                        }
//                }
//            }
//        }
//    }

    fun connectToRoom() {
        viewModelScope.launch {
            try {
                Timber.e { System.currentTimeMillis().toString() }
                Timber.e { "Url: $url" }
                Timber.e { "Token => $token" }
                launch { collectEvents() }

                RtcEngine.connectInRoom(url, token)
//                room.connect(
//                    url = url,
//                    token = token,
//                    options = ConnectOptions(
//                        audio = true, video = true, autoSubscribe = true
//                    )
//                )

//                room.audioHandler.start()

                setupLocalTrack()
                setupRemoteTrack()
            } catch (e: Throwable) {
                mutableError.value = e
            }
        }
    }

    // Create and publish local audio/video tracks
    private suspend fun setupLocalTrack() {
        withContext(coroutineContext) {
            val localParticipant = room.localParticipant
            localParticipant.setMicrophoneEnabled(true)

            mutableMicEnabled.postValue(
                getParticipantStatusMap(
                    localParticipant,
                    localParticipant.isMicrophoneEnabled()
                )
            )

            localParticipant.setCameraEnabled(true)
            mutableCameraEnabled.postValue(
                getParticipantStatusMap(
                    localParticipant,
                    localParticipant.isCameraEnabled()
                )
            )

            if (room.audioHandler is AudioSwitchHandler) {
                audioHandler = room.audioHandler as AudioSwitchHandler
            }

            Timber.e { "Before Selected audio => ${audioHandler.selectedAudioDevice?.name}" }
            updateAudioSetting(SPEAKER_PHONE)
            Timber.e { "After Selected audio => ${audioHandler.selectedAudioDevice?.name}" }
//            val audioTrack = localParticipant.createAudioTrack(
//                "audio",
//                this@CallViewModel.options.audioTrackCaptureDefaults!!
//            )
////
//            localParticipant.publishAudioTrack(audioTrack)

//            val videoTrack = localParticipant.createVideoTrack(
//                "video", LocalVideoTrackOptions(
//                    position = CameraPosition.FRONT
//                )
//            )
//
//            localParticipant.publishVideoTrack(videoTrack)
//            updateVideoTrack(localParticipant)
            mutableCameraPosition.postValue(CameraPosition.FRONT)
            mutableLocalVideoTrack.postValue(getVideoTrack(localParticipant))
        }
    }

    // Create and publish remote audio/video tracks
    suspend fun setupRemoteTrack() {
        withContext(coroutineContext) {
            remoteParticipant = room.remoteParticipants.values.firstOrNull()
            mutableRemoteVideoTrack.postValue(remoteParticipant?.let { getVideoTrack(it) })
//            remoteParticipant?.let { observeSpeaking(it) }
//            remoteParticipant?.let { getRemoteParticipantIdentity(it) }
//            remoteParticipant?.let { observeRemoteParticipantAudioTrack(it) }
//            remoteParticipant?.let { checkRemoteParticipantConnectivity(it) }
//            remoteParticipant?.let { updateVideoTrack(it) }
        }
    }

    fun resumeRemoteVideoTrack() {
        viewModelScope.launch {
            val videoTrack = remoteParticipant?.let { return@let getVideoTrack(it) }
            videoTrack?.start()
            mutableRemoteVideoTrack.postValue(videoTrack)
        }
    }

    fun pauseRemoteVideoTrack() {
        viewModelScope.launch {
            val videoTrack = remoteParticipant?.let { return@let getVideoTrack(it) }
            videoTrack?.stop()
            mutableRemoteVideoTrack.postValue(videoTrack)
        }
    }

    /**
     * Start a screen capture with the result intent from
     * [MediaProjectionManager.createScreenCaptureIntent]
     */
    fun startScreenCapture(mediaProjectionPermissionResultData: Intent) {
        val localParticipant = room.localParticipant
        viewModelScope.launch {
            val screencastTrack =
                localParticipant.createScreencastTrack(mediaProjectionPermissionResultData = mediaProjectionPermissionResultData)
            localParticipant.publishVideoTrack(
                screencastTrack
            )

            // Must start the foreground prior to startCapture.
            screencastTrack.startForegroundService(null, null)
            screencastTrack.startCapture()

            this@CallViewModel.localScreencastTrack = screencastTrack
            mutableScreencastEnabled.postValue(screencastTrack.enabled)
        }
    }

    fun stopScreenCapture() {
        viewModelScope.launch {
            localScreencastTrack?.let { localScreencastVideoTrack ->
                localScreencastVideoTrack.stop()
                room.localParticipant.unpublishTrack(localScreencastVideoTrack)
                mutableScreencastEnabled.postValue(localScreencastTrack?.enabled ?: false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        room.disconnect()
//        room.release()

        // Clean up foreground service
//        val foregroundServiceIntent = Intent(application, ForegroundService::class.java)
//        application.stopService(foregroundServiceIntent)
    }

    fun toggleMicrophone() {
        viewModelScope.launch {
            val enabled = room.localParticipant.isMicrophoneEnabled().not()
            room.localParticipant.setMicrophoneEnabled(enabled)
            mutableMicEnabled.postValue(getParticipantStatusMap(room.localParticipant, enabled))
        }
    }

    fun toggleCamera() {
        viewModelScope.launch {
            val enabled = room.localParticipant.isCameraEnabled().not()
            room.localParticipant.setCameraEnabled(enabled)
            mutableCameraEnabled.postValue(getParticipantStatusMap(room.localParticipant, enabled))
        }
    }

    fun flipCamera() {
        val videoTrack = room.localParticipant.getTrackPublication(Track.Source.CAMERA)
            ?.track as? LocalVideoTrack
            ?: return

        val newPosition = when (videoTrack.options.position) {
            CameraPosition.FRONT -> CameraPosition.BACK
            CameraPosition.BACK -> CameraPosition.FRONT
            else -> null
        }

        videoTrack.switchCamera(position = newPosition)
        mutableCameraPosition.postValue(newPosition ?: CameraPosition.FRONT)
    }

    fun dismissError() {
        mutableError.value = null
    }

    fun sendData(message: String) {
        viewModelScope.launch {
            room.localParticipant.publishData(message.toByteArray(Charsets.UTF_8))
        }
    }

    fun toggleSubscriptionPermissions() {
        mutablePermissionAllowed.value = !mutablePermissionAllowed.value
        room.localParticipant.setTrackSubscriptionPermissions(mutablePermissionAllowed.value)
    }

    // Debug functions
    fun simulateMigration() {
        room.sendSimulateScenario(Room.SimulateScenario.MIGRATION)
    }

    fun simulateNodeFailure() {
        room.sendSimulateScenario(Room.SimulateScenario.NODE_FAILURE)
    }

    fun reconnect() {
        Timber.e { "Reconnecting." }
        room.disconnect()
        viewModelScope.launch {
            connectToRoom()
        }
    }

    fun disconnect() {
        room.disconnect()
    }

    fun updateAudioSetting(audioType: AudioType) {
        audioHandler.selectDevice(getAudioDevice(audioType))
        audioHandler.start()
    }

    private fun getAudioDevice(audioType: AudioType) =
        audioHandler.availableAudioDevices.find { it.name == audioType.value }

    companion object {
        const val REMOTE_PARTICIPANT = "remote_participant"
        const val LOCAL_PARTICIPANT = "local_participant"
    }
}
