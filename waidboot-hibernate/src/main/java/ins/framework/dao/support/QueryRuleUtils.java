package ins.framework.dao.support;

import ins.framework.common.QueryRule;
import ins.framework.utils.StringUtils;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class QueryRuleUtils {
	public static void createCriteriaWithQueryRule(Criteria criteria, QueryRule queryRule) {
		for (QueryRule.Rule rule : queryRule.getRuleList()) {
			switch (rule.getType()) {
			case QueryRule.BETWEEN:
				processBetween(criteria, rule);
				break;
			case QueryRule.EQ:
				processEqual(criteria, rule);
				break;
			case QueryRule.LIKE:
				processLike(criteria, rule);
				break;
			case QueryRule.NOTEQ:
				processNotEqual(criteria, rule);
				break;
			case QueryRule.GT:
				processGreaterThen(criteria, rule);
				break;
			case QueryRule.GE:
				processGreaterEqual(criteria, rule);
				break;
			case QueryRule.LT:
				processLessThen(criteria, rule);
				break;
			case QueryRule.LE:
				processLessEqual(criteria, rule);
				break;
			case QueryRule.SQL:
				processSQL(criteria, rule);
				break;
			case QueryRule.IN:
				processIN(criteria, rule);
				break;
			case QueryRule.ISNULL:
				processIsNull(criteria, rule);
				break;
			case QueryRule.ISNOTNULL:
				processIsNotNull(criteria, rule);
				break;
			case QueryRule.ISEMPTY:
				processIsEmpty(criteria, rule);
				break;
			case QueryRule.ISNOTEMPTY:
				processIsNotEmpty(criteria, rule);
				break;
			case QueryRule.ASC_ORDER: // ASC顺序
				// 在getOrderFromQueryRule中处理，所以在此不处理
				break;
			case QueryRule.DESC_ORDER:// DESC顺序
				// 在getOrderFromQueryRule中处理，所以在此不处理
				break;
			default:
				throw new IllegalArgumentException("type " + rule.getType() + " not supported.");
			}
		}
		for (QueryRule subQueryRule : queryRule.getQueryRuleList()) {
			Criteria subCriteria = criteria.createCriteria(subQueryRule.getPropertyName());
			createCriteriaWithQueryRule(subCriteria, subQueryRule);
		}
	}

	protected static void processLike(Criteria criteria, QueryRule.Rule rule) {
		// 没有规则则忽略
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		Object obj = rule.getValues()[0];
		// values[0]不为null
		if (obj != null) {
			String value = obj.toString();
			if (StringUtils.isNotEmpty(value)) {
				value = value.replace('*', '%'); // 通配符标准化
				// value = value.replace("%%", "%"); // 去掉多余通配符
				obj = value;
			}
		}
		criteria.add(Restrictions.like(rule.getPropertyName(), obj));
	}

	protected static void processBetween(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues()) || rule.getValues().length < 2) {
			return;
		}
		criteria.add(Restrictions.between(rule.getPropertyName(), rule.getValues()[0], rule.getValues()[1]));
	}

	protected static void processEqual(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		criteria.add(Restrictions.eq(rule.getPropertyName(), rule.getValues()[0]));
	}

	protected static void processNotEqual(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		criteria.add(Restrictions.ne(rule.getPropertyName(), rule.getValues()[0]));
	}

	protected static void processGreaterThen(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		criteria.add(Restrictions.gt(rule.getPropertyName(), rule.getValues()[0]));
	}

	protected static void processGreaterEqual(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		criteria.add(Restrictions.ge(rule.getPropertyName(), rule.getValues()[0]));
	}

	protected static void processLessThen(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		criteria.add(Restrictions.lt(rule.getPropertyName(), rule.getValues()[0]));
	}

	protected static void processLessEqual(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		criteria.add(Restrictions.le(rule.getPropertyName(), rule.getValues()[0]));
	}

	protected static void processSQL(Criteria criteria, QueryRule.Rule rule) {
		criteria.add(Restrictions.sqlRestriction(rule.getPropertyName()));
	}

	protected static void processIsNull(Criteria criteria, QueryRule.Rule rule) {
		criteria.add(Restrictions.isNull(rule.getPropertyName()));
	}

	protected static void processIsNotNull(Criteria criteria, QueryRule.Rule rule) {
		criteria.add(Restrictions.isNotNull(rule.getPropertyName()));
	}

	protected static void processIsNotEmpty(Criteria criteria, QueryRule.Rule rule) {
		criteria.add(Restrictions.isNotEmpty(rule.getPropertyName()));
	}

	protected static void processIsEmpty(Criteria criteria, QueryRule.Rule rule) {
		criteria.add(Restrictions.isEmpty(rule.getPropertyName()));
	}

	@SuppressWarnings("unchecked")
	protected static void processIN(Criteria criteria, QueryRule.Rule rule) {
		if (ArrayUtils.isEmpty(rule.getValues())) {
			return;
		}
		if (rule.getValues().length == 1 && rule.getValues()[0] != null && rule.getValues()[0] instanceof List) {
			List<Object> list = (List<Object>) rule.getValues()[0];
			if (list != null && !list.isEmpty()) {// List中有值时才加入
				criteria.add(Restrictions.in(rule.getPropertyName(), list));
			}
		} else {
			criteria.add(Restrictions.in(rule.getPropertyName(), rule.getValues()));
		}
	}
}
