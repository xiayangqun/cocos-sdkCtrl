package com.catcap.IAP;

public interface SDKCtrlDelegate {

	//当购买结束是的时候发起的回调，  是否成功，输出信息，payIndex， payCode
	public void  onFinishPurchase(boolean result,  String message,int payIndex , String payCode);
	
	
}
