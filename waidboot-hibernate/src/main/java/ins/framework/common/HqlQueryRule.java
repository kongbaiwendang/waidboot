package ins.framework.common;

/**
 * HQL查询规则
 */
import ins.framework.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public final class HqlQueryRule {
	private StringBuilder hql = new StringBuilder();
	private List<Object> values = new ArrayList<Object>();
	private static final String AND = " and ";

	/**
	 * 得到拼好的HQL语句
	 * @return 拼好的HQL语句
	 */
	public String getHql() {
		String hqlString = hql.toString();
		if (hqlString != null && !(hqlString.equals(""))) {
			hqlString = hqlString.substring(4);
		}
		return hqlString;
	}

	/**
	 * 得到参数的值
	 * @return 参数的值
	 */
	public Object[] getValues() {
		return values.toArray();
	}

	public HqlQueryRule addEqual(String name, Integer value) {
		hql.append(AND).append(name).append("=?");
		values.add(value);
		return this;
	}

	public HqlQueryRule addEqual(String name, Long value) {
		hql.append(AND).append(name).append("=?");
		values.add(value);
		return this;
	}

	public HqlQueryRule addEqual(String name, String value) {
		hql.append(AND).append(name).append("=?");
		values.add(value);
		return this;
	}

	public HqlQueryRule addEqual(String name, Double value) {
		hql.append(AND).append(name).append("=?");
		values.add(value);
		return this;
	}

	public HqlQueryRule addEqual(String name, Float value) {
		hql.append(AND).append(name).append("=?");
		values.add(value);
		return this;
	}

	public HqlQueryRule addEqual(String name, Date value, Integer dateRange) {// NOPMD
		if (dateRange == null) {
			dateRange = DateTime.YEAR_TO_SECOND;
		}
		if (name != null && value != null) {
			if (dateRange == DateTime.YEAR_TO_SECOND) {
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" = ? ");
			} else if (dateRange == DateTime.YEAR_TO_DAY) {
				value.setHours(0);
				value.setMinutes(0);
				value.setSeconds(0);
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" = ? ");
			}
		}
		return this;
	}

	public HqlQueryRule addBetween(String name, Long begin, Long end) {
		hql.append(" And (").append(name).append(" between ? And ?)");
		values.add(begin);
		values.add(end);
		return this;
	}

	public HqlQueryRule addBetween(String name, Integer begin, Integer end) {
		hql.append(" And (").append(name).append(" between ? And ?)");
		values.add(begin);
		values.add(end);
		return this;
	}

	public HqlQueryRule addBetween(String name, Double begin, Double end) {
		hql.append(" And (").append(name).append(" between ? And ?)");
		values.add(begin);
		values.add(end);
		return this;
	}

	public HqlQueryRule addBetween(String name, Float begin, Float end) {
		hql.append(" And (").append(name).append(" between ? And ?)");
		values.add(begin);
		values.add(end);
		return this;
	}

	public HqlQueryRule addGreaterEqual(String name, Integer value) {
		hql.append(AND).append(name).append(" >= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterEqual(String name, Long value) {
		hql.append(AND).append(name).append(" >= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterEqual(String name, Double value) {
		hql.append(AND).append(name).append(" >= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterEqual(String name, Float value) {
		hql.append(AND).append(name).append(" >= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterThen(String name, Integer value) {
		hql.append(AND).append(name).append(" > ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterThen(String name, Long value) {
		hql.append(AND).append(name).append(" > ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterThen(String name, Double value) {
		hql.append(AND).append(name).append(" > ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addGreaterThen(String name, Float value) {
		hql.append(AND).append(name).append(" > ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessEqual(String name, Integer value) {
		hql.append(AND).append(name).append(" <= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessEqual(String name, Long value) {
		hql.append(AND).append(name).append(" <= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessEqual(String name, Double value) {
		hql.append(AND).append(name).append(" <= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessEqual(String name, Float value) {
		hql.append(AND).append(name).append(" <= ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessThan(String name, Integer value) {
		hql.append(AND).append(name).append(" < ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessThan(String name, Long value) {
		hql.append(AND).append(name).append(" < ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessThan(String name, Double value) {
		hql.append(AND).append(name).append(" < ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addLessThan(String name, Float value) {
		hql.append(AND).append(name).append(" < ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addNotEqual(String name, String value) {
		hql.append(AND).append(name).append(" != ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addNotEqual(String name, Integer value) {
		hql.append(AND).append(name).append(" != ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addNotEqual(String name, Long value) {
		hql.append(AND).append(name).append(" != ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addNotEqual(String name, Double value) {
		hql.append(AND).append(name).append(" != ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addNotEqual(String name, Float value) {
		hql.append(AND).append(name).append(" != ? ");
		values.add(value);
		return this;
	}

	public HqlQueryRule addBetween(String name, Date begin, Date end, Integer dateRange) {// NOPMD
		if (dateRange != null) {
			dateRange = DateTime.YEAR_TO_SECOND;
		}
		if (name != null && begin != null && end != null) {
			if (dateRange == DateTime.YEAR_TO_SECOND) {
				this.values.add(begin);
				this.values.add(end);
				hql.append(" And ");
				hql.append(name);
				hql.append(" between ? And ?");
			} else if (dateRange == DateTime.YEAR_TO_DAY) {
				begin.setHours(0);
				begin.setMinutes(0);
				begin.setSeconds(0);
				end.setHours(24);
				end.setMinutes(0);
				end.setSeconds(0);
				this.values.add(begin);
				this.values.add(end);
				hql.append(" And ");
				hql.append(name);
				hql.append(" between ? And ? ");
			}
		}
		return this;
	}

	public HqlQueryRule addGreaterEqual(String name, Date value, Integer dateRange) {// NOPMD
		if (dateRange != null) {
			dateRange = DateTime.YEAR_TO_SECOND;
		}
		if (name != null && value != null) {
			if (dateRange == DateTime.YEAR_TO_SECOND) {
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" >=? ");
			} else if (dateRange == DateTime.YEAR_TO_DAY) {
				value.setHours(0);
				value.setMinutes(0);
				value.setSeconds(0);
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" >=? ");
			}
		}
		return this;
	}

	public HqlQueryRule addGreaterThan(String name, Date value, Integer dateRange) {// NOPMD
		if (dateRange != null) {
			dateRange = DateTime.YEAR_TO_SECOND;
		}
		if (name != null && value != null) {
			if (dateRange == DateTime.YEAR_TO_SECOND) {
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" >? ");
			} else if (dateRange == DateTime.YEAR_TO_DAY) {
				value.setHours(0);
				value.setMinutes(0);
				value.setSeconds(0);
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" >? ");
			}
		}
		return this;
	}

	public HqlQueryRule addLessEqual(String name, Date value, Integer dateRange) {// NOPMD
		if (dateRange != null) {
			dateRange = DateTime.YEAR_TO_SECOND;
		}
		if (name != null && value != null) {
			if (dateRange == DateTime.YEAR_TO_SECOND) {
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" <=? ");
			} else if (dateRange == DateTime.YEAR_TO_DAY) {
				value.setHours(0);
				value.setMinutes(0);
				value.setSeconds(0);
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" <=? ");
			}
		}
		return this;
	}

	public HqlQueryRule addLessThan(String name, Date value, Integer dateRange) {// NOPMD
		if (dateRange != null) {
			dateRange = DateTime.YEAR_TO_SECOND;
		}
		if (name != null && value != null) {
			if (dateRange == DateTime.YEAR_TO_SECOND) {
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" <? ");
			} else if (dateRange == DateTime.YEAR_TO_DAY) {
				value.setHours(0);
				value.setMinutes(0);
				value.setSeconds(0);
				this.values.add(value);
				hql.append(" And ");
				hql.append(name);
				hql.append(" <? ");
			}
		}
		return this;
	}

	public HqlQueryRule addLike(String name, String value) {// NOPMD
		if (value != null) {
			value = StringUtils.replace(value, "*", "%");
		}
		hql.append(AND).append(name).append(" like ?");
		values.add(value);
		return this;
	}

	public HqlQueryRule addIn(String name, String[] value) {
		StringBuffer inCondition = new StringBuffer("");
		for (int i = 0; i < value.length; i++) {
			if (i == value.length - 1) {
				inCondition.append('?');
			} else {
				inCondition.append("?,");
			}
		}
		hql.append(AND).append(name).append(" in (").append(inCondition).append(")");
		for (int i = 0; i < value.length; i++) {
			values.add(value[i]);
		}
		return this;
	}

	public HqlQueryRule addHql(String hql) {
		this.hql.append(hql);
		return this;
	}

	public HqlQueryRule addSql(String sql) {
		hql.append(sql);
		return this;
	}
	
	public HqlQueryRule addValue(Object value) {
		values.add(value);
		return this;
	}
}
