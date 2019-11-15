var args = {};
var abstractid;
var abs;
var wordid;

$(function() {
	getAllArgs();
	wordpatid = getArgs('wordpatid');
	wordpat = getArgs('wordpat');
	$("#wordpat").textbox('setValue', wordpat);
	getWordpatCity(wordpatid);

})
//查询词条地市信息
function getWordpatCity(wordpatid) {
	$.ajax( {
		type : "POST",
		url : "../wordpat.action",
		async : false,
		data : {
			type : "selectWordpatCity",
			wordpatids : wordpatid
		},
		success : function(data, textStatus, jqXHR) {
			var cityname = data.cityname;
			var citycode = data.citycode;
			$("#cityname").val(cityname);
		getCityTree(citycode);
	},

	error : function(jqXHR, textStatus, errorThrown) {
		$.messager.alert('系统异常', "请求数据失败!", "error");
	}
	});
}

//获得地市信息comboxtree
function getCityTree(citycode) {
	//	var city = "南京市,合肥市,江苏省,北京市";
	$('#selLocal').combotree( {
		url : '../getCityTree.action',
		editable : false,
		multiple : true,
		queryParams : {
			local : ""
		},
		   onLoadSuccess:function(node,data){  
			$('#selLocal').combotree('setValues', citycode);
	     }  
	});
}

function update() {
	var cityCode=[]
	    cityCode = $("#selLocal").combotree("getValues");
	var cityName = $('#selLocal').combotree('getText');
	if (cityCode.indexOf("全国") > -1) {
		cityCode = ["全国"];
		cityName="全国";
	}
	var dataStr = {
		type : "updateWordpatCity",
		wordpatids : wordpatid,
		citycode : cityCode.join('|')
	}
	//	$.ajax({
	//		type : "POST",
	//		url : "../worditem.action",
	//		async : false,
	////		data:{action:"updateWordCity", wordid:wordid,curwordclass:wordclass, citycode:cityCode, cityname:cityName},
	//		data:dataStr,
	//		success : function(data, textStatus, jqXHR) {
	//			 if(data.success){ 
	//				 $("#cityname").val(cityName); 
	//				 $.messager.alert('提示',data.msg , "info");
	//			 }else{
	//				 $.messager.alert('提示',data.msg , "info"); 
	//			 }
	//			
	//		},
	//				
	//		error : function(jqXHR, textStatus, errorThrown) {
	//			$.messager.alert('系统异常', "请求数据失败!", "error");
	//		}
	//	});

	$.post("../wordpat.action", dataStr, function(data) {
		if (data.success) {
			$("#cityname").val(cityName);
			$("#selLocal").combotree("setValues",cityCode);
			$.messager.alert('提示', data.msg, "info");
		} else {
			$.messager.alert('提示', data.msg, "info");
		}
	}, "json");

}

function getArgs(strParam) {
	return args[strParam];
}

function getAllArgs() {
	var q = location.href.indexOf('?');
	var query = location.href.substring(q + 1); // Get query string
	var pairs = query.split("&"); // Break at ampersand
	for ( var i = 0; i < pairs.length; i++) {
		var pos = pairs[i].indexOf('='); // Look for "name=value"
		if (pos == -1)
			continue; // If not found, skip
		var argname = pairs[i].substring(0, pos); // Extract the name
		var value = pairs[i].substring(pos + 1); // Extract the value
		value = decodeURIComponent(value); // Decode it, if needed
		args[argname] = value; // Store as a property
	}
}