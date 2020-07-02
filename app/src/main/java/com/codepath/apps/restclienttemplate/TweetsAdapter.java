package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

// Second step is to extend the RecyclerView adapter but parameterize it with the ViewHolder we
// defined in the first step
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    private static final String TAG = "TweetsAdapter";
    // Third step is assisted by Android as the method headers required by extending in 2nd step are
    // generated and you just need to fill them out and create a constructor for the adapter
    Context context;
    List<Tweet> tweets;

    // Pass context and list of tweets into adapter
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
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

    // Define a View Holder (starting point)
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        ImageView ivMedia;
        TextView tvName;
        TextView tvScreenName;
        TextView tvBody;
        TextView tvRelativeTimestamp;

        // The itemView passed in is the representation of one row in the RecyclerView, i.e. a Tweet,
        // and, thus, contains all the components we defined in one row (item_tweet.xml)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Get references to all the components in the passed ItemView
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
        }

        // Bind data stored in our Tweet model into the itemView of the view holder
        public void bind(Tweet tweet) {
            tvRelativeTimestamp.setText(getRelativeTimeAgo(tweet.createdAt));
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
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
                        .transform(new RoundedCornersTransformation(30, 10))
                        .override(150, 150)
                        .centerCrop()
                        .into(ivMedia);
            }
            else {
                ivMedia.setVisibility(View.GONE);
            }
        }
    }

}
