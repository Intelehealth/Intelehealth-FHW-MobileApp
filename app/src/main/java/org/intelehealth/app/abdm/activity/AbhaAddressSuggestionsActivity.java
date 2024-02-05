package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.chip.Chip;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.EnrollSuggestionRequestBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionResponse;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAadharMobileVerificationBinding;
import org.intelehealth.app.databinding.ActivityAbhaAddressSuggestionsBinding;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.WindowsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AbhaAddressSuggestionsActivity extends AppCompatActivity {
    private Context context = AbhaAddressSuggestionsActivity.this;
    public static final String TAG = AbhaAddressSuggestionsActivity.class.getSimpleName();
    ActivityAbhaAddressSuggestionsBinding binding;
    private String txnID, accessToken;
    private List<String> phrAddressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbhaAddressSuggestionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AbhaAddressSuggestionsActivity.this);  // changing status bar color

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("accessToken");
        OTPVerificationResponse response = (OTPVerificationResponse) intent.getSerializableExtra("payload");
        txnID = response.getTxnId();     // auto-generated address from abdm end.
        phrAddressList = response.getABHAProfile().getPhrAddress();     // auto-generated abha preferred address from abdm end.
        Log.d(TAG, "phrAddress: " + phrAddressList.toString());

       /* createDynamicChips("prajwalw@sbx");
        createDynamicChips("prajuuu@sbx");
        createDynamicChips("aparna@sbx");
        createDynamicChips("kavita@sbx");
        createDynamicChips("hello@sbx");*/  // todo: this is just for testing.

        if (binding.chipGrp.getChildCount() > 0) {
            for (int i = 0; i < binding.chipGrp.getChildCount(); i++) {
                int finalI = i;
                binding.chipGrp.getChildAt(i).setOnClickListener(v -> {
                    Chip chip = binding.chipGrp.findViewById(binding.chipGrp.getChildAt(finalI).getId());
                    chip.setChecked(true);
                    Log.d(TAG, "ischecked: " + chip.getText().toString());

                    //here you can call your method to load Images


                });
            }
        }

        // api - start
        String url = UrlModifiers.getEnrollABHASuggestionUrl();
        EnrollSuggestionRequestBody body = new EnrollSuggestionRequestBody();
        body.setTxnId(txnID);

        Single<EnrollSuggestionResponse> enrollSuggestionResponseSingle =
                AppConstants.apiInterface.PUSH_ENROLL_ABHA_ADDRESS_SUGGESTION(url, accessToken, body);

        new Thread(new Runnable() {
            @Override
            public void run() {
                enrollSuggestionResponseSingle
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<EnrollSuggestionResponse>() {
                            @Override
                            public void onSuccess(EnrollSuggestionResponse enrollSuggestionResponse) {
                                Log.d(TAG, "onSuccess: suggestion: " + enrollSuggestionResponse);
                                if (enrollSuggestionResponse.getAbhaAddressList() != null) {
                                    for (String phrAddress : enrollSuggestionResponse.getAbhaAddressList()) {
                                        createDynamicChips(phrAddress);
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: suggestion" + e.toString());
                            }
                        });
            }
        }).start();
        // api - end

    }

    private void createDynamicChips(String chipTitle) {
        Chip chip = new Chip(context);
        chip.setId(ViewCompat.generateViewId());
        chip.setText(chipTitle);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(R.color.colorPrimary);
       // chip.setChipStrokeColorResource(R.color.colorPrimaryDark);
        chip.setTextColor(getColor(R.color.white));
        chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Chip);
        chip.isCloseIconVisible();
        binding.chipGrp.addView(chip);
    }
}