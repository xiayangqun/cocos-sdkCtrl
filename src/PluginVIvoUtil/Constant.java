package com.catcap.IAP.PluginVIvoUtil;

public class Constant {

	public final static String Ensure = "确定";

	public static final String DEFAULT_CHARSET = "UTF-8";

	public static final String PARAM_VERSION = "version";
	public static final String PARAM_SIGNMETHOD = "signMethod";// 签名方法,对关键信息进行签名的算法名称：MD5
	public static final String PARAM_SIGNATURE = "signature";// 签名信息,对关键信息签名后得到的字符串

	public static final String PARAM_STOREID = "storeId";// 商户id
	public static final String PARAM_STOREORDER = "storeOrder";// 商户自定义的订单号
																// 商户自定义，最长 64
																// 位字母、数字和下划线组成
	public static final String PARAM_NOTIFYURL = "notifyUrl";// 异步通知urL

	public static final String PARAM_ORDER_TIME = "orderTime";// 交易开始日期时间,yyyyMMddHHmmss
	
	public static final String PARAM_APP_ID = "appId";

	public static final String PARAM_ORDER_AMOUNT = "orderAmount";// 交易金额
																	// 单位：元，币种：人民币，小数点后两位，如：1.01
	public static final String PARAM_ORDER_TITLE = "orderTitle";// 商品的标题
	public static final String PARAM_ORDER_DESC = "orderDesc";// 订单描述

	public static final String RESPONE_SIGNMETHOD = "signMethod";// 签名方法
																	// 对关键信息进行签名的算法名称：MD5
	public static final String RESPONE_SIGNATURE = "signature";// 签名信息
																// 对关键信息签名后得到的字符串
	public static final String RESPONE_VIVO_SIGNATURE = "vivoSignature";// 签名信息
	public static final String RESPONE_RESP_CODE = "respCode";// 响应码
	public static final String RESPONE_RESP_MSG = "respMsg";// 响应信息

	public static final String RESPONE_VIVO_ORDER = "vivoOrder";// 交易流水号 vivo订单号

	public static final String CONS_TRANS_NO = "transNo";
	public static final String CONS_RESULT_CODE = "result_code";
	public static final String CONS_PROC_NAME = "prod_name";
	public static final String CONS_PAY_RESULT = "pay_result";
	public static final String CONS_TOTAL_FREE = "total_free";
	public static final String CONS_PAY_MSG = "pay_msg";
	
	//线上环境com.example.paydemo
//	 public static final String STORE_ID = "20131030114035786189";
//	 public static final String SIGN_KEY = "20131030114035565895";
//	 public static final String APP_ID = "1007"; // com.example.paydemo
	 
	//线上环境com.tuomi.zznzh.vivo
//	public static final String STORE_ID = "20140304162939412429";
//	public static final String SIGN_KEY = "969E6D5F1C1A1E8C5E8FF17B15FC6938";
//	public static final String APP_ID = "4de4c7e0c5f0d62f7a189976d5d68825"; // com.tuomi.zznzh.vivo
	
	public static final String STORE_ID = "STORE_ID";
	public static final String SIGN_KEY = "SIGN_KEY";
	public static final String APP_ID = "APP_ID"; // com.tuomi.zznzh.vivo
}