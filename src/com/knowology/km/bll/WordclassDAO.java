package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.jsp.jstl.sql.Result;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.entity.UserCity;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

public class WordclassDAO {
	/**
	 * 根据条件分页查询词类
	 * 
	 * @param wordclass参数词类
	 * @param start参数起始条数
	 * @param limit参数每页条数
	 * @return json字符串
	 */
	public static Object select(String wordclass, Boolean wordclassprecise,String wordclasstype,int start, int limit, String citycode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		JSONArray jsonArr1 = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
			return jsonObj;
		}
		User user = (User)sre;
		System.out.println(user==null);
		String userCity = UserCity.cityCodes.get(user.getUserID());
		//String idustry = user.getCustomer().split("->")[0];
		// 执行SQL语句，获取相应的数据源
		int count = UserOperResource.getWordclassCount(user,wordclass, wordclassprecise, wordclasstype,"基础", citycode);
		// 判断数据源不为null且含有数据
		if (count>0) {
				// 将条数放入jsonObj的total对象中
				jsonObj.put("total",count);
				jsonArr = UserOperResource.getWordclass(user,wordclass, wordclassprecise,wordclasstype,"基础", start, limit, citycode);
				// 判断数据源不为null且含有数据
				if (jsonArr != null && jsonArr.size()> 0) {
					for(Object obj : jsonArr){
						JSONObject jb = (JSONObject)obj;
						String word = jb.getString("wordclass");
						int count1 = UserOperResource.getWordCount("", false, true, "", word, "基础", userCity);
						// 判断数据源不为null且含有数据
						if (count1  > 0) {
							jb.put("isHaveItem", "有");
						}else{
							jb.put("isHaveItem", "无");
						}
						jsonArr1.add(jb);
					}
					// 将jsonArr数组放入jsonObj的root对象中
					jsonObj.put("root", jsonArr1);
				}
				
			} else {
				// 将0放入jsonObj的total对象中
				jsonObj.put("total", 0);
				// 清空jsonArr数组
				jsonArr.clear();
				// 将空数组放入jsonObj的root对象中
				jsonObj.put("root", jsonArr);
			}
		
