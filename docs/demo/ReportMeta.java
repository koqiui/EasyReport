package com.swb.bean.mercht.entity;

import java.io.Serializable;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.swb.common.annotation.Column;
import com.swb.common.annotation.Id;
import com.swb.common.json.JsonDateTimeSerializer;
import com.swb.common.json.JsonNullDeserializer;

/**
 * 报表元数据
 * 
 * @author koqiui
 * @date 2019年11月9日 下午8:14:19 <br/>
 * 
 *       TODO 启用@Table以数据库实体类实现
 */
// @Table(name = "report_meta", module = Module.Mercht, uniqueKeys = { @UniqueKey(fieldNames = { "code" }) }, desc = "报表元数据")
public class ReportMeta implements Serializable {
	private static final long serialVersionUID = 1L;
	//
	@Id(type = Types.INTEGER)
	private Integer id;

	@Column(nullable = false, type = Types.VARCHAR, length = 128, desc = "用户自定义代码（全局唯一）")
	private String code;

	@Column(nullable = false, type = Types.VARCHAR, length = 60, desc = "报表名称")
	private String name;

	@Column(nullable = false, type = Types.BOOLEAN, defaultValue = "FALSE", desc = "是否适合运营商")
	private Boolean forOperator;

	@Column(nullable = false, type = Types.BOOLEAN, defaultValue = "FALSE", desc = "是否适合供应商")
	private Boolean forVendor;

	@Column(nullable = false, type = Types.BOOLEAN, defaultValue = "FALSE", desc = "是否适合零售商")
	private Boolean forRetailer;

	@Column(nullable = false, type = Types.BOOLEAN, defaultValue = "FALSE", desc = "是否适合服务商")
	private Boolean forServicer;

	@Column(nullable = false, type = Types.BOOLEAN, defaultValue = "FALSE", desc = "是否禁用")
	private Boolean disabled;

	@Column(name = "`desc`", type = Types.VARCHAR, length = 120, desc = "展示数据内容说明")
	private String desc;

	@Column(nullable = false, type = Types.INTEGER, desc = "加入顺序或手工调整")
	private Integer seqNo;

	@Column(nullable = false, type = Types.TIMESTAMP, defaultValue = "CURRENT_TIMESTAMP", updatable = false, desc = "创建时间")
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonNullDeserializer.class)
	private Date createTime;

	@Column(nullable = false, type = Types.TIMESTAMP, defaultValue = "CURRENT_TIMESTAMP", desc = "时间戳")
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonNullDeserializer.class)
	private Date ts;

	// ------------------ 以下为非数据表列字段 ---------------------

	private String uuid;
	/** meta column */
	private List<Map<String, Object>> metaColumns;
	/** { param meta } list */
	private List<Map<String, Object>> queryParams;
	/** { name, text, width ...UI参考信息 } list */
	private List<Map<String, Object>> metaColList;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getForOperator() {
		return forOperator;
	}

	public void setForOperator(Boolean forOperator) {
		this.forOperator = forOperator;
	}

	public Boolean getForVendor() {
		return forVendor;
	}

	public void setForVendor(Boolean forVendor) {
		this.forVendor = forVendor;
	}

	public Boolean getForRetailer() {
		return forRetailer;
	}

	public void setForRetailer(Boolean forRetailer) {
		this.forRetailer = forRetailer;
	}

	public Boolean getForServicer() {
		return forServicer;
	}

	public void setForServicer(Boolean forServicer) {
		this.forServicer = forServicer;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(Integer seqNo) {
		this.seqNo = seqNo;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public List<Map<String, Object>> getMetaColumns() {
		return metaColumns;
	}

	public void setMetaColumns(List<Map<String, Object>> metaColumns) {
		this.metaColumns = metaColumns;
	}

	public List<Map<String, Object>> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(List<Map<String, Object>> queryParams) {
		this.queryParams = queryParams;
	}

	public List<Map<String, Object>> getMetaColList() {
		return metaColList;
	}

	public void setMetaColList(List<Map<String, Object>> metaColList) {
		this.metaColList = metaColList;
	}
}
