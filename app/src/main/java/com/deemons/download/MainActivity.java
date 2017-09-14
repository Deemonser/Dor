package com.deemons.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deemons.dor.Dor;
import com.deemons.dor.error.DorObserver;
import com.deemons.dor.utils.RxUtils;

import io.reactivex.annotations.NonNull;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Dor.init(Dor.builder().context(this).baseUrl(API.HOST).logLevel(HttpLoggingInterceptor.Level.BODY));

        API api = Dor.getAPI(API.class);

        api.getGirlList(8, 8)
                .compose(RxUtils.<String>io_main())
                .subscribe(new DorObserver<String>() {
                    @Override
                    public void onNext(@NonNull String s) {

                    }
                });


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://gank.io/api/")
                .build();


        API apiServer = retrofit.create(API.class);

        Call<ResponseBody> girlPic = apiServer.getGirlPic(8, 8);


        girlPic.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });


        //        Dor.download("http://imtt.dd.qq.com/16891/901DE537371541B8482D4B18CE8BF806.apk?fsname=com.tencent.tmgp.sgame_1.20.1.21_20012107.apk&csr=1bbd")
        //                .subscribeOn(Schedulers.io())
        //                .observeOn(AndroidSchedulers.mainThread())
        //                .subscribe(new Observer<Status>() {
        //                    @Override
        //                    public void onSubscribe(@NonNull Disposable d) {
        //
        //                    }
        //
        //                    @Override
        //                    public void onNext(@NonNull Status status) {
        //                        Log.d("MainActivity", status.toString());
        //                    }
        //
        //                    @Override
        //                    public void onError(@NonNull Throwable e) {
        //                        e.printStackTrace();
        //                    }
        //
        //                    @Override
        //                    public void onComplete() {
        //                        Log.d("MainActivity", "onComplete");
        //                    }
        //                });


    }


}
