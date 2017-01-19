package ins.framework.dao.database.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 查询规则。提供简便的方式来生成查询规则明细用于后台查询。<br>
 * 注意:如果传入规则对应的值为空或null也将加入查询条件中.<br>
 * 示例代码如下：
 * 
 * <pre>
 * 	&lt;code&gt;
 * QueryRule queryRule = QueryRule.getInstance();
 * queryRule.addLike(&quot;username&quot;, user.getUsername());
 * queryRule.addLike(&quot;monicker&quot;, user.getMonicker());
 * queryRule.addBetween(&quot;id&quot;, lowerId, upperId);
 * queryRule.addDescOrder(&quot;id&quot;);
 * queryRule.addAscOrder(&quot;username&quot;);
 * list = userService.query(queryRule);
 * 
 * &lt;/code&gt;
 * 另外一种写法: 
 *   	
 * &lt;code&gt;
 * QueryRule queryRule = QueryRule.getInstance().addLike(&quot;username&quot;,
 * 		user.getUsername()).addLike(&quot;monicker&quot;, user.getMonicker()).addBetween(
 * 		&quot;id&quot;, lowerId, upperId).addDescOrder(&quot;id&quot;).addAscOrder(&quot;username&quot;);
 * list = userService.query(queryRule);
 * 
 * &lt;/code&gt;
 * </pre>
 * @author zhouxianli
 */
public final class QueryRule implements Serializable {
	private static final long serialVersionUID = 1L;
	/** ASC顺序 */
	public static final int ASC_ORDER = 101;
	/** ASC逆序 */
	public static final int DESC_ORDER = 102;
	/** LIKE */
	public static final int LIKE = 1;
	/** IN */
	public static final int IN = 2;
	/** BETWEEN */
	public static final int BETWEEN = 3;
	/** 等于 */
	public static final int EQ = 4;
	/** 不等于 */
	public static final int NOTEQ = 5;
	/** 大于 */
	public static final int GT = 6;
	/** 大于等于 */
	public static final int GE = 7;
	/** 小于 */
	public static final int LT = 8;
	/** 小于等于 */
	public static final int LE = 9;
	/** SQL支持 */
	public static final int SQL = 10;
	/** 等于NULL */
	public static final int ISNULL = 11;
	/** 不等于NULL */
	public static final int ISNOTNULL = 12;
	/** 空 */
	public static final int ISEMPTY = 13;
	/** 非空 */
	public static final int ISNOTEMPTY = 14;
	public static final int MAX_RESULTS = 101;
	public static final int FIRST_RESULTS = 102;
	private List<Rule> ruleList = new ArrayList<Rule>();
	private List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
	private String propertyName;
	/**
	 * 是否忽略没有值的规则(用于界面查询)
	 */
	private boolean ignoreNoValue = false;

	/**
	 * 默认构造方法，禁止直接外部实例化
	 * @see #getInstance()
	 */
	private QueryRule() {
	}

