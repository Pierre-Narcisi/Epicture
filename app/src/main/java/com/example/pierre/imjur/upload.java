package com.example.pierre.imjur;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class upload extends AppCompatActivity {
    private OkHttpClient httpClient;
    public static final int PICK_IMAGE = 1;
    private Uri image;
    private String title = "";
    private String desc = "";
    private String accessToken;

    private byte[] readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        byte[] input = new byte[inputStream.available()];
        inputStream.read(input);
        inputStream.close();
        return input;
    }


    private void uploadImage() throws IOException {
        if (image == null) {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.upload_layout), "no image selected.", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        httpClient = new OkHttpClient.Builder()
                .build();
        byte[] imageAsBytes = readTextFromUri(image);
        Bitmap bp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"title\""),
                        RequestBody.create(null, title)
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image\""),
                        RequestBody.create(MediaType.parse("image/*"), title)
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"description\""),
                        RequestBody.create(null, desc)
                )
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image/")
                .header("Authorization", "Bearer " + accessToken)
                .post(formBody)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.upload_layout), "An error has occured 2.", Snackbar.LENGTH_LONG);
                snackbar.show();
                Log.e(null, "An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    boolean success = new JSONObject(response.body().string()).getBoolean("success");
                    if (success == true) {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.upload_layout), "Image uploaded.", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        finish();
                    } else {
                        Integer status = new JSONObject(response.body().string()).getInt("status");
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.upload_layout), "An error has occured 1." + status, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } catch (JSONException e) {
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ImageButton imgBtn = findViewById(R.id.imageButton);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });


        Button upBtn = findViewById(R.id.UploadBtn);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = findViewById(R.id.img_title).toString();
                desc = findViewById(R.id.img_desc).toString();
                try {
                    uploadImage();
                } catch (IOException e) {

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            ImageButton imgbtn = findViewById(R.id.imageButton);
            accessToken = getIntent().getStringExtra("accessToken");
            image = data.getData();
            imgbtn.setImageURI(image);
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.upload_layout), "Gg mec.", Snackbar.LENGTH_LONG);

            snackbar.show();
        }
    }
}
