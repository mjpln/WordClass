var curwordclass = "";//当前词类
var curworditem = ""; //当前词条
var curwordclassid = "";//当前词类id
var curwordid = "";//当前词条id
var curtype = "";//当前词条类型
var rwordid="";
var curwordclasstype ="基础";//当前词类归属
var usercity = "";
var usercityname = "";


function Read4(hef) {
    var progressBar=Ext.Msg.show({
        x:100,
        y:100,    
        title:"提示",
        msg:"正在努力为您生成数据...",
        progress:true,
        width:300
    });
    var bartext = "0.3%";
    var curnum = 0.03;
    progressBar.updateProgress(curnum,bartext);
    var ta = Ext.TaskManager.start({
        run: function () {
            Ext.Ajax.request({
                url: '/KM/file/progress?'+hef,
                method: 'post',
                async: false,
                success: function (resp1, request) {
                    var data = Ext.JSON.decode(resp1.responseText);
                    if(data&&data.state){
                        switch(data.state){
                            case 1:
                                bartext = "0.3%";
                                curnum = 0.03;
                                progressBar.updateProgress(curnum,bartext);
                                break;
                            case 2:
                                curnum =(data.index/data.total);
                                bartext = (data.index/data.total*100).toFixed(1) + "%";
                                progressBar.updateProgress(curnum,bartext);
                                break;
                            case 3:
                                curnum = 0.999;
                                bartext = "99.9%";
                                progressBar.updateProgress(curnum,bartext,'数据生成成功,正在下载...');   
                                break;
                            case 4:
                                curnum = 1;
                                bartext = 100+"%";
                                progressBar.updateProgress(curnum,bartext);
                                setTimeout(function(value) {
                                    Ext.TaskManager.stop(ta);
                                    progressBar.hide();
                                }, 2000);
                                break;
                        }
                    }else{
                        setTimeout(function(value) {
                            Ext.TaskManager.stop(ta);
                            progressBar.hide();
                        }, 3000);
                    }
                },
                failure: function (result, request) {
                    Ext.MessageBox.alert('系统异常', '请求数据失败！');
                }
            });
        },
        interval:3000
    });
}

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
