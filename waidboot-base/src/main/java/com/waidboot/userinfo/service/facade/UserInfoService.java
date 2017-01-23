package com.waidboot.userinfo.service.facade;

import com.waidboot.vo.userinfo.UserinfoVo;

public interface UserInfoService {
	
	public void saveUserInfo(UserinfoVo userinfoVo);
	public void delUserInfo(Long id);
	public void updateUserInfo();
	public UserinfoVo getUserinfo(Long id);
}
