package com.republic.wintercom;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import com.republic.wintercom.media.MediaHandler;

import java.io.File;
import java.io.IOException;


public class SpeakActivity extends Activity {

    private final MediaHandler handler = MediaHandler.getInstance();
    ImageButton btnImagePushToTalk;
    private String wavFileName;
    private boolean isRecording = false;
    private boolean isPlaying = false;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnImagePushToTalk = (ImageButton)findViewById(R.id.imageButtonPushToTalk);


        //  OnTouchListener - toggle image if button pressed
        btnImagePushToTalk.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();

                if (action==MotionEvent.ACTION_DOWN) {

                    btnImagePushToTalk.setBackgroundResource(R.drawable.pushtotalkred);
                    if (!isRecording) {
                        try {
                            wavFileName = handler.startAudioRecording(SpeakActivity.this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isRecording = true;
                    } else {
                        handler.stopAudioRecording();
                        isRecording = false;
                    }


                }
                else if (action==MotionEvent.ACTION_UP) {

                    btnImagePushToTalk.setBackgroundResource(R.drawable.pushtotalkgreen);
                    handler.stopAudioRecording();



                    final File file = new File(wavFileName);
                    if (!isPlaying && file.exists()) {
                        try {
                            handler.play(wavFileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isPlaying = true;
                    } else {
                        handler.stop();
                        isPlaying = false;
                    }

                }

                return false;
            }
        });

    }

}
