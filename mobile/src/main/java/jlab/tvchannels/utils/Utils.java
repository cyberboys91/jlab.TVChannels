package jlab.tvchannels.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import jlab.tvchannels.R;
import jlab.tvchannels.model.Country;

public class Utils {
    public static final String CURRENT_FRAGMENT_ID_KEY = "CURRENT_FRAGMENT_ID_KEY";
    public static final String REFRESH_COUNT_CHANNELS_ACTION = "REFRESH_COUNT_CHANNELS_ACTION";
    public static final String IS_TV_KEY = "IS_TV_KEY";
    public static final String COUNT_CHANNELS_KEY = "COUNT_CHANNELS_KEY";
    public static final String LOAD_RADIO_CHANNELS_ACTION = "jlab.LOAD_RADIO_CHANNELS";
    public static final String LOAD_TV_CHANNELS_ACTION = "jlab.LOAD_TV_CHANNELS";
    public static final int[] colors = new int[] {R.color.amber_700,
            R.color.teal_700, R.color.purple_A700, R.color.deep_purple_700};

    public static void launchIntent(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static Snackbar createSnackBar(Context context, View viewForSnack, String message) {
        if (viewForSnack == null)
            return null;
        Snackbar result = Snackbar.make(viewForSnack, message, Snackbar.LENGTH_LONG);
        ((TextView) result.getView().findViewById(R.id.snackbar_text)).setTextColor(context
                .getResources().getColor(R.color.white));
        return result;
    }

    private static Snackbar createSnackBar(View viewForSnack, int message) {
        if (viewForSnack == null)
            return null;
        return Snackbar.make(viewForSnack, message, Snackbar.LENGTH_LONG);
    }

    public static void showSnackBar(Context context, View viewForSnack, int msg) {
        Snackbar snackbar = createSnackBar(viewForSnack, msg);
        if (snackbar != null) {
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(context
                    .getResources().getColor(R.color.white));
            snackbar.setActionTextColor(viewForSnack.getResources().getColor(R.color.colorAccent));
            snackbar.show();
        }
    }

    public static void showSnackBar(Context context, View viewForSnack, String msg) {
        Snackbar snackbar = Utils.createSnackBar(context, viewForSnack, msg);
        if (snackbar != null) {
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(context
                    .getResources().getColor(R.color.white));
            snackbar.show();
        }
    }

    public static int countChannels (ArrayList<Country> countries) {
        int result = 0;
        for (Country country : countries)
            result += country.countChannels();
        return result;
    }

    public static boolean isReproducibleWithExoplayer(String format) {
        return format.contains("m3u8");
    }
}
