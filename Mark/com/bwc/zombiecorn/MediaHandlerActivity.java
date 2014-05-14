package com.bwc.zombiecorn;

import android.app.Activity;
import android.media.*;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class MediaHandlerActivity extends Activity {

    private final static String REMOTE_AUDIO_URL = "http://a.tumblr.com/tumblr_lxhtvtiAKo1qmw162o1.mp3";

    private final static int MIN_BUFFER_SIZE = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private ExtAudioRecorder extAudioRecorder;
    private String fileName;
    private String wavFileName;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private AudioTrack audioTrack;
    private final MediaHandler handler = MediaHandler.getInstance();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp";
//        wavFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.wav";
    }

    public void playLocalAudio(final View view) {
        if (null == this.mediaPlayer) {
            this.mediaPlayer = MediaPlayer.create(this, R.raw.poker_face);
            mediaPlayer.start();
        } else {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    private AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.d("findAudioRecord", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("findAudioRecord", rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }

    public void playback(final View view) throws IOException {
        final File file = new File(fileName);
        if (file.exists() && null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } else if (null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void record(final View view) throws IOException {
        if (null == mediaRecorder) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                Log.e("recordAndPlay", "prepare() failed", e);
            }

            mediaRecorder.start();
        } else {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }


/*
        if (null == this.audioTrack) {
            final AudioRecord recorder = this.findAudioRecord();
//            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                    44100, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                    44100,
                    recorder.getSampleRate(),
//                    AudioFormat.CHANNEL_OUT_MONO,
//                    recorder.getChannelConfiguration(),
//                    AudioFormat.ENCODING_PCM_16BIT,
                    recorder.getAudioFormat(),
//                    MIN_BUFFER_SIZE,
                    MIN_BUFFER_SIZE,
                    AudioTrack.MODE_STREAM);

//            final InputStream in = getResources().openRawResource(R.raw.poker_face);
            final byte[] buffer = new byte[MIN_BUFFER_SIZE];
            recorder.startRecording();
            int read = recorder.read(buffer, 0, MIN_BUFFER_SIZE);
            System.out.println(read);
            recorder.stop();
            recorder.release();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            int read = in.read(buffer);
//            System.out.println(read);
//            audioTrack.play();
//            while (-1 != read) {
//                System.out.println(read);
                audioTrack.write(buffer, 0, MIN_BUFFER_SIZE);
//                read = in.read(buffer);
//            }
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;


        }
        */
    }

    public void recordWav(final View view) throws IOException {
        if (!isRecording) {
            wavFileName = handler.startAudioRecording(this);
            isRecording = true;
        } else {
            handler.stopAudioRecording();
            isRecording = false;
        }
//        if (null == extAudioRecorder) {
//            extAudioRecorder = ExtAudioRecorder.getInstanse(false);
//            extAudioRecorder.setOutputFile(wavFileName);
//            extAudioRecorder.prepare();
//
//            System.out.println(extAudioRecorder.getState() == ExtAudioRecorder.State.READY);
//            extAudioRecorder.start();
//        } else {
//            extAudioRecorder.stop();
//            extAudioRecorder.release();
//            extAudioRecorder = null;
//        }
    }

    public void playbackWav(final View view) throws IOException {
        final File file = new File(wavFileName);
        if (!isPlaying && file.exists()) {
            handler.play(wavFileName);
            isPlaying = true;
        } else {
            handler.stop();
            isPlaying = false;
        }

//        final File file = new File(wavFileName);
//        if (file.exists() && null == mediaPlayer) {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDataSource(wavFileName);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } else if (null != mediaPlayer) {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
    }

    public void playRemoteAudio(final View view) {
        if (null == this.mediaPlayer) {
            this.mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(REMOTE_AUDIO_URL);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        } else {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    public void playLocalVideo(final View view) {
        if (null == this.mediaPlayer) {
            this.mediaPlayer = MediaPlayer.create(this, R.raw.addy);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
