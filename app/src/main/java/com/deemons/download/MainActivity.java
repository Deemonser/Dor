package com.deemons.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deemons.dor.Net;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Net.init(Net.builder().context(this));
//
//
//        Net.getDownload().download("http://imtt.dd.qq.com/16891/901DE537371541B8482D4B18CE8BF806.apk?fsname=com.tencent.tmgp.sgame_1.20.1.21_20012107.apk&csr=1bbd")
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
//
//                    }
//                });

    }
}