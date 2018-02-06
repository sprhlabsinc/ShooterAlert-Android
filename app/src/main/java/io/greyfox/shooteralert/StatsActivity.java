package io.greyfox.shooteralert;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Calendar;

import io.greyfox.shooteralert.app.AppConfig;
import io.greyfox.shooteralert.app.AxisValueFormatter;
import io.greyfox.shooteralert.app.ValueFormatter;
import io.greyfox.shooteralert.helper.SessionManager;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

/**
 * Created by volkov on 12/22/16.
 */

public class StatsActivity extends AppCompatActivity {

    private SessionManager session;
    private BarChart mChart;
    private TextView count_txt, description_txt;
    protected String[] mMonths = new String[] {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        session = new SessionManager(getApplicationContext());

        mChart = (BarChart) findViewById(R.id.chart);
        count_txt = (TextView) findViewById(R.id.count_txt);
        description_txt = (TextView) findViewById(R.id.description_txt);

        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(false);
        mChart.setHighlightFullBarEnabled(false);

        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new AxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setDrawGridLines(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setEnabled(false);

        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mMonths[(int) value % mMonths.length];
            }
        });
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);

        description_txt.setText(String.format("U.S. mass shootings in %d", year));
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int sum = 0;
        for (int i = 0; i <= month + 1; i ++) {
            int[] nRes = AppConfig.getShootInfo(AppConfig.shootInfoList, i, year);
            float val1 = nRes[0];
            float val2 = nRes[1];
            if (i == 0) {
                val1 = 0; val2 = 0;
            }

            yVals1.add(new BarEntry(
                    i,
                    new float[]{val1, val2}));
            sum += val1;
            sum += val2;
        }
        count_txt.setText(String.valueOf(sum));
        if (month > 7) {
            mChart.getXAxis().setTextSize(16);
        }
        else {
            mChart.getXAxis().setTextSize(20);
        }
        mChart.getXAxis().setTextColor(R.color.colorPrimary);

        BarDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "");
            set1.setDrawIcons(true);
            set1.setColors(getColors());
            set1.setStackLabels(new String[]{"Fatalities", "Injured"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueFormatter(new ValueFormatter());
            data.setValueTextSize(14);
            data.setValueTextColor(Color.WHITE);

            mChart.setData(data);
        }

        mChart.setFitBars(true);
        mChart.invalidate();
    }
    private int[] getColors() {

        int stacksize = 2;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        colors[0] = rgb("#2ec4b6");
        colors[1] = rgb("#393e46");

        return colors;
    }

    @Override
    public void onBackPressed() {

    }
}
