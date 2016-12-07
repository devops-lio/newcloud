/**
 * Created by Administrator on 2016/9/22.
 */
(function () {
	//getTotal();
	checkAgentName();
	//选择框
	  var seleType = $('.seleType');
      var costTypeUl = $('.costType > ul');
      var costTypeLi = $('.costType > ul > li');

      seleType.unbind('click')
      seleType.click(function(){
          var nn = seleType.index(this);
          costTypeUl.eq(nn).css('display','block');
          return false;
      });
      costTypeLi.unbind('click');
      costTypeLi.click(function(){
          var nnd = costTypeLi.index(this);
          console.log(nnd);
          var str = costTypeLi.eq(nnd).text();
          console.log(str);
          seleType.text(str);
          costTypeUl.css('display','none');

          console.log(seleType.text())
      });
      costTypeUl.unbind('click');
      costTypeUl.click(function(){
          return false;
      });
      /* 选择费用类型 */

      $('body').click(function(){
          costTypeUl.css('display','none');
      });
	// 退出按钮
	$('.menu > li.exit').click(function(){
		window.location.href=ctx+"/logOut";
	});

    //展开收起左侧菜单
    $('.module').click(function(){
        shouzhan();
    });
    $('.incline').click(function(){
        shouzhan();
    });
   
    //展开收起左侧菜单
    function shouzhan(){
        var str = $('#leftNav').attr('class');
        if(str=='on'){
            $('#leftNav').removeClass('on');
            $('.inBtn').removeClass('on');
            $('.cTitle').addClass('on');
            $('.content').addClass('on');
            $('.incline').addClass('on');
        }else{
            $('#leftNav').addClass('on');
            $('.inBtn').addClass('on');
            $('.cTitle').removeClass('on');
            $('.content').removeClass('on');
            $('.incline').removeClass('on');
        }
    }
    // 查询一个时间段内的总收入
    $('#incomebtn').click(function () {
    	var siteid = $('.seleType').attr('siteid');
    	if(siteid==undefined||siteid==''){
    		$(".win>span").html("请先选择场所");
			winHint();
    		return;
    	}else{
    		getBetweenTimeIncome(siteid);
    	}
    })
    
     $('#zhuanhua').click(function () {
    	var siteid = $('.seleType').attr('siteid');
    	/*if(siteid==undefined||siteid==''){*/
    		/*$(".win>span").html("请先选择场所");
			winHint();
    		return;*/
    	/*}else{*/
    		 var agentname = $('#agentname').val();
    		 var end = $("#endAts").val();
    		 var start = $("#startAts").val();
    		 if(agentname==""){
    			 $(".win>span").html("请输入代理商账号");
    				winHint();
    	    		return;
    		 }
    		 if(start==""||end==""){
    			 $(".win>span").html("请输入查询时间");
    				winHint();
    	    		return;
    		 }
    	/*}*/
    	getOtherBili(agentname,start,end)
    })
    
})();

function getTotaldate(){
	$('.sitelist>li').click(function(){
		var n = $('.sitelist>li').index(this);
		var siteid = $('.sitelist>li').eq(n).attr("siteid");
	    var sitename = $('.sitelist>li').eq(n).text();
	    $('.seleType').text(sitename);
	    $('.sitelist').css('display','none');
	    $('.seleType').attr('siteid',siteid);
		getTotal(siteid);
	});
}


var clearTime;
function checkAgentName(){
	$("#agentname").keydown(function(){
		clearTimeout(clearTime);
		clearTime = setTimeout(function(){
			var agentname = $.trim($("#agentname").val());
			var pattern = /^1[34578]\d{9}$/;
			if(pattern.test(agentname)){
				 getCloudSiteByName(agentname);
			}else{
				if(agentname.length==11){
					$(".win>span").html("请输入正确的手机号");
					winHint();
					return;
				}
			}
		},1000);
	});
}
/**
 * 根据代理商账号获得所有的场所
 * @param username
 */
function getCloudSiteByName(username){
	$('.sitelist>li').remove();
	$.ajax({
		type:'post',
		data:{
			username:username
		},
		url:ctx+"/SettlementRatio/getCloudSiteByName",
		success:function(data){
			eval("data="+data)
			if(data.code==200){
				var htmls = "";
				for (var i = 0; i < data.data.length; i++) {
					htmls+="<li siteid='"+data.data[i].id+"'>"+data.data[i].site_name+"</li>";
				}
				$('.sitelist').html(htmls);
				 getTotaldate();
			}else{
				$(".win>span").html("代理商暂未开通场所");
				winHint();
				return;
			}
		},
		error:function(){
			$(".win>span").html("网络繁忙请稍后");
			winHint();
		}
	});
}
function getTotal(siteid){
 $.ajax({
    	type:"post",
    	url:ctx+"/SettlementRatio/getYesTodincome",
    	data:{
    		siteId:siteid
    	},
    	success:function(data){
    		if(data=="loseSession"){
				 window.location.href=ctx+"/toLogin";
				 return;
			}	
    		 eval("data="+data)
			// 昨天收入
		    $('#yesterday').text(data.data.yesterday);
		    // 今天收入
		    $('#today').text(data.data.today);
		    // 总收入
		    $('#total').text(data.data.total);
    	}
    });
}


function getOtherBili(username,startTime,endTime){
	$.ajax({
    	type:"post",
    	url:ctx+"/SettlementRatio/getOtherBili",
    	data:{
    		username:username,
    		startTime:startTime,
    		endTime:endTime
    	},
    	success:function(data){
    		if(data=="loseSession"){
				 window.location.href=ctx+"/toLogin";
				 return;
			}	
    		 eval("data="+data)
    		 
		     $('.show-num').eq(0).text(data.data[1].uvbili);
    		 $('.show-num').eq(1).text(data.data[1].payBili);
    		 $('.show-num').eq(3).text(data.data[0].count);
    		 $('.show-num').eq(2).text(data.data[0].suma);
    	}
    });
}



function getBetweenTimeIncome(siteid){
	var end = $("#endAt").val();
	var start = $("#startAt").val();
	if(end==""||end==null){
		$(".win>span").html("终止时间不能为空");
		winHint();
		return;
	}
	
	$.ajax({
    	type:"post",
    	url:ctx+"/SettlementRatio/getTimeBetween",
    	data:{
    		startTime:start,
    		endTime:end,
    		siteId:siteid
    	},
    	success:function(data){
    		if(data=="loseSession"){
				 window.location.href=ctx+"/toLogin";
				 return;
			}	
    		 eval("data="+data)
             $('#total').text(data.data.income);
            
    	}
    });
}
function win(){
	$('.win').css('display','block').fadeOut(3000);
};
function winHint(){
	$('.win').css('display','block').fadeOut(2000);
};