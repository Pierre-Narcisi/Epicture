package com.example.pierre.imjur;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class login extends Activity {
    public Button btn;
    public void init() {
        btn = findViewById(R.id.signIn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent connect = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.imgur.com/oauth2/authorize?client_id=0b402a9ed7cc167&response_type=token\n"));
                startActivityForResult(connect, 1);
            }
        });
    }

    @Override
    protected void onCreate(Bundle InstanceState) {
        super.onCreate(InstanceState);
        setContentView(R.layout.activity_login);
        Uri uri = this.getIntent().getData();
        if (uri !=null) {
            String infos[] = uri.getFragment().split("&");
            Intent imjur = new Intent(login.this, imjur.class);
            imjur.putExtra("accessToken", infos[0].replace("access_token=", ""));
            imjur.putExtra("username", infos[4].replace("account_username=", ""));
            imjur.putExtra("accountId", infos[0].replace("account_id=", ""));
            startActivity(imjur);
        }
        init();
    }
}
