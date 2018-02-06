package io.greyfox.shooteralert.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import io.greyfox.shooteralert.MapsActivity;
import io.greyfox.shooteralert.R;

import static io.greyfox.shooteralert.app.AppConfig.APP_NAME;

public class MarkerInfoView extends Dialog implements View.OnClickListener {

    private TextView city_txt, incident_date_txt, deaths_txt, injuries_txt;
    private Button other_event_but, url1_txt, url2_txt, share_but;
    private ShootInfo mShootInfo;
    private MapsActivity mActivity;

    public MarkerInfoView(Context context, ShootInfo shootInfo, MapsActivity activity) {
        super(context);
        setContentView(R.layout.marker_infoview);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mShootInfo = shootInfo;
        mActivity = activity;

        city_txt = (TextView) findViewById(R.id.city_txt);
        incident_date_txt = (TextView) findViewById(R.id.incident_date_txt);
        deaths_txt = (TextView) findViewById(R.id.deaths_txt);
        injuries_txt = (TextView) findViewById(R.id.injuries_txt);

        other_event_but = (Button) findViewById(R.id.other_event_but);
        url1_txt = (Button) findViewById(R.id.url1_txt);
        url2_txt = (Button) findViewById(R.id.url2_txt);
        share_but = (Button) findViewById(R.id.share_but);

        other_event_but.setOnClickListener(this);
        url1_txt.setOnClickListener(this);
        url2_txt.setOnClickListener(this);
        share_but.setOnClickListener(this);

        setShootData();
    }

    private void setShootData() {
        city_txt.setText(mShootInfo.city);
        incident_date_txt.setText(AppConfig.parseDateToddMMyyyy(mShootInfo.incident_date));
        deaths_txt.setText(String.valueOf(mShootInfo.killed));
        injuries_txt.setText(String.valueOf(mShootInfo.injured));
        url1_txt.setText(mShootInfo.url1.replace("http://", "").replace("https://", ""));
        url2_txt.setText(mShootInfo.url2.replace("http://", "").replace("https://", ""));
    }

    @Override
    public void onClick(View v) {
        if (v == other_event_but) {
            for (int i = 0; i < AppConfig.shootInfoList.size(); i ++) {
                if (mShootInfo.id == AppConfig.shootInfoList.get(i).id) {
                    if (i + 1 >= AppConfig.shootInfoList.size()) {
                        mShootInfo = AppConfig.shootInfoList.get(0);
                    }
                    else {
                        mShootInfo = AppConfig.shootInfoList.get(i + 1);
                    }
                    setShootData();

                    return;
                }
            }
        }
        else if (v == url1_txt) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mShootInfo.url1));
            mActivity.startActivity(browserIntent);
        }
        else if (v == url2_txt) {
            if (!mShootInfo.url2.equals("")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mShootInfo.url2));
                mActivity.startActivity(browserIntent);
            }
        }
        else if (v == share_but) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = String.format("Here is shoot at %s, %s, %s in %s. (Kills: %d, Injuries: %d)", mShootInfo.address, mShootInfo.city, mShootInfo.state,
                    AppConfig.parseDateToddMMyyyy(mShootInfo.incident_date), mShootInfo.killed, mShootInfo.injured);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, APP_NAME);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

            mActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }
}
