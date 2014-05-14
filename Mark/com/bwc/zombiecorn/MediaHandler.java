package com.bwc.zombiecorn;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * @author Mark Gilmore
 */
public class MediaHandler {
    private static final Object LOCK = new Object();
    private static MediaHandler INSTANCE = null;

    private File recordingFile;
    private ExtAudioRecorder extAudioRecorder;
    private MediaPlayer mediaPlayer;

    private MediaHandler() {

    }

    public static MediaHandler getInstance() {
        if (null == INSTANCE) {
            synchronized (LOCK) {
                if (null == INSTANCE) {
                    INSTANCE = new MediaHandler();
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Starts an audio recording
     *
     * @param context
     * @return path to the recording file
     */
    public String startAudioRecording(final Context context) throws IOException {
        if (null == extAudioRecorder) {
            try {
                recordingFile = File.createTempFile("audio", ".wav", context.getCacheDir());
                Log.d("startAudioRecording", "recording audio to " + recordingFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e("startAudioRecording", "unable to create recording file", e);
                throw e;
            }
            extAudioRecorder = ExtAudioRecorder.getInstanse(false);
            extAudioRecorder.setOutputFile(recordingFile.getAbsolutePath());
            extAudioRecorder.prepare();
            extAudioRecorder.start();
        }

        return recordingFile.getAbsolutePath();
    }

    /**
     * Stops an audio recording, and sends it out to the community at large
     */
    public void stopAudioRecording() {
        if (null != extAudioRecorder) {
            extAudioRecorder.stop();
            extAudioRecorder.release();
            extAudioRecorder = null;
        }
    }

    /**
     * Plays the given file
     *
     * @param fileName
     * @throws IOException
     */
    public void play(final String fileName) throws IOException {
        final File file = new File(fileName);
        if (file.exists() && null == mediaPlayer) {
            Log.d("play", "playing " + fileName);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("play.onCompletion", "recording ended, cleaning up");
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
            try {
                mediaPlayer.setDataSource(fileName);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("startAudioRecording", "unable to prepare playback for file " + fileName, e);
                throw e;
            }
            mediaPlayer.start();
        } else {
            Log.d("play", "file " + fileName + " does not exist or not playing");
        }
    }

    public void stop() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}
