package com.tyjradio.jrdvoicerecorder.recorder.recording;



import java.io.File;

/**
 * Pcm格式的音频记录器
 *
 * @author maple
 * @time 2018/4/10.
 */
public class PcmRecorder extends BaseDataRecorder {

    public PcmRecorder(File file, AudioRecordConfig config, PullTransport pullTransport) {
        super(file, config, pullTransport);
    }

}