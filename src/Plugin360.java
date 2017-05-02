package com.catcap.IAP;

import org.json.JSONException;
import org.json.JSONObject;

import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.CPCallBackMgr.MatrixCallBack;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Plugin360 extends SDKAbstract
{

	//将这些参数修改成你的appName	
	private static String APPNAME = "你的应用名字，例如《王者荣誉》";



	protected MatrixCallBack mSDKCallback;
	private IDispatcherCallback mPayCallback;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		this.initMSDKCallback();
		this.initMPayCallback();
		
		Matrix.init(SDKCtrl.cocosActivity, mSDKCallback);
		Matrix.silentLogin(SDKCtrl.cocosActivity);
		
	}
	
	private void initMSDKCallback()
	{
		mSDKCallback = new MatrixCallBack()
		{
			@Override
			public void execute(Context context, int functionCode, String functionParams)
			{
				if (functionCode == ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT)
				{
					// 调用 sdk 的切换帐号接口，详细接口说明见 3.2.2.2 小节。
					// doSdkSwitchAccount(getLandscape(context));
				}
			}
			
		};
	}
	
	private void initMPayCallback()
	{
		this.mPayCallback = new IDispatcherCallback()
		{
			@Override
			public void onFinished(String data)
			{
				try
				{
					JSONObject jsonRes = new JSONObject(data);
					// error_code 状态码： 0 支付成功， -1 支付取消， 1 支付失败， -2 支付进行中
					int errorCode = jsonRes.optInt("error_code");
					if (errorCode == 0)
						SDKAbstract.finishPurchase(true, "支付成功");
					else
						SDKAbstract.finishPurchase(false, jsonRes.optString("error_msg", ""));
				}
				catch (JSONException e)
				{
					SDKAbstract.finishPurchase(false, "发生了未知的错误");
					e.printStackTrace();
				}
			}
		};
		
	}
	
	//这是360主动发起登录。在这里，我们只是使用了静默登录而没有调用这个接口
	private void sdkLogin()
	{
		Intent intent = new Intent(SDKCtrl.cocosActivity, ContainerActivity.class);
		
		intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGIN);
		intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, false);
		intent.putExtra(ProtocolKeys.IS_LOGIN_SHOW_CLOSE_ICON, true);
		intent.putExtra(ProtocolKeys.IS_SUPPORT_OFFLINE, true);
		intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, true);
		intent.putExtra(ProtocolKeys.IS_HIDE_WELLCOME, true);
		
		// 可选参数，静默自动登录失败后是否显示登录窗口，默认不显示
		intent.putExtra(ProtocolKeys.IS_SHOW_LOGINDLG_ONFAILED_AUTOLOGIN, true);
		
		Matrix.execute(SDKCtrl.cocosActivity, intent, new IDispatcherCallback()
		{
			@Override
			public void onFinished(String data)
			{
				try
				{
					JSONObject joRes = new JSONObject(data);
					JSONObject joData = joRes.getJSONObject("data");
					String mode = joData.optString("mode", "");
					if (mode.equals("offline"))
					{
						Toast.makeText(SDKCtrl.cocosActivity, "离线登录成功", Toast.LENGTH_LONG).show();
					}
					else
					{
						int errno = joRes.optInt("errno");
						if (errno == 0)
						{
							Toast.makeText(SDKCtrl.cocosActivity, "登录成功了", Toast.LENGTH_LONG).show();
						}
						else
						{
							Toast.makeText(SDKCtrl.cocosActivity, "登录失败，发生了未知的错误", Toast.LENGTH_LONG).show();
						}
						
					}
					
				}
				catch (Exception e)
				{
					Toast.makeText(SDKCtrl.cocosActivity, "登录失败，发生了未知的错误", Toast.LENGTH_LONG).show();
				}
				
			}
		});
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if(SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Toast.makeText(SDKCtrl.cocosActivity, "您的网络存在问题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.d("SDKCtrl", "Plugin360 initiatePurchase：" + payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		Bundle bundle = new Bundle();
		bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, false);
		bundle.putString(ProtocolKeys.AMOUNT, String.valueOf(this.getPriceValue(payIndex) * 100));
		bundle.putString(ProtocolKeys.PRODUCT_NAME, this.getPayDes(payIndex));
		bundle.putString(ProtocolKeys.PRODUCT_ID, this.getPayCode(payIndex));
		bundle.putString(ProtocolKeys.NOTIFY_URI, null);
		bundle.putString(ProtocolKeys.APP_NAME, Plugin360.APPNAME);
		bundle.putString(ProtocolKeys.APP_USER_NAME, null);
		bundle.putString(ProtocolKeys.APP_USER_ID, null);
		bundle.putString(ProtocolKeys.APP_ORDER_ID, String.valueOf(System.currentTimeMillis()));
		bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_PAY);
		Intent intent = new Intent(SDKCtrl.cocosActivity, ContainerActivity.class);
		intent.putExtras(bundle);
		
		Matrix.invokeActivity(SDKCtrl.cocosActivity, intent, mPayCallback);
		
	}
	
	@Override
	public boolean showMoreGame()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean showExitGame()
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, false);
		bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);
		Intent intent = new Intent(SDKCtrl.cocosActivity, ContainerActivity.class);
		intent.putExtras(bundle);
		Matrix.invokeActivity(SDKCtrl.cocosActivity, intent, new IDispatcherCallback()
		{
			
			@Override
			public void onFinished(String data)
			{
				try
				{
					JSONObject joRes = new JSONObject(data);
					int which = joRes.optInt("which");
					if (which == 2)
					{
						SDKCtrl.cocosActivity.finish();
						System.exit(0);
					}
				}
				catch (Exception e)
				{
					
				}
			}
			
		});
		return true;
	}
	
	@Override
	public boolean showInterstitialAd()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean showBannerAd()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean hideBannerAd()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public void onDestroy()
	{
		Matrix.destroy(SDKCtrl.cocosActivity);
	}
	
}
