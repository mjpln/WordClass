package com.knowology.km.action;

import java.sql.SQLException;

import com.alibaba.fastjson.JSONObject;
import com.knowology.km.bll.QuestionUploadDao;
import com.knowology.km.bll.WorditemDAO;

public class QuestionUploadAction {
	
	private Object m_result;
	private String id;
	
	private int page;
	private int rows;
	
	private String question;
	private String other;
	private String starttime;
	private String endtime;
	private String username;
	private String status;
	private String hot;
	private String hot2;
	private String selProvince;
	private String selCity;
	private int sid;
	private Integer pid;
	private String pid2;
	private String reason;
	private String solution;
	private String fileName;
	
	private String attrArr;
	private String url;
	
	private String ids;
	private String local;
	private String sign;
	private boolean isitem;
	
	private int type;
	private Boolean wordprecise;
	private Boolean iscurrentword;
	private String wordtype;
	private String curwordclass;
	private String citycode;
	private String word;
	private String contatiner;
	private String curworditem;
	private String request;
	
	// 删除问法
	public String deleteOther(){
		m_result = QuestionUploadDao.deleteOther(ids);
		return "success";
	}
	 // 更新单条问题
	public String updateQueName(){
		m_result = QuestionUploadDao.updateQueName(pid,question,sid,other,selProvince,selCity);
		return "success";
	}
	// 删除同义问法
	public String delOther() {
		m_result = QuestionUploadDao.delOther(sid);
		return "success";
	}
	
	// 获取session
	public String getSession(){
		m_result = QuestionUploadDao.getSession();
		return "success";
	}
	
	// 获取下拉省份
	public String getProvince(){
		m_result = QuestionUploadDao.selProvince();
		return "success";
	}
	
	// 获取下拉城市
	public String getCity(){
		m_result = QuestionUploadDao.getCity(id);
		return "success";
	}
	
	// 分页查询所有问法
	public String gethotquestion(){
		m_result = QuestionUploadDao.gethotquestion(page,rows,question,other,starttime,endtime,username,status,selProvince,selCity,hot,hot2,pid,ids);
		return "success";
	}
	
	// 分页查询热点问法
	public String gethotquestion2(){
		m_result = QuestionUploadDao.gethotquestion2(page,rows,question,starttime,endtime,status,selProvince,selCity,hot,hot2,pid);
		return "success";
	}
	
	// 设置热点问法
	public String setAttr(){
		m_result = QuestionUploadDao.setAttr(ids);
		return "success";
	}
	
	// 导出excel(全部)
	public String exportxls(){
		m_result = QuestionUploadDao.ExportExcel();
		return "success";
	}
	
	// 导出选中问法
	public String ExportExcel(){
		m_result = QuestionUploadDao.ExportExcel(ids);
		return "success";
	}
	
	// 根据条件全量下载问法
	public String ExportExcel2(){
		m_result = QuestionUploadDao.ExportExcel2(question,other,starttime,endtime,username,status,selProvince,selCity);
		return "success";
	}

	// 报错提交
	public String doSaveReport(){
		m_result = QuestionUploadDao.doSaveReport(ids,reason,solution);
		return "success";
	}

	// 导入问法
	public String importxls(){
		m_result = QuestionUploadDao.ImportExcel(fileName);
		return "success";
	}
	
	// 批量理解
	public String understand(){
		m_result = QuestionUploadDao.understand(attrArr,url);
		return "success";
	}
	
	// 获取热点问题的同义问法
	public String getsonquestion(){
		m_result = QuestionUploadDao.getsonquestion(question,pid2,status,rows,page);
		return "success";
	}
	
	// 插入同义问法
	public String insertother(){
		m_result = QuestionUploadDao.insertother(question,pid,selProvince,selCity);
		return "success";
	}
	
	// 获取选中地市
	public String selLocal(){
		m_result = QuestionUploadDao.selLocal(local);
		return "success";
	}
	
