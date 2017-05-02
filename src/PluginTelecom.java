package com.catcap.IAP;
import java.util.HashMap;
import java.util.Map;

import com.catcap.Base;

import android.util.Log;
import android.widget.Toast;
import cn.egame.terminal.paysdk.EgameExitListener;
import cn.egame.terminal.paysdk.EgamePay;
import cn.egame.terminal.paysdk.EgamePayListener;


public class PluginTelecom extends SDKAbstract {

	
	private EgamePayListener payListener ;
	
	
	@Override
	public void init(SDKConfig _sdkConfig) {
		// TODO Auto-generated method stub
		this.sdkConfig =_sdkConfig;
		this.initPayListener();
		EgamePay.init(SDKCtrl.cocosActivity);
		Log.d("SDKCtrl", "init PulginTelecom finished");
	}

	@Override
	public void initiatePurchase(int payIndex) {
		
		Log.d("SDKCtrl", "PluginTelecom initiatePurchase："+payIndex);
		
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
	    HashMap<String,String>  payParams=new HashMap<String,String>();
		payParams.put(EgamePay.PAY_PARAMS_KEY_TOOLS_ALIAS, payCode);
		EgamePay.pay(SDKCtrl.cocosActivity, payParams, this.payListener);

	}

	@Override
	public boolean showMoreGame() {
		EgamePay.moreGame(SDKCtrl.cocosActivity);
		return true;
	}

	@Override
	public boolean showExitGame() {
		EgamePay.exit(SDKCtrl.cocosActivity, new EgameExitListener() {
			@Override
			public void cancel() {
				 Toast.makeText(SDKCtrl.cocosActivity, "取消退出", Toast.LENGTH_SHORT).show();
			}
			@Override
			public void exit() {
				SDKCtrl.cocosActivity.finish();
				 System.exit(0);
			}
		});

		return true;
	}
	
	private void initPayListener()
	{
		this.payListener = new EgamePayListener() {
			
			public void payFailed(Map<String, String> params, int errorInt) {
				boolean result =false;
				String message = "道具支付失败：错误代码：" + errorInt;
				SDKAbstract.finishPurchase(result, message);
			}

			@Override
			public void payCancel(Map<String, String> arg0) {
				boolean result =false;
				String message = "支付取消";
				SDKAbstract.finishPurchase(result, message);
			}

			@Override
			public void paySuccess(Map<String, String> arg0) {
				// 支付成功的处理
				boolean result =true;
				String message = "支付成功";
				SDKAbstract.finishPurchase(result, message);
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
