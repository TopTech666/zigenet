package com.ecar.ecarnetwork.http.converter;

import android.util.Log;

import com.ecar.ecarnetwork.bean.ResBase;
import com.ecar.ecarnetwork.http.exception.InvalidException;
import com.ecar.ecarnetwork.http.util.TagLibUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 自定义响应体的转换器
 */
public class ResponseConverter<T> implements Converter<ResponseBody, T> {

    /**
     * 1.是否分出 ResponseBody value =null 时
     * 选择1.1 抛出自定义NetException。异常判断 ，判断instanceof NetException
     * 选择1.2 判断Http 异常。根据retrofit自动抛出的（疑问，是解析抛出的？还是异常直接抛出？）
     * <p>
     * 2.1 策略1：直接返回json解析。订阅者subscriber 中判断state 自处理
     * <p>
     * 2.2 策略2：根据泛型解析json后 (已采用)
     * 1.1 判断state 成功/失败 （失败抛ResultException，订阅者在OnError中自处理）
     * 成功-->返回json解析（直接返回反序列话后bean 或者二次解析，看需求）
     * 失败-->抛失败异常（1.state--目测一体化没什么用，2.msg）
     * <p>
     * --------->2*2 四种情况
     */
    public static final String TAG = "ResponseConverter";

    private final Gson gson;
    private final Type type;
    private final Type mArrayType;

    ResponseConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
        mArrayType = new TypeToken<List<T>>() {
        }.getType();
    }


    @Override
    public T convert(final ResponseBody value) throws IOException {
        ResBase base = null;
        ArrayList<T>  arrayList = null;
        String response = null;
        try {
            response = value.toString();
        } catch (Exception e) {
            e.printStackTrace();
            TagLibUtil.showLogDebug("请求成功，获取返回值失败");
            return null;
        }
        Log.i("thread", Thread.currentThread().getName());
        try {
            if (response.startsWith("[")){
                arrayList =  gson.fromJson(response, mArrayType);
            }else {
                base = gson.fromJson(response, type);
            }
//            base.head=value.header(RESPONES_HEADERNAME,"");
//            base.netCode=value.code();
            /**
             * 保存utv
             */
            if (base == null) {//非成功
                throw new InvalidException(InvalidException.FLAG_ERROR_RELOGIN, "网络错误", (ResBase)base);
            }
        } finally {
            value.close();
        }
//        if(base!=null&&!TextUtils.isEmpty(response)){
//            base.jsonStr=response;
//        }
        return (T)(response.startsWith("[")?arrayList: base );
    }


}
