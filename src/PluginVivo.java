package com.catcap.IAP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.bbk.payment.network.NetworkRequestAgent;
import com.bbk.payment.payment.OnVivoSinglePayResultListener;
import com.bbk.payment.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.vivo.sdkplugin.aidl.VivoUnionManager;
import org.json.JSONObject;

public class PluginVivo extends SDKAbstract implements DialogInterface.OnClickListener
{
	// 在这里修改你从Vivo后台得到的参数
	static private String STORDID = "你的stordID";
	static private String APPID = "你的appid";
	static private String APPKEY = "你的appkey";
	
	private VivoUnionManager mVivoUnionManager;
	private OnVivoSinglePayResultListener payResultListener;
	private boolean isSupportWeiXin;
	AlertDialog.Builder whichDialog;
	static private PluginVivo Instance = null;
	
	@Override
	public void init(SDKConfig _sdkConfig)
	{
		Instance = this;
		this.sdkConfig = _sdkConfig;
		
		payResultListener = new OnVivoSinglePayResultListener()
		{
			@Override
			public void payResult(String transNo, boolean result, String resultMsg, String payMsg)
			{
				String showMsg =  payMsg;
				if (result)
					SDKAbstract.finishPurchase(true, showMsg);
				else
					SDKAbstract.finishPurchase(false, showMsg);
			};
		};
		
		mVivoUnionManager = new VivoUnionManager(SDKCtrl.cocosActivity);
		mVivoUnionManager.initVivoSinglePayment(SDKCtrl.cocosActivity, payResultListener);
		mVivoUnionManager.singlePaymentInit(SDKCtrl.cocosActivity);
	}
	
