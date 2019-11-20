package com.tyjradio.jrdvoicerecorder.utils;

import java.util.ArrayList;

public class OpusUtils {
    static{
        System.loadLibrary("opusJni");
    }

    public native long createEncoder(int sampleRateInHz, int channelConfig, int complexity);
    public native long createDecoder(int sampleRateInHz, int channelConfig);
    public native int encode(long handle, short[] lin, int offset, byte[] encoded);
    public native int decode(long handle, byte[] encoded, short[] lin);
    public native void destroyEncoder(long handle);
    public native void destroyDecoder(long handle);
}
