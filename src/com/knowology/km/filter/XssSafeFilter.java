package com.knowology.km.filter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * 防SQL、xss注入、设置跨站访问服务
 */
public class XssSafeFilter implements Filter {

	private static Logger logger = Logger.getLogger(XssSafeFilter.class);

	/**
	 * 移除"script" , "'"拦截
	 */
	public final static String[] INJ_STRARRAY = { "mid", "master", "truncate",
			"char", "insert", "select", "delete", "update", "declare",
			"iframe", "onreadystatechange", "alert", "atestu", "xss", "script",
			"|","&",";","%","'","\"","\\'","\\\"","<",">","(",")","+",",","\\",
			(char)10+"",(char)13+"" };

	public final static String excludeUrls = "/add.action,/update.action";

	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest xssRequest = (HttpServletRequest) request;

		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String pathInfo = xssRequest.getPathInfo() == null ? "" : xssRequest
				.getPathInfo();
		
		if(pathInfo.length()>0){
		if(pathInfo.indexOf(".")<0){
			String warning = "非法链接，禁止访问!";
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();

			out.println("<script type=\"text/javascript\">");
			out.println("alert('" + warning + "');");
			out.println("history.go(-1);");
			out.println("</script>");
			out.flush();
			out.close();
			return;
		}
	}
		
		String url = xssRequest.getServletPath() + pathInfo;
		

		/**
		 * 
		 * 排除部分URL不做过滤。 此处可以加载不需要过滤的url
		 */
		if (excludeUrls.contains(url)) {
			chain.doFilter(xssRequest, response);
			return;
		}

		/**
		 * 获取请求所有参数，校验防止SQL注入
		 */
		@SuppressWarnings("unchecked")
		Map<String, Object> parameters = new HashMap<String, Object>(
				xssRequest.getParameterMap());

		/**
		 * 防止XSS漏洞
		 */
		if (parameters != null && parameters.size() > 0) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				String paramVale = "";
				if(entry.getValue() instanceof String ){
					paramVale = (String)entry.getValue();
				}
				
				/**
				 * 校验是否存在SQL注入信息
				 */
				if (checkSQLInject(paramVale)) {
					String warning = "输入项中不能包含非法字符。";

					response.setContentType("text/html; charset=UTF-8");
					PrintWriter out = response.getWriter();

					out.println("<script type=\"text/javascript\">");
					out.println("alert('" + warning + "');");
					out.println("history.go(-1);");
					out.println("</script>");
					out.flush();
					out.close();
					return;
				}
			}
		}
		// 设置可跨域访问服务
//		httpResponse.addHeader("Access-Control-Allow-Origin",
//				"http://172.16.1.10:8080");
//		httpResponse.addHeader("Access-Control-Allow-Origin",
//				"http://www.domain.com:8080");
		
//		httpResponse.addHeader("Access-Control-Allow-Origin",
//		"http://192.168.226.85:8282");
//		httpResponse.addHeader("Access-Control-Allow-Origin",
//		"http://127.0.0.1:8080/KM/login/login.jsp");
		httpResponse.addHeader("Access-Control-Allow-Methods",
		"POST, GET");
		
		chain.doFilter(xssRequest, response);
	}

	private boolean checkSQLInject(String str) {
		String[] inj_stra = XssSafeFilter.INJ_STRARRAY;
		str = str.toLowerCase(); // sql不区分大小写

		for (int i = 0; i < inj_stra.length; i++) {
			if (str.indexOf(inj_stra[i]) >= 0) {
				logger.info("特殊字符，传入str=" + str + ",特殊字符：" + inj_stra[i]);
				return true;
			}
		}

		return false;
	}

	public void destroy() {
	}

}
