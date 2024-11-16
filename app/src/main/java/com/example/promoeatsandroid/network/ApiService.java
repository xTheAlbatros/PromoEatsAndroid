package com.example.promoeatsandroid.network;

import com.example.promoeatsandroid.models.AuthResponse;
import com.example.promoeatsandroid.models.LoginRequest;
import com.example.promoeatsandroid.models.User;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.models.Promotion;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import java.util.List;


public interface ApiService {
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("api/auth/authenticate")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @GET("api/auth/logout")
    Call<Void> logout(@Header("Authorization") String token);

    @GET("api/restaurants")
    Call<List<Restaurant>> getRestaurants(@Header("Authorization") String token);

    @GET("api/restaurant/{id}/promotions")
    Call<List<Promotion>> getPromotions(@Header("Authorization") String token, @Path("id") int restaurantId);
}
