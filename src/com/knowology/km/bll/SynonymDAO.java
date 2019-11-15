package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import oracle.net.aso.a;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.entity.UserCity;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

public class SynonymDAO {
	/**
	 * 带分页的查询满足条件的别名名称
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param synonym参数别名名称
	 * @param isprecise参数是否精确查询
	 * @param iscurrentworditem参数是否当前词条
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return json串
	 */
	public static Object select(int start, int limit, String synonym,
			Boolean isprecise, Boolean iscurrentworditem, Boolean iscurrentwordclass, String type,
			String curworditem, String curwordclass, String citycode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		Integer count = UserOperResource.getSynonymCount(synonym, isprecise, iscurrentworditem, iscurrentwordclass, type, curworditem, curwordclass,"基础",citycode);
			// 判断数据源不为null且含有数据
			if (count > 0) {
				// 将条数放入jsonObj的total对象中
				jsonObj.put("total", count);
				// 执行带分页的查询满足条件的SQL语句
				Result rs = UserOperResource.selectSynonym(start, limit, synonym, isprecise, iscurrentworditem, iscurrentwordclass, type, curworditem, curwordclass,"基础", citycode);
				if (rs != null && rs.getRowCount() > 0) {
					Object sre = GetSession.getSessionByKey("accessUser");
					User user = (User)sre;
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
						// 生成id对象
						obj.put("id", start + i + 1);
						// 生成worditem对象
						obj.put("worditem", rs.getRows()[i].get("worditem"));
						// 生成synonym对象
						obj.put("synonym", rs.getRows()[i].get("word"));
						// 生成type对象
						obj.put("type", rs.getRows()[i].get("type"));
						// 生成wordclass对象
						obj.put("wordclass", rs.getRows()[i].get("wordclass"));
						// 生成wordid对象
						obj.put("wordid", rs.getRows()[i].get("wordid"));
						// 生成stdwordid对象
						obj.put("stdwordid", rs.getRows()[i].get("stdwordid"));
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
	 * 更新别名
	 * 
	 * @param oldsynonym参数原有别名
	 * @param newsynonym参数新的别名
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordid参数别名id
	 * @param stdwordid参数词条id
	 * @return 更新返回的json串
	 */
	public static Object update(String oldsynonym, String newsynonym,
			String oldtype, String newtype, String wordid, String stdwordid, String curworditem, String curwordclass) {
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
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		/*String customer = user.getCustomer();
		
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		//获取要修改的别名及地市
		Result oldRs = UserOperResource.getSynonymCity(curwordclass, wordid);
		String wordclassid = oldRs.getRows()[0].get("wordclassid").toString();
		String oldCityCode = oldRs.getRows()[0].get("city") == null ? "全国" : oldRs.getRows()[0].get("city").toString();
		String oldCityName = oldRs.getRows()[0].get("cityname") == null ? "全国" : oldRs.getRows()[0].get("cityname").toString();
		String oldcityname1 = oldCityName;
		//用户有权限的城市和词条地市的交集
		String ownCities = ""; 
		//用户权限不为全国且原词条地市不为全国
		if (!userCityCode.contains("全国") && !oldCityCode.contains("全国")){
				//循环原词条地市属性和用户权限地市是否有交集
			for(String oldCity : oldCityCode.split(",")){
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
		if (userCityCode.contains("全国") && !oldCityCode.contains("全国")) {
			ownCities = oldCityCode;
		}
		if (!userCityCode.contains("全国") && oldCityCode.contains("全国")){
			ownCities = userCityCode;
		}
		// 判断是否更新的是别名名称
		if (!newsynonym.equals(oldsynonym)) {
			// 判断是否已存在相同词类
//			if (Exists(stdwordid, newsynonym)) {
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将别名重复信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "别名已存在!");
//				return jsonObj;
//			}
			if(Exists(wordclassid, newsynonym)){
				String newcityname1 = "";
				String newCityName2 = "";
				String newCityCode2 = "";
				String existCity = "";
				int update = 0;
				//更改后别名地市是否包含改之前的地市
				boolean newHaveOld = true;
				//获取该别名
				Result rs = selectSynonym(wordclassid, newsynonym);
				String type1 = rs.getRows()[0].get("type").toString();
				if(!"标准名称".equals(type1)&&!"普通词".equals(type1)){
					String allNewCityCode = "";
					String allNewCityName = "";
					for(int i = 0; i < rs.getRowCount(); i++){
						// 获取别名地市
						String newCityCode = rs.getRows()[i].get("city") == null ? "全国" : rs.getRows()[i].get("city").toString();
						String newCityName = rs.getRows()[i].get("cityname") == null ? "全国" : rs.getRows()[i].get("cityname").toString();
						String stdId = rs.getRows()[i].get("stdwordid") == null ? "": rs.getRows()[i].get("stdwordid").toString();
						if (stdId.equals(stdwordid)) {
							newcityname1 = newCityName;
							newCityName2 = newCityName;
							newCityCode2 = newCityCode;
							update = i;
						}
						if (!newCityCode.contains("全国")) {
							if("".equals(allNewCityCode)){
								allNewCityCode += newCityCode;
								allNewCityName += newCityName;
							}else {
								allNewCityCode += "," + newCityCode;
								allNewCityName += "," + newCityName;
							}
						}else {
							allNewCityCode = "全国";
							break;
						}
					}
					//如果已存在别名地市为全国，则提示已存在
					if (!"全国".equals(allNewCityCode)&&allNewCityCode.split(",").length < WorditemDAO.cityMap.keySet().size()) {
						if (oldCityCode.contains("全国") && userCityCode.contains("全国")) {
							ownCities = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
						}
						//循环要修改的地市
						for (String ownCity : ownCities.split(",")) {
							//如果已存在别名地市不包含要修改的地市
							if (!allNewCityCode.contains(ownCity)) {
								if("".equals(newCityCode2)){
									newCityCode2 +=  ownCity;
									newCityName2 +=  WorditemDAO.cityMap.get(ownCity);
									newHaveOld = false;
								}else {
									newCityCode2 += "," + ownCity;
									newCityName2 += "," + WorditemDAO.cityMap.get(ownCity);
								}
							}else {
								if ("".equals(existCity)) {
									existCity += WorditemDAO.cityMap.get(ownCity);
								}else {
									existCity += "," + WorditemDAO.cityMap.get(ownCity);
								}
							}
						}
						// 如果原别名地市为全国且用户权限为全国则将新别名地市改为全国
						if (oldCityCode.contains("全国") && userCityCode.contains("全国")) {
							oldCityCode = "";
						} else {
							// 不包含原有地市
							if (!newHaveOld) {
								// 将原有地市全国改为地市集合
								if (oldCityCode.contains("全国")) {
									oldCityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
									oldCityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
								}
								if (!"".equals(ownCities)) {
									for (String ownCity : ownCities.split(",")) {
										String cityName = WorditemDAO.cityMap
												.get(ownCity);
										if (oldCityCode.contains(ownCity)) {
											// 原地市去除修改的地市
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
							int d = 0;
							// 去除交集地市后原别名地市为空，删除该别名，不为空则将原别名地市去除修改的地市。
							if ("".equals(oldCityCode)) {
								c = UserOperResource.deleteSynonyms(user,
										wordid, oldsynonym, curworditem,
										curwordclass,oldcityname1);
							} else {
								c = UserOperResource.updateSynonymCity(user,
										curwordclass, curworditem, wordid, oldCityName,
										oldCityCode,oldsynonym,oldcityname1);
							}
							if(WorditemDAO.cityMap.keySet().size()<=newCityCode2.split(",").length){
								newCityName2="全国";
								newCityCode2="全国";
							}
							if("".equals(newcityname1)){
								List<String> lstSynonym = new ArrayList<String>();
								lstSynonym.add(newsynonym);
								d = UserOperResource.insertSynonym(user, wordclassid, lstSynonym, stdwordid, newtype, curworditem, curwordclass, newCityCode2, newCityName2);
							}else{
								d = UserOperResource.updateSynonymCity(user,curwordclass, curworditem, rs.getRows()[update].get("wordid").toString(), newCityName2, newCityCode2,newsynonym,newcityname1);
							}
							// 判断事务处理结果
							if (c > 0 && d > 0) {
								// 事务处理成功
								// 将true放入jsonObj的success对象中
								jsonObj.put("success", true);
								if("".equals(existCity)){
									// 将成功信息放入jsonObj的msg对象中
									jsonObj.put("msg", "修改成功!");
								}else {
									jsonObj.put("msg", "修改成功!"+existCity+"在其他词条的别名中已存在");
								}
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
					jsonObj.put("msg", "别名在词条中已存在!");
					return jsonObj;
				}
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将别名重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "别名已存在!");
				return jsonObj;
			}
			
		}
		String newCityCode = "";
		String newCityName = "";
		// 如果是全国用户，直接修改
		if (userCityCode.contains("全国")) {
			int d = UserOperResource.updateSynonym(user, oldsynonym,
					newsynonym, oldtype, newtype, wordid, stdwordid,
					curwordclass);
			if (d > 0) {
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
		} else {
			if (oldCityCode.contains("全国")) {
				oldCityCode = WorditemDAO.cityMap.keySet().toString()
						.replace(" ", "").replace("[", "").replace("]", "");
				oldCityName = WorditemDAO.cityMap.values().toString()
						.replace(" ", "").replace("[", "").replace("]", "");
			}
			for (String ownCity : ownCities.split(",")) {
				String cityName = WorditemDAO.cityMap.get(ownCity);
				if (oldCityCode.contains(ownCity)) {
					oldCityCode = MyUtil.removeCity(oldCityCode, ownCity);
				}
				if (oldCityName.contains(cityName)) {
					oldCityName = MyUtil.removeCity(oldCityName, cityName);
				}
				if (newCityCode != "") {
					newCityCode += "," + ownCity;
					newCityName += "," + cityName;
				} else {
					newCityCode += ownCity;
					newCityName += cityName;
				}
			}

			int c = 0;
			if ("".equals(oldCityCode)) {
				c = UserOperResource.deleteSynonyms(user, wordid,
						oldsynonym, curworditem, curwordclass, oldcityname1);
			} else {
				c = UserOperResource.updateSynonymCity(user,curwordclass,curworditem,
						wordid, oldCityName, oldCityCode,oldsynonym,oldcityname1);
			}
			List<String> lstSynonym = new ArrayList<String>();
			lstSynonym.add(newsynonym);
			int d = UserOperResource.insertSynonym(user, oldRs.getRows()[0]
					.get("wordclassid").toString(), lstSynonym, stdwordid,
					newtype, curworditem, curwordclass, newCityCode,
					newCityName);
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
	 * 判断别名是否重复
	 * 
	 * @param stdwordid参数词条id
	 * @param synonym参数别名名称
	 * @return 是否重复
	 */
	public static Boolean Exists(String curwordclassid, String synonym) {
		// 对应绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 定义查询别名的SQL语句
		String sql = "select word from word where rownum<2 and wordclassid=? and lower(word)=lower(?)";
		// 绑定词类id参数
		lstpara.add(curwordclassid);
		// 绑定别名名称参数
		lstpara.add(synonym);
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 有数据，表示重复
				return true;
			} else {
				// 没有数据，不是不重复
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			return false;
		}
	}

	/**
	 * 新增别名
	 * 
	 * @param wordclassid参数词类id
	 * @param synonyms参数多个别名名称
	 * @param type参数别名类型
	 * @param stdwordid参数词条ID
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return 新增返回的json串
	 */
	public static Object insert(String wordclassid, String synonym,
			String type, String stdwordid, String curworditem,
			String curwordclass,String citycode,String cityname) {
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
		//String customer = user.getCustomer();
		
		/*if(!"全行业".equals(customer)){//临时限制省级用户编辑
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
		
		// 用换行符拆分用户输入的多条别名,去除空字符串和空格等空白字符
		List<String> lstSynonym = Arrays.asList(synonym.split("\n"));
		List<String> listWord= new ArrayList<String>();
		String msg = "";
		String msg1 = "";
		String msg2 = "";
		int count = 0 ;
		// 循环遍历别名集合
		for (int i = 0; i < lstSynonym.size(); i++) {
			// 判断是否已存在相同别名
//			if (Exists(stdwordid, lstSynonym.get(i))) {
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将重复信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "第" + (i + 1) + "条别名已存在!");
//				return jsonObj;
//			}
			String aString = lstSynonym.get(i);
			if(listWord.contains(aString)){
				if("".equals(msg)){
					msg="第";	
				}
				msg = msg + (i + 1) + ",";
				continue;
			}
			if(Exists(wordclassid,lstSynonym.get(i))){
				//获取该别名
				//Result rs = UserOperResource.selectSynonym(0, 2, lstSynonym.get(i), true, true, type, curworditem, curwordclass, "基础", "");
				Result rs = selectSynonym(wordclassid, lstSynonym.get(i));
				String type1 = rs.getRows()[0].get("type").toString();
				if(!"标准名称".equals(type1)&&!"普通词".equals(type1)){
					String allOldCityCode = "";
					String allOldCityName = "";
					String oldcityname1 = "";
					String newCityCode = "";
					String newCityName = "";
					String existWord = "";
					int update = 0;
					for(int j = 0; j < rs.getRowCount(); j++){
						// 获取别名地市
						String oldCityCode = rs.getRows()[j].get("city") == null ? "全国" : rs.getRows()[j].get("city").toString();
						String oldCityName = rs.getRows()[j].get("cityname") == null ? "全国" : rs.getRows()[j].get("cityname").toString();
						if(oldCityCode.contains("全国")){
							allOldCityCode = "全国";
							break;
						}
						String stdid = rs.getRows()[j].get("stdwordid") == null ? "全国" : rs.getRows()[j].get("stdwordid").toString();
						if(stdwordid.equals(stdid)){
							oldcityname1 = oldCityName;
							newCityCode = oldCityCode;
							newCityName = oldCityName;
							update = j;
						}
						if(allOldCityCode.equals("")){
							allOldCityCode += oldCityCode;
							allOldCityName += oldCityName;
						}else {
							allOldCityCode += "," + oldCityCode;
							allOldCityName += "," + oldCityName;
						}
					}
					// 如果原别名地市包含全国，则提示已存在
					if (!allOldCityCode.contains("全国") && allOldCityCode.split(",").length < WorditemDAO.cityMap.keySet().size()) {
						int n = 0;
						if (citycode.contains("全国")) {
							citycode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
						} 
						for (String code : citycode.split(",")) {
							if (!allOldCityCode.contains(code)) {
								if ("".equals(newCityCode)) {
									newCityCode +=  code;
									newCityName +=  WorditemDAO.cityMap.get(code);
								}else{
									newCityCode += "," + code;
									newCityName += "," + WorditemDAO.cityMap.get(code);
								}
								n = 1;
							}else {
								if("".equals(existWord)){
									existWord = WorditemDAO.cityMap.get(code);
								}else {
									existWord += "," + WorditemDAO.cityMap.get(code);
								}
							}
						}
						
						if (n > 0) {
							if(WorditemDAO.cityMap.keySet().size()<=newCityCode.split(",").length){
								newCityCode="全国";
								newCityName="全国";
							}
							int m = 0;
							if ("".equals(oldcityname1)) {
								List<String> lst2 = new ArrayList<String>();
								lst2.add(lstSynonym.get(i));
								m = UserOperResource.insertSynonym(user, wordclassid, lst2, stdwordid, type, curworditem, curwordclass, newCityCode, newCityName);
							}else{
								m = UserOperResource.updateSynonymCity(user, curwordclass, curworditem, rs.getRows()[update].get("wordid").toString(), newCityName, newCityCode,lstSynonym.get(i),oldcityname1);
							}
							if (m <= 0) {
								// 将false放入jsonObj的success对象中
								jsonObj.put("success", false);
								// 将信息放入jsonObj的msg对象中
								jsonObj.put("msg", msg);
								return jsonObj;
							}
							if(!"".equals(existWord)){
								msg2 += "第" + (i+1) +"条别名" + existWord +"在其他词条下已存在<br>";
							}
							count++;
							continue;
						}
					}
					if ("".equals(msg)) {
						msg = "第";
					}
					msg = msg + (i + 1) + ",";
				}else {
					if("".equals(msg1)){
						msg1="第";	
					}
					msg1 = msg1+(i + 1)+",";
				}
			}else{
				listWord.add(lstSynonym.get(i));	
			}	
		}
		if (msg.length() > 1) {
			msg = msg.substring(0, msg.lastIndexOf(","));
			msg = msg + "条别名已存在!";
		}
		if(msg1.length()>1){
			msg1 = msg1.substring(0, msg1.lastIndexOf(","));
			msg1 =msg1 +"条别名在词条中已存在!";
		}
		// 执行新增操作，返回新增事务的结果
//		int c = _insert(wordclassid, lstSynonym, stdwordid, type, curworditem,
//				curwordclass);
		int c = 0;
		if (listWord.size() > 0) {
			c = UserOperResource.insertSynonym(user, wordclassid, listWord, stdwordid, type, curworditem, curwordclass,citycode, cityname);
		}
		// 判断事务处理结果
		if (c > 0 || count > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", count+listWord.size()+"条别名保存成功!<br>"+msg+"<br>"+msg1+"<br>"+msg2);
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!<br>"+msg+"<br>"+msg1+"<br>"+msg2);
		}
		return jsonObj;
	}

	/**
	 * 新增别名的操作
	 * 
	 * @param wordclassid参数词类id
	 * @param lstSynonym参数别名集合
	 * @param stdwordid参数词条id
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return 新增返回的结果
	 */
	private static int _insert(String wordclassid, List<String> lstSynonym,
			String stdwordid, String type, String curworditem,
			String curwordclass) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义保存别名的SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 循环遍历别名集合
		for (int i = 0; i < lstSynonym.size(); i++) {
			// 定义新增别名的SQL语句
			sql = "insert into word(wordid,wordclassid,word,stdwordid,type) values(?,?,?,?,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 获取别名的序列值
			String id = String.valueOf(SeqDAO.GetNextVal("seq_word_id"));
			// 绑定id参数
			lstpara.add(id);
			// 绑定词类id参数
			lstpara.add(wordclassid);
			// 绑定别名参数
			lstpara.add(lstSynonym.get(i));
			// 绑定词条id参数
			lstpara.add(stdwordid);
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
			lstlstpara.add(MyUtil.LogParam(" ", " ", "增加别名", "上海", curwordclass
					+ "==>" + curworditem + "==>" + lstSynonym.get(i), "WORD"));
		}
		
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
		
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 删除别名
	 * @param stdwordid参数别名id
	 * @param synonym参数别名名称
	 * @param curworditem参数词条名称
	 * @param curwordclass参数词类名称
	 * @return 删除返回的json串
	 */
	public static Object delete(String stdwordid, String synonym,
			String curworditem, String curwordclass) {
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
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		//获取该别名地市
		String[] ids = stdwordid.split(",");
		String msg = "";
		int i = 1;
		int c = 0;
		for (String id : ids) {
			Result rs = UserOperResource.getSynonymCity(curwordclass, id);
			String cityCode = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
			String cityName = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
			String cityname1 = cityName;
			if (userCityCode.contains("全国")) {
				cityCode = "";
			}else {
				if (cityCode.contains("全国")) {
					cityCode = WorditemDAO.cityMap.keySet().toString().replace(" ", "").replace("[", "").replace("]", "");
					cityName = WorditemDAO.cityMap.values().toString().replace(" ", "").replace("[", "").replace("]", "");
				}
				boolean delete = false;
				//原有地市中删除用户有权限的地市
				for (String userCode : userCityCode.split(",")) {
					String userCityName = WorditemDAO.cityMap.get(userCode);
					if (cityCode.contains(userCode)) {
						cityCode = MyUtil.removeCity(cityCode, userCode);
						delete = true;
					}
					if (cityName.contains(userCityName)) {
						cityName = MyUtil.removeCity(cityName, userCityName);
					}
				}
				if (!delete) {
					if("".equals(msg)){
						msg="第";	
					}
					msg = msg+ i +",";
					i++;
					continue;
				}
			
			}
			if ("".equals(cityCode)) {
				c = UserOperResource.deleteSynonyms(user, id, rs.getRows()[0].get("word").toString(), curworditem, curwordclass,cityname1);
			}else {;
				c = UserOperResource.updateSynonymCity(user,curwordclass, curworditem, id, cityName, cityCode,rs.getRows()[0].get("word").toString(),cityname1);
			}
			i++;
		}
		if(msg.length()>1){
			msg = msg.substring(0, msg.lastIndexOf(","));
			msg =msg +"条别名不包含本省地市!";
		}
		
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!<br>"+msg);
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!<br>"+msg);
		}
		return jsonObj;
	}
	/**
	 *  更新别名地市
	 * @param wordclass 词类名称
	 * @param wordid 修改的别名id
	 * @param cityCode 城市编码
	 * @param cityName 城市名称
	 * @return
	 */
	public static Object updateSynonymCity(String wordclass, String wordid, String cityCode, String cityName){
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		if(cityCode.contains("全选")){
			if (cityCode.contains("全选,")) {
				cityCode = cityCode.replace("全选,", "");
				cityName = cityName.replace("全选,", "");
			}else {
				cityCode = cityCode.replace("全选", "");
				cityName = cityName.replace("全选", "");
			}
			
		}
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		/*String customer = user.getCustomer();
		
		if(!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}*/
		if(!user.isPower()){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		String userCityCode = UserCity.cityCodes.get(user.getUserID());
		if(userCityCode == null || "".equals(userCityCode)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "请联系管理员配置用户权限!");
			return jsonObj;
		}
		Result rs = UserOperResource.getSynonymCity(wordclass, wordid);
		if(rs.getRowCount()>0){
			String stdid = rs.getRows()[0].get("stdwordid").toString();
			Result stRs = UserOperResource.selectWordCity(wordclass, stdid);
			String curworditem = stRs.getRows()[0].get("word").toString();
			String oldcityname1= rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
			String oldCityCode = "";
			String oldCityName = "";
			if (userCityCode.contains("全国")) {
				if (!cityCode.contains("全国")) {
					oldCityCode = cityCode;
					oldCityName = cityName;
				} else {
					oldCityCode = "全国";
					oldCityName = "全国";
				}
			}else {
				//获取当前别名原地市信息
				
				oldCityCode = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
				oldCityName = rs.getRows()[0].get("cityname") == null ? "全国" : rs.getRows()[0].get("cityname").toString();
				//获取用户权限地市和原地市的交集，用来确认是否有删除城市
				String inCity = "";
				//如果原笔名为全国，则交集为用户权限地市
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
						//原词条地市去除交集地市
						oldCityCode = MyUtil.removeCity(oldCityCode, inCityCode);
						oldCityName = MyUtil.removeCity(oldCityName, WorditemDAO.cityMap.get(inCityCode));
					}
				}
				//去除后如果为空
				if ("".equals(oldCityCode)){
					oldCityCode = cityCode;
					oldCityName = cityName;
				}else {
					oldCityCode += "," + cityCode;
					oldCityName += "," + cityName;
				}
			}
			if(WorditemDAO.cityMap.keySet().size()<=oldCityCode.split(",").length){
				oldCityCode="全国";
				oldCityName="全国";
			}
			int c  = UserOperResource.updateSynonymCity(user,wordclass, curworditem, wordid, oldCityName, oldCityCode,rs.getRows()[0].get("word").toString(),oldcityname1);
			
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
		}else{
			jsonObj.put("msg", "更新失败!");
			jsonObj.put("success", false);
		}
		return jsonObj;
	}
	/**
	 * 根据词类id和别名名称查询别名
	 * @param curwordclassid 词类id
	 * @param worditem	别名名称
	 * @return
	 */
	public static Result selectSynonym(String curwordclassid, String worditem){
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 查询词条是否重复的SQL语句
		String sql = "select * from word where rownum<50 and wordclassid=? and lower(word)=lower(?)";
		// 绑定词类id参数
		lstpara.add(curwordclassid);
		// 绑定词条参数
		lstpara.add(worditem);
		// 执行SQL语句，获取相应的数据源
		Result rs = Database.executeQuery(sql, lstpara.toArray());
		return rs;
			
	}
}