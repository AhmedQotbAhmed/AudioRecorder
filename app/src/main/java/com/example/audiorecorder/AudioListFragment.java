package com.example.audiorecorder;


import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick{
    private int position;
    private ConstraintLayout playsheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList;
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;
    private MediaPlayer mediaPlayer= null;
    private boolean isPlaying=false;
    private File file_To_Play;

    //ui elements
    private ImageButton playBtn;
    private ImageButton forward_btn;
    private ImageButton back_btn;
    private TextView playerHeader;
    private TextView playerFileName;
    private SeekBar playerSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;

    public AudioListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hena 3lshan el elbgaia bta3y el sheet lma binzl t7t w mash bitl3 tany
        playsheet=view.findViewById(R.id.player_sheet);

        bottomSheetBehavior=BottomSheetBehavior.from(playsheet);
        audioList =view.findViewById(R.id.audio_list_view);

        playBtn = view.findViewById(R.id.player_play_btn);
        playerHeader= view.findViewById(R.id.player_header_title);
        playerFileName= view.findViewById(R.id.player_file_name);

        forward_btn=view.findViewById(R.id.player_forward_btn);
        back_btn=view.findViewById(R.id.player_back_btn);

        playerSeekBar=view.findViewById(R.id.player_SeekBar);

        String path =getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory= new File(path);
        allFiles =directory.listFiles();

        audioListAdapter=new AudioListAdapter(allFiles,this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState==BottomSheetBehavior.STATE_HIDDEN)
                { bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }});

        back_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (position>0) { stopAudio();
                playAudio(allFiles[position - 1]);
                    position-=1;}
                else{} }
        });

        forward_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (position>= allFiles.length-1) { }
                else{
                    stopAudio();
                    playAudio(allFiles[position + 1]);
                    position+=1;
                }
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (isPlaying){ pauseAudio(); }
                else {
                    if (file_To_Play!=null) { resumeAudio(); }}
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (file_To_Play!=null){ pauseAudio();}

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (file_To_Play!=null) {
                    int progess = seekBar.getProgress();
                    mediaPlayer.seekTo(progess);
                    resumeAudio();
                }

            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onItemListClick(File file, int position) {
        this.position=position;
        file_To_Play=file;
        if(isPlaying){
            //stop
            stopAudio();
            playAudio(file_To_Play);
        }
        else{
            //play
            playAudio(file_To_Play);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void pauseAudio(){
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
        isPlaying=false;
        seekBarHandler.removeCallbacks(updateSeekBar);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void resumeAudio(){
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
        isPlaying=true;
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar,0);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopAudio() {
        //stop the audio
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
        isPlaying=false;
        mediaPlayer.stop();
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playAudio(File fileToPlay) {
        //play the audio
        mediaPlayer=new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
        playerFileName.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        isPlaying=true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();

                playerHeader.setText("Finished");
                mediaPlayer.reset();



            }
        });
        playerSeekBar.setMax(mediaPlayer.getDuration());
        seekBarHandler= new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar,0);



    }

    private void updateRunnable() {
        updateSeekBar= new Runnable() {
            @Override
            public void run() {
                playerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this,100);

            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying)
        {stopAudio();}
    }
}
