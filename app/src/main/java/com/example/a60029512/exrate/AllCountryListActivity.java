package com.example.a60029512.exrate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AllCountryListActivity extends AppCompatActivity {
    ArrayList<Item> itemList = new ArrayList<Item>();
    ItemAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_country_list);

        String url = "http://172.16.2.9:52273/allcountryinfo";
        new AllCountryInfo().execute(url, "", "");
    }

    //통신을 하기위한 클래스
    class AllCountryInfo extends AsyncTask<String, String, String> {
        ProgressDialog dialog = new ProgressDialog(AllCountryListActivity.this);

        // 네트워크 통신시 스레드를 사용하는 AsyncTask를 상속받아서 doInBackground를 사용한다
        @Override
        protected String doInBackground(String... params) {

            StringBuilder output = new StringBuilder(); //문자열을 담기 위한 객체
            try{
                URL url = new URL(params[0]); // URL화 한다.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET"); // get방식 통신
                InputStream is = conn.getInputStream(); //input스트림 개방
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8")); //문자열 셋 세팅
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line+ "\n");
                }
                conn.disconnect();

            }catch(MalformedURLException | ProtocolException exception) {
                exception.printStackTrace();
            }catch(IOException io){
                io.printStackTrace();
            }
            return output.toString();
        }

        //데이타 로딩중
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("조회중...");
            dialog.show();
        }

        //통신이 끝난후 후처리(JSON 문자열이 넘어옴)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();

            String countryList = getPreferences("countryList");
            Log.v("output=countryList=",countryList);
            boolean countryChk=false;
            itemList.clear();
            try {
                JSONArray jarray = new JSONArray(s);
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출

                    String code       = jObject.getString("num_code");
                    String alpha2     = jObject.getString("alpha2");
                    String alpha3     = jObject.getString("alpha3");
                    String kor_name   = jObject.getString("kor_name");

//                    Log.v("output:alpha2=", alpha2);
//                    Log.v("output:alpha3=", alpha3);
//                    Log.v("output:kor_name=", kor_name);
//                    if(countryList.contains(alpha3)){
//                        countryChk = true;
//                    }

                    String resName = "@drawable/"+alpha2.toLowerCase();

                    itemList.add(new Item(code, resName, alpha3, kor_name, countryList, countryChk));
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            adapter = new ItemAdapter(AllCountryListActivity.this, R.layout.activity_add_country, itemList);
            ListView listView = (ListView)findViewById(R.id.allcountryitem);
            listView.setAdapter(adapter);

        }
    }

    private void saveCountry(){
        Intent intent = new Intent(AllCountryListActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    class Item {
        String code;
        int image;
        String alpha3;
        String kor_name;
        String countryList;
        boolean selected;
        Item(String code, String resName, String alpha3, String kor_name, String countryList, boolean countryChk) {
            this.code = code;
            this.image = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());
            this.alpha3 = alpha3;
            this.kor_name = kor_name;
            this.countryList = countryList;
            this.selected = countryChk;
        }
    }

    class ItemAdapter extends ArrayAdapter {
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.activity_all_country_item, null);
            }

            ImageView additemimage = (ImageView) convertView.findViewById(R.id.additemimage);
            TextView addcountrythreeword = (TextView) convertView.findViewById(R.id.addcountrythreeword);
            TextView addcountryname = (TextView) convertView.findViewById(R.id.addcountryname);
            ImageView addcheck = (ImageView) convertView.findViewById(R.id.addcheck);

            final Item item = itemList.get(position);

