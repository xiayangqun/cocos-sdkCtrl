package com.catcap.IAP;

import com.anzhi.sdk.middle.single.manage.AnzhiSingleSDK;
import com.anzhi.sdk.middle.single.manage.CPInfo;
import com.anzhi.sdk.middle.single.manage.SingleGameCallBack;
import com.anzhi.sdk.middle.single.util.MD5;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.anzhi.sdk.middle.single.util.Base64;

import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import com.catcap.IAP.PluginAnzhiUtil.*;

public class PluginAnzhi extends SDKAbstract implements SingleGameCallBack
{
	private static String APPKEY = "你的appkey";
	private static String APPSECRET = "你的appsercet";
	
	private AnzhiSingleSDK midManage;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		midManage = AnzhiSingleSDK.getInstance();
		midManage.init(SDKCtrl.cocosActivity, APPKEY, APPSECRET, this);
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if(SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Message msg = SDKHandler.Instance.obtainMessage(SDKHandler.HANDLE_TOAST, "当前没有网络连接");
			SDKHandler.Instance.sendMessage(msg);
			return;
		}
		
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		JSONObject json = new JSONObject();
		try
		{
			// 游戏方生成的订单号,可以作为与安智订单进行关联
			json.put("cpOrderId", "catcap_" + System.currentTimeMillis());
			json.put("cpOrderTime", System.currentTimeMillis());
			json.put("amount", this.getPriceValue(payIndex) * 100);
			json.put("cpCustomInfo", "catcapNanchao");
			json.put("productCount", 1);
			json.put("productName", this.getPayDes(payIndex));
			json.put("productCode", this.getPayCode(payIndex));
			
			String data = json.toString();
			String encryptData = Des3Util.encrypt(data, APPSECRET);
			String md5 = MD5.encodeToString(APPSECRET);
			AnzhiSingleSDK.getInstance().pay(encryptData, md5);
			
		}
		catch (JSONException e)
		{
			SDKAbstract.finishPurchase(false, "生成Json字符串时出错");
		}
		
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
		this.midManage.exitGame(SDKCtrl.cocosActivity);
		return true;
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
	
	@Override
	public void callBack(int type, String data)
	{
		switch (type)
		{
			case SingleGameCallBack.SDK_TYPE_LOGIN:
				this.loginFinishCallback(data);
				break;
			case SingleGameCallBack.SDK_TYPE_LOGOUT:
				break;
			case SingleGameCallBack.SDK_TYPE_PAY:
				this.payFinishCallback(data);
				break;
			case SingleGameCallBack.SDK_TYPE_INIT:
				this.initFinishCallback(data);
				break;
			case SingleGameCallBack.SDK_TYPE_EXIT_GAME:
				SDKCtrl.showExitGame("Mobile");
				break;
			case SingleGameCallBack.SDK_TYPE_CANCEL_EXIT_GAME:
				break;
			case SingleGameCallBack.SDK_TYPE_CANCEL_PAY:
				SDKAbstract.finishPurchase(false, "用户取消支付");
				break;
			case SingleGameCallBack.SDK_TYPE_CANCEL_LOGIN:
				break;
		}
	}
	
	private void payFinishCallback(String data)
	{
		try
		{
			JSONObject json = new JSONObject(data);
			int payStatus = json.optInt("payStatus");
			switch (payStatus)
			{
				case 1:
					SDKAbstract.finishPurchase(true, "支付成功");
					break;
				case 2:
					// 支付进行中
					break;
				case 3:
					SDKAbstract.finishPurchase(false, "支付失败");
					break;
			}
		}
		catch (JSONException e)
		{
			SDKAbstract.finishPurchase(false, "支付失败，返回的son格式不正确");
		}
	};
	
	private void initFinishCallback(String data)
	{
		midManage.login(SDKCtrl.cocosActivity);
		midManage.addPop(SDKCtrl.cocosActivity);
	}
	
	private void loginFinishCallback(String data)
	{
		try
		{
			JSONObject json = new JSONObject(data);
			if (json.optInt("code") == 200)
				Log.d("SDKCtrl", "PluginAnzhi login successed");
			else
				Log.d("SDKCtrl", "PluginAnzhi login failed");
		}
		catch (JSONException e)
		{
			Log.d("SDKCtrl", "PluginAnzhi login exception");
		}
	}
	
	@Override
	public void onResume()
	{
		this.midManage.onResumeInvoked();
	}
	
	@Override
	public void onPause()
	{
		this.midManage.onPauseInvoked();
	}
	
	@Override
	public void onStop()
	{
		this.midManage.onStopInvoked();
		
	}
	
	@Override
	public void onDestory()
	{
		this.midManage.onDestoryInvoked();
	}
	
}
