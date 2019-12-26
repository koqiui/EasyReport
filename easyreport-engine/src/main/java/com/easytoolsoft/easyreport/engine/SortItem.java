package com.easytoolsoft.easyreport.engine;

/**
 * 排序项目
 * 
 * @author koqiui
 * @date 2019年12月26日 下午2:55:47
 *
 */
public class SortItem {
	public String name;
	public String order;

	public SortItem() {
		//
	}

	public SortItem(String name, String order) {
		this.name = name;
		this.order = order;
	}

	public String toString() {
		return this.order == null ? this.name : this.name + " " + this.order;
	}

	public String toString(String tblAlias) {
		String fullName = this.name.indexOf(".") == -1 ? tblAlias + "." + this.name : this.name;
		return this.order == null ? fullName : fullName + " " + this.order;
	}
}
