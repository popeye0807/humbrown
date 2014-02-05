package com.minyisoft.webapp.core.model.enumField;

/**
 * @author qingyong_ou 枚举对象接口
 */
public interface DescribableEnum<T> {
	/**
	 * 获取枚举值
	 * 
	 * @return
	 */
	public T getValue();

	/**
	 * 获取枚举值描述
	 * 
	 * @return
	 */
	public String getDescription();
}
