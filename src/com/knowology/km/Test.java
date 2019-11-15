package com.knowology.km;

import java.util.ArrayList;
import java.util.List;


import oracle.jdbc.driver.OracleDriver;





public class Test extends OracleDriver{
	public static void main(String[] args){	
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		list.add("6");
		for(int i = 0 ; i<list.size();i++){
			if(list.get(i) !=null){
				list.remove(list.get(i));
			}
			System.out.print(list.size()+"___");
			System.out.print(i);
			System.out.println(list.get(i));
		}
	
		System.out.print(123);
	}
}