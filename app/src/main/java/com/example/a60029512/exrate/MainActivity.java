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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.a60029512.exrate.R.id.countryamt1;

public class MainActivity extends AppCompatActivity {

    ArrayList<Item> itemList = new ArrayList<Item>();
    ItemAdapter adapter = null;
    DecimalFormat format = new DecimalFormat(".####");

    int[] countrynameAry = {R.id.countryname1, R.id.countryname2};
    int[] currencycodeAry = {R.id.currencycode1, R.id.currencycode2};
    int[] countryimageAry   = {R.id.countryimage1, R.id.countryimage2};
    int[] countrythreewordAry  = {R.id.countrythreeword1, R.id.countrythreeword2};
    int[] countryamtAry  = {countryamt1, R.id.countryamt2};

    String flagstr = null;
    String endChk = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText onEditText1 = (EditText)findViewById(countryamtAry[0]);
        EditText onEditText2 = (EditText)findViewById(countryamtAry[1]);
        onEditText1.addTextChangedListener(textWatcherInput);
        onEditText2.addTextChangedListener(textWatcherInput);

        ImageView iv = (ImageView)findViewById(R.id.changeCountry);
        iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
               changeCountry();
            }
        });

        String url = "";

        //SharedPreferences에 값이 저장되어있는지 확인하여 없으면 초기화하고 있으면 값을 가져온다.
        if (getPreferences("countryCount").equals("")) {
            //url = "http://127.0.0.1:52273/getcountryinfo?country='KR','US'";
//            url = "https://api.manana.kr/exchange/rate/KRW/USD,JPY,CAD.json";
            url = "http://172.16.2.9:52273/getcountryinfo?country='KOR','JPN'";
            flagstr = "getcountryinfo";
            savePreferences("countryList", "KOR,JPN");
            savePreferences("currencyList", "KRW,JPY");
        } else {
            String countryList = getPreferences("countryList");
            countryList = countryList.replaceAll(",","','");
            url = "http://172.16.2.9:52273/getcountryinfo?country='"+countryList+"'";
            flagstr = "getcountryinfo";
        }

        new HttpConn().execute(url, "", "");
    }



    //통신을 하기위한 클래스
    class HttpConn extends AsyncTask<String, String, String> {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        // 네트워크 통신시 스레드를 사용하는 AsyncTask를 상속받아서 doInBackground를 사용한다
        @Override
        protected String doInBackground(String... params) {
            Log.v("output=url:", params[0]);
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
//                Log.v("output=doInBackground:", output.toString());

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

            Log.v("output",s);

            if(flagstr.equals("getcountryinfo")) {
                String[] countryListAry=null;
                String countryList ="";
                String currencyList="";

                try {
                    JSONArray jarray = new JSONArray(s);

                    savePreferences("countryCount", jarray.length()+"");
                    countryListAry = getPreferences("countryList").split(",");
                    itemList.clear();

                    for (int i=0 ; i<countryListAry.length ; i++) {
                        for(int k=0 ; k<jarray.length() ; k++){
                            JSONObject jObject = jarray.getJSONObject(k);  // JSONObject 추출

                            String code       = jObject.getString("currency_code");
                            String alpha2     = jObject.getString("alpha2");
                            String alpha3     = jObject.getString("alpha3");
                            String kor_name   = jObject.getString("kor_name");
                            String resName = "@drawable/"+alpha2.toLowerCase();

                            if(i<2){
                                if(countryListAry[i].equals(alpha3)) {
                                    //상단 대표국가에 추가한다
                                    setTextViewVal(countrynameAry[i], kor_name);
                                    setTextViewVal(currencycodeAry[i], code);
                                    setImageView(countryimageAry[i], resName);
                                    setTextViewVal(countrythreewordAry[i], alpha3);
                                }
                            }else{
                                //itemList.add(new Item(code, resName, alpha3, kor_name, countryList));
                            }

                            if(currencyList.equals("")){
                                currencyList = code;
                            }else{
                                if(!currencyList.contains(code)){
                                    currencyList = currencyList + "," + code;
                                }
                            }
                        }
                    }
                    itemList.add(new Item("null", "", "", "", ""));

                }catch(JSONException e){
                    e.printStackTrace();
                }

                adapter = new ItemAdapter(MainActivity.this, R.layout.activity_add_country, itemList);
                ListView listView = (ListView)findViewById(R.id.currencylistitem);
                listView.setAdapter(adapter);

                flagstr = "getcurrencyinfo";
                new HttpConn().execute(getSearchUrl(),"","");

            }else if(flagstr.equals("getcurrencyinfo")) {

                endChk = "";

                try {
                    JSONArray jarray = new JSONArray(s);
                    double stdCurrency = 0;
                    String[] currencyList = getPreferences("currencyList").split(",");

                    if( getEditTextVal(countryamtAry[0]).equals("")) {
                        setEditTextVal(countryamtAry[0], 1 + "");
                        savePreferences(currencyList[0] + "", "1");
                        stdCurrency = 1;
                    }else{
                        stdCurrency = Double.parseDouble(getEditTextVal(countryamtAry[0]));
                        savePreferences(currencyList[0]+"", stdCurrency+"");
                    }

                    for (int i=0; i<jarray.length(); i++) {
                        JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출

                        String date = jObject.getString("date");
                        String name = jObject.getString("name");
                        double rate = Double.parseDouble(jObject.getString("rate"));
//                        double rate = Double.parseDouble(String.format("%.4f", jObject.getString("rate")));

//                        Log.v("output:date=", date);
//                        Log.v("output:name=", name);
//                        Log.v("output:rate=", rate+"");
Log.v("output",rate+"");
                        if(1 > rate){
                            savePreferences(currencyList[i+1], format.format(rate));
                            rate = Double.parseDouble(format.format(rate*stdCurrency));

                        }else if(1 < rate){
                            //실제 환율 기준금액 1원일때 금액
                            String strRate = format.format(1*rate);
                            savePreferences(currencyList[i+1], "0"+strRate);

                            //환율 계산
                            rate = Double.parseDouble(strRate)*stdCurrency;

                        }else if(1 == rate){
                            rate = rate*stdCurrency;
                        }
                        Log.v("output2",rate+"");
                        if((i+1)==1){
                            if(rate < 1){
                                setEditTextVal(countryamtAry[1], "0"+format.format(rate));
                            }else {
                                setEditTextVal(countryamtAry[1], format.format(rate));
                            }
                        }else{

                        }
                    }

                }catch(JSONException e){
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                endChk = "true";
            }
        }
    }

    class ItemAdapter extends ArrayAdapter {
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (itemList.get(position).code.equals("null")) {
                    convertView = layoutInflater.inflate(R.layout.activity_add_country, null);
                } else {
                    convertView = layoutInflater.inflate(R.layout.activity_currency_list_item, null);
                }
            }
            if (itemList.get(position).code.equals("null")) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, AllCountryListActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                Item item = itemList.get(position);

                ImageView additemimage = (ImageView) convertView.findViewById(R.id.additemimage);
                TextView addcountrythreeword = (TextView) convertView.findViewById(R.id.addcountrythreeword);
                TextView addcountryname = (TextView) convertView.findViewById(R.id.addcountryname);

                additemimage.setImageResource(item.image);
                addcountrythreeword.setText(item.alpha3);
                addcountryname.setText(item.kor_name);
            }
            return convertView;
        }

        public ItemAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
            super(context, resource, objects);
        }
    }

    class Item {
        String code;
        int image;
        String alpha3;
        String kor_name;
        String countryList;
        Item(String code, String resName, String alpha3, String kor_name, String countryList) {
            this.code = code;
            this.image = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());
            this.alpha3 = alpha3;
            this.kor_name = kor_name;
            this.countryList = countryList;
        }
    }

    public String getSearchUrl(){
        String currencyList = getPreferences("currencyList");
        String url = "https://api.manana.kr/exchange/rate/"+
                currencyList.substring(0,3)+"/"+
                currencyList.substring(4)+".json";
        return url;
    }

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

    //////////////////////////////////////////////////////////////////////////////////

    TextWatcher textWatcherInput = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            Log.i("onTextChanged", s.toString());
            calc();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
            Log.i("beforeTextChanged", s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            Log.i("afterTextChanged", s.toString());
        }
    };

    public void calc(){
        final View view = getCurrentFocus();
        int id = view.getId();

        if(endChk.equals("")){
            return;
        }

//        int countryCount = Integer.parseInt(getPreferences("countryCount"));
        String[] countryList = getPreferences("countryList").split(",");
        String[] currencyList = getPreferences("currencyList").split(",");

        double stdAmt=0;
        double subAmt=0;
        String setAmt="";
        for(int i=0 ; i<countryList.length ; i++){
            if(id == countryamtAry[0]){
                stdAmt = Double.parseDouble(getEditTextVal(id));
                subAmt = Double.parseDouble(getPreferences(currencyList[1]));
                setAmt = format.format(stdAmt*subAmt);
                if(!setAmt.equals(getEditTextVal(countryamtAry[1]))) {
                    setEditTextVal(countryamtAry[1], format.format(stdAmt * subAmt));
                }

            }else if(id == countryamtAry[1]){
                stdAmt = Double.parseDouble(getEditTextVal(id));
                subAmt = Double.parseDouble(getPreferences(currencyList[0]));
                setAmt = format.format(stdAmt*subAmt);
                if(!setAmt.equals(getEditTextVal(countryamtAry[0]))) {
                    setEditTextVal(countryamtAry[0], format.format(stdAmt * subAmt));
                }
            }

        }
    }

    private void changeCountry(){
        String[] countryList = getPreferences("countryList").split(",");
        String[] currencyList = getPreferences("currencyList").split(",");
        String url = "http://172.16.2.9:52273/getcountryinfo?country='"+countryList[1]+"','"+countryList[0]+"'";
        Log.v("output",url+"");
        savePreferences("countryList", countryList[1]+","+countryList[0]);
        savePreferences("currencyList", currencyList[1]+","+currencyList[0]);
        flagstr = "getcountryinfo";
        new HttpConn().execute(url, "", "");
    }
    //////////////////////////////////////////////////////////////////////////////////

    // TextView의 값을 리턴한다
    //인자값 : TextView
    private String getTextViewVal( int id){
        TextView tx = (TextView)findViewById(id);
        return tx.getText().toString();
    }

    //TextView의 값을 입력한다
    //인자값 : TextView, 입력할 문자
    private void setTextViewVal( int id, String val){
        TextView tx = (TextView)findViewById(id);
        tx.setText(val);
    }

    //EditText의 값을 리턴한다
    //인자값 : EditText
    private String getEditTextVal(int id){
        EditText ex = (EditText)findViewById(id);
        return ex.getText().toString();
    }

    //EditText의 값을 입력한다
    //인자값 : EditText, 입력숫자
    private void setEditTextVal( int id, String val){
        EditText ex = (EditText)findViewById(id);
        ex.setText(val);
    }

    private void setImageView( int id, String resName){
        ImageView mv = (ImageView)findViewById(id);
        int resID = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());//getActivity().getPackageName()
        mv.setImageResource(resID);
    }


}