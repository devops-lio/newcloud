package com.broadeast.controller;

import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broadeast.util.CRSendSms;


@Controller
@RequestMapping("/TelCodeManageSSS")
public class SendRandCodeController {
	private static Logger logger=Logger.getLogger(SendRandCodeController.class);
	/**
	 * 计费管理系统的发送验证码
	 * @param tel
	 * @param request
	 * @param session
	 * @param content
	 * @return
	 */
	@RequestMapping("sendTelCode")
	@ResponseBody
	public String sendTelCode(@RequestParam String tel, HttpServletRequest request,HttpSession session,String content){
		String randCode = randCode();
		System.out.println("验证码 ："+randCode);
		String sign = "宽东方";
		int result = -1;
		try {
			String text ="验证码:"+randCode+content;
				result = CRSendSms.sendSms(text, sign, tel);
//				result = 0;
				if(result==0){
					session.setAttribute("randCode", randCode);
					session.setAttribute("randCodeTime", new Date().getTime());
				}
		} catch (Exception e) {
			logger.error("发送验证码失败", e);
		}
		return result+"";
	}
	
	/**
	 * 生成三位随机数
	 * 
	 * @return
	 */
	private String randCode() {
		String code = "";
		for (int i = 0; i < 4; i++) {
			Random rand = new Random();
			code += rand.nextInt(9);
		}
		return code;
	}
}
