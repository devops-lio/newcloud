
var x=90;
var time;
var state = 0;
$(function() {
	
	document.getElementById('checkbox').addEventListener('change',function () {
		var btn = $('#register');
        var isAgree = this.checked;
        if (!isAgree) {
        	btn.attr("disabled", false);
        	btn.css('background-color', '#57c6d4')
        	
        }
        else {
        	 btn.attr("disabled",true);
        	 btn.css('background-color', '#e5e5e5')
        }
    });
	
	
	$("#user_register_pwd").keyup(function(){
		var len = $("#user_register_pwd").val().length;
		var d = $('.pwdStrongDiv').eq(0).find('div');
		var text = $('.pwdStrong').eq(0);
		if (len>=6 && len <=9) {
			d.eq(0).css('background-color','#ff6142');
			d.eq(1).css('background-color','#e5e5e5');
			d.eq(2).css('background-color','#e5e5e5');
			text.text('弱');
			text.css('color','#ff6142');
			text.show();
		} else if (len>9 && len<=14) {
			d.eq(0).css('background-color','#ffd042');
			d.eq(1).css('background-color','#ffd042');
			d.eq(2).css('background-color','#e5e5e5');
			text.text('中');
			text.css('color','#ffd042');
			text.show();
		} else if (len>14) {
			d.eq(0).css('background-color','#00c340');
			d.eq(1).css('background-color','#00c340');
			d.eq(2).css('background-color','#00c340');
			text.text('强');
			text.css('color','#00c340');
			text.show();
		} else {
			d.eq(0).css('background-color','#e5e5e5');
			d.eq(1).css('background-color','#e5e5e5');
			d.eq(2).css('background-color','#e5e5e5');
			text.hide();
		}
	});
	
	
	/* --------------记住密码-------------------- */
	$('.rememberPWD').click(function(){
		var str=$('.rememberPWD>span').attr('class');
		if(str!='on'){
			$('.rememberPWD>span').attr('class','on');
		}else{
			$('.rememberPWD>span').attr('class','');
		}
	});
	/* ---------------------注册按钮事件---------------------------- */
	$('.fn-register').click(function(){
		$("#user_register_verify").val("");
		$("#user_register_pwd").val("");
		$("#user_register_name").val("");
		$("#agent_register_name").val("");
		forgOrReg(1);
		maskBlock();
	});
	/* -------------关闭按钮事件---------------- */
	$('.new>h2>i').click(function(){
		maskDisp(1);
	});
	/* -------------忘记密码------------ */
	$('.forget').click(function(){
		$("#user_forget_verify").val("");
		$("#user_forget_pwd").val("");
		$("#user_repeat_pwd").val("");
		$("#user_forget_name").val("");
		forgOrReg(0);
		maskBlock();
	});
	/* --------------获取验证码--------------- */
	
	/* ---------------是/否--------------- */
	$('.whether>button').click(function(){
		var n=$('.whether>button').index(this);
		whetherDisp(n);
	});
//----------------------------------------------注册start-------------------------------------//
	
	$("#register").click(function() {
			$("#register-form").submit();
	});
jQuery.validator.addMethod("phone", function(value, element) { 
    return this.optional(element) || /^(((13[0-9]{1})|(14[4-7]{1})|(15[0-9]{1})|(170)|(17[1-8]{1})|(18[0-9]{1}))+\d{8})$/.test(value);         
}, "请输入正确手机账号！"); 

var registerForm =$("#register-form").validate({
	
	errorPlacement : function(error, element) {
		$("#errorCode").html("");
		$(".error").html("");
			error.css({
				display : "inline",
				color : "red",
				position : "relative",
			}).appendTo(element.next().addClass("error"));
		},
	submitHandler : function(form) {
		var yzmNumber=$("#user_register_verify").val();
		var password=$("#user_register_pwd").val();
		var telephone=$("#user_register_name").val();
		var agent = $('#agent_register_name').val();
		$.ajax({
			type : "post",
			url : ctx+"/UserManage/userRegister",
			data : {
				telephone : telephone,
				password : password,
				yzmNumber : yzmNumber,
				agent:agent
			},
			success:function(data){
				eval("data = " + data);
				if(data.code==200){
						$('.win>span').html("注册成功！");
						maskDisp(0);
						var obj=$('.gain');
						clear(obj);
						setTimeout(function(){
							location.reload();
						}, 1000);
					}else if(data.code==201){
						$("#errorCode").html(data.msg);
				}
				
			},
			error:function(e){
			}
		});	
	},
	rules : {
		user_name : {
			required : true,
			rangelength:[11,11],
			phone:true,
			remote:{
			    url: ctx+"/backRegistCheckTel",     //后台处理程序
			    type: "post",               //数据发送方式
			    data: {                     //要传递的数据
			    	telephone: function() {
			            return $("#user_register_name").val();
			        }
			    }
			},
		},	
		agent_name:{
			required : true,
		},
		user_pwd : {
			required : true,
			rangelength:[4,16],
		},
	
		user_verify : {
			required : true,
			digits : true,
			rangelength:[4,4]
			}
		
	},
	messages : {
		user_name : {
			required : "请输入手机号",
			rangelength : "请输入正确的手机号！",
			phone : "请输入正确的手机号！",
			remote : "该手机号已经注册，请登录"
		},
		agent_name:{
			required : "请输入代理商",
		},
		user_pwd : {
			required : "请输入密码",
			rangelength:"用户密码为4~16位长度"
		},
		user_verify : {
			required : "请输入验证码",
			digits :"验证码是数字",
			rangelength:"验证码长度为4~4位"
		}
	}
});


	$("#registGain").click(function(){
		if(registerForm.element($("#user_register_name"))){
			$.ajax({
				type : "POST",
				url : ctx+"/TelCodeManage/sendTelCode",
				data : {
					tel : $("#user_register_name").val(),
					//content : ".欢迎您注册宽东方WI-FI计费管理系统，此码5分钟内有效，请勿转发他人。"
					templateCode:"SMS_12936361"
				},
				success : function(data) {
					if (data == -1) {
						return false;
					}
					if(data==-2){
						clearInterval(time);
						$('#registGain').html('重新获取验证码');
						$('#registGain').removeAttr("disabled");
						$('#registGain').css('background','#57c6d4');
						floatAlert(280,60,'请不要频繁发送验证码,谢谢您的配合与支持!',2500);
						return false;
					}
					var obj=$('#registGain');
					countDown(obj,90);
				}
			});
		}
		
		
	});
	
	//----------------------------------------------注册end-------------------------------------//
	//----------------------------------------------登录start-------------------------------------------//

	jQuery.validator.addMethod("phone", function(value, element) { 
	    return this.optional(element) || /^(((13[0-9]{1})|(14[4-7]{1})|(15[0-9]{1})|(170)|(17[1-8]{1})|(18[0-9]{1}))+\d{8})$/.test(value);         
	}, "请输入正确手机账号！"); 
	$("#loginForm").validate({
		// 错误提示样式,在下方提示
		errorPlacement : function(error, element) {
			//把错误提示清除
			$('.form-pwd>span').text("");
			error.css({
				display : "inline",
				color : "red",
				position : "relative",
			}).appendTo(element.next().addClass("error"));
		},
		submitHandler : function(form) {
			//验证用户名及密码是否正确
			$.ajax({
				type:'post',
				url:ctx +'/checkUser',
				data:{
					user_name:$("#user_login_name").val(),
					user_pwd:$("#user_login_pwd").val(),
					state : $('.rememberPWD>span').attr('class')=="on"?1:0
				},
				success:function(data){
					eval("data = " + data);
					if(data.state){
						//跳转至首页
						form.submit();
					}else{
						$('#errorText').text(data.errorText);
						//提醒用户账号密码错误
					}
				}
			});
		},
		// 校验规则
		rules : {
			user_name : {
				required : true,
				rangelength:[11,11],
				phone:true,
				remote:{
				    url: ctx+"/checkTel",     //后台处理程序
				    type: "post",               //数据发送方式
				    data: {                     //要传递的数据
				    	telephone: function() {
				            return $("#user_login_name").val();
				        }
				    }
				}
			},
			user_pwd : {
				required : true,
				rangelength:[4,16],
			}
		},
		// 提示文本
		messages : {
			user_name : {
				required : "请输入手机号",
				rangelength:"请输入正确的手机号！",
				phone:"请输入正确的手机号！",
				remote:"该手机号还未注册,请先注册!"
			},
			user_pwd : {
				required : "请输入密码",
				rangelength:"用户密码为4~16位长度"
			}
		}
	});
	$("#fn-login").click(function() {
		$(".error").html("");
		$("#loginForm").submit();
	});
	// ----------------------------------------------登录校验end-------------------------------------------//
	// ----------------------------------------------忘记密码start-------------------------------------//
	$("#retrievePassword").click(function() {
			$("#forget-form").submit();
	});

	$("#forget-form").validate({
						// 错误提示样式,在下方提示
						errorPlacement : function(error, element) {
							$("#errorMsg").html("");
								error.css({
									display : "inline",
									color : "red",
									position : "relative",
								}).appendTo(element.next().addClass("error"));
							},
						submitHandler : function(form) {
							$.ajax({
								type : "POST",
								url : ctx+"/UserManage/forgetPassword",
								data : {
									telephone :$("#user_forget_name").val(),
									password : $("#user_forget_pwd").val(),
									yzmNumber : $("#user_forget_verify").val()
								},
								success : function(data) {
									eval("data = " + data);
									if (data.code == 303) {
										$('.win>span').html("修改成功！");
										maskDisp(0);
										var obj=$('.gain');
										clear(obj);
										setTimeout(function(){
											location.reload();
										}, 1000);
									}else {
										$("#errorMsg").html(data.msg);
									}
									
								},
								error:function(data){
								}
							});
						},
//						 校验规则
						rules : {
							user_name : {
								required : true,
								rangelength:[11,11],
								phone:true,
								remote:{
								    url: ctx+"/checkTel",     //后台处理程序
								    type: "post",               //数据发送方式
								    data: {                     //要传递的数据
								    	telephone: function() {
								            return $.trim($("#user_forget_name").val());
								        }
								    }
								},
							},	
							user_pwd : {
								required : true,
								rangelength:[4,16],
							},
							user_repeat_pwd : {
								required : true,
								rangelength:[4,16],
								equalTo : "#user_forget_pwd"
							},
							user_verify : {
								required : true,
								digits : true,
								rangelength:[4,4]
								}
							
						},
						messages : {
							user_name : {
								required : "请输入手机号",
								rangelength:"请输入正确的手机号！",
								phone:"请输入正确的手机号！",
								remote:"该手机号没有注册"
							},
							user_pwd : {
								required : "请输入密码",
								rangelength:"用户密码为4~16位长度"
							},
							user_repeat_pwd : {
								required : "请输入密码",
								rangelength:"用户密码为4~16位长度",
								equalTo : "两次密码输入不一致",
							},
							user_verify : {
								required : "请输入验证码",
								digits :"验证码是数字",
								rangelength:"验证码长度为4~4位"
							}
						}
					});


	$("#forgetGain").click(function(){
		if(registerForm.element($("#user_forget_name"))){
			$.ajax({
				type : "POST",
				url : ctx+"/TelCodeManage/sendTelCode",
				data : {
					tel : $("#user_forget_name").val(),
					//content : "您正在修改宽东方Wi-Fi计费管理系统登录密码，此码5分钟内有效，请勿转发他人,谢谢您的配合与支持!"
					templateCode:"SMS_12921441"	
				},
				success : function(msg) {
					if (msg == -1) {
						return false;
					}else if(msg==-2){
						clearInterval(time);
						$('.gain').html('重新获取验证码');
						$('.gain').removeAttr("disabled");
						$('.gain').css('background','#57c6d4');
						floatAlert(280,60,'请不要频繁发送验证码,谢谢您的配合与支持!',2500);
						return false;
					}else{
						var obj=$('.gain');
						countDown(obj,90);
					}
				},
				error:function(data){
				}
			});
		}
		
		// ----------------------------------------------忘记密码end-------------------------------------//
		
	});
	$('#user_login_pwd').keypress(function(event){
		var e = event || window.event || arguments.callee.caller.arguments[0];
	    
		if(e && e.keyCode==13){ 
			$(".error").html("");
			$("#loginForm").submit();
		}
	});
});

