package com.broadeast.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.broadeast.util.DateUtil;
import com.broadeast.util.MD5;
import com.broadeast.util.SHA256;

/**
 * @author pengxw E-mail:pengxianwei@kdfwifi.com
 * @version 创建时间：2016年7月28日 上午9:51:26
 * @describe
 */
@Service
public class InsertIkuaiInfo {
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void insert() throws IOException{
		System.out.println("导入开始");
		 String encoding="utf-8";
         File file=new File("C:\\Users\\Administrator\\Desktop\\pppuser (1).txt");
         if(file.isFile() && file.exists()){ //判断文件是否存在
             InputStreamReader read = new InputStreamReader(
             new FileInputStream(file),encoding);//考虑到编码格式
             BufferedReader bufferedReader = new BufferedReader(read);
             String lineTxt = null;
             String regExp = "^(((13[0-9]{1})|(14[4-7]{1})|(15[0-9]{1})|(170)|(17[1-8]{1})|(18[0-9]{1}))+\\d{8})$";  
             Pattern p = Pattern.compile(regExp);  
           int i=1;
             while((lineTxt = bufferedReader.readLine()) != null){
            	 String[] codes = lineTxt.split(" ");
            	 Matcher m = p.matcher(codes[3].split("=")[1]);  
            	 if(m.find()){
            		 //System.out.println(codes[3].split("=")[1]);
            		 System.out.println(codes[3].split("=").length>1?codes[3].split("=")[1]:null);
            		String pass = SHA256.getUserPassword(codes[3].split("=")[1], MD5.encode(codes[4].split("=")[1]).toLowerCase());
            		if(checkName(codes[3].split("=")[1])<1){
            		String sql="INSERT INTO t_portal_user(user_name,pass_word,state,user_nickname) VALUES('"+codes[3].split("=")[1]
            		+"','"+pass+"',0,'"+(codes[14].split("=").length>1?codes[14].split("=")[1]:"")+"')";
            		int insertu = jdbcTemplate.update(sql);
            		System.out.println(insertu>0?"添加成功！":"添加失败！");;
            		if(codes[5].split("=").length>1){
            		int userID =	getuserName(codes[3].split("=")[1]);
               			sql="INSERT INTO t_site_customer_info(expiration_time,site_id,portal_user_id)VALUES('"+codes[5].split("=")[1]
               	            		+"',427,"+userID+")";
               	       System.out.println(jdbcTemplate.update(sql)>0?"添加了一条计费":"计费添加失败");
              		 i++;
            		}
            		}
            	 }else if(codes[5].split("=").length>1){
            		 
            	 }
             }
             System.out.println(i);
             read.close();
		System.out.println("导入结束");
	}
	}
	private int checkName(String name){
		String sql = "select id from t_portal_user where user_name='"+name+"'";
		return jdbcTemplate.queryForList(sql).size();
	}
	private int getuserName(String name){
		String sql = "select id from t_portal_user where user_name='"+name+"'";
		return jdbcTemplate.queryForInt(sql);
	}
}
