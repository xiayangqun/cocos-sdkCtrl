package com.catcap.IAP;

import java.util.UUID;

import com.xiaomi.gamecenter.sdk.*;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class PluginMi extends SDKAbstract
{
	
	//custom param begin 在这里写上你得到的appid和appKey
	private static String APPID ="您的appID ";
	private static String APPKEY="你的appKey";

	//custom param end


	static boolean isMiLogined = false;
	
	protected static final int MILOGIN_SUCCESS = 0;
	protected static final int MILOGIN_SUCCESS_GO_ON_BUY = 1;
	protected static final int MILOGIN_CANCEL = 2;
	protected static final int MILOGIN_ACTION_EXECUTED= 3;
	protected static final int MILOGIN_FAIL= 4;
	static private Handler miHandler = new Handler()
	{		
		@Override
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case MILOGIN_SUCCESS:
					Toast.makeText(SDKCtrl.cocosActivity, "小米账户登录成功", Toast.LENGTH_SHORT).show();
					break;
				case MILOGIN_SUCCESS_GO_ON_BUY:
					Toast.makeText(SDKCtrl.cocosActivity, "小米账户登录成功，点击购买按钮开始购买吧", Toast.LENGTH_SHORT).show();
					break;
				case MILOGIN_FAIL:
					Toast.makeText(SDKCtrl.cocosActivity, "小米账户登录失败，可能影响您的购买，请检查网络", Toast.LENGTH_SHORT).show();
					break;	
				case MILOGIN_CANCEL:
					Toast.makeText(SDKCtrl.cocosActivity, "用户取消了登录", Toast.LENGTH_SHORT).show();
					break;
				case MILOGIN_ACTION_EXECUTED:
					Toast.makeText(SDKCtrl.cocosActivity, "小米账户登录中", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		}
	};
	
	/*	
	 * 一定要注意这个 init 要在 Application 的 onCreate调用
	 * */
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		
		MiAppInfo appInfo = new MiAppInfo();
		appInfo.setAppId(PluginMi.APPID);
		appInfo.setAppKey(PluginMi.APPKEY);
		MiCommplatform.Init(SDKCtrl.mainApplication, appInfo);
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if (SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Toast.makeText(SDKCtrl.cocosActivity, "您的网络存在问题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(isMiLogined == false)
		{
			this.tryMiLogin();
			return;
		}
		
		Log.d("SDKCtrl", "PluginMobile initiatePurchase：" + payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		MiBuyInfo miBuyInfo = new MiBuyInfo();
		miBuyInfo.setCpOrderId(String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString());// 订单号唯一（不为空）
		miBuyInfo.setProductCode(payCode);
		miBuyInfo.setCount(1);
		
		MiCommplatform.getInstance().miUniPay(SDKCtrl.cocosActivity, miBuyInfo, new OnPayProcessListener()
		{
			@Override
			public void finishPayProcess(int code)
			{
				switch (code)
				{
					case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
						SDKAbstract.finishPurchase(true, "购买成功");
						break;
					case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
						SDKAbstract.finishPurchase(false, "取消购买");
						break;
					case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
						SDKAbstract.finishPurchase(false, "购买失败");
						break;
					case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
						SDKAbstract.finishPurchase(false, "操作正在执行中");
						break;
					default:
						SDKAbstract.finishPurchase(false, "购买失败");
						break;
				}
			}
		});
		
	}
	
	private void tryMiLogin()
	{
		MiCommplatform.getInstance().miLogin(SDKCtrl.cocosActivity, new OnLoginProcessListener()
		{
			@Override
			public void finishLoginProcess(int code, MiAccountInfo arg1)
			{
				switch (code)
				{
					case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
						isMiLogined = true;
						miHandler.sendEmptyMessage(MILOGIN_SUCCESS_GO_ON_BUY);
						break;
					case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
						miHandler.sendEmptyMessage(MILOGIN_CANCEL);
					      break;
					case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:	
						miHandler.sendEmptyMessage(MILOGIN_ACTION_EXECUTED);
					      break;	
					case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
					default:
						isMiLogined =false;
						miHandler.sendEmptyMessage(MILOGIN_FAIL);
						break;
				}
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
		MiCommplatform.getInstance().miAppExit( SDKCtrl.cocosActivity, new OnExitListner()
		{
			@Override
			public void onExit( int code )
			{
				if ( code == MiErrorCode.MI_XIAOMI_EXIT )
				{
					SDKCtrl.cocosActivity.finish();
					System.exit(0);
				}
			}
		} );
		
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
