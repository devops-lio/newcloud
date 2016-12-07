<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="java.util.Date"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssPath" value="${ctx}/allstyle/newstyle/css" />
<c:set var="jsPath" value="${ctx}/allstyle/newstyle/js" />
<c:set var="imgPath" value="${ctx}/allstyle/newstyle/img" />
<c:set var="upLoadPath" value="http://realnameauth.oss-cn-shanghai.aliyuncs.com/user_picture/"/>
<!DOCTYPE html>
<html>
<%
long getTimestamp=new Date().getTime(); //时间戳
%>
<script type="text/javascript">
	var ctx="${ctx}";
	var imgPath="${imgPath}";
	var upLoadPath="${upLoadPath}";
	var state = "${state}";
</script>
<head>
	<meta charset="utf-8">
	<title>后台管理</title>
	<link rel="stylesheet" type="text/css" href="${cssPath }/common.css">
	<link rel="stylesheet" type="text/css" href="${cssPath }/general.css">
	<link rel="stylesheet" type="text/css" href="${cssPath }/user_mana.css">
</head>
<body>
<div id="header">
	<div class="fl msg_title">
		<img src="${imgPath }/kdf.png" class="fl"/>
		<span><i>Wi-Fi 运营系统 &#149;</i>后台管理员 </span>
	</div>
	<div class="fr user_msg">
		<div class="user_phone tip"><img src="${imgPath }/new_cont_06.png" class="fl"/>${user.userName }
			<ul class="menu">
				<li class="personageCenter">个人中心</li>
				<li class="exit">退出</li>
			</ul>
		</div>
		<img src="${imgPath }/new_cont_09.png" class="fr msg_tip tip"/>
	</div>
</div>
<div id="leftNav" class="on">
	<span class="module tip"></span>
	<ul class="linkList">
		<li class="index tip"><span>运营概览</span><p>运营概览</p></li>
		<li class="place tip"><span>场所管理</span><p>场所管理</p></li>
		<li class="billing tip"><span>计费管理</span><p>计费管理</p></li>
		<li class="user tip on"><span>用户管理</span><p>用户管理</p></li>
		<li class="withdraw tip"><span>资金管理</span><p>资金管理</p></li>
		<li class="personal tip"><span>个人中心</span><p>个人中心</p></li>
	</ul>
	<p class="incline"></p>
	<!-- <p class="incline"><span class="fl"></span><span class="inBtn tip on"></span></p> -->
</div>
<div class="container">
	<p class="cTitle"><span>用户管理</span></p>
	<div class="content">
		<p class="cn_table"><span class="on">在线用户</span><span>注册用户</span><span>付费用户</span><span>注册未付费用户</span><span>流失用户</span><span>实名认证待处理</span></p>
		<div class="us_query">
			<div class="dv_query fn_select">按场所查询 
				<span>全部</span>
				<ul>
					<li class="on" value="">全部</li>
					<c:forEach var="p" items="${siteList }">
						<li value="${p.id}" class="siteChorses">${p.site_name }</li>
					</c:forEach>
				</ul>
			</div>
			<p class="us_nm_query"><input class="nameby" type="text" maxlength="11" placeholder="请输入手机号" name=""  onkeyup="this.value=this.value.replace(/\D/g,'')"><button class="qu_btn"></button></p>
			<button class="a_btn  addUserBtn">+ 新增用户</button>
		</div>
		<div class="l_us_query">
			<div class="dv_query fn_select">按场所查询 
				<span>全部</span>
				<ul>
					<li>全部</li>
						<c:forEach var="p" items="${siteList }">
						<li value="${p.id}" class="siteChorses">${p.site_name }</li>
					</c:forEach>
				</ul>
			</div>
			<p><input id="sTime" type="text" readonly="readonly" onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate"> 至 <input id="eTime" type="text" readonly="readonly" onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate"><button class="a_btn date_query">查询</button><button class="a_btn rebeat">重置</button></p>
		</div>
		<p class="l_num">流失总人数<span></span>(如果用户上网时间到期后，两周内没有续费则视为流失用户)<button class="a_btn big">导出Excel</button></p>
		<p class="s_num">剩余带审核人数<span></span>人<button class="a_btn  nowAuth">+ 现场认证</button></p>
		<div class="in_user_table">
			<p>
				<span class="t1">用户名</span>
				<span class="t2">实名认证</span>
				<span class="t3">最后一次充值金额</span>
				<span class="t4">总消费金额</span>
				<span class="t5">消费次数</span>
				<span class="t6">归属场所</span>
				<span class="t7">操作</span>
			</p>
			<ul class="ta_list">
				
			</ul>
		</div>
		<div class="user_table">
			<p>
				<span class="t1">用户名</span>
				<span class="t2">账户余量</span>
				<span class="t3">最后一次充值金额</span>
				<span class="t4">总消费金额</span>
				<span class="t5">消费次数</span>
				<span class="t6">归属场所</span>
				<span class="t7">操作</span>
			</p>
			<ul class="ta_list">
				
			</ul>
		</div>
		<div class="w_user_table">
			<p>
				<span class="t1">用户名</span>
				<span class="t2">实名认证</span>
				<span class="t7">归属场所</span>
				<!-- <span class="">操作</span> -->
			</p>
			<ul class="ta_list">
			</ul>
		</div>
		<div class="l_user_table">
			<p>
				<span class="t1">用户名</span>
				<span class="t3">到期时间</span>
				<span class="t4">最后一次充值金额</span>
				<span class="t5">总消费金额</span>
				<span class="t6">消费次数</span>
				<span class="t7">归属场所</span>
			</p>
			<ul class="ta_list">
			</ul>
		</div>
		<div class="s_user_table">
			<p>
				<span>用户名(手机号)</span>
				<span>姓名</span>
				<span>身份证号</span>
				<span>所属场所</span>
				<span>地址</span>
				<span>身份证正反面照片</span>
				<span>操作</span>
			</p>
			<ul class="ta_list">
				
			</ul>
		</div>
		<div class="pager">
			<span class="page_first bt">首页</span>
			<span class="page_pre bt">◀</span>
			<span class="page_cont"><i></i>/<i></i></span>
			<span class="page_next bt">▶</span>
			<span class="page_last bt">尾页</span>
			<input class="page_to" type="text" name="" onkeyup="this.value=this.value.replace(/\D/g,'')">
			<span class="skip bt">跳转</span>
		</div>
	</div>
