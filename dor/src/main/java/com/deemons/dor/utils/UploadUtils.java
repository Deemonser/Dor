package com.deemons.dor.utils;

import android.support.annotation.StringDef;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * author： deemons
 * date:    2017/8/19
 * desc:
 */

public class UploadUtils {


    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String APPLICATION_JSON = "application/json; charset=utf-8";
    public static final String APPLICATION_OTCET_STREAM = "application/otcet-stream";

    @StringDef({MULTIPART_FORM_DATA, APPLICATION_JSON, APPLICATION_OTCET_STREAM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NormalMediaType {
    }


    public static RequestBody getRequestBody(File file) {
        return getRequestBody(file, MULTIPART_FORM_DATA);
    }


    public static RequestBody getRequestBody(File file, @NormalMediaType String mediaType) {
        return RequestBody.create(MediaType.parse(mediaType), file);
    }

    public static MultipartBody.Part getBodyPart(String name, File file) {
        return getBodyPart(name, file, MULTIPART_FORM_DATA);
    }

    public static MultipartBody.Part getBodyPart(String name, File file, @NormalMediaType String mediaType) {
        return MultipartBody.Part.createFormData(name, file.getName(), getRequestBody(file, mediaType));
    }

}
