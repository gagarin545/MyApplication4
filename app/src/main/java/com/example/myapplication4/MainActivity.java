package com.example.myapplication4;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.myapplication4.Variable.debug;
import static com.example.myapplication4.Variable.kod_tit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String[] mValues;
    static Boolean status = true;
    Button bt1;
    ListView lv;
    open_db pgcon;
    private static String sql1 = "WITH RECURSIVE skv ( time, kod_tit, namecity, count) AS (" +
            "SELECT time_close::date, i.kod_tit, namecity, count(*) from  city c LEFT JOIN incident i using (kod_tit) where time_close::date = current_date and c.kod_city= i.kod_city and control_term ~ 'СКВ' and i.kod_tit = " + kod_tit + " group by i.kod_tit, time_close::date, namecity), kv (time, kod_tit, namecity, count) AS (" +
            "SELECT time_close::date, i.kod_tit, namecity, count(*) from  city c LEFT JOIN incident i using (kod_tit) where time_close::date = current_date and c.kod_city= i.kod_city and i.kod_tit = " + kod_tit + " group by i.kod_tit, time_close::date, namecity )" +
            "select k.time, k.namecity, k.count as kcount, s.count as scount from kv k LEFT OUTER JOIN skv s  using (namecity) order by k.namecity, k.time;";
    private static String sql2 = "WITH RECURSIVE kv (time, kod_tit, namecity, count) AS ("+
            "SELECT time_close::date, i.kod_tit, namecity, count(*) from city c LEFT OUTER JOIN incident i using( kod_tit ) where  EXTRACT(WEEK from current_date) - 1 = EXTRACT(WEEK from time_close) and c.kod_city= i.kod_city and i.kod_tit =  " + kod_tit + " group by time_close::date, i.kod_tit, namecity),"+
            "skv ( time, kod_tit, namecity, count) AS ( "+
            "SELECT time_close::date, i.kod_tit, namecity, count(*) from city c LEFT OUTER JOIN incident i using( kod_tit ) where  EXTRACT(WEEK from current_date) - 1 = EXTRACT(WEEK from time_close) and c.kod_city= i.kod_city and control_term ~ 'СКВ' and i.kod_tit =  " + kod_tit + " group by time_close::date, i.kod_tit, namecity) "+
            "select k.time, k.namecity, k.count as kcount, s.count as scount from kv k LEFT OUTER JOIN skv s using ( namecity, time) order by k.time, k.namecity ;";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listView);

        bt1 = (Button) findViewById(R.id.button); // определение кнопки Send
        bt1.setOnClickListener(this);
        bt1.callOnClick();

    }

    @Override
    public void onClick(View v) {
        ArrayList<ArrayList> pg_data = new ArrayList<>();
        ArrayList<BarData> list = new ArrayList<>();

        if (status) {

            bt1.setText("Сегодня");
            Log.e(debug, "--+++ " + bt1.getText() + status);
            pgcon = new open_db(sql1, pg_data);
            status = false;
        } else {

            bt1.setText("За прошлую неделю");
            Log.e(debug, "------ " + bt1.getText() + status);
            pgcon = new open_db(sql2, pg_data);
            status = true;
        }

        try {
            pgcon.execute();
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e(debug, pg_data.size() + "++++++++++ " + status);

        for (ArrayList<String[]> datas : pg_data) {
            Log.e(debug, datas.size() + "------ " + status);
            mValues = new String[datas.size()];
            for (int i = 0 ; i < datas.size(); i++) {
                mValues[i] = datas.get(i)[1];
                Log.e(debug, datas.get(i)[0] + datas.get(i)[1] + datas.get(i)[2] +datas.get(i)[3] );
            }
            list.add(generateData(datas));
        }
            ChartDataAdapter chartDataAdapter = new ChartDataAdapter(getApplicationContext(), list);
            lv.setAdapter(chartDataAdapter);
    }

    private class ChartDataAdapter extends ArrayAdapter<BarData>  {

        ChartDataAdapter(Context context, List<BarData> objects) {
            super(context, 0, objects);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BarData data = getItem(position);
            ViewHolder holder = null;

            if(convertView == null){
                holder = new ViewHolder();
                convertView = getLayoutInflater().from(getContext()).inflate(R.layout.list_item_barchat, null );
                holder.chart = (BarChart) convertView.findViewById(R.id.chart);
                convertView.setTag(holder);}
            else
                holder = (ViewHolder) convertView.getTag();

            holder.chart.getDescription().setEnabled(false);
            holder.chart.setDrawGridBackground(false);

         //   holder.chart.getDescription().setText("Миасс");

            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.TOP);
            xAxis.setDrawGridLines(true);

            xAxis.setLabelRotationAngle(-60);
            xAxis.setLabelCount( mValues.length, false);

            xAxis.setValueFormatter(new MyXAxisValueFormatter(mValues));

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setLabelCount(4, false);
            leftAxis.setSpaceTop(20f);

            YAxis rigthAxis = holder.chart.getAxisRight();
            rigthAxis.setLabelCount(4, false);
            rigthAxis.setSpaceTop(20f);

            holder.chart.setData(data);
            holder.chart.setFitBars(true);
            holder.chart.animateY(500);
            return convertView;

        }

        private class ViewHolder{
            BarChart chart;

        }
    }

    private BarData generateData(ArrayList<String[]> da){
        ArrayList<BarEntry> order_blue = new ArrayList<>();
        ArrayList<BarEntry> order_red = new ArrayList<>();
        int i = 0, kcount = 0, scount = 0;

        for( String ss[]: da) {
            order_blue.add(new BarEntry(i, (float) Integer.parseInt(ss[2]) ));
            kcount +=  Integer.parseInt(ss[2]);
            order_red.add(new BarEntry(i++, (float) Integer.parseInt(ss[3])) );
            scount +=  Integer.parseInt(ss[3]);
        }

        BarDataSet dataset1 = new BarDataSet(order_blue,  " Заявки - " + kcount );
        BarDataSet dataset2 = new BarDataSet(order_red, "  Просрочено - " + scount + "  за " + da.get(0)[0]);
        dataset1.setColors(Color.GREEN);
        dataset2.setColors(Color.RED);
        dataset2.setBarShadowColor(Color.rgb(203,203,203));
        dataset1.setBarShadowColor(Color.rgb(203,203,203));

        BarData data = new BarData(dataset1, dataset2);
        data.setBarWidth(0.6f);

        return data;
    }

}

