package com.tyjradio.jrdvoicerecorder.recorder.recording;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * 录音参数配置
 *
 * @author maple
 * @time 2018/4/10.
 */
public interface AudioRecordConfig {

    int channelPositionMask();

    int audioSource();

    int frequency();

    int audioEncoding();

    byte bitsPerSample();

    /**
     * Application should use this default implementation of {@link AudioRecordConfig} to configure
     * the Audio Record Source.
     */
    class Default implements AudioRecordConfig {
        private final int audioSource;
        private final int channelPositionMask;
        private final int frequency;
        private final int audioEncoding;

        public Default() {
            this.audioSource = MediaRecorder.AudioSource.MIC;
            this.audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
            this.channelPositionMask = AudioFormat.CHANNEL_IN_MONO;
            this.frequency = 44100;
        }

        public Default(int audioSource, int audioEncoding, int channelPositionMask, int frequency) {
            this.audioSource = audioSource;
            this.audioEncoding = audioEncoding;
            this.channelPositionMask = channelPositionMask;
            this.frequency = frequency;
        }

        @Override
        public int channelPositionMask() {
            return channelPositionMask;
        }

        @Override
        public int audioSource() {
            return audioSource;
        }

        @Override
        public int frequency() {
            return frequency;
        }

        @Override
        public int audioEncoding() {
            return audioEncoding;
        }

        @Override
        public byte bitsPerSample() {
            if (audioEncoding == AudioFormat.ENCODING_PCM_16BIT) {
                return 16;
            } else if (audioEncoding == AudioFormat.ENCODING_PCM_8BIT) {
                return 8;
            } else {
                return 16;
            }
        }
    }
}
