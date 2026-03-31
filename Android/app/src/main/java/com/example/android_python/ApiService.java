package com.example.android_python;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    /**
     * Kiểm tra kết nối với Server
     * Tương ứng với @app.route("/scan") trong Python
     */
    @GET("scan")
    Call<ResponseBody> checkConnection();

    /**
     * Gửi ảnh bài thi lên Server để xử lý và nhận lại ảnh kết quả
     * Tương ứng với @app.route("/predict", methods=["POST"])
     * * @Part file: Phải khớp với key 'request.files.get("file")' bên Python
     */
    @Multipart
    @POST("predict")
    Call<ResponseBody> uploadExam(@Part MultipartBody.Part file);
}