package org.intelehealth.app.activities.prescription.thermalprinter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.rt.printerlibrary.enumerate.SettingEnum;
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
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TextPrintESCActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, PrinterObserver {
    private static final String TAG = TextPrintESCActivity.class.getSimpleName();
    private TextView presTextview, drSignTextview, drDetailsTextview;
    private Button btnTextPrint;
    private String printStr;
    private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    private ESCFontTypeEnum curESCFontType = null;
    private Intent intent;
    private String prescData, doctorDetails, fontFamily, drSignText;
    private Bitmap mBitmap = null;
    private int bmpPrintWidth = 40;
    private TextView tvDeviceSelected;
    private Button btnConnect, btnDisConnect;
    private Object configObj;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private ProgressBar pbConnect;
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    public static PrinterInterface curPrinterInterface = null;
    private SessionManager sessionManager;
    private CheckBox ckSmallFont, ckAntiWhite, ckDoubleWidth,
            ckDoubleHeight, ckBold, ckUnderline;
    private Spinner spinEscFontType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_print_escactivity);
        sessionManager = new SessionManager(getBaseContext());
        initView();
        addListener();
        init();
    }

    @SuppressLint("WrongViewCast")
    public void initView() {
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivBack = toolbar.findViewById(R.id.iv_back_arrow_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        tvTitle.setText(getString(R.string.view_print));
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(v -> onBackPressed());
        ivIsInternet.setVisibility(View.GONE);

        presTextview = findViewById(R.id.pres_textview);
        drSignTextview = findViewById(R.id.drSign_textview);
        drDetailsTextview = findViewById(R.id.drDetails_textview);
        btnTextPrint = findViewById(R.id.btn_txtprint);
        tvDeviceSelected = findViewById(R.id.tv_device_selected);
        btnConnect = findViewById(R.id.btn_connect);
        btnDisConnect = findViewById(R.id.btn_disConnect);
        pbConnect = findViewById(R.id.pb_connect);
    }

    public void addListener() {
        btnTextPrint.setOnClickListener(this);
        tvDeviceSelected.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnDisConnect.setOnClickListener(this);
    }
    private String removeNull(String inputString) {
        // Remove all occurrences of "&null" (case-insensitive) and "null" (case-insensitive)


        return inputString
                .replaceAll("(?i)& ?\\bnull\\b", "") // Remove "&null" or "null" with optional "&"
                .trim();
    }
    public void init() {
        IntelehealthApplication.getInstance().setCurrentCmdType(BaseEnum.CMD_ESC);
        // printerFactory = new UniversalPrinterFactory();
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        PrinterObserverManager.getInstance().add(this);

        if (curPrinterInterface != null) {
            // to maintain the bluetooth pairing throughout the app.
            rtPrinter.setPrinterInterface(curPrinterInterface);
            tvDeviceSelected.setText(curPrinterInterface.getConfigObject().toString());
            tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
            printerInterfaceArrayList.add(curPrinterInterface);
            rtPrinter.setPrinterInterface(curPrinterInterface);
            setPrintEnable(true);
        }

        IntelehealthApplication.getInstance().setRtPrinter(rtPrinter);
        rtPrinter = IntelehealthApplication.getInstance().getRtPrinter();
        textSetting = new TextSetting();

        intent = this.getIntent();
        if (intent != null) {
            //   prescData = Html.fromHtml(intent.getStringExtra("sms_prescripton")).toString();
            prescData = intent.getStringExtra("sms_prescripton");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                presTextview.setText(Html.fromHtml(removeNull(prescData), Html.FROM_HTML_MODE_COMPACT));
            } else {
                presTextview.setText(Html.fromHtml(removeNull(prescData)));
            }

           /* prescData = "    - Not Provided\n" +
                    "    ";*/
            doctorDetails = Html.fromHtml(intent.getStringExtra("doctorDetails")).toString();

            if (intent.getStringExtra("font-family") != null)
                fontFamily = Html.fromHtml(intent.getStringExtra("font-family")).toString();

            if (intent.getStringExtra("drSign-text") != null)
                drSignText = Html.fromHtml(intent.getStringExtra("drSign-text")).toString();
        }
        Log.e("pres:", "prescFinall:" + intent.getStringExtra("sms_prescripton") + intent.getStringExtra("doctorDetails"));

        String fontFamilyFile = "";
        if (fontFamily != null) {
            if (fontFamily.toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "fonts/Youthness.ttf";
            } else if (fontFamily.toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "fonts/Asem.otf";
            } else if (fontFamily.toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "fonts/Arty.otf";
            } else if (fontFamily.toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "fonts/Almondita.ttf";
            }
        }

        if (fontFamily != null) {
            Typeface face = Typeface.createFromAsset(getAssets(), fontFamilyFile);
            drSignTextview.setTypeface(face);
        }

        drSignTextview.setTextSize(60f);
        drSignTextview.setIncludeFontPadding(false);
        drSignTextview.setTextColor(getResources().getColor(R.color.ink_pen));
        drSignTextview.setBackgroundColor(getResources().getColor(R.color.white));
        drSignTextview.setText(drSignText);
        drSignTextview.setDrawingCacheEnabled(true);
        drSignTextview.buildDrawingCache();
        drSignTextview.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        drSignTextview.layout(0, 0, drSignTextview.getMeasuredWidth(), drSignTextview.getMeasuredHeight());

        mBitmap = drSignTextview.getDrawingCache(); // converting Textview to Bitmap Image.

        //  pres_textview.setText(prescData);
        drDetailsTextview.setText(doctorDetails);
        Log.e("pres:", "prescFinal:" + presTextview.getText().toString() + drSignTextview.getText().toString() +
                drDetailsTextview.getText().toString());

//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.doctor_sign);
//        showImage(uri);
    }

    private void textPrint() throws UnsupportedEncodingException {
        printStr = presTextview.getText().toString();

        if (TextUtils.isEmpty(printStr)) {
            printStr = "Hello Printer";
        }

        switch (IntelehealthApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            default:
                break;
        }
    }


    private void escPrint() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (rtPrinter != null) {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();

                    escCmd.append(escCmd.getHeaderCmd());// Initial //btnCmds = 2......
                    escCmd.setChartsetName(mChartsetName);
                    CommonSetting commonSetting = new CommonSetting();

                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                    bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);

                    Position txtposition = new Position(0, 0);
                    textSetting.setTxtPrintPosition(txtposition);
                    // textSetting.setAlign(CommonEnum.ALIGN_RIGHT);
                    // commonSetting.setEscLineSpacing(getInputLineSpacing());
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            escCmd.append(escCmd.getTextCmd(textSetting, String.valueOf(Html.fromHtml(prescData, Html.FROM_HTML_MODE_COMPACT))));
                        } else {
                            presTextview.setText(Html.fromHtml(prescData));
                            escCmd.append(escCmd.getTextCmd(textSetting, String.valueOf(Html.fromHtml(prescData))));
                        }

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    try {
                        if (mBitmap != null)
                            escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }
                    escCmd.append(escCmd.getLFCRCmd());

                    //here it prints 2nd time taking the position of the cursor where the priting ended above.
                    txtposition.x = 20;
                    textSetting.setTxtPrintPosition(txtposition);
                    try {
                        escCmd.append(escCmd.getTextCmd(textSetting, doctorDetails));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getHeaderCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    Log.i(TAG, FuncUtils.ByteArrToHex(escCmd.getAppendCmds()));
                    if (rtPrinter.getPrinterInterface() != null) {
                        DialogUtils dialogUtils = new DialogUtils();
                        dialogUtils.showCommonDialog(TextPrintESCActivity.this, R.drawable.ui2_bell_icon_primary, getResources().getString(R.string.printing), getResources().getString(R.string.prescription_printing), true, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), action -> {
                        });
                     /*   // If without selecting Bluetooth user click Print button crash happens so added this condition.
                        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());

                        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(TextPrintESCActivity.this);
                        alertdialogBuilder.setMessage(R.string.printing);
                        alertdialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

                        androidx.appcompat.app.AlertDialog alertDialog = alertdialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        IntelehealthApplication.setAlertDialogCustomTheme(TextPrintESCActivity.this, alertDialog);*/
                    } else {
                        Toast.makeText(TextPrintESCActivity.this, getResources().getString
                                (R.string.tip_have_no_paired_device), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_txtprint) {
            try {
                textPrint();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
                /*case R.id.btn_select_chartsetname:
                showSelectChartsetnameDialog();
                break;*/
        } else if (id == R.id.tv_device_selected) {
            showBluetoothDeviceChooseDialog(); // Here on click, will open the Dialog that will show all the nearby Bluetooth devices...
        } else if (id == R.id.btn_connect) {
            doConnect(); //Here on clicking will connect with the selected Bluetooth device...
        } else if (id == R.id.btn_disConnect) {
            doDisConnect();
        }
    }

    private void doDisConnect() {

        if (Integer.parseInt(tvDeviceSelected.getTag().toString()) == BaseEnum.NO_DEVICE) {
            return;
        }
        if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
            rtPrinter.disConnect();
        }

        tvDeviceSelected.setText(getString(R.string.please_connect));
        tvDeviceSelected.setTag(BaseEnum.NO_DEVICE);
        //setPrintEnable(false);
        btnConnect.setEnabled(false);
        btnDisConnect.setEnabled(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isEnable) {
        if (compoundButton == ckSmallFont) {
            if (isEnable) {
                textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
                spinEscFontType.setSelection(0);
            } else {
                textSetting.setIsEscSmallCharactor(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckAntiWhite) {
            if (isEnable) {
                textSetting.setIsAntiWhite(SettingEnum.Enable);
            } else {
                textSetting.setIsAntiWhite(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckDoubleWidth) {
            if (isEnable) {
                textSetting.setDoubleWidth(SettingEnum.Enable);
            } else {
                textSetting.setDoubleWidth(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckDoubleHeight) {
            if (isEnable) {
                textSetting.setDoubleHeight(SettingEnum.Enable);
            } else {
                textSetting.setDoubleHeight(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckBold) {
            if (isEnable) {
                textSetting.setBold(SettingEnum.Enable);
            } else {
                textSetting.setBold(SettingEnum.Disable);
            }
        }
        if (compoundButton == ckUnderline) {
            if (isEnable) {
                textSetting.setUnderline(SettingEnum.Enable);
            } else {
                textSetting.setUnderline(SettingEnum.Disable);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
    }

    //This will open a Dialog that will show all the Bluetooth devices...
    private void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(
                new BluetoothDeviceChooseDialog.OnDeviceItemClickListener() {
                    @Override
                    public void onDeviceItemClick(BluetoothDevice device) {
                        if (TextUtils.isEmpty(device.getName())) {
                            tvDeviceSelected.setText(device.getAddress());
                        } else {
                            tvDeviceSelected.setText(device.getName() + " [" + device.getAddress() + "]");
                        }
                        configObj = new BluetoothEdrConfigBean(device);
                        tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
                        isConfigPrintEnable(configObj);
                    }
                });

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(TextPrintESCActivity.this,
                    getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            Toast.makeText(TextPrintESCActivity.this,
                    getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Bluetooth is Turned ON.
            bluetoothDeviceChooseDialog.show(TextPrintESCActivity.this.getSupportFragmentManager(), null);
        }

    }

    private void isConfigPrintEnable(Object configObj) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true);
        } else {
            setPrintEnable(false);
        }
    }

    private void setPrintEnable(boolean isEnable) {
        // btn_txtprint.setEnabled(isEnable);
        btnConnect.setEnabled(!isEnable);
        btnDisConnect.setEnabled(isEnable);
    }

    private boolean isInConnectList(Object configObj) {
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

    private void doConnect() {

        if (Integer.parseInt(tvDeviceSelected.getTag().toString()) == BaseEnum.NO_DEVICE) { // No device is selected.
            Toast.makeText(this, "Please select device", Toast.LENGTH_SHORT).show();
            //  showAlertDialog(getString(R.string.main_pls_choose_device));
            return;
        }

        pbConnect.setVisibility(View.VISIBLE);
        TimeRecordUtils.record("Start：", System.currentTimeMillis());
        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
        connectBluetooth(bluetoothEdrConfigBean);
    }

    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);

        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //do nothing...
        }
    }


    public void showAlertDialog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog.Builder dialog =
                        new android.app.AlertDialog.Builder(TextPrintESCActivity.this);
                dialog.setTitle("Please connect device");
                dialog.setMessage(msg);
                dialog.setNegativeButton(R.string.cancel, null);
                dialog.show();
            }
        });
    }

    @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbConnect.setVisibility(View.GONE);
                // disconnect and connect button color change.
                if (state == CommonEnum.CONNECT_STATE_SUCCESS) {
                    Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
                            + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                } else if (state == CommonEnum.CONNECT_STATE_INTERRUPTED) {
                    if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
                                        + getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TextPrintESCActivity.this, getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    }
                }


                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
//                        Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
//                        tvDeviceSelected.setText(printerInterface.getConfigObject().toString());
                        tvDeviceSelected.setTag(BaseEnum.HAS_DEVICE);
                        curPrinterInterface = printerInterface; // set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        btnDisConnect.setEnabled(true);
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
                        tvDeviceSelected.setText(R.string.please_connect);
                        tvDeviceSelected.setTag(BaseEnum.NO_DEVICE);
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}