package com.ensias.healthcareapp.activity;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class YouTubePlayerActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        webView = findViewById(R.id.youtubeWebView);

        String videoUrl = getIntent().getStringExtra("videoUrl");

        if (videoUrl != null && videoUrl.contains("youtube")) {
            String videoId = extractYoutubeVideoId(videoUrl);
            String embedUrl = "https://www.youtube.com/embed/" + videoId;

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);

            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient());

            webView.loadUrl(embedUrl);
        }
    }

    private String extractYoutubeVideoId(String url) {
        String videoId = "";
        if (url.contains("v=")) {
            String[] split = url.split("v=");
            if (split.length > 1) {
                videoId = split[1].split("&")[0];
            }
        } else if (url.contains("youtu.be/")) {
            videoId = url.substring(url.lastIndexOf("/") + 1);
        }
        return videoId;
    }
}
