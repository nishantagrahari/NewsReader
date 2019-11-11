package com.nagrahari.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Webcontent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webcontent);

        WebView wV=(WebView)findViewById(R.id.webView);
        wV.getSettings().setJavaScriptEnabled(true);
        wV.setWebViewClient(new WebViewClient());

        Intent intent=getIntent();


        wV.loadUrl(intent.getStringExtra("url"));

    }
}
