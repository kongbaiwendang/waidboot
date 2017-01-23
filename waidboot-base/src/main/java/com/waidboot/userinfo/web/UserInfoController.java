package com.waidboot.userinfo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waidboot.common.web.constant.RestConstants;
import com.waidboot.common.web.vo.RestCommonData;
import com.waidboot.userinfo.service.facade.UserInfoService;
import com.waidboot.vo.userinfo.UserinfoVo;

@RestController
public class UserInfoController {
	
	@Autowired
	private UserInfoService userInfoService;
	
	@RequestMapping("userinfo/save")
	public RestCommonData addUserInfo(){
		RestCommonData res = new RestCommonData();
		UserinfoVo userinfoVo = new UserinfoVo();
		
		userinfoVo.setUsername("张三");
		userinfoVo.setAge(20);
		userinfoVo.setSex(1);
		userInfoService.saveUserInfo(userinfoVo);
		res.setResCode(RestConstants.SUCCESS.code);
		res.setResDescribe(RestConstants.SUCCESS.describe);
		return res;
	}
}
