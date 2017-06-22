package com.sxnwlfkk.dailyroutines.views.guide;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;


public class GuideActivity extends Activity {

    private View.OnClickListener websiteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.website_url)));
            startActivity(intent);
        }
    };

    private View.OnClickListener githubButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.github_url)));
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("The Guide");

        String versionName = "";
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView aboutTextView = (TextView) findViewById(R.id.guide_text_view);

        Spanned aboutText = Html.fromHtml("<h1>Daily Routines</h1><h2>Version " + versionName + "</h2>"
                + getString(R.string.guide_txt));
        aboutTextView.setText(aboutText);

        Button githubButton = (Button) findViewById(R.id.guide_visit_github);
        githubButton.setOnClickListener(githubButtonClickListener);
        Button websiteButton = (Button) findViewById(R.id.guide_visit_website);
        websiteButton.setOnClickListener(websiteButtonClickListener);
    }
}
