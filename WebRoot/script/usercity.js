$(function(){  //页面加载时执行
	getUsercity();
});
//页面加载时获取用户权限地市
function getUsercity(){
	//获取地市
	$.ajax({
		type : "POST",
		url: '../worditem.action',
		dataType: "json",
		data:{action:"getCity"},
		success : function(data){
			if (data.success){
				usercity = data.citycode;
				usercityname = data.cityname;
				//Ext.getCmp('export1').show();
				/*Ext.getCmp("addusercity").setValue(usercityname);
				Ext.getCmp("addusercity").setSubmitValue(usercity);*/
//				Ext.getCmp("addusercity2").setValue(usercityname);
//				Ext.getCmp("addusercity2").setSubmitValue(usercity);
			}else{
				alert("获取用户权限地市失败！");
			}
		},
		error:function (XMLHttpRequest, textStatus, errorThrown) {
			alert("获取用户权限地市失败！");
		}
	});
}