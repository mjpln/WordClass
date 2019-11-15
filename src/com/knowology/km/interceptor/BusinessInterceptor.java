package com.knowology.km.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.knowology.Bean.User;
import com.knowology.km.util.GetSession;
import com.opensymphony.xwork2.ActionInvocation;

public class BusinessInterceptor {
	public String intercept(ActionInvocation arg0) throws Exception {
		// 获取request域中信息
		HttpServletRequest req = ServletActionContext.getRequest();
		// 获得当前请求url
		String url = req.getServletPath();
		// 获得请求类型
		String type = req.getHeader("X-Requested-With");
		// 获得session
		Object sre = GetSession.getSessionByKey("accessUser");
		// 鉴权结果
		User user = (User) sre;
		PrintWriter printWriter = null;
		if(!user.isPower()){
					printWriter = ServletActionContext.getResponse()
							.getWriter();
					printWriter.print("noLimit");
					printWriter.flush();
					printWriter.close();
					return null;
		}
		return arg0.invoke();
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}
}
