package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserOperResource;
import com.knowology.dal.Database;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.entity.InsertOrUpdateParam;
import com.knowology.km.entity.UserCity;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getConfigValue;

public class WorditemSentenceDAO {
	/**
	 * 带分页的查询满足条件的词条信息
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param worditem参数词条
	 * @param worditemprecise参数是否精确
	 * @param iscurrentwordclass参数是否当前词类
	 * @param worditemtype参数词条类型
	 * @param curwordclass参数当前词类名称
	 * @return json串
	 */
	public static Object select(int start, int limit, String worditem,
			Boolean worditemprecise, Boolean iscurrentwordclass,
			String worditemtype, String curwordclass, String citycode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		worditem = worditem.replaceAll("[\\[|\\]|!|<|>|+|*]{1}", "%");
		int count = UserOperResource.getWordCount(worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "子句", citycode);
			// 判断数据源不为null且含有数据
			if (count  > 0) {
				// 将条数放入jsonObj的total对象中
				jsonObj.put("total", count);
				Result rs = UserOperResource.selectWord(start, limit, worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "子句", citycode);
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					Object sre = GetSession.getSessionByKey("accessUser");
					User user = (User) sre;
					String userCity = UserCity.cityCodes.get(user.getUserID());
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义json对象
						JSONObject obj = new JSONObject();
						String city = rs.getRows()[i].get("city") == null ? "全国" : rs.getRows()[i].get("city").toString();
						if (city.contains("全国") || userCity.contains("全国")) {
							obj.put("isInclude", true);
						}else {
							if(userCity!=null && userCity.length()>0){
								for(String ucode : userCity.split(",")){
									if(city.contains(ucode)){
										obj.put("isInclude", true);
										break;
									}
								}
								
							}
						}
						if (!obj.getBooleanValue("isInclude")){
							obj.put("isInclude", false);
						}
						if(UserOperResource.isHaveOtherName(rs.getRows()[i].get("wordid").toString())){
							//if (!oldCityCode.contains("全国")) {
							//获取包含交集地市的别名
							Integer count1 = UserOperResource.getSynonymCount("", false, true, true, null, rs.getRows()[i].get("word").toString(), curwordclass, "子句", userCity);
							if (count1 > 0) {
								obj.put("isHaveSynonym", "有");
							} else {
								obj.put("isHaveSynonym", "无");
							}
						}else {
							obj.put("isHaveSynonym", "无");
						}
						// 生成id对象
						obj.put("id", start + i + 1);
						// 生成worditem对象
						String worditemStr  = rs.getRows()[i].get("word").toString();
						if(worditemStr.contains("近类")||worditemStr.contains("*")||worditemStr.contains("父类")||worditemStr.contains("子句")){
							worditemStr =SimpleString.worpattosimworpat(worditemStr + "@2#编者=\"自学习\"").split("#")[0]; 
						}
						// 生成worditem对象
						obj.put("worditem", worditemStr);
						// 生成wordclass对象
						obj.put("wordclass", rs.getRows()[i].get("wordclass"));
						// 生成type对象
						obj.put("type", rs.getRows()[i].get("type"));
						// 生成wordid对象
						obj.put("wordid", rs.getRows()[i].get("wordid"));
						// 生成wordclassid对象
						obj.put("wordclassid", rs.getRows()[i]
								.get("wordclassid"));
						// 将生成的对象放入jsonArr数组中
						jsonArr.add(obj);
					}
				}
				// 将jsonArr数组放入jsonObj的root对象中
				jsonObj.put("root", jsonArr);
			} else {
				// 将0放入jsonObj的total对象中
				jsonObj.put("total", 0);
				// 清空jsonArr数组
				jsonArr.clear();
				// 将空的jsonArr数组放入jsonObj的root对象中
				jsonObj.put("root", jsonArr);
			}
		
		return jsonObj;
	}

	/**
	 * 更新词条
	 * 
	 * @param oldworditem参数原有词条
	 * @param newworditem参数新的词条
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordclassid参数词类id
	 * @param wordid参数词条id
	 * @param request参数request请求
	 * @return 更新返回的json串
	 */
	public static Object update(String oldworditem, String newworditem,
			String oldtype, String newtype, String wordclassid, String wordid,String curwordclass,String curwordclasstype,
			HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User) sre;
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		/*String customer = user.getCustomer();
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		//获取原词条的地市属性
		Result oldRs = UserOperResource.selectWordCity(curwordclass, wordid);
		String oldCityCode = oldRs.getRows()[0].get("city") == null ? "全国" : oldRs.getRows()[0].get("city").toString();
		String oldCityName = oldRs.getRows()[0].get("cityname") == null ? "全国" : oldRs.getRows()[0].get("cityname").toString();
		String oldCityName1 = oldCityName;
		//用户有权限的城市和词条地市的交集
		String ownCities = "";
		String[] oldCities = oldCityCode.split(",");
		//用户权限不为全国且原词条地市不为全国
		if (!userCityCode.contains("全国") && !oldCityCode.contains("全国")){
			if(oldCities.length>0){
				//循环原词条地市属性和用户权限地市是否有交集
				for(String oldCity : oldCities){
					for(String userCity : userCityCode.split(",")){
						if(oldCity.equals(userCity)){
							//将相同地市放入交集集合
							if ("".equals(ownCities)) {
								ownCities = oldCity;
							}else {
								ownCities += "," + oldCity;	
							}
						}
					}
				}
				//如果用户权限不为全国或者用户权限地市与原词条地市无交集
				if(ownCities.length() < 1){
					jsonObj.put("success", false);
					jsonObj.put("msg", "当前用户没有修改权限!");
					return jsonObj;
				}
			}
		}
		if (userCityCode.contains("全国") && !oldCityCode.contains("全国")) {
			ownCities = oldCityCode;
		}
		if (!userCityCode.contains("全国") && oldCityCode.contains("全国")){
			ownCities = userCityCode;
		}
		newworditem = newworditem.trim();
		if(newworditem.endsWith("类")||newworditem.contains("*")){//说明是存子句条
	    newworditem = newworditem.replace(" ", "");
		// 将新的词条简单词模保存词模
		newworditem = SimpleString.SimpleWordPatContentToWordPat(newworditem);
		// 将旧的词条简单词模保存词模
		oldworditem = SimpleString.SimpleWordPatContentToWordPat(oldworditem);
		// 合法性检查
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
		String path = request.getSession().getServletContext().getRealPath("/");
		// 词模检查结果字符串
		String checkInfo = "";
		// 语法检查过程出现异常！
		Boolean checkflag = true;
		// 定义校验变量
		CheckInforef curcheckInfo = new CheckInforef();
		try {
			// 调用词模检查函数
			if (!CheckInput.CheckGrammer(path, newworditem, 0, curcheckInfo))
				// 词模有误
				checkflag = false;
		} catch (Exception ex) {
			// 检查过程中出现异常，则报错
			checkflag = false;
			curcheckInfo.curcheckInfo = "模板语法有误！";
		}

		// 分条报错
		if (!"".equals(curcheckInfo.curcheckInfo)
				&& (!"没有语法错误".equals(curcheckInfo.curcheckInfo))) {
			checkInfo += curcheckInfo.curcheckInfo + "<br>";
		}
		// 词模检查失败，则报错
		if (!checkflag) {
			if(!checkInfo.contains("数据库")&&!checkInfo.contains("缺少")){
				checkInfo = "语法有误，请确认后添加!";
			}
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", checkInfo);
			return jsonObj;
		}
		// 将新的词条词模变成词条
		newworditem = newworditem.split("@")[0];
		// 将旧的词条词模变成词条
		oldworditem = oldworditem.split("@")[0];
		
		}
		if ((oldtype.equals(newtype) && "标准名称".equals(oldtype))||(!oldtype.equals(newtype) && "标准名称".equals(oldtype))) {
			if(UserOperResource.isHaveOtherName(wordid)){
				//if (!oldCityCode.contains("全国")) {
					//获取包含交集地市的别名
					Integer count = UserOperResource.getSynonymCount("", false, true, true, null, oldworditem, curwordclass, curwordclasstype, ownCities);
					if (count > 0) {
						jsonObj.put("success", false);
						// 将存在信息放入jsonObj的msg对象中
						jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
						return jsonObj;
					}
					
			}
		}
			//判断修改后的词条名称是否数据库中有记录
			if(Exists(wordclassid, newworditem, newtype)){
				//更改后词条地市是否包含改之前的地市
				boolean newHaveOld = true;
				Result newRs = selectWordCity(wordclassid, newworditem, newtype);
				String type1 = newRs.getRows()[0].get("type").toString();
				if("标准名称".equals(type1)||"普通词".equals(type1)){
					//获取修改后词条的地市
					String newCityCode = newRs.getRows()[0].get("city") == null ? "全国" : newRs.getRows()[0].get("city").toString();
					String newCityName = newRs.getRows()[0].get("cityname") == null ? "全国" : newRs.getRows()[0].get("cityname").toString();
					String newCityName1 = newCityName;
					//更名后的词条地市不能为全国
					if(!newCityCode.contains("全国")){
						//如果原词条地市为全国且用户权限为全国则将新词条地市改为全国
						if (oldCityCode.contains("全国") && userCityCode.contains("全国")) {
							oldCityCode = "";
							newCityCode = "全国";
							newCityName = "全国";
							newHaveOld = false;
						}else {
							//判断修改后的词条地市是否包含原有地市
							for(String ownCity : ownCities.split(",")){
								if(!newCityCode.contains(ownCity)){
									newCityCode += "," + ownCity;
									newCityName += "," + WorditemDAO.cityMap.get(ownCity);
									newHaveOld = false;
								}		
							}
							//不包含原有地市
							if(!newHaveOld){
								//将原有地市全国改为地市集合
								if (oldCityCode.contains("全国")){
									oldCityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
									oldCityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
								}
								if (!"".equals(ownCities)) {
									for(String ownCity : ownCities.split(",")){
										String cityName = WorditemDAO.cityMap.get(ownCity);
										if (oldCityCode.contains(ownCity)) {
											//原地市去除修改的地市
											oldCityCode = MyUtil.removeCity(oldCityCode, ownCity);
										}
										if (oldCityName.contains(cityName)) {
											oldCityName = MyUtil.removeCity(oldCityName, cityName);
										}
									}
								}
							}
						}
						if (!newHaveOld) {
							int c = 0;
							//去除交集地市后原词条地市为空，删除该词条，不为空则将原词条地市去除修改的地市。
							if ("".equals(oldCityCode)){
								c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, oldworditem, "基础", oldCityName1);
							}else {
								c = UserOperResource.updateWordCity(user, curwordclass, wordid, oldCityName, oldCityCode, oldworditem, oldCityName1);
							} 
							if(WorditemDAO.cityMap.keySet().size()<=newCityCode.split(",").length){
								newCityName="全国";
								newCityCode="全国";
							}
							int d = UserOperResource.updateWordCity(user, curwordclass, newRs.getRows()[0].get("wordid").toString(), newCityName, newCityCode, newworditem, newCityName1);
							// 判断事务处理结果
							if (c > 0 && d>0) {
								// 事务处理成功
								// 将true放入jsonObj的success对象中
								jsonObj.put("success", true);
								// 将成功信息放入jsonObj的msg对象中
								jsonObj.put("msg", "修改成功!");
								return jsonObj;
							} else {
								// 事务处理失败
								// 将false放入jsonObj的success对象中
								jsonObj.put("success", false);
								// 将失败信息放入jsonObj的msg对象中
								jsonObj.put("msg", "修改失败!");
								return jsonObj;
							}
						}
					}
				}else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将存在信息放入jsonObj的msg对象中
					jsonObj.put("msg", "词条在别名中已存在!");
					return jsonObj;
				}
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将存在信息放入jsonObj的msg对象中
				jsonObj.put("msg", "词条已存在!");
				return jsonObj;
			}
			
//		}
		String newCityCode = "";
		String newCityName = "";
		//如果是全国用户，直接修改
		if (userCityCode.contains("全国")) {
			int d = UserOperResource.updateWord(user, oldworditem, newworditem, oldtype, newtype, wordid, wordclassid, curwordclass, curwordclasstype, "基础");
			if (d > 0 ) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "修改成功!");
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "修改失败!");
			}
		}else {
			if (oldCityCode.contains("全国")) {
				oldCityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
			}
			for(String ownCity : ownCities.split(",")){
				String cityName = WorditemDAO.cityMap.get(ownCity);
				if (oldCityCode.contains(ownCity)) {
					oldCityCode = MyUtil.removeCity(oldCityCode, ownCity);
				}
				if (oldCityName.contains(cityName)) {
					oldCityName = MyUtil.removeCity(oldCityName, cityName);
				}
				if(newCityCode!=""){
					newCityCode += "," + ownCity;
					newCityName += "," + cityName;
				}else{
					newCityCode += ownCity;
					newCityName += cityName;
				}
			}

			int c = 0;
			if ("".equals(oldCityCode)){
				c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, oldworditem, "子句", oldCityName1);
			}else {
				c = UserOperResource.updateWordCity(user, curwordclass, wordid, oldCityName, oldCityCode, oldworditem, oldCityName1);
			} 
			List<String> worditemList = new ArrayList<String>();
			worditemList.add(newworditem);
			int d = UserOperResource.insertWord(user, wordclassid, curwordclass, curwordclasstype, worditemList, newtype, "子句", newCityCode, newCityName);
			// 判断事务处理结果
			if (c > 0 && d > 0) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "修改成功!");
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "修改失败!");
			}
		}
		return jsonObj;
	}

	/**
	 * 判断词条是否重复
	 * 
	 * @param curwordclassid参数当前词类id
	 * @param worditem参数词条名称
	 * @param newtype参数词条类型
	 * @return 是否重复
	 */
	public static Boolean Exists(String curwordclassid, String worditem,
			String newtype) {
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 查询词条是否重复的SQL语句
		String sql = "select wordid from word where rownum<2 and wordclassid=? and lower(word)=lower(?) ";
		// 绑定词类id参数
		lstpara.add(curwordclassid);
		// 绑定词条参数
		lstpara.add(worditem);
		// 绑定词条类型参数
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源标签null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 有重复词条，返回true
				return true;
			} else {
				// 没有重复词条，返回false
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			return false;
		}
	}

	/**
	 * 修改词条操作
	 * 
	 * @param oldworditem参数原有词条
	 * @param newworditem参数新的词条
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordid参数词条id
	 * @return 修改返回的结果
	 */
	private static int _update(String oldworditem, String newworditem,
			String oldtype, String newtype, String wordid,String wordclassid) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 更新词条的SQL语句
		String sql = "update word set word=?,type=? where wordid=? ";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(newworditem);
		// 绑定类型参数
		lstpara.add(newtype);
		// 绑定词条id参数
		lstpara.add(wordid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);
		
		// 更新当前词类编辑时间
		sql = "update wordclass set time =sysdate  where  wordclassid = ?";
		// 定义绑定参数集合
		lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(wordclassid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);
		 

		// 将旧的词条简单词模转换为词模
		String simpleOldworditem = SimpleString.SimpleWordPatContentToWordPat(
				oldworditem).split("@")[0];
		// 将新的词条简单词模转换为词模
		String simpleNewworditem = SimpleString.SimpleWordPatContentToWordPat(
				newworditem).split("@")[0];
		// 更新与子句词模有关的词模的SQL语句
		sql = "update wordpat t set t.wordpat=replace(t.wordpat,?,?),t.simplewordpat=replace(t.simplewordpat,?,?) where t.wordpatid in(select w.wordpatid from service s,kbdata k,wordpat w where s.serviceid=k.serviceid and k.kbdataid=w.kbdataid and s.brand=? and w.wordpat like ''||?||'%')";
		// 定义绑定参数集合
		lstpara = new ArrayList<String>();
		// 绑定旧的子句词条参数
		lstpara.add(oldworditem);
		// 绑定新的子句词条参数
		lstpara.add(newworditem);
		// 绑定旧的子句词模参数
		lstpara.add(simpleOldworditem);
		// 绑定新的子句词模参数
		lstpara.add(simpleNewworditem);
		// 绑定品牌参数
		lstpara.add(getConfigValue.brand);
		// 绑定子句词模参数
		lstpara.add(oldworditem + "@");
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);

		// 生成操作日志记录
		// 将SQL语句放入集合中
		lstsql.add(MyUtil.LogSql());
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(MyUtil.LogParam(getConfigValue.brand, " ", "更新模板", "上海",
				"更新子句词条时更新相关的模板：" + simpleOldworditem + "==>"
						+ simpleNewworditem, "WORDPAT"));

		// 生成操作日志记录
		// 将SQL语句放入集合中
		lstsql.add(MyUtil.LogSql());
		// 将对应的绑定参数集合放入集合中
		lstlstpara
				.add(MyUtil.LogParam(" ", " ", "更新词条", "上海",
						oldworditem + "==>" + newworditem + "," + oldtype
								+ "==>" + newtype, "WORD"));
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 新增子句词条
	 * 
	 * @param worditem参数词条
	 * @param curwordclass参数当前词类名称
	 * @param curwordclassid参数当前词类id
	 * @param isstandardword参数是否是标准词
	 * @param curwordclasstype 词类归属
	 * @param request参数request请求
	 * @return 新增返回的json串
	 */
	public static Object insert(String worditem, String curwordclass,
			String curwordclassid, String curwordclasstype,Boolean isstandardword,String citycode,String cityname,
			HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User) sre;
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		/*String customer = user.getCustomer();
		
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		// 判断当前处理id是否为空，null
		if (curwordclassid == null || "".equals(curwordclassid)) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", "请选择当前词类!");
			return jsonObj;
		}
		// 用换行符拆分用户输入的多条词条,去除空字符串和空格等空白字符
		List<String> lstWorditem = Arrays.asList(worditem.split("\n"));
		// 合法性检查
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
		String path = request.getSession().getServletContext().getRealPath("/");
		// 词模检查结果字符串
		String checkInfo = "";
		// 语法检查过程出现异常！
		Boolean checkflag = true;
		// 定义子句词条遍历
		String pattern = "";
		// 定义子句词条集合
		List<String> worditemList = new ArrayList<String>();
		// 定义校验变量
		CheckInforef curcheckInfo = new CheckInforef();
		// 循环遍历子句词条集合
		for (int i = 0; i < lstWorditem.size(); i++) {
			String wd = lstWorditem.get(i);
			if(!wd.endsWith("近类")&&!wd.endsWith("父类")&&!wd.endsWith("子句")&&!wd.contains("*")){//说明是存子句条
				checkflag = true;
				pattern = wd;
			}else{
				wd = wd.replace(" ", "");
				// 将子句词条变成简单词模
				pattern = wd + "#无序#编者=\"自学习\"";
				// 定义词模对象
				InsertOrUpdateParam m_param = new InsertOrUpdateParam();
				// 将子句变成的简单词模赋值给词模对象
				m_param.simplewordpat = pattern;
				// 将简单词模变成词模
				pattern = SimpleString.SimpleWordPatToWordPat(m_param);
				try {
					// 调用词模检查函数
					if (!CheckInput.CheckGrammer(path, pattern, 0, curcheckInfo))
						// 词模有误
						checkflag = false;
				} catch (Exception ex) {
					// 检查过程中出现异常，则报错
					checkflag = false;
					curcheckInfo.curcheckInfo = "模板语法有误！";
				}
				// 判断校验
				if (!"".equals(curcheckInfo.curcheckInfo)
						&& (!"没有语法错误".equals(curcheckInfo.curcheckInfo))) {
					checkInfo += "第" + (i + 1) + "条：" + curcheckInfo.curcheckInfo
							+ "<br>";
				}
			}
			// 将词条变成的词模放入词条集合中
			worditemList.add(pattern);
		}

		// 词模检查失败，则报错
		if (!checkflag) {
			if(!checkInfo.contains("数据库")&&!checkInfo.contains("缺少")){
				checkInfo = "语法有误，请确认后添加!";
			}
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", checkInfo);
			return jsonObj;
		}
		// 获取该词条的type
		String type = "";
		if (isstandardword) {
			type = "标准名称";
		} else {
			type = "普通词";
		}
		String msg = "";
		String msg1 = "";
		List<String> listWord= new ArrayList<String>();
		int count = 0;
		// 循环添加每条词模
		for (int i = 0; i < worditemList.size(); i++) {
			// 判断是否已存在相同词条
//			if (Exists(curwordclassid, worditemList.get(i).split("@")[0], type)) {
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "第" + (i + 1) + "条词条已存在!");
//				return jsonObj;
//			}
			if(listWord.contains(worditemList.get(i).split("@")[0])){
				if("".equals(msg)){
					msg="第";	
				}
				msg = msg+(i + 1)+",";
				continue;
			}
			if(Exists(curwordclassid, worditemList.get(i).split("@")[0], type)){
				Result rs = selectWord(curwordclassid, worditemList.get(i).split("@")[0], type);
				String type1 = rs.getRows()[0].get("type").toString();
				if("标准名称".equals(type1)||"普通词".equals(type1)){
					String oldCityCode = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
					String oldCityName = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
					String oldCityName1 = oldCityName;
					String wordid = rs.getRows()[0].get("wordid").toString();
					if (!oldCityCode.contains("全国")){
						int n = 0;
						if (citycode.contains("全国")) {
							oldCityCode = "全国";
							oldCityName = "全国";
							n = 1;
						}else {
							for (String code : citycode.split(",")) {
								if (!oldCityCode.contains(code)) {
									oldCityCode += "," + code;
									oldCityName += "," + WorditemDAO.cityMap.get(code);
									n = 1;
								}
							}
						}
						if (n > 0) {
							//更新地市属性
							if(WorditemDAO.cityMap.keySet().size()<=oldCityCode.split(",").length){
								oldCityCode="全国";
								oldCityName="全国";
							}
							int m = UserOperResource.updateWordCity(user, curwordclass, wordid, oldCityName, oldCityCode,worditemList.get(i).split("@")[0],oldCityName1 );
							if (m <= 0) {
								// 将false放入jsonObj的success对象中
								jsonObj.put("success", false); 
								// 将信息放入jsonObj的msg对象中
								jsonObj.put("msg", msg);
								return jsonObj;
							}
							count++;
							continue;
						}
					
					}
					if("".equals(msg)){
						msg="第";	
					}
					msg = msg+(i + 1)+",";
				}else {
					if("".equals(msg1)){
						msg1="第";	
					}
					msg1 = msg1+(i + 1)+",";
				}
			}else{
				listWord.add(worditemList.get(i).split("@")[0]);	
			}
		}
		if(msg.length()>1){
			msg = msg.substring(0, msg.lastIndexOf(","));
			msg =msg +"条词条已存在!";
		}
		if(msg1.length()>1){
			msg1 = msg1.substring(0, msg1.lastIndexOf(","));
			msg1 =msg1 +"条词条在别名中已存在!";
		}
		// 执行SQL语句，获取事务处理的结果
//		int c = _insert(curwordclassid, curwordclass, curwordclasstype,worditemList, type);
		int c = 0;
		if(listWord.size()>0){
			// 执行SQL语句，获取事务处理的结果
			c = UserOperResource.insertWord(user, curwordclassid, curwordclass, curwordclasstype, listWord, type, "子句",citycode,cityname);
		}
		// 判断事务处理结果
		if (c > 0 || count > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", count+listWord.size()+"条词条保存成功!<br>"+msg+"<br>"+msg1);
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!<br>"+msg+"<br>"+msg1);
		}
		return jsonObj;
	}

	/**
	 * 保存操作
	 * 
	 * @param curwordclassid参数词类id
	 * @param curwordclass参数词类名称
	 * @param worditemList参数词条集合
	 * @param curwordclasstype 词类归属
	 * @param type参数词条类型
	
	 * @return 保存返回的结果
	 */
	private static int _insert(String curwordclassid, String curwordclass,String curwordclasstype,
			List<String> worditemList, String type) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义保存词条的SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 定义词条遍历
		String worditem = "";
		// 循环遍历词条集合
		for (int i = 0; i < worditemList.size(); i++) {
			worditem = worditemList.get(i).split("@")[0];
			// 定义保存词条的SQL语句
			sql = "insert into word(wordid,wordclassid,word,type) values(?,?,?,?) ";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 获取词条表的序列值
			String id = String.valueOf(SeqDAO.GetNextVal("seq_word_id"));
			// 绑定id参数
			lstpara.add(id);
			// 绑定词类id参数
			lstpara.add(curwordclassid);
			// 绑定词类名称
			lstpara.add(worditem);
			// 绑定类型参数
			lstpara.add(type);
			// 将SQL语句放入集合中
			lstsql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstlstpara.add(lstpara);

			// 生成操作日志记录
			// 将SQL语句放入集合中
			lstsql.add(MyUtil.LogSql());
			// 将对应的绑定参数集合放入集合中
			lstlstpara.add(MyUtil.LogParam(" ", " ", "增加词条", "上海", curwordclass
					+ "==>" + worditem, "WORD"));
		}
		
		// 更新当前词类编辑时间
		sql = "update wordclass set time =sysdate  where  wordclassid = ?";
		// 定义绑定参数集合
		lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(curwordclassid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);

		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 删除词条
	 * 
	 * @param wordid参数词条id
	 * @param curwordclass参数词类名称
	 * @param worditem参数词条名称
	 * @return 删除返回的json串
	 */
	public static Object delete(String wordid, String curwordclass,String curwordclasstype,
			String worditem) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User) sre;
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		/*String customer = user.getCustomer();
		
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		// 将子句词条变成简单词模
		String pattern = worditem + "#无序#编者=\"自学习\"";
		// 定义词模对象
		InsertOrUpdateParam m_param = new InsertOrUpdateParam();
		// 将子句变成的简单词模赋值给词模对象
		m_param.simplewordpat = pattern;
		if(pattern.endsWith("类")||pattern.contains("*")){//说明是子句条
			// 将简单词模变成词模
			pattern = SimpleString.SimpleWordPatToWordPat(m_param);
			pattern = pattern.split("@")[0];	
		}else{
			pattern = worditem;	
		}
		
		
	
//		// 定义多条SQL语句集合
//		List<String> lstsql = new ArrayList<String>();
//		// 定义多条SQL语句对应的绑定参数集合
//		List<List<?>> lstlstpara = new ArrayList<List<?>>();
//		// 定义SQL语句
//		String sql = "";
//		// 定义绑定参数集合
//		List<String> lstpara = new ArrayList<String>();
//		// 删除词条的SQL语句
//		sql = "delete from word where wordid=?";
//		// 定义绑定参数集合
//		lstpara = new ArrayList<String>();
//		// 绑定词条id参数
//		lstpara.add(wordid);
//		// 将SQL语句放入集合中
//		lstsql.add(sql);
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(lstpara);
//
//		// 删除别名的SQL语句
//		sql = "delete from word  where stdwordid=?";
//		// 定义绑定参数集合
//		lstpara = new ArrayList<String>();
//		// 绑定词条id参数
//		lstpara.add(wordid);
//		// 将SQL语句放入集合中
//		lstsql.add(sql);
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(lstpara);
//		
//		// 更新当前词类编辑时间
//		sql = "update wordclass set time =sysdate  where  wordclass = ?";
//		// 定义绑定参数集合
//		lstpara = new ArrayList<String>();
//		// 绑定词条参数
//		lstpara.add(curwordclass);
//		// 将SQL语句放入集合中
//		lstsql.add(sql);
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(lstpara);
//
//		// 删除与子句词模有关的词模
//		sql = "delete from wordpat t where t.wordpatid in(select w.wordpatid from service s,kbdata k,wordpat w where s.serviceid=k.serviceid and k.kbdataid=w.kbdataid and s.brand=? and w.wordpat like ''||?||'%')";
//		// 定义绑定参数集合
//		lstpara = new ArrayList<String>();
//		// 绑定品牌参数
//		lstpara.add(getConfigValue.brand);
//		// 绑定子句词模参数
//		lstpara.add(pattern + "@");
//		// 将SQL语句放入集合中
//		lstsql.add(sql);
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(lstpara);
//
//		// 生成操作日志记录
//		// 将SQL语句放入集合中
//		lstsql.add(MyUtil.LogSql());
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(MyUtil.LogParam(getConfigValue.brand, " ", "删除模板", "上海",
//				"删除子句词条时删除相关的词模：" + worditem + "@", "WORDPAT"));
//
//		// 生成操作日志记录
//		// 将SQL语句放入集合中
//		lstsql.add(MyUtil.LogSql());
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(MyUtil.LogParam(" ", " ", "删除词条", "上海", curwordclass
//				+ "==>" + worditem, "WORD"));
//		// 执行SQL语句，绑定事务处理，返回事务处理的结果
//		int c = Database.executeNonQueryTransaction(lstsql, lstlstpara);
		//获取要删除的词条地市
		Result rs = UserOperResource.selectWordCity(curwordclass, wordid);
		String oldCityCode = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
		String oldCityName = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
		String oldCityName1 = oldCityName;
		//如果用户为全国权限，直接删除
		if (userCityCode.contains("全国")) {
			oldCityCode = "";
		}else {
			if (oldCityCode.contains("全国")) {
				oldCityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
			}
			boolean delete = false;
			//原有地市中删除用户有权限的地市
			for (String userCode : userCityCode.split(",")) {
				String userCityName = WorditemDAO.cityMap.get(userCode);
				if (oldCityCode.contains(userCode)) {
					oldCityCode = MyUtil.removeCity(oldCityCode, userCode);
					delete = true;
				}
				if (oldCityName.contains(userCityName)) {
					oldCityName = MyUtil.removeCity(oldCityName, userCityName);
				}
			}
			if (!delete) {
				jsonObj.put("success", false);
				jsonObj.put("msg", "该词条不包含当前用户权限地市!");
				return jsonObj;
			}
		}
		
		int c = 0;
		if ("".equals(oldCityCode)){
			//如果去除后词条地市为空  则直接删除
			c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, worditem, "子句", oldCityName1);
		}else {
			//去除后词条地市不为空，更新词条地市属性
			c = UserOperResource.updateWordCity(user, curwordclass, wordid, oldCityName, oldCityCode, worditem, oldCityName1);
			//获取词条下所有别名
			Integer count1 = UserOperResource.getSynonymCount("", false, true, true, "", worditem, curwordclass, curwordclasstype, "");
			if (count1 > 0){
				Result rs1 = UserOperResource.selectSynonym(0, count1, "", false, true,  true, "", worditem, curwordclass,"子句", "");
				for (int i = 0; i < rs1.getRowCount(); i++) {
					int count = 0;
					//获取别名地市属性
					String cityCode = rs1.getRows()[i].get("city") == null ? "全国" : rs1.getRows()[i].get("city").toString();
					String cityName = rs1.getRows()[i].get("cityname") == null ? "全国" : rs1.getRows()[i].get("cityname").toString();
					String cityName1= cityName;
					//如果地市为全国改为所有地市集合
					if ("全国".equals(cityCode)) {
						cityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
						cityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
					}
					//别名地市中去除用户权限地市
					for (String userCity : userCityCode.split(",")) {
						String userCityName = WorditemDAO.cityMap.get(userCity);
						if (cityCode.contains(userCity)) {
							cityCode = MyUtil.removeCity(cityCode, userCity);
							cityName = MyUtil.removeCity(cityName, userCityName);
							count ++;
						}
					}
					int d = 0;
					if (count > 0){
						if ("".equals(cityCode)) {
							d = UserOperResource.deleteSynonyms(user, rs1.getRows()[i].get("wordid").toString(), rs1.getRows()[i].get("word").toString(), worditem, curwordclass, cityName1);
						}else {
							d = UserOperResource.updateSynonymCity(user,curwordclass, worditem, rs1.getRows()[i].get("wordid").toString(), cityName, cityCode,rs1.getRows()[i].get("word").toString(),cityName1);
						}
					}
					if (d <= 0) {
						// 事务处理失败
						// 将false放入jsonObj的success对象中
						jsonObj.put("success", false);
						// 将失败信息放入jsonObj的msg对象中
						jsonObj.put("msg", "删除失败!");
					}
				}
			}
		}
		//int c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, pattern, "子句");
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}
	/**
	 * 根据词类id和词条名称查询词条
	 * @param curwordclassid 词类id
	 * @param worditem	词条名称
	 * @param newtype 词条类型
	 * @return
	 */
	public static Result selectWord(String curwordclassid, String worditem,String newtype){
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 查询词条是否重复的SQL语句
		String sql = "select * from word where rownum<2 and wordclassid=? and lower(word)=lower(?) ";
		// 绑定词类id参数
		lstpara.add(curwordclassid);
		// 绑定词条参数
		lstpara.add(worditem);
		// 执行SQL语句，获取相应的数据源
		Result rs = Database.executeQuery(sql, lstpara.toArray());
		return rs;
			
	}
	/**
	 *@description  更新词条city
	 *@param wordclass 词类
	 *@param wordid 词条ID
	 *@param cityNme 城市名称
	 *@param cityCode 城市代码
	 *@return 
	 *@returnType Object 
	 */
	public static Object updateSentenceCity(String wordclass ,String wordid,String cityNme,String cityCode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		if(cityCode.contains("全选")){
			cityCode.replace("全选,", "");
			cityNme.replace("全选,", "");
		}
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		/*String customer = user.getCustomer();
		
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		String oldCityCode = "";
		String oldCityName = "";
		//全国权限用户直接修改地市，如果修改的地市包含全国则地市为全国
		if (userCityCode.contains("全国")) {
			if (!cityCode.contains("全国")) {
				oldCityCode = cityCode;
				oldCityName = cityNme;
			}else {
				oldCityCode = "全国";
				oldCityName = "全国";
			}
		}else {
			//获取当前词条原地市信息
			Result rs = UserOperResource.selectWordCity(wordclass,wordid);
			oldCityCode = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
			oldCityName = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
			String worditem = rs.getRows()[0].get("word").toString();
			//获取用户城市和原地市的交集，用来确认是否有删除城市
			String inCity = "";
			//如果原地市为全国，则交集为用户权限
			if (!userCityCode.contains("全国") && oldCityCode.contains("全国")) {
				inCity = userCityCode;
			} else {
				for (String userCode : userCityCode.split(",")) {
					for (String oldCode : oldCityCode.split(",")) {
						if (userCode.equals(oldCode)) {
							if ("".equals(inCity)) {
								inCity = userCode;
							}else {
								inCity += "," + userCode;
							}
						}
					}
				}
			}
			if (oldCityCode.contains("全国")) {
				oldCityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
			}
			if (!"".equals(inCity)) {
				//循环交集地市，看是否有删除地市
				for (String inCityCode : inCity.split(",")){
					//如果修改后的地市不包含交集地市，则表示删除该城市
					if (!cityCode.contains(inCityCode)) {
						//获取词条下的所有别名，判断别名下是否有该删除地市，有的话提示无法修改
						Integer count = UserOperResource.getSynonymCount("", false, true, true, null, worditem, wordclass, "子句", "");
						Result synonymRs = UserOperResource.selectSynonym(0, count, "", false, true, true, "", worditem, wordclass,"子句", "");
						for(int i = 0 ; i < synonymRs.getRowCount() ; i++){
							String synonymCitiyCode = synonymRs.getRows()[i].get("city") == null ? "全国" : synonymRs.getRows()[i].get("city").toString();
							//判断别名中是否包含删除地市，有的话提示无法修改
							if (synonymCitiyCode.contains("全国") || synonymCitiyCode.contains(inCityCode)) {
								jsonObj.put("success", false);
								jsonObj.put("msg", "请先修改别名地市！");
								return jsonObj;
							}
						}
					}
					//原词条地市去除交集地市
					oldCityCode = MyUtil.removeCity(oldCityCode, inCityCode);
					oldCityName = MyUtil.removeCity(oldCityName, WorditemDAO.cityMap.get(inCityCode));
				}
			}
			//去除后如果为空
			if ("".equals(oldCityCode)){
				oldCityCode = cityCode;
				oldCityName = cityNme;
			}else {
				oldCityCode += "," + cityCode;
				oldCityName += "," + cityNme;
			}
			
		}
		if(WorditemDAO.cityMap.keySet().size()<=oldCityCode.split(",").length){
			oldCityCode="全国";
			oldCityName="全国";
		}
		int c  = UserOperResource.updateWordCity(user,wordclass, wordid, oldCityName, oldCityCode,"","");
		
		// 判断数据源不为null且含有数据
		if (c>0) {
				// 生成id对象
			jsonObj.put("msg", "更新成功!");
			jsonObj.put("success", true);
		}else{
			jsonObj.put("msg", "更新失败!");
			jsonObj.put("success", false);
		}
		
		return jsonObj;
	}

	/**
	 * 根据词类id和词条名称查询词条city
	 * @param curwordclassid 词类id
	 * @param worditem	词条名称
	 * @param newtype 词条类型
	 * @return
	 */
	public static Result selectWordCity(String curwordclassid, String worditem,String newtype){
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 查询词条是否重复的SQL语句
		String sql = "select * from word where rownum<2 and wordclassid=? and word=? ";
		// 绑定词类id参数
		lstpara.add(curwordclassid);
		// 绑定词条参数
		lstpara.add(worditem);
		// 执行SQL语句，获取相应的数据源
		Result rs = Database.executeQuery(sql, lstpara.toArray());
		return rs;
			
	}
}