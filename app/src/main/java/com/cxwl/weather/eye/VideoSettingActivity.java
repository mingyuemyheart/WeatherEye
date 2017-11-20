package com.cxwl.weather.eye;

/**
 * 视频操作
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.adapter.ForePositionAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CustomHttpClient;
import com.cxwl.weather.eye.utils.CustomHttpClient2;
import com.cxwl.weather.eye.view.RoundMenuView;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

@SuppressLint("SimpleDateFormat")
public class VideoSettingActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private ImageView ivBack = null;
	private RelativeLayout reTitle = null;
	private TextView tvTitle = null;
	private EyeDto data = null;
	private Configuration configuration = null;//方向监听器
	private ProgressBar progressBar = null;
	private TXCloudVideoView mPlayerView = null;
	private TXLivePlayer mLivePlayer = null;

	private RoundMenuView roundMenuView = null;
	private float startDegree = 0;//开始角度
	private float clickDegree = 0;//选中角度
	private ImageView ivMenuDir = null;
	private MyBroadCastReceiver mReceiver = null;
	private String commandBaseUrl = "https://tqwy.tianqi.cn/tianqixy/userInfo/facilordeinset";//发送指令baseUrl
	//操作类型 1 上，2下，3左，4右，5右上，6左上，7右下，8左下，
			//10变倍大，11变倍小，13聚焦近，14聚焦远，17打开光圈，18关闭光圈，19打开雨刷，
			//20关闭雨刷，23巡航开始，24巡航关闭，30亮度，31色度，32对比度，33饱和度，28预位置
	private String orderType = "1";
	private String OrederValue1 = "50";//0-100,水平、垂直速度、亮度、对比度、饱和度、色度，默认为50
	private int speed = 50;//速度
	private int brightness = 50;//亮度
	private int contrast = 50;//对比度
	private int saturation = 50;//饱和度
	private int chroma = 50;//色度
	
	private ImageView ivMinuse1, ivMinuse2, ivMinuse3, ivMinuse4, ivMinuse5;
	private ImageView ivPlus1, ivPlus2, ivPlus3, ivPlus4, ivPlus5;
	private TextView tvValue1 = null;
	private SeekBar seekBar1, seekBar2, seekBar3, seekBar4;
	private TextView tvSeekBar1, tvSeekBar2, tvSeekBar3, tvSeekBar4;
	private LinearLayout ll8 = null;
	private TextView tvForePosition = null;
	private static final int HANDLER_SPEED_MINUSE_DOWN = 2;
	private static final int HANDLER_SPEED_MINUSE_UP = 3;
	private static final int HANDLER_SPEED_PLUS_DOWN = 4;
	private static final int HANDLER_SPEED_PLUS_UP = 5;
	private boolean isClick = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videosetting);
		mContext = this;
		initBroadCastReceiver();
		initWidget();
		initRoundMenuView();
	}
	
	private void initBroadCastReceiver() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONST.CIRCLE_CONTROLER);
		registerReceiver(mReceiver, intentFilter);
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("视频设置");
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.eye_btn_close);
		ivMenuDir = (ImageView) findViewById(R.id.ivMenuDir);
		ivMinuse1 = (ImageView) findViewById(R.id.ivMinuse1);
		ivMinuse1.setOnTouchListener(ivMinuse1Listener);
		ivMinuse2 = (ImageView) findViewById(R.id.ivMinuse2);
		ivMinuse2.setOnClickListener(this);
		ivMinuse3 = (ImageView) findViewById(R.id.ivMinuse3);
		ivMinuse3.setOnClickListener(this);
		ivMinuse4 = (ImageView) findViewById(R.id.ivMinuse4);
		ivMinuse4.setOnClickListener(this);
		ivMinuse5 = (ImageView) findViewById(R.id.ivMinuse5);
		ivMinuse5.setOnClickListener(this);
		ivPlus1 = (ImageView) findViewById(R.id.ivPlus1);
		ivPlus1.setOnTouchListener(ivPlus1Listener);
		ivPlus2 = (ImageView) findViewById(R.id.ivPlus2);
		ivPlus2.setOnClickListener(this);
		ivPlus3 = (ImageView) findViewById(R.id.ivPlus3);
		ivPlus3.setOnClickListener(this);
		ivPlus4 = (ImageView) findViewById(R.id.ivPlus4);
		ivPlus4.setOnClickListener(this);
		ivPlus5 = (ImageView) findViewById(R.id.ivPlus5);
		ivPlus5.setOnClickListener(this);
		tvValue1 = (TextView) findViewById(R.id.tvValue1);
		tvSeekBar1 = (TextView) findViewById(R.id.tvSeekBar1);
		tvSeekBar2 = (TextView) findViewById(R.id.tvSeekBar2);
		tvSeekBar3 = (TextView) findViewById(R.id.tvSeekBar3);
		tvSeekBar4 = (TextView) findViewById(R.id.tvSeekBar4);
		seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
		seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
		seekBar4 = (SeekBar) findViewById(R.id.seekBar4);
		seekBar1.setOnSeekBarChangeListener(seekBarListener1);
		seekBar2.setOnSeekBarChangeListener(seekBarListener2);
		seekBar3.setOnSeekBarChangeListener(seekBarListener3);
		seekBar4.setOnSeekBarChangeListener(seekBarListener4);
		ll8 = (LinearLayout) findViewById(R.id.ll8);
		ll8.setOnClickListener(this);
		tvForePosition = (TextView) findViewById(R.id.tvForePosition);
		
		tvValue1.setText(speed+"");
		tvSeekBar1.setText(brightness+"");
		tvSeekBar2.setText(contrast+"");
		tvSeekBar3.setText(saturation+"");
		tvSeekBar4.setText(chroma+"");
		
		mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
		mPlayerView.setOnClickListener(this);
		mLivePlayer = new TXLivePlayer(mContext);
		mLivePlayer.setPlayerView(mPlayerView);
		showPort();
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				if (!TextUtils.isEmpty(data.StatusUrl)) {
					asyncQueryNetState(data.StatusUrl);
				}
				asyncQueryParameter("https://tqwy.tianqi.cn/tianqixy/userInfo/obtain");
			}
		}
	}
	
	private OnTouchListener ivMinuse1Listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isClick = true;
				Thread mThread = new Thread() {
					public void run() {
						while (isClick) {
							handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_DOWN);
							speed--;
							if (speed <= 0) {
								speed = 0;
							}
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				};
				mThread.start();
				break;
			case MotionEvent.ACTION_UP:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_UP);
				break;
			case MotionEvent.ACTION_CANCEL:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_UP);
				break;

			default:
				break;
			}
			return true;
		}
	};
	
	private OnTouchListener ivPlus1Listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isClick = true;
				Thread mThread = new Thread() {
					public void run() {
						while (isClick) {
							handler.sendEmptyMessage(HANDLER_SPEED_PLUS_DOWN);
							speed++;
							if (speed >= 100) {
								speed = 100;
							}
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				};
				mThread.start();
				break;
			case MotionEvent.ACTION_UP:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_PLUS_UP);
				break;
			case MotionEvent.ACTION_CANCEL:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_PLUS_UP);
				break;

			default:
				break;
			}
			return true;
		}
	};
	
	/**
	 * 获取摄像头移动速度、亮度、饱和度、对比度、色度
	 */
	private void asyncQueryParameter(String requestUrl) {
		HttpAsyncTaskParameter task = new HttpAsyncTaskParameter();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskParameter extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskParameter() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("FID", data.fId);//设备id
	        
			nvpList.add(pair1);
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("code")) {
							String code  = object.getString("code");
							if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
								if (!object.isNull("list")) {
									JSONArray array = object.getJSONArray("list");
									JSONObject obj = array.getJSONObject(0);
									if (!obj.isNull("speed")) {
										speed = obj.getInt("speed");
										tvValue1.setText(speed+"");
									}
									if (!obj.isNull("brightness")) {
										brightness = obj.getInt("brightness");
										tvSeekBar1.setText(brightness+"");
										seekBar1.setProgress(brightness);
									}
									if (!obj.isNull("contrast")) {
										contrast = obj.getInt("contrast");
										tvSeekBar2.setText(contrast+"");
										seekBar2.setProgress(contrast);
									}
									if (!obj.isNull("saturation")) {
										saturation = obj.getInt("saturation");
										tvSeekBar3.setText(saturation+"");
										seekBar3.setProgress(saturation);
									}
									if (!obj.isNull("chroma")) {
										chroma = obj.getInt("chroma");
										tvSeekBar4.setText(chroma+"");
										seekBar4.setProgress(chroma);
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 获取内网是否可用，不可用切换位外网
	 */
	private void asyncQueryNetState(String requestUrl) {
		HttpAsyncTaskNetState task = new HttpAsyncTaskNetState();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient2.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskNetState extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskNetState() {
		}
		
		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient2.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient2.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (!TextUtils.isEmpty(requestResult) && requestResult.contains("Active connections")) {
				initTXCloudVideoView(data.streamPrivate);
			}else {
				initTXCloudVideoView(data.streamPublic);
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 初始化播放器
	 */
	private void initTXCloudVideoView(String streamUrl) {
		if (!TextUtils.isEmpty(streamUrl)) {
			mLivePlayer.startPlay(streamUrl, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
			mLivePlayer.setPlayListener(new ITXLivePlayListener() {
				@Override
				public void onPlayEvent(int arg0, Bundle arg1) {
					if (arg0 == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {//视频播放开始
						progressBar.setVisibility(View.GONE);
					}
				}
				
				@Override
				public void onNetStatus(Bundle status) {
					TextView tv = (TextView) findViewById(R.id.tv);
					tv.setText("Current status, CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)+
			                ", RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)+
			                ", SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps"+
			                ", FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS)+
			                ", ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps"+
			                ", VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps");
				}
			});
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
		}
	}
	
	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reTitle.setVisibility(View.VISIBLE);
		fullScreen(false);
		switchVideo();
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
		fullScreen(true);
		switchVideo();
	}
	
	/**
	 * 横竖屏切换视频窗口
	 */
	private void switchVideo() {
		if (mPlayerView != null) {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getRealMetrics(dm);
			int width = dm.widthPixels;
			int height = width*9/16;
			LayoutParams params = mPlayerView.getLayoutParams();
			params.width = width;
			params.height = height;
			mPlayerView.setLayoutParams(params);
		}
		if (mLivePlayer != null) {
			mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
			mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
		}
	}
	
	private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
	
	private Handler handler = new Handler() {  
	    public void handleMessage(Message msg) {  
	    	switch (msg.what) {
			case HANDLER_SPEED_MINUSE_DOWN:
				OrederValue1 = speed+"";
				tvValue1.setText(speed+"");
				ivMinuse1.setImageResource(R.drawable.eye_btn_minuse_press);
				break;
			case HANDLER_SPEED_MINUSE_UP:
				ivMinuse1.setImageResource(R.drawable.eye_btn_minuse);
				break;
			case HANDLER_SPEED_PLUS_DOWN:
				OrederValue1 = speed+"";
				tvValue1.setText(speed+"");
				ivPlus1.setImageResource(R.drawable.eye_btn_plus_press);
				break;
			case HANDLER_SPEED_PLUS_UP:
				ivPlus1.setImageResource(R.drawable.eye_btn_plus);
				break;

			default:
				break;
			}
	    	
	    };  
	};  
	
	/**
	 * 初始化圆形菜单按钮
	 */
	private void initRoundMenuView() {
		roundMenuView = (RoundMenuView) findViewById(R.id.roundMenuView);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		int width = dm.widthPixels;
		LayoutParams params = roundMenuView.getLayoutParams();
		params.width = width*3/5;
		params.height = width*3/5;
		roundMenuView.setLayoutParams(params);
		roundMenuView.setRadius(params.width/2);
		roundMenuView.setCenterXY(params.width/2, params.height/2);
		
		LayoutParams params2 = ivMenuDir.getLayoutParams();
		params2.width = params.width/3;
		params2.height = params.height/3;
		ivMenuDir.setLayoutParams(params2);
	}
	
	/**
	 * 旋转菜单动画
	 */
	private void rotateMenu(ImageView image, float startDegree, float endDegree) {
		RotateAnimation rotate = new RotateAnimation(startDegree, endDegree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(500);
		rotate.setFillAfter(true);
		image.startAnimation(rotate);
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (TextUtils.equals(intent.getAction(), CONST.CIRCLE_CONTROLER)) {//接收圆形控制器指令
				Bundle bundle = intent.getExtras();
				orderType = bundle.getString("orderType");
				OrederValue1 = speed+"";
				if (TextUtils.equals(orderType, "7")) {//右下
					clickDegree = 135;
				}else if (TextUtils.equals(orderType, "2")) {//下
					clickDegree = 180;
				}else if (TextUtils.equals(orderType, "8")) {//左下
					clickDegree = 225;
				}else if (TextUtils.equals(orderType, "3")) {//左
					clickDegree = 270;
				}else if (TextUtils.equals(orderType, "6")) {//左上
					clickDegree = 315;
				}else if (TextUtils.equals(orderType, "1")) {//上
					clickDegree = 360;
				}else if (TextUtils.equals(orderType, "5")) {//右上
					clickDegree = 45;
				}else if (TextUtils.equals(orderType, "4")) {//右
					clickDegree = 90;
				}
				if (startDegree == 0) {
					rotateMenu(ivMenuDir, startDegree, clickDegree-45);
				}else {
					rotateMenu(ivMenuDir, startDegree-45, clickDegree-45);
				}
				startDegree = clickDegree;
				asyncQueryPostCommand(commandBaseUrl);
			}
		}
	}
	
	private OnSeekBarChangeListener seekBarListener1 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			brightness = arg1;
			OrederValue1 = brightness+"";
			tvSeekBar1.setText(brightness+"");
			orderType = "30";//亮度
			asyncQueryPostCommand(commandBaseUrl);
		}
	};
	
	private OnSeekBarChangeListener seekBarListener2 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			contrast = arg1;
			OrederValue1 = contrast+"";
			tvSeekBar2.setText(contrast+"");
			orderType = "32";//对比度
			asyncQueryPostCommand(commandBaseUrl);
		}
	};
	
	private OnSeekBarChangeListener seekBarListener3 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			saturation = arg1;
			OrederValue1 = saturation+"";
			tvSeekBar3.setText(saturation+"");
			orderType = "33";//饱和度
			asyncQueryPostCommand(commandBaseUrl);
		}
	};
	
	private OnSeekBarChangeListener seekBarListener4 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			chroma = arg1;
			OrederValue1 = chroma+"";
			tvSeekBar4.setText(chroma+"");
			orderType = "31";//色度
			asyncQueryPostCommand(commandBaseUrl);
		}
	};
	
	/**
	 * 异步请求
	 */
	private void asyncQueryPostCommand(String requestUrl) {
		HttpAsyncTaskCommand task = new HttpAsyncTaskCommand();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskCommand extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskCommand() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("FID", data.fId);//设备id
	        NameValuePair pair2 = new BasicNameValuePair("FacilityZid", data.fGroupId);//设备组id
	        NameValuePair pair3 = new BasicNameValuePair("FacilityIP", data.fGroupIp);//设备组ip
	        NameValuePair pair4 = new BasicNameValuePair("OrederType", orderType);//命令类型
	        NameValuePair pair5 = new BasicNameValuePair("OrederValue1", OrederValue1);//水平速度
	        NameValuePair pair6 = new BasicNameValuePair("OrederValue2", OrederValue1);//垂直速度
	        
			nvpList.add(pair1);
			nvpList.add(pair2);
			nvpList.add(pair3);
			nvpList.add(pair4);
			nvpList.add(pair5);
			nvpList.add(pair6);
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("code")) {
							String code  = object.getString("code");
							if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
								//success
							}else if (TextUtils.equals(code, "701")) {
								if (!object.isNull("reason")) {
									String reason = object.getString("reason");
									if (!TextUtils.isEmpty(reason)) {
										Toast toast = Toast.makeText(mContext, reason, 2000);
										toast.setGravity(Gravity.TOP, 0, 300);
										toast.show();
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	private void foreDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_fore_position, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		tvMessage.setText("设置预位置");
		TextView tvNegtive = (TextView) view.findViewById(R.id.tvNegtive);
		
		final List<EyeDto> foreList = new ArrayList<EyeDto>();
		foreList.clear();
		for (int i = 0; i < 10; i++) {
			EyeDto dto = new EyeDto();
			dto.forePosition = i+"";
			foreList.add(dto);
		}
		ListView listView = (ListView) view.findViewById(R.id.listView);
		ForePositionAdapter foreAdapter = new ForePositionAdapter(mContext, foreList);
		listView.setAdapter(foreAdapter);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				dialog.dismiss();
				EyeDto dto = foreList.get(arg2);
				tvForePosition.setText(dto.forePosition);
				OrederValue1 = tvForePosition.getText().toString();
				if (TextUtils.isEmpty(tvForePosition.getText().toString())) {
					OrederValue1 = "0";
				}
				orderType = "25";//查看预位置
				asyncQueryPostCommand(commandBaseUrl);
			}
		});
		
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}
	
	private void exit() {
		if (configuration == null) {
	        finish();
	        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
		        finish();
		        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLivePlayer != null) {
			mLivePlayer.stopPlay(true);// true代表清除最后一帧画面
			mLivePlayer = null;
		}
		if (mPlayerView != null) {
			mPlayerView.onDestroy();
			mPlayerView = null;
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			exit();
			break;
		case R.id.llBack:
			finish();
			overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			break;
		case R.id.ivMinuse1:
			speed--;
			OrederValue1 = speed+"";
			tvValue1.setText(speed+"");
			break;
		case R.id.ivPlus1:
			speed++;
			OrederValue1 = speed+"";
			tvValue1.setText(speed+"");
			break;
		case R.id.ivMinuse2:
			OrederValue1 = speed+"";
			orderType = "11";//变倍小
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivPlus2:
			OrederValue1 = speed+"";
			orderType = "10";//变倍大
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivMinuse3:
			OrederValue1 = speed+"";
			orderType = "14";//聚焦近
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivPlus3:
			OrederValue1 = speed+"";
			orderType = "13";//聚焦远
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivMinuse4:
			OrederValue1 = speed+"";
			orderType = "18";//关闭光圈
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivPlus4:
			OrederValue1 = speed+"";
			orderType = "17";//打开光圈
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivMinuse5:
			OrederValue1 = speed+"";
			orderType = "20";//关闭雨刷
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ivPlus5:
			OrederValue1 = speed+"";
			orderType = "19";//打开雨刷
			asyncQueryPostCommand(commandBaseUrl);
			break;
		case R.id.ll8://设置预位置
			foreDialog();
			break;

		default:
			break;
		}
	}

}