	@Override
	public void initiatePurchase(int payIndex)
	{
		if (SDKCtrl.isNetworkAvailable(SDKCtrl.cocosActivity) == false)
		{
			Toast.makeText(SDKCtrl.cocosActivity, "您的网络有问题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		whichDialog = new AlertDialog.Builder(SDKCtrl.cocosActivity);
		whichDialog.setTitle("发起支付");
		whichDialog.setMessage("请选择购买方式");
		whichDialog.setPositiveButton("微信直付", this);
		whichDialog.setNegativeButton("Vivo支付", this);
		whichDialog.show();
		
		Log.d("SDKCtrl", "PluginVivo initiatePurchase：" + payIndex);
		String payCode = this.sdkConfig.payCodeMap.get(String.valueOf(payIndex));
		// 这两句话非常重要，一定不要忘记写
		SDKAbstract.payIndex = payIndex;
		SDKAbstract.payCode = payCode;
		
		// NameValuePair[] nameValuePairs = this.doPaymentInit(payIndex);
		// InitialPayTask initialPayTask = new InitialPayTask();
		// initialPayTask.execute(nameValuePairs);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		this.isSupportWeiXin = (which == -1);
		if (this.isSupportWeiXin)
			Log.d("SDKCtrl", "选择使用微信直付");
		else
			Log.d("SDKCtrl", "选择使用Vivo付款");
		
		NameValuePair[] nameValuePairs = this.doPaymentInit(SDKAbstract.payIndex);
		InitialPayTask initialPayTask = new InitialPayTask();
		initialPayTask.execute(nameValuePairs);
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
	public void onDestroy()
	{
		mVivoUnionManager.singlePaymentExit(SDKCtrl.cocosActivity);
		mVivoUnionManager.cancelVivoSinglePayment(payResultListener);
	}
	
	private NameValuePair[] doPaymentInit(int payIndex)
	{
		
		String storeOrder = ("catcap" + UUID.randomUUID().toString()).replaceAll("-", "");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String orderTime = format.format(new Date());
		String notifyUrl = "http://s.catcap.cn/PPPayCallback.php";
		
		Map<String, String> param_map = new HashMap<String, String>();
		param_map.put("notifyUrl", notifyUrl);
		param_map.put("orderAmount", String.valueOf(this.getPriceValue(payIndex)) + ".00");
		param_map.put("orderDesc", this.getPayDes(payIndex));
		param_map.put("orderTitle", this.getPayDes(payIndex));
		param_map.put("orderTime", orderTime);
		param_map.put("storeId", PluginVivo.STORDID);
		param_map.put("appId", PluginVivo.APPID);
		param_map.put("storeOrder", storeOrder);
		param_map.put("version", "1.0.0");
		
		String signature = generateSignature(param_map);
		NameValuePair[] nameValuePairs = new NameValuePair[11];
		int i = 0;
		nameValuePairs[i++] = new BasicNameValuePair("notifyUrl", notifyUrl);
		nameValuePairs[i++] = new BasicNameValuePair("orderAmount",
				String.valueOf(this.getPriceValue(payIndex)) + ".00");
		nameValuePairs[i++] = new BasicNameValuePair("orderDesc", this.getPayDes(payIndex));
		nameValuePairs[i++] = new BasicNameValuePair("orderTitle", this.getPayDes(payIndex));
		nameValuePairs[i++] = new BasicNameValuePair("orderTime", orderTime);
		nameValuePairs[i++] = new BasicNameValuePair("signature", signature);
		nameValuePairs[i++] = new BasicNameValuePair("signMethod", "MD5");
		nameValuePairs[i++] = new BasicNameValuePair("storeId", PluginVivo.STORDID);
		nameValuePairs[i++] = new BasicNameValuePair("appId", PluginVivo.APPID);
		nameValuePairs[i++] = new BasicNameValuePair("storeOrder", storeOrder);
		nameValuePairs[i++] = new BasicNameValuePair("version", "1.0.0");
		
		return nameValuePairs;
	}
	
	private String generateSignature(Map<String, String> param_map)
	{
		String result = null;
		result = VivoSignUtils.getVivoSign(param_map, PluginVivo.APPKEY);
		return result;
	}
	
	/* VivoSignUtils begin */
	private static class VivoSignUtils
	{
		public final static String SIGNATURE = "signature";
		public final static String SIGN_METHOD = "signMethod";
		public static final String QSTRING_EQUAL = "=";
		public static final String QSTRING_SPLIT = "&";
		
		public static String buildReq(Map<String, String> req, String key)
		{
			Map<String, String> filteredReq = paraFilter(req);
			String signature = getVivoSign(filteredReq, key);
			filteredReq.put(SIGNATURE, signature);
			filteredReq.put(SIGN_METHOD, "MD5");
			return createLinkString(filteredReq, false, true); // 请求字符串，key不需要排序，value需要URL编码
		}
		
		public static boolean verifySignature(Map<String, String> para, String key)
		{
			Map<String, String> filteredReq = paraFilter(para);
			String signature = getVivoSign(filteredReq, key);
			String respSignature = para.get(SIGNATURE);
			System.out.println("服务器签名：" + signature + " | 请求消息中的签名：" + respSignature);
			if (null != respSignature && respSignature.equals(signature))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public static String getVivoSign(Map<String, String> para, String key)
		{
			Map<String, String> filteredReq = paraFilter(para);
			String prestr = createLinkString(filteredReq, true, false);
			prestr = prestr + QSTRING_SPLIT + md5Summary(key);
			return md5Summary(prestr);
		}
		
		public static Map<String, String> paraFilter(Map<String, String> para)
		{
			Map<String, String> result = new HashMap<String, String>();
			if (para == null || para.size() <= 0) { return result; }
			for (String key : para.keySet())
			{
				String value = para.get(key);
				if (value == null || value.equals("") || key.equalsIgnoreCase(SIGNATURE)
						|| key.equalsIgnoreCase(SIGN_METHOD))
				{
					continue;
				}
				result.put(key, value);
			}
			return result;
		}
		
		public static String createLinkString(Map<String, String> para, boolean sort, boolean encode)
		{
			List<String> keys = new ArrayList<String>(para.keySet());
			if (sort) Collections.sort(keys);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < keys.size(); i++)
			{
				String key = keys.get(i);
				String value = para.get(key);
				if (encode)
				{
					try
					{
						value = URLEncoder.encode(value, "utf-8");
					}
					catch (UnsupportedEncodingException e)
					{
					}
				}
				
				if (i == keys.size() - 1)
				{
					sb.append(key).append(QSTRING_EQUAL).append(value);
				}
				else
				{
					sb.append(key).append(QSTRING_EQUAL).append(value).append(QSTRING_SPLIT);
				}
			}
			return sb.toString();
		}
		
		public static String md5Summary(String str)
		{
			if (str == null) { return null; }
			MessageDigest messageDigest = null;
			try
			{
				messageDigest = MessageDigest.getInstance("MD5");
				messageDigest.reset();
				messageDigest.update(str.getBytes("utf-8"));
			}
			catch (NoSuchAlgorithmException e)
			{
				return str;
			}
			catch (UnsupportedEncodingException e)
			{
				return str;
			}
			byte[] byteArray = messageDigest.digest();
			StringBuffer md5StrBuff = new StringBuffer();
			for (int i = 0; i < byteArray.length; i++)
			{
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
					md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				else
					md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}
			return md5StrBuff.toString();
		}
	}
	/* VivoSignUtils end */
	
	/* InitialPayTask begin */
	private static class InitialPayTask extends AsyncTask<NameValuePair, Integer, String>
	{
//		static private ProgressDialog waitingDialog = null;
//		static private Timer mTimer = null;
//		static private TimerTask mTimerTask = null;
//		
//		static private Handler cancelDialogHandler = new Handler()
//		{
//			@Override
//			public void handleMessage(android.os.Message msg)
//			{
//				if (waitingDialog != null)
//				{
//					waitingDialog.dismiss();
//					waitingDialog = null;
//					Toast.makeText(SDKCtrl.cocosActivity, "支付失败，网络连接超时", Toast.LENGTH_SHORT).show();
//					
//					if (mTimer != null)
//					{
//						mTimer.cancel();
//						mTimerTask.cancel();
//						mTimer = null;
//						mTimerTask = null;
//					}
//					
//				}
//			}
//		};
		
		@Override
		protected void onPreExecute()
		{
			// todo 显示Dialog
//			if (waitingDialog == null)
//			{
//				waitingDialog = new ProgressDialog(SDKCtrl.cocosActivity);
//				waitingDialog.setTitle("支付中");
//				waitingDialog.setMessage("等待服务器的响应...");
//				waitingDialog.setIndeterminate(true);
//				waitingDialog.setCancelable(false);
//				waitingDialog.show();
//			}
//			
//			if (mTimer == null && mTimerTask == null)
//			{
//				mTimer = new Timer();
//				mTimerTask = new TimerTask()
//				{
//					public void run()
//					{
//						cancelDialogHandler.sendEmptyMessage(1);
//					}
//				};
//				mTimer.schedule(mTimerTask, 5000);
//			}
			Toast.makeText(SDKCtrl.cocosActivity, "正在发起支付，请不要频繁点击按钮", Toast.LENGTH_SHORT).show();
			
		}
		
		@Override
		protected String doInBackground(NameValuePair... nameValuePairs)
		{
			NetworkRequestAgent networkRequestAgent = new NetworkRequestAgent(SDKCtrl.cocosActivity);
			String result = networkRequestAgent.sendRequest("https://pay.vivo.com.cn/vivoPay/getVivoOrderNum",
					nameValuePairs);
			Log.d("doInBackground", result);
			return result;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			try
			{
//				if (waitingDialog != null)
//				{
//					waitingDialog.dismiss();
//					waitingDialog = null;
//				}
//				
//				if (mTimer != null)
//				{
//					mTimer.cancel();
//					mTimerTask.cancel();
//					mTimer = null;
//					mTimerTask = null;
//				}
				
				if (!UtilTool.checkStringIsNull(result))
				{
					Log.d("InitialPayTask", "result=" + result);
					
					JSONObject jsonVo = new JSONObject(result);
					String respCode = jsonVo.getString("respCode");
					if (respCode.equals("200"))
					{
						String transNo = jsonVo.getString("vivoOrder");
						String signature = jsonVo.getString("vivoSignature");
						
						Bundle localBundle = new Bundle();
						localBundle.putString("transNo", transNo);
						localBundle.putString("signature", signature);
						 localBundle.putString("package",SDKCtrl.cocosActivity.getPackageName());
						localBundle.putBoolean("useWeixinPay", false);
						localBundle.putString("useMode", "00");
						localBundle.putString("productName", PluginVivo.Instance.getPayDes(SDKAbstract.payIndex));
						localBundle.putString("productDes", PluginVivo.Instance.getPayDes(SDKAbstract.payIndex));
						
						DecimalFormat format_d = new DecimalFormat("#.##");
						String prize = "" + PluginVivo.Instance.getPriceValue(SDKAbstract.payIndex) + ".00";
						Double price = format_d.parse(prize).doubleValue();
						localBundle.putDouble("price", price);
						localBundle.putString("userId", "aiyangcheng3User");
						
						if (PluginVivo.Instance.isSupportWeiXin)
						{
							localBundle.putInt("mPaymentType", 1);
							PluginVivo.Instance.mVivoUnionManager.singlePaymentDirectly(SDKCtrl.cocosActivity,localBundle);
						}
						else
						{
							PluginVivo.Instance.mVivoUnionManager.singlePayment(SDKCtrl.cocosActivity, localBundle);
						}
						
					}
					else
					{
						Toast.makeText(SDKCtrl.cocosActivity, "支付失败，网络连接出现了问题", Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					Toast.makeText(SDKCtrl.cocosActivity, "支付失败，网络连接出现了问题", Toast.LENGTH_SHORT).show();
				}
			}
			catch (Exception e)
			{
				Toast.makeText(SDKCtrl.cocosActivity, "支付失败，网络连接出现了问题", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}
	/* InitialPayTask end */
	
	/* UtilTool begin */
	public static class UtilTool
	{
		private static final String TAG = "UtilTool";
		
		public static boolean checkStringIsNull(String target)
		{
			if (null == target || target.trim().length() == 0) { return true; }
			return false;
		}
		
		public static String convertStreamToString(InputStream is)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			try
			{
				while ((line = reader.readLine()) != null)
				{
					sb.append(line);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
		
		public static void log(String tag, String info)
		{
			Log.d(tag, info);
		}
		
		public static void chmod(String permission, String path)
		{
			try
			{
				String command = "chmod " + permission + " " + path;
				Runtime runtime = Runtime.getRuntime();
				runtime.exec(command);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		//
		// show the progress bar.
		public static ProgressDialog showProgress(Context context, CharSequence title, CharSequence message,
				boolean indeterminate, boolean cancelable)
		{
			ProgressDialog dialog = null;
			if (!UtilTool.checkStringNull(message.toString()))
			{
				dialog = new ProgressDialog(context);
				dialog.setTitle(title);
				dialog.setMessage(message);
				dialog.setIndeterminate(indeterminate);
				dialog.setCancelable(false);
			}
			return dialog;
		}
		
		// public static void showDialog(Activity context, String strTitle,
		// String strText)
		// {
		// if (!checkStringNull(strText))
		// {
		// AlertDialog.Builder tDialog = new AlertDialog.Builder(context);
		// // tDialog.setIcon(icon);
		// tDialog.setTitle(strTitle);
		// tDialog.setMessage(strText);
		// tDialog.setPositiveButton(Constant.Ensure, null);
		// if (context.isFinishing())
		// {
		// Log.e(TAG, "activity isFinishing");
		// }
		// else
		// {
		// tDialog.show();
		// }
		// }
		// }
		
		public static JSONObject string2JSON(String str, String split)
		{
			JSONObject json = new JSONObject();
			try
			{
				String[] arrStr = str.split(split);
				for (int i = 0; i < arrStr.length; i++)
				{
					String[] arrKeyValue = arrStr[i].split("=");
					json.put(arrKeyValue[0], arrStr[i].substring(arrKeyValue[0].length() + 1));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return json;
		}
		
		public static boolean checkStringNull(String str)
		{
			if (null == str || str.trim().length() == 0) { return true; }
			return false;
		}
		
		public static String generateRandomSeq(int len)
		{
			Random rnd = new Random(System.currentTimeMillis());
			String random_seq = new String();
			random_seq = String.valueOf(rnd.nextInt(10));
			for (int i = 1; i < len; i++)
			{
				int temp = rnd.nextInt(len);
				switch (temp)
				{
					case 10:
						random_seq += "A";
						break;
					case 11:
						random_seq += "B";
						break;
					case 12:
						random_seq += "C";
						break;
					case 13:
						random_seq += "D";
						break;
					case 14:
						random_seq += "E";
						break;
					case 15:
						random_seq += "F";
						break;
					default:
						random_seq += String.valueOf(temp);
						break;
				}
			}
			return random_seq;
		}
	}
	/* UtilTool end */
}
