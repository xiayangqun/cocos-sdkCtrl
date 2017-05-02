package com.catcap.IAP;

import java.util.HashMap;
import java.util.Map;

import com.android.huawei.pay.plugin.IHuaweiPay;
import com.android.huawei.pay.plugin.IPayHandler;
import com.android.huawei.pay.plugin.MobileSecurePayHelper;
import com.android.huawei.pay.plugin.PayParameters;
import com.android.huawei.pay.util.HuaweiPayUtil;
import com.android.huawei.pay.util.Rsa;
import com.huawei.opensdk.OpenSDK;
import com.huawei.opensdk.RetCode;
import com.huawei.gamebox.buoy.sdk.impl.BuoyOpenSDK;
import com.huawei.gamebox.buoy.sdk.inter.UserInfo;
import com.huawei.gamebox.buoy.sdk.InitParams;
import com.huawei.gamebox.buoy.sdk.UpdateInfo;
import com.huawei.gamebox.buoy.sdk.IGameCallBack;


import android.util.Log;
import android.widget.Toast;

public class PluginHUAWEI extends SDKAbstract
{
	
	// 将以下几个静态变量设置成你从华为后台获取到的序列号
	private static String APPID = "您的appid";
	private static String PAYID = "你的payID";
	private static String CPID="你的cpid";
	private static String PAYPRIKEY ="你的 payPrikey 。贼长的一串字符串";
	private static String BUOYSECRET = "你的buoySecret，贼长的一个字串串";
	private static String USERNAME = "你公司的名字，例如《super cell》";
	// 是否显示华为SDK打印og，调试的时候设置成true，结束的是设置成false
	private static boolean SHOWHUAWEILOG = true;
	// 显示支付界面方向，1是竖屏，2是横屏
	private static int SCREENTORIENT = 1;
	
	
	private IPayHandler payListener;
	private static InitParams buoyParams;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		this.initPayListener();
		this.initBuoyParams();
		this.startLogin();
		//BuoyOpenSDK.getIntance().showSmallWindow(SDKCtrl.cocosActivity);
		//OpenSDK.asyncLogin(SDKCtrl.cocosActivity, PluginHUAWEI.APPID, PluginHUAWEI.PAYID, PluginHUAWEI.PAYPRIKEY);
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if (SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Toast.makeText(SDKCtrl.cocosActivity, "您的网络存在问题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.d("SDKCtrl", "PluginHUAWEI initiatePurchase：" + payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		String price = String.valueOf(this.getPriceValue(payIndex)) + ".00";
		String productName = this.getPayDes(payIndex);
		String productDesc = this.getPayDes(payIndex);
		String requestId = String.valueOf(System.currentTimeMillis());
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("userID", PluginHUAWEI.PAYID);
		params.put("applicationID", PluginHUAWEI.APPID);
		params.put("amount", price);
		params.put("productName", productName);
		params.put("productDesc", productDesc);
		params.put("requestId", requestId);
		
		String noSign = HuaweiPayUtil.getSignData(params);
		String sign = Rsa.sign(noSign, PluginHUAWEI.PAYPRIKEY);
		
		HashMap<String, Object> payInfo = new HashMap<String, Object>();
		payInfo.put("amount", price);
		payInfo.put("productName", productName);
		payInfo.put("requestId", requestId);
		payInfo.put("productDesc", productDesc);
		payInfo.put("userName", PluginHUAWEI.USERNAME);
		payInfo.put("applicationID", PluginHUAWEI.APPID);
		payInfo.put("userID", PluginHUAWEI.PAYID);
		payInfo.put("sign", sign);
		payInfo.put("serviceCatalog", "X6");
		// 发布
		payInfo.put("showLog", PluginHUAWEI.SHOWHUAWEILOG);
		payInfo.put("screentOrient", PluginHUAWEI.SCREENTORIENT);
		
		IHuaweiPay payHelper = new MobileSecurePayHelper();
		payHelper.startPay(SDKCtrl.cocosActivity, payInfo, this.payListener);
		
	}
	
	private void initPayListener()
	{
		this.payListener = new IPayHandler()
		{
			@Override
			public void onFinish(Map<String, String> payResp)
			{
				if (payResp.get(PayParameters.returnCode).equals("0")
						&& payResp.get(PayParameters.errMsg).equals("success"))
				{
					SDKAbstract.finishPurchase(true, "支付成功");
				}
				else if ("30002".equals(payResp.get(PayParameters.returnCode)))
				{
					SDKAbstract.finishPurchase(false, "支付结果超时了");
				}
				else if ("30008".equals(payResp.get(PayParameters.returnCode)))
				{
					SDKAbstract.finishPurchase(false, "用户需要重新登录");
					startLogin();
				}
				else
				{
					SDKAbstract.finishPurchase(false, "支付失败");
				}
			}
		};
	}
	
	private void initBuoyParams()
	{
		PluginHUAWEI.buoyParams = new InitParams(PluginHUAWEI.APPID, PluginHUAWEI.CPID, PluginHUAWEI.BUOYSECRET, new IGameCallBack(){

			@Override
			public void onDestoryed()
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onDestoryed");
			}

			@Override
			public void onHidenFailed(int arg0)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onHidenSuccessed()
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onInitFailed(int arg0)
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onInitFailed");
				
			}

			@Override
			public void onInitFinished(UpdateInfo arg0)
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onInitFinished");
				
			}

			@Override
			public void onInitStarted()
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onInitStarted");
			}

