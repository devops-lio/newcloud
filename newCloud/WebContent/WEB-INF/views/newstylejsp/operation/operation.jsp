<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="java.util.Date"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssPath" value="${ctx}/allstyle/newstyle/css" />
<c:set var="jsPath" value="${ctx}/allstyle/newstyle/js" />
<c:set var="imgPath" value="${ctx}/allstyle/newstyle/img" />
<!DOCTYPE html>
<html>
<%
	long getTimestamp = new Date().getTime(); //时间戳
%>
<head>
<meta charset="utf-8">
<title>后台管理</title>
<link rel="stylesheet" type="text/css" href="${cssPath}/common.css">
<link rel="stylesheet" type="text/css" href="${cssPath}/general.css">
<link rel="stylesheet" type="text/css" href="${cssPath}/index.css">
<style type="text/css">${ demo.css}
.ab_btn{
	min-width: 80px;
	height: 30px;
	border: none;
	background: #57c6d4;
	color: #fff;
	cursor: pointer;
}
</style>
<script type="text/javascript">
	var ctx = "${ctx}";
</script>
</head>
<body>
	<div id="header">
		<div class="fl msg_title">
			<img src="${imgPath}/kdf.png" class="fl" /><span><i>Wi-Fi运营系统 &#149;</i>后台管理员 </span>
		</div>
		<div class="fr user_msg">
			<div class="user_phone tip">
				<img src="${imgPath}/new_cont_06.png" class="fl" />${user.userName}
				<ul class="menu">
					<li class="personageCenter">个人中心</li>
					<li class="exit">退出</li>
				</ul>
			</div>
			<img src="${imgPath}/new_cont_09.png" class="fr msg_tip tip" />
		</div>
	</div>
	<div id="leftNav" class="on">
		<span class="module tip"></span>
		<ul class="linkList">
			<li class="index tip on" ><span>运营概览</span><p>运营概览</p></li>
			<li class="place tip" ><span>场所管理</span><p>场所管理</p></li>
			<li class="billing tip"  ><span>计费管理</span><p>计费管理</p></li>
			<li class="user tip" ><span>用户管理</span><p>用户管理</p></li>
			<li class="withdraw tip" ><span>资金管理</span><p>资金管理</p></li>
			<li class="personal tip"  ><span>个人中心</span><p>个人中心</p></li>
		</ul>
		<p class="incline"></p>
		<!-- <p class="incline"><span class="fl"></span><span class="inBtn tip on"></span></p> -->
	</div>
	<div class="container">
		<p class="cTitle">
			<span>运营概览</span>
		</p>
		<div class="content">
			<div class="device_info">
				<div class="dev_se fn_select">
					场所 <span class="chorsesite" siteid="all">全部</span>
					<ul class="allsite">
						<li>全部</li>
						<c:forEach var="p" items="${siteList}">
							<li value="${p.id}" title="${p.site_name}">${p.site_name}</li>
						</c:forEach>
					</ul>
				</div>
				<div class="info_txt all">
					<p class="user_num">
						人数：<span id="totalpeople">0</span><img class="wh_ts"
							src="${imgPath}/wh.png"><i class="float">该值表示您覆盖场所的总人数，即若
							您覆盖的是学校，该值为全校师生的 总人数。</i>
					</p>
					<p class="today_income">
						今日收益：<span id="todayincome">0.00元</span>
					</p>
					<p class="all_income">
						收入：<span id="totalincome">0.00元</span>
					</p>
				</div>
			</div>
			<div class="various">
				<div class="var_box">
				<div class="independent">
					<p>昨日独立用户总数</p>
					<span>0</span>
				</div>
			</div>
			<div class="var_box">
				<div class="register">
					<p>昨日登录用户数</p>
					<span>0</span>
				</div>
			</div>
			<div class="var_box">
				<div class="pay">
					<p>付费用户数</p>
					<span>0</span>
				</div>
			</div>
			<div class="var_box">
				<div class="yesterday">
					<p>注册用户数</p>
					<span>0</span>
				</div>
			</div>
			<div class="var_box">
				<div class="real_time">
					<p>实时用户数</p>
					<span>0</span>
				</div>
			</div>
			</div>
			<div class="annular_chart">
				<span>用户入网情况概览</span>
				<div class="both" title="当覆盖率达到或大于100%的时候表示一个用户使用多个终端导致">
					<p class="round" id="tryNOtTry">0%</p>
					<canvas id='perception' width='200' height='200'></canvas>
					<div class="perception">
						用户渗透率<img class="wh_ts" src="${imgPath}/ts.png">
						<p class="float">该值表示您覆盖场所用户对您部署无 线的知晓程度。(若全场所的人都知道
							，则为100%。若全不知道，则为0% 需要加大宣传力度)</p>
					</div>
				</div>
				<div class="both">
					<p class="round" id="reNotRe">0%</p>
					<canvas id='tryOut' width='200' height='200'></canvas>
					<div class="tryOut">
						新增注册转换率<img class="wh_ts" src="${imgPath}/ts.png">
						<p class="float">该值表示您覆盖场所用户对您网络速 度的满意程度。(越高表示网络体验越 让人满意)</p>
					</div>
				</div>
				<div class="both">
					<p class="round" id="payNotPay">0%</p>
					<canvas id='register' width='200' height='200'></canvas>
					<div class="registers">
						新增付费转换率<img class="wh_ts" src="${imgPath}	/ts.png">
						<p class="float">该值表示您覆盖场所用户对您网络速度及及格的满意程度。(如果指标过低则您需要优化网络稳定性、及提升网速，也有可能是该场所有竞争对手存在)</p>
					</div>
				</div>
			</div>
			<div class="userLineDraw">
				<p>
					起始时间<input type="text" readonly="readonly"
						onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate"
						id="userStartDate"> 至 <input id="userEndDate" type="text"
						readonly="readonly" onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})"
						class="Wdate">
					<button class="ab_btn" id="userGrow" attr="usergrow">查询</button>
				</p>
				<div id="userLineDraw" style="width: 100%;"></div>
			</div>
			<div class="pay_type">
				<span>缴费类型占比</span>
				<div id="pieChart" style="width: 400px; height: 300px; margin: 0 auto;"></div>
			</div>
			<div class="income">
				<div class="slsq">
					收入统计(元) <span class="swicth d" id="swicthdm"><i></i></span> <span>
					<input type="text" readonly="readonly"
						onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate"
						id="startTime"> 至 <input type="text" readonly="readonly"
						onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate"
						id="endTime">
						<button class="a_btn" id="dayquery">查询</button> <span>
				</div>
				<div id="histogram" style="width: 100%; margin-top: 10px;"></div>
			</div>
			<div class="user_add">
				<div class="t_tit">
					用户增长趋势<img class="wh_ts" src="${imgPath}/ts.png">
					<p class="float">用户增长趋势=总付费用户数-流失付 费用户数（用户过期后近两年内若无
						缴费行为视为流失用户，学校排除寒 暑假、工厂排除春节。）</p>
					<button class="a_btn big">流失用户详情</button>
				</div>
				<div id="lineChart" style="width: 100%; margin-top: 10px"></div>
			</div>
		</div>
	</div>
	<div class="barcontainer"><div class="meter"></div></div>
	<script type="text/javascript"
		src="${ctx}/allstyle/newstyle/jquery-2.1.4.min.js"></script>
	<script src="${jsPath}/operation/line/highcharts.js"></script>
	<script src="${jsPath}/operation/line/exporting.js"></script>
	<script type="text/javascript" src="${ctx}/allstyle/newstyle/general.js"></script>
	<script type="text/javascript" src="${jsPath}/operation/index.js"></script>
	<script type="text/javascript"
		src="${ctx}/allstyle/newstyle/My97DatePicker/WdatePicker.js"></script>
</body>
</html>