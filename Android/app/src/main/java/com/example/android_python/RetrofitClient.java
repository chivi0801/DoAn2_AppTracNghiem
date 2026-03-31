package com.example.android_python;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    // LƯU Ý:
    // - Nếu dùng máy ảo (Emulator): "http://10.0.2.2:5000/"
    // - Nếu dùng máy thật: Thay bằng IP máy tính của bạn (VD: "http://192.168.1.5:5000/")
    private static final String BASE_URL = "http://10.0.2.2:5000/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // 1. Khởi tạo Logging Interceptor (Để theo dõi log API trong Logcat)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Cấu hình OkHttpClient (Thêm thời gian chờ xử lý ảnh)
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS) // Đợi kết nối 30s
                    .readTimeout(60, TimeUnit.SECONDS)    // Đợi server xử lý ảnh 60s
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            // 3. Khởi tạo Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    // Hàm tiện ích để lấy nhanh ApiService
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}