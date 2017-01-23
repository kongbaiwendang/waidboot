package ins.framework.mybatis;

import java.util.Collection;

import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.github.miemiedev.mybatis.paginator.domain.Paginator;
/**
 * 一页数据
 * @author zhouxianli
 *
 * @param <PO对象>
 */
public class Page<E> extends PageList<E> {
	private static final long serialVersionUID = 1L;

	public Page() {
	}

	public Page(Collection<? extends E> c) {
		super(c);
	}

	public Page(Collection<? extends E> c, Paginator p) {
		super(c, p);
	}

	public Page(Paginator p) {
		super(p);
	}

	public int getPageSize() {
		Paginator paginator = super.getPaginator();
		if (paginator != null) {
			return paginator.getLimit();
		}
		return 0;
	}

	public int getPageNo() {
		Paginator paginator = super.getPaginator();
		if (paginator != null) {
			return paginator.getPage();
		}
		return 0;
	}

	public int getTotalCount() {
		Paginator paginator = super.getPaginator();
		if (paginator != null) {
			return paginator.getTotalCount();
		}
		return 0;
	}

}
