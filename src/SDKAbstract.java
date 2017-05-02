package com.catcap.IAP;

public abstract class SDKAbstract {

	public SDKConfig sdkConfig;
	

	static protected int payIndex = -99;
	static protected String payCode ="null";
	
	
	public abstract void init(SDKConfig _sdkConfig);
	public abstract void initiatePurchase(int payIndex);
	
	//如果支持MoreGame，ExitGame并且成功的显示了相关的界面，那么返回True
	public abstract boolean showMoreGame();
	public abstract boolean showExitGame();
	
	//如果支持显示广告，并且成功的显示了广告，那么返回true
	public abstract boolean showInterstitialAd();
	public abstract boolean showBannerAd();
	public abstract boolean hideBannerAd();
	
	public void onCreate(){};
	public void onStart(){};
	public void onRestart(){};
	public void onResume(){};
	public void onPause(){};
	public void onStop(){};
	public void onDestory(){};
	
	
	public String getPayCode(int payIndex)
	{
		 String payIndexStr = String.valueOf(payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(payIndexStr);
		return payCode;
	}
	
	
	public  String  getPayDes(int payIndex)
	{
		 String payIndexStr = String.valueOf(payIndex);
		String payDes = this.sdkConfig.payDesMap.get(payIndexStr);
		return payDes;
	}
	
	public  int getPriceValue(int payIndex)
	{
		String payIndexStr = String.valueOf(payIndex);
		String priceValueStr = this.sdkConfig.priceValueMap.get(payIndexStr);
		return  Integer.parseInt(priceValueStr);
	}
	
	public String getPriceDes(int payIndex)
	{
		String payIndexStr = String.valueOf(payIndex);
		String priceDes = this.sdkConfig.priceDesMap.get(payIndexStr);
		
		return priceDes;
	}
	
	static protected void finishPurchase(boolean result,  String message )
	{
			SDKCtrl.delegate.onFinishPurchase(result, message, SDKAbstract.payIndex, SDKAbstract.payCode);
	}
}
