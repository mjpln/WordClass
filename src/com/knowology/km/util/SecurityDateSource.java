package com.knowology.km.util;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;
 
public class SecurityDateSource extends DruidDataSource{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public void setUsername(String username) {
        try {
            username = ConfigTools.decrypt(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setUsername(username);
    }
 
    @Override
    public void setPassword(String password) {
        try {
            password = ConfigTools.decrypt(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setPassword(password);
    }
 
    public static void main(String[] args) throws Exception{
        String password = "123456";
        String username = "root";
        System.out.println("加密后的password = [" + ConfigTools.encrypt(password) + "]");
        System.out.println("加密后的username = [" + ConfigTools.encrypt(username) + "]");
    }
}