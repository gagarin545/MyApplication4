package com.example.myapplication4;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.myapplication4.MainActivity.status;
import static com.example.myapplication4.Variable.debug;
import static com.example.myapplication4.Variable.kod_tit;

public class open_db extends AsyncTask {
    private Connection c;
    private Statement stmt;

    private String sql;
    private ArrayList<ArrayList> data;
    ArrayList<String[]> dat0;
    String sql0 = "select distinct namecity from city where kod_tit = " + kod_tit;

    open_db(String sql, ArrayList<ArrayList> data) {
        this.sql = sql;
        this.data = data;

    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://178.46.165.205:5432/zlt36","ura", "Kukish54");

            stmt = c.createStatement();

            ArrayList<String[]> dat = new ArrayList<String[]>();
            ResultSet rs = stmt.executeQuery( sql0 );

            ArrayList<String> town = new ArrayList<>();
            while ( rs.next() ) {
                town.add(rs.getString("namecity"));
            }

            rs.close();

            TimeUnit.SECONDS.sleep(1);
            rs = stmt.executeQuery( sql );

            for( String city: town) {
                String[] str = new String[4];
                str[1] = city;
                str[2] = "0";
                str[3] = "0";
                dat.add(str);
            }

            while ( rs.next() ) {
                String time = rs.getString("time");
                String City = rs.getString("namecity");

                if( time.equals( dat.get(0)[0]) ) {
                    for (String[] s : dat) {
                        if (City.equals(s[1])) {
                            if ((s[2] = rs.getString("kcount")) == null) s[2] = "0";
                            if ((s[3] = rs.getString("scount")) == null) s[3] = "0";
                        }
                        s[0] = time;
                    }
                }
                else
                {
                    if( data.size() > 0) {
                        dat = new ArrayList<>();
                        for( String city: town) {
                            String[] str = new String[4];
                            str[0] = time;
                            str[1] = city;
                            str[2] = "0";
                            str[3] = "0";
                            dat.add(str);
                        }
                    }

                    data.add(dat);

                    for (String[] s : dat)
                        if (City.equals(s[1])) {
                            s[0] = time;
                            if ((s[2] = rs.getString("kcount")) == null) s[2] = "0";
                            if ((s[3] = rs.getString("scount")) == null) s[3] = "0";
                        }
                }
            }

            rs.close();
            stmt.close();
            c.close();

        }  catch (Exception e) {
            e.printStackTrace();
            Log.e(debug,e.getClass().getName()+": "+e.getMessage());
        }
        return null;
    }
}
