package com.readdle.swiftjava.sample;

import com.readdle.codegen.anotation.SwiftCallbackFunc;
import com.readdle.codegen.anotation.SwiftDelegate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;

@SwiftDelegate(protocols = {"SampleDelegate"})
public abstract class SampleDelegateAndroid {

    // Swift JNI private native pointer
    private long nativePointer = 0L;

    // Swift JNI init method
    public native void init();

    // Swift JNI release method
    public native void release();


    public SampleDelegateAndroid() {
        init();
    }

    SampleValue sampleValue = null;

    @SwiftCallbackFunc("setSampleValue(value:)")
    public void setSampleValue(@Nullable SampleValue value) {
        sampleValue = value;
        onSetSampleValue(value);
        if (value != null) {
            Log.i("TAG", value.toString());
        }
    }

    @NonNull @SwiftCallbackFunc
    public SampleValue getSampleValue() {
        return sampleValue;
    }

    @SwiftCallbackFunc
    public static void setTimestamp(@NonNull Long value) {
        Log.i("TAG", value.toString());
    }

    @NonNull @SwiftCallbackFunc
    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Nullable @SwiftCallbackFunc
    public SampleValue funcWithNil() {
        return null;
    }

    @SwiftCallbackFunc
    public void throwableFunc(@NonNull Boolean flag) throws Exception {
        if (flag) {
            throw new IllegalArgumentException("Exception");
        }
    }

    @SwiftCallbackFunc @NonNull
    public String throwableFuncWithReturnType(@NonNull Boolean flag) throws Exception {
        if (flag) {
            throw new IllegalArgumentException("Exception");
        }
        else {
            return "throwableFuncWithReturnType";
        }
    }

    @SwiftCallbackFunc @NonNull
    public ByteBuffer funcWithData() {
        byte[] tenBytes = new byte[10];
        return ByteBuffer.wrap(tenBytes);
    }

    abstract void onSetSampleValue(SampleValue value);

}
