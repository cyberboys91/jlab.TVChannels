package jlab.tvchannels;

import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

import jlab.tvchannels.component.ChannelList;
import jlab.tvchannels.component.SwipeLoadingLayout;
import jlab.tvchannels.controller.SharedPreferencesController;
import jlab.tvchannels.controller.VolleyController;
import jlab.tvchannels.model.Channel;
import jlab.tvchannels.model.ChannelOptions;
import jlab.tvchannels.utils.SourcesManagement;
import jlab.tvchannels.utils.Utils;
import jlab.tvchannels.utils.VideoUtils;

public class DetailChannelActivity extends AppCompatActivity {
    public static final String TAG = DetailChannelActivity.class.getSimpleName();
    public static final String EXTRA_MESSAGE = "jlab.tvchannels.CHANNEL_DETAIL";
    public static final String EXTRA_TYPE = "jlab.tvchannels.CHANNEL_TYPE";
    public static final String TYPE_TV = "TV";
    public static final String TYPE_RADIO = "RADIO";
    private Channel channel;
    private String typeOfStream, description;

    private ImageView channelImageIV;
    private TextView channelNameTV, channelDescription;
    private GridView channelSourceLV;
    private MediaPlayer mediaPlayer; //TODO This should be moved to another Dialog/Fragment
    private FloatingActionButton fbPlayOrPauseRadio;
    private MaterialCardView tvLastPlaying;
    private SwipeLoadingLayout sllLoading;
    private int indexLastPlaying = -1;
    private ChannelList channelList;
    private View btChannelURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Bundle intentExtras = savedInstanceState != null
                ? savedInstanceState
                : getIntent().getExtras();
        if (intentExtras != null) {
            channelList = (ChannelList) intentExtras.getSerializable(EXTRA_MESSAGE);
            if (channelList != null) {
                channel = channelList.getChannel();
                description = String.format("%s - %s", channelList.getCountryName(), channelList.getCommunityName());
            }
            typeOfStream = intentExtras.getString(EXTRA_TYPE);
        }

