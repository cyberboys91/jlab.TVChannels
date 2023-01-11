package jlab.tvchannels.controller;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.util.ArrayList;
import java.util.Scanner;

import jlab.tvchannels.MainActivity;
import jlab.tvchannels.model.Channel;
import jlab.tvchannels.model.ChannelOptions;
import jlab.tvchannels.model.Community;
import jlab.tvchannels.model.Country;
import jlab.tvchannels.utils.APIUtils;
import jlab.tvchannels.utils.Utils;

public class APIController {
    public static final String TAG = APIController.class.getSimpleName();
    private static APIController instance;
    private ArrayList<Country> televisionChannels;
    private ArrayList<Country> radioChannels;

    private APIController() {
    }

    public static APIController getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    private synchronized static void createInstance() {
        if (instance == null) {
            instance = new APIController();
        }
    }

    public void loadChannels(TypeOfRequest typeOfRequest, boolean forceUpdate, final Context context, final ResponseServerCallback responseServerCallback) {
        MainActivity.setLoading(true);
        if (typeOfRequest.equals(TypeOfRequest.TV)) {
            if (!forceUpdate && televisionChannels != null && !televisionChannels.isEmpty()) {
                Log.i(TAG, "Load tv channels from cache");
                responseServerCallback.onChannelsLoadServer(televisionChannels);
            } else {
                Log.i(TAG, "Load tv channels from server: " + APIUtils.TV_URL);
                televisionChannels = new ArrayList<>();
                downloadChannels(APIUtils.TV_URL, televisionChannels, context,
                        responseServerCallback, false);
            }
        } else {
            if (typeOfRequest.equals(TypeOfRequest.RADIO)) {
                if (!forceUpdate && radioChannels != null && !radioChannels.isEmpty()) {
                    Log.i(TAG, "Load radio channels from cache");
                    responseServerCallback.onChannelsLoadServer(radioChannels);
                } else {
                    Log.i(TAG, "Load radio channels from server: " + APIUtils.RADIO_URL);
                    radioChannels = new ArrayList<>();
                    downloadChannels(APIUtils.RADIO_URL, radioChannels, context,
                            responseServerCallback, true);
                }
            }
        }
    }

    private String getJsonString (String fileName, final Context context) throws IOException {
        InputStreamReader inputStream = new InputStreamReader(context.getAssets().open(fileName));
        CharArrayWriter result = new CharArrayWriter();
        char[] buffer = new char[102400];
        for (int length; (length = inputStream.read(buffer)) != -1; )
            result.write(buffer, 0, length);
        return result.toString();
    }

    private void downloadChannels(final String fileName, final ArrayList<Country> channelsToMatch,
                                  final Context context, final ResponseServerCallback responseServerCallback,
                                  final boolean isRadio) {
        try {
            JSONArray countriesJsonArray = new JSONArray(getJsonString(fileName, context));
            for (int i = 0; i < countriesJsonArray.length(); ++i) {
                int countChannelsForCountry = 0;
                JSONObject country = countriesJsonArray.getJSONObject(i);

                String countryName = country.getString("name");
                JSONArray communitiesArray = country.getJSONArray("ambits");
                ArrayList<Community> communities = new ArrayList<>();

                for (int j = 0; j < communitiesArray.length(); ++j) {
                    JSONObject communityJson = communitiesArray.getJSONObject(j);

                    String communityName = communityJson.getString("name");
                    JSONArray channelsArray = communityJson.getJSONArray("channels");
                    ArrayList<Channel> channels = new ArrayList<>();

                    for (int k = 0; k < channelsArray.length(); ++k) {
                        JSONObject channelJson = channelsArray.getJSONObject(k);

                        String channelName = channelJson.getString("name");
                        String channelWeb = channelJson.getString("web");
                        String channelLogo = channelJson.getString("logo");
                        String channelEPG = channelJson.getString("epg_id");
                        JSONArray channelOptionsJson = channelJson.getJSONArray("options");
                        String channelExtraInfo = channelJson.getString("extra_info");

                        ArrayList<ChannelOptions> channelOptions = new ArrayList<>();
                        boolean add = false;
                        for (int z = 0; z < channelOptionsJson.length(); ++z) {
                            JSONObject optionJson = channelOptionsJson.getJSONObject(z);

                            String optionFormat = optionJson.getString("format");
                            String optionURL = optionJson.getString("url");

                            if (isRadio || Utils.isReproducibleWithExoplayer(optionFormat)) {
                                add = true;
                                channelOptions.add(new ChannelOptions(optionFormat, optionURL));
                            }
                        }
                        if (add) {
                            Channel channel = new Channel(channelName, channelWeb, channelLogo,
                                    channelEPG, channelOptions, channelExtraInfo);
                            countChannelsForCountry++;
                            //Log.i(TAG, "Adding channel: " + channel.toString());
                            channels.add(channel);
                        }
                    }
                    communities.add(new Community(communityName, channels));
                }
                channelsToMatch.add(new Country(countryName, communities,
                        countChannelsForCountry));
            }
            responseServerCallback.onChannelsLoadServer(channelsToMatch);
        } catch (JSONException|IOException e) {
            Log.e(TAG, "ERROR Parsing JSON");
            e.printStackTrace();
            responseServerCallback.error();
        }
    }

    public enum TypeOfRequest {
        TV,
        RADIO
    }

    public interface ResponseServerCallback {
        void onChannelsLoadServer(ArrayList<Country> countries);
        void error();
    }
}
