package com.knowology.km.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import com.knowology.km.bll.SynonymSentenceDAO;

public class SynonymSentenceAction implements ServletRequestAware {
	private HttpServletRequest request;
	private int start;
	private int limit;
	private String synonym;
	private Boolean isprecise;
	private Boolean iscurrentworditem;
	private Boolean iscurrentwordclass;
	private String type;
	private String curworditem;
	private String curwordclass;

	private String oldsynonym;
	private String newsynonym;
	private String oldtype;
	private String newtype;
	private String wordid;
	private String stdwordid;

	private String wordclassid;
	private String citycode;
	private String cityname;
	private String action;
	private Object m_result;
	private Boolean isInclude;

	public String execute() {
		if ("select".equals(action)) {// 查询子句别名
			m_result = SynonymSentenceDAO.selectNew(start, limit, synonym,
					isprecise, iscurrentworditem, iscurrentwordclass, type, curworditem,
					curwordclass , citycode);
		} else if ("update".equals(action)) {// 更新子句别名
			m_result = SynonymSentenceDAO.update(oldsynonym, newsynonym,
					oldtype, newtype, wordid, stdwordid, curworditem,curwordclass,request);
		} else if ("insert".equals(action)) {// 新增子句别名
			m_result = SynonymSentenceDAO.insert(wordclassid, synonym, type,
					stdwordid, curworditem, curwordclass,request, citycode, cityname);
		} else if ("delete".equals(action)) {// 删除子句别名
			m_result = SynonymSentenceDAO.delete(stdwordid, synonym,
					curworditem, curwordclass);
		} else if ("updatesynonymcity".equals(action)) {
			m_result = SynonymSentenceDAO.updateSynonymCity(curwordclass, wordid, citycode, cityname);
		}
		return "success";
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

	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}

	public Boolean getIsprecise() {
		return isprecise;
	}

	public void setIsprecise(Boolean isprecise) {
		this.isprecise = isprecise;
	}

	public Boolean getIscurrentworditem() {
		return iscurrentworditem;
	}

	public void setIscurrentworditem(Boolean iscurrentworditem) {
		this.iscurrentworditem = iscurrentworditem;
	}
	public Boolean getIscurrentwordclass() {
		return iscurrentwordclass;
	}

	public void setIscurrentwordclass(Boolean iscurrentwordclass) {
		this.iscurrentwordclass = iscurrentwordclass;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCurworditem() {
		return curworditem;
	}

	public void setCurworditem(String curworditem) {
		this.curworditem = curworditem;
	}

	public String getCurwordclass() {
		return curwordclass;
	}

	public void setCurwordclass(String curwordclass) {
		this.curwordclass = curwordclass;
	}

	public String getOldsynonym() {
		return oldsynonym;
	}

	public void setOldsynonym(String oldsynonym) {
		this.oldsynonym = oldsynonym;
	}

	public String getNewsynonym() {
		return newsynonym;
	}

	public void setNewsynonym(String newsynonym) {
		this.newsynonym = newsynonym;
	}

	public String getOldtype() {
		return oldtype;
	}

	public void setOldtype(String oldtype) {
		this.oldtype = oldtype;
	}

	public String getNewtype() {
		return newtype;
	}

	public void setNewtype(String newtype) {
		this.newtype = newtype;
	}

	public String getWordid() {
		return wordid;
	}

	public void setWordid(String wordid) {
		this.wordid = wordid;
	}

	public String getStdwordid() {
		return stdwordid;
	}

	public void setStdwordid(String stdwordid) {
		this.stdwordid = stdwordid;
	}

	public String getWordclassid() {
		return wordclassid;
	}

	public void setWordclassid(String wordclassid) {
		this.wordclassid = wordclassid;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}
	
	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public void setIsInclude(Boolean isInclude) {
		this.isInclude = isInclude;
	}

	public Boolean getIsInclude() {
		return isInclude;
	}
	
}
