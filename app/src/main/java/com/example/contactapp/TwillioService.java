package com.example.contactapp;

import android.util.Log;

import com.hihi.twiliosms.TwilioMessage;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;



 class TwilioService {
    public static String ACCOUNT_SID ="AC11174b81d84a5a2bf91bcf6838861622", AUTH_TOKEN = "8f721b31ba72c37bed0c08b77114f222";
    public static void sendSms(String phoneNumber,String message){
        OkHttpClient client = new OkHttpClient.Builder().build();
        client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("Authorization", Credentials.basic(ACCOUNT_SID, AUTH_TOKEN))
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.twilio.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TwilioApi twilioApi = retrofit.create(TwilioApi.class);
        Call<TwilioMessage> call = twilioApi.sendSms(
                ACCOUNT_SID,
                phoneNumber,
                "+12029153949",
                message
        );
        call.enqueue(new Callback<TwilioMessage>() {
            @Override
            public void onResponse(Call<TwilioMessage> call, Response<TwilioMessage> response) {
                Log.d("TWILIO API", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<TwilioMessage> call, Throwable t) {
                Log.d("TWILIO API", "onResponse: " + t.getMessage());

            }
        });

    }
    public interface TwilioApi {
        @FormUrlEncoded
        @POST("/2010-04-01/Accounts/{accountSid}/Messages.json")
        Call<TwilioMessage> sendSms(
                @Path("accountSid") String accountSid,
                @Field("To") String to,
                @Field("From") String from,
                @Field("Body") String body
        );
    }
}