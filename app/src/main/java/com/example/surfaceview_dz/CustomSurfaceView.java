package com.example.surfaceview_dz;
import static android.view.MotionEvent.ACTION_DOWN;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread thread = null;
    private int screenWidth, screenHeight;
    private List<Circle> krygi = new ArrayList<>();
    private Random random = new Random();

    public CustomSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                spawnCircle((int) event.getX(), (int) event.getY(), 10, 10);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void spawnCircle(int x, int y, int speedX, int speedY) {
        int radius = random.nextInt(100) + 20;
        int R = random.nextInt(256);
        int G = random.nextInt(256);
        int B = random.nextInt(256);
        int color = Color.argb(255, R, G, B);
        synchronized (krygi) {
            krygi.add(new Circle(x, y, radius, color, speedX, speedY));
        }
    }

    private void start() {
        thread = new DrawThread(getHolder());
        thread.setRunning(true);
        thread.start();
    }

    private void stop() {
        if (thread != null) {
            thread.setRunning(false);
            thread.interrupt();
            thread = null;
        }
    }

    private class DrawThread extends Thread {

        private SurfaceHolder holder;
        private boolean isRunning = false;
        private Paint paint;

        public DrawThread(SurfaceHolder holder) {
            this.holder = holder;
            paint = new Paint();
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            while (isRunning) {
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        update();
                        draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void update() {
            synchronized (krygi) {
                for (Circle circle : krygi) {
                    circle.x += circle.speedX;
                    circle.y += circle.speedY;
                    if (circle.x <= circle.radius || circle.x >= screenWidth - circle.radius) {
                        circle.speedX = -circle.speedX;
                    }
                    if (circle.y <= circle.radius || circle.y >= screenHeight - circle.radius) {
                        circle.speedY = -circle.speedY;
                    }
                }
            }
        }

        private void draw(Canvas canvas) {
            List<Circle> circlesCopy;
            synchronized (krygi) {
                circlesCopy = new ArrayList<>(krygi);
            }

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (Circle circle : circlesCopy) {
                paint.setColor(circle.color);
                canvas.drawCircle(circle.x, circle.y, circle.radius, paint);
            }
        }
    }

    private class Circle {
        int x, y;
        int radius;
        int color;
        int speedX;
        int speedY;

        Circle(int x, int y, int radius, int color, int speedX, int speedY) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
            this.speedX = speedX;
            this.speedY = speedY;
        }
    }
}
