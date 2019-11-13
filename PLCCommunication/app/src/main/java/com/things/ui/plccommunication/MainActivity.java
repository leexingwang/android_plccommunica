package com.things.ui.plccommunication;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import HslCommunication.Core.Net.NetHandle;
import HslCommunication.Core.Net.NetworkBase.NetworkDoubleBase;
import HslCommunication.Core.Transfer.DataFormat;
import HslCommunication.Core.Types.ActionOperateExThree;
import HslCommunication.Core.Types.ActionOperateExTwo;
import HslCommunication.Core.Types.OperateResult;
import HslCommunication.Core.Types.OperateResultExOne;
import HslCommunication.Enthernet.ComplexNet.NetComplexClient;
import HslCommunication.Enthernet.PushNet.NetPushClient;
import HslCommunication.Enthernet.SimplifyNet.NetSimplifyClient;
import HslCommunication.ModBus.ModbusTcpNet;
import HslCommunication.Profinet.Keyence.KeyenceMcAsciiNet;
import HslCommunication.Profinet.Keyence.KeyenceMcNet;
import HslCommunication.Profinet.Melsec.MelsecA1ENet;
import HslCommunication.Profinet.Melsec.MelsecMcAsciiNet;
import HslCommunication.Profinet.Melsec.MelsecMcNet;
import HslCommunication.Profinet.Omron.OmronFinsNet;
import HslCommunication.Profinet.Siemens.SiemensFetchWriteNet;
import HslCommunication.Profinet.Siemens.SiemensPLCS;
import HslCommunication.Profinet.Siemens.SiemensS7Net;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText plc_communication_ip_address;
    private EditText plc_communication_port;
    private EditText plc_communication_operate_address;
    private EditText plc_communication_operate_values;
    private Spinner plc_communication_type_spinner;
    private Spinner plc_communication_type_spinner_details;
    private Button plc_communication_commit_read;
    private Button plc_communication_commit_write;
    private Button plc_communication_communication_done;
    private TextView plc_communication_commit_results;
    private String current_plc_name;
    private SiemensS7Net networkDoubleBaseClint;
    private String current_plc_details_name;
    private String plc_communication_ip_address_value;
    private String plc_communication_port_value;
    private String plc_communication_operate_read_or_write_address_value;
    private String plc_communication_operate_write_value;
    private OperateResult connect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plc_communication_ip_address = findViewById(R.id.plc_communication_ip_address);
        plc_communication_port = findViewById(R.id.plc_communication_port);
        plc_communication_operate_address = findViewById(R.id.plc_communication_operate_address);
        plc_communication_operate_values = findViewById(R.id.plc_communication_operate_values);
        plc_communication_type_spinner = findViewById(R.id.plc_communication_type_spinner);
        plc_communication_type_spinner_details = findViewById(R.id.plc_communication_type_spinner_details);
        plc_communication_commit_read = findViewById(R.id.plc_communication_commit_read);
        plc_communication_commit_write = findViewById(R.id.plc_communication_commit_write);
        plc_communication_communication_done = findViewById(R.id.plc_communication_communication_done);
        plc_communication_commit_results = findViewById(R.id.plc_communication_commit_results);
        plc_communication_commit_read.setOnClickListener(this);
        plc_communication_commit_write.setOnClickListener(this);
        plc_communication_communication_done.setOnClickListener(this);
        initSpinner();
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            plc_communication_commit_results.setMovementMethod(ScrollingMovementMethod.getInstance());
            plc_communication_commit_results.setText(plc_communication_commit_results.getText().toString() + "\n" + "读取结果：" + message.obj + "\n");
            return false;
        }
    });

    private void initSpinner() {

        final String[] plcName = getResources().getStringArray(R.array.PLC_Name);
        //创建ArrayAdapter对象
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plcName);
        plc_communication_type_spinner.setAdapter(adapter);
        /**选项选择监听*/
        plc_communication_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                current_plc_name = plcName[position];
                selectPLCDetailsType(current_plc_name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void selectPLCDetailsType(String current_plc_name) {
        if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[0])) {
            final String[] plcName = getResources().getStringArray(R.array.Siemens_Details_TYPE);
            //创建ArrayAdapter对象
            selectDetailsType(plcName);
        }
