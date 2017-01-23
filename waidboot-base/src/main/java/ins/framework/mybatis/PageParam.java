package ins.framework.mybatis;

import java.util.List;

import org.apache.ibatis.session.RowBounds;

import com.github.miemiedev.mybatis.paginator.domain.Order;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;

public class PageParam extends PageBounds {
	private static final long serialVersionUID = 1L;
    private int totalCount;
    
	public PageParam() {
		super();
	}

	public PageParam(RowBounds rowBounds) {
		super(rowBounds);

	}

	/**
	 * Query TOP N, default containsTotalCount = false
	 * 
	 * @param limit
	 */
	public PageParam(int limit) {
		super(limit);
	}

	public PageParam(int page, int limit) {
		super(page, limit);
	}

	public PageParam(int page, int limit, boolean containsTotalCount) {
		super(page, limit, containsTotalCount);
	}

	/**
	 * Just sorting, default containsTotalCount = false
	 * 
	 * @param orders
	 */
	public PageParam(List<Order> orders) {
		super(orders);
	}

	/**
	 * Just sorting, default containsTotalCount = false
	 * 
	 * @param order
	 */
	public PageParam(Order... order) {
		super(order);
	}

	public PageParam(int page, int limit, Order... order) {
		super(page, limit, order);
	}

	public PageParam(int page, int limit, List<Order> orders) {
		super(page, limit, orders);
	}

	public PageParam(int page, int limit, List<Order> orders, boolean containsTotalCount) {
		super(page, limit, orders, containsTotalCount);
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
