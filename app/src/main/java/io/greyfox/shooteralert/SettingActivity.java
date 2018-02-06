package io.greyfox.shooteralert;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import io.greyfox.shooteralert.helper.SessionManager;

/**
 * Created by volkov on 12/22/16.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private SessionManager session;
    private Switch push_switch;
    private Button send_feedback_but, write_review_but, faq_but;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        session = new SessionManager(getApplicationContext());

        push_switch = (Switch) findViewById(R.id.push_switch);
        send_feedback_but = (Button) findViewById(R.id.send_feedback_but);
        write_review_but = (Button) findViewById(R.id.write_review_but);
        faq_but = (Button) findViewById(R.id.write_review_but);

        send_feedback_but.setOnClickListener(this);
        write_review_but.setOnClickListener(this);
        faq_but.setOnClickListener(this);

        push_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                session.setNotificationSetting(isChecked);
            }
        });
        push_switch.setChecked(session.getNotificationSetting());
    }

    @Override
    public void onClick(View v) {
        if (v == send_feedback_but) {
            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "alex@infusionti.com" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            Email.putExtra(Intent.EXTRA_TEXT, "Dear ...," + "");
            startActivity(Intent.createChooser(Email, "Send Feedback:"));

        }
        else if (v == write_review_but) {
            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("https://play.google.com/store/apps/details?id=io.greyfox.shooteralert&hl=en"));
            startActivity(marketLaunch);
        }
        else if (v == faq_but) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    @Override
    public void onBackPressed() {

    }
}
