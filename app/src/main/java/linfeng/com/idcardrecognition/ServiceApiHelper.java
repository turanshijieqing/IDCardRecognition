package linfeng.com.idcardrecognition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lf
 * on 2018/4/11
 */

public class ServiceApiHelper {

    private final int READ_TIMEOUT = 20000;
    private final int CONNECT_TIMEOUT = 20000;
    private Logger mLogger = Logger.getLogger();
    private ServiceApi service;
    private static final ServiceApiHelper INSTANCE = new ServiceApiHelper();

    private HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            try {
                String text = URLDecoder.decode(message, "utf-8");
                mLogger.d("-----request interceptor log is %s ", text);
            } catch (Exception e) {
                mLogger.d("-----request interceptor log is %s ", message);
            }
        }
    });

    private ServiceApiHelper() {
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            mLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClient.addInterceptor(mLoggingInterceptor);
        }
        OkHttpClient okHttpClientBuild = okHttpClient.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();

        service = new Retrofit.Builder()
                .client(okHttpClientBuild)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ServiceApi.HOST)
                .build().create(ServiceApi.class);
    }

    public static ServiceApi getApiService() {
        return INSTANCE.service;
    }

}
