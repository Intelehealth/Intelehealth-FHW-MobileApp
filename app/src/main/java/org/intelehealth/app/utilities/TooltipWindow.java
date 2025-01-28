package org.intelehealth.app.utilities;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.intelehealth.app.R;

public class TooltipWindow {
    private static final int MSG_DISMISS_TOOLTIP = 100;
    private Context ctx;
    private PopupWindow tipWindow;
    private View contentView;
    private LayoutInflater inflater;
    TextView tooltipText;

    public TooltipWindow(Context ctx) {
        this.ctx = ctx;
        tipWindow = new PopupWindow(ctx);
        tipWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.tooltip_layout, null);
        tooltipText = contentView.findViewById(R.id.tooltip_text);
    }

    public void showToolTip(View anchor, String text) {
        tooltipText.setText(text);
        tipWindow.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
        tipWindow.setWidth(ActionBar.LayoutParams.WRAP_CONTENT);
        tipWindow.setOutsideTouchable(true);
        tipWindow.setTouchable(true);
        tipWindow.setFocusable(true);
        tipWindow.setContentView(contentView);
        int screen_pos[] = new int[2];
        anchor.getLocationOnScreen(screen_pos);
        Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchor.getWidth(), screen_pos[1] + anchor.getHeight());
        contentView.measure(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();
        int position_x = anchor_rect.centerX() - (contentViewWidth / 2);
        int position_y = anchor_rect.bottom - (anchor_rect.height() / 2);
        tipWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position_x,position_y);
        handler.sendEmptyMessageDelayed(MSG_DISMISS_TOOLTIP, 4000);
    }
    public boolean isTooltipShown() {
        if (tipWindow != null && tipWindow.isShowing())
            return true;
        return false;
    }
    public void dismissTooltip() {
        if (tipWindow != null && tipWindow.isShowing())
            tipWindow.dismiss();
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_DISMISS_TOOLTIP) {
                if (tipWindow != null && tipWindow.isShowing())
                    tipWindow.dismiss();
            }
        };
    };
}