package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    int songPosition;
    int counter = 0;
    private MediaPlayer mediaPlayer;
    //Button pauseButton;
    private boolean isMusicPlayerInit;
    private List<String> musicFileList;
    SeekBar seekBar;
    TextView currentPositionTextView, songDurationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentPositionTextView = findViewById(R.id.currentPosition);
        songDurationTextView = findViewById(R.id.songDuration);
        //pauseButton = findViewById(R.id.pauseButton);


    }

    //checking permissions
    private static final String[] PERMISSIONS ={
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_PERMISSIONS = 12345; //ANY NUMBER YOU WANT

    private static final int PERMISSIONS_COUNT = 1;
    //check permissions are granted or not
    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
        for(int i =0; i<PERMISSIONS_COUNT; i++){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
                //permission denied
            }
        }
        return false;
        //permission granted
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //checking permissions
        if(arePermissionsDenied()){
            //checks user denied the permission or not
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate(); //recreate the activity when user denied the permission
        }
        else{
            onResume();
        }
    }



    private void addMusicFilesFrom(String dirPath){
        final File musicDir = new File(dirPath);
        if(!musicDir.exists()){
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();
        for(File file : files){
            final String path = file.getAbsolutePath();
            if(path.endsWith(".mp3")){
                musicFileList.add(path);
            }
        }
    }
    @SuppressLint("NewApi")
    private void fillMusicList(){
        musicFileList.clear();
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)));
    }



    private int playMusicFile(String path){
        mediaPlayer = new MediaPlayer();

        try{
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mediaPlayer.getDuration();
    }

//private void playSong(){
//
//}

    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        if(!isMusicPlayerInit){

            final ListView listView = findViewById(R.id.musicListViewId); //initialize the listview
            final TextAdapter textAdapter = new TextAdapter();
            musicFileList = new ArrayList<>();
            fillMusicList();
            textAdapter.setData(musicFileList);
            listView.setAdapter(textAdapter);
            seekBar = findViewById(R.id.seekBar);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int songProgress;
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    songProgress = progress;
                    //mediaPlayer.seekTo(songPosition);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int nowPos;
                   songPosition=songProgress;
                   nowPos = songPosition;
                   mediaPlayer.seekTo(songPosition);
                }
            });

            final Button pauseButton = findViewById(R.id.pauseButton);

            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (counter == 0){
                        mediaPlayer.pause();
                        pauseButton.setText("Play");
                        counter = 1;
                    }
                    else{
                        mediaPlayer.start();
                        pauseButton.setText("Pause");
                        counter = 0;
                    }
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String musicFilePath = musicFileList.get(position);
                    final int songDuration = playMusicFile(musicFilePath)/1000;
                    seekBar.setMax(songDuration);
                    seekBar.setVisibility(View.VISIBLE);
                    currentPositionTextView.setVisibility(View.VISIBLE);
                    songDurationTextView.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                    songDurationTextView.setText(String.valueOf(songDuration/60)+":"+String.valueOf(songDuration%60)); //show the duration of the song in text view
//updating seekbar position

                   // isSongPlaying = true;
                    new Thread(){
                        @Override
                        public void run() {
                            songPosition = 0;
                            while(songPosition<songDuration){
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if(counter == 0){
                                    songPosition++;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            seekBar.setProgress(songPosition);
                                            currentPositionTextView.setText(String.valueOf(songPosition/60)+":"+String.valueOf(songPosition%60));
                                        }
                                    });
                                }

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mediaPlayer.pause();
                                    songPosition=0;
                                    mediaPlayer.seekTo (songPosition);
                                    currentPositionTextView.setText(""+songPosition);
                                    pauseButton.setText("Play");
                                    counter=1;
                                    seekBar.setProgress(songPosition);
                                }
                            });



                            //mediaPlayer.stop();
                        }
                    }.start();
                }
            });
            isMusicPlayerInit=true;
        }
    }
}
