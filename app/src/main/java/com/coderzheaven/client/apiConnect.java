package com.coderzheaven.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import retrofit2.http.POST;

public class apiConnect extends AppCompatActivity {
    String url = "https://run.mocky.io/";
private EditText id, title, lat, lon;
private Button updateBtn;
private TextView responseTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_connect);

        id= findViewById(R.id.getId);
        title= findViewById(R.id.title);
        lat= findViewById(R.id.getLat);
        lon= findViewById(R.id.getLong);
        updateBtn =findViewById(R.id.buttonApi);
        responseTV = findViewById(R.id.textViewResponse);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking if the edit text is empty or not.
                if (TextUtils.isEmpty(id.getText().toString()) && TextUtils.isEmpty(title.getText().toString())&& TextUtils.isEmpty(lat.getText().toString())&& TextUtils.isEmpty(lon.getText().toString())) {

                    // displaying a toast message if the edit text is empty.
                    Toast.makeText(apiConnect.this, "Please enter your data..", Toast.LENGTH_SHORT).show();
                    return;
                }
                String value= id.getText().toString();
                int finalValue=Integer.parseInt(value);
                String value1= lat.getText().toString();
                int finalValue1=Integer.parseInt(value);
                String value2= lon.getText().toString();
                int finalValue2=Integer.parseInt(value);
                // calling a method to update data in our API.
                callPUTDataMethod(finalValue, title.getText().toString(),finalValue1, finalValue2 );
            }
        });



    }

    private void callPUTDataMethod(int id, String title, int lat, int lon) {



        // on below line we are creating a retrofit
        // builder and passing our base url
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)

                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())

                // at last we are building our retrofit builder.
                .build();

        // below the line is to create an instance for our retrofit api class.
        RetrofitApi retrofitAPI = retrofit.create(RetrofitApi.class);

        // passing data from our text fields to our modal class.
        DataModel2 modal = new DataModel2(id, title, lat, lon);

        // calling a method to create an update and passing our modal class.
        Call<DataModel2> call = retrofitAPI.updateData(modal);

        // on below line we are executing our method.
        call.enqueue(new Callback<DataModel2>() {
            @Override
            public void onResponse(Call<DataModel2> call, Response<DataModel2> response) {
                // this method is called when we get response from our api.
                Toast.makeText(apiConnect.this, "Data updated to API", Toast.LENGTH_SHORT).show();





                // we are getting a response from our body and
                // passing it to our modal class.
                DataModel2 responseFromAPI = response.body();

                // on below line we are getting our data from modal class
                // and adding it to our string.
                String responseString = "Response Code : " + response.code() + "\nid : " + responseFromAPI.getid() + "\n" + "title : " + responseFromAPI.getTitle()+"\n" + "lat : " + responseFromAPI.getlat()+"\n" + "lon : " + responseFromAPI.getlon();

                // below line we are setting our string to our text view.
                responseTV.setText(responseString);
            }

            @Override
            public void onFailure(Call<DataModel2> call, Throwable t) {

                // setting text to our text view when
                // we get error response from API.
                responseTV.setText("Error found is : " + t.getMessage());
            }
        });
    }


}