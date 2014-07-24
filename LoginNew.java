package com.pinganfu.cusintro.activities;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pinganfu.cusintro.BuildConfig;
import com.pinganfu.cusintro.R;
import com.pinganfu.cusintro.adapter.CashierPopuAdapter;
import com.pinganfu.cusintro.bean.CustInfo;
import com.pinganfu.cusintro.interfaces.OnSimpleFinishListener;
import com.pinganfu.cusintro.interfaces.SimpleTextWatcher;
import com.pinganfu.cusintro.parser.JsonTags;
import com.pinganfu.cusintro.provider.Loading;
import com.pinganfu.cusintro.provider.LoginHelper;
import com.pinganfu.cusintro.provider.MyRequest;
import com.pinganfu.cusintro.provider.network.UrlsProvider;
import com.pinganfu.cusintro.provider.requestbody.AgentBody;
import com.pinganfu.cusintro.utils.MethodPhoneNum;
import com.pinganfu.cusintro.utils.ResultCode;
import com.pinganfu.cusintro.utils.ToastUtil;
import com.pinganfu.cusintro.utils.Utils;
import com.pinganfu.cusintro.views.ClearEditText;

public class LoginNew extends BaseActivity implements OnClickListener {
	private static final String TAG = LoginNew.class.getSimpleName();

	public static final String PHONE_NUM = "phone";

