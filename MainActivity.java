package com.example.a79463.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private BluetoothAdapter bluetoothAdapter;
    private Switch bluetoothSwitch;
    private Button bluetoothSearch;
    private Spinner bluetoothList;
    private Button bluetoothConnect;
    private ArrayAdapter<String> adapter;
    private List<String> list = new ArrayList<String>();
    private String strMacAddress;
    private static boolean booleanConnect = false;
    private TextView lookpwm;
    private Button mbtiaocan;

    private Button ok1,ok2,ok3,ok4;
    private TextView tvpwm1,tvpwm2,tvpwm3,tvpwm4;
    private EditText etpwm1,etpwm2,etpwm3,etpwm4;


    //msg 定义
    private static final int msgShowConnect = 1;

    /**************service 命令*********/
    static final int CMD_STOP_SERVICE = 0x01;       // Main -> service
    static final int CMD_SEND_DATA = 0x02;          // Main -> service
    static final int CMD_SYSTEM_EXIT =0x03;         // service -> Main
    static final int CMD_SHOW_TOAST =0x04;          // service -> Main
    static final int CMD_CONNECT_BLUETOOTH = 0x05;  // Main -> service
    static final int CMD_RECEIVE_DATA = 0x06;       // service -> Main

    MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSwitch = (Switch) findViewById(R.id.bluetoothswitch);
        bluetoothSearch = (Button) findViewById(R.id.buttonSearchBluetooth);
        bluetoothList = (Spinner) findViewById(R.id.list_bluetooth);
        bluetoothConnect = (Button) findViewById(R.id.buttonConnect);

        mbtiaocan=findViewById(R.id.button_tiaocan);

        ok1=findViewById(R.id.btn_pwmok1);
        ok2=findViewById(R.id.btn_pwmok2);
        ok3=findViewById(R.id.btn_pwmok3);
        ok4=findViewById(R.id.btn_pwmok4);

        lookpwm=findViewById(R.id.textView_pwm);
        tvpwm1=findViewById(R.id.textView_pwm1);
        tvpwm2=findViewById(R.id.textView_pwm2);
        tvpwm3=findViewById(R.id.textView_pwm3);
        tvpwm4=findViewById(R.id.textView_pwm4);

        etpwm1=findViewById(R.id.editText_pwm1);
        etpwm2=findViewById(R.id.editText_pwm2);
        etpwm3=findViewById(R.id.editText_pwm3);
        etpwm4=findViewById(R.id.editText_pwm4);


        ok1.setOnClickListener(this);
        ok2.setOnClickListener(this);
        ok3.setOnClickListener(this);
        ok4.setOnClickListener(this);


        /*检查手机是否支持蓝牙*/
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //表明此手机不支持蓝牙
            Toast.makeText(MainActivity.this, "未发现蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setChecked(true);
        }
        String name = bluetoothAdapter.getName();
        //获取本地蓝牙地址
        String address = bluetoothAdapter.getAddress();
        //打印相关信息
        Log.i("BLE Name", name);
        Log.i("BLE Address", address);

        /*添加蓝牙列表*/
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothList.setAdapter(adapter);
        bluetoothList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                strMacAddress = adapter.getItem(i);
                adapterView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*蓝牙总开关*/
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启，则开启蓝牙
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    bluetoothAdapter.disable();
                    Toast.makeText(MainActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*蓝牙搜索*/
        bluetoothSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);//震动
                if (bluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "未发现蓝牙设备", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "蓝牙设备未开启", Toast.LENGTH_SHORT).show();
                }

                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        adapter.remove(device.getAddress());
                        adapter.add(device.getAddress());
                    }
                } else {
                    //注册，当一个设备被发现时调用mReceive
                    Log.i("seach", "hhhhhh");
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);
                }
            }
        });

        /*蓝牙连接或断开*/
        bluetoothConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (strMacAddress == null) {
                    Toast.makeText(MainActivity.this, "请先搜索设备", Toast.LENGTH_SHORT).show();
                } else {
                    if (booleanConnect == false) {

                        Intent i = new Intent(MainActivity.this, MyService.class);
                        i.putExtra("Mac", strMacAddress);
                        startService(i);

                        bluetoothConnect.setEnabled(false);
                    }
                    else // 断开蓝牙
                    {
                        booleanConnect = false;
                        //stopService(new Intent(MainActivity.this, MyService.class));
                        bluetoothConnect.setText("连接");

                        Intent intent = new Intent();//创建Intent对象
                        intent.setAction("android.intent.action.cmd");
                        intent.putExtra("cmd", CMD_STOP_SERVICE);
                        sendBroadcast(intent);//发送广播连接蓝牙

                    }

                }
            }
        });

        mbtiaocan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                Intent intent =new Intent(MainActivity.this,OsciPirmeICS.class);
                startActivity(intent);
            }
        });

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("fond:", "mReceiver");

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 已经配对的则跳过
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapter.add(device.getAddress());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  //搜索结束
                Log.e("fond:", "ACTION_DISCOVERY_FINISHED");
                if (adapter.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "没有搜索到设备", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };
    /*********************************************************************************************/
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("OnStart", "Start");
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("onDestroy", "Destroy");
        if(receiver!=null){
            MainActivity.this.unregisterReceiver(receiver);
        }
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i("onResume", "Resume");
        receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.bluetooth.admin.bluetooth");
        MainActivity.this.registerReceiver(receiver,filter);
    }

    public void showToast(String str){//显示提示信息
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_pwmok1 :
                double a=Double.parseDouble(String.valueOf(etpwm1.getText()));
                Log.e("number", String.valueOf(a));
                tvpwm1.setText(String.valueOf(a));
                SendDat('A',a);
                break;
            case R.id.btn_pwmok2 :
                double b=Double.parseDouble(String.valueOf(etpwm2.getText()));
                tvpwm2.setText(String.valueOf(b));
                SendDat('B',b);
                break;
            case R.id.btn_pwmok3 :
                double c=Double.parseDouble(String.valueOf(etpwm3.getText()));
                tvpwm3.setText(String.valueOf(c));
                SendDat('C',c);
                break;
            case R.id.btn_pwmok4 :
                double d=Double.parseDouble(String.valueOf(etpwm4.getText()));
                tvpwm4.setText(String.valueOf(d));
                SendDat('D',d);
                break;
        }
    }

    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals("android.intent.action.bluetooth.admin.bluetooth")){
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");

                if(cmd == CMD_SHOW_TOAST){
                    String str = bundle.getString("str");
                    showToast(str);
                    if ("连接成功建立，可以开始操控了!".equals(str))
                    {
                        booleanConnect = true;
                        bluetoothConnect.setEnabled(true);
                        bluetoothConnect.setText("断开");
                    }
                    else if("连接失败".equals(str))
                    {
                        booleanConnect = false;
                        bluetoothConnect.setEnabled(true);
                        bluetoothConnect.setText("连接");
                    }
                }
                else if(cmd == CMD_SYSTEM_EXIT){
                    System.exit(0);
                }
                else if(cmd == CMD_RECEIVE_DATA)
                {
                    String strtemp = bundle.getString("str");
                    Log.i("tv",strtemp);
                    String s=strtemp.substring(1,strtemp.length()-1);
                    lookpwm.setText(s);
                }

            }
        }
    }

    public void SendBlueToothProtocol(String value){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        intent.putExtra("cmd", CMD_SEND_DATA);
        intent.putExtra("command", (byte)0x00);
        intent.putExtra("value", value);
        sendBroadcast(intent);//发送广播
    }


    public void SendDat(char ss,double a){
        int b=(int)(a*10);
        Log.e("number", String.valueOf(b));
        int a1,a2,a3,a4,a5;
        a1=b/10000;
        a2=(b/1000)%10;
        a3=(b/100)%10;
        a4=(b/10)%10;
        a5=b%10;
        String s1="$";
        String s2="#";
        String s=s1+ss+a1+a2+a3+a4+a5+s2;
        SendBlueToothProtocol(s);
    }
}
