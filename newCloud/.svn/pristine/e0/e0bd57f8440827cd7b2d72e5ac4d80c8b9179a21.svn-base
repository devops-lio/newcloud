package com.broadeast.controller;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import sun.security.util.BigInt;

import com.broadeast.bean.AjaxPageBean;
import com.broadeast.entity.CloudUser;
import com.broadeast.service.impl.HeartServiceImpl;
import com.broadeast.service.impl.SitePriceConfigServiceImpl;
import com.broadeast.util.DateUtil;
import com.broadeast.util.ExecuteResult;
import com.broadeast.util.WanipUtil;

/**
 * 接受各种设备的
 * Copyright (c) All Rights Reserved, 2016.
 * 版权所有                  kdf Information Technology Co .,Ltd
 * @Project		newCloud
 * @File		HeartController.java
 * @Date		2016年6月28日 下午2:15:47
 * @Author		cuimiao
 */
@Controller                         
public class HeartManageController {
	private static Logger logger=Logger.getLogger(HeartManageController.class);
	
	@Autowired
	public HeartServiceImpl heartServiceImpl;
	/**
	 * RouterOs心跳处理 
	 * @Description: TODO
	 * @param mac
	 * @param nasid
	 * @param os_date
	 * @param uptime
	 * @param request
	 * @param response
	 * @return
	 * @Date		2016年7月2日 下午1:08:40
	 * @Author		cuimiao
	 */
	@RequestMapping("rh")//receiveHeart
	@ResponseBody
	public String receiveHeart(@RequestParam(defaultValue = "mac_default") String mac,@RequestParam(defaultValue = "nasid_default") String nasid,
							@RequestParam(defaultValue = "date_default") String os_date,@RequestParam(defaultValue = "time_default") String uptime,
							HttpServletRequest request,HttpServletResponse response){
		//"GET /up.php?mac=E4:8D:8C:60:B1:65&nasid=hukaizhu_1&
		//os_date=Mikrotik&uptime=04:50:16%20up%2000:47:44,%20load%20average:%200% HTTP/1.1" 404 973
//		System.out.println("mac:" + mac);
//		System.out.println("nasid:" + nasid);
//		System.out.println("os_date:" + os_date);
//		System.out.println("uptime:" + uptime);
		//处理mac
		//支持mac地址 18:17:25:35:63:5C 格式
		mac = mac.replace(":", "");
		//支持mac地址 18-17-25-35-63-5C 格式
		mac = mac.replace("-", "");
		/**
		 * mac:E4:8D:8C:60:B1:65
		 * nasid:hukaizhu_1
		 * os_date:Mikrotik （不存）
		 * uptime:08:08:00 up 20:58:02, load average: 0%
		 * */
		//获取wan网IP
		String wanIp = WanipUtil.getWanIp(request, response);
		//解析uptime
		String lastTime = DateUtil.getStringDate();
		String cpuRate = uptime.substring(uptime.length()-3,uptime.length()-1).trim();
		if(cpuRate.equals("00")){
			//处理100%CPU占用率的极端情况
			cpuRate = "100";
		}
		//解析运行时长
		String runTime = uptime.substring(uptime.indexOf("up")+3, uptime.indexOf(","));
		//解析uptime 存储 最后心跳时间、设备运行时间、cpu占用率
		heartServiceImpl.updateInfoByNasid(cpuRate, lastTime, mac,wanIp,runTime, nasid);
//		System.out.println("success by cuimiao");
		return "";
		  
	}
}
