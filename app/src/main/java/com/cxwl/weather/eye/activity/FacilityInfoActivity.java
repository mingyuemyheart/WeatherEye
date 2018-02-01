package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.FacilityInfoAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.RefreshLayout;
import com.cxwl.weather.eye.view.RefreshLayout.OnLoadListener;
import com.cxwl.weather.eye.view.RefreshLayout.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 设备设置
 * @author shawn_sun
 *
 */

public class FacilityInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView listView = null;
	private FacilityInfoAdapter mAdapter = null;
	private List<EyeDto> facilityList = new ArrayList<>();
	private int page = 1;
	private int pageCount = 20;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private LinearLayout llSelect = null;
	private LinearLayout llBottom = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facilityinfo);
		mContext = this;
		showDialog();
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.BOTH);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
		refreshLayout.setOnLoadListener(new OnLoadListener() {
			@Override
			public void onLoad() {
				page++;
				OkHttpList("https://tqwy.tianqi.cn/tianqixy/userInfo/selfacility");
			}
		});
	}
	
	private void refresh() {
		page = 1;
		facilityList.clear();
		OkHttpList("https://tqwy.tianqi.cn/tianqixy/userInfo/selfacility");
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("设备信息");
		llSelect = (LinearLayout) findViewById(R.id.llSelect);
		llBottom = (LinearLayout) findViewById(R.id.llBottom);
		
		refresh();
	}
	
	private void initListView() {
		listView = (ListView) findViewById(R.id.listView);
		mAdapter = new FacilityInfoAdapter(mContext, facilityList);
		listView.setAdapter(mAdapter);
//		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				if (!TextUtils.equals(CONST.AUTHORITY, CONST.COMMON)) {
//					llSelect.setVisibility(View.VISIBLE);
//					llBottom.setVisibility(View.VISIBLE);
//				}
//				return false;
//			}
//		});
	}
	
	/**
	 * 获取视频列表
	 */
	private void OkHttpList(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("intpage", page+"");
		builder.add("pagerow", pageCount+"");
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				final String result = response.body().string();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject object = new JSONObject(result);
								if (object != null) {
									if (!object.isNull("code")) {
										String code  = object.getString("code");
										if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
											if (!object.isNull("list")) {
												JSONArray array = new JSONArray(object.getString("list"));
												for (int i = 0; i < array.length(); i++) {
													JSONObject itemObj = array.getJSONObject(i);
													EyeDto dto = new EyeDto();
													if (!itemObj.isNull("Fzid")) {
														dto.fGroupId = itemObj.getString("Fzid");
													}
													if (!itemObj.isNull("Fid")) {
														dto.fId = itemObj.getString("Fid");
													}
													if (!itemObj.isNull("FacilityIP")) {
														dto.fGroupIp = itemObj.getString("FacilityIP");
													}
													if (!itemObj.isNull("Fzname")) {
														dto.fGroupName = itemObj.getString("Fzname");
													}
													if (!itemObj.isNull("Location")) {
														dto.location = itemObj.getString("Location");
													}
													if (!itemObj.isNull("FacilityNumber")) {
														dto.fNumber = itemObj.getString("FacilityNumber");
													}
													if (!itemObj.isNull("FacilityUrlWithin")) {
														dto.streamPrivate = itemObj.getString("FacilityUrlWithin");
													}
													if (!itemObj.isNull("FacilityUrl")) {
														dto.streamPublic = itemObj.getString("FacilityUrl");
													}
													facilityList.add(dto);
												}
												if (mAdapter != null) {
													mAdapter.notifyDataSetChanged();
												}
											}
										}else {
											//失败
											if (!object.isNull("reason")) {
												String reason = object.getString("reason");
												if (!TextUtils.isEmpty(reason)) {
													Toast.makeText(mContext, reason, Toast.LENGTH_SHORT).show();
												}
											}
										}
									}
								}
								refreshLayout.setRefreshing(false);
								refreshLayout.setLoading(false);
								cancelDialog();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}