package com.waidboot.po.userinfo;

import java.io.Serializable;

import lombok.Data;

/**
 *
 * 通过ins-framework-mybatis工具自动生成，请勿手工修改。表userinfo的PO对象<br/>
 * 对应表名：userinfo
 *
 */
@Data
public class Userinfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 对应字段：id */
	private Long id;
	/** 对应字段：username,备注：姓名 */
	private String username;
	/** 对应字段：sex,备注：性别 */
	private Integer sex;
	/** 对应字段：age,备注：年龄 */
	private Integer age;

}
