package com.example.breakoutgame;

import android.graphics.RectF;

public class Bat {
    // RectF is an object that holds four coordinates - just what we need
    private RectF rect;

    // How long will our paddle will be
    private float length;

    // X is the far left of the rectangle which forms our paddle
    private float x;

    // This will hold the pixels per second speed that the paddle will move
    private float paddleSpeed;

    // Which ways can the paddle move
    final int STOPPED = 0;
    final int LEFT = 1;
    final int RIGHT = 2;

    // Is the paddle moving and in which direction
    private int paddleMoving = STOPPED;
    Bat(int screenX, int screenY){
        // 130 pixels wide and 20 pixels high
        length = 130;
        float height = 80;

        // Start paddle in roughly the sceen centre
        x = screenX / 2;

        // Y is the top coordinate
        float y = screenY - 20;

        rect = new RectF(x, y, x + length, y + height);

        // How fast is the paddle in pixels per second
        paddleSpeed = 400;
    }

    RectF getRect(){
        return rect;
    }

    // This method will be used to change/set if the paddle is going left, right or nowhere
    void setMovementState(int state){
        paddleMoving = state;
    }

    void update(long fps){
        if(paddleMoving == LEFT){
            x = x - paddleSpeed / fps;
        }

        if(paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }

}
