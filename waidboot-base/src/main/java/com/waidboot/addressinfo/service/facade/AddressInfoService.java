/**
 * 作者：王亚冬
 * 时间：2017-01-23
 * 说明：
 *
 */
package com.waidboot.addressinfo.service.facade;

import com.waidboot.vo.addressinfo.AddressinfoVo;

/**
 * 作者：王亚冬
 * 时间：2017-01-23
 * 说明：
 */
public interface AddressInfoService {
	
	public void saveAddressInfo(AddressinfoVo addressinfoVo);
	public AddressinfoVo queryAddressInfo(Long id);

}