        mediaPlayer = new MediaPlayer();
        SharedPreferencesController.init(this);
        setUpElements();
        setUpListeners();
        loadChannel();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TYPE, typeOfStream);
        outState.putSerializable(EXTRA_MESSAGE, channelList);
    }

    private void setUpElements() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (channelList != null)
            toolbar.setTitle(channelList.getChannel().getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        channelImageIV = findViewById(R.id.channel_image_detail_tv);
        channelNameTV = findViewById(R.id.channel_name_detail_tv);
        channelDescription = findViewById(R.id.tvChannelDescription);
        btChannelURL = findViewById(R.id.ivDetails);
        channelSourceLV = findViewById(R.id.channel_source_detail_lv);
        sllLoading = findViewById(R.id.sllLoading);
        fbPlayOrPauseRadio = findViewById(R.id.fbPlayOrPauseRadio);
    }

    private void setUpListeners() {
        btChannelURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPopupWindow(v);
            }
        });
        fbPlayOrPauseRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null)
                    return;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    fbPlayOrPauseRadio.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mediaPlayer.start();
                    fbPlayOrPauseRadio.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
    }

    protected SpannableStringBuilder getSpannableFromText(String text1, String text2, int color) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(
                String.format("%s (%s)", text1, text2));
        int indexSep = text1.length() + 2;
        ForegroundColorSpan colorSpan = new ForegroundColorSpan
                (getResources().getColor(color));
        strBuilder.setSpan(colorSpan, indexSep, indexSep + text2.length(), 0);
        Selection.selectAll(strBuilder);
        return strBuilder;
    }

    private void displayPopupWindow(View anchorView) {
        LayoutInflater inflater = LayoutInflater.from(anchorView.getContext());
        PopupWindow popup = new PopupWindow(anchorView.getContext());
        View layout = inflater.inflate(R.layout.popup_info_website, null);

        TextView tvWebsites = layout.findViewById(R.id.tvWebsites);
        tvWebsites.setText(channel.getWeb());
        popup.setContentView(layout);
        popup.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.showAsDropDown(anchorView);
    }

    private void loadChannel() {
        channelNameTV.setText(channel.getName());
        channelDescription.setText(description);
        // Load first option
        if (!channel.getOptions().isEmpty()) {
            // Fill available options
            final ArrayList<String> urls = new ArrayList<>();
            final ArrayList<String> formats = new ArrayList<>();
            final ArrayList<SpannableStringBuilder> options = new ArrayList<>();
            String opt = getString(R.string.option);
            int index = 0;
            for (ChannelOptions channelOption : channel.getOptions()) {
                urls.add(channelOption.getUrl());
                formats.add(channelOption.getFormat());
                options.add(getSpannableFromText
                        (String.format("%s %s", opt, index + 1), channelOption.getFormat(),
                                Utils.colors[index++ % Utils.colors.length]));
            }

            ArrayAdapter<SpannableStringBuilder> sourcesAdapter = new ArrayAdapter<>(this,
                    R.layout.item_list_detail_channel, android.R.id.text1, options);
            channelSourceLV.setAdapter(sourcesAdapter);
            channelSourceLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String source = urls.get(position);
                    ((MaterialCardView) view.findViewById(R.id.btOption))
                            .setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(
                            getResources().getColor(R.color.white));
                    switch (typeOfStream) {
                        case TYPE_TV:
                            if (Utils.isReproducibleWithExoplayer(formats.get(position)))
                                loadVideo(source);
                            else if (isReproducibleWithYoutube(source))
                                VideoUtils.watchYoutubeVideo(getApplicationContext(), source);
                            else
                                VideoUtils.watchUnknownVideo(getApplicationContext(), source);
                            break;
                        case TYPE_RADIO:
                            loadRadio(source);
                            break;
                    }

                    if (tvLastPlaying != null && indexLastPlaying != position) {
                        tvLastPlaying.setCardBackgroundColor(getResources()
                                .getColor(R.color.deep_purple_A100));
                        ((TextView) view.findViewById(android.R.id.text1)).setTextColor(
                                getResources().getColor(R.color.white));
                    }
                    tvLastPlaying = view.findViewById(R.id.btOption);
                    indexLastPlaying = position;
                }
            });
        }

        ImageRequest request = new ImageRequest(channel.getLogo(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        channelImageIV.setImageBitmap(bitmap);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        channelImageIV.setImageResource(R.drawable.icon);
                    }
                });
        VolleyController.getInstance(this).addToQueue(request);
    }

    private boolean isReproducibleWithYoutube(String url) {
        return url.contains("youtube") || url.contains("youtu.be");
    }

    private void loadVideo(String streamURL) {
        Utils.showSnackBar(this, fbPlayOrPauseRadio, R.string.channel_detail_reproducing_tv);
        DialogFragment newFragment = VideoDialogFragment.newInstance(streamURL);
        newFragment.show(getSupportFragmentManager(), "VideoDialog");
    }

    private void loadRadio(final String streamURL) {
        Utils.showSnackBar(this, fbPlayOrPauseRadio, R.string.channel_detail_reproducing_radio);
        sllLoading.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initMediaPlayer(streamURL);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    sllLoading.hide();
                    showErrorLoadingRadio();
                }
            }
        }).start();
    }

    private void initMediaPlayer(String streamURL) throws IOException {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(getBaseContext(), Uri.parse(streamURL));
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mediaPlayer != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mediaPlayer.start();
                            sllLoading.hide();
                            if (mediaPlayer.isPlaying()) {
                                showOrHidePlayOrPauseButton(true, true);
                            }
                        }
                    });
                }
            }
        });
        mediaPlayer.setWakeMode(getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                showErrorLoadingRadio();
                return true;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                showOrHidePlayOrPauseButton(false, false);
            }
        });
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepareAsync();
    }

    private void showOrHidePlayOrPauseButton(boolean play, boolean show) {
        fbPlayOrPauseRadio.setImageResource(play
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);
        fbPlayOrPauseRadio.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showErrorLoadingRadio() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sllLoading.hide();
                Utils.showSnackBar(getBaseContext(), fbPlayOrPauseRadio, R.string.error_loading_radio);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            showOrHidePlayOrPauseButton(false, false);
            mediaPlayer.reset();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_detail_channel, menu);

        boolean isItemFavorite = false;
        if(typeOfStream != null) {
            if (typeOfStream.equals(TYPE_TV)) {
                isItemFavorite = SourcesManagement.isTVChannelFavorite(channel.getName());
            } else {
                isItemFavorite = SourcesManagement.isRadioChannelFavorite(channel.getName());
            }
        }

        if (isItemFavorite) {
            menu.getItem(0).setIcon(R.drawable.heart);
        } else {
            menu.getItem(0).setIcon(R.drawable.heart_outline);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favorites) {
            boolean isItemFavorite;
            if (typeOfStream.equals(TYPE_TV)) {
                isItemFavorite = SourcesManagement.isTVChannelFavorite(channel.getName());
                SourcesManagement.setTVChannelFavorite(channel.getName(), !isItemFavorite);
            } else {
                isItemFavorite = SourcesManagement.isRadioChannelFavorite(channel.getName());
                SourcesManagement.setRadioChannelFavorite(channel.getName(), !isItemFavorite);
            }
            if (isItemFavorite) {
                item.setIcon(R.drawable.heart_outline);
            } else {
                item.setIcon(R.drawable.heart);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
