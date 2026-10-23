package com.example.accelerometer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private volatile boolean playing = false;

    private volatile boolean gameStarted = false;
    private final Rect startButton = new Rect();

    private final SurfaceHolder holder;
    private final Paint paint;

    private Bitmap player;
    private Bitmap star;
    private Bitmap asteroid;

    private float playerX;
    private float playerY;

    private float starX;
    private float starY;

    private float asteroidX;
    private float asteroidY;

    private final int objectSize = 150;

    private int score = 0;

    private final Random random;

    private boolean initialized = false;

    private volatile float tiltX = 0;

    public GameView(Context context) {
        super(context);

        holder = getHolder();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        random = new Random();

        player = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.spaceship
        );

        star = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.star
        );

        asteroid = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.asteroid
        );

        player = Bitmap.createScaledBitmap(
                player,
                objectSize,
                objectSize,
                true
        );

        star = Bitmap.createScaledBitmap(
                star,
                objectSize,
                objectSize,
                true
        );

        asteroid = Bitmap.createScaledBitmap(
                asteroid,
                objectSize,
                objectSize,
                true
        );

        starY = -200;
        asteroidY = -600;

        setFocusable(true);
    }

    @Override
    public void run() {

        while (playing) {

            if (gameStarted) {
                update();
            }

            drawGame();

            try {
                Thread.sleep(17);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void update() {

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) {
            return;
        }

        if (!initialized) {

            playerX = (width - objectSize) / 2f;

            resetStar();
            resetAsteroid();

            initialized = true;
        }

        playerY = height - objectSize - 50;

        playerX -= tiltX * 6;

        if (playerX < 0) {
            playerX = 0;
        }

        if (playerX > width - objectSize) {
            playerX = width - objectSize;
        }

        starY += 10;
        asteroidY += 13;

        if (starY > height) {
            resetStar();
        }

        if (asteroidY > height) {
            resetAsteroid();
        }

        checkCollisions();
    }

    private void checkCollisions() {

        Rect playerRect = new Rect(
                (int) playerX,
                (int) playerY,
                (int) playerX + objectSize,
                (int) playerY + objectSize
        );

        Rect starRect = new Rect(
                (int) starX,
                (int) starY,
                (int) starX + objectSize,
                (int) starY + objectSize
        );

        Rect asteroidRect = new Rect(
                (int) asteroidX,
                (int) asteroidY,
                (int) asteroidX + objectSize,
                (int) asteroidY + objectSize
        );

        if (Rect.intersects(playerRect, starRect)) {
            score++;
            resetStar();
        }

        if (Rect.intersects(playerRect, asteroidRect)) {
            score--;
            resetAsteroid();
        }
    }
    private void resetStar() {

        int maxX = Math.max(
                1,
                getWidth() - objectSize
        );

        starX = random.nextInt(maxX);
        starY = -objectSize;
    }

    private void resetAsteroid() {

        int maxX = Math.max(
                1,
                getWidth() - objectSize
        );

        asteroidX = random.nextInt(maxX);
        asteroidY = -objectSize - 300;
    }

    private void drawGame() {

        if (!holder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = holder.lockCanvas();

        if (canvas == null) {
            return;
        }

        try {

            canvas.drawColor(
                    Color.rgb(20, 20, 45)
            );

            paint.setAntiAlias(true);
            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            if (!gameStarted) {

                float centerX =
                        canvas.getWidth() / 2f;

                float centerY =
                        canvas.getHeight() / 2f;

                paint.setColor(Color.WHITE);
                paint.setTextSize(70);
                paint.setFakeBoldText(true);

                canvas.drawText(
                        "STAR GRABBER!",
                        centerX,
                        centerY - 220,
                        paint
                );

                int buttonWidth = 350;
                int buttonHeight = 160;

                startButton.set(
                        (int) centerX - buttonWidth / 2,
                        (int) centerY - buttonHeight / 2,
                        (int) centerX + buttonWidth / 2,
                        (int) centerY + buttonHeight / 2
                );

                paint.setColor(
                        Color.rgb(210, 55, 65)
                );

                canvas.drawRoundRect(
                        startButton.left,
                        startButton.top,
                        startButton.right,
                        startButton.bottom,
                        50,
                        50,
                        paint
                );

                paint.setColor(Color.WHITE);
                paint.setTextSize(65);

                canvas.drawText(
                        "START",
                        centerX,
                        centerY + 22,
                        paint
                );

                paint.setFakeBoldText(false);
                paint.setTextSize(35);

                canvas.drawText(
                        "Tilt to move your spaceship",
                        centerX,
                        centerY + 180,
                        paint
                );

                canvas.drawText(
                        "Collect stars while avoid asteroids to score points!",
                        centerX,
                        centerY + 235,
                        paint
                );

            } else {

                paint.setColor(Color.WHITE);
                paint.setFakeBoldText(false);
                paint.setTextAlign(
                        Paint.Align.LEFT
                );

                canvas.drawBitmap(
                        player,
                        playerX,
                        playerY,
                        paint
                );

                canvas.drawBitmap(
                        star,
                        starX,
                        starY,
                        paint
                );

                canvas.drawBitmap(
                        asteroid,
                        asteroidX,
                        asteroidY,
                        paint
                );

                paint.setColor(Color.WHITE);
                paint.setTextSize(60);

                canvas.drawText(
                        "Score: " + score,
                        40,
                        80,
                        paint
                );
            }

        } finally {

            holder.unlockCanvasAndPost(
                    canvas
            );
        }
    }


    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        if (event.getAction()
                == MotionEvent.ACTION_DOWN) {

            if (!gameStarted &&
                    startButton.contains(
                            (int) event.getX(),
                            (int) event.getY()
                    )) {

                gameStarted = true;

                return true;
            }
        }

        return true;
    }


    public void movePlayer(float x) {

        tiltX = x;
    }

    public void resume() {

        if (playing) {
            return;
        }

        playing = true;

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {

        playing = false;

        if (gameThread != null) {

            try {

                gameThread.join();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

            gameThread = null;
        }
    }
}