//            Log.v("output:item=",getPreferences("1"+item.code)+"/"+"1"+item.code);
//            if(!getPreferences("1"+item.code).equals("1"+item.code)){
//            }

            additemimage.setImageResource(item.image);
            addcountrythreeword.setText(item.alpha3);
            addcountryname.setText(item.kor_name);

            if (item.selected == true) {
                addcheck.setImageResource(R.drawable.check);
            }else {
                addcheck.setImageResource(0);
            }
            /*if(item.countryList.contains(item.alpha3)){//체크 이미지
                addcheck.setImageResource(R.drawable.check);
            }else{
                addcheck.setImageResource(0);
            }

            /*additemimage.setId(Integer.parseInt("1"+item.code));
            addcountrythreeword.setId(Integer.parseInt("2"+item.code));
            addcountryname.setId(Integer.parseInt("3"+item.code));
            addcheck.setId(Integer.parseInt("4"+item.code));

            additemimage.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {checkImg(v.getId());}});
            addcountrythreeword.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {checkImg(v.getId());}});
            addcountryname.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {checkImg(v.getId());}});
            addcheck.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {checkImg(v.getId());}});*/

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String countryList = getPreferences("countryList");
                    if (item.selected == true) {
                        item.selected = false;
                        //국가의 위치를 찾아서 쉽표와 함께 제거한다
                        if(countryList.indexOf(item.alpha3)==0){
                            countryList = countryList.replace(item.alpha3+",", "");
                        }else{
                            countryList = countryList.replace(","+item.alpha3, "");
                        }
                    } else {
                        item.selected = true;
                        countryList = countryList+","+item.alpha3;
                    }
                    savePreferences("countryList", countryList);
                    adapter.notifyDataSetChanged();
                }
            });

            return convertView;
        }

        public ItemAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
            super(context, resource, objects);
        }
    }

/*    private void checkImg(int id){
        String code = (""+id).substring(1);
        String countryList = getPreferences("countryList");
        int countryCount = Integer.parseInt(getPreferences("countryCount"));
        TextView tx = (TextView)findViewById(Integer.parseInt("2"+code));//alpha3 가져오기위한 TextView

//        int countryCount = Integer.parseInt(getPreferences("countryCount"));//등록된 국가수

        //이미지뷰 생성
        id = Integer.parseInt("4"+code);
        ImageView mv = (ImageView)findViewById(id);

        if(countryList.contains(tx.getText().toString())){//국가제거
            //선택한 국가가 마지막 남은 2개의 국가중 하나일경우
            if(countryCount<=2){
                Toast.makeText(AllCountryListActivity.this,"최소 2개이상의 국가가 선택되어야 합니다.",Toast.LENGTH_SHORT).show();
                return;
            }else if(countryList.indexOf(tx.getText().toString())< 5){
                Toast.makeText(AllCountryListActivity.this,"대표국가는 삭제 불가능 합니다.",Toast.LENGTH_SHORT).show();
                return;
            }

            //국가의 위치를 찾아서 쉽표와 함께 제거한다
            if(countryList.indexOf(tx.getText().toString())==0){
                countryList = countryList.replace(tx.getText().toString()+",", "");
            }else{
                countryList = countryList.replace(","+tx.getText().toString(), "");
            }
            mv.setImageResource(0);
            savePreferences("countryList", countryList);
            savePreferences("countryCount", (countryCount-1)+"");

            Log.v("output:del_countryList", getPreferences("countryList"));
            Log.v("output:del_countryList", getPreferences("countryCount"));

        }else{//국가추가
            if(countryCount>=5){
                Toast.makeText(AllCountryListActivity.this,"국가 선택은 5개 까지 가능합니다.",Toast.LENGTH_SHORT).show();
                return;
            }
            countryList = countryList+","+tx.getText().toString();

            mv.setImageResource(R.drawable.check);
            savePreferences("countryList", countryList);
            savePreferences("countryCount", (countryCount+1)+"");

            Log.v("output:add_countryList", getPreferences("countryList"));
            Log.v("output:add_countryList", getPreferences("countryCount"));
        }


    }*/

    //SharedPreferences의 String 값을 리턴한다.
    private String getPreferences(String name) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString(name, "");
    }

    // 값 저장하기
    private void savePreferences(String name, String val) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(name, val);
        editor.commit();
    }
}


