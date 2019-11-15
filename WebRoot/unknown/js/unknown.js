var args = {};
var wordclass;
var word;
var wordid;
var synonym;

$(function() {
	getAllArgs();
	wordclass = getArgs('wordclass');
	word = getArgs('worditem');
	wordid = getArgs('wordid');
	synonym = getArgs('synonym');
	$("#worclassname").textbox('setValue', wordclass);
	$("#wordname").textbox('setValue', word);
	getWordCity(wordid, wordclass);
	$('#selLocal').combotree({
            //url: 'get_data.php',
            multiple:true,
            editable:false,
            onSelect:function(node) {//选中一行时触发
                  
            },
            onCheck:function(node, checked){//选中复选框时触发
            	var tree = $('#selLocal').combotree('tree');
                var root = tree.tree('getRoot');
                var children = tree.tree('getChildren',root);
                if(checked){
                	if(node.text=='全选'){
                		var ids = [];
                		for(var i=0;i<children.length;i++){
		                	var child = children[i];
		                	if(child.text!='全选'){
			                	if(child.text!='全国'){
			                		if(!child.checked){
			                			tree.tree('check',child.target);
			                		}
			                		ids.push(node.id);
			                	}else{
			                		if(child.checked){
			                			tree.tree('uncheck',child.target);
			                		}
			                	}
		                	}
	                	}
                	}else{
		                if(node.text=='全国'){
		                	for(var i=0;i<children.length;i++){
			                	var child = children[i];
			                	if(child.text!='全国'){
			                		if(child.checked){
			                			tree.tree('uncheck',child.target);
			                		}
			                	}
		                	}
		                }else{
		                	for(var i=0;i<children.length;i++){
			                	var child = children[i];
			                	if(child.text=='全国'){
			                		if(child.checked){
			                			tree.tree('uncheck',child.target);
			                		}
			                	}
		                	}
		                }
                	}
                }else{
                	if(node.text=='全选'){
                		for(var i=0;i<children.length;i++){
		                	var child = children[i];
	                		if(child.checked){
	                			tree.tree('uncheck',child.target);
	                		}
		                	
	                	}
                	}
                }
                //console.info(node);
                //console.info(checked);
            }
        });

})
// 查询词条地市信息
function getWordCity(wordid, wordclass) {
	$.ajax( {
		type : "POST",
		url : "../worditem.action",
		async : false,
		data : {
			action : "selectWordCity",
			wordid : wordid,
			curwordclass : wordclass
		},
		success : function(data, textStatus, jqXHR) {
			var citycodeu = data.usercity;
			var cityname = data.cityname;
			var citycode = data.citycode;
			$("#cityname").val(cityname);
			$("#cityname1").val(cityname);
			// alert(cityname);
		getCityTree(citycode, wordid,citycodeu);
	},

	error : function(jqXHR, textStatus, errorThrown) {
		$.messager.alert('系统异常', "请求数据失败!", "error");
	}
	});
}

// 获得地市信息comboxtree
function getCityTree(citycode, wordid,citycodeu) {
	// var city = "南京市,合肥市,江苏省,北京市";
	$('#selLocal').combotree( {
		url : '../getCityTree.action',
		editable : false,
		multiple : true,
		queryParams : {
			local : "",
			sign : wordid
		},
	   onLoadSuccess:function(node,data){  
	   		if(citycode.indexOf("全国")!=-1&&citycodeu.indexOf("全国")==-1){
	   			$('#selLocal').combotree('setValues', citycodeu);
	   		}else{
				$('#selLocal').combotree('setValues', citycode);
	   		}
       }  
	});
}

function update() {
	var cityCode = $("#selLocal").combotree("getValues");
	var cityName = $('#selLocal').combotree('getText');
	if(cityCode==null||cityCode==''){
		alert("地市不能为空！");
		return;
	}
	if (cityCode.indexOf("全国") > -1) {
		cityCode = "全国";
		cityName="全国";
	}

	var request = {
		action : "updateWordCity",
		wordid : wordid,
		curwordclass : wordclass,
		citycode : cityCode,
		cityname : cityName
	};

	var dataStr = {
		m_request : JSON.stringify(request)
	}
	// $.ajax({
	// type : "POST",
	// url : "../worditem.action",
	// async : false,
	// // data:{action:"updateWordCity", wordid:wordid,curwordclass:wordclass,
	// citycode:cityCode, cityname:cityName},
	// data:dataStr,
	// success : function(data, textStatus, jqXHR) {
	// if(data.success){
	// $("#cityname").val(cityName);
	// $.messager.alert('提示',data.msg , "info");
	// }else{
	// $.messager.alert('提示',data.msg , "info");
	// }
	//			
	// },
	//				
	// error : function(jqXHR, textStatus, errorThrown) {
	// $.messager.alert('系统异常', "请求数据失败!", "error");
	// }
	// });

	$.post("../worditem.action", dataStr, function(data) {
		if (data.success) {
			$("#cityname").val(data.cityName);
			$("#selLocal").combotree("setValues",cityCode);
			$.messager.alert('提示', data.msg, "info");
		} else {
			$.messager.alert('提示', data.msg, "info");
		}
	}, "json");

}
function update1() {
	var cityCode = $("#selLocal").combotree("getValues");
	var cityName = $('#selLocal').combotree('getText');
	if(cityCode==null||cityCode==''){
		alert("地市不能为空！");
		return;
	}
	if (cityCode.indexOf("全国") > -1) {
		cityCode = "全国";
		cityName="全国";
	}

	var request = {
		action : "updatesynonymcity",
		wordid : wordid,
		curwordclass : wordclass,
		citycode : cityCode,
		cityname : cityName
	};

	var dataStr = {
		m_request : JSON.stringify(request)
	}

	$.post("../synonym.action", dataStr, function(data) {
		if (data.success) {
			$("#cityname1").val(data.cityName);
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

