/*
 * Copyright (c) 2018 Michael Pöhn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.torproject.android.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import net.freehaven.tor.control.TorControlConnection;

import org.torproject.jni.TorService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webview);
        TextView statusTextView = findViewById(R.id.status);

        GenericWebViewClient webViewClient = new GenericWebViewClient();
        webViewClient.setRequestCounterListener(requestCount ->
                runOnUiThread(() -> statusTextView.setText("Request Count: " + requestCount)));
        webView.setWebViewClient(webViewClient);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra(TorService.EXTRA_STATUS);
                Toast.makeText(context, status, Toast.LENGTH_SHORT).show();


            }
        }, new IntentFilter(TorService.ACTION_STATUS));


        bindService(new Intent(this, TorService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                //moved torService to a local variable, since we only need it once
                TorService torService = ((TorService.LocalBinder) service).getService();
                TorControlConnection conn = torService.getTorControlConnection();

                while ((conn = torService.getTorControlConnection())==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (conn != null)
                {
                    Toast.makeText(MainActivity.this, "Got Tor control connection", Toast.LENGTH_LONG).show();
                    webView.setBackgroundColor(Color.WHITE); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.setWebViewClient(new WebViewClient()); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.getSettings().setLoadsImagesAutomatically(true); // automatically loads images to enhance wrapper experience
                    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // forces the wrapper to load from internet resources instead of cache which ensure you are seeing the most up-to-date information
                    webView.getSettings().setSupportMultipleWindows(false); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.getSettings().getMediaPlaybackRequiresUserGesture(); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.getSettings().setJavaScriptEnabled(true); // enables javascript - normally, this is a security no-no - however, the web-page you are wrapping is protected in an application sandbox, an isolated process, and with a certificate. the website would have to be the culprit for you to be truly concerned.
                    CookieManager.getInstance().setAcceptCookie(true); // set to false
                    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false); // prevents third-party cookies - usually these cookies are not necessary for a website to function
                    webView.getSettings().setSupportZoom(true);
                    webView.setOverScrollMode(View.OVER_SCROLL_NEVER); // prevents choppy rendering when scrolling
                    webView.getSettings().setAllowContentAccess(false); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.getSettings().setAllowFileAccess(false); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.getSettings().setDomStorageEnabled(false); // take a look at the documentation on the android studio developer page: https://developer.android.com/docs
                    webView.clearHistory(); // a good practice to clear history - clears history every time the application boots up so you preserve your current session when in use
                    webView.clearCache(true); // a good practice to clear all cache to prevent loading issues
                    webView.clearFormData(); // a good practice to reset form data
                    webView.setScrollbarFadingEnabled(false); // forces the scrollbar to stay visible
                    webView.loadUrl("https://aidaccess.org/"); // change this url to whatever you want to create your wrapper!

                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        },BIND_AUTO_CREATE);

    }
}
