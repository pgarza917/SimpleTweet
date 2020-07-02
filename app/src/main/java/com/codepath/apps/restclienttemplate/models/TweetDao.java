package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TweetDao {

    @Query("SELECT Tweet.body AS tweet_body, Tweet.createdAt AS tweet_createdAt, Tweet.mediaUrl as tweet_mediaUrl, Tweet.id AS tweet_id, User.*" +
            " FROM Tweet INNER JOIN User ON Tweet.userId = User.id ORDER BY createdAt DESC LIMIT 15")
    List<TweetWithUser> recentItems();

    // Ellipses mean that this method can take any number of Tweet objects in as an array
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(Tweet... tweets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);
}
