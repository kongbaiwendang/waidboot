package ins.framework.mybatis;

import java.util.List;

/**
 * <p>
 * Mapper 继承该接口后，无需编写 mapper.xml 文件，即可获得CRUD功能
 * </p>
 * <p>
 * 这个 Mapper 支持 id 泛型
 * </p>
 */
public interface MybatisBaseDao<T, I> {

	/**
	 * <p>
	 * 插入一条记录
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	int insert(T entity);

	/**
	 * <p>
	 * 插入一条记录（选择字段， null 字段不插入）
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	int insertSelective(T entity);

	/**
	 * <p>
	 * 插入（批量）
	 * </p>
	 * 
	 * @param entityList
	 *            实体对象列表
	 * @return int
	 */
	// int insertBatch(List<T> entityList);

	/**
	 * <p>
	 * 根据 主键 删除
	 * </p>
	 * 
	 * @param id
	 *            主键ID
	 * @return int
	 */
	int deleteByPrimaryKey(I id);

	/**
	 * <p>
	 * 根据 columnMap 条件，删除记录
	 * </p>
	 * 
	 * @param columnMap
	 *            表字段 map 对象
	 * @return int
	 */
	// int deleteByMap(@Param("cm") Map<String, Object> columnMap);

	/**
	 * <p>
	 * 根据 entity 条件，删除记录
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	// int deleteSelective(@Param("ew") T entity);

	/**
	 * <p>
	 * 删除（根据ID 批量删除）
	 * </p>
	 * 
	 * @param idList
	 *            主键ID列表
	 * @return int
	 */
	int deleteBatchByPrimaryKeys(List<I> idList);

	/**
	 * <p>
	 * 根据 ID 修改
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	int updateByPrimaryKey(T entity);

	/**
	 * <p>
	 * 根据 ID 选择修改
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 */
	int updateSelectiveByPrimaryKey(T entity);

	/**
	 * <p>
	 * 根据 whereEntity 条件，更新记录
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return whereEntity 实体查询条件（可以为 null）
	 */
	// int update(@Param("et") T entity, @Param("ew") T whereEntity);

	/**
	 * <p>
	 * 根据 whereEntity 条件，选择更新记录
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return whereEntity（可以为 null） 实体查询条件
	 */
	// int updateSelective(@Param("et") T entity, @Param("ew") T whereEntity);

	/**
	 * <p>
	 * 根据ID 批量更新
	 * </p>
	 * 
	 * @param entityList
	 *            实体对象列表
	 * @return int
	 */
	// int updateBatchByPrimaryKey(List<T> entityList);

	/**
	 * <p>
	 * 根据 ID 查询
	 * </p>
	 * 
	 * @param id
	 *            主键ID
	 * @return T
	 */
	T selectByPrimaryKey(I id);

	/**
	 * <p>
	 * 查询（根据ID 批量查询）
	 * </p>
	 * 
	 * @param idList
	 *            主键ID列表
	 * @return List<T>
	 */
	List<T> selectBatchByPrimaryKeys(List<I> idList);

	/**
	 * <p>
	 * 查询（根据 columnMap 条件）
	 * </p>
	 * 
	 * @param columnMap
	 *            表字段 map 对象
	 * @return List<T>
	 */
	// List<T> selectByMap(@Param("cm") Map<String, Object> columnMap);

	/**
	 * <p>
	 * 根据 entity 条件，查询一条记录
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return T
	 */
	// T selectOne(T entity);
	/**
	 * <p>
	 * 根据 分页参数和entity对象，查询一页记录
	 * </p>
	 * 
	 * @param pageParam
	 *            分页参数
	 * @param entity
	 *            实体对象
	 * @return Page<T>
	 */
	Page<T> selectPage(PageParam pageParam, T entity);
	/**
	 * <p>
	 * 根据 entity 条件，查询总记录数
	 * </p>
	 * 
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	// int selectCount(T entity);

	/**
	 * <p>
	 * 根据 entity 条件，查询全部记录
	 * </p>
	 * 
	 * @param entityWrapper
	 *            实体对象封装操作类（可以为 null）
	 * @return List<T>
	 */
	// List<T> selectList(@Param("ew") EntityWrapper<T> entityWrapper);
	//
	// /**
	// * <p>
	// * 根据 entity 条件，查询全部记录（并翻页）
	// * </p>
	// *
	// * @param rowBounds
	// * 分页查询条件（可以为 RowBounds.DEFAULT）
	// * @param entityWrapper
	// * 实体对象封装操作类（可以为 null）
	// * @return List<T>
	// */
	// List<T> selectPage(RowBounds rowBounds, @Param("ew") EntityWrapper<T>
	// entityWrapper);

}