		return jsonObj;
	}

	/**
	 * 更新词类的具体操作
	 * 
	 * @param id参数id
	 * @param oldvalue参数原有词类
	 * @param newvalue参数新的词类
	 * @return 更新返回的结果
	 */
	private static int _update(String id, String oldvalue, String newvalue) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义更新词类的SQL语句
		String sql = "update wordclass set wordclass=?, time = sysdate where wordclassid=? ";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定词类参数
		lstpara.add(newvalue);
		// 绑定id参数
		lstpara.add(id);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);

		// 生成操作日志记录
		// 将SQL语句放入集合中
		lstsql.add(MyUtil.LogSql());
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(MyUtil.LogParam(" ", " ", "更新词类", "上海", oldvalue + "==>"
				+ newvalue, "WORDCLASS"));

		/**
		 * 下列注释的操作替换成成了触发器 // 更新词类的SQL语句 sql =
		 * "update wordpat set wordpat=replace(wordpat,?,?) where wordpat like ?"
		 * ; // 定义绑定参数集合 lstpara = new ArrayList<String>(); // 绑定原有词类参数
		 * lstpara.add("!" + oldvalue + "|"); // 绑定新的词类参数 lstpara.add("!" +
		 * newvalue + "|"); // 绑定词类参数 lstpara.add("%!" + oldvalue + "|%"); //
		 * 将SQL语句放入集合中 lstsql.add(sql); // 将对应的绑定参数集合放入集合中
		 * lstlstpara.add(lstpara);
		 * 
		 * // 定义绑定参数集合 lstpara = new ArrayList<String>(); // 绑定原有词类参数
		 * lstpara.add("!" + oldvalue + ">"); // 绑定新的词类参数 lstpara.add("!" +
		 * newvalue + ">"); // 绑定词类参数 lstpara.add("%!" + oldvalue + ">%"); //
		 * 将SQL语句放入集合中 lstsql.add(sql); // 将对应的绑定参数集合放入集合中
		 * lstlstpara.add(lstpara);
		 */
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 更新词类
	 * 
	 * @param id参数id
	 * @param oldvalue参数原有词类值
	 * @param newvalue参数新的词类值
	 * @return 修改返回的json串
	 */
	public static Object update(String id, String oldvalue, String newvalue,String oldwordclasstype,String newwordclasstype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断是否已存在相同词类
//		if (Exists(newvalue)) {
//			// 将false放入jsonObj的success对象中
//			jsonObj.put("success", false);
//			// 将重复信息放入jsonObj的msg对象中
//			jsonObj.put("msg", "该词类基础词库或企业词库中已存在!");
//		} 
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		
		if(!user.isPower()||!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		
		if(UserOperResource.isExistWordclass(oldvalue, newvalue)){
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将重复信息放入jsonObj的msg对象中
			jsonObj.put("msg", "该词类词库中已存在!");
		}else {
			// 执行修改操作，返回事务处理的结果
			//int c = _update(id, oldvalue, newvalue);
			 sre = GetSession.getSessionByKey("accessUser");
			if(sre==null||"".equals(sre)){
				// 将0放入jsonObj的total对象中
				jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "登录超时，请注销后重新登录");
				return jsonObj;
			}
//			User user = (User)sre;
			
			int c = UserOperResource.updateWordcalss(user, id, oldvalue, newvalue, oldwordclasstype, newwordclasstype, "基础");
			// 判断事务处理结果
			if (c > 0) {
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
	 * 判断词类是否存在
	 * 
	 * @param wordclass参数词类
	 * @return 是否存在
	 */
	public static Boolean Exists(String wordclass) {
		// 查询词类的SQL语句
		//String sql = "select * from wordclass where wordclass=? and container=? ";
		String sql = "select * from wordclass where wordclass= ? ";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定词类参数
		lstpara.add(wordclass);
		// 绑定类型参数
		//lstpara.add("基础");
		try {
			// 执行SQL语句，返回数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 有重复词类，返回true
				return true;
			} else {
				// 没有重复词类，返回false
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			return false;
		}
	}

	/**
	 * 词类添加
	 * 
	 * @param wordclasses参数处理
	 * @return 添加返回的json串
	 */
	public static Object insert(String wordclass,String wordclasstype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		
		if(!user.isPower()||!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		
		
		// 用换行符拆分用户输入的多条词类,去除空字符串和空格等空白字符
		List<String> lstWordclass = Arrays.asList(wordclass.split("\n"));
		List<String> listWord= new ArrayList<String>();
		String msg = "";
		for (int i = 0; i < lstWordclass.size(); i++) {
			// 判断是否已存在相同词类
//			if (Exists(lstWordclass.get(i))) {
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将重复信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "第" + (i + 1) + "条词类基础词库或企业词库中已存在!");
//				return jsonObj;
//			}msg += "第" + (i + 1)+"条["+lstWordclass.get(i)+"]<br>";
			if(UserOperResource.isExistWordclass("", lstWordclass.get(i))||listWord.contains(lstWordclass.get(i))){
				// 将false放入jsonObj的success对象中
				//jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				//jsonObj.put("msg", "第" + (i + 1) + "条词库中已存在!");
				//return jsonObj;
				if("".equals(msg)){
					msg = "以下词类因已存在而被忽略<br>";
				}
				msg += "第" + (i + 1)+"条["+lstWordclass.get(i)+"]<br>";
			}else {
				listWord.add(lstWordclass.get(i));
			}
		}
		// 执行添加操作，返回添加事务处理的结果
		//int c = _insert(lstWordclass);
		sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
		}
//		user = (User)sre;
		// 执行SQL语句，获取相应的数据源
		int c = UserOperResource.insertWordcalss(user, listWord, wordclasstype, "基础",user.getCustomer());
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存成功!<br>"+msg);
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!<br>"+msg);
		}
		return jsonObj;
	}

	/**
	 * 词类添加具体方法
	 * 
	 * @param lstWordclass词类的集合
	 * @return 添加返回的结果
	 */
	private static int _insert(List<String> lstWordclass) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 循环遍历词类集合
		for (int i = 0; i < lstWordclass.size(); i++) {
			// 插入词类的SQL语句
			sql = "insert into wordclass(wordclassid,wordclass,container) values(?,?,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 获取词类表的序列值
			String id = String.valueOf(SeqDAO.GetNextVal("seq_wordclass_id"));
			// 绑定id参数
			lstpara.add(id);
			// 绑定词类参数
			lstpara.add(lstWordclass.get(i));
			// 绑定类型参数
			lstpara.add("基础");
			// 将SQL语句放入集合中
			lstsql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstlstpara.add(lstpara);

			// 生成操作日志记录
			// 将SQL语句放入集合中
			lstsql.add(MyUtil.LogSql());
			// 将对应的绑定参数集合放入集合中
			lstlstpara.add(MyUtil.LogParam(" ", " ", "增加词类", "上海", lstWordclass
					.get(i), "WORDCLASS"));
		}
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 删除词类
	 * 
	 * @param wordclassid参数词类id
	 * @param wordclass参数词类
	 * @return 删除返回的json串
	 */
	public static Object delete(String wordclassid, String wordclass,String wordclasstype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		
		if(!user.isPower()||!"全行业".equals(customer)){//临时限制省级用户编辑
			jsonObj.put("success", false);
			jsonObj.put("msg", "无操作权限!");
			return jsonObj;
		}
		
//		// 定义多条SQL语句集合
//		List<String> lstsql = new ArrayList<String>();
//		// 对应多条SQL语句对应的绑定参数集合
//		List<List<?>> lstlstpara = new ArrayList<List<?>>();
//		// 删除词类的SQL语句
//		String sql = "delete from wordclass where wordclassid=? ";
//		// 定义绑定参数集合
//		List<String> lstpara = new ArrayList<String>();
//		// 绑定词类id参数
//		lstpara.add(wordclassid);
//		// 将删除词类sql存入集合中
//		lstsql.add(sql);
//		// 将删除词类参数集合存入集合中
//		lstlstpara.add(lstpara);
//
//		// 生成操作日志记录
//		// 将SQL语句放入集合中
//		lstsql.add(MyUtil.LogSql());
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(MyUtil.LogParam(" ", " ", "删除词类", "上海", wordclass,
//				"WORDCLASS"));
//		// 执行SQL语句，绑定事务处理，返回事务处理结果
//		int c = Database.executeNonQueryTransaction(lstsql, lstlstpara);
		 sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
		}
//		User user = (User)sre;
		int c = UserOperResource.deleteWordclass(user, wordclassid, wordclass, wordclasstype ,"基础");
		
		
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
}