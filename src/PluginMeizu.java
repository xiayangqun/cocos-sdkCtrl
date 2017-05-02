package com.catcap.IAP;

import java.util.UUID;

import com.meizu.gamesdk.model.callback.MzPayListener;
import com.meizu.gamesdk.model.model.MzPayParams;
import com.meizu.gamesdk.model.model.PayResultCode;
import com.meizu.gamesdk.offline.core.MzGameCenterPlatform;
import com.meizu.gamesdk.utils.MD5Utils;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class PluginMeizu extends SDKAbstract
{

	private static String APPID = "你的appID";
	private static String APPKEY="你的appKey";
	private static String APPSECRET="你的appSecret";
	private static String CPUSERINFO ="cp名字，比如《super cell》";


	private MzPayListener payListener;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		this.initPayListener();
		MzGameCenterPlatform.init(SDKCtrl.cocosActivity, PluginMeizu.APPID, PluginMeizu.APPKEY);
		MzGameCenterPlatform.orderQueryConfirm(SDKCtrl.cocosActivity, this.payListener);
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if(SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Toast.makeText(SDKCtrl.cocosActivity, "您的网络存在问题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.d("SDKCtrl", "PluginMeizu initiatePurchase：" + payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		String cpUserInfo = PluginMeizu.CPUSERINFO;
		String totalPrice = String.valueOf(this.getPriceValue(payIndex));
		String orderId = String.valueOf(System.currentTimeMillis());
		String productId = payCode;
		String productSubject = this.getPayDes(payIndex);
		String productBody = this.getPayDes(payIndex);
		int payType = 0;
		long createTime = System.currentTimeMillis();
		String appid = PluginMeizu.APPID;
		
		StringBuilder builder = new StringBuilder();
		final String equalStr = "=";
		final String andStr = "&";
		builder.append("app_id" + equalStr + appid + andStr);
		builder.append("cp_order_id" + equalStr + orderId + andStr);
		builder.append("create_time" + equalStr + createTime + andStr);
		builder.append("pay_type" + equalStr + payType + andStr);
		builder.append("product_body" + equalStr + productBody + andStr);
		builder.append("product_id" + equalStr + productId + andStr);
		builder.append("product_subject" + equalStr + productSubject + andStr);
		builder.append("total_price" + equalStr + totalPrice + andStr);
		builder.append("user_info" + equalStr + cpUserInfo);
		builder.append(":" + PluginMeizu.APPSECRET);
		String sign = MD5Utils.sign(builder.toString());
		
		String signType = "md5";
		Bundle payInfo = new Bundle();
		payInfo.putString(MzPayParams.ORDER_KEY_ORDER_APPID, appid);
		payInfo.putString(MzPayParams.ORDER_KEY_CP_INFO, cpUserInfo);
		payInfo.putString(MzPayParams.ORDER_KEY_AMOUNT, totalPrice);
		payInfo.putString(MzPayParams.ORDER_KEY_ORDER_ID, orderId);
		payInfo.putString(MzPayParams.ORDER_KEY_PRODUCT_BODY, productBody);
		payInfo.putString(MzPayParams.ORDER_KEY_PRODUCT_ID, productId);
		payInfo.putString(MzPayParams.ORDER_KEY_PRODUCT_SUBJECT, productSubject); 
		payInfo.putInt(MzPayParams.ORDER_KEY_PAY_TYPE, payType);
		payInfo.putString(MzPayParams.ORDER_KEY_SIGN, sign);
		payInfo.putString(MzPayParams.ORDER_KEY_SIGN_TYPE, signType);
		payInfo.putBoolean(MzPayParams.ORDER_KEY_DISABLE_PAY_TYPE_SMS, true);
		payInfo.putLong(MzPayParams.ORDER_KEY_CREATE_TIME,createTime);
		payInfo.putInt(MzPayParams.ORDER_KEY_PAY_CHANNEL, 1);
		MzGameCenterPlatform.singlePay(SDKCtrl.cocosActivity, payInfo, this.payListener);
	}
	
	private void initPayListener()
	{
		this.payListener = new MzPayListener()
		{
			@Override
			public void onPayResult(int code, Bundle info, String errorMsg)
			{
				if (SDKAbstract.payIndex == -99) return;
				
				if (code == PayResultCode.PAY_SUCCESS)
				{
					SDKAbstract.finishPurchase(true, "购买成功");
				}
				else if (code == PayResultCode.PAY_ERROR_CANCEL)
				{
					SDKAbstract.finishPurchase(false, "取消购买");
				}
				else if (code == PayResultCode.PAY_ERROR_CODE_DUPLICATE_PAY)
				{
					SDKAbstract.finishPurchase(false, "重复支付");
				}
				else if (code == PayResultCode.PAY_ERROR_GAME_VERIFY_ERROR)
				{
					SDKAbstract.finishPurchase(false, errorMsg);
				}
				else
				{
					SDKAbstract.finishPurchase(false, errorMsg);
				}
			}
		};
		
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
	
}
