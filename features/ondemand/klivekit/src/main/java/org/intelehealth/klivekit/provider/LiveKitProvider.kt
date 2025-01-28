package org.intelehealth.klivekit.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.AudioOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.room.Room
import io.livekit.android.room.participant.AudioTrackPublishDefaults
import io.livekit.android.room.participant.VideoTrackPublishDefaults
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalAudioTrackOptions
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.VideoPreset43
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.HardwareVideoEncoderFactory

/**
 * Created by Vaghela Mithun R. on 16-09-2023 - 20:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object LiveKitProvider {

    fun createRoom(@ApplicationContext context: Context) = provideLiveKitRoom(
        context = context, options = provideRoomOptions(
            provideLocalAudioTrackOptions(),
            provideLocalVideoTrackOptions(),
            provideAudioPublishDefault(),
            provideVideoPublishTrack()
        ), audioSwitchHandler = provideAudioSwitchHandler(context)
    )

    private fun provideLocalAudioTrackOptions() = LocalAudioTrackOptions(
        noiseSuppression = true,
        echoCancellation = true,
        autoGainControl = true,
        highPassFilter = true,
        typingNoiseDetection = true,
    )

    private fun provideLocalVideoTrackOptions() = LocalVideoTrackOptions(
        deviceId = "",
        position = CameraPosition.FRONT,
        captureParams = VideoPreset43.H1440.capture,
    )

    private fun provideAudioPublishDefault() = AudioTrackPublishDefaults(
        audioBitrate = 20_000,
        dtx = true,
    )

    private fun provideVideoPublishTrack() = VideoTrackPublishDefaults(
        videoEncoding = VideoPreset43.H480.encoding
//        videoEncoding = VideoPreset169.VGA.encoding,
//            videoCodec = VideoCodec.VP8.codecName
    )

    private fun provideRoomOptions(
        localAudioTrackOptions: LocalAudioTrackOptions,
        localVideoTrackOptions: LocalVideoTrackOptions,
        audioTrackPublishDefaults: AudioTrackPublishDefaults,
        videoTrackPublishDefaults: VideoTrackPublishDefaults
    ) = RoomOptions(
        audioTrackCaptureDefaults = localAudioTrackOptions,
        audioTrackPublishDefaults = audioTrackPublishDefaults,
        adaptiveStream = true
    )

    private fun provideAudioSwitchHandler(@ApplicationContext context: Context) = AudioSwitchHandler(context)

    private fun provideLiveKitRoom(
        @ApplicationContext context: Context, options: RoomOptions, audioSwitchHandler: AudioSwitchHandler
    ): Room = LiveKit.create(
        appContext = context, options = options, overrides = LiveKitOverrides(
            okHttpClient = RetrofitProvider.getOkHttpClient(), audioOptions = AudioOptions(
                audioHandler = audioSwitchHandler, audioOutputType = io.livekit.android.AudioType.CallAudioType()
            ), videoEncoderFactory = HardwareVideoEncoderFactory(
                EglBase.create().eglBaseContext, true, true
            )
        )
    )

}