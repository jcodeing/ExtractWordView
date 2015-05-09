package com.jcodeing.extractwordview;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jcodeing.extractwordview.widget.EWListView;
import com.jcodeing.extractwordview.widget.EWListViewChildET;


public class MainActivity extends ActionBarActivity {
    private EWListView ewlv;
    private String[] poetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init data
        Resources res = getResources();
        poetry = res.getStringArray(R.array.poetry);
        //init view
        ewlv = (EWListView) findViewById(R.id.ewlv);
        //
        ewlv.activity=this;
        ewlv.setAdapter(new EwlvAdapter());
    }

    class EwlvAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return poetry.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String verse = poetry[position];
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.ewlv_item, null);
                holder = new ViewHolder();
                holder.ewe_text = (EWListViewChildET)
                        convertView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.ewe_text.setText(verse);
            return convertView;
        }
    }

    class ViewHolder {
        EWListViewChildET ewe_text;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
