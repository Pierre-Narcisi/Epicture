package com.example.pierre.imjur;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class imageActivity extends AppCompatActivity {

    private OkHttpClient httpClient;
    private String album_hash;
    private String upvote;
    private String downvote;

    private void fetch_comment() {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/"+album_hash+"/comments")
                .header("Authorization", "Client-ID 00df112ea314ebd")
                .header("User-Agent", "imjur")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    final List<Comment> comments = new ArrayList<Comment>();
                    JSONArray items = data.getJSONArray("data");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        Comment comment = new Comment();
                        comment.author = item.getString("author");
                        comment.phrase = item.getString("comment");
                        comments.add(comment);

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            render_comment(comments);
                        }
                    });
                } catch (JSONException e) {
                }
            }
        });

    }

    private void render_comment(final List<Comment> comments) {
        RecyclerView rv = (RecyclerView) findViewById(R.id.gallery_comment);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter<CommentVH> adapter = new RecyclerView.Adapter<CommentVH>() {
            @Override
            public CommentVH onCreateViewHolder(ViewGroup parent, int viewType) {
                CommentVH vh = new CommentVH(getLayoutInflater().inflate(R.layout.comment, null));
                vh.author = (TextView) vh.itemView.findViewById(R.id.name);
                vh.phrase= (TextView) vh.itemView.findViewById(R.id.comment);
                return vh;
            }

            @Override
            public void onBindViewHolder(CommentVH holder, int position) {
                holder.author.setText(comments.get(position).author);
                holder.phrase.setText(comments.get(position).phrase);
            }

            @Override
            public int getItemCount() {
                return comments.size();
            }


        };
        rv.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        rv.addItemDecoration(itemDecoration);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16; // Gap of 16px
            }
        });
        rv.setNestedScrollingEnabled(false);
    }

    private static class CommentVH extends RecyclerView.ViewHolder {
        TextView phrase;
        TextView author;

        public CommentVH(View itemView) {
            super(itemView);
        }
    }

    private static class Comment {
        String phrase;
        String author;
    }

    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/"+album_hash)
                .header("Authorization", "Client-ID 00df112ea314ebd")
                .header("User-Agent", "imjur")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(null, "An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONArray items = data.getJSONObject("data").getJSONArray("images");
                    final List<imageActivity.Photo> photos = new ArrayList<imageActivity.Photo>();
                    upvote = Integer.toString(data.getJSONObject("data").getInt("ups"));
                    downvote = Integer.toString(data.getJSONObject("data").getInt("downs"));


                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        imageActivity.Photo photo = new imageActivity.Photo();
                        photo.id = item.getString("id");
                        if (item.getString("description") == "null")
                            photo.title = "";
                        else
                            photo.title = item.getString("description");
                        photo.height = item.getInt("height");
                        photos.add(photo); // Add photo to list
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            render(photos);
                        }
                    });
                } catch (JSONException e) {
                }
            }
        });
    }

    private void render(final List<imageActivity.Photo> photos) {
        RecyclerView rv = (RecyclerView) findViewById(R.id.gallery);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter<imageActivity.PhotoVH> adapter = new RecyclerView.Adapter<imageActivity.PhotoVH>() {
            @Override
            public imageActivity.PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                imageActivity.PhotoVH vh = new imageActivity.PhotoVH(getLayoutInflater().inflate(R.layout.gallery, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo_gallery);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title_gallery);
                return vh;
            }

            @Override
            public void onBindViewHolder(imageActivity.PhotoVH holder, int position) {
                Picasso.with(imageActivity.this).load("https://i.imgur.com/" +
                        photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(photos.get(position).title);
            }

            @Override
            public int getItemCount() {
                return photos.size();
            }


        };
        rv.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        rv.addItemDecoration(itemDecoration);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16; // Gap of 16px
            }
        });
        rv.setNestedScrollingEnabled(false);
    }

    private static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title;

        public PhotoVH(View itemView) {
            super(itemView);
        }
    }

    private static class Photo {
        int height;
        String id;
        String title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        album_hash = getIntent().getStringExtra("album_hash");
        fetchData();
        fetch_comment();

        TextView up = findViewById(R.id.ups);
        TextView down = findViewById(R.id.downs);
        up.setText("Upvotes: "+upvote);
        down.setText("Downvotes: "+downvote);

    }
}
