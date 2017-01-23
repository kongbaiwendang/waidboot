package com.waidboot.vo.addressinfo;

import java.io.Serializable;

import lombok.Data;

/**
 *
 * 通过ins-framework-mybatis工具自动生成。表addressinfo的VO对象<br/>
 * 对应表名：addressinfo
 *
 */
@Data
public class AddressinfoVo implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 对应字段：id */
	private Long id;
	/** 对应字段：userid */
	private Long userid;
	/** 对应字段：addressinfo */
	private String addressinfo;

}
