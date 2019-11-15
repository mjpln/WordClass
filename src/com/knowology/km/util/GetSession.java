package com.knowology.km.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;


public class GetSession {

	public static  Object getSessionByKey(String key) {
		HttpServletRequest request=ServletActionContext.getRequest();
		HttpSession session=request.getSession();
		Object obj = session.getAttribute(key);
		if(obj==null) {	 
			ServletContext Context = session.getServletContext();  
			ServletContext ContextKM = Context.getContext("/KM");
			if(ContextKM!=null) {
				HttpSession sessionKM = (HttpSession)ContextKM.getAttribute("session");
				if(sessionKM!=null) {
					obj = sessionKM.getAttribute("user");
					if(obj!=null) {
						JSONObject json = JSONObject.parseObject(sessionKM.getAttribute("user").toString());
						User user = JSONObject.toJavaObject(json, User.class);
						session.setAttribute("accessUser", user);
						obj = user;
					}				
				}
			}
		}
		 
		return obj;
	}
}
