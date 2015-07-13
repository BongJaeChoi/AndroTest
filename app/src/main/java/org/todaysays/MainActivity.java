package org.todaysays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {

    // View 관련 변수
    private Button iv_upload_image;
    private TextView txtView;
    private Button btn_upload_image;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    ArrayList<ImageView> imageView = new ArrayList<ImageView>();

    // 서버와 json 형식으로 통신 하기위한 변수
    String imgUrl = "http://128.199.102.18/Mapics/appimg/";
    String serverUrl = "http://128.199.102.18/Mapics/View/appdata.php";

    back imgTask1;
    back imgTask2;
    back imgTask3;
    back imgTask4;
    phpDown task;
    ArrayList<ListItem> listItem= new ArrayList<ListItem>();
    int i=0;

    // 서버로 업로드할 파일관련 변수
    public String uploadFilePath;
    public String uploadFileName;
    private int REQ_CODE_PICK_PICTURE = 1;

    // 파일을 업로드 하기 위한 변수 선언
    private int serverResponseCode = 0;

    // GPSTracker class
    private GpsInfo gps;
    private TextView txtLat;
    private TextView txtLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 변수 초기화
        InitVariable();
    }

    // 초기화
    private void InitVariable() {
        // 이미지를 넣을 뷰
        task = new phpDown();
        imgTask1 = new back();
        imgTask2 = new back();
        imgTask3 = new back();
        imgTask4 = new back();
        imageView1 = (ImageView)findViewById(R.id.imageView1);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        imageView3 = (ImageView)findViewById(R.id.imageView3);
        imageView4 = (ImageView)findViewById(R.id.imageView4);

        imageView.add(imageView1);
        imageView.add(imageView2);
        imageView.add(imageView3);
        imageView.add(imageView4);

        // JSON 통신 테스트
        task.execute(serverUrl);
        // img 받기
        // 성능이슈 존재. 너무 늦게 받아옴
        // 이미지가 직렬처리되기때문에 상당히느려짐
        imgTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,imgUrl + "img2.bmp");
        imgTask2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,imgUrl + "img3.bmp");
        imgTask3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,imgUrl + "img4.bmp");
        imgTask4.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,imgUrl + "img5.bmp");
//        imgTask1.execute(imgUrl + "img2.bmp");
//        imgTask2.execute(imgUrl + "img3.bmp");
//        imgTask3.execute(imgUrl + "img4.bmp");
//        imgTask4.execute(imgUrl + "img5.bmp");

        // 동작 버튼
        txtView = (TextView)findViewById(R.id.txtView);
        iv_upload_image = (Button) findViewById(R.id.iv_upload_image);
        iv_upload_image.setOnClickListener(this);
        btn_upload_image = (Button) findViewById(R.id.btn_upload_image);
        btn_upload_image.setOnClickListener(this);

        // gps 표시
        txtLat = (TextView) findViewById(R.id.txtLat);
        txtLon = (TextView) findViewById(R.id.txtLon);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_upload_image:
                // 사진 가져오기
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(MediaStore.Images.Media.CONTENT_TYPE);
                i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // images on the SD card.

                // 결과를 리턴하는 Activity 호출
                startActivityForResult(i, REQ_CODE_PICK_PICTURE);

                // 사진 가져올때 gps 위치
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    txtLat.setText(String.valueOf(latitude));
                    txtLon.setText(String.valueOf(longitude));

                    Toast.makeText(
                            getApplicationContext(),
                            "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                            Toast.LENGTH_LONG).show();
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
                break;
            case R.id.btn_upload_image:
                //이미지 디코딩 작업 구현 필요
                //통짜로 올릴수 없음 4096x4096 초과 업로드 불가
                if (uploadFilePath != null) {
                    UploadImageToServer uploadimagetoserver = new UploadImageToServer();
                    uploadimagetoserver.execute("http://128.199.102.18/Mapics/View/ImgUploadToServer.php");
                } else {
                    Toast.makeText(MainActivity.this, "You didn't insert any image", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private class back extends AsyncTask<String, Void, Bitmap>{
        Bitmap bmImg;
        @Override
        protected Bitmap doInBackground(String... urls) {
            // TODO Auto-generated method stub
            try{
                URL myFileUrl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                //String json = DownloadHtml("http://117.16.243.116/appdata.php");
                InputStream is = conn.getInputStream();

                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e){
                e.printStackTrace();
            }
            return bmImg;
        }

        protected void onPostExecute(Bitmap img){
            imageView.get(i++).setImageBitmap(bmImg);
        }
    }

    private class phpDown extends AsyncTask<String, Integer,String>{
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder jsonHtml = new StringBuilder();
            try{
                // 연결 url 설정
                URL url = new URL(urls[0]);
                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // 연결되었으면.
                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    // 연결되었음 코드가 리턴되면.
                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for(;;){
                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                            String line = br.readLine();
                            if(line == null) break;
                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return jsonHtml.toString();
        }

        protected void onPostExecute(String str){
            String imgurl;
            String txt1;
            String txt2;
            try{

                JSONObject root = new JSONObject(str);
                JSONArray ja = root.getJSONArray("results");
                for(int i=0; i<ja.length(); i++){
                    JSONObject jo = ja.getJSONObject(i);
                    imgurl = jo.getString("imgurl");
                    txt1 = jo.getString("txt1");
                    txt2 = jo.getString("txt2");
                    listItem.add(new ListItem(imgurl,txt1,txt2));
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            txtView.setText("urlimg :"+listItem.get(0).getData(0)+"\ntxt1:"+ listItem.get(0).getData(1)+"\ntxt2:"+listItem.get(0).getData(2));
        }
    }


    // ==================================== 사진을 불러오는 소스코드 ============================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_PICK_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String path = getPath(uri);
                String name = getName(uri);

                uploadFilePath = path;
                uploadFileName = name;

                Bitmap bit = BitmapFactory.decodeFile(path);
                imageView.get(0).setImageBitmap(bit);
            }
        }
    }

    // 실제 경로 찾기
    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // 파일명 찾기
    private String getName(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns.DISPLAY_NAME};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // uri 아이디 찾기
    private String getUriId(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns._ID};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // ============================== 사진을 서버에 전송하기 위한 스레드 ========================
    private class UploadImageToServer extends AsyncTask<String, String, String> {
        ProgressDialog mProgressDialog;
        String fileName = uploadFilePath;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 10240 * 10240;
        File sourceFile = new File(uploadFilePath);

        @Override
        protected void onPreExecute() {
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Loading...");
            mProgressDialog.setMessage("Image uploading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... serverUrl) {
            if (!sourceFile.isFile()) {
                runOnUiThread(new Runnable() {
                    public void run() {}
                });
                return null;
            } else {
                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(serverUrl[0]);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    // 읽기,쓰기 허용, 캐시사용 안함
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    // 전송 타입
                    conn.setRequestMethod("POST");
                    // 헤더설정
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    // 이미지 전송
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    if (serverResponseCode == 200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "File Upload Completed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        Toast.makeText(MainActivity.this, serverResponseCode, Toast.LENGTH_SHORT).show();
                    }
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            } // End else block
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressDialog.dismiss();
        }
    }
}


