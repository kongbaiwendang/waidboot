package com.waidboot.common.web.vo;

import lombok.Data;

/**
 * 作者：王亚冬
 * 时间：2017-01-22
 * 说明：@RestController返回的公共对象。
 */
@Data
public class RestCommonData {
	
	/**
	 * 作者：王亚冬
	 * 描述：返回响应代码
	 * 时间：2017-01-22
	 */
	private String resCode;
	/**
	 * 作者：王亚冬
	 * 描述：响应描述
	 * 时间：2017-01-22
	 */
	private String resDescribe;
	/**
	 * 作者：王亚冬
	 * 描述：响应数据，如果不存在，可以传空
	 * 时间：2017-01-22
	 */
	private Object dataObj;
	
}
