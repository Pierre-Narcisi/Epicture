package com.example.pierre.imjur;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class favoriteFragment extends Fragment {
    private OkHttpClient httpClient;
    private String username;
    private String accessToken;


    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/" + username + "/favorites/0/newest")
                .header("Authorization", "Bearer " + accessToken)
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
                    JSONArray items = data.getJSONArray("data");
                    final List<Photo> photos = new ArrayList<Photo>();
                    Log.d("chibre", response.body().string());

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        Photo photo = new Photo();
                        if (item.getBoolean("is_album")) {
                            photo.toPrint = item.getString("cover");
                        } else {
                            photo.toPrint = item.getString("id");
                        }
                        photo.id = item.getString("id");
                        photo.title = item.getString("title");
                        photos.add(photo); // Add photo to list
                    }

                    getActivity().runOnUiThread(new Runnable() {
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


    private void render(final List<Photo> photos) {
        RecyclerView rv = (RecyclerView) getView().findViewById(R.id.rv_of_photos);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.addOnItemTouchListener(new goToImageActivity(getActivity(), (RecyclerView) getView().findViewById(R.id.rv_of_photos), new goToImageActivity.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent album = new Intent(getActivity(), imageActivity.class);
                album.putExtra("album_hash", photos.get(position).id);
                startActivity(album);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.image, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                return vh;
            }

            @Override
            public void onBindViewHolder(PhotoVH holder, int position) {
                holder.currentItem = photos.get(position);
                Picasso.with(getActivity()).load("https://i.imgur.com/" +
                        photos.get(position).toPrint + ".jpg").into(holder.photo);
                holder.title.setText(photos.get(position).title);
            }

            @Override
            public int getItemCount() {
                return photos.size();
            }


        };
        rv.setAdapter(adapter);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16; // Gap of 16px
            }
        });
    }

    static class goToImageActivity implements RecyclerView.OnItemTouchListener {

        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            public void onItemClick(View view, int position);

            public void onLongItemClick(View view, int position);
        }

        GestureDetector mGestureDetector;

        public goToImageActivity(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && mListener != null) {
                        mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    class PhotoVH extends RecyclerView.ViewHolder {

        public View view;
        public Photo currentItem;
        ImageView photo;
        TextView title;


        public PhotoVH(View itemView) {
            super(itemView);
        }
    }

    static class Photo {
        String toPrint;
        String id;
        String title;
    }

    private void setInfo() {
        Intent intent = getActivity().getIntent();
        username = intent.getStringExtra("username");
        accessToken = intent.getStringExtra("accessToken");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setInfo();
        fetchData();
        return inflater.inflate(R.layout.favorite_fragment, container, false);
    }
}

