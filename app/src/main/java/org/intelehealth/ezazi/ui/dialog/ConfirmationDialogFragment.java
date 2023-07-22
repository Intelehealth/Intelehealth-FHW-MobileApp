package org.intelehealth.ezazi.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.intelehealth.ezazi.databinding.DialogConfirmationViewBinding;
import org.intelehealth.ezazi.ui.dialog.model.DialogArg;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ConfirmationDialogFragment extends BaseDialogFragment<String> implements View.OnClickListener {
    private OnConfirmationActionListener listener;

    public interface OnConfirmationActionListener {
        void onAccept();

        default void onDecline() {
        }
    }

    public void setListener(OnConfirmationActionListener listener) {
        this.listener = listener;
    }

    @Override
    View getContentView() {
        DialogConfirmationViewBinding binding = DialogConfirmationViewBinding.inflate(getLayoutInflater(), null, false);
        binding.setContent(args.getContent());
        binding.setHeading(args.getTitle());
        return binding.getRoot();
    }

    @Override
    boolean hasTitle() {
        return false;
    }

    @Override
    public void onSubmit() {
        if (listener != null) listener.onAccept();
    }

    @Override
    public void onDismiss() {
        if (listener != null) listener.onDecline();
    }

    public static final class Builder extends BaseBuilder<String, ConfirmationDialogFragment> {

        private OnConfirmationActionListener listener;

        public Builder(Context context) {
            super(context);
        }

        public Builder listener(OnConfirmationActionListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public ConfirmationDialogFragment build() {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            fragment.setArguments(bundle());
            fragment.setListener(listener);
            return fragment;
        }
    }
}