	/**
	 * 构造方法，禁止直接外部实例化
	 * @param propertyName
	 *            属性名
	 * @see #getInstance()
	 */
	private QueryRule(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * 得到QueryRule的一个实例
	 * @return QueryRule的一个实例
	 */
	public static QueryRule getInstance() {
		return new QueryRule();
	}

	/**
	 * 添加ASC顺序排列的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addAscOrder(&quot;username&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性名
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addAscOrder(String propertyName) {
		ruleList.add(new Rule(ASC_ORDER, propertyName));
		return this;
	}

	/**
	 * 添加ASC逆序排列的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addDescOrder(&quot;username&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性列表
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addDescOrder(String propertyName) {
		ruleList.add(new Rule(DESC_ORDER, propertyName));
		return this;
	}

	/**
	 * 添加IsNull类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addIsNull(&quot;username&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性列表
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addIsNull(String propertyName) {
		ruleList.add(new Rule(ISNULL, propertyName));
		return this;
	}

	/**
	 * 添加IsNotNull类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addIsNotNull(&quot;username&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性列表
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addIsNotNull(String propertyName) {
		ruleList.add(new Rule(ISNOTNULL, propertyName));
		return this;
	}

	/**
	 * 添加IsEmpty类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addIsEmpty(&quot;username&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性列表
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addIsEmpty(String propertyName) {
		ruleList.add(new Rule(ISEMPTY, propertyName));
		return this;
	}

	/**
	 * 添加IsNotEmpty类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addIsNotEmpty(&quot;username&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性列表
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addIsNotEmpty(String propertyName) {
		ruleList.add(new Rule(ISNOTEMPTY, propertyName));
		return this;
	}

	/**
	 * 添加LIKE(匹配)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * 	 QueryRule queryRule = QueryRule.getInstance();
	 * 	 queryRule.addLike(&quot;username&quot;, user.getUsername());
	 * 	 queryRule.addLike(&quot;monicker&quot;, user.getMonicker())
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addLike(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(LIKE, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加LIKE(匹配)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * 	 QueryRule queryRule = QueryRule.getInstance();
	 * 	 queryRule.addLikeIfExist(&quot;username&quot;, user.getUsername());
	 * 	 queryRule.addLikeIfExist(&quot;monicker&quot;, user.getMonicker())
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addLikeIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(LIKE, propertyName, value));
		return this;
	}

	/**
	 * 添加Equal（等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addEqual(&quot;username&quot;, user.getUsername());
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addEqual(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(EQ, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加Equal（等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addEqualIfExist(&quot;username&quot;, user.getUsername());
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addEqualIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(EQ, propertyName, value));
		return this;
	}
	/**
	 * 添加BETWEEN类型的查询规则.如果任一规则值为空或为null时将忽略此条规则<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addBetween(&quot;id&quot;, lowerId, upperId);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性名
	 * @param values
	 *            规则值(变参)
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addBetween(String propertyName, Object... values) {
		if (ignoreNoValue) {
			if (values == null || values.length != 2) {
				return this;
			}
			if (values[0] == null || StringUtils.isEmpty(values[0].toString())) {
				return this;
			}
			if (values[1] == null || StringUtils.isEmpty(values[1].toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(BETWEEN, propertyName, values));
		return this;
	}
	
	/**
	 * 如果存在值则添加BETWEEN类型的查询规则.如果任一规则值为空或为null时将忽略此条规则<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addBetweenIfExist(&quot;id&quot;, lowerId, upperId);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param values
	 *            规则值(变参)
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addBetweenIfExist(String propertyName, Object... values) {

		if (values == null || values.length != 2) {
			return this;
		}
		if (values[0] == null || StringUtils.isEmpty(values[0].toString())) {
			return this;
		}
		if (values[1] == null || StringUtils.isEmpty(values[1].toString())) {
			return this;
		}

		ruleList.add(new Rule(BETWEEN, propertyName, values));
		return this;
	}

	/**
	 * 添加IN类型的查询规则.如果规则值的List中不含有数据时将忽略此条规则<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * List&lt;Integer&gt; idList = new ArrayList&lt;Integer&gt;();
	 * idList.add(1);
	 * idList.add(3);
	 * idList.add(5);
	 * queryRule.addIn(&quot;id&quot;, idList);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param values
	 *            规则值的List
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addIn(String propertyName, List<Object> values) {
		if (ignoreNoValue) {
			if (values == null || values.isEmpty()) {
				return this;
			}
		}
		ruleList.add(new Rule(IN, propertyName, values));
		return this;
	}
	
	/**
	 * 如果存在值则添加IN类型的查询规则.如果规则值的List中不含有数据时将忽略此条规则<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * List&lt;Integer&gt; idList = new ArrayList&lt;Integer&gt;();
	 * idList.add(1);
	 * idList.add(3);
	 * idList.add(5);
	 * queryRule.addInIfExist(&quot;id&quot;, idList);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param values
	 *            规则值的List
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addInIfExist(String propertyName, List<Object> values) {

		if (values == null || values.isEmpty()) {
			return this;
		}

		ruleList.add(new Rule(IN, propertyName, values));
		return this;
	}

	/**
	 * 添加IN类型的查询规则，如果规则值不含有数据时将忽略此条规则<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addIn(&quot;id&quot;, 1, 3, 9);
	 * queryRule.addIn(&quot;id&quot;, 1, 3, 5, 7, 9);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param values
	 *            规则值,数量可变
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addIn(String propertyName, Object... values) {
		if (ignoreNoValue) {
			if (values == null || values.length == 0) {
				return this;
			}
		}
		ruleList.add(new Rule(IN, propertyName, values));
		return this;
	}
	
	/**
	 * 如果存在值则添加IN类型的查询规则，如果规则值不含有数据时将忽略此条规则<br>
	 * 例：
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addInIfExist(&quot;id&quot;, 1, 3, 9);
	 * queryRule.addInIfExist(&quot;id&quot;, 1, 3, 5, 7, 9);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param values
	 *            规则值,数量可变
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addInIfExist(String propertyName, Object... values) {

		if (values == null || values.length == 0) {
			return this;
		}

		ruleList.add(new Rule(IN, propertyName, values));
		return this;
	}

	/**
	 * 添加NotEqual（不等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addNotEqual(&quot;username&quot;, user.getUsername());
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addNotEqual(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(NOTEQ, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加NotEqual（不等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addNotEqualIfExist(&quot;username&quot;, user.getUsername());
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addNotEqualIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(NOTEQ, propertyName, value));
		return this;
	}

	/**
	 * 添加GreaterThan（大于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addGreaterThan(&quot;id&quot;, 3);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addGreaterThan(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(GT, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加GreaterThan（大于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addGreaterThanIfExist(&quot;id&quot;, 3);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addGreaterThanIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(GT, propertyName, value));
		return this;
	}

	/**
	 * 添加GreaterEqual（大于等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addGreaterEqual(&quot;id&quot;, 3);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addGreaterEqual(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(GE, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加GreaterEqual（大于等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addGreaterEqualIfExist(&quot;id&quot;, 3);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addGreaterEqualIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(GE, propertyName, value));
		return this;
	}

	/**
	 * 添加LessThan（小于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addLessThan(&quot;id&quot;, 10);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addLessThan(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(LT, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加LessThan（小于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addLessThanIfExist(&quot;id&quot;, 10);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */

	public QueryRule addLessThanIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(LT, propertyName, value));
		return this;
	}
	/**
	 * 添加LessEqual（小于等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addLessEqual(&quot;id&quot;, 10);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addLessEqual(String propertyName, Object value) {
		if (ignoreNoValue) {
			if (value == null || StringUtils.isEmpty(value.toString())) {
				return this;
			}
		}
		ruleList.add(new Rule(LE, propertyName, value));
		return this;
	}

	/**
	 * 如果存在值则添加LessEqual（小于等于)类型的查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addLessEqualIfExist(&quot;id&quot;, 10);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            规则值
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addLessEqualIfExist(String propertyName, Object value) {

		if (value == null || StringUtils.isEmpty(value.toString())) {
			return this;
		}

		ruleList.add(new Rule(LE, propertyName, value));
		return this;
	}

	/**
	 * 直接执行Sql限定查询条件.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * queryRule.addSql(&quot;username like '%hello'&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param sql
	 *            sql查选限定条件
	 * @return QueryRule 查询规则本身
	 */
	public QueryRule addSql(String sql) {
		ruleList.add(new Rule(SQL, sql));
		return this;
	}

	/**
	 * public QueryRule setFirstResult(Integer firstResult) { ruleList.add(new
	 * Rule(FIRST_RESULTS, "",firstResult)); return this; }
	 * public QueryRule setMaxResults(Integer maxResult) { ruleList.add(new
	 * Rule(MAX_RESULTS, "",maxResult)); return this; }
	 */
	/**
	 * 添加子查询规则.<br>
	 * 例：
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * QueryRule queryRule = QueryRule.getInstance();
	 * 
	 * QueryRule addressQueryRule = queryRule.addSubQueryRule(&quot;addresses&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * @param propertyName
	 *            绑定的属性名称
	 * @return QueryRule 子查询规则
	 */
	public QueryRule addSubQueryRule(String propertyName) {
		QueryRule queryRule = new QueryRule(propertyName);
		queryRuleList.add(queryRule);
		return queryRule;
	}

	/**
	 * 得到所有查询规则明细
	 * @return 所有查询规则明细
	 */
	public List<Rule> getRuleList() {
		return ruleList;
	}

	/**
	 * 得到所有查询规则
	 * @return 所有查询规则
	 */
	public List<QueryRule> getQueryRuleList() {
		return queryRuleList;
	}

	/**
	 * 得到属性名称
	 * @return 属性名称
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * 查询规则明细类
	 * @author zhouxianli
	 */
	public class Rule implements Serializable {
		private static final long serialVersionUID = 1L;
		private int type;
		private String propertyName;
		private Object[] values;

		private Rule(int type, String propertyName) {
			this.propertyName = propertyName;
			this.type = type;
		}

		private Rule(int type, String propertyName, Object... objects) {
			this.propertyName = propertyName;
			this.values = objects;
			this.type = type;
		}

		public Object[] getValues() {
			return values;
		}

		public int getType() {
			return type;
		}

		public String getPropertyName() {
			return propertyName;
		}
	}

	public boolean isIgnoreNoValue() {
		return ignoreNoValue;
	}

	public void setIgnoreNoValue(boolean ignoreNoValue) {
		this.ignoreNoValue = ignoreNoValue;
	}
}
