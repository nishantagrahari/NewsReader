package com.nagrahari.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> newsTitle;
    ArrayList<String> newsURL;

    ArrayAdapter arrayAdapter;
    SQLiteDatabase newsDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        newsTitle=new ArrayList<>();
        newsURL=new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,newsTitle);
        listView.setAdapter(arrayAdapter);


        newsDb = this.openOrCreateDatabase("news",MODE_PRIVATE,null);
        newsDb.execSQL("CREATE TABLE IF NOT EXISTS newsTable (id INTEGER Primary key,title VARCHAR,url VARCHAR)");

        updateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),Webcontent.class);
                intent.putExtra("url",newsURL.get(position));
                startActivity(intent);
            }
        });

        DownloadTask task=new DownloadTask();
        try{
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void updateListView()
    {
        Log.i("Connection__db_status:","Connected");

        Cursor c= newsDb.rawQuery("Select * from newsTable",null);

        int idIndex=c.getColumnIndex("id");
        int titleIndex = c.getColumnIndex("title");
        int urlIndex = c.getColumnIndex("url");

        if(c.moveToFirst()) {
            newsTitle.clear();
            newsURL.clear();

            while (c.moveToNext()) {
                String title = c.getString(titleIndex);
                String url = c.getString(urlIndex);
                Log.i("news title", title);
                Log.i("news url", url);
                newsTitle.add(title);
                newsURL.add(url);
            }
            arrayAdapter.notifyDataSetChanged();
        }


      /* if(c.moveToFirst()){
           newsTitle.clear();
           newsURL.clear();

           do{
              newsTitle.add(c.getString(titleIndex));
              newsURL.add(c.getString(urlIndex));
           }while(c.moveToNext());
           arrayAdapter.notifyDataSetChanged();
       }*/
    }

    public class DownloadTask extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... urls) {

            String result="",articleJson="";
            HttpURLConnection urlConnection=null;
            int items=15;

            try {
                URL url=new URL(urls[0]);
                urlConnection=(HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);
                int data=reader.read();
                while(data!=-1)
                {
                    char current=(char)data;
                    result=result+current;
                    data=reader.read();
                }
                Log.i("answer is",result);

                JSONArray array=new JSONArray(result);

                newsDb.execSQL("DELETE FROM newsTable");

                if(items<array.length()) {
                    for (int i = 0; i < items; i++) {

                        String keys = array.getString(i);
                        //Log.i("keys are:", keys);

                        URL url1=new URL("https://hacker-news.firebaseio.com/v0/item/"+keys+".json?print=pretty");

                        urlConnection=(HttpURLConnection)url1.openConnection();
                        InputStream in1 = urlConnection.getInputStream();
                        InputStreamReader reader1=new InputStreamReader(in1);
                        int data1=reader1.read();

                        articleJson="";

                        while(data1 != -1)
                        {

                            char current=(char)data1;
                            articleJson=articleJson+current;
                            data1=reader1.read();

                        }

                        //Log.i("Article info:",articleJson);
                        JSONObject jsonObject = new JSONObject(articleJson);

                        if(!jsonObject.isNull("title") && !jsonObject.isNull("url"))
                        {
                            String articleJsonTitle=jsonObject.getString("title");
                            String articleJsonUrl=jsonObject.getString("url");

                            Log.i("JSON title:",articleJsonTitle);
                            Log.i("JSON url:",articleJsonUrl);

                            newsDb.execSQL("INSERT INTO newsTable(title,url) VALUES ('"+articleJsonTitle+"','"+articleJsonUrl+"')");
                            //newsDb.execSQL("INSERT INTO newsTable(title,url) VALUES ('nishant','www.google.com')");

                        }
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            updateListView();
            super.onPostExecute(s);
        }
    }
}
