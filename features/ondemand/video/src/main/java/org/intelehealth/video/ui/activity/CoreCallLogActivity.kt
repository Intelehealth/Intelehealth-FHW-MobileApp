package org.intelehealth.video.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.core.utils.extensions.viewModelByFactory
import org.intelehealth.video.data.CallLogRepository
import org.intelehealth.video.model.VideoCallLog
import org.intelehealth.video.room.CallDatabase
import org.intelehealth.video.ui.viewmodel.CallLogViewModel

/**
 * Created by Vaghela Mithun R. on 16-10-2023 - 12:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class CoreCallLogActivity : AppCompatActivity() {
    private val callLogViewModel: CallLogViewModel by viewModelByFactory {
        CallLogViewModel(CallLogRepository(CallDatabase.getInstance(this).rtcCallLogDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callLogViewModel.getCallLogs().observe(this) { onLogs(it) }
        setupActionBar()
    }

    abstract fun onLogs(logs: List<VideoCallLog>)

    fun clearLog() = callLogViewModel.clearLogs()

    open fun setupActionBar() {
    }

}