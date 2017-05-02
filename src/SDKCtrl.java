package com.catcap.IAP;

import java.util.HashMap;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SDKCtrl
{
	
	// 主Activity
	static public Cocos2dxActivity cocosActivity;
	
	// 代理
	static public SDKCtrlDelegate delegate;
	
	//
	static public HashMap<String, SDKAbstract> sdksMap = new HashMap<String, SDKAbstract>();
	
	/*
	 * 运营商类型 "Mobile"：移动 "Unicom"： "联通" "Telecom"：电信 "Unknow" : 未知
	 */
	static public String operatorType = "Mobile";
	
	/*
	 * 支付的优先级 1：短信 2：渠道 3：18元以上走渠道，18元以下走短信
	 */
	static public int paymentPriority = 1;
	
	/*
	 * 渠道名字 'Baidu' 'UC'
	 */
	static public String channelName = "";
	
	static public void init(Cocos2dxActivity activity, SDKCtrlDelegate _delegate, String _channelName)
	{
		SDKCtrl.cocosActivity = activity;
		SDKCtrl.delegate = _delegate;
		SDKCtrl.channelName = _channelName;
		initOperatorType();
	}
	
	// 初始化运营商类型
	static private void initOperatorType()
	{
		TelephonyManager telManager = (TelephonyManager) cocosActivity
				.getSystemService(cocosActivity.TELEPHONY_SERVICE);
		String operator = telManager.getSimOperator();
		if (operator != null)
		{
			if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")
					|| operator.equals("898600"))
				operatorType = "Mobile";
			else if (operator.equals("46001"))
				operatorType = "Unicom";
			else if (operator.equals("46003"))
				operatorType = "Telecom";
			else
				operatorType = "Unknow";
		}
	}
	
	// 根据SDK的名字来初始化
	static public void initSDK(String sdkName, SDKConfig sdkConfig)
	{
		Class<?> C = null;
		try
		{
			C = Class.forName("com.catcap.IAP.Plugin" + sdkName);
		}
		catch (ClassNotFoundException var8)
		{
			Log.e("SDKCtrl",
					"com.catcap.IAP.Plugin" + sdkName + " not found!!! maybe you forget to include this file!!!");
			var8.printStackTrace();
			return;
		}
		
		Object sdkObj = null;
		try
		{
			sdkObj = C.newInstance();
		}
		catch (Exception e)
		{
			Log.e("SDKCtrl", "com.catcap.IAP.Plugin" + sdkName + " createfailed");
			e.printStackTrace();
		}
		
		SDKAbstract sdk = (SDKAbstract) sdkObj;
		sdk.init(sdkConfig);
		sdksMap.put(sdkName, sdk);
	}
	
	// 发起支付请求
	public static void initiatePurchase(int payIndex, String sdkName)
	{
		
		Log.d("SDKCtrl", "initiatePurchase :" + payIndex);
		
		if (sdkName != null && sdkName.equals("") == false)
		{
			sdksMap.get(sdkName).initiatePurchase(payIndex);
		}
		else
		{
			
			if (paymentPriority == 1)
			{
				// 优先短信
				if (operatorType.equals("Unknow") == false &&  sdksMap.get(operatorType)!=null )
					sdksMap.get(operatorType).initiatePurchase(payIndex);
				else if (sdksMap.get(channelName) != null)
					sdksMap.get(channelName).initiatePurchase(payIndex);
				else
					sdksMap.get("Mobile").initiatePurchase(payIndex);
			}
			else if (paymentPriority == 2)
			{
				// 优先渠道
				if (sdksMap.get(channelName) != null)
					sdksMap.get(channelName).initiatePurchase(payIndex);
				else if (operatorType.equals("Unknow") == false &&  sdksMap.get(operatorType)!=null)
					sdksMap.get(operatorType).initiatePurchase(payIndex);
				else
					sdksMap.get("Mobile").initiatePurchase(payIndex);
				
			}
			else if (paymentPriority == 3)
			{
				SDKAbstract channelSDK = sdksMap.get(channelName);
				if (channelSDK != null)
				{
					String priceStr = channelSDK.sdkConfig.priceValueMap.get(String.valueOf(payIndex));
					int priceInt = Integer.parseInt(priceStr);
					if (priceInt >= 18)
					{
						// 大于18元 走渠道支付
						channelSDK.initiatePurchase(payIndex);
					}
					else
					{
						// 小于18元 走短信支付
						if (operatorType.equals("Unknow") == false &&  sdksMap.get(operatorType)!=null)
							sdksMap.get(operatorType).initiatePurchase(payIndex);
						else
							sdksMap.get("Mobile").initiatePurchase(payIndex);
					}
				}
				else
				{
					// 走短信支付
					if (operatorType.equals("Unknow") == false &&  sdksMap.get(operatorType)!=null)
						sdksMap.get(operatorType).initiatePurchase(payIndex);
					else
						sdksMap.get("Mobile").initiatePurchase(payIndex);
				}
			}
		}
	}
	
	public static String getPayCode(int payIndex)
	{
		if (channelName.equals("") == false)
		{
			SDKAbstract sdk = sdksMap.get(channelName);
			if (sdk != null) { return sdk.getPayCode(payIndex); }
		}
		
		if (operatorType.equals("Unknow") == false)
		{
			SDKAbstract sdk = sdksMap.get(operatorType);
			if (sdk != null) { return sdk.getPayCode(payIndex); }
		}
		
		return sdksMap.get("Mobile").getPayCode(payIndex);
		
	}
	
	// 获取商品名称 例如 “魔兽大礼包”
	public static String getPayDes(int payIndex)
	{
		if (channelName.equals("") == false)
		{
			SDKAbstract sdk = sdksMap.get(channelName);
			if (sdk != null) { return sdk.getPayDes(payIndex); }
		}
		
		if (operatorType.equals("Unknow") == false)
		{
			SDKAbstract sdk = sdksMap.get(operatorType);
			if (sdk != null) { return sdk.getPayDes(payIndex); }
		}
		
		return sdksMap.get("Mobile").getPayDes(payIndex);
	}
	
	// 获取商品的价格 例如 30
	public static int getPriceValue(int payIndex)
	{
		if (channelName.equals("") == false)
		{
			SDKAbstract sdk = sdksMap.get(channelName);
			if (sdk != null) { return sdk.getPriceValue(payIndex); }
		}
		
		if (operatorType.equals("Unknow") == false)
		{
			SDKAbstract sdk = sdksMap.get(operatorType);
			if (sdk != null) { return sdk.getPriceValue(payIndex); }
		}
		return sdksMap.get("Mobile").getPriceValue(payIndex);
	}
	
	// 获取商品的价格描述例如 “￥30元”
	public static String getPriceDes(int payIndex)
	{
		if (channelName.equals("") == false)
		{
			SDKAbstract sdk = sdksMap.get(channelName);
			if (sdk != null) { return sdk.getPriceDes(payIndex); }
		}
		
		if (operatorType.equals("Unknow") == false)
		{
			SDKAbstract sdk = sdksMap.get(operatorType);
			if (sdk != null) { return sdk.getPriceDes(payIndex); }
		}
		return sdksMap.get("Mobile").getPriceDes(payIndex);
	}
	
	// 显示MoreGame
	public static boolean showMoreGame(String sdkName)
	{
		if (sdkName == null || sdkName.equals(""))
		{
			// 按照优先级来
			if (SDKCtrl.channelName.equals("") != false)
			{
				SDKAbstract sdk = sdksMap.get(channelName);
				if (sdk != null) { return sdk.showMoreGame(); }
			}
			
			if (SDKCtrl.operatorType.equals("UnKnow") == false)
			{
				SDKAbstract sdk = sdksMap.get(operatorType);
				if (sdk != null) { return sdk.showMoreGame(); }
			}
			
			return sdksMap.get("Mobile").showMoreGame();
		}
		else
		{
			return sdksMap.get(sdkName).showMoreGame();
		}
	}
	
	// 显示ExitGame
	public static boolean showExitGame(String sdkName)
	{
		if (sdkName == null || sdkName.equals(""))
		{
			// 按照优先级来
			if (SDKCtrl.channelName.equals("") != false)
			{
				SDKAbstract sdk = sdksMap.get(channelName);
				if (sdk != null) { return sdk.showExitGame(); }
			}
			
			if (SDKCtrl.operatorType.equals("UnKnow") == false)
			{
				SDKAbstract sdk = sdksMap.get(operatorType);
				if (sdk != null) { return sdk.showExitGame(); }
			}
			
			return sdksMap.get("Mobile").showExitGame();
		}
		else
		{
			return sdksMap.get(sdkName).showExitGame();
		}
	}
	
	static public void showInterstitialAd(String sdkName)
	{
		if (sdkName == null || sdkName.equals(""))
		{
			if (SDKCtrl.channelName.equals("") != false)
			{
				SDKAbstract sdk = sdksMap.get(channelName);
				if (sdk != null && sdk.showInterstitialAd()) { return; }
			}
			
			if (SDKCtrl.operatorType.equals("UnKnow") == false)
			{
				SDKAbstract sdk = sdksMap.get(operatorType);
				if (sdk != null && sdk.showInterstitialAd()) { return; }
			}
			
			sdksMap.get("Mobile").showInterstitialAd();
			
		}
		else
		{
			sdksMap.get(sdkName).showInterstitialAd();
		}
	}
	
	static public void showBannerAd(String sdkName)
	{
		if (sdkName == null || sdkName.equals(""))
		{
			if (SDKCtrl.channelName.equals("") != false)
			{
				SDKAbstract sdk = sdksMap.get(channelName);
				if (sdk != null && sdk.showBannerAd()) { return; }
			}
			
			if (SDKCtrl.operatorType.equals("UnKnow") == false)
			{
				SDKAbstract sdk = sdksMap.get(operatorType);
				if (sdk != null && sdk.showBannerAd()) { return; }
			}
			
			sdksMap.get("Mobile").showBannerAd();
			
		}
		else
		{
			sdksMap.get(sdkName).showBannerAd();
		}
	}
	
	static public void hideBannerAd(String sdkName)
	{
		if (sdkName == null || sdkName.equals(""))
		{
			if (SDKCtrl.channelName.equals("") != false)
			{
				SDKAbstract sdk = sdksMap.get(channelName);
				if (sdk != null && sdk.hideBannerAd()) { return; }
			}
			
			if (SDKCtrl.operatorType.equals("UnKnow") == false)
			{
				SDKAbstract sdk = sdksMap.get(operatorType);
				if (sdk != null && sdk.hideBannerAd()) { return; }
			}
			
			sdksMap.get("Mobile").hideBannerAd();
			
		}
		else
		{
			sdksMap.get(sdkName).hideBannerAd();
		}
		
	}
	
	static public void onCreate(String sdkName)
	{
		
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onCreate();
		
	};
	
	static public void onStart(String sdkName)
	{
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onStart();
	};
	
	static public void onRestart(String sdkName)
	{
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onRestart();
	};
	
	static public void onResume(String sdkName)
	{
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onResume();
	};
	
	static public void onPause(String sdkName)
	{
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onPause();
	};
	
	static public void onStop(String sdkName)
	{
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onStop();
	};
	
	static public void onDestory(String sdkName)
	{
		if (sdksMap.get(sdkName) != null) sdksMap.get(sdkName).onDestory();
	};
	
	public static boolean isNetworkAvailable(Context context)
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (null == networkInfo || !(networkInfo.isAvailable() && networkInfo.isConnectedOrConnecting())) return false;
		
		return true;
	}
}
