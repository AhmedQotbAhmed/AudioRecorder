package com.example.audiorecorder;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

private NavController navController;
private ImageButton  listBtn;
private ImageButton  recordBtn;
private TextView fileNameText;
private  String recordPermitions=Manifest.permission.RECORD_AUDIO;
private  int Permition_Code =21;
private  boolean isRecording =false;
private String recordFile;
private Chronometer timer;

private MediaRecorder mediaRecorder;
    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController= Navigation.findNavController(view);

        listBtn=view.findViewById(R.id.record_list_btn);
        recordBtn=view.findViewById(R.id.record_btn);
        timer=view.findViewById(R.id.record_timer);
        fileNameText=view.findViewById(R.id.record_filename);

        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.record_list_btn:
                if (isRecording){
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("OkAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                        isRecording=false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCLE",null);
                    alertDialog.setTitle("Audio Still Recording");
                    alertDialog.setMessage("Are you sure, you want to stop the recording");
                    alertDialog.create().show();
                }
                else{
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                    break;
                }


            case R.id.record_btn:
                if (isRecording){
                    //stop
                    stopRecording();
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped,null));
                    isRecording=false;
                }
                else{
                    //play

                    if (checkPermissions()){
                        startRecording();
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording,null));
                    isRecording=true;
                }}

                break;


        }
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();


        String recordPath= getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat formatTer=new SimpleDateFormat("dd_hh_mm_ss", Locale.CANADA);
        Date now= new Date();
        recordFile="Recording_"+formatTer.format(now)+".3gp";
        fileNameText.setText("Recording File name : "+recordFile);
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath+"/"+recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void stopRecording() {
        timer.stop();
        fileNameText.setText("Recording Stopped &   "+recordFile+" : Saved" );

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder=null;
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                recordPermitions)== PackageManager.PERMISSION_GRANTED)
        { return true;}
        else
        {ActivityCompat.requestPermissions(getActivity(),new String[]{recordPermitions},Permition_Code);
            return false;}
    }

    @Override
    public void onStop() {
        super.onStop();
    if (isRecording){ stopRecording(); }
    }
}
