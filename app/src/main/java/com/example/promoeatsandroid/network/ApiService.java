package com.example.promoeatsandroid.network;

import com.example.promoeatsandroid.models.AuthResponse;
import com.example.promoeatsandroid.models.LoginRequest;
import com.example.promoeatsandroid.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("api/auth/authenticate")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @GET("api/auth/logout")
    Call<Void> logout(@Header("Authorization") String token);
}
