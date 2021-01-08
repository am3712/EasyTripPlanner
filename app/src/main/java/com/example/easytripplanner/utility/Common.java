package com.example.easytripplanner.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.easytripplanner.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Common {

    public static boolean isValidEmail(String strPattern) {

        boolean b = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        ).matcher(strPattern).matches();
        Log.i("Common", "strPattern : " + strPattern);
        Log.i("Common", "matches : " + b);
        return b;
    }

    public static boolean isValidPassword(String password) {
        Pattern pattern;
        Matcher matcher;
        String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isCheckNetwork(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
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
