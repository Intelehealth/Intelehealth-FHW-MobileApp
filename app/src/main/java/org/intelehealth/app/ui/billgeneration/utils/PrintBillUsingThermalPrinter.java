package org.intelehealth.app.ui.billgeneration.utils;

import static com.rt.printerlibrary.enumerate.CommonEnum.ALIGN_MIDDLE;

import static org.intelehealth.app.activities.prescription.thermalprinter.TextPrintESCActivity.curPrinterInterface;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.BluetoothFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.rt.printerlibrary.utils.FuncUtils;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.prescription.thermalprinter.BaseEnum;
import org.intelehealth.app.activities.prescription.thermalprinter.BluetoothDeviceChooseDialog;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.databinding.ActivityBillCreationBinding;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class PrintBillUsingThermalPrinter implements PrinterObserver {
    private Context context;
    private ActivityBillCreationBinding binding;
    private Activity activity;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    private Bitmap mBitmap = null;
    private int bmpPrintWidth = 50;
    private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    private Object configObj;
    //public static PrinterInterface curPrinterInterface = null;
    private FragmentManager fragmentManager;

    public PrintBillUsingThermalPrinter(Context context, ActivityBillCreationBinding binding, Activity activity,
                                 FragmentManager fragmentManager) {
        this.context = context;
        this.binding = binding;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
    }

     public void textPrint() throws UnsupportedEncodingException {
        if (IntelehealthApplication.getInstance().getCurrentCmdType() == BaseEnum.CMD_ESC) {
            escPrint();
        }
    }

    public void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(
                device -> {
                    if (TextUtils.isEmpty(device.getName())) {
                        binding.contentGenerateBill.tvDeviceSelected.setText(device.getAddress());
                    } else {
                        binding.contentGenerateBill.tvDeviceSelected.setText(device.getName() + " [" + device.getAddress() + "]");
                    }
                    configObj = new BluetoothEdrConfigBean(device);
                    binding.contentGenerateBill.tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
                    isConfigPrintEnable(configObj);

                });

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(context,
                    context.getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            Toast.makeText(context,
                    context.getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Bluetooth is Turned ON.
            bluetoothDeviceChooseDialog.show(fragmentManager, null);
        }
    }

    public void initBluetoothDevice() {
        IntelehealthApplication.getInstance().setCurrentCmdType(BaseEnum.CMD_ESC);
        // printerFactory = new UniversalPrinterFactory();
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        PrinterObserverManager.getInstance().add(this);

        if (curPrinterInterface != null) {
            // to maintain the bluetooth pairing throughout the app.
            rtPrinter.setPrinterInterface(curPrinterInterface);
            binding.contentGenerateBill.tvDeviceSelected.setText(curPrinterInterface.getConfigObject().toString());
            binding.contentGenerateBill.tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
            printerInterfaceArrayList.add(curPrinterInterface);
            rtPrinter.setPrinterInterface(curPrinterInterface);
            setPrintEnable(true);
        }

        IntelehealthApplication.getInstance().setRtPrinter(rtPrinter);
        rtPrinter = IntelehealthApplication.getInstance().getRtPrinter();
        textSetting = new TextSetting();
        binding.contentGenerateBill.finalBillCV.setBackgroundColor(context.getResources().getColor(R.color.white));
        binding.contentGenerateBill.finalBillCV.setDrawingCacheEnabled(true);
        binding.contentGenerateBill.finalBillCV.buildDrawingCache();
        binding.contentGenerateBill.finalBillCV.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        binding.contentGenerateBill.finalBillCV.layout(0, 0, binding.contentGenerateBill.finalBillCV.getMeasuredWidth(), binding.contentGenerateBill.finalBillCV.getMeasuredHeight());
        mBitmap = binding.contentGenerateBill.finalBillCV.getDrawingCache(); // converting cardview to bitmap
    }

    @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.contentGenerateBill.pbConnect.setVisibility(View.GONE);
                if (state == CommonEnum.CONNECT_STATE_SUCCESS) {
                    Toast.makeText(context, printerInterface.getConfigObject().toString()
                            + context.getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                } else if (state == CommonEnum.CONNECT_STATE_INTERRUPTED) {
                    if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        Toast.makeText(context, printerInterface.getConfigObject().toString()
                                        + context.getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    }
                }


                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
                        binding.contentGenerateBill.tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
                        curPrinterInterface = printerInterface; // set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
//                            Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                            + getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(TextPrintESCActivity.this, getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        }
                        TimeRecordUtils.record("Time：", System.currentTimeMillis());
                        binding.contentGenerateBill.tvDeviceSelected.setText(R.string.please_connect);
                        binding.contentGenerateBill.tvDeviceSelected.setTag(BaseEnum.NO_DEVICE);
                        curPrinterInterface = null;
                        printerInterfaceArrayList.remove(printerInterface);
                        setPrintEnable(false);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

    }

    public void setPrintEnable(boolean isEnable) {
        binding.contentGenerateBill.btnConnect.setEnabled(!isEnable);
        binding.contentGenerateBill.btnDisConnect.setEnabled(isEnable);
    }

    public void isConfigPrintEnable(Object configObj) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true);
        } else {
            setPrintEnable(false);
        }
    }

    public boolean isInConnectList(Object configObj) {
        boolean isInList = false;
        for (int i = 0; i < printerInterfaceArrayList.size(); i++) {
            PrinterInterface printerInterface = printerInterfaceArrayList.get(i);
            if (configObj.toString().equals(printerInterface.getConfigObject().toString())) {
                if (printerInterface.getConnectState() == ConnectStateEnum.Connected) {
                    isInList = true;
                    break;
                }
            }
        }
        return isInList;
    }

    public void doConnect() {

        if (Integer.parseInt(binding.contentGenerateBill.tvDeviceSelected.getTag().toString()) == BaseEnum.NO_DEVICE) { // No device is selected.
            showAlertDialog(context.getString(R.string.main_pls_choose_device));
            return;
        }

        binding.contentGenerateBill.pbConnect.setVisibility(View.VISIBLE);
        TimeRecordUtils.record("Start：", System.currentTimeMillis());
        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
        connectBluetooth(bluetoothEdrConfigBean);
    }

    public void doDisConnect() {
        if (Integer.parseInt(binding.contentGenerateBill.tvDeviceSelected.getTag().toString()) == BaseEnum.NO_DEVICE) {
            return;
        }
        if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
            rtPrinter.disConnect();
        }

        binding.contentGenerateBill.tvDeviceSelected.setText(context.getString(R.string.please_connect));
        setPrintEnable(false);
    }

    public void showAlertDialog(final String msg) {
        activity.runOnUiThread(() -> {
            android.app.AlertDialog.Builder dialog =
                    new android.app.AlertDialog.Builder(context);
            dialog.setTitle(context.getString(R.string.please_connect_device));
            dialog.setMessage(msg);
            dialog.setNegativeButton(R.string.cancel, null);
            dialog.show();
        });
    }

    public void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);

        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void escPrint() throws UnsupportedEncodingException {
        binding.contentGenerateBill.buttonPrint.setEnabled(false); // to avoid multiple prints at same time if user clicks by mistake
        binding.contentGenerateBill.buttonPrint.setClickable(false);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (rtPrinter != null) {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();

                    escCmd.append(escCmd.getHeaderCmd());// Initial //btnCmds = 2......
                    escCmd.setChartsetName(mChartsetName);
                    CommonSetting commonSetting = new CommonSetting();
                    commonSetting.setAlign(ALIGN_MIDDLE);

                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                    bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);

                    Position txtposition = new Position(0, 0);
                    textSetting.setTxtPrintPosition(txtposition);
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
                    escCmd.append(escCmd.getLFCRCmd());

                    // here printing the image...
                    try {
                        if (mBitmap != null)
                            escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }

                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    if (rtPrinter.getPrinterInterface() != null) {
                        // If without selecting Bluetooth user click Print button crash happens so added this condition.
                        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
                        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
                        alertdialogBuilder.setMessage(R.string.printing);
                        alertdialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                binding.contentGenerateBill.buttonPrint.setEnabled(true);
                                binding.contentGenerateBill.buttonPrint.setClickable(true);
                                activity.finish();
                                Intent intent = new Intent(context, HomeScreenActivity_New.class);
                                context.startActivity(intent);
                            }
                        });

                        androidx.appcompat.app.AlertDialog alertDialog = alertdialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);
                    } else {
                        binding.contentGenerateBill.buttonPrint.setEnabled(true);
                        binding.contentGenerateBill.buttonPrint.setClickable(true);
                        Toast.makeText(context, context.getResources().getString
                                (R.string.tip_have_no_paired_device), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
