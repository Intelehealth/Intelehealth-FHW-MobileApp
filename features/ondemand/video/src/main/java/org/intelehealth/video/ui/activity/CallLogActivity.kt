package org.intelehealth.video.ui.activity

import android.os.Bundle
import org.intelehealth.video.model.VideoCallLog

/**
 * Created by Vaghela Mithun R. on 16-10-2023 - 12:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogActivity : CoreCallLogActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onLogs(logs: List<VideoCallLog>) {

    }
}