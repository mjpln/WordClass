package com.knowology.km.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import com.knowology.km.bll.WorditemSentenceDAO;

public class WorditemSentenceAction implements ServletRequestAware {
	private HttpServletRequest request;
	private int start;
	private int limit;
	private String worditem;
	private Boolean worditemprecise;
	private Boolean iscurrentwordclass;
	private String worditemtype;
	private String curwordclass;

	private String oldworditem;
	private String newworditem;
	private String oldtype;
	private String newtype;
	private String wordclassid;
	private String wordid;

	private String curwordclassid;
	private Boolean isstandardword;
	private String curwordclasstype;
	private String cityname;
	private String citycode;
	private Boolean isInclude;
	private String isHaveSynonym;

	private String action;
	private Object m_result;

	public String execute() {
		if ("select".equals(action)) {// 查询子句词条
			m_result = WorditemSentenceDAO.select(start, limit, worditem,
					worditemprecise, iscurrentwordclass, worditemtype,
					curwordclass, citycode);
		} else if ("update".equals(action)) {// 更新子句词条
			m_result = WorditemSentenceDAO.update(oldworditem, newworditem,
					oldtype, newtype, wordclassid, wordid,curwordclass,curwordclasstype,request);
		} else if ("insert".equals(action)) {// 新增子句词条
			m_result = WorditemSentenceDAO.insert(worditem, curwordclass,
					curwordclassid, curwordclasstype,isstandardword, citycode, cityname, request);
		} else if ("delete".equals(action)) {// 删除子句词条
			m_result = WorditemSentenceDAO.delete(wordid, curwordclass,curwordclasstype,
					worditem);
		} else if ("updateWordCity".equals(action)){
			m_result = WorditemSentenceDAO.updateSentenceCity(curwordclass,wordid,cityname,citycode);
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

	public String getWorditem() {
		return worditem;
	}

	public void setWorditem(String worditem) {
		this.worditem = worditem;
	}

	public Boolean getWorditemprecise() {
		return worditemprecise;
	}

	public void setWorditemprecise(Boolean worditemprecise) {
		this.worditemprecise = worditemprecise;
	}

	public Boolean getIscurrentwordclass() {
		return iscurrentwordclass;
	}

	public void setIscurrentwordclass(Boolean iscurrentwordclass) {
		this.iscurrentwordclass = iscurrentwordclass;
	}

	public String getWorditemtype() {
		return worditemtype;
	}

	public void setWorditemtype(String worditemtype) {
		this.worditemtype = worditemtype;
	}

	public String getCurwordclass() {
		return curwordclass;
	}

	public void setCurwordclass(String curwordclass) {
		this.curwordclass = curwordclass;
	}

	public String getOldworditem() {
		return oldworditem;
	}

	public void setOldworditem(String oldworditem) {
		this.oldworditem = oldworditem;
	}

	public String getNewworditem() {
		return newworditem;
	}

	public void setNewworditem(String newworditem) {
		this.newworditem = newworditem;
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

	public String getWordclassid() {
		return wordclassid;
	}

	public void setWordclassid(String wordclassid) {
		this.wordclassid = wordclassid;
	}

	public String getWordid() {
		return wordid;
	}

	public void setWordid(String wordid) {
		this.wordid = wordid;
	}

	public String getCurwordclassid() {
		return curwordclassid;
	}

	public void setCurwordclassid(String curwordclassid) {
		this.curwordclassid = curwordclassid;
	}

	public Boolean getIsstandardword() {
		return isstandardword;
	}

	public void setIsstandardword(Boolean isstandardword) {
		this.isstandardword = isstandardword;
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

	public String getCurwordclasstype() {
		return curwordclasstype;
	}

	public void setCurwordclasstype(String curwordclasstype) {
		this.curwordclasstype = curwordclasstype;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public void setIsInclude(Boolean isInclude) {
		this.isInclude = isInclude;
	}

	public Boolean getIsInclude() {
		return isInclude;
	}

	public void setIsHaveSynonym(String isHaveSynonym) {
		this.isHaveSynonym = isHaveSynonym;
	}

	public String getIsHaveSynonym() {
		return isHaveSynonym;
	}

}