// 将用户存进cookie中
function setCookie() {

	var username = $("user_login_name").val();
	var password = $("user_login_pwd").val();
	var rememberMeFlag = $("#rememberMe").is(':checked');
	if (rememberMeFlag) {// 存cookie
		var Days = 30;
		var exp = new Date();
		exp.setTime(exp.getTime() + Days * 24 * 60 * 60 * 1000);
		document.cookie = "username=" + Base64.encode(username) + ";expires="
				+ exp.toGMTString();
		document.cookie = "userpwd=" + Base64.encode(password) + ";expires="
				+ exp.toGMTString();
	}
}
// 读取cookie
function readCookies() {

	var cookies = document.cookie.split("; ");
	for (var i = 0; i < cookies.length; i++) {
		var arr = cookies[i].split("=");
		if (arr[0] == "user_name") {
			var username = Base64.decode(arr[1]);
			$("input[name=user_name]").val(username);
		}
		if (arr[0] == "user_pwd") {
			var password = Base64.decode(arr[1]);
			$("input[name=user_pwd]").val(password);
		}
	}
}
//倒计时

function timer(){
	x=x-1;
	$(".set_icon4").val(x+"秒后重新发送");
	if(x<1){
		x=90;
		$(".set_icon4").val("获取手机验证码");
		$(".set_icon4").removeAttr("disabled");
		clearTimeout(mytimer);
	}
}
/* ---------------------方法----------------------- */
var forgOrReg=function(n){
	if(n!=0){
		$('.fn-forget').css('display','none');
		$('.register').css('display','block');
	}else{
		$('.fn-forget').css('display','block');
		$('.register').css('display','none');
	}
}
var maskBlock=function(){
	$('.mask').css('display','block');
	$('.newly').animate({left:'20%'},1000);
//	$('.btns').animate({left:'20%'},1000);
}
var maskDisp=function(n){
	if(n==0){
		$('.newly').animate({left:2000},1000);
		$('.btns').animate({left:2000},1000);
		win();
		setTimeout(function(){
			$('.mask').css('display','none');
		},500);
		$(".error").html("");
	}else{
		$('.whether').css('display','block');
		
	}
};
var whetherDisp=function(n){
	if(n==0){
		$('.whether').css('display','none');
		$('.newly').animate({left:2000},1000);
		$('.btns').animate({left:2000},1000);
		setTimeout(function(){
			$('.mask').css('display','none');
		},500);
		var obj=$('.gain');
		clear(obj);
		$(".error").html("");
		setTimeout(function(){
			location.reload();
		},500);
	}else{
		$('.whether').css('display','none');
	}
}
var win=function(){
	$('.win').css('display','block').fadeOut(2000);
}
var countDown=function(obj,n){
	obj.html('('+n+')秒后重新获取');
	obj.css('background','#ccc');
	time=setInterval(function(){
		n--;
		obj.html('('+n+')秒后重新获取');
		obj.attr({"disabled":"true"});
		if(n==0){
			clearInterval(time);
			obj.html('获取验证码');
			obj.removeAttr("disabled");
			obj.css('background','#57c6d4');
		}
	},1000);
};
var clear=function(obj){
	clearInterval(time);
	obj.html('获取验证码');
	obj.removeAttr("disabled");
	obj.css('background','#57c6d4');
};