	// 获取地市树
	public String getCityTree(){
//		m_result = WorditemDAO.getCityTree(local);//省市县三级
		m_result = QuestionUploadDao.getCityTree(local,sign);//省市二级
		return "success";
	}
	//根据用户权限获取地市树
	public String getAddCityTree(){
		m_result = QuestionUploadDao.getAddCityTree(isitem, sign);
		return "success";
	}
	//获取别名添加时的默认地市
	public String getSynonymCity(){
		m_result = QuestionUploadDao.getSynonymCity( sign);
		return "success";
	}
	//获取筛选地市树
	public String getSelectCityTree(){
		m_result = QuestionUploadDao.getSelectCityTree(isitem);
		return "success";
	}
	// 通过登录信息获取地市树
	public String getCityTreeByLoginInfo(){
//		m_result = WorditemDAO.getCityTree(local);//省市县三级
		m_result = QuestionUploadDao.getCityTreeByLoginInfo(local);//省市二级
		return "success";
	}
	
	// 下载示例
	public String exportexample(){
		m_result = QuestionUploadDao.exportexample();
		return "success";
	}
	
	// 删除原有示例
	public String exsitfile(){
		m_result = QuestionUploadDao.exsitfile();
		return "success";
	}
	//导出词库
	public String exportWord(){
		if(!"".equals(request)&&request!=null){
			
			// 解析参数
			JSONObject json = JSONObject.parseObject(request);
			curwordclass = json.getString("curwordclass");
			word = json.getString("word");
			type = json.getIntValue("type");
			wordprecise = json.getBoolean("wordprecise");
			iscurrentword = json.getBoolean("iscurrentword");
			wordtype = json.getString("wordtype");
			contatiner = json.getString("contatiner");
			curworditem = json.getString("curworditem");
			citycode = json.getString("citycode");
			if(citycode!=null){
				citycode = citycode.replace("\"", "").replace("[", "").replace("]", "");
			}
		}
		QuestionUploadDao.exportSingleWord(1, "", contatiner);
		//QuestionUploadDao.exportWord(type, word, wordprecise, iscurrentword, wordtype, curwordclass, curworditem, citycode,contatiner);
		return null;
	}
	public String exportSingleWord(){
		return null;
	}
	public String isShow(){
		m_result = QuestionUploadDao.isShow();
		return "success";
	}
	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getSelCity() {
		return selCity;
	}

	public void setSelCity(String selCity) {
		this.selCity = selCity;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getSelProvince() {
		return selProvince;
	}

	public void setSelProvince(String selProvince) {
		this.selProvince = selProvince;
	}

	public String getHot() {
		return hot;
	}

	public void setHot(String hot) {
		this.hot = hot;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getHot2() {
		return hot2;
	}

	public void setHot2(String hot2) {
		this.hot2 = hot2;
	}

	public String getAttrArr() {
		return attrArr;
	}

	public void setAttrArr(String attrArr) {
		this.attrArr = attrArr;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getPid2() {
		return pid2;
	}

	public void setPid2(String pid2) {
		this.pid2 = pid2;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public void setIsitem(boolean isitem) {
		this.isitem = isitem;
	}
	public boolean isIsitem() {
		return isitem;
	}
	public void setWordprecise(Boolean wordprecise) {
		this.wordprecise = wordprecise;
	}
	public Boolean getWordprecise() {
		return wordprecise;
	}
	public void setIscurrentword(Boolean iscurrentword) {
		this.iscurrentword = iscurrentword;
	}
	public Boolean getIscurrentword() {
		return iscurrentword;
	}
	public void setWordtype(String wordtype) {
		this.wordtype = wordtype;
	}
	public String getWordtype() {
		return wordtype;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getWord() {
		return word;
	}
	public void setContatiner(String contatiner) {
		this.contatiner = contatiner;
	}
	public String getContatiner() {
		return contatiner;
	}
	public void setCurwordclass(String curwordclass) {
		this.curwordclass = curwordclass;
	}
	public String getCurwordclass() {
		return curwordclass;
	}
	public void setCurworditem(String curworditem) {
		this.curworditem = curworditem;
	}
	public String getCurworditem() {
		return curworditem;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getRequest() {
		return request;
	}

}
