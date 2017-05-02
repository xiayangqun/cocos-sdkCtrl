package com.catcap.IAP;
import java.util.Random;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.callback.SinglePayCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.util.AppUtil;
import com.nearme.platform.opensdk.pay.PayResponse;

import android.util.Log;
import android.widget.Toast;

public class PluginOppo extends SDKAbstract {

	private static String APPSECRET="你的appSecret";



	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		GameCenterSDK.init(PluginOppo.APPSECRET, SDKCtrl.mainApplication);
	}

	@Override
	public void initiatePurchase(int payIndex)
	{
		Log.d("SDKCtrl", "PluginOppo initiatePurchase："+payIndex);	
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		int amount = this.getPriceValue(payIndex) * 100; //从元转换成分
		PayInfo payInfo = new PayInfo(System.currentTimeMillis() + new Random().nextInt(1000) + "", "自定义字段", amount);
		payInfo.setProductDesc(this.getPayDes(payIndex));
		payInfo.setProductName(this.getPayDes(payIndex));
		payInfo.setUseCachedChannel(false);
		
		GameCenterSDK.getInstance().doSinglePay(SDKCtrl.cocosActivity, payInfo, new SinglePayCallback() {
			@Override
			public void onSuccess(String resultMsg) 
			{
				SDKAbstract.finishPurchase(true, "支付成功");
			} 
			
			@Override
			public void onFailure(String resultMsg, int resultCode)
			{
				if( resultCode != PayResponse.CODE_CANCEL )
					SDKAbstract.finishPurchase(false, "支付失败");
				else
					SDKAbstract.finishPurchase(false, "支付取消");	
			}

			@Override
			public void onCallCarrierPay(PayInfo arg0, boolean arg1) 
			{
				//doNothing
			}	
		});
		
	}

	@Override
	public boolean showMoreGame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showExitGame() {
	
		 GameCenterSDK.getInstance().onExit(SDKCtrl.cocosActivity, new GameExitCallback() {
			 @Override
			 public  void exitGame()
			 {
				 SDKCtrl.cocosActivity.finish();
				 System.exit(0);
			 }
			 
		 });
		return true;
	}

	@Override
	public boolean showInterstitialAd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showBannerAd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hideBannerAd() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onPause()
	{
		GameCenterSDK.getInstance().onPause();
	}
	
	@Override
	public void onResume()
	{
		GameCenterSDK.getInstance().onResume(SDKCtrl.cocosActivity);
	}
}
