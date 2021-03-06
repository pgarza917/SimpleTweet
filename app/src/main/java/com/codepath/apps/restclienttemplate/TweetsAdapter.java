package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

// Second step is to extend the RecyclerView adapter but parameterize it with the ViewHolder we
// defined in the first step
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    private static final String TAG = "TweetsAdapter";
    // Third step is assisted by Android as the method headers required by extending in 2nd step are
    // generated and you just need to fill them out and create a constructor for the adapter
    Context context;
    List<Tweet> tweets;
    TwitterClient client;

    // Pass context and list of tweets into adapter
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        this.client = TwitterApp.getRestClient(context);
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        // This ViewHolder is the one we just defined below in the first step
        return new ViewHolder(view);
    }

    // Bind values based on the position of the elements
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with passed view holder. Made cleaner by having method in View Holder
        // class take care of this binding
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clear all items in the Recycler View
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            // Handling different relative time differences (just now, minutes, hours, days)
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "1m";
            } else if (diff < 60 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 120 * MINUTE_MILLIS) {
                return "1h";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "1d";
            } else {
                return diff / DAY_MILLIS + "d";
            }

        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }

    // Method to turn large integer counts into shorter format, e.g. 50,125 --> 50.1k
    public String getFormattedCount(long count) {
        String formattedCount = "";
        String quantifier = "";
        int powerOfTen = 1;
        if(count < 10000) {
            formattedCount = String.format("%d", count);
        } else if(count < 1000000) {
            powerOfTen = 1000;
            quantifier = "K";
        } else if (count < 10000000) {
            powerOfTen = 1000000;
            quantifier = "M";
        }

        long remainder = count % powerOfTen;
        String remainderDigit = Long.toString(remainder).substring(0, 1);
        if(remainderDigit.equals("0")) {
            formattedCount = String.format("%d%s", count / powerOfTen, quantifier);
        } else {
            formattedCount = String.format("%d.%s%s", count / powerOfTen, remainderDigit, quantifier);
        }

        return formattedCount;
    }

    // Define a View Holder (starting point)
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageButton ibLike;
        ImageView ivProfileImage;
        ImageView ivMedia;
        TextView tvName;
        TextView tvScreenName;
        TextView tvBody;
        TextView tvRelativeTimestamp;
        TextView tvLikes;

        // The itemView passed in is the representation of one row in the RecyclerView, i.e. a Tweet,
        // and, thus, contains all the components we defined in one row (item_tweet.xml)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Get references to all the components in the passed ItemView
            ibLike = itemView.findViewById(R.id.ibLike);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            // Set a click listener on the like button
            ibLike.setOnClickListener(this);
        }

        // Bind data stored in our Tweet model into the itemView of the view holder
        public void bind(Tweet tweet) {
            tvRelativeTimestamp.setText(getRelativeTimeAgo(tweet.createdAt));
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            String likeCount = getFormattedCount(tweet.likeCount);
            tvLikes.setText(likeCount);
            // Insert an '@' before screen name
            String fullScreenName = String.format("@%s", tweet.user.screenName);
            tvScreenName.setText(fullScreenName);

            Glide.with(context)
                    .load(tweet.user.publicImageUrl)
                    .circleCrop()
                    .into(ivProfileImage);
            // Making sure there is embedded media to display
            if(!tweet.mediaUrl.isEmpty()) {
                ivMedia.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.mediaUrl)
                        .transform(new MultiTransformation(new CenterCrop(), new RoundedCornersTransformation(10, 0)))
                        .into(ivMedia);
            }
            else {
                ivMedia.setVisibility(View.GONE);
            }

            // Change the color of the like button and like count based on whether the tweet
            // has been "liked" by the user
            if(tweet.liked) {
                ibLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
                tvLikes.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
            else {
                ibLike.setColorFilter(ContextCompat.getColor(context, R.color.grey));
                tvLikes.setTextColor(ContextCompat.getColor(context, R.color.grey));
            }
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.ibLike) {
                final int position = getAdapterPosition();
                Tweet tweet = tweets.get(position);
                JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to unlike tweet");
                        try {
                            Tweet updatedTweet = Tweet.fromJson(json.jsonObject);
                            tweets.remove(position);
                            tweets.add(position, updatedTweet);
                            notifyItemChanged(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure: " + response, throwable);
                    }
                };
                if(tweet.liked) {
                    client.unlikeTweet(tweet.id, handler);
                } else {
                    client.likeTweet(tweet.id, handler);
                }
            }
        }
    }

}
