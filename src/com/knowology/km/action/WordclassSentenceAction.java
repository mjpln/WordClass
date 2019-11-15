package com.knowology.km.action;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.alibaba.fastjson.JSONObject;
import com.knowology.GlobalValue;
import com.knowology.Bean.User;
import com.knowology.km.access.UserManager;
import com.knowology.km.bll.WordclassSentenceDAO;

public class WordclassSentenceAction {
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
	private String question;

	private String action;
	private Object m_result;

	public String index() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		
		if(userid!=null&&ioa!=null) {
			User user = UserManager.constructLoginUser(userid, ioa);
			if(user!=null) {
				//session中加入user
				//if (ioa.contains("4Gå®¢æåºç¨")||ioa.contains("IVRæºå¨äºº")||ioa.contains("æä»¤ç³»ç»åºç¨")){
				String customer = user.getCustomer();
				if (ioa.contains("4G业务客服")||ioa.contains("IVR机器人")||ioa.contains("指令系统应用")||ioa.contains("国漫中心IVR")||ioa.contains("中国电信集团话务质检")){
					user.setPower(true);
				}else if("全行业".equals(customer)) {
					user.setPower(true);
				}else{
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
		if ("select".equals(action)) {// 查询子句词库
			m_result = WordclassSentenceDAO.select(wordclass, wordclassprecise,wordclasstype,start, limit, citycode);
		} else if ("update".equals(action)) {// 更新子句词库
			m_result = WordclassSentenceDAO 
			.update(id, oldvalue, newvalue,oldwordclasstype,newwordclasstype);
		} else if ("insert".equals(action)) {// 新增子句词类
			m_result = WordclassSentenceDAO.insert(wordclass,wordclasstype);
		} else if ("delete".equals(action)) {// 删除子句词类
			m_result = WordclassSentenceDAO.delete(wordclassid, wordclass,wordclasstype);
		} else if ("wordpat".equals(action)) {
			m_result = WordclassSentenceDAO.wordpat(question, citycode);
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

	public Boolean getWordclassprecise() {
		return wordclassprecise;
	}

	public void setWordclassprecise(Boolean wordclassprecise) {
		this.wordclassprecise = wordclassprecise;
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

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getCityname() {
		return cityname;
	}

	public void setIsHaveItem(String isHaveItem) {
		this.isHaveItem = isHaveItem;
	}

	public String getIsHaveItem() {
		return isHaveItem;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQuestion() {
		return question;
	}

	
}
