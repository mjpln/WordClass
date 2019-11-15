package com.knowology.km.action;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserManager;
import com.knowology.km.bll.WordclassDAO;

public class WordclassAction {
	private String wordclass;
	private int start;
	private int limit;

	private String id;
	private String oldvalue;
	private String newvalue;
	private String oldwordclasstype;
	private String newwordclasstype;
	private Boolean wordclassprecise;
	private String wordclasstype;
	private String wordclassid;
	private String userid;
	private String ioa;
	private String citycode;
	private String cityname;
	private String isHaveItem;
	
	private String action;
	private Object m_result;
	//km点击基础词库页面进入
	public String index() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		//判断用户id、登陆路径
		if(userid!=null&&ioa!=null) {
			User user = UserManager.constructLoginUser(userid, ioa);
			if(user!=null) {
				//用户商家权限判断
				String customer = user.getCustomer();
				if (ioa.contains("4G业务客服")||ioa.contains("IVR机器人")||ioa.contains("指令系统应用")||ioa.contains("国漫中心IVR")||ioa.contains("中国电信集团话务质检")){
					user.setPower(true);
				}else if("全行业".equals(customer)) {
					user.setPower(true);
				}else {
					user.setPower(false);
				}
				session.setAttribute("accessUser", user);
			}
		} else {
			//获取ServletContext，然后可通过context.getContext("/KM")获取KM的ServletContext
			ServletContext context = session.getServletContext();
			ServletContext contextKM = context.getContext("/KM");
			if(contextKM!=null) {
				//通过KM的ServletContext获取session，实现session共享
				HttpSession sessionKM = (HttpSession)contextKM.getAttribute("session");
				if(sessionKM!=null) {
					Object obj = sessionKM.getAttribute("accessUser");
					if(obj!=null) {
						JSONObject json = (JSONObject)JSONObject.toJSON(sessionKM.getAttribute("accessUser"));
						User user = JSONObject.toJavaObject(json, User.class);
						session.setAttribute("accessUser", user);
					}				
				}
			}
		}
		
		return "success";
	}
	
	public String execute() {
		if ("select".equals(action)) {// 查询词类
			m_result = WordclassDAO.select(wordclass,wordclassprecise,wordclasstype, start, limit,citycode);
		} else if ("update".equals(action)) {// 修改词类
			m_result = WordclassDAO.update(id, oldvalue, newvalue,oldwordclasstype,newwordclasstype);
		} else if ("insert".equals(action)) {// 新增词类
			m_result = WordclassDAO.insert(wordclass,wordclasstype);
		} else if ("delete".equals(action)) {// 删除词类
			m_result = WordclassDAO.delete(wordclassid, wordclass,wordclasstype);
		}
		return "success";
	}

	public String getWordclass() {
		return wordclass;
	}

	public void setWordclass(String wordclass) {
		this.wordclass = wordclass;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOldvalue() {
		return oldvalue;
	}

	public void setOldvalue(String oldvalue) {
		this.oldvalue = oldvalue;
	}

	public String getNewvalue() {
		return newvalue;
	}

	public void setNewvalue(String newvalue) {
		this.newvalue = newvalue;
	}

	public String getWordclassid() {
		return wordclassid;
	}

	public void setWordclassid(String wordclassid) {
		this.wordclassid = wordclassid;
	}

	public Boolean isWordwodclassprecise() {
		return wordclassprecise;
	}

	public void setWordwodclassprecise(boolean wordclassprecise) {
		this.wordclassprecise = wordclassprecise;
	}

	public Boolean getWordclassprecise() {
		return wordclassprecise;
	}

	public void setWordclassprecise(Boolean wordclassprecise) {
		this.wordclassprecise = wordclassprecise;
	}

	public String getOldwordclasstype() {
		return oldwordclasstype;
	}

	public void setOldwordclasstype(String oldwordclasstype) {
		this.oldwordclasstype = oldwordclasstype;
	}

	public String getNewwordclasstype() {
		return newwordclasstype;
	}

	public void setNewwordclasstype(String newwordclasstype) {
		this.newwordclasstype = newwordclasstype;
	}

	public String getWordclasstype() {
		return wordclasstype;
	}

	public void setWordclasstype(String wordclasstype) {
		this.wordclasstype = wordclasstype;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getIoa() {
		return ioa;
	}

	public void setIoa(String ioa) {
		this.ioa = ioa;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getCitycode() {
		return citycode;
	}


	public void setIsHaveItem(String isHaveItem) {
		this.isHaveItem = isHaveItem;
	}

	public String getIsHaveItem() {
		return isHaveItem;
	}
}
