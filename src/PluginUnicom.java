package com.catcap.IAP;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;


import android.util.Log;

public class PluginUnicom extends SDKAbstract {

	
	private UnipayPayResultListener  payListener =null;
	private UnipayPayResultListener initListener =null;
	@Override
	public void init(SDKConfig _sdkConfig) 
	{
		this.sdkConfig = _sdkConfig;
		this.iniUnipayPayResultListener();
		Utils.getInstances().initPayContext(SDKCtrl.cocosActivity	, this.initListener );
		Log.d("SDKCtrl", "init PluginUnicon finished");
	}

	@Override
	public void initiatePurchase(int payIndex) 
	{
		Log.d("SDKCtrl", "PluginUnicon initiatePurchase："+payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		Utils.getInstances().pay(SDKCtrl.cocosActivity , payCode, this.payListener);

	}

	@Override
	public boolean showMoreGame()
	{
		Log.e("SDKCtrl", "you want show More Game, but there i not include More Game in PluginUnicom");
		return false;
	}

	@Override
	public boolean showExitGame()
	{
		Log.e("SDKCtrl", "you want show Exit Game, but there i not include Exit Game in PluginUnicom");
		return false;
	}
	
	private void iniUnipayPayResultListener()
	{
			this.initListener = new UnipayPayResultListener() {
			
			@Override
			public void PayResult(String arg0, int arg1, int arg2, String arg3) {
			
				switch (arg1) {
				case 1://success
					Log.d("SDKCtrl","PluginUnicom 登录成功");
					break;

				case 2://fail
					Log.d("SDKCtrl","PluginUnicom 登录失败");
					break;
				
				case 3://cancel
					Log.d("SDKCtrl","PluginUnicom 登录取消");
					break;
				default:
					Log.d("SDKCtrl","PluginUnicom 登录未知");
					break;
				}
			}
		};
		
		
		this.payListener = new UnipayPayResultListener() {
		
			@Override
			public void PayResult(String arg0, int arg1, int arg2, String arg3) {
				
				boolean result = false;
				String message = null;
				switch (arg1) {
				case 1://success
					result = true;
					message = "支付成功了";
					break;

				case 2://fail
					result = false;
					message = "支付失败了";
					break;
				
				case 3://cancel
					result =false;
					message = "支付取消了";
					break;
				default:
					// 支付结果未知  = 失败
					result =false;
					message = "支付结果未知了";
					break;
				}
				SDKAbstract.finishPurchase(result,message);
			}
		};
	}

	public  boolean showInterstitialAd()
	{
		return false;
			
	}
	
	public  boolean showBannerAd()
	{
		return false;
	}
	
	public boolean hideBannerAd()
	{
		return false;
	}
	
}