	private Context mContext;
	private ClearEditText pocketEditAdmin;
	private EditText pocketEditPass, pocketEditCashier;
	private ImageView popuBtn, backBtn;
	private PopupWindow popupWindow;
	private TextView tittleText;
	private RelativeLayout bottomLayout;
	private LinearLayout cashierLayout;
	private String status;
	private boolean isManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_new);
		mContext = this;
		initViews();
	}

	private void initViews() {
		initViewPagerView2();
		setPwd();
	}

	private void setPwd() {
		pocketEditAdmin.setText(LoginHelper.getLastUserName(this, isManager));
		pocketEditAdmin.setClearIconVisible(false);
	}

	private void initViewPagerView2() {
		tittleText = (TextView) findViewById(R.id.tv_title);
		cashierLayout = (LinearLayout) findViewById(R.id.login_cashier_layout);
		bottomLayout = (RelativeLayout) findViewById(R.id.login_bottom_layout);
		backBtn = (ImageView) findViewById(R.id.iv_back);
		pocketEditAdmin = (ClearEditText) findViewById(R.id.login_admin_pocket);
		pocketEditCashier = (EditText) findViewById(R.id.login_cashier_pocket);
		Button registerPocket = (Button) findViewById(R.id.login_regist_pocket);
		final Button pocketLoginBtn = (Button) findViewById(R.id.login_login_pocket);
		pocketEditPass = (EditText) findViewById(R.id.login_pass_pocket);
		Button passLostPocket = (Button) findViewById(R.id.login_lost_pocket);
		popuBtn = (ImageView) findViewById(R.id.login_admin_popuBtn);

		status = this.getIntent().getStringExtra("status");
		if ("1".equals(status)) {
			tittleText.setText(getResources().getString(
					R.string.lgoin_cashier_login));
			cashierLayout.setVisibility(RelativeLayout.VISIBLE);
			bottomLayout.setVisibility(RelativeLayout.GONE);
			isManager = false;
		} else {
			tittleText.setText(getResources().getString(
					R.string.login_manager_login));
			isManager = true;
		}
		pocketLengthChange(pocketLoginBtn);
		registerPocket.setOnClickListener(this);
		passLostPocket.setOnClickListener(this);
		pocketLoginBtn.setOnClickListener(this);
		popuBtn.setOnClickListener(this);
		backBtn.setOnClickListener(this);
		// pocketEditAdmin.setOnKeyListener(phoneOnKey);
		pocketEditAdmin.addTextChangedListener(phoneWatcher);
	}


	private void popupAcountsHistory(View anchor,
			final List<String> acoutList) {
		if (acoutList==null) {
			cashierLayout.setBackgroundResource(R.drawable.edittext_background);
		}else{
			cashierLayout.setBackgroundResource(R.drawable.edittext_background_no_corner);
		}
		ListView listView = new ListView(this);
		CashierPopuAdapter adapter=new CashierPopuAdapter(this, acoutList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, String> map = (HashMap<String, String>) parent
						.getItemAtPosition(position);
				popupWindow.dismiss();
				cashierLayout.setBackgroundResource(R.drawable.edittext_background);
				pocketEditCashier.setText((map.get(PHONE_NUM)));

				Editable e = pocketEditCashier.getText();
				pocketEditCashier.setSelection(e.length());
			}
		});
		getPopuWindow(anchor, listView);
	}

	private void getPopuWindow(View anchor, ListView listView) {
		popupWindow = new PopupWindow(listView, anchor.getWidth(),
				ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new ColorDrawable());
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.showAsDropDown(anchor);
	}

	private OnDismissListener popupWindowListener = new OnDismissListener() {

		@Override
		public void onDismiss() {
			cashierLayout.setBackgroundResource(R.drawable.edittext_background);
			popuBtn.setImageResource(R.drawable.dropdown);
		}
	};

	private boolean checkLogin(String loginId, String pwd) {
		if (MethodPhoneNum.checkPhoneNumOnly(loginId)) {
			return true;
		} else {
			ToastUtil.showToast(mContext,
					R.string.login_acount_phone_not_correct);
		}
		if (TextUtils.isEmpty(pwd)) {
			ToastUtil.showToast(mContext, R.string.login_acount_pwd_not_null);
			return false;
		}
		return false;
	}

	private void requestLogin(final String cashierNum,
			final String savedLoginId, final String loginId, String pwd) {
		String appId = getString(R.string.appid_for_login);

		String url = UrlsProvider.getUserLogin(this);
		Log.i(TAG, "url=" + url);
		final MyRequest loginRequest = new MyRequest(mContext);
		loginRequest.buildRequest(TAG, url,
				AgentBody.getUserLoginNew(this, appId, loginId, pwd),
				new OnSimpleFinishListener(this) {
					@Override
					public void onSuccess(Object object) {
						mProgressDialog.dismiss();

						JsonObject respJsonObject = (JsonObject) object;
						JsonObject custInfoObject = respJsonObject
								.getAsJsonObject(JsonTags.USER_INFO);

						Gson gson = new Gson();
						CustInfo custInfo = gson.fromJson(custInfoObject,
								CustInfo.class);
						custInfo.setSavedMobile(savedLoginId);
						if (BuildConfig.DEBUG)
							Log.d(TAG, "UserInfo=" + custInfo);

						loginSuccess(custInfo);
					}

					@Override
					public void onHandleError(String respCode, Object object) {
						int toastMsg = R.string.login_fail;
						try {
							// if (ResultCode.C_USER_NOT_EXIST.equals(respCode))
							// {
							// toastMsg = R.string.login_acount_not_exist;
							// } else
							if (ResultCode.C_USER_ERROR.equals(respCode)) {
								toastMsg = R.string.login_user_error;
							}
							// else if (ResultCode.C_USER_PWD_EXCEED
							// .equals(respCode)) {
							// toastMsg = R.string.login_acount_not_exist;
							// }
						} catch (Exception e) {
							e.printStackTrace();
						}
						loginFail(getString(toastMsg));
					}

					@Override
					public void onFail(VolleyError object) {
						 //loginFail(getString(R.string.login_fail));

						// /**临时调试________________________*/
						mProgressDialog.dismiss();
						CustInfo custInfo = new CustInfo();
						custInfo.setSavedMobile(savedLoginId);
						loginSuccess(custInfo);
					}
				});
		loginRequest.excute();

		mProgressDialog = Loading.showLoading(this, null);
	}

	private void loginFail(String toastMsg) {
		mProgressDialog.dismiss();
		ToastUtil.showToast(mContext, toastMsg);
	}

	private void loginSuccess(CustInfo custInfo) {
		// 保存登录信息
		// isManager = ("1".equals(status));
		LoginHelper.saveUserInfo(this, isManager, custInfo);
		// 跳转
		Intent intent = new Intent(mContext, HomeActivity.class);
		startActivity(intent);
		finish();

	}

	boolean isAdmin, isPass;

	private void pocketLengthChange(final Button button) {
		pocketEditPass.addTextChangedListener(new SimpleTextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				String strChange = s.toString();
				if (strChange.length() > 0) {
					isPass = true;
				} else {
					isPass = false;
				}

				if (isPass && isAdmin) {
					button.setEnabled(true);
				} else
					button.setEnabled(false);
			}

		});
		pocketEditAdmin.addTextChangedListener(new SimpleTextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				String strChange = s.toString().replace(" ", "");

				if (strChange.length() == 11) {
					isAdmin = true;
				} else {
					isAdmin = false;
				}

				if (isPass && isAdmin) {
					button.setEnabled(true);
				} else
					button.setEnabled(false);
			}

		});

	}

	@Override
	public void onClick(View arg0) {
		Intent intent;
		switch (arg0.getId()) {
		case R.id.login_regist_pocket:
			intent = new Intent(mContext, Register.class);
			intent.putExtra("check", 1);
			startActivity(intent);
			break;
		case R.id.login_lost_pocket:
			intent = new Intent(mContext, PasswordLostPocket.class);
			startActivity(intent);
			break;
		case R.id.iv_back:
			finish();
			break;
		case R.id.login_admin_popuBtn:
			List<String> acoutList = LoginHelper.getHistoryAcounts(this);
			if (acoutList.size()==0) {
				popuBtn.setImageResource(R.drawable.dropdown);
			}else{
				popuBtn.setImageResource(R.drawable.dropup);
			}
			getListNotNull(acoutList);
			break;
		case R.id.login_login_pocket:
			Utils.hideSoftInput(pocketEditAdmin);
			Utils.hideSoftInput(pocketEditPass);
			String loginIdPocket = pocketEditAdmin.getText().toString().trim();
			String pwdPocket = pocketEditPass.getText().toString();
			String loginId = loginIdPocket.replaceAll(" ", "");
			getPhoneLength(loginIdPocket, pwdPocket, loginId);
			break;
		default:
			break;
		}
	}

	private void getPhoneLength(String loginIdPocket, String pwdPocket,
			String loginId) {
		if (loginId.length() == 11) {
			if (checkLogin(loginId, pwdPocket)) {
				requestLogin(null, loginIdPocket, loginId, pwdPocket);
			}
		} else {
			ToastUtil.showToast(mContext,
					R.string.login_acount_phone_not_correct);
		}
	}

	private void getListNotNull(List<String> acoutList) {
		if (acoutList != null && acoutList.size() != 0) {
			popupAcountsHistory(cashierLayout, acoutList);
			popupWindow.setOnDismissListener(popupWindowListener);
		}
	}

	private SimpleTextWatcher phoneWatcher = new SimpleTextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (before == 0 && count == 1) {
				MethodPhoneNum
						.addPhoneNum(s.toString(), start, pocketEditAdmin);
			} else if (before == 1 && count == 0) {
				MethodPhoneNum
						.delPhoneNum(s.toString(), start, pocketEditAdmin);
			}
			Log.i("", s + "," + start + "," + before + "," + count);
		};

	};
}
