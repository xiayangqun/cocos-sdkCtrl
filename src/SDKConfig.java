package com.catcap.IAP;

import java.util.HashMap;

public class SDKConfig {

	//存放SDK需要初始化的时候的一些特殊的不通用的变量
	public   HashMap<String, String> specialConfigMap;
	

	//存放价格的描述。 例如 put <"1","18"> 
	public HashMap<String , String> priceValueMap;

	//存放价格的中文描述，例如 put<"1","￥18元">
	public HashMap<String,String> priceDesMap;
	
	
	//存放商品描述的Map ,例如 put<"1","魔兽大礼包">
	public HashMap<String , String> payDesMap;
	
	//存放支付代码的Map，例如  put<"1","1992239002">
	public HashMap<String , String> payCodeMap;
	
	public SDKConfig(HashMap<String, String>  _specialConfigMap , HashMap<String , String> _priceValueMap ,    HashMap<String,String> _priceDesMap,   HashMap<String , String> _payDesMap,     HashMap<String , String> _payCodeMap )
	{
		this.specialConfigMap=_specialConfigMap;
		this.priceValueMap=_priceValueMap;
		this.priceDesMap=_priceDesMap;
		this.payDesMap=_payDesMap;
		this.payCodeMap = _payCodeMap;
	}
}