			@Override
			public void onInitSuccessed()
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onInitSuccessed");
				BuoyOpenSDK.getIntance().showSmallWindow(SDKCtrl.cocosActivity);
			}

			@Override
			public void onShowFailed(int arg0)
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onShowFailed");
			}

			@Override
			public void onShowSuccssed()
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onShowSuccssed");
				
			}

			@Override
			public void onUpdateCheckFinished(UpdateInfo arg0)
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onUpdateCheckFinished");
			}

			@Override
			public void onUpdateError(int arg0)
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onUpdateError");
			}

			@Override
			public void onValidFail()
			{
				Log.d("SDKCtrl", "PluginHUAWEI BuoyOpenSDK  onValidFail");
			}
		});
	}
	
	
	private void startLogin()
	{
		
		int retCode = OpenSDK.init(SDKCtrl.cocosActivity, PluginHUAWEI.APPID, PluginHUAWEI.PAYID,PluginHUAWEI.BUOYSECRET, new UserInfo()
		{
					@Override
					public void dealUserInfo(HashMap<String, String> userInfo)
					{
						if (null == userInfo)
						{
							Toast.makeText(SDKCtrl.cocosActivity, "用户信息为空登录失败了", Toast.LENGTH_SHORT).show();
							//OpenSDK.asyncLogin(SDKCtrl.cocosActivity, PluginHUAWEI.APPID, PluginHUAWEI.PAYID, PluginHUAWEI.PAYPRIKEY);
						}
						else if ("1".equals((String) userInfo.get("loginStatus")))
						{
							Toast.makeText(SDKCtrl.cocosActivity, "登录成功！", Toast.LENGTH_SHORT).show();
						}
						
						BuoyOpenSDK.getIntance().showSmallWindow(SDKCtrl.cocosActivity);
					}			
		});
		if (RetCode.SUCCESS == retCode) OpenSDK.start();
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
		// TODO Auto-generated method stub
		return false;
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
	
	public void onPause()
	{
		BuoyOpenSDK.getIntance().hideSmallWindow(SDKCtrl.cocosActivity); 
		BuoyOpenSDK.getIntance().hideBigWindow(SDKCtrl.cocosActivity);
	}
	public void onResume()
	{
		BuoyOpenSDK.getIntance().showSmallWindow(SDKCtrl.cocosActivity);
	};
	
}
