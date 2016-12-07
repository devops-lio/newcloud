package com.broadeast.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.broadeast.bean.AjaxPageBean;
import com.broadeast.entity.CloudUser;
import com.broadeast.entity.PortalUser;
import com.broadeast.entity.SiteCustomerInfo;
import com.broadeast.entity.SiteIncome;
import com.broadeast.entity.SitePaymentRecord;
import com.broadeast.entity.SitePriceConfig;
import com.broadeast.service.impl.SitePriceConfigServiceImpl;
import com.broadeast.service.impl.UserServiceImpl;
import com.broadeast.util.BigDecimalUtil;
import com.broadeast.util.DateUtil;
import com.broadeast.util.ExecuteResult;

@Controller
public class SitePriceConfigController {
	@Autowired
	private SitePriceConfigServiceImpl cr;
	@Autowired
	private SitePriceConfigServiceImpl scs;
	@Autowired
	private UserServiceImpl usi;
	
	
	/**
	 * 返回规则列表 并且用户场所名称
	 * @param curPage
	 * @param pageSize
	 * @param session
	 * @return
	 */
	@RequestMapping("getSitePriceConfigList")
	@ResponseBody
	public String getChargeRule(@RequestParam(defaultValue = "1") int curPage,
			@RequestParam(defaultValue = "10") int pageSize,@RequestParam(defaultValue = "-1") String siteName, HttpSession session) {
		ExecuteResult result=new ExecuteResult(); result.setCode(200);
		int userId=((CloudUser)session.getAttribute("user")).getId();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("uid",userId+"");
		map.put("curPage",curPage+"");//当前第二页
		map.put("pageSize",pageSize+"");//当前页条数  每页显示条
		if (!siteName.equals("-1")) {
			map.put("siteName",siteName);
		}
		try {
			AjaxPageBean ab = cr.getChargeRule(map);
			  result.setCode(1);
			  result.setData(ab); //返回用户场所
			} catch (Exception e) {
				result.setCode(-1);
				result.setMsg("查询异常");
				e.printStackTrace();
				}
		return result.toJsonString();
	}
	/**
	 * 返回用户拥有的场所
	 * @param session
	 * @return
	 */
	@RequestMapping("getSiteList")
	@ResponseBody
	public String getUserSite(HttpSession session){
		int userId=((CloudUser)session.getAttribute("user")).getId();
		ExecuteResult result=new ExecuteResult();
		result.setCode(200);
		result.setData(cr.getUserSiet(userId));
		return result.toJsonString();
	}

	
	
	

	/**
	 *更新场所设置（是否开启试用 +终端数） 
	 *更新 成功=1  失败=-1
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping("updatesite")
	@ResponseBody
	public String UpdateSite(HttpServletRequest request,HttpSession session,
			@RequestParam(defaultValue = "-1") int SiteIdUpdate,@RequestParam(defaultValue = "-1")  int updateNum,
			@RequestParam  String editSiteName,
			@RequestParam  String editSiteAddress,
			@RequestParam(defaultValue = "-1") int probatives	){ 
		int userId=((CloudUser)session.getAttribute("user")).getId();
		int  i=cr.UpdateSite(SiteIdUpdate, userId, probatives, updateNum,editSiteName,editSiteAddress);
		ExecuteResult result=new ExecuteResult();
		result.setCode(i);
		return result.toJsonString();
	}
	/**
	 * 跳转计费管理
	 * @return
	 */
	@RequestMapping("SitePriceConfig")
	public String RsSitePriceConfigList(){
		return "/SitePriceConfig/SitePriceConfigList";
	}
}
