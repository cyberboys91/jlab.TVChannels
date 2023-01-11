package jlab.tvchannels;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    private View rootView;
    private TextView appVersionTV;
    private LinearLayout shareLL;
    private LinearLayout websiteLL;
    private LinearLayout telegramLL;
    private LinearLayout repositoryLL;
    private LinearLayout sourceRepositoryLL;
    private LinearLayout buyMeACoffeeLL;
    private TextView tvRadioReportTV;
    private TextView appReportTV;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        setUpElements();
        setUpListeners();

        setSocialMediaLinks();
        setReportLinks();
        setVersion();

        return rootView;
    }

    private void setSocialMediaLinks() {
        shareLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = "TDTChannels\n\n" +
                        "Lista de canales de televisión, y radio, que se emiten en abierto por Internet. " +
                        "Especialmente enfocado a España, y ampliando a canales Internacionales. Gratuito y sin publicidad.\n\n" +
                        "https://github.com/LaQuay/TDTChannels";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "TDT Channels");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.about_share)));
            }
        });

        websiteLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tdtchannels.com"));
                startActivity(intent);
            }
        });

        telegramLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=tdtchannels"));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/tdtchannels"));
                    startActivity(intent);
                }
            }
        });

        repositoryLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LaQuay/TDTChannels"));
                startActivity(intent);
            }
        });

        sourceRepositoryLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LaQuay/TDTChannels-APP"));
                startActivity(intent);
            }
        });

        buyMeACoffeeLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/mLaQuay"));
                startActivity(intent);
            }
        });
    }

    private void setReportLinks() {
        tvRadioReportTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LaQuay/TDTChannels/issues"));
                startActivity(intent);
            }
        });

        appReportTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LaQuay/TDTChannels-APP/issues"));
                startActivity(intent);
            }
        });
    }

    private void setVersion() {
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);

            appVersionTV.setText(getString(R.string.app_name) + " - " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setUpElements() {
        shareLL = rootView.findViewById(R.id.fragment_settings_share_ll);
        websiteLL = rootView.findViewById(R.id.fragment_settings_website_ll);
        telegramLL = rootView.findViewById(R.id.fragment_settings_telegram_ll);
        repositoryLL = rootView.findViewById(R.id.fragment_settings_main_repo_ll);
        sourceRepositoryLL = rootView.findViewById(R.id.fragment_settings_main_repo_source_ll);
        buyMeACoffeeLL = rootView.findViewById(R.id.fragment_settings_buy_coffee_ll);
        tvRadioReportTV = rootView.findViewById(R.id.fragment_settings_tv_radio_report_tv);
        appReportTV = rootView.findViewById(R.id.fragment_settings_app_report_tv);
        appVersionTV = rootView.findViewById(R.id.fragment_about_version_name_tv);
    }

    private void setUpListeners() {

    }
}
