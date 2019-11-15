package com.knowology.km.filter;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserManager;
import com.knowology.km.util.GetSession;

/**
 * 登录验证
 */
public class LoginFilter implements Filter {

	private static Logger logger = Logger.getLogger(LoginFilter.class);

	public void destroy() {
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		
		//获取请求路径
		String path = req.getServletPath();
		if(("/wordclass/app.html".equals(path))||("/wordclass_sentence/app.html".equals(path))) {
			String userid = req.getParameter("userid");
			String ioa = req.getParameter("ioa");
			
			//如果请求路径为主页面且带参数userid和ioa
			if(userid!=null&&ioa!=null) {
				User user = UserManager.constructLoginUser(userid, ioa);
				if(user!=null) {
					//session中加入user
					session.setAttribute("accessUser", user);
					chain.doFilter(request, response);					
				}
				return;
			}
		}

		/*
		 * 请求路径非主页面，或者虽然为主页面但是没有带参数，判断KM登录状态
		 * 获取ServletContext，然后可通过context.getContext("/KM")获取KM的ServletContext，
		 * 通过KM的ServletContext获取session，实现session共享
		*/
		ServletContext context = session.getServletContext();  
		ServletContext contextKM = context.getContext("/KM");
		if(contextKM!=null) {
			HttpSession sessionKM = (HttpSession)contextKM.getAttribute("session");
			if(sessionKM!=null) {
				Object obj = sessionKM.getAttribute("accessUser");
				if(obj!=null) {
					JSONObject json = (JSONObject)JSONObject.toJSON(sessionKM.getAttribute("accessUser"));
					User user = JSONObject.toJavaObject(json, User.class);
					session.setAttribute("accessUser", user);
					chain.doFilter(request, response);
				}				
			}
		}			
	}

	public void init(FilterConfig arg0) throws ServletException {
		
	}
}
