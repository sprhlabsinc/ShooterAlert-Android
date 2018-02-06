package io.greyfox.shooteralert.app;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import io.greyfox.shooteralert.MapsActivity;
import io.greyfox.shooteralert.R;

public class MarkerInfoFragment extends Fragment implements View.OnClickListener {

    private TextView city_txt, incident_date_txt, deaths_txt, injuries_txt;
    private Button other_event_but, url1_txt, url2_txt;
    public ShootInfo mShootInfo;
    private MapsActivity mActivity;

    public MarkerInfoFragment() {
    }

    public MarkerInfoFragment(ShootInfo shootInfo, MapsActivity activity) {
        this.mShootInfo = shootInfo;
        this.mActivity = activity;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.marker_infoview, null);

        city_txt = (TextView) rootView.findViewById(R.id.city_txt);
        incident_date_txt = (TextView) rootView.findViewById(R.id.incident_date_txt);
        deaths_txt = (TextView) rootView.findViewById(R.id.deaths_txt);
        injuries_txt = (TextView) rootView.findViewById(R.id.injuries_txt);

        other_event_but = (Button) rootView.findViewById(R.id.other_event_but);
        url1_txt = (Button) rootView.findViewById(R.id.url1_txt);
        url2_txt = (Button) rootView.findViewById(R.id.url2_txt);

        other_event_but.setOnClickListener(this);
        url1_txt.setOnClickListener(this);
        url2_txt.setOnClickListener(this);

        city_txt.setText(mShootInfo.city);
        incident_date_txt.setText(AppConfig.parseDateToddMMyyyy(mShootInfo.incident_date));
        deaths_txt.setText(String.valueOf(mShootInfo.killed));
        injuries_txt.setText(String.valueOf(mShootInfo.injured));
        url1_txt.setText(mShootInfo.url1.replace("http://", "").replace("https://", ""));
        url2_txt.setText(mShootInfo.url2.replace("http://", "").replace("https://", ""));

        return rootView;
    }

    @Override
    public void onClick(View v) {

        if (v == other_event_but) {
            mActivity.moveNext();
        }
        else if (v == url1_txt) {
            if (!mShootInfo.url1.equals("")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mShootInfo.url1));
                startActivity(browserIntent);
            }
        }
        else if (v == url2_txt) {
            if (!mShootInfo.url2.equals("")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mShootInfo.url2));
                startActivity(browserIntent);
            }
        }
    }
}
