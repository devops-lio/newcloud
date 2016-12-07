package com.broadeast.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.QueryResult;
import org.nutz.dao.impl.entity.info.LinkInfo;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.lang.Lang;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import sun.misc.BASE64Decoder;
import sun.org.mozilla.javascript.internal.ObjArray;

import com.broadeast.bean.AjaxPageBean;
import com.broadeast.bean.CloudRouterBean;
import com.broadeast.bean.RouterInfoBean;
import com.broadeast.bean.SiteInfoBean;
import com.broadeast.entity.CloudSite;
import com.broadeast.entity.CloudSiteRouters;
import com.broadeast.entity.SitePriceConfig;
import com.broadeast.entity.StoreRouter;
import com.broadeast.util.CalendarUtil;
import com.broadeast.util.InitContext;
import com.broadeast.util.OssManage;
import com.broadeast.util.PagingFactory;
@Repository
@SuppressWarnings("all")
public class CloudSiteServiceImpl {

	private static Logger log=Logger.getLogger(CloudSiteServiceImpl.class);
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="nutDao")
	private Dao nutDao;
	
	@Resource(name="templJdbcTemplate")
	private JdbcTemplate templJdbcTemplate;
	
	@Resource(name="templNutDao")
	private Dao templNutDao;
	
		
		/**
		 * 添加场所
		 * @param user
		 * @return
		 */
		public CloudSite addCloudSite(final String siteType,final String siteName,
				final String address,final int userId,final String siteNum,
				final int zd_num,final int sy_time,final int state,final int exTime){
			final Object[] objs = new Object[1];
			try {
		        Trans.exec(new Atom(){
		            public void run() {
		        		//开启事务 保存user表
		            	CloudSite cloudsite = new CloudSite();
		            	cloudsite.setSiteType(siteType);
		            	cloudsite.setUser_id(userId);
		            	cloudsite.setSite_name(siteName);
		            	cloudsite.setAddress(address);
		            	cloudsite.setSiteNum(Integer.valueOf(siteNum));
		            	cloudsite.setAllow_client_num(zd_num);
		            	cloudsite.setIs_probative(sy_time);
		            	cloudsite.setState(state);
		            	cloudsite.setExTime((exTime==1?3600:exTime*60));
//		            	cloudsite.setBannerUrl(bannerUrl);//插入bannerUrl
		            	CloudSite siteTemp = nutDao.insert(cloudsite);
		            	objs[0] = siteTemp;
		            	//保存SitePriceConfig表
		            	//按小时收费
		            	SitePriceConfig spchour=new SitePriceConfig();
		            	spchour.setSite_id(cloudsite.getId());
		            	spchour.setPrice_type(0);
		            	spchour.setUnit_price(new BigDecimal(1));
		            	spchour.setCharge_type(0);
		            	spchour.setName("时");
		            	
		            	//按天收费
		            	SitePriceConfig spcday=new SitePriceConfig();
		            	spcday.setSite_id(cloudsite.getId());
		            	spcday.setPrice_type(1);
		            	spcday.setUnit_price(new BigDecimal(10));
		            	spcday.setCharge_type(0);
		            	spcday.setName("天");
		            	//按月收费
		            	SitePriceConfig spcmonth=new SitePriceConfig();
		            	spcmonth.setSite_id(cloudsite.getId());
		            	spcmonth.setPrice_type(2);
		            	spcmonth.setUnit_price(new BigDecimal(100));
		            	spcmonth.setCharge_type(0);
		            	spcmonth.setName("月");
		            	List<SitePriceConfig> spclist=new ArrayList<SitePriceConfig>();
		            	spclist.add(spchour);
		            	spclist.add(spcday);
		            	spclist.add(spcmonth);
		            	
		            	Daos.ext(nutDao, FieldFilter.create(SitePriceConfig.class, "^site_id|price_type|unit_price|name$")).insert(spclist);
		            }
		        });
	        }catch (Exception e) {
	        	log.error("userRegist 事务报错--", e);
	        	return (CloudSite) objs[0];
	        }
			return (CloudSite) objs[0];
		}
		
		/**
		 * 更新场所信息
		 * @param user
		 * @return
		 */
		public int updateCloudSite(final String siteType,final String siteName,
				final String address,final int siteId,final String siteNum,
				final int zd_num,final int sy_time,final int state,int exTime){
			int count = 0;
			try {
				CloudSite cloudsite = this.getCloudSiteById(siteId);
				cloudsite.setSiteType(siteType);
				cloudsite.setUser_id(cloudsite.getUser_id());
				cloudsite.setSite_name(siteName);
				cloudsite.setAddress(address);
				cloudsite.setSiteNum(Integer.valueOf(siteNum));
				cloudsite.setAllow_client_num(zd_num);
				cloudsite.setIs_probative(sy_time);
				cloudsite.setState(state);
				cloudsite.setBannerUrl(cloudsite.getBannerUrl());//插入bannerUrl
				cloudsite.setExTime((exTime==1?3600:exTime*60));//更该用户过期重复认证时间
				count = nutDao.update(cloudsite);
				this.updateExTime((exTime==1?3600:exTime*60), siteId);
			} catch (Exception e) {
				log.error("更新场所信息失败");
			} 
			return count;
		}
		 
		/**
		 * 查询当前登录用户下是否还有未绑定设备的场所 
		 * @param userId
		 * @return
		 */
		public int querySiteConfigBySiteIdAndUserID(int userId){
			String sql="select count(site.id) from t_cloud_site site where site.user_id="+userId+" and site.id not in(select routers.site_id from t_cloud_site_routers routers)";
			int count=jdbcTemplate.queryForInt(sql);
			return count;
		}
	
		
		/**
		 * 获取用户名下的所有场所
		 * @param userId
		 * @param curPage
		 * @param pageSize
		 * @return
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public AjaxPageBean getUserSiteListInfo(int userId,int curPage,int pageSize){
			AjaxPageBean ab=null;
			StringBuilder sbd = new StringBuilder();
			sbd.append(" select m.id,max(m.site_name)site_name ,m.state,max(m.address)address, ");
			sbd.append(" max(m.is_probative)is_probative,max(m.allow_client_num)allow_client_num,max(m.create_time)create_time,count(m.mac)mac_num,group_concat(m.mac separator ',')macs,m.site_type,m.siteNum,m.exTime from ( ");
			sbd.append(" select a.id,a.site_name,a.address,a.is_probative,a.allow_client_num,a.state,a.create_time,b.mac,a.site_type,a.siteNum,a.exTime from t_cloud_site a LEFT JOIN t_cloud_site_routers b on a.id=b.site_id ");
			sbd.append(" where a.user_id ="+userId+" order by a.create_time desc ) m GROUP BY m.id ORDER BY m.create_time desc");
			String sql = sbd.toString();
			try {
		        ab=PagingFactory.getPageNationResultList(jdbcTemplate,new BeanPropertyRowMapper<SiteInfoBean>(SiteInfoBean.class), sql, curPage, pageSize);
		        StringBuffer sb=new StringBuffer();
		        for(int i=0;i<ab.getData().size();i++){
		        	SiteInfoBean sib=(SiteInfoBean) ab.getData().get(i);
		        	sb.append(sib.getId()).append(",");
		        }
		        
		        if(sb.length()>0){
			        String in=sb.substring(0, sb.length()-1);
			        String portalNumSQL="select a.site_id,count(DISTINCT a.portal_user_id) p_num from t_site_income a where a.site_id in ("+in+") group by a.site_id ";
			        List<Map<String, Object>> result= jdbcTemplate.queryForList(portalNumSQL);
			        if(result.size()>0){
			        	for(int i=0;i<ab.getData().size();i++){
				        	SiteInfoBean sib=(SiteInfoBean) ab.getData().get(i);
				        	for(int j=0;j<result.size();j++){
				        		int n=Integer.parseInt(result.get(j).get("site_id")+"");
				        		if(sib.getId()==n){
				        			sib.setPortalUserNum(Long.parseLong((result.get(j).get("p_num")==null?0:result.get(j).get("p_num"))+""));
				        		}
				        	}
				        }
			        }
			        List<SiteInfoBean> list = new ArrayList();
			        for(int i=0;i<ab.getData().size();i++){
			        	SiteInfoBean sib=(SiteInfoBean) ab.getData().get(i);
			        	//获取在线人数
			        	sib.setCreate_time(getOnlineNum((sib.getId()+"").trim())+"");
			        	list.add(sib);
					}
			        ab.setData(list);
		        }
		        return ab;
			}catch (Exception e) {
				log.error(this.getClass().getCanonicalName(), e);	        
		        return ab;
		    }
		}
		
		/**
		 * 绑定设备 
		 * 重载 by:cuimiao
		 * 该方法向t_cloud_site_routers表和radgroupreply表中插入数据
		 * @param userId
		 * @param mac
		 * @return
		 */
		public boolean bindDeviceToUser(CloudSiteRouters router,String maxUpSpeed ,String maxDownSpeed){
			boolean flag = false;
			
			//RouterOs的速度是 为100时，存1M以此类推
			if("ros".equals(router.getRouterType())){
				maxUpSpeed = ((Integer.parseInt(maxUpSpeed)/100)!=0?(Integer.parseInt(maxUpSpeed)/100):1)+"M";
				maxDownSpeed = ((Integer.parseInt(maxDownSpeed)/100)!=0?(Integer.parseInt(maxDownSpeed)/100):1)+"M";
			}
			
			//插入操作：t_cloud_site_routers
			CloudSiteRouters ur=nutDao.fetch(CloudSiteRouters.class, Cnd.where("siteId", "=", router.getSiteId()).and("dfid", "=", router.getDfid()));
			if(ur==null){
				flag = true;
				ur=new CloudSiteRouters();
				ur.setSiteId(router.getSiteId());
				ur.setMac(router.getMac().toUpperCase());
				ur.setIp(router.getIp());
				ur.setRouterType(router.getRouterType());
				ur.setSecretKey(router.getSecretKey());
				ur.setDfid(router.getDfid());
				ur.setInstallPosition(router.getInstallPosition());
				ur.setLastTime(new Date());
				ur=nutDao.insert(ur);
			}
			
			if(flag && !"wifidog".equals(router.getRouterType()) && !"ros".equals(router.getRouterType())){
				
				//小辣椒上传速度以bit计算，所以在maxUpSpeed和maxDownSpeed
				if("coovachilli".equals(router.getRouterType())){
					maxUpSpeed = (Integer.parseInt(maxUpSpeed)*8)+"";
					maxDownSpeed = (Integer.parseInt(maxDownSpeed)*8)+"";
				}
				
				/**操作radgroupreply进行限速配置*/
				//查询上传 t2_router_type的router_attribute，router_op
				StringBuffer sqlSelectUp = new StringBuffer();
				sqlSelectUp.append(" SELECT ");
				sqlSelectUp.append(" router_attribute,router_op");
				sqlSelectUp.append(" FROM t2_router_type");
				sqlSelectUp.append(" where router_type = ? ");
				sqlSelectUp.append(" and router_attribute_name = 'max_up_speed'");
				List<Map<String, Object>> mapUp = new ArrayList<Map<String, Object>>();
				try{
					mapUp = jdbcTemplate.queryForList(sqlSelectUp.toString(), new Object[]{router.getRouterType()});
				}catch(Exception e){
					e.printStackTrace();
				}
				//查询下载 t2_router_type的router_attribute，router_op
				if(!"ros".equals(router.getRouterType())){
					StringBuffer sqlSelectDown = new StringBuffer();
					sqlSelectDown.append(" SELECT ");
					sqlSelectDown.append(" router_attribute,router_op");
					sqlSelectDown.append(" FROM t2_router_type");
					sqlSelectDown.append(" where router_type = ? ");
					sqlSelectDown.append(" and router_attribute_name = 'max_down_speed'");
					List<Map<String, Object>> mapDown = new ArrayList<Map<String, Object>>();
					try{
						mapDown = jdbcTemplate.queryForList(sqlSelectDown.toString(), new Object[]{router.getRouterType()});
					}catch(Exception e){
						e.printStackTrace();
					}
					
					//插入下载：radgroupreply 隐患：没有事务处理
					StringBuffer sqlInsertDown = new StringBuffer();
					sqlInsertDown.append(" insert into ");
					sqlInsertDown.append(" radgroupreply(groupname,attribute,op,value)");
					sqlInsertDown.append(" values(?,?,?,?)");
					try{
						jdbcTemplate.update(sqlInsertDown.toString(),
								new Object[]{router.getDfid(),mapDown.get(0).get("router_attribute"),mapDown.get(0).get("router_op"),maxDownSpeed});
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				//插入上传：radgroupreply 隐患：没有事务处理
				StringBuffer sqlInsertUp = new StringBuffer();
				sqlInsertUp.append(" insert into ");
				sqlInsertUp.append(" radgroupreply(groupname,attribute,op,value)");
				sqlInsertUp.append(" values(?,?,?,?)");
				try{
					jdbcTemplate.update(sqlInsertUp.toString(),
							new Object[]{router.getDfid(),mapUp.get(0).get("router_attribute"),mapUp.get(0).get("router_op"),maxUpSpeed});
				}catch(Exception e){
					e.printStackTrace();
				}
				//插入下载：radgroupreply
				StringBuffer sqlInsertCoova = new StringBuffer();
				sqlInsertCoova.append(" insert into ");
				sqlInsertCoova.append(" radgroupreply(groupname,attribute,op,value)");
				sqlInsertCoova.append(" values(?,?,?,?)");
				try{
					jdbcTemplate.update(sqlInsertCoova.toString(),
							new Object[]{router.getDfid(),"Idle-Timeout",":=",this.getExpirationTime(router.getSiteId())});
				}catch(Exception e){
					e.printStackTrace();
				}
				
				//除wifidog 向radgroupcheck插入一条数据  Simultaneous-Use
				StringBuffer sqlInsertUse = new StringBuffer();
				sqlInsertUse.append(" insert into ");
				sqlInsertUse.append(" radgroupcheck(groupname,attribute,op,value)");
				sqlInsertUse.append(" values(?,?,?,?)");
				try{
					jdbcTemplate.update(sqlInsertUse.toString(),
							new Object[]{router.getDfid(),"Simultaneous-Use",":=","1"});
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			if("ros".equals(router.getRouterType())){
				//插入下载：radgroupreply
				StringBuffer sqlInsertCoova = new StringBuffer();
				sqlInsertCoova.append(" insert into ");
				sqlInsertCoova.append(" radgroupreply(groupname,attribute,op,value)");
				sqlInsertCoova.append(" values(?,?,?,?)");
				try{
					//TODO
					jdbcTemplate.update(sqlInsertCoova.toString(),
							new Object[]{router.getDfid(),"Mikrotik-Rate-Limit",":=",maxUpSpeed+"/"+maxDownSpeed});
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			return flag;//绑定路由操作成功视为该操作执行成功  
		}
		
		/**
		 * @Description  获得用户过期时间,避免重复认证默认600s
		 * @date 2016年10月12日上午11:16:41
		 * @author guoyingjie
		 * @param siteId
		 */
		public int getExpirationTime(int siteId){
			int exTime = 600;
			try {
				CloudSite site = nutDao.fetch(CloudSite.class,Cnd.where("id","=",siteId));
				exTime = site.getExTime();
			} catch (Exception e) {
				log.error(this.getClass().getCanonicalName());
			}
			return exTime;
		}
		/**
		 * 
		 * @Description  更改用户过期时间,避免重复认证默认600s
		 * @date 2016年10月12日上午11:50:36
		 * @author guoyingjie
		 * @param value
		 * @param siteId
		 */
		public void updateExTime(int value ,int siteId){
			try {
				String sql = "UPDATE radgroupreply SET value=? WHERE groupname IN (SELECT dfid FROM t_cloud_site_routers where site_id = ?) AND attribute = 'Idle-Timeout'";
				jdbcTemplate.update(sql,new Object[]{value,siteId});
			} catch (Exception e) {
				log.error(this.getClass().getCanonicalName());
			}
		}
		
		/**
		 * @Description 更改绑定设备 
		 * @date 2016年9月2日上午9:44:03
		 * @author guoyingjie
		 * @param router
		 * @param maxUpSpeed--上传速度   (wifidog不限速)
		 * @param maxDownSpeed--下载速度
		 * @return
		 */
		public boolean updateDeviceToUser(final CloudSiteRouters router, final String maxUpSpeed,final String maxDownSpeed){
			try {
		        Trans.exec(new Atom(){
		            public void run() {
		    			nutDao.update(router); 
		    			//RouterOs的速度是 为100时，存1M以此类推
		    			if("ros".equals(router.getRouterType())){
		    				String maxUpSpeeds = ((Integer.parseInt(maxUpSpeed)/100)!=0?(Integer.parseInt(maxUpSpeed)/100):1)+"M";
		    				String maxDownSpeeds = ((Integer.parseInt(maxDownSpeed)/100)!=0?(Integer.parseInt(maxDownSpeed)/100):1)+"M";
		    				String sql = "UPDATE radgroupreply SET value = ? WHERE groupname = ? AND attribute = 'Mikrotik-Rate-Limit'";
		    				jdbcTemplate.update(sql,new Object[]{maxUpSpeeds+"/"+maxDownSpeeds,router.getDfid()});
		    			}
		    			//小辣椒上传速度以bit计算，所以在maxUpSpeed和maxDownSpeed
		    			if("coovachilli".equals(router.getRouterType())){
		    				String maxUpSpeeds = (Integer.parseInt(maxUpSpeed)*8)+"";
		    				String maxDownSpeeds = (Integer.parseInt(maxDownSpeed)*8)+"";
		    				String upSpeed = "UPDATE radgroupreply SET value = ? WHERE groupname = ? AND attribute = 'ChilliSpot-Bandwidth-Max-Up'";
		    				String downSpeed = "UPDATE radgroupreply SET value = ? WHERE groupname = ? AND attribute = 'ChilliSpot-Bandwidth-Max-Down'";
	    					jdbcTemplate.update(upSpeed,new Object[]{maxUpSpeeds,router.getDfid()});
	    					jdbcTemplate.update(downSpeed,new Object[]{maxDownSpeeds,router.getDfid()});
		    			}
		    			//ikuai的限速单位为kb,前端穿过来就是kb单位
		    			if("ikuai".equals(router.getRouterType())){
		    				String upSpeed = "UPDATE radgroupreply SET value = ? WHERE groupname = ? AND attribute = 'RP-Upstream-Speed-Limit'";
		    				String downSpeed = "UPDATE radgroupreply SET value = ? WHERE groupname = ? AND attribute = 'RP-Downstream-Speed-Limit'";
	    					jdbcTemplate.update(upSpeed,new Object[]{maxUpSpeed,router.getDfid()});
	    					jdbcTemplate.update(downSpeed,new Object[]{maxDownSpeed,router.getDfid()});
		    			}
		            }
		        });
		        return true;
	        }catch (Exception e) {
	        	log.error("updateDeviceToUser 事务报错--", e);
	        	return false;
	        }
		}
		
		/**
		 * @Description 返回设备信息
		 * @date 2016年9月1日上午10:38:26
		 * @author guoyingjie
		 * @param pageNumber
		 * @param pageSize
		 * @return
		 */
		public List<CloudRouterBean> getDeviceList(int siteId,int pageNumber, int pageSize){
			List<CloudRouterBean> routers = new LinkedList<CloudRouterBean>();
			pageNumber=(pageNumber-1)<0?0:(pageNumber-1);
			pageNumber = pageNumber*pageSize;
			try {
				String sql = "SELECT * FROM t_cloud_site_routers where site_id =? ORDER BY create_time DESC  LIMIT ?,?";
				List<CloudSiteRouters> list = jdbcTemplate.query(sql,new Object[]{siteId,pageNumber,pageSize},
						new BeanPropertyRowMapper(CloudSiteRouters.class));
				if(list.size()>0&&list!=null&&list.get(0)!=null){
					for (int i = 0; i < list.size(); i++) {
						CloudSiteRouters cst = list.get(i);
						CloudRouterBean bean = new CloudRouterBean();
						bean.setId(cst.getId());
						bean.setSiteId(cst.getSiteId());
						bean.setNasid(cst.getDfid());
						bean.setDeviceType(cst.getRouterType());
						bean.setPosition(cst.getInstallPosition());
						bean.setSecretKey(cst.getSecretKey());
						bean.setCpu(cst.getCpuRate()==null?"0%":cst.getCpuRate()+"%");
						String runTime = cst.getRunTime();
						if(runTime!=null){
							String[] str = runTime.split(":");
							runTime = str[0]+"小时"+str[1]+"分";
						}else{
							runTime  = "未知";
						}
						bean.setRunTime(runTime);
						Date lastTime = cst.getLastTime();
						if(lastTime!=null){
							if(new Date().getTime()-lastTime.getTime()>4*60*1000){//大于4倍心跳异常
								bean.setState(1);//1--异常
							}else{
								bean.setState(0);
							}
						}
						String[] str = this.getUpAndDown(cst.getDfid(),cst.getRouterType());
						bean.setMaxup(str[0]);
						bean.setMaxdown(str[1]);
						routers.add(bean);
					}
				}
			} catch (Exception e) {
				log.error("返回设备信息");
			}
			return routers;
		}
		 /**
		  * @Description 获取场所设备总页数
		  * @date 2016年9月1日上午11:57:34
		  * @author guoyingjie
		  * @param siteId
		  * @return
		  */
		public int deviceTotalPage(int siteId,int pageSize){
			int totalPage = 0;
			try {
				String sql = "SELECT COUNT(id) FROM t_cloud_site_routers where site_id = ?";
				long total = jdbcTemplate.queryForObject(sql, new Object[]{siteId},Long.class);
				int totalCount = Integer.valueOf((total + "").trim());
				totalPage = (totalCount % pageSize) > 0 ? (totalCount / pageSize + 1): totalCount / pageSize;
			} catch (Exception e) {
				log.error("返回设备信息总页数异常");
			}
			return totalPage;
		}
	 
		/**
		 * 获得场所列表总页数
		 * @param pageSize
		 * @param userId
		 * @return
		 */
		public int getTotalPage(int pageSize, int userId) {
			int totalPageNum = 0;
			try {
				String sql = "SELECT COUNT(*) from (select m.id,max(m.site_name)site_name ,max(m.address)address, "
						+ "max(m.is_probative)is_probative,max(m.allow_client_num)allow_client_num,max(m.create_time)create_time,count(m.mac)mac_num,group_concat(m.mac separator ',')macs from ( "
						+ "select a.id,a.site_name,a.address,a.is_probative,a.allow_client_num,a.create_time,b.mac from t_cloud_site a LEFT JOIN t_cloud_site_routers b on a.id=b.site_id "
						+ "where a.user_id="
						+ userId
						+ " order by a.create_time desc "
						+ ") m GROUP BY m.id) k";
				long count = jdbcTemplate.queryForObject(sql, Long.class);
				int totalCount = Integer.valueOf((count + "").trim());
				totalPageNum = (totalCount % pageSize) > 0 ? (totalCount / pageSize + 1): totalCount / pageSize;
			} catch (Exception e) {
				totalPageNum=0;
				log.error("获取场所总页数异常");
			}
			return totalPageNum;
		}
		    
		/**
		 * 获得在线人数
		 * @param siteId
		 * @return
		 */
		@SuppressWarnings("deprecation")
		public int getOnlineNum(String siteId){
			Integer onlineCoova = 0;
			StringBuffer sqlCoova = new StringBuffer();
			sqlCoova.append("  select count(1) from radacct");
			sqlCoova.append("  where ");
			sqlCoova.append("  dfid in (SELECT dfid FROM t_cloud_site_routers WHERE site_id=?) ");
			sqlCoova.append("  AND acctstoptime IS NULL");
			onlineCoova = jdbcTemplate.queryForInt(sqlCoova.toString(), new Object[]{siteId});
			return onlineCoova;
		}
		
		
		/**
		 * 获得在nasid
		 * @param siteId
		 * @return
		 */
		public List<Map<String,Object>> getNasidList(){
			String sql = "SELECT dfid FROM t_cloud_site_routers";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
			return list;
		}
		
		/**
		 * 
		 * @Description: 获取该场所下所有路由
		 * @param siteId
		 * @return
		 * @Date		2016年6月18日 下午6:17:40
		 * @Author		cuimiao
		 */
		public List<Map<String,Object>> getRouterList(String siteId){
			String sql = "SELECT id,secret_key FROM t_cloud_site_routers where site_id = ?";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(sql,new Object[]{ siteId });
			return list;
		}
		 
		/**
		 * @Description 删除设备异常的设备
		 * @date 2016年9月1日下午4:57:01
		 * @author guoyingjie
		 * @param dfid
		 * @param id===设备id
		 * @return
		 */
		public boolean deleteErrorDevice(final String dfid,final int id){
			try {
		        Trans.exec(new Atom(){
		            public void run() {
		            	String sql ="DELETE FROM t_cloud_site_routers WHERE dfid = ? AND id = ?";
		    			jdbcTemplate.update(sql,new Object[]{dfid,id});
		    			
		    			//删除设备相关配置（4.1.1版主要针对于爱快和小辣椒的配置）
		    			//通用配置（小辣椒和爱快）
		    			String sqlCommon = "delete from radgroupreply where groupname = ?";
		    			jdbcTemplate.update(sqlCommon,new Object[]{dfid});
		    			//小辣椒配置
		    			String sqlCoova = "delete from radgroupcheck where groupname = ?";
		    			jdbcTemplate.update(sqlCoova,new Object[]{dfid});
		            }
		        });
		        return true;
	        }catch (Exception e) {
	        	log.error("删除设备异常的设备 事务报错--", e);
	        	return false;
	        }
		}
	    
		/**
		 * 根据id查询场所是否存在
		 * @param id
		 * @return
		 */
		public CloudSite getCloudSiteById(int id){
			CloudSite c=nutDao.fetch(CloudSite.class,Cnd.where("id","=",id));
			return c;
		}
		   
   /**
    * @Description 将传过来的base64转化为图片流
    * @date 2016年5月24日上午9:06:45
    * @author guoyingjie
    * @param base64String
    * @return
    */
	public static InputStream getInputStream(String base64String){
		InputStream in = null;
		if(base64String!=null&&!"".equals(base64String)){
			String formateStr = base64String.substring(base64String.indexOf("base64")+7);
			BASE64Decoder decoder = new BASE64Decoder();
	        try {
	            // Base64解码
	            byte[] bytes = decoder.decodeBuffer(formateStr);
	            for (int i = 0; i < bytes.length; ++i) {
	                if (bytes[i] < 0) {// 调整异常数据
	                    bytes[i] += 256;
	                }
	            }
	            in = new ByteArrayInputStream(bytes);
	        } catch (Exception e) {
	        	log.error("获得图片流失败",e);
	        }
		} 
		return in;
	}
	/**
	 * 生成十二位随机数
	 */
	public static String randCode() {
		String code = "";
		for (int i = 0; i < 3; i++) {
			Random rand = new Random();
			code += rand.nextInt(9);
		}
		return code.trim()+new Date().getTime();
	}
	/**
	 * 
	 *	@Description:用户登陆表更换名称
	 *  @author songyanbiao
	 *	@Date 2016年6月14日
	 */
	public void updateLoginTable(){
		try {
				String tableName="radacct_"+CalendarUtil.yesteday();
				String sql="INSERT INTO "+tableName+" (SELECT * FROM radacct )";
				String delSql="DELETE FROM radacct WHERE acctstoptime IS NOT NULL AND DATE_FORMAT(acctstoptime,'%m-%d-%y') < DATE_FORMAT(NOW(),'%m-%d-%y') OR DATE_ADD(acctupdatetime,INTERVAL 20 MINUTE)<NOW()";
				if(updateTable(tableName)){
					jdbcTemplate.update(sql);
					jdbcTemplate.update(delSql);
					System.out.println("成功");
				}		
			
		} catch (Exception e) {
			log.error("用户登陆表更换名称出错",e);
		}
		
	}
	
	public boolean updateTable(String tableName){
		boolean flag=true;
		String table = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME='"
				+ tableName + "'";		
		String sql="CREATE TABLE "+tableName+"("
				+"`radacctid` bigint(21) NOT NULL AUTO_INCREMENT,"
				+"`terminal_device` text COMMENT '设备类型',"
				+"`acctsessionid` varchar(64) NOT NULL DEFAULT '',"
				+"`acctuniqueid` varchar(32) NOT NULL DEFAULT '',"
				+"`username` varchar(64) NOT NULL DEFAULT '',"
				+"`groupname` varchar(64) NOT NULL DEFAULT '',"
				+"`realm` varchar(64) DEFAULT '',"
				+"`nasipaddress` varchar(15) NOT NULL DEFAULT '',"
				+"`nasportid` varchar(15) DEFAULT NULL,"
				+"`nasporttype` varchar(32) DEFAULT NULL,"
				+"`acctstarttime` datetime DEFAULT NULL,"
				+"`acctupdatetime` datetime DEFAULT NULL,"
				+"`acctstoptime` datetime DEFAULT NULL,"
				+"`acctinterval` int(12) DEFAULT NULL,"
				+"`acctsessiontime` int(12) unsigned DEFAULT NULL,"
				+"`acctauthentic` varchar(32) DEFAULT NULL,"
				+"`connectinfo_start` varchar(50) DEFAULT NULL,"
				+"`connectinfo_stop` varchar(50) DEFAULT NULL,"
				+"`acctinputoctets` bigint(20) DEFAULT NULL,"
				+"`acctoutputoctets` bigint(20) DEFAULT NULL,"
				+"`calledstationid` varchar(50) NOT NULL DEFAULT '',"
				+"`callingstationid` varchar(50) NOT NULL DEFAULT '',"
				+"`acctterminatecause` varchar(32) NOT NULL DEFAULT '',"
				+"`servicetype` varchar(32) DEFAULT NULL,"
				+"`framedprotocol` varchar(32) DEFAULT NULL,"
				+"`framedipaddress` varchar(15) NOT NULL DEFAULT '',"
				+"`acctstartdelay` int(12) DEFAULT NULL,"
				+"`acctstopdelay` int(12) DEFAULT NULL,"
				+"`xascendsessionsvrkey` varchar(10) DEFAULT NULL,"
				+"`dfid` varchar(45) DEFAULT NULL,"
				+"PRIMARY KEY (`radacctid`),"
				+"UNIQUE KEY `acctuniqueid` (`acctuniqueid`),"
				+"KEY `username` (`username`),"
				+"KEY `framedipaddress` (`framedipaddress`),"
				+"KEY `acctsessionid` (`acctsessionid`),"
				+"KEY `acctsessiontime` (`acctsessiontime`),"
				+"KEY `acctstarttime` (`acctstarttime`),"
				+"KEY `acctinterval` (`acctinterval`),"
				+"KEY `acctstoptime` (`acctstoptime`),"
				+"KEY `nasipaddress` (`nasipaddress`)"
				+");";
		try {
			List<String> isTable = templJdbcTemplate.queryForList(table,
					String.class);
			if (isTable != null && isTable.size() != 0) {
				log.error("该表已存在");
				flag=false;
			}else{
				jdbcTemplate.update(sql);
			}
		} catch (Exception e) {
			log.error("修改用户登陆表时出错",e);
			flag=false;
		}
		
		return flag;
	}
	/**
	 * 获取Ros配置文件字符串
	 * @Description: 
	 * @return
	 * @Date		2016年6月27日 下午3:25:04
	 * @Author		cuimiao
	 */
	public String getConfigStrForRos(String wanPort , String lanPort , String nasid , String secret){
		StringBuffer sb = new StringBuffer();
		/**--------------配置文件-----------*/
		sb.append("/interface set "+wanPort+" name=\""+wanPort+"-gateway\";");
		sb.append("\r\n");// 换行符
		sb.append("/ip dhcp-client add interface="+wanPort+"-gateway disabled=no comment=\"default configuration\";");
		sb.append("\r\n");// 换行符
		sb.append("/interface bridge add name=bridge-local disabled=no auto-mac=yes protocol-mode=rstp;");
		sb.append("\r\n");// 换行符
		sb.append("/ip address add address=192.168.88.1/24 interface=bridge-local comment=\"default configuration\";");
		sb.append("\r\n");// 换行符
		sb.append("/ip pool add name=\"default-dhcp\" ranges=192.168.88.10-192.168.88.254;");
		sb.append("\r\n");// 换行符
		sb.append("/ip dhcp-server add name=default address-pool=\"default-dhcp\" interface=bridge-local lease-time=10m disabled=no;");
		sb.append("\r\n");// 换行符
		sb.append("/ip dhcp-server network add address=192.168.88.0/24 gateway=192.168.88.1 comment=\"default configuration\";");
		sb.append("\r\n");// 换行符
		sb.append("/ip dns set allow-remote-requests=yes");
		sb.append("\r\n");// 换行符
		sb.append("/ip dns static add name=router address=192.168.88.1");
		sb.append("\r\n");// 换行符
		sb.append("/ip dns static add name=alidns1 address=223.5.5.5");
		sb.append("\r\n");// 换行符
		sb.append("/ip dns static add name=alidns2 address=223.6.6.6");
		sb.append("\r\n");// 换行符
		sb.append("/ip firewall nat add chain=srcnat out-interface="+wanPort+"-gateway action=masquerade comment=\"default configuration\"");
		sb.append("\r\n");// 换行符
		sb.append("/ip neighbor discovery set [find name=\""+wanPort+"-gateway\"] discover=no");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot profile add hotspot-address=10.5.50.1 name=hsprof1");
		sb.append("\r\n");// 换行符
		sb.append("/ip pool add name=hs-pool-3 ranges=10.5.48.1-10.5.63.254");
		sb.append("\r\n");// 换行符
		sb.append("/interface bridge add name=\"HS_bridge\" disabled=no auto-mac=yes protocol-mode=rstp");
		sb.append("\r\n");// 换行符
		sb.append("/interface wireless set wlan1 mode=ap-bridge ssid=KDF_HOTSPOT default-forwarding=no disabled=no band=2ghz-b/g/n");
		sb.append("\r\n");// 换行符
		sb.append("/interface wireless set wlan2 mode=ap-bridge ssid=@KDF_HOTSPOT default-forwarding=no disabled=no band=5ghz-a/n/ac");
		sb.append("\r\n");// 换行符
		sb.append("/ip dhcp-server add address-pool=hs-pool-3 disabled=no interface=HS_bridge lease-time=1h name=dhcp1");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot add address-pool=hs-pool-3 disabled=no interface=HS_bridge name=hotspot1 profile=hsprof1");
		sb.append("\r\n");// 换行符
		sb.append("/ip address add address=10.5.50.1/20 comment=\"KDF network\" interface=HS_bridge network=10.5.48.0");
		sb.append("\r\n");// 换行符
		sb.append("/ip dhcp-server network add address=10.5.48.0/20 comment=\"hotspot network\" gateway=10.5.50.1");
		sb.append("\r\n");// 换行符
		sb.append("/ip firewall nat add action=masquerade chain=srcnat comment=\"masquerade hotspot network\" src-address=10.5.48.0/20");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot user add name=admin");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot profile set hsprof1 use-radius=yes");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot profile set hsprof1 login-by=http-pap,http-chap,https");
		sb.append("\r\n");// 换行符
		sb.append("/radius add service=hotspot address=101.200.151.129 secret="+secret+" timeout=3000ms");
		sb.append("\r\n");// 换行符
		sb.append("/radius add service=hotspot address=182.92.149.4 secret="+secret+" timeout=3000ms");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=eduportal4.solarsys.cn");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=edu1.solarsys.cn");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=edu.solarsys.cn");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=oss.kdfos.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=oss.solarsys.cn");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=m.jdpay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=ss.symcd.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=tongjissl.jdpay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=plus.jdpay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=static.jdpay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=img14.360buyimg.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=img11.360buyimg.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=img30.360buyimg.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=img13.360buyimg.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=img20.360buyimg.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=img12.360buyimg.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=m.wangyin.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=wappaygw.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=c.cnzz.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=as.alipayobjects.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=a.alipayobjects.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=bb.ahjem.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=sd.symcd.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=g.alicdn.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=ocsp2.globalsign.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=ynuf.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=ss.symcd.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=mclient.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=rds.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=i.alipayobjects.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=t.alipayobjects.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=mapi.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=unitradeadapter.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=excashier.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=assets.alicdn.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=kcart.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=log.mmstat.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=acjs.aliyun.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=omeo.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden add dst-host=unitradeprod.alipay.com");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=58.211.137.192");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=58.216.10.24");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=23.59.139.27");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=23.13.171.27");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=23.49.139.27");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=122.225.34.223/24");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=122.228.95.95/24");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=117.34.112.192/27");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=117.71.17.110/27");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=124.193.226.240/27");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=140.205.153.72/16");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=110.76.18.203/16");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=110.75.158.71/16");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot walled-garden ip add dst-address=202.108.250.240/24");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot profile set radius-interim-update=5m numbers=hsprof1");
		sb.append("\r\n");// 换行符
		sb.append("/ip firewall filter add action=accept chain=input disabled=no dst-port=8291 protocol=tcp place-before=0 comment=\"Allow WinBox from WAN\"");
		sb.append("\r\n");// 换行符
		sb.append("/system clock set time-zone-autodetect=no time-zone-name=manual");
		sb.append("\r\n");// 换行符
		sb.append("/system clock manual set time-zone=gmt dst-delta=+00:00");
		sb.append("\r\n");// 换行符
		sb.append("/system ntp client set enabled=yes server-dns-names=cn.pool.ntp.org");
		sb.append("\r\n");// 换行符
		sb.append("/system scheduler add interval=1h name=up on-event=\"/tool fetch keep-result=no mode=http address=edu1.solarsys.cn host=edu1.solarsys.cn src-path=(\\\"cloud/rh\\\\\\?mac=\\\".[/interface ethernet get 0 mac-address].\\\"&nasid=\\\".[/system identity get name].\\\"&os_date=Mikrotik&uptime=\\\".[/system clock get time].\\\"%20up%20\\\".[/system resource get uptime].\\\",%20load%20average:%20\\\".[/system resource get cpu-load].\\\"%25\\\")\" policy=ftp,reboot,read,write,policy,test,password,sniff,sensitive start-date=jan/01/1970 start-time=01:00:00");
		sb.append("\r\n");// 换行符
		sb.append("/ip hotspot user profile set default shared-users=5");
		sb.append("\r\n");// 换行符
		sb.append("/system identity set name="+nasid+"");
		sb.append("\r\n");// 换行符
		sb.append("/interface bridge port add bridge=HS_bridge interface="+lanPort+"");
		sb.append("\r\n");// 换行符
		sb.append(":if ([:len [/file find name=flash]] > 0) do={/ip hotspot profile set html-directory=/flash/hotspot;/tool fetch url=http://oss.solarsys.cn/firmware/mikrotik/login.html dst-path=/flash/hotspot/login.html mode=http;/tool fetch url=http://oss.solarsys.cn/firmware/mikrotik/alogin.html dst-path=/flash/hotspot/alogin.html mode=http;} else={/ip hotspot profile set html-directory=/hotspot;/tool fetch url=http://oss.solarsys.cn/firmware/mikrotik/login.html dst-path=/hotspot/login.html mode=http;/tool fetch url=http://oss.solarsys.cn/firmware/mikrotik/alogin.html dst-path=/hotspot/alogin.html mode=http;}");
		return sb.toString();
	}
	
	/**
	 * 将banner_url追加至数据库
	 * @Description: 
	 * @param bannerUrl
	 * @param siteId
	 * @return
	 * @Date		2016年6月29日 下午2:22:52
	 * @Author		cuimiao
	 */
	public int updateBanner(String bannerUrl,String siteId){
		//更新条数，当为0时表示更新失败
		int count = 0;
		//查询该场所下的bannerUrl
		StringBuffer sqlSelect = new StringBuffer();
		sqlSelect.append(" select banner_url");
		
		
		sqlSelect.append(" from t_cloud_site");
		sqlSelect.append(" where id = ?");
		
		String oldUrls = jdbcTemplate.queryForObject(sqlSelect.toString(), String.class, new Object[]{siteId});
		
		StringBuffer sqlUpdate = new StringBuffer();
		sqlUpdate.append(" update t_cloud_site ");
		sqlUpdate.append(" set banner_url = ? ");
		sqlUpdate.append(" where id = ?");
		//没有老url
		count = jdbcTemplate.update(sqlUpdate.toString(), new Object[]{bannerUrl,siteId});
		return count;
	}
	/**
	 * 获取bannerUrls
	 * @Description: 
	 * @param siteId
	 * @return
	 * @Date		2016年6月29日 下午5:48:46
	 * @Author		cuimiao
	 */
	public String getBannerUrls(String siteId){
		//查询该场所下的bannerUrl
		StringBuffer sqlSelect = new StringBuffer();
		sqlSelect.append(" select banner_url");
		sqlSelect.append(" from t_cloud_site");
		sqlSelect.append(" where id = ?");
		String urls = jdbcTemplate.queryForObject(sqlSelect.toString(), String.class, new Object[]{siteId});
		return urls;
	}
	/**
	 * @Description  检测dfid是否存在
	 * @date 2016年8月31日下午2:42:26
	 * @author guoyingjie
	 * @param nasid
	 * @return
	 */
	public boolean getNasid(String nasid){
		String sql = "SELECT dfid FROM t_cloud_site_routers where dfid=?";
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql,nasid);
		if(list.size()>0&&list!=null&&list.get(0).get("dfid")!=null&&!"".equals(list.get(0).get("dfid"))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @Description  dfid获得路由表
	 * @date 2016年8月31日下午2:42:26
	 * @author guoyingjie
	 * @param nasid
	 * @return
	 */
	public CloudSiteRouters getRouterByNasid(String nasid){
		CloudSiteRouters router =  null;
		try {
			router =  nutDao.fetch(CloudSiteRouters.class,Cnd.where("dfid","=", nasid));
		} catch (Exception e) {
			log.error("获得dfid获得路由表异常");
		}
		return router;
	}
	/**
	 * @Description  获得场所的上传与下载速度
	 * @date 2016年9月2日下午5:10:14
	 * @author guoyingjie
	 * @param nasid
	 * @param type
	 * @return
	 */
	public String[] getUpAndDown(String nasid,String type){
		String[] str = new String[2];
		if("ros".equals(type)){
			String sql = "SELECT value FROM  radgroupreply  WHERE groupname = ? AND attribute = 'Mikrotik-Rate-Limit'";
			String updown = jdbcTemplate.queryForObject(sql,new Object[]{nasid},String.class);
			if(updown!=null&&!"".equals(updown)){
				str[0] = updown.split("/")[0];
				str[1] = updown.split("/")[1];
			}
		}
		//小辣椒上传速度以bit计算，所以在maxUpSpeed和maxDownSpeed
		if("coovachilli".equals(type)){
			String upSpeed = "SELECT value FROM  radgroupreply  WHERE groupname = ? AND attribute = 'ChilliSpot-Bandwidth-Max-Up'";
			String downSpeed = "SELECT value FROM  radgroupreply  WHERE groupname = ? AND attribute = 'ChilliSpot-Bandwidth-Max-Down'";
			str[0] = jdbcTemplate.queryForObject(upSpeed,new Object[]{nasid},String.class)+"B";
			str[1] = jdbcTemplate.queryForObject(downSpeed,new Object[]{nasid},String.class)+"B";
		}
		//ikuai的限速单位为kb,前端穿过来就是kb单位
		if("ikuai".equals(type)){
			String upSpeed = "SELECT value FROM  radgroupreply  WHERE groupname = ? AND attribute = 'RP-Upstream-Speed-Limit'";
			String downSpeed = "SELECT value FROM  radgroupreply  WHERE groupname = ? AND attribute = 'RP-Downstream-Speed-Limit'";
			str[0] = jdbcTemplate.queryForObject(upSpeed,new Object[]{nasid},String.class)+"KB";
			str[1] = jdbcTemplate.queryForObject(downSpeed,new Object[]{nasid},String.class)+"KB";
		}
		return str;
	}
	 
}
