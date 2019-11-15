var curwordclass = "";//当前词类
var curworditem = ""; //当前词条
var curwordclassid = "";//当前词类id
var curwordid = "";//当前词条id
var curtype = "";//当前词条类型
var curwordclasstype ="子句";//当前词类归属
var usercity = "";
var usercityname = "";

function quchong(a){
    var n = {},r = []; //n为hash表，r为临时数组
    for(var i = 0; i < a.length; i++){ //遍历当前数组
        if (!n[a[i]]){ //如果hash表中没有当前项
            n[a[i]] = true; //存入hash表
            r.push(a[i]); //把当前数组的当前项push到临时数组里面
        }
    }
    return r;
}
