package com.example.flixster.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Transient;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Parcel // annotation indicates class in Parcelable
public class Movie {

    // values from API
    // fields must be public for parceler
    String title;
    String overview;
    String posterPath; // only the path
    String backdropPath;
    Double voteAverage;
    String releaseDate;
    Integer id;
    ArrayList<Integer> genre_ids = new ArrayList<>();

    // no-arg, empty constructor required for Parceler
    public Movie() {}

    // initialize from JSON data
    public Movie(JSONObject object) throws JSONException {
        title = object.getString("title");
        overview = object.getString("overview");
        posterPath = object.getString("poster_path");
        backdropPath = object.getString("backdrop_path");
        voteAverage = object.getDouble("vote_average");
        releaseDate = object.getString("release_date");
        id = object.getInt("id");
        JSONArray results = object.getJSONArray("genre_ids");
        for (int i = 0; i < results.length(); i++) {
            try {
                genre_ids.add(results.getInt(i));
            } catch (JSONException e) {
                Log.e("MovieDetailsActivity", "Error parsing genre ids");
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Integer getId() {
        return id;
    }

    public ArrayList<Integer> getGenre_ids() {
        return genre_ids;
    }
}
