package com.catcap.IAP;


import com.duoku.platform.single.DKPlatform;
import com.duoku.platform.single.DkErrorCode;
import com.duoku.platform.single.DkProtocolKeys;
import com.duoku.platform.single.DKPlatformSettings;
import com.duoku.platform.single.callback.IDKSDKCallBack;
import com.duoku.platform.single.item.DKOrderPayChannelData;
import com.duoku.platform.single.item.DKOrderStatus;
import com.duoku.platform.single.item.GamePropsInfo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class PluginBaidu extends SDKAbstract {


	@Override
	public void init(SDKConfig _sdkConfig) 
	{
		
		this.sdkConfig = _sdkConfig;
		IDKSDKCallBack initcompletelistener = new IDKSDKCallBack(){
			@Override
			public void onResponse(String paramString) {
				try {
					JSONObject jsonObject = new JSONObject(paramString);
					int mFunctionCode = jsonObject.getInt(DkProtocolKeys.FUNCTION_CODE);
					if(mFunctionCode == DkErrorCode.BDG_CROSSRECOMMEND_INIT_FINSIH)
					{
						//PluginBaidu.baiduLogin();
						PluginBaidu.showPingXuan();	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		//初始化函数
		DKPlatform.getInstance().init(  SDKCtrl.cocosActivity , false ,DKPlatformSettings.SdkMode.SDK_PAY,null,null,initcompletelistener);

	}

	@Override
	public void initiatePurchase(int payIndex)
	{
		Log.d("SDKCtrl", "PluginMobile initiatePurchase："+payIndex);	
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		
		String payIndexStr = String.valueOf(payIndex);
		String priceValue = this.sdkConfig.priceValueMap.get(payIndexStr);
		String payDes = this.sdkConfig.payDesMap.get(payIndexStr);
		String thirdpay = "qpfangshua";
		
		GamePropsInfo props = new GamePropsInfo(payCode, priceValue,payDes,thirdpay);
		DKPlatform.getInstance().invokePayCenterActivity(SDKCtrl.cocosActivity , props, null,null, null,null,new IDKSDKCallBack(){
			@Override
			public void onResponse(String paramString) {
				try {
						JSONObject jsonObject = new JSONObject(paramString);
						int mStatusCode = jsonObject.getInt(DkProtocolKeys.FUNCTION_STATUS_CODE);
						switch(mStatusCode)
						{
							case(DkErrorCode.BDG_RECHARGE_SUCCESS):
								SDKAbstract.finishPurchase(true, "支付成功了");
								break;
							case(DkErrorCode.BDG_RECHARGE_ACTIVITY_CLOSED):
							case(DkErrorCode.BDG_RECHARGE_CANCEL):
								SDKAbstract.finishPurchase(false, "支付取消了");
								break;
							case(DkErrorCode.BDG_RECHARGE_FAIL):
								SDKAbstract.finishPurchase(false, "支付失败了");
								break;
							case(DkErrorCode.BDG_RECHARGE_USRERDATA_ERROR):
							case(DkErrorCode.BDG_RECHARGE_EXCEPTION):
							default:
								SDKAbstract.finishPurchase(false, "支付异常了");
								break;
						}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean showMoreGame() {
	
		return false;
	}

	@Override
	public boolean showExitGame() {
		
		DKPlatform.getInstance().bdgameExit(SDKCtrl.cocosActivity, new IDKSDKCallBack() {
			@Override
			public void onResponse(String paramString)
			{
				SDKCtrl.cocosActivity.finish();
				System.exit(0);
			}
		});
		return true;
	}

	@Override
	public boolean showInterstitialAd() {
	
		return false;
	}

	@Override
	public boolean showBannerAd() {
	
		return false;
	}

	@Override
	public boolean hideBannerAd() {
	
		return false;
	}
	
	//登录游戏
	static private void baiduLogin()
	{
		IDKSDKCallBack loginlistener = new IDKSDKCallBack(){
			@Override
			public void onResponse(String paramString) {
				Log.d("SDKCtrl", "开始登陆");
			}
		};
		DKPlatform.getInstance().invokeBDInit(SDKCtrl.cocosActivity, loginlistener);
	}
	
	//显示品宣传模块
	static private void  showPingXuan()
	{
		DKPlatform.getInstance().bdgameInit(SDKCtrl.cocosActivity, new IDKSDKCallBack() {
			@Override
			public void onResponse(String paramString) {
				Log.d("SDKCtrl", "PluginBaidu pingxuan success!!");
			}
		});
	}
	
	@Override
	public void onPause()
	{
		DKPlatform.getInstance().pauseBaiduMobileStatistic(SDKCtrl.cocosActivity);
	}
	
	@Override
	public void onResume()
	{
		DKPlatform.getInstance().resumeBaiduMobileStatistic(SDKCtrl.cocosActivity); 
	}
}
