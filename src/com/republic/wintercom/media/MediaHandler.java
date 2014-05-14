package com.republic.wintercom.media;

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

    private AudioRecorder recorder;
    private Thread recorderThread;
    private Player player;
    private Thread playerThread;

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
        String filePath = null;
        if (null == recorder) {
            try {
                filePath = File.createTempFile("audio", ".wav", context.getCacheDir()).getAbsolutePath();
                Log.d("startAudioRecording", "recording audio to " + filePath);
            } catch (IOException e) {
                Log.e("startAudioRecording", "unable to create recording file", e);
                throw e;
            }

            recorder = new AudioRecorder(filePath);
            recorderThread = new Thread(recorder);
            recorderThread.start();
        }

        return filePath;
    }

    /**
     * Stops an audio recording, and sends it out to the community at large
     */
    public void stopAudioRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder = null;
            recorderThread = null;
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
        if (file.exists() && null == player) {
            Log.d("play", "playing " + fileName);
            player = new Player(fileName);
            playerThread = new Thread(player);
            playerThread.start();
        } else {
            Log.d("play", "file " + fileName + " does not exist or not playing");
        }
    }

    public void stop() {
        if (null != player) {
            player.stop();
            player = null;
            playerThread = null;
        }
    }

    private class AudioRecorder implements Runnable {
        private final String filePath;
        private ExtAudioRecorder extAudioRecorder;
        private boolean recording;

        private AudioRecorder(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            recording = true;
            extAudioRecorder = ExtAudioRecorder.getInstanse(false);
            extAudioRecorder.setOutputFile(filePath);
            extAudioRecorder.prepare();
            extAudioRecorder.start();

            while (recording) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            if (null != extAudioRecorder) {
                extAudioRecorder.stop();
                extAudioRecorder.release();
                extAudioRecorder = null;
            }

            recording = false;
        }
    }

    private class Player implements Runnable {
        private final String filePath;
        private MediaPlayer mediaPlayer;
        private boolean playing;

        private Player(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            playing = true;
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
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("startAudioRecording", "unable to prepare playback for file " + filePath, e);
                throw new RuntimeException("unable to play", e);
            }
            mediaPlayer.start();

            while (playing) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            if (null != mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            playing = false;
        }
    }
}