//        if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[0])) {
//            final String[] plcName = getResources().getStringArray(R.array.Keyence_Details_TYPE);
//            //创建ArrayAdapter对象
//            selectDetailsType(plcName);
//        }
//        else if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[1])) {
//            final String[] plcName = getResources().getStringArray(R.array.Melsec_Details_TYPE);
//            selectDetailsType(plcName);
//
//        } else if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[2])) {
//            final String[] plcName = getResources().getStringArray(R.array.Omron_Details_TYPE);
//            selectDetailsType(plcName);
//        } else if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[3])) {
//            final String[] plcName = getResources().getStringArray(R.array.Siemens_Details_TYPE);
//            selectDetailsType(plcName);
//        }
    }

    private void selectDetailsType(final String[] plcName) {
        //创建ArrayAdapter对象
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plcName);
        plc_communication_type_spinner_details.setAdapter(adapter);
        /**选项选择监听*/
        plc_communication_type_spinner_details.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                current_plc_details_name = plcName[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 创建不同类型的链接客服端
     *
     * @param current_plc_name
     */
    private void cratePLCClient(String current_plc_name, String current_plc_details_name) {
//        if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[0])) {
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Keyence_Details_TYPE)[0]))
//                networkDoubleBaseClint = new KeyenceMcAsciiNet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Keyence_Details_TYPE)[1]))
//                networkDoubleBaseClint = new KeyenceMcNet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//        } else if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[1])) {
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Melsec_Details_TYPE)[0]))
//                networkDoubleBaseClint = new MelsecA1ENet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Melsec_Details_TYPE)[1]))
//                networkDoubleBaseClint = new MelsecMcAsciiNet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Melsec_Details_TYPE)[2]))
//                networkDoubleBaseClint = new MelsecMcNet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//        } else if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[2])) {
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Omron_Details_TYPE)[0]))
//                networkDoubleBaseClint = new OmronFinsNet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//        } else if (TextUtils.equals(current_plc_name, getResources().getStringArray(R.array.PLC_Name)[3])) {
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Siemens_Details_TYPE)[0]))
//                networkDoubleBaseClint = new SiemensFetchWriteNet(plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
//            if (TextUtils.equals(current_plc_details_name, getResources().getStringArray(R.array.Siemens_Details_TYPE)[1]))
//                networkDoubleBaseClint = new SiemensS7Net(SiemensPLCS.S1200, plc_communication_ip_address_value);
//        }

        networkDoubleBaseClint = new SiemensS7Net(SiemensPLCS.S1200, plc_communication_ip_address_value, Integer.parseInt(plc_communication_port_value));
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.plc_communication_commit_write) {
            plc_communication_ip_address_value = plc_communication_ip_address.getText().toString();
            plc_communication_port_value = plc_communication_port.getText().toString();
            plc_communication_operate_read_or_write_address_value = plc_communication_operate_address.getText().toString();
            plc_communication_operate_write_value = plc_communication_operate_values.getText().toString();
            if (TextUtils.isEmpty(plc_communication_operate_read_or_write_address_value)) {
                Snackbar.make(view, "操作地址", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            if (TextUtils.isEmpty(plc_communication_operate_write_value)) {
                Snackbar.make(view, "写入数据为空", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=Integer.parseInt(plc_communication_operate_write_value);
                    ((SiemensS7Net) networkDoubleBaseClint).Write(plc_communication_operate_read_or_write_address_value, i>0?true:false);
                    Boolean value = ((SiemensS7Net) networkDoubleBaseClint).ReadBool(plc_communication_operate_read_or_write_address_value).Content;
                    // 上面是初始化
                    System.out.println(value);
                    Message message = handler.obtainMessage();
                    message.obj = value.toString();
                    handler.sendMessage(message);
                }
            }).start();
        }
        if (view.getId() == R.id.plc_communication_commit_read) {
            plc_communication_ip_address_value = plc_communication_ip_address.getText().toString();
            plc_communication_port_value = plc_communication_port.getText().toString();
            plc_communication_operate_read_or_write_address_value = plc_communication_operate_address.getText().toString();
            if (TextUtils.isEmpty(plc_communication_operate_read_or_write_address_value)) {
                Snackbar.make(view, "操作地址", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    Boolean value = ((SiemensS7Net) networkDoubleBaseClint).ReadBool(plc_communication_operate_read_or_write_address_value).Content;
                    // 上面是初始化
                    System.out.println(value);
                    Message message = handler.obtainMessage();
                    message.obj = value.toString();
                    handler.sendMessage(message);
                }
            }).start();


        }

        if (view.getId() == R.id.plc_communication_communication_done) {
            plc_communication_ip_address_value = plc_communication_ip_address.getText().toString();
            plc_communication_port_value = plc_communication_port.getText().toString();
            plc_communication_operate_read_or_write_address_value = plc_communication_operate_address.getText().toString();
            plc_communication_operate_write_value = plc_communication_operate_values.getText().toString();
            if (TextUtils.isEmpty(plc_communication_ip_address_value)) {
                Snackbar.make(view, "ip 地址为空", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            if (TextUtils.isEmpty(plc_communication_port_value)) {
                Snackbar.make(view, "端口号为空", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            if (TextUtils.isEmpty(current_plc_name) || TextUtils.isEmpty(current_plc_details_name)) {
                Snackbar.make(view, "未指定PLC类型", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cratePLCClient(current_plc_name, current_plc_details_name);
                    networkDoubleBaseClint.SetPersistentConnection();
                    connect = networkDoubleBaseClint.ConnectServer();

//
//                    SiemensS7Net siemens_net = new SiemensS7Net(SiemensPLCS.S1200,"192.168.1.195",102);
//                    OperateResult connect = siemens_net.ConnectServer();
                    if (connect.IsSuccess) {
                        System.out.println("connect success!");
                    } else {
                        System.out.println("failed:" + connect.Message);
                    }
//                    networkDoubleBaseClint.ConnectClose();
                    // 上面是初始化
                    Boolean value = networkDoubleBaseClint.ReadBool("I0.5").Content;

                    System.out.println(value);

                    System.out.println(value);
                    Message message = handler.obtainMessage();
                    message.obj = value.toString();
                    handler.sendMessage(message);
//                    SiemesTest();
                }
            }).start();

            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (connect != null && connect.IsSuccess) {
                        System.out.println("connect success!");
                        Snackbar.make(view, "链接成功", Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        Snackbar.make(view, "链接失败", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
            }, 3000);

//            networkDoubleBaseClint.ConnectClose();
        }
    }


    public static void NetSimplifyClientTest() {
        NetSimplifyClient client = new NetSimplifyClient("127.0.0.1", 12345);

        OperateResultExOne<String> read = client.ReadFromServer(new NetHandle(2), "测试数据");
        if (read.IsSuccess) {
            System.out.println(read.Content);
        } else {
            System.out.println("读取失败：" + read.Message);
        }
    }

    private static void MelsecA1ETest() {
        MelsecA1ENet melsec_net = new MelsecA1ENet("192.168.0.100", 5000);
        melsec_net.SetPersistentConnection();

        boolean[] M100 = melsec_net.ReadBool("M100", (short) 1).Content;           // 读取M100是否通，十进制地址
        boolean[] X20 = melsec_net.ReadBool("X20", (short) 1).Content;             // 读取X20是否通，八进制地址
        boolean[] Y20 = melsec_net.ReadBool("Y20", (short) 1).Content;             // 读取Y20是否通，八进制地址
        short short_D1000 = melsec_net.ReadInt16("D1000").Content;                 // 读取D1000的short值
        int int_D1000 = melsec_net.ReadInt32("D1000").Content;                     // 读取D1000-D1001组成的int数据
        float float_D1000 = melsec_net.ReadFloat("D1000").Content;                 // 读取D1000-D1001组成的float数据
        long long_D1000 = melsec_net.ReadInt64("D1000").Content;                   // 读取D1000-D1003组成的long数据
        double double_D1000 = melsec_net.ReadDouble("D1000").Content;              // 读取D1000-D1003组成的double数据
        String str_D1000 = melsec_net.ReadString("D1000", (short) 10).Content;     // 读取D1000-D1009组成的条码数据


        melsec_net.Write("M100", new boolean[]{true});                            // 写入M100为通
        melsec_net.Write("Y20", new boolean[]{true});                             // 写入Y20为通
        melsec_net.Write("X20", new boolean[]{true});                             // 写入X20为通
        melsec_net.Write("D1000", (short) 1234);                                  // 写入D1000  short值  ,W3C0,R3C0 效果是一样的
        melsec_net.Write("D1000", 1234566);                                // 写入D1000  int值
        melsec_net.Write("D1000", 123.456f);                               // 写入D1000  float值
        melsec_net.Write("D1000", 123.456d);                               // 写入D1000  double值
        melsec_net.Write("D1000", 123456661235123534L);                    // 写入D1000  long值
        melsec_net.Write("D1000", "K123456789");                           // 写入D1000  string值


        OperateResultExOne<boolean[]> read = melsec_net.ReadBool("M100", (short) 10);
        if (read.IsSuccess) {
            boolean m100 = read.Content[0];
            boolean m101 = read.Content[1];
            boolean m102 = read.Content[2];
            boolean m103 = read.Content[3];
            boolean m104 = read.Content[4];
            boolean m105 = read.Content[5];
            boolean m106 = read.Content[6];
            boolean m107 = read.Content[7];
            boolean m108 = read.Content[8];
            boolean m109 = read.Content[9];
        } else {
            System.out.print("读取失败：" + read.Message);
        }


        OperateResultExOne<byte[]> read1 = melsec_net.Read("D100", (short) 5);
        if (read1.IsSuccess) {
            short D100 = melsec_net.getByteTransform().TransInt16(read1.Content, 0);
            short D101 = melsec_net.getByteTransform().TransInt16(read1.Content, 2);
            short D102 = melsec_net.getByteTransform().TransInt16(read1.Content, 4);
            short D103 = melsec_net.getByteTransform().TransInt16(read1.Content, 6);
            short D104 = melsec_net.getByteTransform().TransInt16(read1.Content, 8);
        } else {
            System.out.print("读取失败：" + read1.Message);
        }


        //解析复杂数据
        OperateResultExOne<byte[]> read3 = melsec_net.Read("D4000", (short) 10);
        if (read3.IsSuccess) {
            double 温度 = melsec_net.getByteTransform().TransInt16(read3.Content, 0) / 10d;//索引很重要
            double 压力 = melsec_net.getByteTransform().TransInt16(read3.Content, 2) / 100d;
            boolean IsRun = melsec_net.getByteTransform().TransInt16(read3.Content, 4) == 1;
            int 产量 = melsec_net.getByteTransform().TransInt32(read3.Content, 6);
            String 规格 = melsec_net.getByteTransform().TransString(read3.Content, 10, 10, "ascii");
        } else {
            System.out.print("读取失败：" + read3.Message);
        }

        // 写入测试，M100-M104 写入测试 此处写入后M100:通 M101:断 M102:断 M103:通 M104:通
        boolean[] values = new boolean[]{true, false, false, true, true};
        OperateResult write = melsec_net.Write("M100", values);
        if (write.IsSuccess) {
            System.out.print("写入成功");
        } else {
            System.out.print("写入失败：" + write.Message);
        }


        OperateResultExOne<Boolean> read2 = melsec_net.ReadBool("M100");
        if (read2.IsSuccess) {
            System.out.println(read2.Content);
        } else {
            System.out.println("读取失败：" + read.Message);
        }
    }

    private static void MelsecTest() {
        MelsecMcNet melsec_net = new MelsecMcNet("192.168.1.192", 6001);


        boolean[] M100 = melsec_net.ReadBool("M100", (short) 1).Content;            // 读取M100是否通，十进制地址
        boolean[] X1A0 = melsec_net.ReadBool("X1A0", (short) 1).Content;            // 读取X1A0是否通，十六进制地址
        boolean[] Y1A0 = melsec_net.ReadBool("Y1A0", (short) 1).Content;            // 读取Y1A0是否通，十六进制地址
        boolean[] B1A0 = melsec_net.ReadBool("B1A0", (short) 1).Content;            // 读取B1A0是否通，十六进制地址
        short short_D1000 = melsec_net.ReadInt16("D1000").Content;                 // 读取D1000的short值  ,W3C0,R3C0 效果是一样的
        int int_D1000 = melsec_net.ReadInt32("D1000").Content;                     // 读取D1000-D1001组成的int数据
        float float_D1000 = melsec_net.ReadFloat("D1000").Content;                 // 读取D1000-D1001组成的float数据
        long long_D1000 = melsec_net.ReadInt64("D1000").Content;                   // 读取D1000-D1003组成的long数据
        double double_D1000 = melsec_net.ReadDouble("D1000").Content;              // 读取D1000-D1003组成的double数据
        String str_D1000 = melsec_net.ReadString("D1000", (short) 10).Content;     // 读取D1000-D1009组成的条码数据


        melsec_net.Write("M100", new boolean[]{true});                          // 写入M100为通
        melsec_net.Write("Y1A0", new boolean[]{true});                        // 写入Y1A0为通
        melsec_net.Write("X1A0", new boolean[]{true});                        // 写入X1A0为通
        melsec_net.Write("B1A0", new boolean[]{true});                        // 写入B1A0为通
        melsec_net.Write("D1000", (short) 1234);                                   // 写入D1000  short值  ,W3C0,R3C0 效果是一样的
        melsec_net.Write("D1000", 1234566);                                // 写入D1000  int值
        melsec_net.Write("D1000", 123.456f);                               // 写入D1000  float值
        melsec_net.Write("D1000", 123.456d);                               // 写入D1000  double值
        melsec_net.Write("D1000", 123456661235123534L);                    // 写入D1000  long值
        melsec_net.Write("D1000", "K123456789");                           // 写入D1000  string值


        OperateResultExOne<boolean[]> read = melsec_net.ReadBool("M100", (short) 10);
        if (read.IsSuccess) {
            boolean m100 = read.Content[0];
            boolean m101 = read.Content[1];
            boolean m102 = read.Content[2];
            boolean m103 = read.Content[3];
            boolean m104 = read.Content[4];
            boolean m105 = read.Content[5];
            boolean m106 = read.Content[6];
            boolean m107 = read.Content[7];
            boolean m108 = read.Content[8];
            boolean m109 = read.Content[9];
        } else {
            System.out.print("读取失败：" + read.Message);
        }


        OperateResultExOne<byte[]> read1 = melsec_net.Read("D100", (short) 5);
        if (read1.IsSuccess) {
            short D100 = melsec_net.getByteTransform().TransByte(read1.Content, 0);
            short D101 = melsec_net.getByteTransform().TransByte(read1.Content, 2);
            short D102 = melsec_net.getByteTransform().TransByte(read1.Content, 4);
            short D103 = melsec_net.getByteTransform().TransByte(read1.Content, 6);
            short D104 = melsec_net.getByteTransform().TransByte(read1.Content, 8);
        } else {
            System.out.print("读取失败：" + read1.Message);
        }


        //解析复杂数据
        OperateResultExOne<byte[]> read3 = melsec_net.Read("D4000", (short) 10);
        if (read3.IsSuccess) {
            double 温度 = melsec_net.getByteTransform().TransInt16(read3.Content, 0) / 10d;//索引很重要
            double 压力 = melsec_net.getByteTransform().TransInt16(read3.Content, 2) / 100d;
            boolean IsRun = melsec_net.getByteTransform().TransInt16(read3.Content, 4) == 1;
            int 产量 = melsec_net.getByteTransform().TransInt32(read3.Content, 6);
            String 规格 = melsec_net.getByteTransform().TransString(read3.Content, 10, 10, "ascii");
        } else {
            System.out.print("读取失败：" + read3.Message);
        }

        // 写入测试，M100-M104 写入测试 此处写入后M100:通 M101:断 M102:断 M103:通 M104:通
        boolean[] values = new boolean[]{true, false, false, true, true};
        OperateResult write = melsec_net.Write("M100", values);
        if (write.IsSuccess) {
            System.out.print("写入成功");
        } else {
            System.out.print("写入失败：" + write.Message);
        }


        OperateResultExOne<Boolean> read2 = melsec_net.ReadBool("M100");
        if (read2.IsSuccess) {
            System.out.println(read2.Content);
        } else {
            System.out.println("读取失败：" + read.Message);
        }
    }

    private static void MelsecAsciiTest() {

        MelsecMcAsciiNet melsec = new MelsecMcAsciiNet("192.168.1.192", 6001);
        OperateResultExOne<short[]> read = melsec.ReadInt16("D100", (short) 2);
        if (read.IsSuccess) {
            System.out.println(Arrays.toString(read.Content));
        } else {
            System.out.println(read.ToMessageShowString());
        }
    }


    private static void SiemesTest() {
        SiemensS7Net siemens_net = new SiemensS7Net(SiemensPLCS.S1200, "192.168.1.195", 102);
        OperateResult connect = siemens_net.ConnectServer();
        if (connect.IsSuccess) {
            System.out.println("connect success!");
        } else {
            System.out.println("failed:" + connect.Message);
        }
        siemens_net.ConnectClose();


        // 上面是初始化
        System.out.println(siemens_net.ReadByte("M100").Content);

        byte m100_byte = siemens_net.ReadByte("M100").Content;
        short m100_short = siemens_net.ReadInt16("M100").Content;
        int m100_int = siemens_net.ReadInt32("M100").Content;
        long m100_long = siemens_net.ReadInt64("M100").Content;
        float m100_float = siemens_net.ReadFloat("M100").Content;
        double m100_double = siemens_net.ReadDouble("M100").Content;
        String m100_string = siemens_net.ReadString("M100", (short) 10).Content;

        siemens_net.Write("M100", (byte) 123);
        siemens_net.Write("M100", (short) 123);
        siemens_net.Write("M100", (int) 123);
        siemens_net.Write("M100", (long) 123);
        siemens_net.Write("M100", 123.456f);
        siemens_net.Write("M100", 123.456d);
        siemens_net.Write("M100", "1234567890");

        OperateResultExOne<byte[]> read = siemens_net.Read("M100", (short) 10);
        {
            if (read.IsSuccess) {
                byte m100 = read.Content[0];
                byte m101 = read.Content[1];
                byte m102 = read.Content[2];
                byte m103 = read.Content[3];
                byte m104 = read.Content[4];
                byte m105 = read.Content[5];
                byte m106 = read.Content[6];
                byte m107 = read.Content[7];
                byte m108 = read.Content[8];
                byte m109 = read.Content[9];
            } else {
                // 发生了异常
            }
        }

    }

    private static void PushNetTest() {
        NetPushClient client = new NetPushClient("127.0.0.1", 23467, "A");
        OperateResult connect = client.CreatePush(new ActionOperateExTwo<NetPushClient, String>() {
            @Override
            public void Action(NetPushClient content1, String content2) {
                System.out.println(content2);
            }
        });
        if (connect.IsSuccess) {
            System.out.println("连接成功!");
        } else {
            System.out.println("连接失败!" + connect.Message);
        }
    }

    private static void ModbusTcpTets() {
        ModbusTcpNet modbusTcpNet = new ModbusTcpNet("127.0.0.1", 502, (byte) 0x01);
        // 当你需要指定格式的数据解析时，就需要设置下面的这个信息
        modbusTcpNet.setDataFormat(DataFormat.BADC);
        OperateResultExOne<Double> read = modbusTcpNet.ReadDouble("s=2;x=4;200");
        if (read.IsSuccess) {
            System.out.println(read.Content);
        } else {
            System.out.println(read.Message);
        }

        modbusTcpNet.Write("100", new int[]{12345, -12345});
    }

    private static void NetComplexClientTest() {
        System.out.println("Hello World!等待10s关闭");
        NetComplexClient client = new NetComplexClient();
        client.setIpAddress("127.0.0.1");
        client.setPort(12346);
        client.setClientAlias("测试1");
        client.AcceptString = new ActionOperateExThree<NetComplexClient, NetHandle, String>() {
            @Override
            public void Action(NetComplexClient content1, NetHandle content2, String content3) {
                System.out.println("Handle:" + content2.get_CodeValue() + "  Value:" + content3);
            }
        };


        client.ClientStart();

//        client.Send(new NetHandle(1),"asdasdi阿斯达阿斯达");
//        System.out.println(client.getDelayTime());
//        Thread.sleep(100000);
//        client.ClientClose();


    }


}
