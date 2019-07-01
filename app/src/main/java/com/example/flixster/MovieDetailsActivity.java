package com.example.flixster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flixster.models.Movie;
import com.example.flixster.models.Config;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

// this is in the branch!
public class MovieDetailsActivity extends AppCompatActivity {

    // the movie to display
    Movie movie;

    String videoKey;
    String genres;

    AsyncHttpClient client;
    Config config;

    // the view objects, resolved by ButterKnife
    /*@BindView(R.id.tvTitle)*/ TextView tvTitle;
    /*@BindView(R.id.tvOverview)*/ TextView tvOverview;
    /*@BindView(R.id.rbVoteAverage)*/ RatingBar rbVoteAverage;
    /*@BindView(R.id.tvVoteAvgNum)*/ TextView tvVoteAvgNum;
    /*@BindView(R.id.tvReleaseDate)*/ TextView tvReleaseDate;
    ImageView ivVideo;
    TextView tvDirections;
    TextView tvGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        client = new AsyncHttpClient();

        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        rbVoteAverage = findViewById(R.id.rbVoteAverage);
        tvVoteAvgNum = findViewById(R.id.tvVoteAvgNum);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        ivVideo = findViewById(R.id.ivVideo);
        tvDirections = findViewById(R.id.tvDirections);
        tvGenres = findViewById(R.id.tvGenres);

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0..10, convet to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
        tvVoteAvgNum.setText(String.format("%s", voteAverage));

        // set the release date
        String releaseDate = movie.getReleaseDate();
        String[] dateNums = releaseDate.split("-");
        String month = dateNums[1];
        switch (month) {
            case "01": month = "January"; break;
            case "02": month = "February"; break;
            case "03": month = "March"; break;
            case "04": month = "April"; break;
            case "05": month = "May"; break;
            case "06": month = "June"; break;
            case "07": month = "July"; break;
            case "08": month = "August"; break;
            case "09": month = "September"; break;
            case "10": month = "October"; break;
            case "11": month = "November"; break;
            case "12": month = "December"; break;
            default: month = "Error: Value of month was: " + month;
        }
        tvReleaseDate.setText(String.format("Released: %s %s, %s", month, dateNums[2], dateNums[0]));

        getGenreMeanings();

        getConfiguration();

    }

    public void getGenreMeanings() {
        String genre_url = MovieListActivity.API_BASE_URL + "/genre/movie/list";
        RequestParams genre_params = new RequestParams();
        genre_params.add(MovieListActivity.API_KEY_PARAM, getString(R.string.api_key));
        client.get(genre_url, genre_params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<String> genre_names = new ArrayList<>();
                try {
                    JSONArray results = response.getJSONArray("genres");
                    ArrayList<Integer> genre_ids = movie.getGenre_ids();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject genre = results.getJSONObject(i);
                        if (genre_ids.contains(genre.get("id"))) {
                            genre_names.add((String) genre.get("name"));
                        }
                    }
                    genres = genre_names.get(0);
                    for (int i = 1; i < genre_names.size(); i++) {
                        genres += ", " + genre_names.get(i);
                    }
                    Log.i("MovieDetailsActivity", "Loaded genres list: " + genres);
                    tvGenres.setText("Genres: " + genres);
                } catch (JSONException e) {
                    genre_names.add("Error determining genres");
                    Log.e("MovieDetailsActivity", "Error parsing genre names");
                }
            }
        });
    }

    public void playVideo(View view) {
        Intent intent = new Intent(view.getContext(), MovieTrailerActivity.class);
        if (videoKey != null) {
            intent.putExtra("id", videoKey);
            view.getContext().startActivity(intent);
        }
    }

    private void getVideoKey(Movie movie) {

        // create the url
        String url = MovieListActivity.API_BASE_URL + "/movie/" + movie.getId() + "/videos";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(MovieListActivity.API_KEY_PARAM, getString(R.string.api_key));
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    JSONObject first = results.getJSONObject(0);
                    videoKey = first.getString("key");
                } catch (JSONException e) {
                    videoKey = null;
                    Log.e("MovieTrailerActivity", "Error parsing video key");
                }
            }
        });
    }

    private void getConfiguration() {
        // create the url
        String url = MovieListActivity.API_BASE_URL + "/configuration";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(MovieListActivity.API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        // execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i("MovieDetailsActivity", String.format("Loaded configuration with imageBaseUrl %s and posterSize %s",
                            config.getImageBaseUrl(),
                            config.getBackdropSize()));
                    getBackdrop();
                    getVideoKey(movie);
                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }
            }
        });
    }

    private void getBackdrop() {
        String imageUrl = config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath());
        Glide.with(this.getApplicationContext())
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(this.getApplicationContext(), 25, 0))
                .placeholder(R.drawable.flixster_backdrop_placeholder)
                .error(R.drawable.flixster_backdrop_placeholder)
                .into(ivVideo);
    }

    // handle errors, log and alert user
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log the error
        Log.e("MovieDetailsActivity", message, error);
        //alert the user to avoid silent errors
        if (alertUser) {
            //show a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
