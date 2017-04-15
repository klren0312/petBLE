package com.wdd.bledemo.activity;

import java.util.ArrayList;

import com.wdd.bledemo.R;
import com.wdd.bledemo.R.id;
import com.wdd.bledemo.R.layout;
import com.wdd.bledemo.adapter.DeviceAdapter;
import com.wdd.bledemo.entity.EntityDevice;
import com.wdd.bledemo.service.BLEService;
import com.wdd.bledemo.utils.BluetoothController;
import com.wdd.bledemo.utils.ConstantUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 主界面
 * @author wangdandan
 *
 */
public class MainActivity extends Activity {
	private Button search;
	private ListView listview;
	private ArrayList<EntityDevice> list = new ArrayList<EntityDevice>();
	private DeviceAdapter adapter;
	private Intent intentService;
	private MsgReceiver receiver;

	private TextView connectedDevice;
	private TextView receivedMessage;

	private EditText editSend;
	private Button btnSend;
	BluetoothController controller = BluetoothController.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivity_main);
		initView();
		initService();
		initData();
		addListener();
		registerReceiver();
		
	}

	private void addListener() {
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
									long arg3) {
				BluetoothController.getInstance().connect(list.get(index));
			}
		});

		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				list.clear();//清空上次的搜索结果
				connectedDevice.setText("");
				adapter.notifyDataSetChanged();
				if(!BluetoothController.getInstance().initBLE()){//手机不支持蓝牙
					Toast.makeText(MainActivity.this, "您的手机不支持蓝牙",
							Toast.LENGTH_SHORT).show();
					return;//手机不支持蓝牙就啥也不用干了，关电脑睡觉去吧
				}
				if (!BluetoothController.getInstance().isBleOpen()) {// 如果蓝牙还没有打开
					Toast.makeText(MainActivity.this, "请打开蓝牙",
							Toast.LENGTH_SHORT).show();
					return;
				}
				new GetDataTask().execute();// 搜索任务

			}
		});

		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String str="a";
				if(str!=null&&str.length()>0){
					controller.write(str.getBytes());
				}
				else {
					toast("请填上要发送的内容");
				}

			}
		});
	}

	private void initData() {
		adapter = new DeviceAdapter(this, list);
		listview.setAdapter(adapter);
	}

	/**
	 * 开始服务, 初始化蓝牙
	 */
	private void initService() {
		//开始服务
		intentService = new Intent(MainActivity.this,BLEService.class);
		startService(intentService);
		// 初始化蓝牙
		BluetoothController.getInstance().initBLE();
	}

	/**
	 * findViewById
	 */
	private void initView() {
		connectedDevice=(TextView) findViewById(R.id.connected_device);
		receivedMessage=(TextView) findViewById(R.id.received_message);
		listview = (ListView) findViewById(R.id.list_devices);
		editSend=(EditText) findViewById(R.id.edit_send);
		btnSend=(Button) findViewById(R.id.btn_send);
		search = (Button) findViewById(R.id.btn_search);
	}

	private void registerReceiver() {
		receiver=new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
		intentFilter.addAction(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
		intentFilter.addAction(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
		intentFilter.addAction(ConstantUtils.ACTION_STOP_CONNECT);
		registerReceiver(receiver, intentFilter);
	}

	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			if(BluetoothController.getInstance().isBleOpen()){
				BluetoothController.getInstance().startScanBLE();
			};// 开始扫描
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
		}
	}

	/**
	 * 广播接收器
	 *
	 */
	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					ConstantUtils.ACTION_UPDATE_DEVICE_LIST)) {
				String name = intent.getStringExtra("name");
				String address = intent.getStringExtra("address");
				boolean found=false;//记录该条记录是否已在list中，
				for(EntityDevice device:list){
					if(device.getAddress().equals(address)){
						found=true;
						break;
					}
				}// for
				if(!found){
					EntityDevice temp = new EntityDevice();
					temp.setName(name);
					temp.setAddress(address);
					list.add(temp);
					adapter.notifyDataSetChanged();
				}
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE)){
				connectedDevice.setText("连接的蓝牙是："+intent.getStringExtra("address"));
			}

			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_STOP_CONNECT)){
				connectedDevice.setText("");
				toast("连接已断开");
			}

			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE)){
				receivedMessage.append("\n\r"+intent.getStringExtra("message"));
			}
		}
	}


	private void toast(String str) {
		Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(intentService);
		unregisterReceiver(receiver);
	}

}

