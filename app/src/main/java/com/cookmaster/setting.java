package com.cookmaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class setting {
    public static void changeLanguage(Activity activity, Context context, SharedPreferences sharedPreferences) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Choose Language");
        dialog.setSingleChoiceItems(new String[]{"English", "Hindi"}, -1, (dialogInterface, i) -> {
            if (i == 1) {
                setLocale("hi", context);
                sharedPreferences.edit().putString("appLanguage", "hi").apply();
            } else {
                setLocale("en", context);
                sharedPreferences.edit().putString("appLanguage", "en").apply();
            }
            context.startActivity(new Intent(context, launcherActivity.class));
            activity.finish();
        });
        dialog.create();
        dialog.show();
    }
    public static void setLocale(String language, Context context){
        Locale locale=new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration=new Configuration();
        configuration.locale=locale;
        context.getResources().updateConfiguration(configuration,context.getResources().getDisplayMetrics());
    }
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
        return 0;
    }
    public static String getTime(String format){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            return DateTimeFormatter.ofPattern(format).format(LocalDateTime.now());
        return "#";
    }
    static ArrayList<String> stringToStringArray(String string){
        ArrayList<String> array=new ArrayList<>(1);
        int i=0;
        while (i<string.length()) {
            StringBuilder str= new StringBuilder();
            while (++i<string.length() && string.charAt(i)!= '#'){
                str.append(string.charAt(i));
            }
            array.add(String.valueOf(str));
        }
        return array;
    }
    public static void successDialog(Activity activity,Context context,String msg){
        Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.success_dialog_layout);
        dialog.setCanceledOnTouchOutside(false);
        TextView title=dialog.findViewById(R.id.title_successDialog);
        title.setText(msg);
        dialog.setOnCancelListener(dialogInterface -> activity.finish());
        dialog.findViewById(R.id.gotItBtn_successDialog).setOnClickListener(view -> dialog.cancel());
        dialog.show();
    }
    public static void thankYouDialog(Context context, String msg){
        Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.thank_you_layout);
        dialog.setCanceledOnTouchOutside(false);
        TextView msgTV=dialog.findViewById(R.id.msg);
        msgTV.setText(msg);
        dialog.findViewById(R.id.closeBtn).setOnClickListener(view -> dialog.cancel());
        dialog.show();
    }
}
