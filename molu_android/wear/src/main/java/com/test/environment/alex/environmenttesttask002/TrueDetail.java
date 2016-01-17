package com.test.environment.alex.environmenttesttask002;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

public class TrueDetail extends Activity implements DelayedConfirmationView.DelayedConfirmationListener{

    private TextView mTextView;

    DelayedConfirmationView delayedConfirmationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_true_detail);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

                delayedConfirmationView = (DelayedConfirmationView)
                        findViewById(R.id.totimer);

                delayedConfirmationView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TrueDetail.this.finish();
                    }
                });

                DataInfo.initData();
                String d = "司机名:" + DataInfo.Driver.GetDriver().name + "\n"
                        + "手机号:" + DataInfo.Driver.GetDriver().phone + "\n"
                        + "预计时间:" + DataInfo.Driver.GetDriver().time + "\n"
                        + "车牌号:" + DataInfo.Driver.GetDriver().cartNo;

                d += "\n\n";
                d += "即将进入陌路";

                mTextView.setText(d);
                onStartTimer();
            }
        });


    }

    public void onStartTimer() {
        delayedConfirmationView.setTotalTimeMs(5 * 1000);
        delayedConfirmationView.setListener(this);
        delayedConfirmationView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        Intent intent = new Intent(TrueDetail.this, UberDetail.class);
        startActivity( intent );
        this.finish();
    }

    @Override
    public void onTimerSelected(View view) {
        this.finish();
    }
}
