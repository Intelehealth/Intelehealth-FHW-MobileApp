package org.intelehealth.app.activities.prescription.thermalprinter

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.intelehealth.app.R

class BluetoothDeviceChooseDialog : DialogFragment() {

    private val TAG = this::class.java.simpleName

    private lateinit var mContext: Context
    private var mListener: OnDeviceItemClickListener? = null
    private lateinit var lvPairedDevices: ListView
    private lateinit var lvFoundDevices: ListView
    private lateinit var tvPairedDeviceEmpty: TextView
    private lateinit var tvFoundDeviceEmpty: TextView
    private lateinit var tvSearchDevice: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnHide: Button

    private var mBluetoothReceiver: BroadcastReceiver? = null
    private var mBluetoothIntentFilter: IntentFilter? = null
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDeviceAdapter: BluetoothDeviceAdapter
    private lateinit var foundDeviceAdapter: BluetoothDeviceAdapter
    private lateinit var pairedDeviceList: MutableList<BluetoothDevice>
    private lateinit var foundDeviceList: MutableList<BluetoothDevice>
    private var mSearchInited =
        false // true if the search button has been pressed and data initialized
    private var mRegistered = false // true if the receiver is registered
    private var isHidePairedDevList = false // true if the paired device list is hidden

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mContext = activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_choose_bluetooth_device, null)
        initView(view)
        setListener()
        initData()
        val builder = AlertDialog.Builder(mContext)
        builder.setView(view).setCancelable(true)
        val dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
            addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }

        return dialog
    }

    private fun initView(view: View) {
        lvPairedDevices = view.findViewById(R.id.lv_dialog_choose_bluetooth_device_paired_devices)
        tvPairedDeviceEmpty =
            view.findViewById(R.id.tv_dialog_choose_bluetooth_device_paired_devices_empty)
        lvFoundDevices = view.findViewById(R.id.lv_dialog_choose_bluetooth_device_found_devices)
        tvFoundDeviceEmpty =
            view.findViewById(R.id.tv_dialog_choose_bluetooth_device_found_devices_empty)
        tvSearchDevice = view.findViewById(R.id.tv_dialog_choose_bluetooth_device_search_device)
        progressBar = view.findViewById(R.id.pb_dialog_choose_bluetooth_device_progress_bar)
        btnHide = view.findViewById(R.id.btn_hide)
    }

    private fun setListener() {
        tvSearchDevice.setOnClickListener {
            tvSearchDevice.isEnabled = false
            progressBar.visibility = View.VISIBLE
            tvFoundDeviceEmpty.visibility = View.GONE
            if (mSearchInited) {
                foundDeviceList.clear()
                foundDeviceAdapter.notifyDataSetChanged()
            } else {
                foundDeviceList = mutableListOf()
                foundDeviceAdapter = BluetoothDeviceAdapter(mContext, foundDeviceList)
                lvFoundDevices.adapter = foundDeviceAdapter
                mBluetoothReceiver = BluetoothDeviceReceiver()
                mBluetoothIntentFilter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                mSearchInited = true
            }
            ContextCompat.registerReceiver(
                mContext,
                mBluetoothReceiver!!,
                mBluetoothIntentFilter!!,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            mRegistered = true
            mBluetoothAdapter.startDiscovery()
        }

        lvPairedDevices.setOnItemClickListener { parent, _, position, _ ->
            mBluetoothAdapter.cancelDiscovery()
            if (mRegistered) {
                mContext.unregisterReceiver(mBluetoothReceiver)
                mRegistered = false
            }
            mListener?.onDeviceItemClick(parent.getItemAtPosition(position) as BluetoothDevice)
            dialog?.dismiss()
        }

        lvFoundDevices.setOnItemClickListener { parent, _, position, _ ->
            mBluetoothAdapter.cancelDiscovery()
            if (mRegistered) {
                mContext.unregisterReceiver(mBluetoothReceiver)
                mRegistered = false
            }
            mListener?.onDeviceItemClick(parent.getItemAtPosition(position) as BluetoothDevice)
            dialog?.dismiss()
        }

        btnHide.setOnClickListener {
            if (isHidePairedDevList) {
                isHidePairedDevList = false
                lvPairedDevices.visibility = View.VISIBLE
                btnHide.text = "Hide_↑↑↑"
            } else {
                isHidePairedDevList = true
                lvPairedDevices.visibility = View.GONE
                btnHide.text = "Show_↓↓↓"
            }
        }
    }

    private fun initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDeviceList = mBluetoothAdapter.bondedDevices.toMutableList()
        if (pairedDeviceList.isEmpty()) {
            tvPairedDeviceEmpty.visibility = View.VISIBLE
        }
        pairedDeviceAdapter = BluetoothDeviceAdapter(mContext, pairedDeviceList)
        lvPairedDevices.adapter = pairedDeviceAdapter
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mBluetoothAdapter.cancelDiscovery()
        if (mRegistered) {
            mContext.unregisterReceiver(mBluetoothReceiver)
        }
    }

    fun setOnDeviceItemClickListener(listener: OnDeviceItemClickListener) {
        mListener = listener
    }

    interface OnDeviceItemClickListener {
        fun onDeviceItemClick(device: BluetoothDevice)
    }

    private inner class BluetoothDeviceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val devType = device?.bluetoothClass?.majorDeviceClass
                if (devType != BluetoothClass.Device.Major.IMAGING) {
                    return
                }
                if (device !in foundDeviceList) {
                    foundDeviceList.add(device)
                    foundDeviceAdapter.notifyDataSetChanged()
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                mBluetoothAdapter.cancelDiscovery()
                mContext.unregisterReceiver(mBluetoothReceiver)
                mRegistered = false
                tvSearchDevice.isEnabled = true
                progressBar.visibility = View.GONE
                if (foundDeviceList.isEmpty()) {
                    tvFoundDeviceEmpty.visibility = View.VISIBLE
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = requireContext().resources.getDimensionPixelSize(R.dimen.internet_dialog_width)  // Set width of the dialog
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)  // Set width and height
        }
    }
}