package com.broadeast.controller;

import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

/**
 * Copyright (c) All Rights Reserved, 
 * 2016. 版权所有 kdf Information Technology Co.,Ltd
 * @Project newCloud
 * @File SendMsgController.java
 * @Date 2016年3月29日 下午3:01:50
 * @Author gyj
 */
@Controller
@RequestMapping("/TelCodeManage")
public class SendMsgController {
	private static Logger logger = Logger.getLogger(SendMsgController.class);
	private static final String URL = "https://eco.taobao.com/router/rest";
	private static final String KEY = "23425716";
	private static final String SECRET = "4451f27576ed9115736ddf079121c56b";
	private static final String SIGN = "宽东方";
	private static final String MESTYPE = "normal";

	/**
	 * @Description 发送验证码调用接口
	 * @param telephone --- 被发送人的手机号
	 * @param templateCode ---模板短信id,这个是自定义的短信模板
	 * @param session
	 * @return -1是发送失败,0是发送成功,-2是一分钟内同一个手机号多次发送
	 */
	@RequestMapping("sendTelCode")
	@ResponseBody
	public String getSendMsgRandCode(@RequestParam String tel,@RequestParam String templateCode, HttpSession session) {
		try {
			String code = randCode();
			String str = sendMsgToUser(tel, templateCode, code);
			if (!"error".equals(str)) {
				boolean flag = sendMsgIsSuccessful(str);
				if (flag) {
					session.setAttribute(tel, code);
					session.setAttribute("randCodeTime", new Date().getTime());
					return "0";
				} else {
					String errStr = backErrorMsg(str);
					if ("触发业务流控".equals(errStr)) {
						return "-2";
					} else {
						return "-1";
					}
				}
			} else {
				return "-1";
			}
		} catch (Exception e) {
			logger.error("发送验证码失败", e);
			return "-1";
		}
	}

	/**
	 * @Description 短信发送接口
	 * @param telephone--- 接收人电话
	 * @param templateCode--- 模板短信id(后台自定义模板)
	 * @param code--- 验证码
	 * @return 成功响应的话返回json格式的字符串,异常了返回error
	 */
	public static String sendMsgToUser(String telephone, String templateCode,String code) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		TaobaoClient client = new DefaultTaobaoClient(URL, KEY, SECRET);
		AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
		req.setExtend("");
		req.setSmsType(MESTYPE);
		req.setSmsFreeSignName(SIGN);
		req.setSmsParamString(json.toString());
		req.setRecNum(telephone);
		req.setSmsTemplateCode(templateCode);
		AlibabaAliqinFcSmsNumSendResponse rsp;
		try {
			rsp = client.execute(req);
			return rsp.getBody();
		} catch (ApiException e) {
			logger.error("发送验证码失败", e);
			return "error";
		}
	}

	/**
	 * 生成四位随机数的验证码
	 * @return 四位验证码
	 */
	private static String randCode() {
		String code = "";
		for (int i = 0; i < 4; i++) {
			Random rand = new Random();
			code += rand.nextInt(9);
		}
		return code.trim();
	}

	/**
	 * @Description 判断短信是否发送成功
	 * @param jsonString 发送短信后的响应结果
	 * @return false--发送失败,true--发送成功
	 */
	public static boolean sendMsgIsSuccessful(String jsonString) {
		try {
			JSONObject json = JSONObject.fromObject(jsonString);
			Object obj = json.get("alibaba_aliqin_fc_sms_num_send_response");
			if (obj == null || "".equals(obj)) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
 
	/**
	 * @Description 返回错误信息
	 * @param jsonstring 发送短信的错误响应结果
	 * @return 错误信息的描述
	 */
	public static String backErrorMsg(String jsonstring) {
		JSONObject json = JSONObject.fromObject(jsonstring);
		JSONObject obj = JSONObject.fromObject(json.get("error_response"));
		String result = obj.getString("sub_msg");
		if(result==null||"".equals(result)){
			return "error";
		}else{
			return result;
		}
	}

	/**
	 * 测试时打开这个方法 注释真正发短信的方法
	 * @throws ApiException 
	 */
	
/*	@RequestMapping("sendTelCode")
	@ResponseBody
	public String getSendMsgRandCode(@RequestParam String tel,@RequestParam String templateCode, HttpSession session) {
		String code = randCode();
		session.setAttribute(tel, code);
		session.setAttribute("randCodeTime", new Date().getTime());
		System.out.println(code);
		return "0";
		 
		
		
	}*/
	 
	
}
