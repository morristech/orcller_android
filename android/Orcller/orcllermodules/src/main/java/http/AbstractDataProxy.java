package http;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/4/15.
 */
public class AbstractDataProxy {
    public void execute() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.interceptors().add(new Interceptor() {
                                          @Override
                                          public Response intercept(Interceptor.Chain chain) throws IOException {
                                              Request original = chain.request();

                                              Request request = original.newBuilder()
                                                      .header("User-Agent", "Your-App-Name")
                                                      .header("Accept", "application/vnd.yourapi.v1.full+json")
                                                      .method(original.method(), original.body())
                                                      .build();

                                              return chain.proceed(request);
                                          }
                                      }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient)
                        .build();
    }
}