</div>
<div class="mask"></div>
<div class="us_card">
	<span class="close tip"><img src="${imgPath }/close.png"></span>
	<div class="card1">
		身份证正面照
		<img src="${imgPath }/photo.png">
	</div>
	<div class="card2">
		身份证反面照
		<img src="${imgPath }/photo.png">
	</div>
</div>
<div class="pay">
	<p class="p_tit">充值<img class="tip gb_close" src="${imgPath }/gb.png"></p>
	<p class="pay_user">充值用户<span></span></p>
	<div class="dv_query fn_select payType">充值类型 
		<span id="p_type"></span>
		<ul>
		</ul>
	</div>
	<p class="pay_num">充值数量<input id="p_num" type="text" name=""  onkeyup="this.value=this.value.replace(/\D/g,'')" value="1"></p>
	<p class="pay_all">充值总额<span><i id="p_allNum"></i>元</span></p>
	<p class="btns"><button class="a_btn lj_pay">立即充值</button><button class="q_btn qx_pay">取消</button></p>
</div>
<div class="sb_bd">
	<p class="p_tit">现场认证<img class="tip gb_close" src="${imgPath }/gb.png"></p>
	<p class="bd_inp">绑定手机号<input id="x_phone" type="text" onkeyup="this.value=this.value.replace(/\D/g,'')" maxlength="11" name=""></p>
	<p class="bd_inp">&nbsp;&nbsp;&nbsp;真实姓名<input id="x_name" type="text" name=""></p>
	<p class="bd_inp">&nbsp;&nbsp;&nbsp;身份证号<input id="x_card" type="text" name=""></p>
	<p class="bd_inp">&nbsp;&nbsp;&nbsp;宿舍位置<input id="x_ads" type="text" name=""></p>
	<p class="btns"><button class="a_btn lj_bd">绑定</button><button class="q_btn qx_bd">取消</button></p>
</div>
<div class="add_user">
	<p class="p_tit">新增用户<img class="tip gb_close" src="${imgPath }/gb.png"></p>
	<p class="sex">&nbsp;&nbsp;&nbsp;性别<span id="sex"><i class="boy on">男</i><i class="girl">女</i></span></p>
	<p class="bd_inp">手机号<input id="cj_phone" type="text" maxlength="11" name=""></p>
	<p class="bd_inp">&nbsp;&nbsp;&nbsp;密码<input id="cj_pwd" type="text" name=""></p>
	<p class="btns"><button class="a_btn lj_cj">立即创建</button><button class="q_btn qx_cj">取消</button></p>
</div>
<div class="dhk">
	<p class="d_txt">对话文本</p>
	<p class="d_btns"><button class="a_btn dhqd_btn">确定</button><button class="q_btn dhqx_btn">取消</button></p>
</div>
<div class="imgShow">
		<div><img src="${imgPath}/shit.jpg"></div>
		<div class="marked"></div>
</div>
<script type="text/javascript" src="${ctx}/allstyle/newstyle/jquery-2.1.4.min.js"></script>
<script type="text/javascript" src="${ctx}/allstyle/newstyle/general.js"></script>
<script type="text/javascript" src="${jsPath}/userMange/user_mana.js"></script>
<script type="text/javascript" src="${ctx}/allstyle/newstyle/My97DatePicker/WdatePicker.js"></script>
</body>
</html>