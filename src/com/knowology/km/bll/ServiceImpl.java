/**  
 * @Project: KM 
 * @Title: ServiceImpl.java
 * @Package com.knowology.km.action
 * @author c_wolf your emai address
 * @date 2014-4-23 上午11:02:32
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.km.bll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.knowology.Bean.User;
import com.knowology.bll.CommonLibServiceAttrDao;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.km.access.UserOperResource;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.getConfigValue;

/**
 * 内容摘要 ：
 * 
 * 类修改者 修改日期 修改说明
 * 
 * @ClassName ServiceImpl
 *            <p>
 *            Company: knowology
 *            </p>
 * @author c_wolf your emai address
 * @date 2014-4-23 上午11:02:32
 * @version V1.0
 */

public class ServiceImpl {
	// <summary>
	// 获取所有的品牌,返回DataTable
	// </summary>
	// <returns></returns>
	public Result GetAllBrand() {
		// 根据当前登陆用户查询出他所能操作的业务
		String sql = "select * from service where service='知识库'";
		//String sql = "select * from service where serviceid=0";
		
		try {
			return Database.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 处理业务  嘉实基金、华夏基金 同时存在基金服务下 
	 * */
	public static String getServiceStr(String industryOrganizationApplication) {
		String serviceString = MyClass.ServiceRoot();
		String repalceService = getConfigValue.replaceService.replace(" ", "");
		String serviceArry[] = repalceService.split(",");
		for (int a = 0; a < serviceArry.length; a++) {
			if (serviceString.indexOf(serviceArry[a]) != -1) {
				serviceString = serviceString.replace(serviceArry[a], "基金服务");
			}
		}
		return serviceString;
	}

	/**
	 * 处理数据库数据
	 * */
	public static String getchildServiceStr(
			String industryOrganizationApplication) {
		String serviceString = MyClass.ServiceRoot();
		String repalceService = getConfigValue.replaceService.replace(" ", "");
		String serviceArry[] = repalceService.split(",");
		for (int a = 0; a < serviceArry.length; a++) {
			if (serviceString.indexOf(serviceArry[a]) != -1) {
				serviceString = serviceString.replace(serviceArry[a], "基金服务");
			}
		}
		return serviceString;
	}

	/**
	 * 根据城市ID，业务ID，品牌来查询子业务
	 */
	public Result GetChildrenByCityBrand(String cityids, String brand,
			String serviceid,String sevicecontainer,String serviceName) {
		StringBuilder sb = new StringBuilder();
		List<String> paras = new ArrayList<String>();
		Result res = null;
		String serviceString;
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre== null||"".equals(sre)){
			return null;
		}
		User user = (User)sre;
		String industryOrganizationApplication = user.getRealindustryOrganizationApplication();

		if("currentindustry".equals(sevicecontainer)){//加载当前行业主题树
		  industryOrganizationApplication = industryOrganizationApplication.split("->")[0]+"主题";	
		  serviceString = "'"+industryOrganizationApplication+"'";
		}else if("generalindustry".equals(sevicecontainer)){//加载通用行业主题树
		  industryOrganizationApplication = "通用行业主题";
		  serviceString = "'通用行业主题'";
		}else{//根据登录行业加载对应业务树
			//serviceString = getServiceStr(industryOrganizationApplication);
			//serviceString = MyClass.ServiceRoot();
			serviceString = LoginDAO
			.getServiceRoot(industryOrganizationApplication).replace("@", ",");
			
		}
	
		if ("0".equals(serviceid)) {
			// sb
			// .append("select distinct serviceid,ss.service,parentname, brand,cityid,container,mincredit,CASE WHEN (select count(*) from service  where parentid=ss.serviceid)>0 then 'false'     ELSE 'true' end as leaf from service ss  "
			// + ",roleservicerel ree,  workerrolerel wr "
			// + "where  parentid=0 and ss.service!='知识库'  "
			// + "AND wr.workerid ='"
			// + MyClass.LoginUserId()
			// +
			// "' AND ree.service = ss.service AND ree.roleid  = wr.roleid and cityid  in ("
			// + cityids +
			// ") and ss.service in ("+serviceString+") ORDER BY service");
//
//			sb
//					.append("select distinct serviceid,ss.service,parentname, brand,cityid,container,mincredit,CASE WHEN (select count(*) from service  where parentid=ss.serviceid)>0 then 'false'     ELSE 'true' end as leaf from service ss  "
//							+ " where ss.cityid  in ("
//							+ cityids
//							+ ") "
//							+ "and ss.service in ("
//							+ serviceString
//							+ ") ORDER BY service");
//			if (UserOperResource.isAccessOnServiceTreeCheck((User)sre)) {// 判断是否有查看的操作权限
				//serviceString = "'个性化业务','移动业务','手机终端'";
				res = UserOperResource.getChildByRoot((User)sre,serviceString);

//			} else {
//				res = null;
//			}
			return res;
		} else {
			if (industryOrganizationApplication.indexOf("基金行业") != -1
					&& "基金服务".equals(brand)) {
				String hidService = getConfigValue.hideService.replace(" ", "");
				String serviceroot = MyClass.ServiceRoot();
				String servicerootArray[] = serviceroot.replace("'", "").split(
						",");
				String hidServiceArray[] = hidService.split(",");
				String childService = "";
				List<String> list = Arrays.asList(hidServiceArray);
				String sameservice = "";
				for (int i = 0; i < hidServiceArray.length; i++) {
					for (int k = 0; k < servicerootArray.length; k++) {
						if (hidServiceArray[i].equals(servicerootArray[k])) {
							sameservice = servicerootArray[k];
						}
					}
				}
				for (int n = 0; n < list.size(); n++) {
					if (!list.get(n).endsWith(sameservice)) {
						childService += "'" + list.get(n) + "',";
					}
				}
				if (!"".equals(childService)) {
					childService = childService.substring(0, childService
							.length() - 1);
				}
//				sb
//						.append("select serviceid,service,parentname, brand,cityid,container,mincredit,CASE WHEN (select count(*) from service  where parentid=ss.serviceid)>0 then 'false'     ELSE 'true' end as leaf from service ss where (brand=?) and cityid  in ("
//								+ cityids
//								+ ") and and parentid in () and  ss.service not in("
//								+ childService + ")");
//				if(UserOperResource.isAccessByResourceID(user,"service",serviceid,"S",brand)){
				res = UserOperResource.getChildServiceByParentID(user, serviceid, brand, childService);
//				}else{
//				 return null;	
//				}
				
			} else {
//				sb
//						.append("select serviceid,service,parentname, brand,cityid,container,mincredit,CASE WHEN (select count(*) from service  where parentid=ss.serviceid)>0 then 'false'     ELSE 'true' end as leaf from service ss where (brand=?) and cityid  in ("
//								+ cityids + ") and parentid in ()" );
//				if(UserOperResource.isAccessByResourceID(user,"service",serviceid,"S",brand)){
					res = UserOperResource.getChildServiceByParentID(user,serviceid,brand);
//				}else{
//					return null;
//				}
				
			}

//			if (brand.equals("舆情分析")) {
//				paras.add("舆情产品分析");
//			} else {
//				paras.add(brand);
//			}
//			if (serviceid != null && serviceid != "") {
//				sb.append(" and parentid in (" + serviceid + ")");
//			} else {
//				sb.append(" and parentname=? ");
//				if (brand == "IPTV") {
//					paras.add("IPTV点播");
//				} else {
//					paras.add(brand);
//
//				}
//			}
		}

//		try {
//			
//			
//			if (UserOperResource.isAccessOnServiceTreeCheck((User)sre)) {// 判断是否有查看的操作权限
//				res = UserOperResource.getService((User)sre);
//			} else {
//				res = null;
//			}
//
//			//res = Database.executeQuery(sb.toString(), paras.toArray());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return res;
	}

	/***
	 * 根据城市ID，摘要，业务ID等获取业务
	 */
	@SuppressWarnings("unchecked")
	public JSONObject GetJsonServiceByCityBrandService(String cityids,
			String brand, String service, String serviceid,String sevicecontainer) {
		Result res = null;// 查询的结果集
		JSONObject resultObj = JSONObject.fromObject("{}");
		String result = "{success:true,total:0,root:[]}";
//		if (cityids == null)// 如果没有城市ID，那么返回null
//		{
//			return null;
//		}
		// 如果当前没有业务ID传进来，那么说明只是查询第一级业务
		if (service == null || service.equals("")) {
			//res = GetAllBrand();
			res = UserOperResource.getRootBrand();
		} else {
			res = GetChildrenByCityBrand(cityids, brand, serviceid,sevicecontainer,service);
		}
		if (res == null || res.getRowCount() == 0) {
			return JSONObject.fromObject(result);
		}
		resultObj.put("success", true);
		resultObj.put("total", res.getRowCount());
		SortedMap[] mpas = res.getRows();

		for (int i = 0; i < res.getRowCount(); i++) {
			SortedMap map = mpas[i];
			Object o = map.get("SERVICE");
			Object o1 = map.get("BRAND");
			Object o2 = map.get("SERVICEID");
			Object o4 = map.get("PARENTNAME");
			Object o5 = map.get("CONTAINER");
			Object o6 = map.get("MINCREDIT");
			if (o5 == null) {
				o5 = "";
			}
			boolean leaf = false;

			if (service != null && !"".equals(service)) {
				Object o3 = map.get("LEAF");
				map.remove("LEAF");
				map.put("leaf", o3);
			} else {
				map.put("leaf", leaf);
			}
			map.remove("PARENTNAME");
			map.remove("BRAND");
			map.remove("SERVICE");
			map.remove("CITYID");
			map.remove("SERVICEID");
			map.remove("CONTAINER");
			map.remove("MINCREDIT");
			map.put("service", o);
			map.put("serviceid", o2);
			map.put("text", o);

			map.put("container", o5);
			map.put("parentname", o4);
			map.put("cityid", cityids);
			map.put("brand", o1);
			map.put("id", o2);
			map.put("mincredit", o6);

		}
		resultObj.put("root", mpas);
		return resultObj;
	}

	@SuppressWarnings("unchecked")
	public JSONArray GetJsonServiceByCityBrandServiceForDDL(String cityids,
			String service,String sevicecontainer) {
		if (cityids == null || "".equals(cityids)) {
			return JSONArray.fromObject("[]");
		}
		Result res = null;
		String sql;
		if (getConfigValue.isMySQL) {
			sql = "select getChildLstByServices('" + cityids + "','"
					+ MyClass.LoginUserId() + "')";
			String result = "";
			try {
				result = Database.executeQueryAisa(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result != "" && result != null) {
				sql = "select SERVICEID,SERVICE,PARENTID,PARENTNAME,BRAND,CITYID,CUSTOMERTYPE,CONTAINER,DOCID,CUSTOMER_SERVICEID from service where serviceid in("
						+ result + ") and upper(service) LIKE ?";
				try {
					res = Database.executeQuery(sql, "%"
							+ service.toUpperCase() + "%");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {// Oracle
			String serviceString;
			Object sre = GetSession.getSessionByKey("accessUser");
			if(sre== null||"".equals(sre)){
				return null;
			}
			User user = (User)sre;
			String industryOrganizationApplication = user.getRealindustryOrganizationApplication();			if("currentindustry".equals(sevicecontainer)){//加载当前行业主题树
				serviceString = "'"+industryOrganizationApplication.split("->")[0]+"主题'";	
				}else if("generalindustry".equals(sevicecontainer)){//加载通用行业主题树
				  serviceString = "'通用行业主题'";
				}else{//根据登录行业加载对应业务树
					serviceString = MyClass.ServiceRoot();
				}
			// sql = "SELECT distinct service " + "FROM " + "  (SELECT * " +
			// "  FROM SERVICE "
			// + "    START WITH service IN "
			// + "    ( SELECT DISTINCT ss.service "
			// + "    FROM service ss , " + "      roleservicerel ree, "
			// + "      workerrolerel wr "
			// + "    WHERE ree.service = ss.service "
			// + "    AND ree.roleid    = wr.roleid "
			// + "    AND cityid       IN(" + cityids + ") "
			// + "    AND wr.workerid   = '" + MyClass.LoginUserId() +
			// "'  and ss.service in("+serviceString+")  ) "
			// + "   CONNECT BY prior service = parentname " + "  ) "
			// + "   WHERE upper(service) LIKE ?";

//			sql = "SELECT distinct service " + "FROM " + "  (SELECT * "
//					+ "  FROM SERVICE " + "    START WITH service IN  ("
//					+ serviceString + ") and  cityid in(" + cityids + ") "
//					+ "   CONNECT BY prior serviceid = parentid " + "  ) "
//					+ "   WHERE upper(service) LIKE ?" ;
//			try {
//				res = Database.executeQuery(sql, "%" + service.toUpperCase()
//						+ "%");
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			
			
			res = UserOperResource.getServiceForDDL(user,serviceString,service);
			
			
		}
		SortedMap[] mpas = res.getRows();
		for (int i = 0; i < res.getRowCount(); i++) {
			SortedMap map = mpas[i];
			Object o = map.get("SERVICE");
			map.remove("SERVICE");
			map.put("text", o);
		}
		JSONArray arr = JSONArray.fromObject(mpas);
		return arr;
	}

	static public Boolean HasChildrenByServiceid(String serviceid) {
		String sql;
		if (getConfigValue.isMySQL) {
			sql = "select serviceid from service where  parentid in ("
					+ serviceid + ") limit 1";
		} else {
			sql = "select serviceid from service where rownum<2 and parentid in ("
					+ serviceid + ")";
		}
		Result res = null;
		try {
			res = Database.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (res == null || res.getRowCount() == 0) {
			return true;
		}
		return false;
	}

	// 传进来的是serviceid
	@SuppressWarnings("unchecked")
	public JSONArray GetJsonServiceByCityServiceForTree1(String cityids,
			String serviceid) {
		String sql;
		Result res = null;
		if (getConfigValue.isMySQL) {
			sql = "SELECT getParentByServiceID('" + serviceid + "','" + cityids
					+ "') ";
			String result = "";
			try {
				result = Database.executeQueryAisa(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result != "") {
				String[] temp = result.split(",");
				sql = "";
				for (int i = 0; i < temp.length; i++) {
					if (i == 0) {
						sql += "select * from service where serviceid="
								+ temp[i] + "";
					} else {
						sql += " UNION ALL SELECT * FROM service WHERE serviceid="
								+ temp[i] + "";
					}
				}
				try {
					res = Database.executeQuery(sql);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			sql = "SELECT * FROM (SELECT *"
					+ "FROM SERVICE    START WITH service IN    ( SELECT DISTINCT ss.service    FROM service ss ,      roleservicerel ree,      workerrolerel wr    WHERE ree.service = ss.service    AND ree.roleid    = wr.roleid    AND cityid       IN("
					+ cityids
					+ ")    AND wr.workerid   = '"
					+ MyClass.LoginUserId()
					+ "'    )"
					+ " CONNECT BY prior serviceid = parentid)T 　　start   WITH serviceid = ? "
					+ "AND cityid  IN(?) 　　connect BY prior parentid = serviceid";
			try {
				res = Database.executeQuery(sql, serviceid, cityids);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (res == null || res.getRowCount() == 0) {
			return JSONArray.fromObject("[]");

		}
		SortedMap[] maps = res.getRows();

		JSONArray son = JSONArray.fromObject("[]");
		for (int i = 0; i < maps.length; i++) {

			SortedMap map = maps[i];
			JSONObject o = JSONObject.fromObject("{}");
			if (i == 0) {// 第一个为当前查询的，看是否有子业务
				boolean leaf = HasChildrenByServiceid(map.get("SERVICEID")
						.toString());
				o.put("leaf", leaf);
			} else {
				o.put("leaf", false);
			}

			o.put("text", map.get("SERVICE"));
			o.put("brand", map.get("BRAND"));
			o.put("serviceid", map.get("SERVICEID"));
			o.put("parentname", map.get("PARENTNAME"));
			o.put("cityid", cityids);
			o.put("id", map.get("SERVICEID"));
			Object c = map.get("container");
			if (c == null) {
				o.put("container", "");
			} else {
				o.put("container", c);
			}

			o.put("children", son);
			son = JSONArray.fromObject("[]");
			son.add(o);
		}
		return son;
	}

	public Result GetJsonAllCity() {
		String sql = "select cityid ,city ,province ,areacode from city";
		Result res = null;
		try {
			res = Database.executeQuery(sql);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public String Inherit_abs(String service, String serviceids,
			String parentname) {
		// 这个sql语句查询出当前业务的父业务，和它本身已经包含的abtract和topic
		String sql_1;
		if (getConfigValue.isMySQL) {
			sql_1 = "SELECT parentid, "
					+ "  group_concat(kk.abstract) as abb, " + "  kk.topic "
					+ "FROM kbdata kk, " + "  service ss "
					+ "WHERE ss.serviceid =? "
					+ "AND ss.serviceid   =kk.serviceid "
					+ "AND kk.abstract   IS NOT NULL "
					+ "GROUP BY ss.parentid, " + "  kk.topic ";
		} else {
			sql_1 = "SELECT parentid, " + "  wm_concat(kk.abstract) as abb, "
					+ "  kk.topic " + "FROM kbdata kk, " + "  service ss "
					+ "WHERE ss.serviceid =? "
					+ "AND ss.serviceid   =kk.serviceid "
					+ "AND kk.abstract   IS NOT NULL "
					+ "GROUP BY ss.parentid, " + "  kk.topic ";
		}
		Result result_1 = null;
		try {
			result_1 = Database.executeQuery(sql_1, serviceids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result_1 == null || result_1.getRowCount() == 0)
			return "{\"checkInfo\":\"当前业务无可继承的主题摘要 ,无需继承!\"}";
		int pid = Integer.parseInt(result_1.getRowsByIndex()[0][0].toString());

		if (pid == 0) {
			return "{\"checkInfo\":\"当前为根业务,无需继承!\"}";
		}
		// 查询出父业务所具有的摘要主题
		String sql_2;
		if (getConfigValue.isMySQL) {
			sql_2 = "select topic,group_concat(abstract) as abb  from kbdata where serviceid ="
					+ pid + " and abstract is not null  group by topic";

		} else {
			sql_2 = "select topic,wm_concat(abstract) as abb  from kbdata where serviceid ="
					+ pid + " and abstract is not null  group by topic";

		}
		Result result_2 = null;
		try {
			result_2 = Database.executeQuery(sql_2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 如果父业务不存在摘要主题，那么就不需要继承了
		if (result_2 == null || result_2.getRowCount() == 0) {
			return "{\"checkInfo\":'父业务无摘要主题，无需继承!'}";
		}
		List listsql = new ArrayList<String>();
		List paramList = new ArrayList();
		String sql_insert;
		if (getConfigValue.isMySQL) {
			int q = SeqDAO.GetNextVal("SEQ_KBDATA_ID");
			sql_insert = "insert into KBDATA(SERVICEID,KBDATAID,TOPIC,ABSTRACT,CITY,CUSTOMERTYPE)"
					+ "values (?," + q + "?,?,'上海','所有客户')";
		} else {
			sql_insert = "insert into KBDATA(SERVICEID,KBDATAID,TOPIC,ABSTRACT,CITY,CUSTOMERTYPE)"
					+ "values (?,SEQ_KBDATA_ID.nextval,?,?,'上海','所有客户')";
		}
		// 如果父业务有摘要主题,那么就要进行继承
		// 新建map来存放当前业务所具有的摘要主题
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < result_1.getRowCount(); i++) {
			SortedMap m = result_1.getRows()[i];
			String key = m.get("TOPIC").toString();// 主题
			String value = m.get("ABB").toString();// 摘要
			map.put(key, value);
		}

		// 新建map来存放父业务所具有的摘要主题
		Map<String, String> mapP = new HashMap<String, String>();
		for (int i = 0; i < result_2.getRowCount(); i++) {
			SortedMap m = result_2.getRows()[i];
			String key = m.get("TOPIC").toString();// 主题
			String value = m.get("ABB").toString();// 摘要
			mapP.put(key, value);
		}

		// 最后我们需要把子业务没有的继承到的摘要主题，继承进去，将父业务的主题摘要去除掉已经继承到的
		// 本来这步可以放在上面map创建时执行，放这里逻辑会清楚一些
		for (String key : mapP.keySet()) {
			String value = mapP.get(key);// 获取该主题下的所有摘要
			value = value.replaceAll("<[^<]{1,}>", "");// 替换掉<*>内容
			List vals = Arrays.asList(value.split(","));// 父业务中key 下的摘要数组
			List arrayList = new ArrayList(vals);

			// 如果子业务存在这个摘要，那么进行去重
			if (map.containsKey(key)) {
				String valuec = map.get(key);
				valuec = valuec.replaceAll("<[^<]{1,}>", "");// 替换掉<*>内容
				List valsc = Arrays.asList(valuec.split(","));// 子业务中key下的摘要数组
				List arrayList1 = new ArrayList(valsc);
				arrayList.removeAll(arrayList1);// 去除子业务已经存在的
				System.out.println(arrayList);
				// 添加批量插入的sql语句
				for (int q = 0; q < arrayList.size(); q++) {
					listsql.add(sql_insert);
					List list = new ArrayList();
					list.add(serviceids);
					list.add(key);
					list.add("<" + service + ">" + arrayList.get(q));
					paramList.add(list);
				}
			}
		}
		int count = Database.executeNonQueryTransaction(listsql, paramList);
		System.out.println(count);
		if (count > 0) {
			return "{\"checkInfo\":\"继承成功!\"}";
		}
		return "{\"checkInfo\":\"已经继承了所有!\"}";

	}

	public static void main(String[] args) {
		/**
		 * String str = "<VOD点播>交互序号选择,<VOD点播>视频点播交互,<VOD点播>歌曲交互"; String str1 ="<IPTV点播>交互序号选择,<IPTV点播>交互地区融合选择,<IPTV点播>交互导演融合选择,<IPTV点播>交互演员融合选择,<IPTV点播>交互集数融合选择"
		 * ; str = str.replaceAll("<[^<]{1,}>", ""); str1 =
		 * str1.replaceAll("<[^<]{1,}>", ""); List<String> list =
		 * Arrays.asList(str.split(",")); List<String> list1 =
		 * Arrays.asList(str1.split(",")); List<String> arrayList = new
		 * ArrayList<String>(list); List<String> arrayList1 = new
		 * ArrayList<String>(list1); arrayList1.removeAll(arrayList);
		 * System.out.println(str); System.out.println(arrayList1.toString());
		 */

	}

	// 传进来的是serviceName
	@SuppressWarnings("unchecked")
	public JSONArray GetJsonServiceByCityServiceForTree(String cityids,
			String serviceid,String sevicecontainer,String parentid) {

		String sql;
		Result res = null;
		// mysql
		if (getConfigValue.isMySQL) {
			sql = "SELECT getParentByServiceID('" + serviceid + "','" + cityids
					+ "') ";
			String result = "";
			try {
				result = Database.executeQueryAisa(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result != "") {
				String[] temp = result.split(",");
				sql = "";
				for (int i = 0; i < temp.length; i++) {
					if (i == 0) {
						sql += "select * from service where serviceid="
								+ temp[i] + "";
					} else {
						sql += " UNION ALL SELECT * FROM service WHERE serviceid="
								+ temp[i] + "";
					}
				}
				try {
					res = Database.executeQuery(sql);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (res == null || res.getRowCount() == 0) {
				return JSONArray.fromObject("[]");

			}
			SortedMap[] maps = res.getRows();
			JSONArray son = JSONArray.fromObject("[]");
			for (int i = 0; i < maps.length; i++) {

				SortedMap map = maps[i];
				JSONObject o = JSONObject.fromObject("{}");
				if (i == 0) {// 第一个为当前查询的，看是否有子业务
					boolean leaf = CommonLibServiceDAO.hasChildrenByServiceid(map.get("SERVICEID")
							.toString());
					o.put("leaf", leaf);
				} else {
					o.put("leaf", false);
				}

				o.put("text", map.get("SERVICE"));
				o.put("brand", map.get("BRAND"));
				o.put("serviceid", map.get("SERVICEID"));
				o.put("parentname", map.get("PARENTNAME"));
				o.put("cityid", cityids);
				o.put("id", map.get("SERVICEID"));
				Object c = map.get("container");
				if (c == null) {
					o.put("container", "");
				} else {
					o.put("container", c);
				}

				o.put("children", son);
				son = JSONArray.fromObject("[]");
				son.add(o);
			}
			return son;

			// oracle
		} else {
			String brand;
			Object sre = GetSession.getSessionByKey("accessUser");
			if(sre== null||"".equals(sre)){
				return null;
			}
			User user = (User)sre;
			String industryOrganizationApplication = user.getRealindustryOrganizationApplication();
			if("currentindustry".equals(sevicecontainer)){//加载当前行业主题树
				brand = "'"+industryOrganizationApplication.split("->")[0]+"主题'";	
				}else if("generalindustry".equals(sevicecontainer)){//加载通用行业主题树
				  brand = "'通用行业主题'";
				}else{//根据登录行业加载对应业务树
					brand = LoginDAO
					.getServiceRoot(industryOrganizationApplication).replace("@", ",");
				 // brand = MyClass.ServiceRoot();
//				  if(!" ".equals(brand)&&brand!=null){//临时处理库中"嘉实基金" 品牌为"基金服务"
//                   if(brand.indexOf("嘉实基金")!=-1){
//                	 brand = brand.replace("嘉实基金","基金服务");
//				   }
//                   
//                   else if(brand.indexOf("华夏基金")!=-1){
//                  	 brand = brand.replace("华夏基金","基金服务");
//  				   }
//                   else if(brand.indexOf("华夏基金(旧)")!=-1){
//                    	 brand = brand.replace("华夏基金(旧)","基金服务");
//    				   }
//				  }
				  }
			
//			if(parentid!=null && !"".equals(parentid)){
//				sql = "SELECT * FROM service 　　start "
//					+ "WITH service = ? "
//					+ "AND cityid  IN (?) 　and brand in("+brand+") and parentid ="+parentid+"　connect BY nocycle prior parentid = serviceid";
//			}else{
//				sql = "SELECT * FROM service 　　start "
//					+ "WITH service = ? "
//					+ "AND cityid  IN (?) 　and brand in("+brand+") 　connect BY nocycle prior parentid = serviceid";
//			}
//			
//			
//			sql = "select * from (SELECT * FROM service 　　start "
//				+ "WITH service = ? "
//				+ "AND cityid  IN (?) 　and brand in("+brand+") 　connect BY nocycle prior parentid = serviceid) cc where cc.service not in('个性化业务','电信集团','testservice_1_2')";
//			try {
//				res = Database.executeQuery(sql, serviceid, cityids);
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
			
			if(sre==null||"".equals(sre)){
				return JSONArray.fromObject("[]");
			}
			
			res = UserOperResource.getServiceForTree(user,parentid, brand, serviceid);
			
			if (res == null || res.getRowCount() == 0) {
				return JSONArray.fromObject("[]");

			}
			// 此时获取的数据为多个，一组一组的，我们先进行分组
			SortedMap[] maps = res.getRows();
			// System.out.println(maps.length); 
			List<ServicesMap> serviceList = new ArrayList<ServicesMap>();
			ServicesMap serviceSingle = null;
			ArrayList li = getMyservice(cityids,brand,serviceid);
			for (int i = 0; i < maps.length; i++) {
				SortedMap map = maps[i];
				String servicename = map.get("SERVICE").toString();
				String id = map.get("SERVICEID").toString();
				if (i == maps.length - 1) {// 最后一个
					HashMap servicesMap = serviceSingle.getList();
					ArrayList serviceKeys = serviceSingle.getIndexs();
					servicesMap.put(id, map);
					serviceKeys.add(id);
					if (isexist(serviceKeys.get(serviceKeys.size() - 2)
							.toString(), li)) {
						serviceList.add(serviceSingle);
					}
				} else if (serviceid.equals(servicename)) {// 如果为当前查询的业务，那么作为第一个
					if (serviceSingle != null) {// 如果之前已经有了，那么先加入到集合中

						ArrayList serviceKeys1 = serviceSingle.getIndexs();
						if (isexist(serviceKeys1.get(serviceKeys1.size() - 2)
								.toString(), li))
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
			JSONArray SON = creatTree(serviceList, cityids);
			// 排序结束后，进行2次遍历
			return SON;

		}
	}

	@SuppressWarnings("unchecked")
	public boolean isexist(String key, ArrayList li) {
		boolean res = false;
		for (int i = 0; i < li.size(); i++) {
			String service = li.get(i).toString();
			if (service.equals(key)) {
				res = true;
				break;
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public ArrayList getMyservice(String citysid,String brand,String service ) {
//		String sql = "SELECT DISTINCT ss.serviceid " + "FROM service ss , "
//				+ "  roleservicerel ree, " + "  workerrolerel wr "
//				+ "WHERE ree.service = ss.service "
//				+ "AND ree.roleid    = wr.roleid " + "AND cityid       IN("
//				+ citysid + ") " + "AND wr.workerid   = '"
//				+ MyClass.LoginUserId() + "'";
		String serviceRoot = MyClass.ServiceRoot();
		String sql;
//		if(brand.indexOf("行业主题")!=-1){
//			 sql = "SELECT DISTINCT ss.serviceid " + "FROM service ss where ss.service in("+brand+")";	
//		}else{
//			 sql ="SELECT DISTINCT ss.serviceid  FROM (SELECT * FROM service 　　start WITH service = '"+service+"' AND cityid  IN (284) 　connect BY nocycle prior parentid = serviceid) ss ";
//		}
		
	
		Result res = null;
//		try {
//			res = Database.executeQuery(sql);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		if(brand.indexOf("行业主题")!=-1){
			res = UserOperResource.getServiceIDByBrand(service,brand);
		}else{
			res = UserOperResource.getServiceIDByService(service,brand);
		}
	
		
		if (res == null || res.getRowCount() == 0) {
			return null;
		} else {
			ArrayList li = new ArrayList();
			for (int i = 0; i < res.getRowCount(); i++) {
				li.add(res.getRows()[i].get("SERVICEID").toString());
			}
			return li;
		}

	}
	
	

	/**
	 *@description   查找模板列名
	 *@param name    查询neir
	 *@param serviceid
	 *@return 
	 *@returnType JSONArray 
	 */
	public static JSONArray getTemplateColumnNameByColumServiceidForDDL(String name,
			String serviceid) {
		Result res = null;
		String sql;
	String content ="";
       if ("null".equals(name)|| "".equals(name)||name == null){
    	   content ="";
       }else{
    	   content = name.toUpperCase();
       }
        res = CommonLibServiceAttrDao.getLikeColumn(content, serviceid);
		SortedMap[] maps = res.getRows();
		for (int i = 0; i < res.getRowCount(); i++) {
			SortedMap map = maps[i];
			Object o = map.get("name");
			map.remove("name");
			map.put("text", o);
		} 
		JSONArray arr = JSONArray.fromObject(maps);
		return arr;
	}
	
	
	

	/**
	 * 方法名称： creatTree 内容摘要：根据业务树组，生成一棵树 修改者名字 修改日期 修改说明
	 * 
	 * @author c_wolf 2014-8-7
	 * @param serviceList
	 *            void
	 * @throws
	 * 
	 */
	@SuppressWarnings("unchecked")
	private JSONArray creatTree(List<ServicesMap> serviceList, String cityids) {
		// 使用第一条数据生成一棵基本树,这棵树是最长的一棵
		ServicesMap smap = serviceList.get(0);
		HashMap<String, SortedMap> servicemap = smap.getList();
		ArrayList servicekeys = smap.getIndexs();
		JSONArray son = JSONArray.fromObject("[]");
		for (int i = 0; i < servicekeys.size(); i++) {
			SortedMap map = servicemap.get(servicekeys.get(i));
			JSONObject o = JSONObject.fromObject("{}");
			if (i == 0) {// 第一个为当前查询的，看是否有子业务
				boolean leaf = CommonLibServiceDAO.hasChildrenByServiceid(map.get("SERVICEID")
						.toString());
				o.put("leaf", leaf);
			} else {
				o.put("leaf", false);
			}

			o.put("text", map.get("SERVICE"));
			o.put("brand", map.get("BRAND"));
			o.put("serviceid", map.get("SERVICEID"));
			o.put("parentname", map.get("PARENTNAME"));
			o.put("cityid", cityids);
			o.put("id", map.get("SERVICEID"));
			Object c = map.get("container");
			if (c == null) {
				o.put("container", "");
			} else {
				o.put("container", c);
			}
			Object o6 = map.get("MINCREDIT");
			o.put("mincredit", o6);
			o.put("children", son);
			son = JSONArray.fromObject("[]");
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
					JSONArray arrN = JSONArray.fromObject("[]");
					for (int z = 0; z < servicekeysN.size() - x; z++) {
						SortedMap map = servicemapN.get(servicekeysN.get(z));
						JSONObject o = JSONObject.fromObject("{}");
						if (z == 0) {// 第一个为当前查询的，看是否有子业务
							boolean leaf = HasChildrenByServiceid(map.get(
									"SERVICEID").toString());
							o.put("leaf", leaf);
						} else {
							o.put("leaf", false);
						}

						o.put("text", map.get("SERVICE"));
						o.put("brand", map.get("BRAND"));
						o.put("serviceid", map.get("SERVICEID"));
						o.put("parentname", map.get("PARENTNAME"));
						o.put("cityid", cityids);
						o.put("id", map.get("SERVICEID"));
						Object c = map.get("container");
						if (c == null) {
							o.put("container", "");
						} else {
							o.put("container", c);
						}

						o.put("children", arrN);
						arrN = JSONArray.fromObject("[]");
						arrN.add(o);
					}
					arr.add(arrN.get(0));
					break;// 生成树后，跳出循环

				}
			}
		}
		return son;
	}
}

class MyListComp implements Comparator<ServicesMap> {
	public int compare(ServicesMap object1, ServicesMap object2) {
		int r = object1.getIndexs().size() - object1.getIndexs().size();
		return r;
	}
}

class ServicesMap {
	@SuppressWarnings("unchecked")
	private HashMap<String, SortedMap> list = new HashMap<String, SortedMap>();
	private ArrayList<String> keys = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	public HashMap<String, SortedMap> getList() {
		return list;
	}

	@SuppressWarnings("unchecked")
	public void setList(HashMap<String, SortedMap> list) {
		this.list = list;
	}

	public ArrayList<String> getIndexs() {
		return keys;
	}

	public void setIndexs(ArrayList<String> indexs) {
		this.keys = indexs;
	}

	public int getIndex(String key) {
		if (list.containsKey(key)) {
			for (int i = 0; i < keys.size(); i++) {
				String nkey = keys.get(i);
				if (key.equals(nkey)) {
					return keys.size() - i;
				}
			}

			return 0;
		} else {
			return -1;
		}
	}

}