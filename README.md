# 说明
## 1. 起源
最近在帮公司接入渠道支付SDK的小姐姐分担工作，分给我一个游戏，让我更新渠道的支付SDK，然后给了我一张表，里边密密麻麻的有三十几个渠道包，看得我头皮发麻。在细细的研究了公司项目（**单机**）的支付流程，为了提高代码的复用性和降低耦合性（**主要是各种渠道的import xxxx.java太烦人**)。写了这么一套的东西，主要是以插件的形式来管理各种支付渠道，需要用到哪个渠道的支付就引入哪个渠道的支付java文件就好了。这种代码组织方式主要是还是从前段时间使用的[SDKBOX](http://www.sdkbox.com/)获取的灵感.

### 使用这个架构的好处如下
1. 代码的复用性更强了，写完的代码保存下来，下次哪个项目需要直接复制过去就好了
2. 代码的耦合性更低了，整个项目的支付只和*SDKCtrl.java*打交道，避免了各种渠道提供的各种奇怪的api


## 2.目录说明
### **src/SDKAbstract.java**
所有渠道插件都要继承自这个Java文件，他提供了基本的

1. 渠道插件初始化
2. 发起支付
3. 退出游戏（如果渠道SDK支持的话）
4. 显示/关闭广告（如果渠道SDK支持的话）


### **src/SDKConfig.java**
渠道SDK的基本配置

### **src/SDKCtrl.java**
项目调用渠道SDK的操作全部都是通过这个java文件，所有渠道的特性对于调用者来说是透明的，调用者并不关心渠道SDK的具体操作是怎么样的。他提供的功能如下 

1. 初始化渠道的SDK
2. 调用渠道的支付
3. 调用渠道的退出
4. 调用渠道的显示广告
5. 在**Cocos2dxActivity**的生命周期内调用相应的生命周期事件（**onCreate，onPause 等等**）

### **src/SDKCtrlDelegate.java**
需要调用者继承这个delegate，并实现购买结束时候的回调

### **src/PluginXXX.java**
各种渠道支付插件的具体实现。请注意本例子已经提供了一些渠道支付写好的插件，但是接入渠道支付时候的**AndroidManifest.xml**的修改，以及手动拷贝渠道支付需要的**jar包**还是需要自己做的

**每个支付渠道都会有自己的appID ,appKey之类的东西，使用相应的插件之前，请修改源码里对应的appid之类的东西。**

**每个支付渠道都会有自己的appID ,appKey之类的东西，使用相应的插件之前，请修改源码里对应的appid之类的东西。**

**每个支付渠道都会有自己的appID ,appKey之类的东西，使用相应的插件之前，请修改源码里对应的appid之类的东西。**

(重要的事情说三遍)

## 3. 使用（这里以**百度的单机SDK**举例）
### 假设我们的Activity类名字叫 **TestActivity**,首先这个**TestActivity**需要继承自**Cococs2dxActivity**并实现**SDKCtrlDelegate**的方法

```java
public class TestActivity extends Cocos2dxActivity implements SDKCtrlDelegate{
	@Override
	public void onFinishPurchase(boolean result, String message, int payIndex, String payCode)
	{
			if(result)
				Log.d("SDKCtrl", "支付成功了，支付成功的序号是"+payIndex);
			else 
				Log.d("SDKCtrl","支付失败了，失败的原因是"+message);
	}
	
}
```

### 第二步在**onCreate**函数里初始化我们的**SDKCtrl**和需要使用到的**渠道SDK**

```java
 protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			SDKCtrl.init(this, this, "Baidu");
			SDKCtrl.initSDK("Baidu",this.getSDKConfig("Baidu"));
		}
```

### getSDKConfig函数如下

```java
  public static SDKConfig getSDKConfig(String sdkName)
	    {
	    	HashMap<String, String> specialConfigMap = new HashMap<String, String>();
	    	HashMap<String , String> priceValueMap = new HashMap<String, String>();
	    	HashMap<String,String> 	priceDesMap =  new HashMap<String, String>();
	    	HashMap<String , String> payDesMap =  new HashMap<String, String>();
	    	HashMap<String , String> payCodeMap = new HashMap<String , String>();
	    
	    	 if(sdkName.equals("Baidu"))
	    	{
	    		 //key 必须是数字，对应着 onFinishPurchase 函数中对应的payIndex
	    		priceValueMap.put("1" , "2");		priceDesMap.put("1", "￥2元");		payDesMap.put ("1","商品1");							payCodeMap.put("1", "15735");					
	        	priceValueMap.put("2" , "8");		priceDesMap.put("2", "￥8元");		payDesMap.put ("2","商品2");							payCodeMap.put("2", "15736");	
	        	priceValueMap.put("3","18");		priceDesMap.put("3", "￥18元");		payDesMap.put ("3","商品3");						payCodeMap.put("3", "15737");	
	        	priceValueMap.put("4" , "30");		priceDesMap.put("4", "￥30元");		payDesMap.put ("4","商品4");						payCodeMap.put("4", "15738");	
	    	}
	    	else
	    	{
	    		Log.e("SDKCtrl",sdkName+" SDKConfig cant find");
	    	}
	    	SDKConfig sdkConfig = new SDKConfig(specialConfigMap, priceValueMap, priceDesMap, payDesMap, payCodeMap);
	    	return sdkConfig;
	    }
```

### 接下来实现生命周期的函数，这里只举2个函数，其他的onPause之类的类比


```java
	  @Override
	  protected void onDestroy() {
	 		super.onDestroy();
	 		SDKCtrl.onDestroy("Baidu");
	 		android.os.Process.killProcess(android.os.Process.myPid());
	 	}
	  
	 
	@Override
	 protected void onRestart() {
	
	 		super.onRestart();
	 		SDKCtrl.onRestart("Baidu);
	 }
```

### 接下来就是比较重要的发起支付了

```java
		void  pay(int payIndex)
	 	{
	 		//payIndx 就是 getConfig函数里的key ，这里的1对应的就是 “商品1 " 计费ID是"15735"
	 		SDKCtrl.initiatePurchase(1, "Baidu");
	 	}
```

### 其他见SDKCtrl.java的源码


## 4. 注意（重要的事情说三遍）
SDKCtrl本身没有考虑过cocos的opengl线程和主线程的问题，请确保发起支付之类的调用和调用购买后的回调的线程安全的问题

SDKCtrl本身没有考虑过cocos的opengl线程和主线程的问题，请确保发起支付之类的调用和调用购买后的回调的线程安全的问题

SDKCtrl本身没有考虑过cocos的opengl线程和主线程的问题，请确保发起支付之类的调用和调用购买后的回调的线程安全的问题













