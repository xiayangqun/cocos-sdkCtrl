package cn.actcap.ayc3;

import java.util.HashMap;

import org.cocos2dx.lib.Cocos2dxActivity;


import com.catcap.IAP.SDKConfig;
import com.catcap.IAP.SDKCtrl;
import com.catcap.IAP.SDKCtrlDelegate;
import com.squareup.utils.Log;

import android.os.Bundle;

public class TestActivity extends Cocos2dxActivity implements SDKCtrlDelegate{

	   protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);	
			SDKCtrl.init(this, this, "Baidu");
			SDKCtrl.initSDK("Baidu",this.getSDKConfig("Baidu"));
		}

	//
	@Override
	public void onFinishPurchase(boolean result, String message, int payIndex, String payCode)
	{
			if(result)
				Log.d("SDKCtrl", "支付成功了，支付成功的序号是"+payIndex);
			else 
				Log.d("SDKCtrl","支付失败了，失败的原因是"+message);
	}
	
	
	  public static SDKConfig getSDKConfig(String sdkName)
	    {
	    	HashMap<String, String> specialConfigMap = new HashMap<String, String>();
	    	HashMap<String , String> priceValueMap = new HashMap<String, String>();
	    	HashMap<String,String> 	priceDesMap =  new HashMap<String, String>();
	    	HashMap<String , String> payDesMap =  new HashMap<String, String>();
	    	HashMap<String , String> payCodeMap = new HashMap<String , String>();
	    
	    	 if(sdkName.equals("Baidu"))
	    	{
	    		 //key 必须是数字，对应着 onFinishPurchase 函数中对应的payIndex
	    		priceValueMap.put("1" , "2");		priceDesMap.put("1", "￥2元");		payDesMap.put ("1","商品1");							payCodeMap.put("1", "15735");					
	        	priceValueMap.put("2" , "8");		priceDesMap.put("2", "￥8元");		payDesMap.put ("2","商品2");							payCodeMap.put("2", "15736");	
	        	priceValueMap.put("3","18");		priceDesMap.put("3", "￥18元");		payDesMap.put ("3","商品3");						payCodeMap.put("3", "15737");	
	        	priceValueMap.put("4" , "30");		priceDesMap.put("4", "￥30元");		payDesMap.put ("4","商品4");						payCodeMap.put("4", "15738");	
	    	}
	    	else
	    	{
	    		Log.e("SDKCtrl",sdkName+" SDKConfig cant find");
	    	}
	    	SDKConfig sdkConfig = new SDKConfig(specialConfigMap, priceValueMap, priceDesMap, payDesMap, payCodeMap);
	    	return sdkConfig;
	    }
	
	 	@Override
	  protected void onDestroy() {
	 		super.onDestroy();
	 		SDKCtrl.onDestroy("Baidu");
	 		android.os.Process.killProcess(android.os.Process.myPid());
	 	}
	  
	 	@Override
	 	protected void onRestart() {
	
	 		super.onRestart();
	 		//SDKCtrl.onRestart("Baidu);
	 	}
	  
	 	void  pay(int payIndex)
	 	{
	 		//payIndx 就是 getConfig函数里的key ，这里的1对应的就是 “商品1 " 计费ID是"15735"
	 		SDKCtrl.initiatePurchase(1, "Baidu");
	 	}
	 	
	 	
	  
}
