package com.taiga_natto.musukatimer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Runnable{

    ImageView imageView;
    TextView textView;
    Button button;

    private long startTime;

    private Thread thread = null;
    private final Handler handler = new Handler();
    private volatile boolean stopRun = false;

    private SimpleDateFormat dataFormat = new SimpleDateFormat("mm:ss.SS");

    private int period = 10;

    boolean buttonFlag=false;
    long musukaTime=180000;

    int timerCnt=0;

    private AudioAttributes audioAttributes;
    private SoundPool soundPool;

    MediaPlayer mediaPlayer;

    // 画像リソースを取得する
    int[] imageRe={
            R.drawable._2,
            R.drawable._3,
            R.drawable._4,
            R.drawable._5,
            R.drawable._6,
            R.drawable._7,
            R.drawable._8};

    //効果音リソース
    int[] soundRe={
            R.raw.sound3mwait,
            R.raw.sound_zikanda,
            R.raw.sound_megamega,
            R.raw.nc77932,
            R.raw.nc95731,
            R.raw.nc95732,
            R.raw.nc95735,
            R.raw.nc95736,
            R.raw.nc95737,
            R.raw.nc98300,
            R.raw.nc105392,
            R.raw.nc105524,
            R.raw.nc105525,
            R.raw.nc105526,
            R.raw.nc105577,
            R.raw.nc105642,
            R.raw.nc105643,
            R.raw.nc105646,
            R.raw.nc105650,
            R.raw.nc105651,
            R.raw.nc105652};

    //BGMリソース
    int[] musicRe={
            R.raw.end_music2,
            R.raw.battle_music1,
            R.raw.battle_music2,
            R.raw.battle_music3,
            R.raw.battle_music4,
            R.raw.battle_music5,
            R.raw.battle_music6,
            R.raw.battle_music7
    };

    int[] soundPools=new int[soundRe.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //関連付け
        imageView=(ImageView)findViewById(R.id.musuka_image);
        textView=(TextView)findViewById(R.id.timer);
        button=(Button)findViewById(R.id.button);

        textView.setText(dataFormat.format(musukaTime));
        button.setText("3分間待ってもらう");
        imageView.setImageResource(R.drawable._1);

        audioAttributes = new AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_GAME)
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(4)
                .build();

        for(int i=0;i<soundRe.length;i++){
            soundPools[i]=soundPool.load(this,soundRe[i], 1);
        }
    }

    public void start(View v){
        if (!buttonFlag){

            // AudioManagerを取得する
            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            // ストリームごとの最大音量を取得する
            int ringMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // 音量を設定する
            am.setStreamVolume(AudioManager.STREAM_MUSIC, ringMaxVolume, 0);

            Random r=new Random();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), musicRe[r.nextInt(musicRe.length-1)+1]);
            mediaPlayer.setVolume(0.1f,0.1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            button.setText("バルス");
            buttonFlag=true;

            soundPool.play(soundPools[0], 1.0f, 1.0f, 0, 0, 1);

            stopRun = false;
            thread = new Thread(this);
            thread.start();

            startTime = System.currentTimeMillis();
            timerCnt=0;
        }
        else{
            soundPool.play(soundPools[2], 1.0f, 1.0f, 0, 0, 1);

             // AudioManagerを取得する
             AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
             // 音量を設定する
             am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

            button.setText("3分間待ってもらう");
            buttonFlag=false;

            stopRun = true;
            thread = null;
        }
    }

    @Override
    public void run() {

        while (!stopRun) {
            // sleep: period msec
            try {
                Thread.sleep(period);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                stopRun = true;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(buttonFlag) {
                        long endTime = System.currentTimeMillis();
                        // カウント = 経過時間 - 開始時間
                        long diffTime = (endTime - startTime);
                        long cnt = musukaTime - diffTime;

                        if(timerCnt==500){
                            Random r = new Random();
                            imageView.setImageResource(imageRe[r.nextInt(imageRe.length)]);
                            soundPool.play(soundPools[r.nextInt(soundRe.length-3)+3], 1.0f, 1.0f, 0, 0, 1);
                            timerCnt=0;
                        }
                        timerCnt=timerCnt+1;

                        if(cnt<10){
                            buttonFlag=false;
                            soundPool.play(soundPools[1], 1.0f, 1.0f, 0, 0, 1);
                            mediaPlayer.stop();
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), musicRe[0]);
                            mediaPlayer.setVolume(0.5f,0.5f);
                            mediaPlayer.start();
                        }

                        textView.setText(dataFormat.format(cnt));
                    }
                    else{
                        textView.setText(dataFormat.format(musukaTime));
                        button.setText("3分間待ってもらう");
                    }
                }
            });
        }
    }
}
