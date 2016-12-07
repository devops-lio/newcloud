package com.broadeast.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.nutz.dao.Dao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.broadeast.entity.BusinessData;
import com.broadeast.util.CalendarUtil;
import com.broadeast.util.DateUtil;

/**
 *  主要用来编写定时器任务函数
 * @author Administrator
 *
 */
@Service
public class TimingtaskServiceImpl {
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Resource(name="templJdbcTemplate")
	private JdbcTemplate templJdbcTemplate;
	
	Logger log = Logger.getLogger(TimingtaskServiceImpl.class);
	
	
		public void lockUserTime(){
			System.out.println("每隔5分钟调用一次");
			String date = CalendarUtil.yesteday();
			String radacct_table="radacct_"+date;
			String table = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME='"
					+ radacct_table + "'";
			List<String> isTable = jdbcTemplate.queryForList(table,
					String.class);
			if (isTable != null && isTable.size() != 0) {
			}else{
				radacct_table = "radacct";
			}
			statistical(radacct_table,date);
			deleteLog4j();
			String sql = "SELECT username,siteid,count(*) as num FROM(SELECT username,(SELECT t.site_id FROM t_cloud_site_routers t WHERE dfid=r.dfid) AS siteid FROM  "+radacct_table+" r GROUP BY username,callingstationid,siteid) a GROUP BY a.username,a.siteid HAVING num>(SELECT allow_client_num FROM t_cloud_site t_c WHERE t_c.id=a.siteid)";
			try{
			List rs = jdbcTemplate.queryForList(sql);
			for(int i=0;i<rs.size();i++){                    
				Map userMap=(Map) rs.get(i);  
				String username = (String)userMap.get("username");
				Long siteid=(Long)userMap.get("siteid");
				String upsql = "UPDATE t_site_customer_info SET lock_time=NOW() WHERE site_id="+siteid+" AND portal_user_id=(select id FROM t_portal_user where user_name='"+username+"') AND (lock_time<DATE_SUB(NOW(),INTERVAL 1 DAY) OR lock_time IS NULL)";
				int num =  jdbcTemplate.update(upsql);
			}
			}catch(Exception e){
				log.error(e);
			}
		}
		/**
		 * @Description: 删除前一天认证日志列表信息
		 * @Date		2016年10月18日 下午3:04:24
		 * @Author		liuzhao
		 */
		public void deleteLog4j(){
			String date = new SimpleDateFormat("yyyy-MM-dd 00:01:00").format(new Date());
			try {
				String delsql = "DELETE FROM radpostauth  WHERE authdate < ?";
				jdbcTemplate.update(delsql,new Object[]{date});
			} catch (Exception e) {
				log.error(e);
			}
			
		}
		public void statistical(String acct_table,String date){
			
			//注册比例 
			String sqlopenporta = "SELECT newuser.num,(newuser.num+openportal.num2) as ping,site_id from(SELECT COUNT(*) as num,site_id FROM (SELECT site_id FROM t_site_customer_info WHERE portal_user_id in (SELECT id FROM t_portal_user WHERE  date_format(create_time,'%Y%m%d')='"+date+"') GROUP BY portal_user_id) as info GROUP BY site_id) as newuser,(SELECT COUNT(1) as num2,site FROM (SELECT DISTINCT clientmac,(SELECT site_id FROM t_cloud_site_routers WHERE dfid = t1.nasid) as site FROM t2_portallog_"+date+" t1,(SELECT callingstationid,dfid FROM "+acct_table+" GROUP BY callingstationid,dfid) t2 WHERE t1.clientmac <> t2.callingstationid and t1.nasid=t2.dfid) as portzhuce GROUP BY site) AS openportal WHERE newuser.site_id=openportal.site";
			
			//独立用户总数
			String sqlopencount="SELECT COUNT(DISTINCT clientmac) as num,(SELECT site_id FROM t_cloud_site_routers tc WHERE tc.dfid= t2.nasid) as site_id  FROM t2_portallog_"+date+" t2 GROUP BY site_id";
			
			//注册用户数
			String sqlregistnum = "SELECT COUNT(*) as num, site_id FROM(SELECT site_id FROM t_cloud_site_portal GROUP BY portal_id) site GROUP BY site_id";
			
			//付费用户总数 
			//String sqlpaycount="SELECT COUNT(*) as num, site_id FROM(SELECT site_id FROM t_site_customer_info  WHERE  TIMESTAMPDIFF(MINUTE,create_time,expiration_time)>((SELECT is_probative FROM t_cloud_site WHERE id = site_id)*60)) site GROUP BY site_id  ORDER BY NULL";
			String sqlpaycount="SELECT COUNT(DISTINCT portal_user_id) as num,site_id FROM t_site_income GROUP BY site_id";
		
			//昨日登录用户数
			String sqllogcount="SELECT COUNT(*) as num,site_id FROM(SELECT DISTINCT username,(SELECT site_id FROM t_cloud_site_routers tc WHERE tc.dfid= ra.dfid) as site_id FROM "+acct_table+" ra) logn GROUP BY site_id";
			
			//网络覆盖率
			String sqlnetcount="SELECT portalnum.num,tcs.siteNum as ping,site as site_id FROM (SELECT COUNT(DISTINCT clientmac)AS num,(SELECT site_id FROM t_cloud_site_routers tc WHERE tc.dfid= t2.nasid) AS site FROM t2_portallog_"+date+"  t2 GROUP BY site) AS portalnum,t_cloud_site AS tcs WHERE portalnum.site = tcs.id";
			
			//注册缴费率
			String sql = "select b.site_id,a.count_pay,b.count_register"
							+" from("
							+" SELECT site.site_id,count(site.portal_user_id) as 'count_pay' FROM t_site_income site "
							+" left join t_portal_user user on user.id = site.portal_user_id "
							+" left join t_site_customer_info info on info.portal_user_id = user.id "
							+" where "
							+" date_format(site.create_time,'%Y%m%d')='"+date+"' "
							+" and date_format(user.create_time ,'%Y%m%d')='"+date+"' "
							+" group by site.site_id) as a "
							+" right join  "
							+" ( "
							+" SELECT info.site_id,count(user.id) as 'count_register' FROM t_portal_user user "
							+" right join t_site_customer_info info on info.portal_user_id = user.id "
							+" where  "
							+" date_format(user.create_time ,'%Y%m%d')='"+date+"' "
							+" group by info.site_id "
							+" ) as b on a.site_id = b.site_id  "
							+" group by a.site_id ";
			
			List<BusinessData> business = new ArrayList<>();
			try{
				//注册比例 
				List rs = jdbcTemplate.queryForList(sqlopenporta);
				for(int i=0;i<rs.size();i++){   
					BusinessData bd = new BusinessData();
					Map numMap=(Map) rs.get(i);  
					Number num = (Number)numMap.get("num");
					Number num2=(Number)numMap.get("ping");
					Number siteid =(Number)numMap.get("site_id");
					bd.setSiteId(siteid==null?0:siteid.intValue());
					bd.setTryRegisterRatenum(num==null?0:num.intValue());
					bd.setTryRegisterRateden(num2==null?0:num2.intValue());
					business.add(bd);
				}//独立用户总数
				rs = jdbcTemplate.queryForList(sqlopencount);
				for(int i=0;i<rs.size();i++){  
					BusinessData bd = null;
					Map numMap=(Map) rs.get(i);  
					Number siteid =(Number)numMap.get("site_id");
					if(siteid==null)
						continue;
					for(int j=0;j<business.size();j++){
						if(siteid.intValue()==business.get(j).getSiteId().intValue()){
							bd = business.get(j);
							Number num = (Number)numMap.get("num");
							business.get(j).setUvNum(num==null?0:num.intValue());
							break;
						}
					}
					if(bd == null){
						bd = new BusinessData();
					}else
						continue;
					Number num = (Number)numMap.get("num");
					bd.setUvNum(num==null?0:num.intValue());
					bd.setSiteId(siteid.intValue());
					business.add(bd);
				}
				//注册用户数
				rs = jdbcTemplate.queryForList(sqlregistnum);
				for(int i=0;i<rs.size();i++){  
					BusinessData bd = null;
					Map numMap=(Map) rs.get(i);  
					Number siteid =(Number)numMap.get("site_id");
					if(siteid==null)
						continue;
					for(int j=0;j<business.size();j++){
						if(siteid.intValue()==business.get(j).getSiteId().intValue()){
							bd = business.get(j);
							Number num = (Number)numMap.get("num");
							business.get(j).setRegisterNum(num==null?0:num.intValue());
							break;
						}
					}
					if(bd == null){
						bd = new BusinessData();
					}else
						continue;
					Number num = (Number)numMap.get("num");
					bd.setRegisterNum(num==null?0:num.intValue());
					bd.setSiteId(siteid.intValue());
					business.add(bd);
				}//付费用户总数 
				rs = jdbcTemplate.queryForList(sqlpaycount);
				for(int i=0;i<rs.size();i++){  
					BusinessData bd = null;
					Map numMap=(Map) rs.get(i);  
					Number siteid =(Number)numMap.get("site_id");
					if(siteid==null)
						continue;
					for(int j=0;j<business.size();j++){
						if(siteid.intValue()==business.get(j).getSiteId().intValue()){
							bd = business.get(j);
							Number num = (Number)numMap.get("num");
							business.get(j).setPayNum(num==null?0:num.intValue());
							break;
						}
					}
					if(bd == null){
						bd = new BusinessData();
					}else
						continue;
					Number num = (Number)numMap.get("num");
					bd.setPayNum(num.intValue());
					bd.setSiteId(siteid.intValue());
					business.add(bd);
				}//昨日登录用户数
				rs = jdbcTemplate.queryForList(sqllogcount);
				for(int i=0;i<rs.size();i++){  
					BusinessData bd = null;
					Map numMap=(Map) rs.get(i);  
					Number siteid =(Number)numMap.get("site_id");
					if(siteid==null)
						continue;
					for(int j=0;j<business.size();j++){
						bd = business.get(j);
						if(siteid.intValue()==business.get(j).getSiteId().intValue()){
							Number num = (Number)numMap.get("num");
							business.get(j).setLoginNum(num==null?0:num.intValue());
							break;
						}
					}
					if(bd == null){
						bd = new BusinessData();
					}else
						continue;
					Number num = (Number)numMap.get("num");
					bd.setLoginNum(num==null?0:num.intValue());
					bd.setSiteId(siteid.intValue());
					business.add(bd);
				}//网络覆盖率
				rs = jdbcTemplate.queryForList(sqlnetcount);
				for(int i=0;i<rs.size();i++){  
					BusinessData bd = null;
					Map numMap=(Map) rs.get(i);  
					Number siteid =(Number)numMap.get("site_id");
					if(siteid==null)
						continue;
					for(int j=0;j<business.size();j++){
						if(siteid.intValue()==business.get(j).getSiteId().intValue()){
							bd = business.get(j);
							Number num = (Number)numMap.get("num");
							Number num2=(Number)numMap.get("ping");
							business.get(j).setPermeateRatenum(num==null?0:num.intValue());
							business.get(j).setPermeateRateden(num2==null?0:num2.intValue());
							break;
						}
					}
					if(bd == null){
						bd = new BusinessData();
					}else
						continue;
					Number num = (Number)numMap.get("num");
					Number num2=(Number)numMap.get("ping");
					bd.setPermeateRatenum(num==null?0:num.intValue());
					bd.setPermeateRateden(num2==null?0:num2.intValue());
					bd.setSiteId(siteid.intValue());
					business.add(bd);
				}
				//注册缴费率
				rs = jdbcTemplate.queryForList(sql);
				for(int i=0;i<rs.size();i++){  
					BusinessData bd = null;
					Map numMap=(Map) rs.get(i);  
					Number siteid =(Number)numMap.get("site_id");
					if(siteid==null)
						continue;
					for(int j=0;j<business.size();j++){
						if(siteid.intValue()==business.get(j).getSiteId().intValue()){
							bd = business.get(j);
							Number num = (Number)numMap.get("count_pay");
							Number num2=(Number)numMap.get("count_register");
							business.get(j).setRegisterPayRatenum(num==null?0:num.intValue());
							business.get(j).setRegisterPayRateden(num2==null?0:num2.intValue());
							break;
						}
					}
					if(bd == null){
						bd = new BusinessData();
					}else{
						continue;
					}
					Number num = (Number)numMap.get("count_pay");
					Number num2=(Number)numMap.get("count_register");
					bd.setRegisterPayRatenum(num==null?0:num.intValue());
					bd.setRegisterPayRateden(num2==null?0:num2.intValue());
					bd.setSiteId(siteid.intValue());
					business.add(bd);
				}
				for(int i=0;i<business.size();i++){
					BusinessData bd = business.get(i);
					String insertsql = "INSERT INTO t3_business_data(site_id,"
							+ "uv_num,register_num,pay_num,login_num,"
							+ "permeate_ratenum,permeate_rateden,"
							+ "try_register_ratenum,try_register_rateden,"
							+ "register_pay_ratenum,register_pay_rateden,"
							+ "create_time)VALUES("
							+bd.getSiteId()+ ","
							+bd.getUvNum()+ ","
							+bd.getRegisterNum() +","
							+bd.getPayNum()+ ","
							+ bd.getLoginNum()+","
							+bd.getPermeateRatenum()+ ","
							+ bd.getPermeateRateden()+","
							+ bd.getTryRegisterRatenum()+","
							+ bd.getTryRegisterRateden()+","
							+ bd.getRegisterPayRatenum()+","
							+bd.getRegisterPayRateden()+","
							+"'"+CalendarUtil.currentTime()+"')";
					jdbcTemplate.update(insertsql);
				}
			}catch(Exception e){
				e.printStackTrace();
					log.error(e);
				}
			
		}
}
