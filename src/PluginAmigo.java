package com.catcap.IAP;

import com.gionee.game.offlinesdk.AppInfo;
import com.gionee.game.offlinesdk.GamePlatform;
import com.gionee.game.offlinesdk.InitPluginCallback;
import com.gionee.game.offlinesdk.OrderInfo;
import com.gionee.game.offlinesdk.QuitGameCallback;
import com.gionee.game.offlinesdk.PayCallback;

import android.os.Message;

//金立游戏大厅
public class PluginAmigo extends SDKAbstract
{
	
	private static String APIKEY = "你的appID";
	private static String SECRETKEY = "你的aecrectkey";
	private static String PUBLICKEY = "你的PublicKey";
	private static String PRICATEKEY = "你的privateKey";
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		AppInfo appInfo = new AppInfo();
		appInfo.setApiKey(APIKEY); // apiKey由开发者后台申请得到
		appInfo.setPrivateKey(PRICATEKEY); // privateKey由开发者后台申请得到
		appInfo.setPayMode(AppInfo.PayMode.NO_ACCOUNT_BY_TRADE_DATA); // 设置支付模式为“指定支付方式支付“
		GamePlatform.init(SDKCtrl.mainApplication, appInfo);
		
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if (SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Message msg = SDKHandler.Instance.obtainMessage(SDKHandler.HANDLE_TOAST, "当前没有网络连接");
			SDKHandler.Instance.sendMessage(msg);
			return;
		}
		
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCpOrderNum(String.valueOf(System.currentTimeMillis()));
		orderInfo.setSubject(this.getPayDes(payIndex));
		orderInfo.setTotalFee(String.valueOf(this.getPriceValue(payIndex)));
		orderInfo.setDealPrice(String.valueOf(this.getPriceValue(payIndex)));
		orderInfo.setPayMethod(GamePlatform.PAY_METHOD_UNSPECIFIED);

		GamePlatform.getInstance().pay(SDKCtrl.cocosActivity, orderInfo, new PayCallback()
		{
			@Override
			public void onSuccess()
			{
				SDKAbstract.finishPurchase(true, "支付成功");
			}
			
			@Override
			public void onFail(String errCode, String errDescription)
			{
				SDKAbstract.finishPurchase(false, errCode + ":" + errDescription);
			}
		});
		
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
		GamePlatform.getInstance().quitGame(SDKCtrl.cocosActivity, new QuitGameCallback()
		{
			@Override
			public void onQuit()
			{
				SDKCtrl.showExitGame("Mobile");
			}
			
			@Override
			public void onCancel()
			{
				
			}
		});
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
	
}
