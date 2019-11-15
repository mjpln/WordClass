//全局ajax控制，用于session超时 提示  
//exjs
Ext.Ajax.on('requestcomplete',checkUserSessionStatus, this);   
function checkUserSessionStatus(conn,response,options){   
	var resText = response.responseText;
	if(resText=='ajaxSessionTimeOut'){     
		sessionTimeOut();  
		return false;
  }  
}  
//$(document).ajaxComplete(function(event,xhr,options){
//	
//});
//jquery
//$.ajaxSetup({  
//    cache: false, //close AJAX cache
//    //contentType:"application/x-www-form-urlencoded;charset=utf-8",   
//    complete:function(XHR,textStatus){     
//        var resText = XHR.responseText;  
//        if(resText=='ajaxSessionTimeOut'){     
//            sessionTimeOut();  
//        }  
//        else if(resText=='ajaxNoLimit'){     
//            noLimit();  
//        }        
//    }   
//}); 
function sessionTimeOut(){  
	alert('用户登录会话已过期，请重新登录！');  
	// Ext.MessageBox.alert('系统提示', '用户登录会话已过期，请重新登录！');
    setTimeout('window.top.location.href = "../login/login.jsp"', 1000);  
}  
  
function noLimit(){  
	// alert(无相应操作权限，请联系系统管理员！');  
	 Ext.MessageBox.alert('操作提示', '无相应操作权限，请联系系统管理员！');
} 




