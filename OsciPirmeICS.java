package com.example.a79463.bluetoothtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class OsciPirmeICS extends AppCompatActivity implements View.OnClickListener {


    private SurfaceHolder holder;
    private SurfaceView showSurfaceView;

    private ImageButton break_return;
    private Button btnShowSin;
    private Button btnShowCos;
    private Button btnShowBrokenLine;
    private Button btnclear;
    private Button button1;
    private Button button2;
    private Button btnok;

    private EditText etput;
    private TextView tvlook;

    private Paint paint;

    private int HEIGHT;
    // 要绘制的曲线的水平宽度
    private int WIDTH;
    // 离屏幕左边界的起始距离
    private final int X_OFFSET = 5;
    // 初始化X坐标
    private int cx = X_OFFSET;
    // 实际的Y轴的位置
    private int centerY ;
    private Timer timer = new Timer();
    private TimerTask task = null;

    public int number_I=0;
    static final int CMD_RECEIVE_DATA = 0x06;       // service -> Main
    MyReceiver1 receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osci_pirme_ics);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        // 获得SurfaceView对象
        showSurfaceView = (SurfaceView) findViewById(R.id.showSurfaceView);
        break_return=findViewById(R.id.break_return);
        btnShowSin = (Button) findViewById(R.id.btnShowSin);
        btnShowCos = (Button) findViewById(R.id.btnShowCos);
        btnShowBrokenLine = (Button) findViewById(R.id.btnShowBrokenLine);
        btnclear=findViewById(R.id.btnclear);
        button1=findViewById(R.id.button1);
        button2=findViewById(R.id.button2);
        btnok=findViewById(R.id.btnok);

        etput=findViewById(R.id.etput);
        tvlook=findViewById(R.id.tvlook);

        btnShowSin.setOnClickListener(this);
        btnShowCos.setOnClickListener(this);
        btnShowBrokenLine.setOnClickListener(this);
        btnclear.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        btnok.setOnClickListener(this);

        InitData();

        // 初始化SurfaceHolder对象
        holder = showSurfaceView.getHolder();
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

        break_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (task != null) {
                    task.cancel();
                }
                finish();
            }
        });
    }

    protected void onResume() {
        super.onResume();

        receiver = new MyReceiver1();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.bluetooth.admin.bluetooth");
        OsciPirmeICS.this.registerReceiver(receiver,filter);

    }
    public void qindping(){

        cx=X_OFFSET;
        showclear();
        test(holder);
    }


    private void InitData() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        //获取屏幕的宽度作为示波器的边长
        HEIGHT = dm.widthPixels;
        WIDTH = dm.widthPixels;
        //Y轴的中心就是高的一半
        Log.e("HEIGHT111", String.valueOf(HEIGHT));
        Log.e("WIDTH111", String.valueOf(WIDTH));
        centerY = HEIGHT / 2;
        Log.e("centerY111", String.valueOf(centerY));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnShowSin:
                showSineCord(view);
                break;
            case R.id.btnShowCos:
                showSineCord(view);
                break;
            case R.id.btnShowBrokenLine:
                showBrokenLine();
                break;
            case R.id.btnclear:
                showclear();
                break;
            case R.id.button1:
                num_1();
                break;

            case R.id.button2:
                test(holder);
                break;

            case R.id.btnok:
                getnum();
                break;

        }

    }

    private void getnum(){
        tvlook.setText(etput.getText());

    }
    private void num_1(){
        number_I=number_I+10;
    }

    private void test(final SurfaceHolder holder){
        drawBackGround(holder);
        cx = X_OFFSET;
        if (task != null) {
            task.cancel();
        }
        task = new TimerTask( ) {
            int startX = 0;
            int startY = 200;
            //@Override
            public void run() {

                int cy = number_I;
                Log.i("number_I", String.valueOf(number_I));
                Canvas canvas = holder.lockCanvas(new Rect(cx-10, cy - 900,
                        cx + 10, cy+ 900));

                // 根据Ｘ，Ｙ坐标画线
                canvas.drawLine(startX, startY ,cx, cy, paint);

                //结束点作为下一次折线的起始点
                startX = cx;
                startY = cy;

                cx+=10;
                // 超过指定宽度，线程取消，停止画曲线
                if (cx > WIDTH) {
//                    task.cancel();
//                    task = null;
                    cx=0;
                }
                // 提交修改
                holder.unlockCanvasAndPost(canvas);
            }
        };
        timer.schedule(task, 0, 3);

    }

    private void showclear(){
//        if (task != null) {
//            task.cancel();
//        }
        drawBackGround(holder);
    }
    /**
     * 折线曲线
     */
    private void showBrokenLine(){

        drawBackGround(holder);
        cx = X_OFFSET;
        if (task != null) {
            task.cancel();
        }
        task = new TimerTask() {
            int startX = 0;
            int startY = 200;
            Random random = new Random();
            @Override
            public void run() {

                int cy = random.nextInt(100)+200;

                Canvas canvas = holder.lockCanvas(new Rect(cx-10, cy - 900,
                        cx + 10, cy + 900));

                // 根据Ｘ，Ｙ坐标画线
                canvas.drawLine(startX, startY ,cx, cy, paint);

                //结束点作为下一次折线的起始点
                startX = cx;
                startY = cy;

                cx+=10;
                // 超过指定宽度，线程取消，停止画曲线
                if (cx > WIDTH) {
                    task.cancel();
                    task = null;
                }
                // 提交修改
                holder.unlockCanvasAndPost(canvas);
            }
        };
        timer.schedule(task, 0, 300);
    }

    /**
     * 正余弦曲线函数
     */
    private void showSineCord(final View view){
        drawBackGround(holder);
        cx = X_OFFSET;
        if (task != null) {
            task.cancel();
        }
        task = new TimerTask() {

            @Override
            public void run() {
                // 根据是正玄还是余玄和X坐标确定Y坐标
                int cy = view.getId()==R.id.btnShowSin?
                        centerY/2- (int) (100 * Math.sin((cx - 5) * 2 * Math.PI/ 150))
                        :centerY/2- (int) (100 * Math.cos((cx - 5) * 2 * Math.PI/ 150));

                Canvas canvas = holder.lockCanvas(new Rect(cx, cy - 2,
                        cx + 2, cy + 2));
                // 根据Ｘ，Ｙ坐标画点
                canvas.drawPoint(cx, cy, paint);
                cx++;
                // 超过指定宽度，线程取消，停止画曲线
                if (cx > WIDTH) {
                    canvas.drawColor(Color.BLACK);
                    task.cancel();
                    task = null;
                }
                // 提交修改
                holder.unlockCanvasAndPost(canvas);
            }
        };
        timer.schedule(task, 0, 3);
    }

    private void drawBackGround(final SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        // 绘制黑色背景
        canvas.drawColor(Color.BLACK);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStrokeWidth(2);

        // 画网格8*8
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GRAY);// 网格为黄色
        mPaint.setStrokeWidth(1);// 设置画笔粗细
        int oldY = 0;
        Log.e("HEIGHT", String.valueOf(HEIGHT));
        Log.e("WIDTH", String.valueOf(WIDTH));
        Log.e("centerY", String.valueOf(centerY));
        for (int i = 0; i <= 8; i++) {// 绘画横线
            canvas.drawLine(0, oldY, WIDTH, oldY, mPaint);
            oldY = oldY + WIDTH/16;
        }
        int oldX = 0;
        for (int i = 0; i <= 16; i++) {// 绘画纵线
            canvas.drawLine(oldX, 0, oldX, HEIGHT, mPaint);
            oldX = oldX + HEIGHT/16;
        }

        // 绘制坐标轴
        canvas.drawLine(X_OFFSET, centerY/2, WIDTH, centerY/2, p);
        canvas.drawLine(X_OFFSET, 40, X_OFFSET, HEIGHT, p);
        holder.unlockCanvasAndPost(canvas);
/*        holder.lockCanvas(new Rect(0, 0, 100, 100));
        canvas.drawColor(Color.WHITE);
        holder.unlockCanvasAndPost(canvas);*/
    }

    public class MyReceiver1 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals("android.intent.action.bluetooth.admin.bluetooth")){
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");
                if(cmd == CMD_RECEIVE_DATA)
                {
                    String strtemp = bundle.getString("str");
                    Log.i("tv",strtemp);
                    String s=strtemp.substring(1,strtemp.length()-1);
                    number_I =Integer.parseInt(s);
                    tvlook.setText(s);
                }

            }
        }
    }

/*    public class MyTread1 extends Thread{
        @Override
        public void run() {
            super.run();
            if (cx > WIDTH+1){

            }
        }
    }*/
}
