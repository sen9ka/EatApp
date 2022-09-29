package com.senya.androideatitv2client.Remote;

import com.senya.androideatitv2client.Model.FCMResponse;
import com.senya.androideatitv2client.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA-2dDAOU:APA91bFt8Yb8RqnXTJ5NCCG80EYUMeTWxA0sCb_DDgAAUFvckGubsg67BnVHa4w-35SlcauLaLNw5L_9IujgkJbCo0EfUv5qOK2OznUlxEB_FLHg9FXOl9QzEMo-ixxNJ26poXkd6Ix6"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
