package com.catcap.IAP;

import android.util.Log;
import android.widget.Toast;
import cn.cmgame.billing.api.*;
import cn.cmgame.billing.api.GameInterface.PropsType;


public class PluginMobile extends SDKAbstract{
	
	private static String APPNAME = "您的应用名字";
	private static String PROVIDER = "您的公司名字";
	private static String SERVICETRL = "你的公司座机电话";
	private static String LOGINNO = "联网获取用户信息用的东西（只有网游才需要）";

	@Override
	public  void init(SDKConfig _sdkConfig)
	{
			this.sdkConfig=_sdkConfig;
			
			GameInterface.initializeApp(SDKCtrl.cocosActivity , PluginMobile.APPNAME , PluginMobile.PROVIDER , PluginMobile.SERVICETRL , PluginMobile.LOGINNO,new GameInterface.ILoginCallback() {
		        @Override
		        public void onResult(int result, String userId, Object o) {
		          if(result== LoginResult.SUCCESS_EXPLICIT || result==LoginResult.SUCCESS_IMPLICIT)
		          {
		        	  	//Toast.makeText(thiz, "登陆成功!", Toast.LENGTH_SHORT).show();
		        	  Log.d("PluginMobile", "登录成功");
		          }
		          else
		          {
		        	  	//Toast.makeText(thiz, "登陆失败!", Toast.LENGTH_SHORT).show();
		        	  Log.d("PluginMobile", "登陆失败");
		          }
		        }
		    }); 
			Log.d("SDKCtrl", "init PluginMobile finished");
	}
	
	@Override
	public  void initiatePurchase(int payIndex)
	{
		Log.d("SDKCtrl", "PluginMobile initiatePurchase："+payIndex);
		
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
			
		GameInterface.doBilling(SDKCtrl.cocosActivity, GameInterface.UiType.FULLSCREEN, PropsType.NORMAL, payCode, "1234567890123456", new GameInterface.IPayCallback() 
		{
			public void onResult(int resultCode, String billingIndex, Object arg) 
			{	
				boolean result=false;
				String message = "";
				switch (resultCode)
				{
					case BillingResult.SUCCESS:
					    if((BillingResult.EXTRA_SENDSMS_TIMEOUT+"").equals(arg.toString()))
					    	message = "短信计费超时";
					    else
					    	message = "购买道具：[" + billingIndex + "] 成功！";		
					
					    result=true;
						break;
					case BillingResult.FAILED:
						result=false;
						message = "购买道具：[" + billingIndex + "] 失败！";
						break;
			          default:
			      		result=false;
			        	message = "购买道具：[" + billingIndex + "] 取消！";
			            break;
				}
				 SDKAbstract.finishPurchase(result,message);
			}
		});
	}
	
	public  boolean showMoreGame()
	{
		 GameInterface.viewMoreGames( SDKCtrl.cocosActivity);
		 return true;
	}
	
	public  boolean showExitGame()
	{
		GameInterface.exit(SDKCtrl.cocosActivity, new GameInterface.GameExitCallback() {
		      @Override
		      public void onConfirmExit() {
		    	SDKCtrl.cocosActivity.finish();
		        System.exit(0);
		      }

		      @Override
		      public void onCancelExit() {
		        Toast.makeText(SDKCtrl.cocosActivity, "取消退出", Toast.LENGTH_SHORT).show();
		      }
		    });
		
		return true;
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
