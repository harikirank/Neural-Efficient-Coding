package com.example.neuralefficientcodepoc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//import com.arthenica.ffmpegkit.FFmpegKit;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isRecording = false;
    private int PERMISSION_CODE = 20;
    private String recordFile;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;

    private MediaRecorder mediaRecorder;
    private Chronometer timer;

    //Button recordButton;
    TextView recordFilename_TextView;
    ImageButton list_ImageButton, recordButton, backButton, homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        //Get rid of action bar in this Class/Activity
        getSupportActionBar().hide();

        //Initialize variables
        recordButton = findViewById(R.id.record_Btn);
        recordFilename_TextView = findViewById(R.id.recordFilename_TV);
        timer = findViewById(R.id.record_timer);
        list_ImageButton = findViewById(R.id.recordList_Btn);
        backButton = findViewById(R.id.soundActivity_backBtn);
        homeButton = findViewById(R.id.soundActivity_homeBtn);


        recordButton.setOnClickListener(this);
        list_ImageButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        homeButton.setOnClickListener(this);
    }//End of onCreate method


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.record_Btn:
                if(isRecording){
                    //Stop Recording
                    stopRecording();
                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.microphone_100px,null));
                    isRecording = false;
                }else {
                    //Start Recording
                    if (checkPermissions()) {
                        startRecording();
                        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.microphone_red100px,null));
                        isRecording = true;
                    }
                }
                break;
            case R.id.recordList_Btn:
                if(isRecording){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent audioListIntent = new Intent(SoundActivity.this, audioList.class);
                            startActivity(audioListIntent);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", null);
                    alertDialog.setTitle("Audio Still Recording");
                    alertDialog.setMessage("Are you sure you want to stop the recording?");
                    alertDialog.create().show();
                }else{
                    Intent audioListIntent = new Intent(SoundActivity.this, audioList.class);
                    startActivity(audioListIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                break;
            case R.id.soundActivity_backBtn:
            case R.id.soundActivity_homeBtn:
                Intent homePageIntent = new Intent(SoundActivity.this, ImageOrSoundSelectionActivity.class);
                startActivity(homePageIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                break;

        }//End of switch cases

    }//End of onClick method

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SoundActivity.this, ImageOrSoundSelectionActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void stopRecording() {
        String recordPath = SoundActivity.this.getExternalFilesDir("/").getAbsolutePath();

        timer.stop();
        recordFilename_TextView.setText("Recording stopped:\n" + recordFile + " was saved.\n Click on the file icon\n to choose a file for processing.");
        mediaRecorder.stop();

        FFmpegSession session = FFmpegKit.execute("-i " + recordPath + "/" + recordFile + ".3gp -acodec pcm_s16le -ar 44100 " + recordPath + "/" + recordFile + ".wav");
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            // SUCCESS
            File fileToDelete = new File(recordPath + "/" + recordFile + ".3gp");

            if (fileToDelete.exists()) {
                boolean deleted = fileToDelete.delete();
                if (deleted) {
                    // File deleted successfully
                } else {
                    // Failed to delete the file
                }
            } else {
                // File does not exist at the specified path
            }
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            // CANCEL
        } else {
            // FAILURE
            Log.d("Audio Failure", String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));
        }

        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        //Here we determine where to store file. In this case in "root"
        String recordPath = SoundActivity.this.getExternalFilesDir("/").getAbsolutePath();
        //Here we format the date (local time)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
        Date now = new Date();

        recordFile = "Recording" + formatter.format(now);
        recordFilename_TextView.setText("Recording:\n " + recordFile);

        mediaRecorder = new MediaRecorder();
        //Get access to phone MIC
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile + ".3gp");
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();

    }//End of startRecording method

    private boolean checkPermissions() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else{
            ActivityCompat.requestPermissions(SoundActivity.this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }//End of checkPermissions method


    @Override
    protected void onStop() {
        super.onStop();

        if(isRecording) {
            stopRecording();
        }
    }//End of onStop method


}//End of SoundActivity class