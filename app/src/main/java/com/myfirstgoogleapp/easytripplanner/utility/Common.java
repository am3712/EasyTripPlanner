package com.myfirstgoogleapp.easytripplanner.utility;

import android.content.Context;

import com.myfirstgoogleapp.easytripplanner.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public abstract class Common {

    public static final String[] MAPS_TRIPS_COLORS = new String[]{"#E30613", "#333333", "#138184", "#BBC1C7", "#39B54A",
            "#F78A07", "#5C7FE3", "#45B7FC", "#AECB53", "#E9BF5B", "#CB6999"};

    public static boolean isValidEmail(String strPattern) {

        boolean b = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        ).matcher(strPattern).matches();
        Timber.i("strPattern : %s", strPattern);
        Timber.i("matches : %s", b);
        return !b;
    }

    public static boolean isValidPassword(String password) {
        Pattern pattern;
        Matcher matcher;
        String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

/*
    open var
    dialog:Dialog?=null

    open fun

    dismissLoadingProgress() {
        if (dialog != null && dialog !!.isShowing){
            dialog !!.dismiss()
            dialog = null
        }
    }

    open fun

    showLoadingProgress(context:Activity) {
        if (dialog != null) {
            dialog !!.dismiss()
            dialog = null
        }
        dialog = Dialog(context)
        dialog !!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog !!.window !!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog !!.setContentView(R.layout.dlg_progress)
        dialog !!.setCancelable(false)
        dialog !!.show()
    }
*/

    public static void alertErrorOrValidationDialog(Context context, String title, String msg) {
        new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> {

                }).show();
        // Create the AlertDialog object and return it

    }
}
