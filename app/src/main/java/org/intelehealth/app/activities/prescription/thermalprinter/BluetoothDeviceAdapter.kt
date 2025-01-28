package org.intelehealth.app.activities.prescription.thermalprinter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.intelehealth.app.R

class BluetoothDeviceAdapter(private val mContext: Context, private val mList: List<BluetoothDevice>) : BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return mList.size
    }

    override fun getItem(position: Int): Any {
        return mList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ViewHolder {
        var tvName: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var view = convertView

        if (view == null) {
            view = mInflater.inflate(R.layout.item_bluetooth_devices, parent, false)
            holder = ViewHolder()
            holder.tvName = view.findViewById(R.id.tv_bluetooth_device_name)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val bluetoothDevice = mList[position]
        holder.tvName?.text = if (TextUtils.isEmpty(bluetoothDevice.name)) {
            bluetoothDevice.address
        } else {
            "${bluetoothDevice.name} [${bluetoothDevice.address}]"
        }

        return view!!
    }
}