package com.catcap.IAP;

import com.appchina.usersdk.Account;
import com.appchina.usersdk.ErrorMsg;
import com.appchina.usersdk.GlobalUtils;
import com.yyh.sdk.AccountCallback;
import com.yyh.sdk.CPInfo;
import com.yyh.sdk.LoginCallback;
import com.yyh.sdk.PayParams;
import com.yyh.sdk.PayResultCallback;
import com.yyh.sdk.YYHSDKAPI;

import android.app.Activity;
import android.os.Message;

public class PluginYYH extends SDKAbstract
{
	private static int LOGINID = 12916;
	private static String LOGINKEY = "你的LoginKey";
	
	private static String APPID = "appid";
	private static String PRIVATEKEY = "你的PRIVATEKEY";
	private static String PUBLICKEY = "你的PUBLICKEY";
	
	private CPInfo mCpInfo;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		
		mCpInfo = new CPInfo();
		
		mCpInfo.needAccount = true;
		mCpInfo.loginId = LOGINID;
		mCpInfo.loginKey = LOGINKEY;
		
		mCpInfo.appid = APPID;
		mCpInfo.privateKey = PRIVATEKEY;
		mCpInfo.publicKey = PUBLICKEY;
		mCpInfo.orientation = CPInfo.PORTRAIT;
		
		YYHSDKAPI.startSplash(SDKCtrl.cocosActivity, mCpInfo.orientation, 3000);
		 
		YYHSDKAPI.singleInit(SDKCtrl.cocosActivity, mCpInfo, new AccountCallback()
		{
			
			@Override
			public void onSwitchAccount(Account arg0, Account arg1)
			{
				
			}
			
			@Override
			public void onLogout()
			{
				
			}
		});
		
	}
	
	
	private void YYHLogin()
	{
		YYHSDKAPI.login(SDKCtrl.cocosActivity, new LoginCallback()
		{
			@Override
			public void onLoginSuccess(Activity activity, Account account)
			{
				YYHSDKAPI.showToolbar(true);
				GlobalUtils.showToast(activity, "登录成功");
			}
			
			@Override
			public void onLoginError(Activity activity, ErrorMsg error)
			{
				GlobalUtils.showToast(activity, error.message);
			}
			
			@Override
			public void onLoginCancel()
			{
				GlobalUtils.showToast(SDKCtrl.cocosActivity, "登录取消");
			}
		});
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
		
		
		if(YYHSDKAPI.isLogined() == false)
		{
			this.YYHLogin();
			return;
		}
		
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		PayParams params = new PayParams();
		params.buildWaresid(Integer.parseInt(payCode));
		params.buildWaresName(this.getPayDes(payIndex));
		params.buildCporderid(String.valueOf(System.currentTimeMillis()));
		params.buildPrice(this.getPriceValue(payIndex));
		
		YYHSDKAPI.startPay(SDKCtrl.cocosActivity, params, new PayResultCallback()
		{
			@Override
			public void onPaySuccess(int resultCode, String resultInfo)
			{
				SDKAbstract.finishPurchase(true, resultInfo);
			}
			
			@Override
			public void onPayFaild(int resultCode, String resultInfo)
			{
				SDKAbstract.finishPurchase(false, resultInfo);
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
