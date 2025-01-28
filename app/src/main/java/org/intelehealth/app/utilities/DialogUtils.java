package org.intelehealth.app.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.adapter.ImagePickerListAdapter;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.ayu.visit.reason.adapter.SelectedChipsPreviewGridAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogUtils {
    public interface CustomDialogListener {
        public static final int POSITIVE_CLICK = 0;
        public static final int NEGATIVE_CLICK = 1;
        public static final int CANCELLED = 2;

        public void onDialogActionDone(int action);
    }

    public interface ImagePickerDialogListener {
        public static final int CAMERA = 0;
        public static final int GALLERY = 1;
        public static final int CANCELLED = 2;

        public void onActionDone(int action);
    }

    public void showOkDialog(Context context, String title, String message, String ok) {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);

        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
        //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public void showerrorDialog(Context context, String title, String message, String ok) {
        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
        //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void patientRegistrationDialog(Context context, Drawable drawable, String title,
                                                 String subTitle, String positiveBtnTxt, String negativeBtnTxt,
                                                 CustomDialogListener customDialogListener) {

        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);

        icon.setImageDrawable(drawable);
       /* dialog_title.setText("Close patient registration?");
        dialog_subtitle.setText("Are you sure you want to close the patient registration?");
        positive_btn.setText("No");
        negative_btn.setText("Yes");*/

        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        positive_btn.setText(positiveBtnTxt);
        negative_btn.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        alertDialog.show();
    }

    public void showCommonDialog(Context context, int iconResource, String title, String message, boolean isSingleButton, String positiveBtnText, String negativeBtnText, CustomDialogListener customDialogListener) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_common_message, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);

        if (iconResource == 0) icon.setVisibility(View.GONE);
        if (message == null || message.equalsIgnoreCase(""))
            dialog_subtitle.setVisibility(View.GONE);
        icon.setImageResource(iconResource);
        dialog_title.setText(title);
        dialog_subtitle.setText(message);
        positive_btn.setText(positiveBtnText);
        negative_btn.setText(negativeBtnText);

        if (isSingleButton) {
            negative_btn.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        alertDialog.show();
    }

    public void showCommonDialogWithChipsGrid(Context context, ArrayList<ReasonData> selectedData, int iconResource, String title, String message, boolean isSingleButton, String positiveBtnText, String negativeBtnText, CustomDialogListener customDialogListener) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_common_message_with_chips_grid, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);
        RecyclerView recyclerView = convertView.findViewById(R.id.rcv_selected_container);

        icon.setImageResource(iconResource);
        dialog_title.setText(title);
        dialog_subtitle.setText(message);
        positive_btn.setText(positiveBtnText);
        negative_btn.setText(negativeBtnText);

        if (isSingleButton) {
            negative_btn.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);

        recyclerView.setLayoutManager(layoutManager);
        SelectedChipsPreviewGridAdapter selectedChipsPreviewGridAdapter = new SelectedChipsPreviewGridAdapter(recyclerView, context, selectedData, null);
        recyclerView.setAdapter(selectedChipsPreviewGridAdapter);

        alertDialog.show();
    }

    public MaterialAlertDialogBuilder showErrorDialogWithTryAgainButton(Context context, Drawable drawable, String title, String message, String buttonText) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_error_and_button, null);
        builder.setView(dialogView);

        ImageView dialogIcon = dialogView.findViewById(R.id.dialog_icon);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        TextView tvSubtitle = dialogView.findViewById(R.id.dialog_subtitle);
        Button tryAgainButton = dialogView.findViewById(R.id.positive_btn);

        dialogIcon.setImageDrawable(drawable);
        tvTitle.setText(title);
        tvSubtitle.setText(message);
        tryAgainButton.setText(buttonText);

        return builder;
    }

    public AlertDialog showCommonLoadingDialog(Context context, String title, String message) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_common_loading_message, null);
        alertdialogBuilder.setView(convertView);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        dialog_title.setText(title);
        dialog_subtitle.setText(message);
        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        alertDialog.show();
        return alertDialog;
    }

    public static AlertDialog showCommonImagePickerDialog(Context context, String title, ImagePickerDialogListener imagePickerDialogListener) {
        List<String> displaySelection = new ArrayList<>();
        String[] options = {context.getString(R.string.take_photo), context.getString(R.string.choose_from_gallery), context.getString(R.string.cancel)};
        displaySelection = Arrays.asList(options);
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View convertView = View.inflate(context, R.layout.image_picker_dialog, null);
        alertDialogBuilder.setView(convertView);

        RecyclerView recyclerView = convertView.findViewById(R.id.rcvDialogImagePicker);
        TextView titleView = convertView.findViewById(R.id.tvTitleDialogImagePicker);
        titleView.setText(title);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        ImagePickerListAdapter dialogListAdapter = new ImagePickerListAdapter(recyclerView, context, displaySelection, new ImagePickerDialogListener() {
            @Override
            public void onActionDone(int action) {
                imagePickerDialogListener.onActionDone(action);
            }
        });
        recyclerView.setAdapter(dialogListAdapter);
        AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_menu_background);
        return alertDialog;
    }

    public void triggerEndAppointmentConfirmationDialog(Context activityContext, CustomDialogListener customDialogListener) {
        showCommonDialog(
                activityContext,
                R.drawable.dialog_close_visit_icon,
                activityContext.getResources().getString(R.string.confirm_cancel_appointment),
                activityContext.getResources().getString(R.string.confirm_cancel_appointment_message),
                false,
                activityContext.getResources().getString(R.string.confirm),
                activityContext.getResources().getString(R.string.cancel),
                customDialogListener
        );
    }
    public static void showPrintingDialog(Context context) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setMessage(R.string.printing)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                })
                .setCancelable(false)
                .create(); // Create the dialog but don't show it yet

        dialog.setOnShowListener(d -> {
            AlertDialog alertDialog = (AlertDialog) d;
            // Setting the color of the positive button
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    context.getResources().getColor(R.color.colorPrimary, context.getTheme())
            );
            // Customizing the dialog theme
            IntelehealthApplication.setAlertDialogCustomTheme((Activity) context, alertDialog);
        });

        dialog.show(); // Now show the dialog
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show();
    }
}
