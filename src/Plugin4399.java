package com.catcap.IAP;

import android.content.pm.ActivityInfo;
import android.os.Message;
import android.util.Log;
import cn.m4399.operate.OperateCenterConfig;
import cn.m4399.operate.SingleOperateCenter;
import cn.m4399.operate.SingleOperateCenter.SingleRechargeListener;
import cn.m4399.operate.UpgradeInfo;
import cn.m4399.operate.model.callback.Callbacks.OnCheckFinishedListener;
import cn.m4399.operate.model.callback.Callbacks.OnDownloadListener;
import cn.m4399.recharge.RechargeOrder;

public class Plugin4399 extends SDKAbstract
{
	private static String GAMEKEY = "你的GameKey";
	private static String GAMENAME = "你的游戏名字";
	
	private SingleOperateCenter mOpeCenter;
	private OperateCenterConfig mOpeConfig;
	private SingleRechargeListener payListener;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		
		mOpeCenter = SingleOperateCenter.getInstance();
		new OperateCenterConfig.Builder(SDKCtrl.cocosActivity).setDebugEnabled(false)
				.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).setSupportExcess(false).setGameKey(GAMEKEY)
				.setGameName(GAMENAME).build();
		
		payListener = new SingleRechargeListener()
		{
			private String msg = "";
			
			@Override
			public void onRechargeFinished(boolean success, String msgstr)
			{
				if(success)
				{
					Message msg = SDKHandler.Instance.obtainMessage(SDKHandler.HANDLE_TOAST, "支付完成请耐心等待订单查询结果");
					SDKHandler.Instance.sendMessage(msg);
				}
				else 
				{
					this.msg = msgstr;
				}
				
				Log.d("SDKCtrl", "onRechargeFinished");
				
			}
			
			@Override
			synchronized public boolean notifyDeliverGoods(boolean shouldDeliver, RechargeOrder o)
			{
				if(shouldDeliver)
					SDKAbstract.finishPurchase(shouldDeliver, "成功获得"+ SDKCtrl.getPayDes(SDKAbstract.payIndex) );
				else 
					SDKAbstract.finishPurchase(shouldDeliver, this.msg);
				
				Log.d("SDKCtrl", "notifyDeliverGoods");
				
				return shouldDeliver;
			}
		};
		mOpeCenter.init(SDKCtrl.cocosActivity, this.payListener);
		Log.d("SDKCtrl", "init Plugin4399 finished");
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if(SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Message msg=SDKHandler.Instance.obtainMessage(SDKHandler.HANDLE_TOAST, "当前没有网络连接");
			SDKHandler.Instance.sendMessage(msg);
			return;
		}
		
		
		Log.d("SDKCtrl", "Plugin4399 initiatePurchase："+payIndex);
		
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		//这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		String priceValue =  String.valueOf( this.getPriceValue(payIndex));
		String payDes = this.getPayDes(payIndex);
		mOpeCenter.recharge(SDKCtrl.cocosActivity, priceValue, payDes);
	
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
	
	@Override
	public
	void onDestory()
	{
		mOpeCenter.destroy();
	}
	
}
