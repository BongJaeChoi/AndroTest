package org.todaysays;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by BongJae on 2015-08-08.
 * php연결하고 텍스트 받아오는 클래스
 */
public class PhpDown extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... urls) {
        StringBuffer jsonHtml = new StringBuffer();
        try {
            // 연결 url 설정
            URL url = new URL(urls[0]);
            // 커넥션 객체 생성
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 연결되었으면.
            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setUseCaches(false);
                // 연결되었음 코드가 리턴되면.
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    for (; ; ) {
                        // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                        String line = br.readLine();
                        if (line == null) break;
                        // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                        jsonHtml.append(line + "\n");
                    }
                    br.close();
                }
                conn.disconnect();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonHtml.toString();
    }

    protected void onPostExecute(String str) {
        String imgurl;
        String txt1;
        String txt2;
        try {

            JSONObject root = new JSONObject(str);
            JSONArray ja = root.getJSONArray("results");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                imgurl = jo.getString("imgurl");
                txt1 = jo.getString("txt1");
                txt2 = jo.getString("txt2");
                //listItem.add(new ListItem(imgurl,txt1,txt2));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //txtView.setText("urlimg :"+listItem.get(0).getData(0)+"\ntxt1:"+ listItem.get(0).getData(1)+"\ntxt2:"+listItem.get(0).getData(2));
        //테스트
    }

}