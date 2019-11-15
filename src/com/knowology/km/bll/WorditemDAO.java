package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpUtils;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.poi.hmef.attribute.MAPIAttribute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.API.HttpClient;
import com.knowology.Bean.User;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.entity.UserCity;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.HttpClientUtils;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getConfigValue;

public class WorditemDAO {

	public static Map<String,String> cityMap = new LinkedHashMap<String,String>();
	// 省市区树结构
	static{
		Result cityRs = null;
		String citySql = "";
		citySql = "select t.name as id,min(s.name) as city from metafield t,metafield s,metafieldmapping a where a.name='地市编码配置' and t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and  t.name like '______' group by t.name order by id";
		try {
			cityRs = Database.executeQuery(citySql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (cityRs != null & cityRs.getRowCount()>0){
			for (int i = 0;i < cityRs.getRowCount();i++){
				if (cityRs.getRows()[i].get("id").toString().length() == 6){
					cityMap.put(cityRs.getRows()[i].get("id").toString(), cityRs.getRows()[i].get("city").toString());
				}
			}
		}
		//System.out.println(cityMap);
	}

	public static Object getCityTree(String local) {
		String cityname[] = local.split(",");
		Map<String,String> map = new HashMap<String,String>();
		for(int m=0;m<cityname.length;m++){
			map.put(cityname[m], "");
		}
		JSONArray jsonAr = new JSONArray();

		//Result rs =null;
		if (!cityMap.isEmpty()){
			JSONObject allJsonObj = new JSONObject();
			allJsonObj.put("id", "全国");
			allJsonObj.put("text", "全国");
			if (map.containsKey("全国")){
				allJsonObj.put("checked", true);
			}
			jsonAr.add(allJsonObj);
			for (Map.Entry<String, String> pro : cityMap.entrySet()){
				if (pro.getKey().contains("0000")){
					JSONObject jsonObj = new JSONObject();
					String id = pro.getKey();
					String province = pro.getValue();
					jsonObj.put("id", id);
					jsonObj.put("text", province);
					if (map.containsKey(province)){
						jsonObj.put("checked", true);
					}
					if (province.indexOf("市")<0){ //省
						String innerid = id.substring(0, 2);
						JSONArray jsonArrSon = new JSONArray();
						for (Map.Entry<String, String> proSon : cityMap.entrySet()){//地级市
							if (proSon.getKey().startsWith(innerid) && proSon.getKey().endsWith("00") && !proSon.getKey().endsWith("0000")){
								JSONObject jsonObjSon = new JSONObject();
								jsonObjSon.put("id", proSon.getKey());
								jsonObjSon.put("text", proSon.getValue());

								String innerInnerId = proSon.getKey().substring(0, 4);
								JSONArray jsonArrSonSon = new JSONArray();
								for (Map.Entry<String, String> proSonSon : cityMap.entrySet()){
									if (proSonSon.getKey().startsWith(innerInnerId) && !proSonSon.getKey().endsWith("00") && !proSonSon.getKey().endsWith("01")){
										JSONObject jsonObjSonSon = new JSONObject();
										jsonObjSonSon.put("id", proSonSon.getKey());
										jsonObjSonSon.put("text", proSonSon.getValue());
										if (map.containsKey(proSonSon.getValue())){
											jsonObjSonSon.put("checked", true);
										}
										jsonArrSonSon.add(jsonObjSonSon);
									}
								}

								if(!jsonArrSonSon.isEmpty()){
									jsonObjSon.put("children", jsonArrSonSon);
									jsonObjSon.put("state", "closed");
								}
								if (map.containsKey(proSon.getValue())){
									jsonObjSon.put("checked", true);
								}
								jsonArrSon.add(jsonObjSon);
							}
						}
						if (!jsonArrSon.isEmpty()){
							jsonObj.put("children",jsonArrSon);
							jsonObj.put("state", "closed");
						}
					} else { // 直辖市
						String innerid = id.substring(0, 2);
						JSONArray jsonArrSon = new JSONArray();
						for (Map.Entry<String, String> proSon : cityMap.entrySet()){
							if (proSon.getKey().startsWith(innerid) && !proSon.getKey().endsWith("00")){
								JSONObject jsonObjSon = new JSONObject();
								jsonObjSon.put("id", proSon.getKey());
								jsonObjSon.put("text", proSon.getValue());
								if (map.containsKey(proSon.getValue())){
									jsonObjSon.put("checked", true);
								}
								jsonArrSon.add(jsonObjSon);
							}
						}
						if (!jsonArrSon.isEmpty()){
							jsonObj.put("children",jsonArrSon);
							jsonObj.put("state", "closed");
						}
					}
					jsonAr.add(jsonObj);
				}
			}
		}
		//System.out.println(jsonAr);
		return jsonAr;
	}

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
		int count = UserOperResource.getWordCount(worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "基础", citycode);
		// 判断数据源不为null且含有数据
		if (count  > 0) {
			// 将条数放入jsonObj的total对象中
			jsonObj.put("total", count);
			Result rs = UserOperResource.selectWord(start, limit, worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "基础", citycode);

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
						Integer count1 = UserOperResource.getSynonymCount("", false, true, true, null, rs.getRows()[i].get("word").toString(), curwordclass, "基础", userCity);
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
					obj.put("worditem", rs.getRows()[i].get("word"));
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
	 * @return 更新返回的json串
	 */
	public static Object update(String oldworditem, String newworditem,
								String oldtype, String newtype, String wordclassid, String wordid,String curwordclass,String curwordclasstype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		//String customer = user.getCustomer();
		//判断用户有权限的地市是否为空
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}

		//获取原词条的地市属性
		Result oldRs = selectWordCity(wordclassid, oldworditem, oldtype);
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
		sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		/*if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/

		// 判断是否已存在相同词条
//		if (Exists(wordclassid, newworditem, newtype)) {
//			// 将false放入jsonObj的success对象中
//			jsonObj.put("success", false);
//			// 将存在信息放入jsonObj的msg对象中
//			jsonObj.put("msg", "词条已存在!");
//			return jsonObj;
//		}
		// 需要判断该标准名称是否已经录入了别名，录入了别名则不能修改！
		if ((oldtype.equals(newtype) && "标准名称".equals(oldtype))||(!oldtype.equals(newtype) && "标准名称".equals(oldtype))) {
//			// 定义查询别名的SQL语句
//			String sql = "select wordid from word where rownum<2 and stdwordid=?";
//			// 定义绑定参数集合
//			List<String> lstpara = new ArrayList<String>();
//			lstpara.add(wordid);
//			try {
//				// 执行SQL语句，获取相应的数据源
//				Result rs = Database.executeQuery(sql, lstpara.toArray());
//				// 判断数据源不为null且含有数据
//				if (rs != null && rs.getRowCount() > 0) {
//					// 将false放入jsonObj的success对象中
//					jsonObj.put("success", false);
//					// 将存在信息放入jsonObj的msg对象中
//					jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
//					return jsonObj;
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//				// 出现错误
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将存在信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
//				return jsonObj;
//			}

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
					/*for(int i = 0 ; i < synonymRs.getRowCount() ; i++){
						String synonymCitiyCode = synonymRs.getRows()[i].get("city") == null ? "全国" : synonymRs.getRows()[i].get("city").toString();
						//判断别名中是否包含用户地市与词条地市交集
							String[] synonymCities = synonymCitiyCode.split(",");
							for(String synonymCity : synonymCities){
								//如果修改的地址中包含别名中的地市，提示已有别名
								if(ownCities.contains(synonymCity)){
									jsonObj.put("success", false);
									// 将存在信息放入jsonObj的msg对象中
									jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
									return jsonObj;
								}
							}
					}*/
				/*}else {
					jsonObj.put("success", false);
					// 将存在信息放入jsonObj的msg对象中
					jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
					return jsonObj;
				}*/
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
				String newCiytName1 = newCityName;
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
								newCityName += "," + cityMap.get(ownCity);
								newHaveOld = false;
							}
						}
						//不包含原有地市
						if(!newHaveOld){
							//将原有地市全国改为地市集合
							if (oldCityCode.contains("全国")){
								oldCityCode = cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
								oldCityName = cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
							}
							if (!"".equals(ownCities)) {
								for(String ownCity : ownCities.split(",")){
									String cityName = cityMap.get(ownCity);
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
							c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, oldworditem, "基础",oldCityName1);
						}else {
							c = UserOperResource.updateWordCity(user,curwordclass, wordid, oldCityName, oldCityCode,oldworditem,oldCityName1);
						}
						if(cityMap.keySet().size()<=newCityCode.split(",").length){
							newCityName="全国";
							newCityCode="全国";
						}
						int d = UserOperResource.updateWordCity(user,curwordclass, newRs.getRows()[0].get("wordid").toString(), newCityName, newCityCode,newworditem,newCiytName1);
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
			}else{
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

		// 执行修改操作，返回修改事务处理结果
//		int c = _update(oldworditem, newworditem, oldtype, newtype, wordid,wordclassid);
//		User user = (User)sre;
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
				oldCityCode = cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
			}
			for(String ownCity : ownCities.split(",")){
				String cityName = cityMap.get(ownCity);
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
				c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, oldworditem, "基础",oldCityName1);
			}else {
				c = UserOperResource.updateWordCity(user,curwordclass, wordid, oldCityName, oldCityCode,oldworditem,oldCityName1);
			}
			List<String> worditemList = new ArrayList<String>();
			worditemList.add(newworditem);
			int d = UserOperResource.insertWord(user, wordclassid, curwordclass, curwordclasstype, worditemList, newtype, "基础", newCityCode, newCityName);
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
			// 判断数据源不为null且含有数据
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
		String sql = "update word set word=?,type=? , time = sysdate  where wordid=? ";
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
		sql = "update wordclass set time =sysdate  where  wordclassid = ? ";
		// 定义绑定参数集合
		lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(wordclassid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);

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
	 * 插入词条
	 *
	 * @param worditem参数词条
	 * @param curwordclass参数当前词类名称
	 * @param curwordclassid参数当前词类id
	 * @param isstandardword参数是否是标准词
	 * @return 插入返回的json串
	 */
	public static Object insert(String worditem, String curwordclass,
								String curwordclassid, String curwordclasstype,Boolean isstandardword,String citycode,String cityname) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}

		//String customer = user.getCustomer();

		/*if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		// 判断当前处理id是否为空，null
		if (curwordclassid == null || "".equals(curwordclassid)) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", "请选择当前词类!");
			return jsonObj;
		}
		if (citycode == null || "".equals(citycode)) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", "请选择所属地市!");
			return jsonObj;
		}
		// 用换行符拆分用户输入的多条词条,去除空字符串和空格等空白字符
		List<String> lstWorditem = new ArrayList<String>(Arrays.asList(worditem.split("\n")));
		// 获取该词条的type
		String type = "";
		if (isstandardword) {
			type = "标准名称";
		} else {
			type = "普通词";
		}
		// 循环遍历词条集合
		String msg ="";
		String msg1 = "";
		List<String> listWord= new ArrayList<String>();
		//判断用户是否有地市权限
		if(userCityCode == null || "".equals(userCityCode)){
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中66
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		int count = 0 ;
		for (int i = 0; i < lstWorditem.size(); i++) {
			String wd = lstWorditem.get(i);
			if(listWord.contains(wd)){
				if("".equals(msg)){
					msg="第";
				}
				msg = msg+(i + 1)+",";
				continue;
			}
			// 判断是否已存在相同词条
			if (Exists(curwordclassid, wd , type)) {
				Result rs = selectWordCity(curwordclassid, wd , type);
				String type1 = rs.getRows()[0].get("type").toString();
				if("标准名称".equals(type1)||"普通词".equals(type1)){
					//获取词条地市属性
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
									oldCityName += "," + cityMap.get(code);
									n = 1;
								}
							}
						}
						if (n > 0) {
							//更新地市属性
							if(cityMap.keySet().size()<=oldCityCode.split(",").length){
								oldCityCode="全国";
								oldCityName="全国";
							}
							int m = UserOperResource.updateWordCity(user,curwordclass, wordid, oldCityName, oldCityCode,wd,oldCityName1);
							if (m <= 0) {
								// 事务处理失败
								// 将false放入jsonObj的success对象中
								jsonObj.put("success", false);
								// 将失败信息放入jsonObj的msg对象中
								jsonObj.put("msg", "保存失败!");
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
				listWord.add(wd);
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
//	    sre = GetSession.getSessionByKey("accessUser");
//		User user = (User)sre;

		int c = 0;
		if(listWord.size()>0){
			// 执行SQL语句，获取事务处理的结果
			c = UserOperResource.insertWord(user, curwordclassid, curwordclass, curwordclasstype, listWord, type, "基础",citycode,cityname);
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
	 * @param lstWorditem参数词条集合
	 * @param type参数词条类型
	 * @return 保存返回的结果
	 */
	private static int _insert(String curwordclassid, String curwordclass,
							   List<String> lstWorditem, String type) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义保存词条的SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 循环遍历词条集合
		for (int i = 0; i < lstWorditem.size(); i++) {
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
			// 绑定词类名称参数
			lstpara.add(lstWorditem.get(i));
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
					+ "==>" + lstWorditem.get(i), "WORD"));
		}

		// 更新当前词类编辑时间
		sql = "update wordclass set time =sysdate  where  wordclassid = ? ";
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
		User user = (User)sre;
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}

		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		//判断用户权限
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		/*String customer = user.getCustomer();
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		//获取当前词条所有地市
		JSONObject jsonObject = (JSONObject)selectWordCity(curwordclass, wordid);
		String oldCityCode = "".equals(jsonObject.getString("citycode")) ? "全国" : jsonObject.getString("citycode");
		String oldCityName = "".equals(jsonObject.getString("cityname")) ? "全国" : jsonObject.getString("cityname");
		String oldCityName1 = oldCityName;
		//如果用户为全国权限，直接删除
		if (userCityCode.contains("全国")) {
			oldCityCode = "";
		}else {
			if (oldCityCode.contains("全国")) {
				oldCityCode = cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
			}
			boolean delete = false;
			//原有地市中删除用户有权限的地市
			for (String userCode : userCityCode.split(",")) {
				String userCityName = cityMap.get(userCode);
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
			c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, worditem, "基础", oldCityName1);
		}else {
			//去除后词条地市不为空，更新词条地市属性
			c = UserOperResource.updateWordCity(user, curwordclass, wordid, oldCityName, oldCityCode,worditem,oldCityName1);
			//获取词条下所有别名
			Integer count1 = UserOperResource.getSynonymCount("", false, true, true, "", worditem, curwordclass, curwordclasstype, "");
			if (count1 > 0){
				Result rs = UserOperResource.selectSynonym(0, count1, "", false, true, true, "", worditem, curwordclass,"基础", "");
				for (int i = 0; i < rs.getRowCount(); i++) {
					int count = 0;
					int d = 0;
					//获取别名地市属性
					String cityCode = rs.getRows()[i].get("city") == null ? "全国" : rs.getRows()[i].get("city").toString();
					String cityName = rs.getRows()[i].get("cityname") == null ? "全国" : rs.getRows()[i].get("cityname").toString();
					String cityname1 = cityName;
					//如果地市为全国改为所有地市集合
					if ("全国".equals(cityCode)) {
						cityCode = cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
						cityName = cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
					}
					//别名地市中去除用户权限地市
					for (String userCity : userCityCode.split(",")) {
						String userCityName = cityMap.get(userCity);
						if (cityCode.contains(userCity)) {
							cityCode = MyUtil.removeCity(cityCode, userCity);
							cityName = MyUtil.removeCity(cityName, userCityName);
							count ++;
						}
					}
					if (count > 0){
						if ("".equals(cityCode)) {
							d = UserOperResource.deleteSynonyms(user, rs.getRows()[i].get("wordid").toString(), rs.getRows()[i].get("word").toString(), worditem, curwordclass, cityname1);
						}else {
							d = UserOperResource.updateSynonymCity(user,curwordclass, worditem, rs.getRows()[i].get("wordid").toString(), cityName, cityCode,rs.getRows()[i].get("word").toString(),cityname1);
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
	 *@description  查询词条city
	 *@param wordclass 词类
	 *@param wordid 词条ID
	 *@return
	 *@returnType Object
	 */
	public static Object selectWordCity(String wordclass ,String wordid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Result rs = UserOperResource.selectWordCity(wordclass,wordid);
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			String cityName = rs.getRows()[0].get("cityname")==null ? "全国":rs.getRows()[0].get("cityname").toString();
			String cityCode = rs.getRows()[0].get("city")==null ? "全国":rs.getRows()[0].get("city").toString();
			while(cityName.contains(",,")){
				cityName = cityName.replace(",,", ",");
			}
			if(cityName.startsWith(",")){
				cityName = cityName.replaceFirst(",", "");
			}
			if(cityName.endsWith(",")){
				cityName = cityName.substring(0, cityName.length()-1);
			}
			while(cityCode.contains(",,")){
				cityCode = cityCode.replace(",,", ",");
			}
			if(cityCode.startsWith(",")){
				cityCode = cityCode.replaceFirst(",", "");
			}
			if(cityCode.endsWith(",")){
				cityCode = cityCode.substring(0, cityCode.length()-1);
			}
			if(cityName.contains("删除标识符")){
				cityName = cityName.replace("删除标识符", "");
				if(cityName.startsWith(",")){
					cityName = cityName.replaceFirst(",", "");
				}
			}
			// 生成id对象
			jsonObj.put("usercity", userCityCode);
			jsonObj.put("cityname", cityName);
			jsonObj.put("citycode", rs.getRows()[0].get("city")==null ? "全国":rs.getRows()[0].get("city"));
			jsonObj.put("success", true);
		}else{
			jsonObj.put("cityname", "");
			jsonObj.put("citycode", "");
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
		String sql = "select * from word where rownum<2 and wordclassid=? and lower(word)=lower(?)";
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
	public static Object updateWordCity(String wordclass ,String wordid,String cityNme,String cityCode) {


		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();

		if(cityCode.contains("全选")){
			if (cityCode.contains("全选,")) {
				cityCode = cityCode.replace("全选,", "");
				cityNme = cityNme.replace("全选,", "");
			}else {
				cityCode = cityCode.replace("全选", "");
				cityNme = cityNme.replace("全选", "");
			}

		}
		if ("".equals(cityCode)) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "地市不能为空!");
			return jsonObj;
		}
		Object sre = GetSession.getSessionByKey("accessUser");
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
		String oldCityCode = "";
		String oldCityName = "";
		//获取当前词条原地市信息
		Result rs = UserOperResource.selectWordCity(wordclass,wordid);
		String oldCityName1 = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
		String worditem = rs.getRows()[0].get("word").toString();
		//全国权限用户直接修改地市，如果修改的地市包含全国则地市为全国
		if (userCityCode.contains("全国")) {
			if(cityCode.contains("全国")){
				oldCityCode = "全国";
				oldCityName = "全国";
			}else {
				oldCityCode = cityCode;
				oldCityName = cityNme;
			}
		}else {
			oldCityCode = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
			oldCityName = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
			//获取用户城市和原地市的交集，用来确认是否有删除城市
			String inCity = "";
			//如果原地市为全国，交集为用户权限地市
			if (oldCityCode.contains("全国")) {
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
				oldCityCode = cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
			}
			if (!"".equals(inCity)){
				/*if("全国".equals(inCity)){
					Result synonymRs = UserOperResource.getSynonymCount("", false, true, null, worditem, wordclass, "基础", "");
					Result synonymRs1 = UserOperResource.getSynonymCount("", false, true, null, worditem, wordclass, "子句", "");
					if(synonymRs.getRowCount()>0||synonymRs1.getRowCount()>0){
						jsonObj.put("success", false);
						jsonObj.put("msg", "请先修改别名地市！");
						return jsonObj;
					}
				}else{*/
				//循环交集地市，看是否有删除地市
				for (String inCityCode : inCity.split(",")){
						/*//如果修改后的地市不包含交集地市，则表示删除该城市
						if (!cityCode.contains(inCityCode)) {
							//获取词条下的所有别名，判断别名下是否有该删除地市，有的话提示无法修改
							Result synonymRs = UserOperResource.getSynonymCount("", false, true, null, worditem, wordclass, "基础", "");
							if (synonymRs.getRowCount()>0) {
								synonymRs = UserOperResource.selectSynonym(0, synonymRs.getRowCount(), "", false, true, "", worditem, wordclass,"基础", "");
								for(int i = 0 ; i < synonymRs.getRowCount() ; i++){
									String synonymCitiyCode = synonymRs.getRows()[i].get("city") == null ? "全国" : synonymRs.getRows()[i].get("city").toString();
									//判断别名中是否包含删除地市，有的话提示无法修改
									if (synonymCitiyCode.contains("全国") || synonymCitiyCode.contains(inCityCode)) {
										jsonObj.put("success", false);
										jsonObj.put("msg", "请先修改别名地市！");
										return jsonObj;
									}
								}
							}else{
								Result synonymRs1 = UserOperResource.getSynonymCount("", false, true, null, worditem, wordclass, "子句", "");
								if (synonymRs1.getRowCount()>0) {
									synonymRs1 = UserOperResource.selectSynonym(0, synonymRs.getRowCount(), "", false, true, "", worditem, wordclass,"子句", "");
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
							}
						}*/

					//原词条地市去除交集地市
					oldCityCode = MyUtil.removeCity(oldCityCode, inCityCode);
					oldCityName = MyUtil.removeCity(oldCityName, cityMap.get(inCityCode));
//					}
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

		if(cityMap.keySet().size()<=oldCityCode.split(",").length){
			oldCityCode="全国";
			oldCityName="全国";
		}
		if (!"全国".equals(oldCityCode)) {
			//获取词条下的所有别名，判断别名下是否有该删除地市，有的话提示无法修改
			Integer count = UserOperResource.getSynonymCount("", false, true, true, null, worditem, wordclass, "基础", "");
			if (count>0) {
				Result synonymRs = UserOperResource.selectSynonym(0, count, "", false, true, true, "", worditem, wordclass,"基础", "");
				for(int i = 0 ; i < synonymRs.getRowCount() ; i++){
					String synonymCitiyCode = synonymRs.getRows()[i].get("city") == null ? "全国" : synonymRs.getRows()[i].get("city").toString();
					if(synonymCitiyCode.contains("全国")){
						jsonObj.put("success", false);
						jsonObj.put("msg", "请先修改别名地市！");
						return jsonObj;
					}
					//判断别名中是否包含删除地市，有的话提示无法修改
					for (String synoymCode : synonymCitiyCode.split(",")) {
						if (!oldCityCode.contains(synoymCode)) {
							jsonObj.put("success", false);
							jsonObj.put("msg", "请先修改别名地市！");
							return jsonObj;
						}
					}

				}
			}else{
				Integer count1 = UserOperResource.getSynonymCount("", false, true, true, null, worditem, wordclass, "子句", "");
				if (count1>0) {
					Result synonymRs1 = UserOperResource.selectSynonym(0, count1, "", false, true, true, "", worditem, wordclass,"子句", "");
					for(int i = 0 ; i < synonymRs1.getRowCount() ; i++){
						String synonymCitiyCode = synonymRs1.getRows()[i].get("city") == null ? "全国" : synonymRs1.getRows()[i].get("city").toString();
						if(synonymCitiyCode.contains("全国")){
							jsonObj.put("success", false);
							jsonObj.put("msg", "请先修改别名地市！");
							return jsonObj;
						}
						//判断别名中是否包含删除地市，有的话提示无法修改
						for (String synoymCode : synonymCitiyCode.split(",")) {
							if (!oldCityCode.contains(synoymCode)) {
								jsonObj.put("success", false);
								jsonObj.put("msg", "请先修改别名地市！");
								return jsonObj;
							}
						}
					}
				}
			}
		}
		int c  = UserOperResource.updateWordCity(user, wordclass, wordid, oldCityName, oldCityCode, worditem, oldCityName1);

		// 判断数据源不为null且含有数据
		if (c>0) {
			// 生成id对象
			jsonObj.put("msg", "更新成功!");
			jsonObj.put("success", true);
			jsonObj.put("cityName", oldCityName);
		}else{
			jsonObj.put("msg", "更新失败!");
			jsonObj.put("success", false);
		}

		return jsonObj;
	}
	public static Object getCity(){
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode = HttpClient.sendGet(getConfigValue.getDatabase("limitUrl"), "userId="+
				user.getUserID());
		System.out.println(cityCode);
		JSONObject jo = JSONObject.parseObject(cityCode);
		if ("success".equals(jo.getString("resultCode"))){
			String city = jo.getString("cityCode");
			city = city.replace("[", "").replace("]", "");
			city = city.replace("\"", "");
			if (UserCity.cityCodes == null) {
				UserCity.cityCodes = new HashMap<String, String>();
			}
			UserCity.cityCodes.put(user.getUserID(), city);
			String cityname = "";
			for (String code : city.split(",")){
				if ("全国".equals(code)) {
					cityname = "全国";
					break;
				}
				if ("".equals(cityname)) {
					cityname = cityMap.get(code);
				}else {
					cityname += "," + cityMap.get(code);
				}
			}
			jsonObj.put("cityname", cityname);
			jsonObj.put("citycode", city);
			jsonObj.put("msg", "获取用户权限成功!");
			jsonObj.put("success", true);
		}else {
			jsonObj.put("msg", "获取用户权限失败!");
			jsonObj.put("success", false);
		}

		return jsonObj;
	}
}


