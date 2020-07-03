package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity=User.class, parentColumns = "id", childColumns = "userId"))
public class Tweet {

    @ColumnInfo
    @PrimaryKey
    public Long id;

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String createdAt;

    @ColumnInfo
    public long userId;

    @Ignore
    public User user;

    // For now, we will only be displaying the first image in media
    @ColumnInfo
    public String mediaUrl;

    @ColumnInfo
    public boolean liked;

    // Empty constructor needed by Parceler Library
    public Tweet() {}

    // Create a Tweet object from info we get from retrieved JSON object
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.id = jsonObject.getLong("id");
        tweet.mediaUrl = "";
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        tweet.liked = jsonObject.getBoolean("favorited");

        // Checking to see if there is any attached media and confirming that
        // it is a photo
        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities.has("media")) {
            JSONObject media = entities.getJSONArray("media").getJSONObject(0);
            if (media.getString("type").equals("photo")) {
                tweet.mediaUrl = media.getString("media_url_https");
            }
        }

        return tweet;
    }

    // Will take an entire json array an construct a list of Tweet objects from it
    // as it is what we will need in our UI
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();

        // Uses fromJson method to create each individual Tweet object from each Json object
        // in the passed json array. Adds all of them to the previously-declared List (tweets)
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }

        return tweets;
    }

}
