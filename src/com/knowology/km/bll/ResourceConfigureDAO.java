package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.bll.CommonLibIndustryApplicationToServicesDAO;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.bll.CommonLibServiceDAO;

/**
 * @author ll
 * 
 */
public class ResourceConfigureDAO {

	/**
	 * 定义全局 cityCodeToCityName字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();

	/**
	 * 定义全局 cityNameToCityCode 字典
	 */
	public static Map<String, String> cityNameToCityCode = new HashMap<String, String>();
	static {

		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");
		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? ""
						: r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r
						.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
				cityNameToCityCode.put(key, value);
			}
		}
	}

	/**
	 * 获得行业下拉框数据
	 * 
	 * @return
	 */
	public static Object createIndustryCombobox() {
		Result rs = CommonLibIndustryApplicationToServicesDAO.getIndustry();
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 获得商家下拉框数据
	 * 
	 * @param industry
	 * @return
	 */
	public static Object createOrganizationByIndustry(String industry) {
		Result rs = CommonLibIndustryApplicationToServicesDAO
				.getOrganizationByIndustry(industry);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 获得应用下拉框数据
	 * 
	 * @param industry
	 * @return
	 */
	public static Object createpplicationByIndustryAndOrganization(
			String industry, String organization) {
		Result rs = CommonLibIndustryApplicationToServicesDAO
				.getApplicationByIndustryAndOrganization(industry, organization);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 构造业务树
	 * 
	 * @param serviceid
	 * @param industry
	 * @param organization
	 * @param application
	 * @return Object
	 */
	public static Object createServiceTree(String serviceid, String industry,
			String organization, String application) {
		String brand = getServiceRoot(industry,organization,application);
		if (brand==null || "".equals(brand)) {
			return new JSONArray();
		}
		return getTreeData(serviceid, brand);
	}
	
	/**
	 * 构造业务树所有节点
	 * 
	 * @param roleid
	 * @param industry
	 * @param organization
	 * @param application
	 * @param resourceType
	 * @return Object
	 */
	public static Object createServiceTreeAll(String roleid, String industry,
			String organization, String application,String resourceType) {
		JSONArray menuTree = new JSONArray();
		String brand="";
		if("service".equalsIgnoreCase(resourceType)){
		  brand = getServiceRoot(industry,organization,application);
		}else if("structuredknowledge".equalsIgnoreCase(resourceType)){
			brand ="'"+organization+"结构化知识'";
		}else if("querymanage".equalsIgnoreCase(resourceType)){
//			brand ="'"+organization+"问题库','"+organization+"模板业务'";
//			brand ="'"+organization+"问题库','"+organization+"指令问题库','"+organization+"模板业务','"+organization+"'";
			brand = getServiceRoot(industry,organization,application);
		}else if("scenariosrules".equalsIgnoreCase(resourceType)){
			brand ="'"+organization+"场景','"+organization+"指令场景','"+application+"场景'";
		}
		if("".equals(brand)){
			return menuTree;
		}
		List<String> roleidList = new ArrayList<String>();
		roleidList.add(roleid);
		String rootserviceId = "";
		// 递归获取根业务下所有子孙节点业务及根业务本身
		Result rs = CommonLibServiceDAO.seletServiceByParentNameRootAndBrand(brand, brand);
		Map<String,String> resourceDic  =getResourceDic(roleidList, resourceType, industry+"->"+organization+"->"+application, null);
		JSONArray jsonArray = new JSONArray();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("serviceid").toString();
				String pid = rs.getRows()[i].get("parentid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				jsonObj.put("parentid", pid);
				if(resourceDic.containsKey(sid)){
					jsonObj.put("checked",true);	
				}
				
				jsonObj.put("state", "closed");
				jsonArray.add(jsonObj);
			}
			
			// 获取根业务id
			rs = CommonLibServiceDAO.getServiceID(brand, brand);
			if(rs != null){
				for (int k = 0; k < rs.getRowCount(); k++) {
			    rootserviceId = rs.getRows()[k].get("serviceid").toString();
				JSONArray serviceTree = treeMenuList(jsonArray, rootserviceId);
				for(Object object : jsonArray){
					JSONObject node = (JSONObject) JSONObject.toJSON(object);
					if(node.getString("id").equals(rootserviceId)){
						if(serviceTree.size() > 0){
							node.put("children", serviceTree);
						}else{
							node.put("state", "");
						}
						menuTree.add(node);
					}
				}
			}
				
			}
		}

		return menuTree;
	}
	
	/**
	 *
	 *@param roleidList 获得已配置角色资源字典
	 *@param resourceType
	 *@param serviceType
	 *@param cityCode
	 *@return 
	 *@returnType Map<String,String> 
	 */
	public static Map<String,String> getResourceDic (List<String> roleidList,String resourceType, String serviceType, List<String> cityCode){
		Map<String,String> map = new HashMap<String, String>();
		Result rs = CommonLibPermissionDAO.getResourceFromDB(roleidList, resourceType, serviceType, cityCode);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				map.put(rs.getRows()[i].get("serviceid") == null ? "" : rs.getRows()[i].get("serviceid").toString(),"");
				
			}
		}
		return map;
	}
	
	/**
	 * 构造业务菜单树数据
	 * @param menuList
	 * @param parentId
	 * @return
	 */
	private static JSONArray treeMenuList(JSONArray menuList, String parentId){
		JSONArray childMenu = new JSONArray();
		for (Object object : menuList) {
			JSONObject jsonMenu = (JSONObject) JSONObject.toJSON(object);
			String serviceid = jsonMenu.getString("id");
			String pid = jsonMenu.getString("parentid");
			if (parentId.equals(pid)) {
				JSONArray c_node = treeMenuList(menuList, serviceid);
				if(c_node == null || c_node.size() == 0){
					jsonMenu.put("state", "");
				} else{
					jsonMenu.put("children", c_node);
				}
				
				childMenu.add(jsonMenu);
			}
		}
		return childMenu;
	}
	

	/**
	 *@description 获得业务相关信息
	 *@param serviceid
	 *@return
	 *@returnType Object
	 */
	public static Object getServiceInfo(String serviceid) {
		JSONObject jsonObj = new JSONObject();
		Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
		if (rs != null && rs.getRowCount() > 0) {
			String city = rs.getRows()[0].get("city") != null ? rs.getRows()[0]
					.get("city").toString() : "全国";
			String cityName = "";
			
			if (!"全国".equals(city)) {
				String cityCodeArray[] = city.split(",");
				for (int i = 0; i < cityCodeArray.length; i++) {
					String cityCode = cityCodeArray[i];
					if (cityCodeToCityName.containsKey(cityCode)) {
						String name = cityCodeToCityName.get(cityCode);
						cityName = cityName + name + ",";
					}
				}
				System.out.println("cityName=ghj="+cityName);
				cityName = cityName.substring(0, cityName.lastIndexOf(","));
				System.out.println("cityName=ghj="+cityName);
			} else {
				cityName = "全国";
			}
			ArrayList<String> list = CommonLibServiceDAO
					.getServicePath(serviceid);
			list.remove("知识库");// 移除根节点
			String servicePath = StringUtils.join(list.toArray(), "/");
			jsonObj.put("success", true);
			jsonObj.put("cityname", cityName);
			jsonObj.put("servicepath", servicePath);
			jsonObj.put("citycode", city);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 *@description 根据用户信息创建地市树
	 *@param flag
	 *@return
	 *@returnType Object
	 */
	public static Object createCityTreeByFServiceid(String serviceid) {
		String cityCode = "";
		Map<String, String> map = new HashMap<String, String>();
		JSONArray jsonAr = new JSONArray();
		Result rs = null;
		rs = CommonLibServiceDAO.getFServiceByServiceid(serviceid);
		JSONObject innerJsonObj = null;
		if (rs != null && rs.getRowCount() > 0) {
			cityCode = rs.getRows()[0].get("city") != null ? rs.getRows()[0]
					.get("city").toString() : "全国";
		}
		if ("".equals(cityCode) || "全国".equals(cityCode)) {
			rs = CommonLibQuestionUploadDao.selProvince();
			if (null != rs && rs.getRowCount() > 0) {
				JSONObject allJsonObj = new JSONObject();
				allJsonObj.put("id", "全国");
				allJsonObj.put("text", "全国");
				if (map.containsKey("全国")) {
					allJsonObj.put("checked", true);
				}
				jsonAr.add(allJsonObj);

				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject jsonObj = new JSONObject();
					String id = rs.getRows()[i].get("id").toString();
					String province = rs.getRows()[i].get("province")
							.toString();
					Result innerRs = null;
					Result innerRs2 = null;
					if (id.endsWith("0000")) {
						innerRs = CommonLibQuestionUploadDao
								.getCityByProvince(id);
					}
					// if (province.indexOf("市") < 0) {
					// innerRs =
					// CommonLibQuestionUploadDao.getCityByProvince(id);
					// }
					// else {
					// innerRs2 = CommonLibQuestionUploadDao.getzCity(id);
					// }
					if (map.containsKey(province)) {
						jsonObj.put("checked", true);
					}
					JSONArray jsonArr = new JSONArray();
					if (null != innerRs && innerRs.getRowCount() > 0) {
						for (int j = 0; j < innerRs.getRowCount(); j++) {

							String cityId = innerRs.getRows()[j].get("id")
									.toString();
							// Result sinnerRs =
							// CommonLibQuestionUploadDao.getScity(cityId);
							// JSONArray sJsonArr = new JSONArray();
							innerJsonObj = new JSONObject();
							// if (sinnerRs != null && sinnerRs.getRowCount() >
							// 0){
							// for (int k = 0 ; k < sinnerRs.getRowCount() ;
							// k++){
							// JSONObject sInnerJsonObj = new JSONObject();
							// sInnerJsonObj.put("id",
							// sinnerRs.getRows()[k].get("id"));
							// sInnerJsonObj.put("text",
							// sinnerRs.getRows()[k].get("city"));
							// if
							// (map.containsKey(sinnerRs.getRows()[k].get("city"))){
							// sInnerJsonObj.put("checked", true);
							// }
							// sJsonArr.add(sInnerJsonObj);
							// }
							// innerJsonObj.put("state", "closed");
							// }
							innerJsonObj.put("id", innerRs.getRows()[j]
									.get("id"));
							innerJsonObj.put("text", innerRs.getRows()[j]
									.get("city"));
							// innerJsonObj.put("children", sJsonArr);
							// if
							// (local.equals(innerRs.getRows()[j].get("city"))){
							// innerJsonObj.put("checked", true);
							// }

							if (map.containsKey(innerRs.getRows()[j]
									.get("city"))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					} else if (null != innerRs2 && innerRs2.getRowCount() > 0) {
						for (int j = 0; j < innerRs2.getRowCount(); j++) {

							JSONArray sJsonArr = new JSONArray();

							innerJsonObj = new JSONObject();
							innerJsonObj.put("id", innerRs2.getRows()[j]
									.get("id"));
							innerJsonObj.put("text", innerRs2.getRows()[j]
									.get("city"));
							innerJsonObj.put("children", sJsonArr);
							if (map.containsKey(innerRs2.getRows()[j]
									.get("city"))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					}
					jsonObj.put("id", rs.getRows()[i].get("id"));
					jsonObj.put("text", rs.getRows()[i].get("province"));
					jsonObj.put("children", jsonArr);
					jsonAr.add(jsonObj);
				}
			}

		} else {
			String cityCodeArray[] = cityCode.split(",");
			Map<String, List<String>> provinceDic = getProvinceDic(cityCodeArray);

			if (provinceDic.size() > 0) {
				JSONObject allJsonObj = new JSONObject();
				for (Map.Entry<String, List<String>> entry : provinceDic
						.entrySet()) {
					List<String> cityCodeList = entry.getValue();
					JSONObject jsonObj = new JSONObject();
					String id = entry.getKey();
					String province = "";
					province = cityCodeToCityName.get(id);

					if (map.containsKey(province)) {
						jsonObj.put("checked", true);
					}
					JSONArray jsonArr = new JSONArray();
					if (cityCodeList.size() > 0) {
						for (int j = 0; j < cityCodeList.size(); j++) {

							String cityId = cityCodeList.get(j);
							innerJsonObj = new JSONObject();
							innerJsonObj.put("id", cityId);
							innerJsonObj.put("text", cityCodeToCityName
									.get(cityId));
							if (map.containsKey(cityCodeToCityName.get(cityId))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					}
					jsonObj.put("id", id);
					jsonObj.put("text", province);
					jsonObj.put("children", jsonArr);
					jsonAr.add(jsonObj);
				}
			}
		}

		return jsonAr;
	}

	/**
	 *@description 获得省份地市字典
	 *@param cityCodeArray
	 *            地市数组
	 *@return
	 *@returnType Map<String,List<String>>
	 */
	public static Map<String, List<String>> getProvinceDic(
			String cityCodeArray[]) {
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		List<String> list = new ArrayList<String>();
		List<String> cityCodeList = new ArrayList<String>();
		for (int i = 0; i < cityCodeArray.length; i++) {
			String cityCode = cityCodeArray[i];
			if (cityCode.endsWith("0000")) {// 判断是否是省份,先将省份放入map中
				map.put(cityCode, list);
			} else {
				cityCodeList.add(cityCode);
			}
		}
		List<String> toRemoveList = new ArrayList<String>();
		// 创建省份地市对应关系
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String province = entry.getKey();
			list = new ArrayList<String>();
			for (int j = 0; j < cityCodeList.size(); j++) {
				String tempCityCode = cityCodeList.get(j);
				// 判断是否是当前省份下地市，如是将其放入value中
				String tCode = tempCityCode.substring(0, 2);
				if (province.startsWith(tCode)) {
					list.add(tempCityCode);
					toRemoveList.add(tempCityCode);// 移除已经找到对应关系的地市

				}
			}
			map.put(province, list);
		}
		// 已经找到对应关系的地市移除
		cityCodeList.removeAll(toRemoveList);
		// 将未找到对应关系的地市作为省份处理

		if (cityCodeList.size() > 0) {
			for (int k = 0; k < cityCodeList.size(); k++) {
				map.put(cityCodeList.get(k), new ArrayList<String>());
			}
		}

		return map;
	}

	/**
	 *@description 修改业务路径
	 *@param serviceid
	 *@param serviceName
	 *@return
	 *@returnType Object
	 */
	public static Object updateServicePath(String serviceid, String parentName,
			String parentid) {
		int count = CommonLibServiceDAO.updateServiceParentid(parentid,
				parentName, serviceid);
		if (count > 0) {
			Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(parentid);
			String city = rs.getRows()[0].get("city") != null ? rs.getRows()[0]
					.get("city").toString() : "全国";
			return updateServiceCity(parentid, city);
		}
		return new JSONObject();

	}

	/**
	 *@description 修改业务地市
	 *@param serviceid
	 *@param cityCode
	 *@return
	 *@returnType Object
	 */
	public static Object updateServiceCity(String serviceid, String cityCode) {
		JSONObject jsonObj = new JSONObject();
		Map<String, String> map;
		int count = -1;
		if ("全国".equals(cityCode)) {// 若待修改地市为全国，直接update
			map = new HashMap<String, String>();
			map.put(serviceid, cityCode);
			count = CommonLibServiceDAO.updateServiceCity(map);
		} else {// 比较当前业务下子节点，如果子节点地市属于当前地市范围不做修改，反之update
			List<String> ttargetCityCode = new ArrayList<String>(Arrays
					.asList(cityCode.split(",")));
			Result rs = CommonLibServiceDAO.getSonServiceInfo(serviceid);
			map = new HashMap<String, String>();
			if (null != rs && rs.getRowCount() > 0) {

				for (int j = 0; j < rs.getRowCount(); j++) {
					String sid = rs.getRows()[j].get("serviceid").toString();
					String city = rs.getRows()[j].get("city") != null ? rs
							.getRows()[j].get("city").toString() : "全国";
					List<String> tcompareCityCode = new ArrayList<String>(
							Arrays.asList(city.split(",")));
					if (!compareCity(ttargetCityCode, tcompareCityCode)) {// 如不包含,放入字典中带update
						map.put(sid, cityCode);
					}
				}
			}
			map.put(serviceid, cityCode);
			count = CommonLibServiceDAO.updateServiceCity(map);
		}
		if (count > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "修改成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "修改失败!");
		}

		return jsonObj;
	}

	/**
	 *@description 判断集合包含关系
	 *@param targetCityCode
	 *@param compareCityCode
	 *@return
	 *@returnType boolean
	 */
	public static boolean compareCity(List<String> targetCityCode,
			List<String> compareCityCode) {
		int size = targetCityCode.size();
		List<String> list = new ArrayList<String>(targetCityCode);
		list.addAll(compareCityCode);// 合并目标集合和待比较集合
		Set set = new HashSet(list);// 通过set去重
		if (set.size() == size || set.size() < size) {// 如果去重后set大小等于目标集合，即目标集合包含所有集合
			return true;
		}
		return false;
	}

	/**
	 *通过构造业务树
	 * 
	 * @param serviceid
	 *@return
	 *@returnType Object
	 */
	public static Object createTree(String serviceid, String flag) {

		Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
		String brand = "";
		if (rs != null && rs.getRowCount() > 0) {
			brand = "'" + rs.getRows()[0].get("brand").toString() + "'";
		}
		if (!"".equals(flag) && flag != null) {
			serviceid = "";
		}
		return getTreeData(serviceid, brand);

	}

	/**
	 *@description 获得数型结构数据
	 *@param serviceid
	 *@param brand
	 *@return
	 *@returnType JSONArray
	 */
	public static JSONArray getTreeData(String serviceid, String brand) {
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibServiceDAO.createServiceTree(serviceid, brand);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("serviceid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				if (CommonLibQueryManageDAO.hasChild(sid) == 0) {// 如果没有子业务
					// jsonObj.put("iconCls", "icon-servicehit");
					jsonObj.put("leaf", true);
				} else {
					// jsonObj.put("expanded","true");
					jsonObj.put("cls", "folder");
					jsonObj.put("leaf", false);
					jsonObj.put("state", "closed");
				}

				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 业务查询
	 * 
	 * @param serviceStr
	 * @return
	 */
	public static Object searchService(String industry, String organization,
			String application, String serviceStr) {
		JSONArray jsonArray = new JSONArray();
	
		String serviceRoot = getServiceRoot(industry,organization,application);
		if (serviceRoot==null || "".equals(serviceRoot)) {
			return new JSONArray();
		}
		Result rs = CommonLibServiceDAO.getLikeService(serviceRoot, serviceStr);
		if (rs != null && rs.getRowCount() > 0) {
			String serviceid = null;
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				serviceid = rs.getRows()[i].get("serviceid").toString();
				jsonObj.put("id", serviceid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				jsonObj.put("textpath", "/"
						+ rs.getRows()[i].get("name_path").toString().replace(
								"->", "/"));
				jsonObj.put("idpath", rs.getRows()[i].get("serviceid_path")
						.toString());
				jsonObj.put("leaf",
						CommonLibQueryManageDAO.hasChild(serviceid) == 0);
				jsonArray.add(jsonObj);
			}
		}

		return jsonArray;
	}

	/**
	 *@description 查找业务根
	 *@param industry
	 *@param organization
	 *@param application
	 *@return
	 *@returnType String
	 */
	public static String getServiceRoot(String industry, String organization,
			String application) {
//		Result rs = CommonLibIndustryApplicationToServicesDAO
//				.getIndustryapplicationToServicesInfo(industry, organization,
//						application);
		Result rs = CommonLibIndustryApplicationToServicesDAO
		.getIndustryapplicationToServicesInfo(industry, organization,
				application,"是");

		String serviceRoot = "";
		String brand = "";
		if (rs != null && rs.getRowCount() > 0) {
			serviceRoot = rs.getRows()[0].get("serviceroot").toString();
			brand = "'" + serviceRoot.replace("|", "','") + "'";
			rs = CommonLibMetafieldmappingDAO.getConfigValue("场景业务根对应关系配置",
					industry + "->" + organization + "->" + application);
			if (rs != null && rs.getRowCount() > 0) {
				brand = brand + ",'" + rs.getRows()[0].get("name").toString()
						+ "'";
			}
			rs = CommonLibMetafieldmappingDAO.getConfigValue("结构化知识业务根对应关系配置",
					industry + "->" + organization + "->" + application);
			if (rs != null && rs.getRowCount() > 0) {
				brand = brand + ",'" + rs.getRows()[0].get("name").toString()
						+ "'";
			}
			
		} 
		return brand;
	}

	/**
	 *@description 通过业务名获取业务层级关系
	 *@param name
	 *@param industry
	 *@param organization
	 *@param application
	 *@return
	 *@returnType JSONArray
	 */
	public static JSONArray createServiceTreeByName(String name,
			String industry, String organization, String application) {
		JSONArray ja = new JSONArray();
		
		String serviceRoot = getServiceRoot(industry,organization,application);
		if (serviceRoot==null || "".equals(serviceRoot)) {
			return new JSONArray();
		}
		
		String sql;
		Result res = null;
		res = CommonLibServiceDAO.getUpHierarchyService(serviceRoot, name);
		if (res == null || res.getRowCount() == 0) {
			return ja;
		}
		// 此时获取的数据为多个，一组一组的，我们先进行分组
		SortedMap[] maps = res.getRows();
		// System.out.println(maps.length);
		List<ServicesMap> serviceList = new ArrayList<ServicesMap>();
		ServicesMap serviceSingle = null;
		List<String> li = new ArrayList<String>();
		for (int i = 0; i < maps.length; i++) {
			SortedMap map = maps[i];
			li.add(map.get("serviceid").toString());
		}
		for (int i = 0; i < maps.length; i++) {
			SortedMap map = maps[i];
			String servicename = map.get("service").toString();
			String id = map.get("serviceid").toString();
			if (i == maps.length - 1) {// 最后一个
				HashMap servicesMap = serviceSingle.getList();
				List<String> serviceKeys = serviceSingle.getIndexs();
				servicesMap.put(id, map);
				serviceKeys.add(id);
				// if (isexist(serviceKeys.get(serviceKeys.size() -
				// 2).toString(),
				// li)) {
				if (li.contains(serviceKeys.get(serviceKeys.size() - 2)
						.toString())) {
					serviceList.add(serviceSingle);
				}

				// }
			} else if (name.equals(servicename)) {// 如果为当前查询的业务，那么作为第一个
				if (serviceSingle != null) {// 如果之前已经有了，那么先加入到集合中

					List<String> serviceKeys1 = serviceSingle.getIndexs();
					// if (isexist(serviceKeys1.get(serviceKeys1.size() - 2)
					// .toString(), li))
					if (li.contains(serviceKeys1.get(serviceKeys1.size() - 2)
							.toString()))

						serviceList.add(serviceSingle);
					serviceSingle = new ServicesMap();// 清空
					HashMap servicesMap = serviceSingle.getList();
					ArrayList serviceKeys = serviceSingle.getIndexs();
					servicesMap.put(id, map);

					serviceKeys.add(id);
				} else {
					serviceSingle = new ServicesMap();
					HashMap servicesMap = serviceSingle.getList();
					ArrayList serviceKeys = serviceSingle.getIndexs();
					servicesMap.put(id, map);
					serviceKeys.add(id);
				}
			} else {
				HashMap servicesMap = serviceSingle.getList();
				ArrayList serviceKeys = serviceSingle.getIndexs();
				servicesMap.put(id, map);
				serviceKeys.add(id);
			}

		}
		// 分组完后，进行，排序
		Collections.sort(serviceList, new MyListComp());
		JSONArray SON = creatTree(serviceList);
		// 排序结束后，进行2次遍历
		return SON;

	}

	// public static boolean isExist(String key, ArrayList li) {
	// boolean res = false;
	// for (int i = 0; i < li.size(); i++) {
	// String service = li.get(i).toString();
	// if (service.equals(key)) {
	// res = true;
	// break;
	// }
	// }
	// return res;
	// }

	@SuppressWarnings("unchecked")
	public static JSONArray creatTree(List<ServicesMap> serviceList) {
		// 使用第一条数据生成一棵基本树,这棵树是最长的一棵
		ServicesMap smap = serviceList.get(0);
		HashMap<String, SortedMap> servicemap = smap.getList();
		ArrayList servicekeys = smap.getIndexs();
		JSONArray son = new JSONArray();
		for (int i = 0; i < servicekeys.size(); i++) {
			SortedMap map = servicemap.get(servicekeys.get(i));
			JSONObject o = new JSONObject();
			if (i == 0) {// 第一个为当前查询的，看是否有子场景
				boolean leaf = CommonLibInteractiveSceneDAO
						.hasChildrenByScenariosid(map.get("SERVICEID")
								.toString());
				o.put("leaf", leaf);
			} else {
				o.put("leaf", false);
			}

			o.put("text", map.get("SERVICE"));
			o.put("id", map.get("SERVICEID"));
			o.put("children", son);
			son = new JSONArray();
			son.add(o);
		}
		// 第一条树创建成功后，遍历后面的树，判断是否后面的树，该添加到哪个节点
		JSONObject root = son.getJSONObject(0);// 这个是树根
		for (int i = 1; i < serviceList.size(); i++) {// 从第二组开始遍历，整棵树（深度为该组的长度）
			ServicesMap smapN = serviceList.get(i);
			HashMap<String, SortedMap> servicemapN = smapN.getList();
			ArrayList servicekeysN = smapN.getIndexs();
			JSONArray nroot = root.getJSONArray("children");
			for (int x = 1; x <= servicekeys.size(); x++) {
				String key = servicekeysN.get(servicekeysN.size() - x - 1)
						.toString();// 从第二个key开始，例如 基金服务
				// String serviceid =
				// servicemapN.get(key).get("SERVICEID").toString();
				boolean haveParent = false;
				for (int y = 0; y < nroot.size(); y++) {
					JSONObject nodeN = nroot.getJSONObject(y);
					if (!nodeN.getString("serviceid").equals(key)) {
						haveParent = false;
					} else {
						haveParent = true;
						nroot = nodeN.getJSONArray("children");
						break;
					}
				}
				if (!haveParent) {// 如果有父节点后，继续下一个,如果没有的话，那么生成分支
					JSONArray arr = nroot;
					JSONArray arrN = new JSONArray();
					for (int z = 0; z < servicekeysN.size() - x; z++) {
						SortedMap map = servicemapN.get(servicekeysN.get(z));
						JSONObject o = new JSONObject();
						if (z == 0) {// 第一个为当前查询的，看是否有子场景
							boolean leaf = CommonLibInteractiveSceneDAO
									.hasChildrenByScenariosid(map.get(
											"SCENARIOSID").toString());
							o.put("leaf", leaf);
						} else {
							o.put("leaf", false);
						}

						o.put("text", map.get("SERVICE"));
						o.put("id", map.get("SCENARIOSID"));

						o.put("children", arrN);
						arrN = new JSONArray();
						arrN.add(o);
					}
					arr.add(arrN.get(0));
					break;// 生成树后，跳出循环

				}
			}
		}
		return son;
	}

	/**
	 * 保存资源
	 *@param resourceid
	 *@param resourceType
	 *@param serviceType
	 *@return 
	 *@returnType Object 
	 */
	public static Object saveResouce(String roleid,String resourceid,String resourceType,String serviceType) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		String resourceidArry [] = resourceid.split(",");
		if(CommonLibPermissionDAO.saveResource(roleid, resourceidArry, resourceType, serviceType)){
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功!");
		}else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}
	
	/**
	 * 获得资源操作类型树形列表
	 *@param roleid
	 *@param resourceType
	 *@param serviceType
	 *@return 
	 */
	public static Object getResourceOperation(String roleid,String resourceType,String serviceType){
		return CommonLibPermissionDAO.getResourceOperation(roleid, resourceType, serviceType);
	}
	
    /**
     *更新资源操作类型
     *@param roleid
     *@param resourceid
     *@param resourceType
     *@param serviceType
     *@param operationType
     *@param isCascade
     *@return 
     *@returnType Object 
     */
    public static Object updateResourceOperationType(String roleid,String resourceid,String resourceType,String serviceType,String operationType,Boolean isCascade){
    	// 定义返回的json串
		JSONObject jsonObj = new JSONObject(); 
		Boolean rs =false;
		if(isCascade){
			rs=CommonLibPermissionDAO.updateResourceOperationTypeCascade(roleid, resourceid, resourceType, serviceType, operationType);	
		}else{
			rs=CommonLibPermissionDAO.updateResourceOperationType(roleid, resourceid, resourceType, serviceType, operationType);
		}
		if(rs){
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功!");
		}else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
    }	
	
}
