package com.catcap.IAP;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import cn.uc.gamesdk.sa.UCGameSdk;
import cn.uc.gamesdk.sa.iface.open.ActivityLifeCycle;
import cn.uc.gamesdk.sa.iface.open.SDKConst;
import cn.uc.gamesdk.sa.iface.open.UCCallbackListener;
import cn.uc.gamesdk.sa.iface.open.UCGameSDKStatusCode;
import cn.uc.paysdk.SDKCore;
import cn.uc.paysdk.face.commons.PayResponse;
import cn.uc.paysdk.face.commons.Response;
import cn.uc.paysdk.face.commons.SDKCallbackListener;
import cn.uc.paysdk.face.commons.SDKError;
import cn.uc.paysdk.face.commons.SDKProtocolKeys;
import org.json.JSONObject;

public class PluginUC extends SDKAbstract
{

	private static String APPNAME="您的应用名字。例如《王者荣耀》";

	
	private SDKCallbackListener ucInitListener;
	private SDKCallbackListener ucPayListener;
	private UCCallbackListener<String> ucSDKinitListener;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		this.sdkConfig = _sdkConfig;
		this.initSDKListener();
		
		UCGameSdk.defaultSdk().setCallback(SDKConst.PAY_INIT_LISTENER, this.ucInitListener);
		UCGameSdk.defaultSdk().setCallback(SDKConst.SDK_INIT_LISTENER, this.ucSDKinitListener);
		
		Bundle payInitData = new Bundle();
		UCGameSdk.defaultSdk().init(SDKCtrl.cocosActivity, payInitData);
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		// TODO Auto-generated method stub
		if (SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Toast.makeText(SDKCtrl.cocosActivity, "您的网络连接存在问题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.d("SDKCtrl", "PluginUC initiatePurchase：" + payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		Intent data = new Intent();
		data.putExtra(SDKProtocolKeys.APP_NAME, PluginUC.APPNAME);
		data.putExtra(SDKProtocolKeys.AMOUNT, String.valueOf(this.getPriceValue(payIndex)));
		data.putExtra(SDKProtocolKeys.PRODUCT_NAME, this.getPayDes(payIndex));
		
		try
		{
			SDKCore.pay(SDKCtrl.cocosActivity, data, this.ucPayListener);
		}
		catch (Exception e)
		{
			SDKAbstract.finishPurchase(false, "发起支付时发生了问题");
			e.printStackTrace();
		}
	}
	
	private void initSDKListener()
	{
		this.ucInitListener = new SDKCallbackListener()
		{
			@Override
			public void onErrorResponse(SDKError arg0)
			{
				Log.d("SDKCtrl", "PluginUC  支付初始化失败:" + arg0);
			}
			
			@Override
			public void onSuccessful(int arg0, Response arg1)
			{
				Log.d("SDKCtrl", "PluginUC 支付初始化成功，可以调用支付接口了");
			}
		};
		
		this.ucPayListener = new SDKCallbackListener()
		{
			@Override
			public void onSuccessful(int status, Response response)
			{
				if (response.getType() == Response.LISTENER_TYPE_PAY)
				{

					response.setMessage(Response.OPERATE_SUCCESS_MSG); 
					try
					{
						String dataStr = response.getData();
						if (dataStr != null)
						{
							JSONObject data = new JSONObject(response.getData());
							String orderStatus = data.getString(PayResponse.ORDER_STATUS); // 订单状态//
														
							if (orderStatus.equals("00"))
								SDKAbstract.finishPurchase(true, "购买成功");
							else if (orderStatus.equals("01"))
								SDKAbstract.finishPurchase(false, "购买失败");
							else if (orderStatus.equals("99"))
								SDKAbstract.finishPurchase(false, "发生了退款");
							else
								SDKAbstract.finishPurchase(false, "未知的原因");
						}
						else
						{
							SDKAbstract.finishPurchase(false, "服务器返回的结果为空");
						}
					}
					catch (Exception ex)
					{
						SDKAbstract.finishPurchase(false, "订单发生了异常");
						ex.printStackTrace();
					}
				}
			}
			
			@Override
			public void onErrorResponse(SDKError error)
			{
				// 支付失败，该回调是在子线程中调用，UI操作需转到UI线程执行
				final String msg = error.getMessage();
				SDKAbstract.finishPurchase(false, msg);
			}
		};
		
		this.ucSDKinitListener = new UCCallbackListener<String>()
		{
			@Override
			public void callback(int statuscode, String msg)
			{
				switch (statuscode)
				{
					case UCGameSDKStatusCode.SUCCESS:
						Log.d("SDKCtrl", "PluginUC UCGameSDK  init SUCCESS");
						break;
					default:
						Log.d("SDKCtrl", "PluginUC UCGameSDK  init failed ");
						break;
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
		UCGameSdk.defaultSdk().exit(SDKCtrl.cocosActivity, new UCCallbackListener<String>()
		{
			@Override
			public void callback(int statuscode, String data)
			{
				if (UCGameSDKStatusCode.SDK_EXIT == statuscode)
				{
					SDKCtrl.cocosActivity.finish();
					System.exit(0);
				}
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
	
	public void onCreate()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_CREATE);
	};
	
	public void onStart()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_START);
	};
	
	public void onRestart()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_RESTART);
	};
	
	public void onResume()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_RESUME);
	};
	
	public void onPause()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_PAUSE);
	};
	
	public void onStop()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_STOP);
	};
	
	public void onDestory()
	{
		UCGameSdk.defaultSdk().lifeCycle(SDKCtrl.cocosActivity, ActivityLifeCycle.LIFE_ON_DESTROY);
	};
	
}
