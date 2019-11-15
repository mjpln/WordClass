package com.knowology.km.bll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.bll.CommonLibIndustryApplicationToServicesDAO;
import com.knowology.bll.CommonLibUserDAO;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.bll.MyClass;
import com.knowology.dal.Database;
import com.knowology.km.entity.GetUserParam;

public class LoginDAO {
	public static GetUserParam CheckUserOld(String id, String password,
			String inOrgApp) {
		if (id == null || password == null)
			return null;
		Result rs = null;
		GetUserParam user = new GetUserParam();
		String industryOrganizationApplication = null;
		String serviceroot = null;
		try {
			/** 数据层封装 */
			rs = UserOperResource.getUserInfo(id);
			if (rs == null || rs.getRows().length == 0) {
				return null;
			} else if (!rs.getRows()[0].get("pwd").toString().equals(
					MyClass.EncryptMD5(password))) {
				return null;
			} else {
				user.setWorkerid(id);
				user.setName(rs.getRows()[0].get("name").toString());
				rs = UserOperResource.getUserRoleInfo(id);
				if (rs != null && rs.getRows().length != 0) {
					String role = rs.getRows()[0].get("rolename").toString();
					user.setRole(role);
					String customer = rs.getRows()[0].get("customer") == null ? ""
							: rs.getRows()[0].get("customer").toString();
					if (!role.contains("云平台")) {
						// if(!"全行业".equals(customer)){
						industryOrganizationApplication = rs.getRows()[0].get(
								"customer").toString();
						if ("全行业".equals(industryOrganizationApplication)) {
							serviceroot = industryOrganizationApplication;
						} else {
							serviceroot = LoginDAO
									.getServiceRoot(industryOrganizationApplication);
						}

						user.setServiceroot(serviceroot);
						user
								.setIndustryOrganizationApplication(industryOrganizationApplication);
					} else {
						serviceroot = LoginDAO.getServiceRoot(inOrgApp);
						user.setServiceroot(serviceroot);
						user.setIndustryOrganizationApplication(inOrgApp);
					}

				} else {
					user.setRole("fail");
				}
				return user;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *验证用户并封装User
	 * 
	 * @param id
	 *            用户ID
	 *@param password
	 *            密码
	 *@param inOrgApp
	 *            登录四层结构信息
	 *@return
	 *@returnType GetUserParam
	 */
	public static GetUserParam CheckUser(String id, String password,
			String inOrgApp) {
		if (id == null || password == null)
			return null;
		Result rs = null;
		GetUserParam user = new GetUserParam();
		String industryOrganizationApplication = null;
		String serviceroot = null;
		rs = UserOperResource.getUserInfo(id);
		if (rs == null || rs.getRows().length == 0) {
			return null;
//		} else if (!rs.getRows()[0].get("pwd").toString().equals(
//				MyClass.EncryptMD5(password))) {
		} else if (!rs.getRows()[0].get("pwdn").toString().equals(
				password)) {
			return null;
		} else {
			user.setWorkerid(id);
			user.setName(rs.getRows()[0].get("name").toString());
			/** 数据层封装 */
			rs = UserOperResource.getUserRoleInfo(id);
			if (rs != null && rs.getRows().length != 0) {
				// 角色
				String role = rs.getRows()[0].get("rolename").toString();
				user.setRole(role);
				// 角色 customer
				String customer = rs.getRows()[0].get("customer") == null ? ""
						: rs.getRows()[0].get("customer").toString();
// 华夏特殊需求

				if (!"全行业".equals(customer)) {

					if ("基金行业->华夏基金->对内应用".equals(customer)) {
						industryOrganizationApplication = "基金行业->华夏基金->多渠道应用";
						serviceroot = LoginDAO
								.getServiceRoot("基金行业->华夏基金->对内应用");

						user.setServiceroot(serviceroot);
						user
								.setIndustryOrganizationApplication(industryOrganizationApplication);
						user.setCustomer("基金行业->华夏基金->对内应用");
						user.setRealindustryOrganizationApplication("基金行业->华夏基金->对内应用");
					} else {
						industryOrganizationApplication = customer;
						serviceroot = LoginDAO
								.getServiceRoot(industryOrganizationApplication);

						user.setServiceroot(serviceroot);
						user
								.setIndustryOrganizationApplication(industryOrganizationApplication);
						user.setCustomer(industryOrganizationApplication);
						user.setRealindustryOrganizationApplication(industryOrganizationApplication);
					}
				} else {
					if ("基金行业->华夏基金->对内应用".equals(inOrgApp)) {
						serviceroot = LoginDAO
								.getServiceRoot("基金行业->华夏基金->对内应用");
						user.setServiceroot(serviceroot);
						user
								.setIndustryOrganizationApplication("基金行业->华夏基金->多渠道应用");
						user.setCustomer(customer);
						user.setRealindustryOrganizationApplication("基金行业->华夏基金->对内应用");
					} else {
						serviceroot = LoginDAO.getServiceRoot(inOrgApp);
						user.setServiceroot(serviceroot);
						user.setIndustryOrganizationApplication(inOrgApp);
						user.setCustomer(customer);
						user.setRealindustryOrganizationApplication(inOrgApp);
						
					}

				}

			} else {
				user.setRole("fail");
			}
			return user;
		}

	}

	/**
	 *通过四层接口串获得业务根
	 * 
	 * @param industryOrganizationApplication
	 *            四层接口串
	 *@return
	 *@returnType String
	 */
	public static String getServiceRoot(String industryOrganizationApplication) {
		StringBuilder sb = new StringBuilder();
		List<String> paras = new ArrayList<String>();
		Result res = null;
		String ioaArray[] = industryOrganizationApplication.split("->");
		res = UserOperResource.getServiceRoot(ioaArray[0], ioaArray[1],
				ioaArray[2]);
		String serviceroot = res.getRows()[0].get("serviceroot").toString();
		String servicearray[] = serviceroot.split("\\|");
		String serviceString = "";
		for (int s = 0; s < servicearray.length; s++) {
			serviceString += "'" + servicearray[s] + "'@";
		}
		serviceString = serviceString.substring(0, serviceString.length() - 1);
		return serviceString;
	}
	


	/**
	 *获得登录行业 商家 应用
	 *@param sqlid
	 *@param paras
	 *@return 
	 *@returnType Object 
	 */
	public static Object getServiceType(String sqlid,String paras) {
		Result rs =null;
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		if("select_ddl_industry".equals(sqlid)){//加载行业
			rs = CommonLibIndustryApplicationToServicesDAO.getIndustry();
		}else if("select_ddl_organization".equals(sqlid)){//加载商家
			rs = CommonLibIndustryApplicationToServicesDAO.getOrganizationByIndustry(paras);
		}else if("select_ddl_application".equals(sqlid)){//加载应用
			rs = CommonLibIndustryApplicationToServicesDAO.getApplicationByIndustryAndOrganization(paras.split(",")[0],paras.split(",")[1]);
		}
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				String name = rs.getRows()[i].get("name") != null ? rs
						.getRows()[i].get("name").toString()
						: "";
						obj.put("NAME", name);
						// 将生成的对象放入jsonArr数组中
						jsonArr.add(obj);	
			}
			jsonObj.put("state","success");
			jsonObj.put("rows", jsonArr);
			
		}else{
			jsonObj.put("state","false");
			jsonObj.put("rows", jsonArr);
		}
		
		
		return jsonObj;
	}
	

	/**
	 *获得角色customer：四层结构串
	 * 
	 * @param id
	 *            用户ID
	 *@return
	 *@returnType List
	 */
	public static List getCustomerAndRoleName(String id) {
		Result rs = UserOperResource.getUserRoleInfo(id);
		List<String> customerlist = new ArrayList<String>();
		String customer = "";
		String rolename = "";
		if (rs != null && rs.getRows().length != 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				customer = rs.getRows()[0].get("customer") == null ? "" : rs
						.getRows()[0].get("customer").toString();
				rolename = rs.getRows()[0].get("rolename") == null ? "" : rs
						.getRows()[0].get("rolename").toString();
				customerlist.add(customer);
				customerlist.add(rolename);
			}
		}
		return customerlist;
	}

	/**
	 *@description 获得角色名
	 *@param id
	 *@return 
	 *@returnType String 
	 */
	public static String getRole(String id) {
		String sql = "select r.rolename as rolename, r.customer  customer from role r,workerrolerel w where r.roleid=w.roleid and  w.workerid='"
				+ id + "'";
		Result rs = null;
		try {
			rs = Database.executeQuery(sql);
		} catch (Exception e) {
			return "";
		}
		String rolename = "";
		if (rs != null && rs.getRows().length != 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String role = rs.getRows()[i].get("rolename").toString();
				rolename += role + "|";
			}
			rolename = rolename.substring(0, rolename.lastIndexOf("|"));
			return rolename;
		} else {
			return "";
		}
	}

}
