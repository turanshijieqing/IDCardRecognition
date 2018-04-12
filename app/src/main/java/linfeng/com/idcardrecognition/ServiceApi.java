package linfeng.com.idcardrecognition;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

/**
 * Created by lf
 * on 2018/4/11
 */

public interface ServiceApi {
    String HOST =  "http://dm-51.data.aliyun.com/" ;

    @POST("rest/160601/ocr/ocr_idcard.json")
    Observable<ResponseBody> recordCD(@HeaderMap Map<String, String> headers, @Body RequestObj requestObj);
}
