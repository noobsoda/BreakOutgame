package com.example.breakoutgame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class BreakOutEngine  extends SurfaceView implements Runnable, SensorEventListener {


    // This is our thread
    private Thread gameThread = null;

    // This is new. We need a SurfaceHolder
    // When we use Paint and Canvas in a thread
    // We will see it in action in the draw method soon.
    private SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    // A Canvas and a Paint object
    private Canvas canvas;
    private Paint paint;

    // How wide and high is the screen?
    private int screenX;
    private int screenY;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps
    private long timeThisFrame;

    private  static SensorManager mSensorManager;
    private Sensor mAccelerometer; //가속도 센스
    private Sensor mMagnetometer; // 자력계 센스
    float[] mGravity = null;
    float[] mGeomagnetic = null;

    Bat bat;
    Ball ball;
    AlertDialog.Builder alertDialogBuilder;
    // Up to 200 bricks
    Brick[] bricks = new Brick[200];
    int numBricks= 0;

    // For sound FX
    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    // The score
    int score = 0;

    // Lives
    int lives = 1;

    public BreakOutEngine(Context context, int x, int y) {
        // This calls the default constructor to setup the rest of the object
        super(context);

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        // Initialize screenX and screenY because x and y are local
        screenX = x;
        screenY = y;

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        bat = new Bat(screenX, screenY);
        ball = new Ball();
        alertDialogBuilder = new AlertDialog.Builder(context);


        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);


        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            loseLifeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = soundPool.load(descriptor, 0);

        }catch(IOException e){
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }
        restart();
    }

    public void pause(){
        playing = false;
        mSensorManager.unregisterListener(this);
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }
    public void resume(){
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_UI);
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();

    }

    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            // Update the frame
            if(!paused){
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

        }
    }
    void gameOver(){
        alertDialogBuilder
                .setMessage("GAME OVER")
                .setCancelable(false)
                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //프로그램을 종료한다.
                        System.exit(0);
                    }
                })
                .setNegativeButton("재시작", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //다이얼로그를 취소한다.
                        restart();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    void restart(){
        ball.reset(screenX, screenY);

        int brickWidth = screenX / 8;
        int brickHeight = screenY / 10;

        // Build a wall of bricks
        numBricks = 0;

        for(int column = 0; column < 8; column ++ ){
            for(int row = 0; row < 3; row ++ ){
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks ++;
            }
        }
    }

    private void update() {
        bat.update(fps);

        ball.update(fps);

        for (int i = 0; i < numBricks; i++) {

            if (bricks[i].getVisibility()) {

                if (RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                    bricks[i].setInvisible();
                    ball.reverseYVelocity();
                    score = score + 10;
                    soundPool.play(explodeID, 1, 1, 0, 0, 1);
                }
            }
        }
        // Check for ball colliding with bat
        if(RectF.intersects(bat.getRect(),ball.getRect())) {
            ball.setRandomXVelocity();
            ball.reverseYVelocity();
            ball.clearObstacleY(bat.getRect().top - 2);
            soundPool.play(beep1ID, 1, 1, 0, 0, 1);
        }
        // Bounce the ball back when it hits the bottom of screen
        // And deduct a life
        if(ball.getRect().bottom > screenY){
            ball.reverseYVelocity();
            ball.clearObstacleY(screenY-2);

            // Lose a life
            lives --;
            soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

            /*if(lives == 0){
                score = 0;
                paused = true;
                //gameOver();

                restart();
            }*/

        }

        // Bounce the ball back when it hits the top of screen
        if(ball.getRect().top < 0){
            ball.reverseYVelocity();
            ball.clearObstacleY(12);
            soundPool.play(beep2ID, 1, 1, 0, 0, 1);
        }

        // If the ball hits left wall bounce
        if(ball.getRect().left < 0){
            ball.reverseXVelocity();
            ball.clearObstacleX(2);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        // If the ball hits right wall bounce
        if(ball.getRect().right > screenX ){
            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - 22);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        // Pause if cleared screen
        if(score == numBricks * 10){
            paused = true;
            restart();
        }
    }
    private void draw(){
        if(ourHolder.getSurface().isValid()){
            canvas = ourHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 26, 128, 182));

            canvas.drawRect(bat.getRect(), paint);

            paint.setColor(Color.argb(255, 255, 255, 255));

            canvas.drawRect(ball.getRect(), paint);

            paint.setColor(Color.argb(255, 249, 129, 0));

            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }
            // Draw the HUD
            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  255, 255, 255));

            // Draw the score
            paint.setTextSize(70);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10,80, paint);

            // Show everything we have drawn
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }
    //여기를 수정해서 센서로 바꿔야 할 듯
    //센서값만 조정해도 될듯
    public boolean onTouchEvent(MotionEvent motionEvent){


        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {


            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                paused = false;

                break;
                /*if (motionEvent.getX() > screenX / 2) {
                    bat.setMovementState(bat.RIGHT);
                } else {
                    bat.setMovementState(bat.LEFT);
                }

                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                bat.setMovementState(bat.STOPPED);
                break;*/
        }

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(lives == 0){
            lives = 1;
            score = 0;
            paused = true;
            gameOver();
        }
        float azimut, pitch, roll;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I,
                    mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation contains: azimut, pitch  and roll
                azimut = orientation[0];
                pitch = orientation[1];
                roll = orientation[2];


                if (roll > 0.3) {
                    bat.setMovementState(bat.RIGHT);
                }
                else if (roll < -0.3)
                    bat.setMovementState(bat.LEFT);
                else
                    bat.setMovementState(bat.STOPPED);




            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




}
