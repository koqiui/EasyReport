$(function () {
    MetaDataDesigner.init();
});

var MetaDataDesigner = {
    init: function () {
        DesignerMVC.View.initControl();
        DesignerMVC.View.resizeDesignerElments();
        DesignerMVC.View.initSqlEditor();
        DesignerMVC.View.initHistorySqlEditor();
        DesignerMVC.View.initPreviewSqlEditor();
        DesignerMVC.View.bindEvent();
        DesignerMVC.View.bindValidate();
        DesignerMVC.View.initData();
    },
    listReports: function (category) {
        DesignerMVC.Controller.listReports(category.id);
    },
    addReport: function () {
        DesignerMVC.Controller.add();
    },
    showMetaColumnOption: function (index, name) {
        DesignerMVC.Controller.showMetaColumnOption(index, name);
    },
    deleteQueryParam: function (index) {
        DesignerMVC.Controller.deleteQueryParam(index);
    }
};

var DesignerCommon = {
    baseUrl: EasyReport.ctxPath + '/rest/report/designer/',
    baseHistoryUrl: EasyReport.ctxPath + '/rest/report/history/',
    baseDsUrl: EasyReport.ctxPath + '/rest/report/ds/',
    baseIconUrl: EasyReport.ctxPath + '/custom/easyui/themes/icons/',
    baseReportUrl: EasyReport.ctxPath + '/report/'
};

var DesignerMVC = {
    URLs: {
        add: {
            url: DesignerCommon.baseUrl + 'add',
            method: 'POST'
        },
        edit: {
            url: DesignerCommon.baseUrl + 'edit',
            method: 'POST'
        },
        list: {
            url: DesignerCommon.baseUrl + 'list',
            method: 'GET'
        },
        find: {
            url: DesignerCommon.baseUrl + 'find',
            method: 'GET'
        },
        remove: {
            url: DesignerCommon.baseUrl + 'remove',
            method: 'POST'
        },
        historyList: {
            url: DesignerCommon.baseHistoryUrl + 'list',
            method: 'GET'
        },
        parseSqlVarNames : {
        	url : DesignerCommon.baseUrl + 'parseSqlVarNames',
        	method : 'POST'
        },
        execSqlText: {
            url: DesignerCommon.baseUrl + 'execSqlText',
            method: 'POST'
        },
        previewSqlText: {
            url: DesignerCommon.baseUrl + 'previewSqlText',
            method: 'POST'
        },
        getMetaColumnScheme: {
            url: DesignerCommon.baseUrl + 'getMetaColumnScheme',
            method: 'GET'
        },
        DataSource: {
            listAll: {
                url: DesignerCommon.baseDsUrl + 'listAll',
                method: 'GET'
            }
        },
        Report: {
            url: DesignerCommon.baseReportUrl + 'uid/',
            method: 'GET'
        }
    },
    Model: {
        MetaColumnOptions: [{
            name: "hidden",
            text: "隐藏列",
            type: 1
        }, {
            name: "optional",
            text: "可选的",
            type: 1
        },/* {
            name: "displayInMail",
            text: "邮件显示",
            type: 1
        }, {
         name : "footings",
         text : "合计",
         type : 1
         }, {
         name : "extensions",
         text : "小计",
         type : 3
         },*/ {
            name: "expression",
            text: "表达式",
            type: 4
        }, {
            name: "comment",
            text: "备注",
            type: 2
        }],
        MetaColumnTypes: [{
            text: "布局列",
            value: 1
        }, {
            text: "维度列",
            value: 2
        }, {
            text: "统计列",
            value: 3
        }, {
            text: "计算列",
            value: 4
        }],
        MetaColumnSortTypes: [{
            text: "默认",
            value: 0
        }, {
            text: "数字优先升序",
            value: 1
        }, {
            text: "数字优先降序",
            value: 2
        }, {
            text: "字符优先升序",
            value: 3
        }, {
            text: "字符优先降序",
            value: 4
        }], 
        MetaColumnAligns: [{
            text: "默认",
            value: ""
        },{
            text: "|<---",
            value: "left"
        }, {
            text: "->||<-",
            value: "center"
        }, {
            text: "--->|",
            value: "right"
        }],
        DataSourceList: []
    },
    View: {
        SqlEditor: null,
        PreviewSqlEditor: null,
        HistorySqlEditor: null,
        initControl: function () {
            $('#report-datagrid').datagrid({
                method: 'get',
                pageSize: 50,
                fit: true,
                pagination: true,
                rownumbers: true,
                fitColumns: true,
                singleSelect: true,
                toolbar: [{
                    text: '详细信息',
                    iconCls: 'icon-info',
                    handler: function () {
                        DesignerMVC.Controller.showDetail();
                    }
                }, '-',{
                    text: '查看JSON',
                    iconCls: 'icon-info',
                    handler: function () {
                        DesignerMVC.Controller.showJson();
                    }
                },'-', {
                    text: '从JSON新增',
                    iconCls: 'icon-add',
                    handler: function(){
                    	DesignerMVC.Controller.addFromJson();
                    }
                }, '-', {
                    text: '增加',
                    iconCls: 'icon-add',
                    handler: DesignerMVC.Controller.add
                }, '-', {
                    text: '修改',
                    iconCls: 'icon-edit1',
                    handler: DesignerMVC.Controller.edit
                }, '-', {
                    text: '复制',
                    iconCls: 'icon-copy',
                    handler: DesignerMVC.Controller.copy
                }, '-', {
                    text: '预览',
                    iconCls: 'icon-preview',
                    handler: DesignerMVC.Controller.preview
                }, '-', {
                    text: '版本',
                    iconCls: 'icon-history',
                    handler: DesignerMVC.Controller.showHistorySql
                }, '-', {
                    text: '删除',
                    iconCls: 'icon-remove',
                    handler: DesignerMVC.Controller.remove
                }],
                loadFilter: function (result) {
                    if (!result.code) {
                        return result.data;
                    }
                    $.messager.alert('失败', result.msg, 'error');
                    return EasyUIUtils.getEmptyDatagridRows();
                },
                columns: [[{
                    field: 'id',
                    title: 'ID',
                    width: 50,
                    sortable: true,
                    formatter: function (value, row, index) {
                    	var data = {
                			id : value
                		};
                    	var tmpl = '<span>${id}</span>';
                    	//展示自定义代码标记
                    	var ucode = row.ucode || '';
                    	if($.trim(ucode) != ''){
                    		data.imgSrc = DesignerCommon.baseIconUrl + 'flag.png?ts=2';
                    		data.title = '自定义代码：' + ucode;
                    		data.msg = data.title;
                    		//
                    		tmpl += '<img style="float:right;margin-top:2px;cursor:default;" src="${imgSrc}" title="${title}" onclick="$.messager.alert(\'提示\', \'${msg}\', \'info\')"/>';
                    	}
                    	//
                    	return juicer(tmpl, data);
                    }
                }, {
                    field: 'name',
                    title: '名称',
                    width: 150,
                    sortable: true
                }, {
                    field: 'dsName',
                    title: '数据源',
                    width: 100,
                    sortable: true
                }, {
                    field: 'status',
                    title: '状态',
                    width: 50,
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value == 1 ? "启用" : "禁用";
                    }
                }, {
                    field: 'createUser',
                    title: '创建者',
                    width: 50,
                    sortable: true
                }, {
                    field: 'comment',
                    title: '说明',
                    width: 100,
                    sortable: true
                }, {
                    field: 'gmtCreated',
                    title: '创建时间',
                    width: 90,
                    sortable: true
                }, {
                    field: 'gmtModified',
                    title: '修改时间',
                    width: 90,
                    sortable: true
                }, {
                    field: 'options',
                    title: '操作',
                    width: 110,
                    formatter: function (value, row, index) {
                        var icons = [{
                            "name": "info",
                            "title": "详细信息"
                        }, {
                            "name": "edit",
                            "title": "编辑"
                        }, {
                            "name": "copy",
                            "title": "复制"
                        }, {
                            "name": "preview",
                            "title": "预览"
                        }, {
                            "name": "history",
                            "title": "版本"
                        }, {
                            "name": "remove",
                            "title": "删除"
                        }];
                        var buttons = [];
                        for (var i = 0; i < icons.length; i++) {
                        	var padding = i==0 ? '' : ' style="padding-left:4px;" ';
                            var tmpl = '<a href="#" title ="${title}"' + padding +
                                'onclick="DesignerMVC.Controller.doOption(\'${index}\',\'${name}\')">' +
                                '<img src="${imgSrc}" alt="${title}" /></a>';
                            var data = {
                                title: icons[i].title,
                                name: icons[i].name,
                                index: index,
                                imgSrc: DesignerCommon.baseIconUrl + icons[i].name + ".png"
                            };
                            buttons.push(juicer(tmpl, data));
                        }
                        return buttons.join(' ');
                    }
                }]],
                onDblClickRow: function (rowIndex, rowData) {
                    return DesignerMVC.Controller.preview();
                },
                onRowContextMenu: function (e, index, row) {
                    e.preventDefault();
                    $('#report-datagrid-ctx-menu').menu('show', {
                        left: e.pageX,
                        top: e.pageY
                    });
                }
            });

            $('#report-datagrid-ctx-menu').menu({
                onClick: function (item) {
                    if (item.name == "preview") {
                        return DesignerMVC.Controller.preview();
                    }
                    if (item.name == "window") {
                        return DesignerMVC.Controller.previewInNewWindow();
                    }
                    if (item.name == "add") {
                        return DesignerMVC.Controller.add();
                    }
                    if (item.name == "edit") {
                        return DesignerMVC.Controller.edit();
                    }
                    if (item.name == "remove") {
                        return DesignerMVC.Controller.remove();
                    }
                    if (item.name == "copy") {
                        return DesignerMVC.Controller.copy();
                    }
                    if (item.name == "info") {
                        return DesignerMVC.Controller.showDetail();
                    }
                    if (item.name == "history") {
                        return DesignerMVC.Controller.showHistorySql();
                    }
                    if (item.name == "refresh") {
                        return DesignerMVC.Controller.reload();
                    }
                }
            });

            $('#report-meta-column-grid').datagrid({
                method: 'post',
                fit: true,
                fitColumns: true,
                singleSelect: true,
                rownumbers: true,
                toolbar: [{
                	text: '设为日期、时间、数值等的默认格式',
                    iconCls: 'icon-format2',
                    handler: function () {
                    	DesignerMVC.Controller.setMetaColumnFormat();
                    }
                }, '-',{
                    iconCls: 'icon-reload',
                    handler: function () {
                    	DesignerMVC.Controller.refreshMetaColumnList();
                    }
                }, '-',{
                    iconCls: 'icon-up',
                    handler: function () {
                    	DesignerMVC.Controller.refreshMetaColumnList();
                        EasyUIUtils.move("#report-meta-column-grid", 'up');
                    }
                }, '-', {
                    iconCls: 'icon-down',
                    handler: function () {
                    	DesignerMVC.Controller.refreshMetaColumnList();
                        EasyUIUtils.move("#report-meta-column-grid", 'down');
                    }
                }, '-', {
                    iconCls: 'icon-add',
                    handler: function () {
                        var rows = $("#report-meta-column-grid").datagrid('getRows') || [];
                        var seqNo = 1;
                        if (rows.length > 0) {
                            for (var i = 0, c=rows.length; i < c; i++) {
                                var type = $("#type" + i).val();
                                if (type == 4) seqNo++;
                            }
                        }
                        $.getJSON(DesignerMVC.URLs.getMetaColumnScheme.url, function (result) {
                            if (!result.code) {
                                var row = result.data;
                                row.name = row.name + '_' + seqNo;
                                row.text = row.name;
                                row.type = 4;
                                row.align = '';
                                row.format = '';
                                row.percent = false;
                                row.sortType = 0;
                                //
                                $('#report-meta-column-grid').datagrid('appendRow', row);
                                //
                                DesignerMVC.Controller.refreshMetaColumnList();
                            }
                        });
                    }
                }, '-', {
                    iconCls: 'icon-cancel',
                    handler: function () {
                    	DesignerMVC.Controller.refreshMetaColumnList();
                    	//
                        var row = $("#report-meta-column-grid").datagrid('getSelected');
                        if (row) {
                            var index = $("#report-meta-column-grid").datagrid('getRowIndex', row);
                            $("#report-meta-column-grid").datagrid('deleteRow', index);
                            var rows = $("#report-meta-column-grid").datagrid('getRows');
                            $("#report-meta-column-grid").datagrid('loadData', rows);
                            if(rows.length > index){
                            	$("#report-meta-column-grid").datagrid('selectRow', index);
                            }
                        }
                    }
                }],
                loadFilter: function (data) {
                	//可能以多种形式调用
                	var rows = null;
                	if($.isArray(data)){
                		rows = data;
                	}
                	else {
                		var rows = data.rows || [];
                	}
                	//
                	for(var i=0,c=rows.length; i<c; i++){
                		var row = rows[i];
                		if(!row.sqlType){//兼容旧的dataType
                			row.sqlType = row.dataType;
                			delete row["dataType"]
                		}
                		if(row.widthInChars == null){//兼容旧的无widthInChars
                			row.widthInChars = row.width || 10;
                			row.width = row.widthInChars * 10;
                		}
                	}
                    return data;
                },
                columns: [[{
                    field: 'name',
                    title: '列名',
                    width: 120,
                    formatter: function (value, row, index) {
                        var id = "name" + index;
                        var tmpl = '<input style="width:98%;" type="text" id="${id}" name="name" value="${value}" />';
                        return juicer(tmpl, {
                            id: id,
                            value: row.name
                        });
                    }
                }, {
                    field: 'text',
                    title: '标题',
                    width: 100,
                    formatter: function (value, row, index) {
                        var id = "text" + index;
                        var tmpl = '<input style="width:98%;" type="text" id="${id}" name="text" value="${value}" />';
                        return juicer(tmpl, {
                            id: id,
                            value: row.text
                        });
                    }
                }, {
                    field: 'type',
                    title: '列类型',
                    width: 70,
                    align : 'center',
                    formatter: function (value, row, index) {
                        var id = "type" + index;
                        var tmpl =
                            '<select id="${id}" name=\"type\">' +
                            '{@each list as item}' +
                            '<option value="${item.value}" {@if item.value == currValue} selected {@/if}>${item.text}</option>' +
                            '{@/each}' +
                            '</select>';
                        return juicer(tmpl, {
                            id: id,
                            currValue: value,
                            list: DesignerMVC.Model.MetaColumnTypes
                        });
                    }
                }, {
                    field: 'sqlType',
                    title: 'Sql类型',
                    width: 75,
                    formatter: function (value, row, index) {
                    	if(value.indexOf('BINARY') != -1){
                    		return '<span style="color:rgb(247,119,39);">'+value+'</span>'
                    	}
                    	else {
                    		return value;
                    	}
                    }
                }, {
                    field: 'widthInChars',
                    title: '字符宽度',
                    width: 60,
                    align : 'right',
                }, {
                    field: 'width',
                    title: '像素宽度',
                    width: 70,
                    align : 'right',
                    formatter: function (value, row, index) {
                    	var id = "width" + index;
                        
                        var tmpl = '<input style="width:95%;text-align:right;" type="text" id="${id}" name="width" value="${value}" />';
                        return juicer(tmpl, {
                            id: id,
                            value: row.width
                        });
                    }
                }, {
                    field: 'align',
                    title: '对齐方式',
                    width: 70,
                    align : 'center',
                    formatter: function (value, row, index) {
                		var id = "align" + index;
                        var tmpl =
                            '<select id="${id}" name=\"align\" style="width:95%;">' +
                            '{@each list as item}' +
                            '<option value="${item.value}" {@if item.value == currValue} selected {@/if}>${item.text}</option>' +
                            '{@/each}' +
                            '</select>';
                        return juicer(tmpl, {
                            id: id,
                            currValue: value || '',
                            list: DesignerMVC.Model.MetaColumnAligns
                        });
                    }
                }, {
                    field: 'format',
                    title: '格式化字符串',
                    width: 140,
                    formatter: function (value, row, index) {
                    	if(DesignerMVC.Util.isNumbericOrDateCol(row.sqlType)){
                    		var id = "format" + index;
                            if (!row.format) {
                                row.format = '';
                            }
                            var tmpl = '<input style="width:95%;text-align:left;" type="text" id="${id}" name="format" value="${value}" />';
                            return juicer(tmpl, {
                                id: id,
                                value: row.format
                            });
                    	}
                        return '';
                    }
                }, {
                    field: 'percent',
                    title: '百分比',
                    width: 50,
                    align: 'center',
                    formatter: function (value, row, index) {
                    	if(DesignerMVC.Util.isDecimalCol(row.sqlType)){
                    		var id = "percent" + index;
                            if (row.percent == null) {
                                row.percent = false;
                            }
                            var tmpl = '<input type="checkbox" id="${id}" name="percent" ${checked} />';
                            return juicer(tmpl, {
                                id: id,
                                checked: row.percent ? 'checked="checked"' : ''
                            });
                    	}
                        return '';
                    }
                }, {
                	field: 'clrLvl',
                	title: '色阶样式',
                    width: 70,
                    align : 'center',
                    formatter: function (value, row, index) {
                    	if(DesignerMVC.Util.isNumbericCol(row.sqlType) && (row.type == 3 || row.type == 4)){
                    		var clrLvlEnabled = row.clrLvlEnabled == true;
                            var tmpl = '<input type="button" style="cursor: pointer;border:1px solid ${borderColor};color:${fontColor};height:20px;border-radius:2px;" value="..设置.." onclick="DesignerMVC.Controller.showMetaColumnClrLvl(${index})" />';
                            return juicer(tmpl, {
                                index : index,
                                borderColor : clrLvlEnabled ? 'darkgreen' : 'gray',
                                fontColor : clrLvlEnabled ? 'darkgreen' : 'black'
                            });
                    	}
                    	return '';
                    }
                }, {
                	field: 'linkFunc',
                	title: '链接函数',
                    width: 70,
                    align : 'center',
                    formatter: function (value, row, index) {
                		var linkFuncExpr = row.linkFuncExpr || '';
                        var tmpl = '<input type="button" style="cursor: pointer;border:1px solid ${borderColor};color:${fontColor};height:20px;border-radius:2px;" value="..设置.." onclick="DesignerMVC.Controller.showMetaColumnLinkFunc(${index})" />';
                        return juicer(tmpl, {
                            index : index,
                            borderColor : $.trim(linkFuncExpr) == '' ? 'gray' : 'darkgreen',
                            fontColor : $.trim(linkFuncExpr) == '' ? 'black' : 'darkgreen'
                        });
                    }
                }, {
                    field: 'sortType',
                    title: '排序类型',
                    width: 100,
                    formatter: function (value, row, index) {
                		var id = "sortType" + index;
                        var tmpl =
                            '<select id="${id}" name=\"sortType\" style="width:95%;">' +
                            '{@each list as item}' +
                            '<option value="${item.value}" {@if item.value == currValue} selected {@/if}>${item.text}</option>' +
                            '{@/each}' +
                            '</select>';
                        return juicer(tmpl, {
                            id: id,
                            currValue: value,
                            list: DesignerMVC.Model.MetaColumnSortTypes
                        });
                    }
                }, {
                    field: 'options',
                    title: '配置',
                    width: 220,
                    formatter: function (value, row, index) {
                        var subOptions = [];
                        // 4:计算列,3:统计列,2:维度列,1:布局列
                        if (row.type == 4) {
                            subOptions = $.grep(DesignerMVC.Model.MetaColumnOptions, function (option, i) {
                                return option.type == 1 || option.type == 2 || option.type == 4;
                            });
                        } else if (row.type == 3) {
                            subOptions = $.grep(DesignerMVC.Model.MetaColumnOptions, function (option, i) {
                                return option.type == 1 || option.type == 2;
                            });
                        } else {
                            subOptions = $.grep(DesignerMVC.Model.MetaColumnOptions, function (option, i) {
                                return option.type == 2 || option.type == 3;
                            });
                        }

                        var htmlOptions = [];
                        for (var i = 0; i < subOptions.length; i++) {
                            var name = subOptions[i].name;
                            var data = {
                                id: name + index,
                                name: name,
                                text: subOptions[i].text,
                                checked: row[name] ? " checked=\"checked\"" : "",
                                imgSrc: "",
                                onClick: ""
                            };
                            var tmpl = "";
                            if (name == "expression" || name == "comment") {
                                data.imgSrc = DesignerCommon.baseIconUrl + name + ".png";
                                data.onClick = "MetaDataDesigner.showMetaColumnOption('" + index + "','" + name + "')";
                                data.extStyle = name == "comment" ? 'float:right;' : 'float:left; margin-right:4px;';
                                tmpl = '<img style="cursor: pointer;${extStyle}" id="${id}" title="${text}" src="${imgSrc}" onclick="${onClick}" />';
                            } else {
                                tmpl = '<label style="float:left; margin-top:2px; margin-right:4px;"><input type="checkbox" id="${id}" name="${name}" ${checked} />${text}</label>'
                            }
                            htmlOptions.push(juicer(tmpl, data));
                        }
                        return htmlOptions.join(" ");
                    }
                }]]
            });

            $('#report-query-param-grid').datagrid({
                method: 'get',
                fit: true,
                singleSelect: true,
                rownumbers: true,
                tools: [{
                    iconCls: 'icon-up',
                    handler: function () {
                        EasyUIUtils.move('#report-query-param-grid', 'up');
                        $('#report-query-param-form').form('reset');
                    	$('#report-query-param-curIndex').val('');
                    }
                }, '-', {
                    iconCls: 'icon-down',
                    handler: function () {
                        EasyUIUtils.move('#report-query-param-grid', 'down');
                        $('#report-query-param-form').form('reset');
                    	$('#report-query-param-curIndex').val('');
                    }
                }],
                columns: [[{
                    field: 'name',
                    title: '参数代码',
                    width: 100
                }, {
                    field: 'text',
                    title: '标签文字',
                    width: 100
                }, {
                    field: 'dataType',
                    title: '数据类型',
                    width: 100,
                    align: 'center'
                }, {
                    field: 'formElement',
                    title: '表单控件',
                    width: 100
                }, {
                    field: 'dataSource',
                    title: '来源类型',
                    width: 100,
                    align: 'center',
                    formatter: function (value, row, index) {
                        if (value == "sql") {
                            return "SQL语句";
                        }
                        if (value == "text") {
                            return "文本定义";
                        }
                        return "";
                    }
                }, {
                    field: 'defaultValue',
                    title: '默认初值',
                    width: 100,
                    formatter: function (value, row, index) {
                        if (row.dataType == "date") {
                           if(row.defaultExpr != null && $.trim(row.defaultExpr) != ''){
                        	   var hintMsg = '默认初值表达式：' + row.defaultExpr;
                        	   return value + ' <span style="color:darkgreen;font-size:16px;float:right;cursor:default;" title="' + hintMsg + '" onclick="$.messager.alert(\'提示\', \''+ hintMsg + '\', \'info\')">❀</span>';
                           }
                        }
                        return value;
                    }
                }, {
                    field: 'defaultText',
                    title: '默认文本',
                    width: 100
                }, {
                    field: 'cascName',
                    title: '级联参数',
                    width: 120
                }, {
                    field: 'width',
                    title: '数据长度',
                    width: 80,
                    align: 'right'
                }, {
                    field: 'required',
                    title: '是否必需',
                    width: 80,
                    align: 'center',
                    formatter: function (value, row, index) {
                        if (value == true) {
                            return "√";
                        }
                        return "";
                    }
                }, {
                    field: 'hidden',
                    title: '是否隐藏',
                    width: 80,
                    align: 'center',
                    formatter: function (value, row, index) {
                        if (value == true) {
                            return "√";
                        }
                        return "";
                    }
                }, {
                    field: 'autoComplete',
                    title: '自动提示',
                    width: 80,
                    align: 'center',
                    formatter: function (value, row, index) {
                        if (value == true) {
                            return "√";
                        }
                        return "";
                    }
                }, {
                    field: 'options',
                    title: '操作',
                    width: 50,
                    align: 'center',
                    formatter: function (value, row, index) {
                        var imgPath = DesignerCommon.baseIconUrl + 'remove.png';
                        var tmpl = '<a href="#" title ="移除" ' +
                            'onclick="MetaDataDesigner.deleteQueryParam(${index})"><img src="${imgPath}" ' +
                            'alt="移除"/"></a>';
                        return juicer(tmpl, {
                            index: index,
                            imgPath: imgPath
                        });
                    }
                }]],
                onDblClickRow: function (index, row) {
                	var someWasNull = false;
                	if(row.hidden == null){
                		someWasNull = true;
                		row.hidden = false;
                	}
                	if(row.content == null){
                		someWasNull = true;
                		row.content = '';
                	}
                	//
                	$("#report-query-param-curIndex").val(index);
                	//
                    $('#report-query-param-form').form('load', row);
                    $("#report-query-param-required").prop("checked", row.required);
                    $("#report-query-param-hidden").prop("checked", row.hidden);
                    $("#report-query-param-autoComplete").prop("checked", row.autoComplete);
                    //
                    var rows = $("#report-query-param-grid").datagrid('getRows');
                    EasyUIUtils.clearDatagrid('#report-query-param-grid');
                    $("#report-query-param-grid").datagrid('loadData', rows);
                    $("#report-query-param-grid").datagrid('selectRow', index);
                    //
                    $('.for-query-param-defaultExpr').css('display', row.dataType == 'date' ? '' : 'none');
                    if(row.dataType == 'date'){//不要影响date的defaultExpr
                    	$('.for-query-param-cascName').css('display', 'none');
                    }
                    else {
                    	if(row.formElement == 'select' || row.formElement == 'selectMul'){
                    		$('.for-query-param-cascName').css('display', '');
                    	}
                    	else {
                    		$('.for-query-param-cascName').css('display', 'none');
                    	}
                    }
                },
                rowStyler: function (index, row) {
                	var curIndex = $.trim($("#report-query-param-curIndex").val());
                	if(curIndex != '' ){
                		curIndex = parseInt(curIndex);
                		if(index == curIndex){
                			return 'color:#0000FF';
                		}
                	}
                	return 'color : inherit';
                }
            });

            $('#report-history-sql-grid').datagrid({
                method: 'get',
                fit: true,
                pagination: true,
                rownumbers: true,
                fitColumns: true,
                singleSelect: true,
                pageSize: 30,
                loadFilter: function (result) {
                    if (!result.code) {
                        return result.data;
                    }
                    $.messager.alert('失败', result.msg, 'error');
                    return EasyUIUtils.getEmptyDatagridRows();
                },
                columns: [[{
                    field: 'gmtCreated',
                    title: '日期',
                    width: 100
                }, {
                    field: 'author',
                    title: '作者',
                    width: 100
                }]],
                onClickRow: function (index, row) {
                    DesignerMVC.Controller.showHistorySqlDetail(row);
                },
                onSelect: function (index, row) {
                    DesignerMVC.Controller.showHistorySqlDetail(row);
                }
            });

            $('#report-history-sql-pgrid').propertygrid({
                scrollbarSize: 0,
                columns: [[
                    {field: 'name', title: '属性项', width: 80, sortable: true},
                    {field: 'value', title: '属性值', width: 300, resizable: false}
                ]]
            });

            $('#report-designer-dlg').dialog({
                closed: true,
                modal: false,
                width: window.screen.width - 350,
                height: window.screen.height - 350,
                maximizable: true,
                minimizable: true,
                maximized: true,
                iconCls: 'icon-designer',
                buttons: [{
                    text: '编辑器放大/缩小',
                    iconCls: 'icon-fullscreen',
                    handler: DesignerMVC.Util.fullscreenEdit
                }, {
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-designer-dlg").dialog('close');
                    }
                }, {
                    text: '保存',
                    iconCls: 'icon-save',
                    handler: DesignerMVC.Controller.save
                }],
                onResize: function (width, height) {
                    DesignerMVC.Util.resizeDesignerDlgElments();
                }
            });

            $('#report-history-sql-dlg').dialog({
                closed: true,
                modal: false,
                width: window.screen.width - 350,
                height: window.screen.height - 350,
                maximizable: true,
                iconCls: 'icon-history',
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-history-sql-dlg").dialog('close');
                    }
                }]
            });

            $('#report-detail-dlg').dialog({
                closed: true,
                modal: true,
                width: window.screen.width - 350,
                height: window.screen.height - 350,
                maximizable: true,
                iconCls: 'icon-info',
                buttons: [{
                    text: '上一条',
                    iconCls: 'icon-prev',
                    handler: function () {
                        EasyUIUtils.cursor('#report-datagrid',
                            '#current-row-index',
                            'prev', function (row) {
                                DesignerMVC.Controller.showDetail(row);
                            });
                    }
                }, {
                    text: '下一条',
                    iconCls: 'icon-next',
                    handler: function () {
                        EasyUIUtils.cursor('#report-datagrid',
                            '#current-row-index',
                            'next', function (row) {
                                DesignerMVC.Controller.showDetail(row);
                            });
                    }
                }, {
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-detail-dlg").dialog('close');
                    }
                }]
            });
            
            $('#report-json-dlg').dialog({
                closed: true,
                modal: true,
                width: window.screen.width - 350,
                height: window.screen.height - 350,
                maximizable: true,
                maximized: true,
                iconCls: 'icon-info',
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-json-dlg").dialog('close');
                    }
                }]
            });
            
            $('#report-json-copy-dlg').dialog({
                closed: true,
                modal: true,
                width: window.screen.width - 350,
                height: window.screen.height - 350,
                maximizable: true,
                maximized: true,
                iconCls: 'icon-info',
                buttons: [{
                    text: '确定',
                    iconCls: 'icon-ok',
                    handler: function () {
                    	var catId = null;
                    	var catName = '';
                    	var node = $('#category-tree').tree('getSelected');
                        if (node) {
	                        var category = node.attributes;
	                        //console.log(category);
	                        catId = category.id;
	                        catName = category.name;
                        }
                        else {
                        	$.messager.alert('警告', '没有所属报表分类 !', 'warning');
                    		return false;
                        }
                        //
                    	var jsonSrcText = $.trim($('#report-json-src-text').val());
                    	var json = null;
                    	try{
                    		json = JSON.parse(jsonSrcText);
                    	}
                    	catch(ex){}
                    	//
                    	if(json == null){
                    		$.messager.alert('警告', '无效json !', 'warning');
                    		return false;
                    	}
                    	//
                    	var dsId = $('#report-dsId').combobox('getValue');
                    	
                    	var row = {
                    		ucode : json['ucode'] || '',
                    		name : json['name'] || '',
                    		sqlText : json['sqlText'] || '',
                    		metaColumns : json['metaColumns'] || '[]',
                    		queryParams : json['queryParams'] || '[]',
                    		options : json['options'] || '{}',
                    		status : json['status'] || 0,
                    		sequence : json['sequence'] || 10,
                    		comment : json['comment'] || '',
                    		//
                    		dsId : dsId,
                    		categoryId : catId,
                    		categoryName : catName
                    	};
                    	$("#report-json-copy-dlg").dialog('close');
                        //console.log(row);
                    	{//走copy新增逻辑
                        	EasyUIUtils.clearDatagrid('#report-meta-column-grid');
                            EasyUIUtils.clearDatagrid('#report-query-param-grid');
                            $('#report-query-param-form').form('reset');
                            $('#report-query-param-curIndex').val('');
                            //
                            var options = DesignerMVC.Util.getOptions();
                            options.iconCls = 'icon-designer';
                            options.data = row;
                            options.title = '报表设计器--【复制+新增】 报表';
                            DesignerMVC.Util.editOrCopy(options, 'copy', true);
                            $('#modal-action').val("add");
                        }
                    }
                }, {
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-json-copy-dlg").dialog('close');
                    }
                }]
            });

            $('#report-preview-sql-dlg').dialog({
                closed: true,
                modal: true,
                maximizable: true,
                iconCls: 'icon-sql',
                width: window.screen.width - 350,
                height: window.screen.height - 350,
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-preview-sql-dlg").dialog('close');
                    }
                }]
            });

            $('#report-column-expression-dlg').dialog({
                closed: true,
                modal: true,
                iconCls: 'icon-formula',
                left: (window.screen.width - 500) / 2,
                top: (window.screen.height - 310) / 2,
                width: 500,
                height: 310,
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-column-expression-dlg").dialog('close');
                    }
                }, {
                    text: '应用',
                    iconCls: 'icon-save',
                    handler: function () {
                        DesignerMVC.Controller.saveMetaColumnOption('expression');
                    }
                }]
            });

            $('#report-column-comment-dlg').dialog({
                closed: true,
                modal: true,
                iconCls: 'icon-comment',
                left: (window.screen.width - 500) / 2,
                top: (window.screen.height - 310) / 2,
                width: 500,
                height: 310,
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-column-comment-dlg").dialog('close');
                    }
                }, {
                    text: '应用',
                    iconCls: 'icon-save',
                    handler: function () {
                        DesignerMVC.Controller.saveMetaColumnOption('comment');
                    }
                }]
            });
            //
            $('#report-column-clrLvl-dlg').dialog({
                closed: true,
                modal: true,
                iconCls: 'icon-comment',
                left: (window.screen.width - 400) / 2,
                top: (window.screen.height - 360) / 2,
                width: 400,
                height: 360,
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-column-clrLvl-dlg").dialog('close');
                    }
                }, {
                    text: '应用',
                    iconCls: 'icon-save',
                    handler: function () {
                        DesignerMVC.Controller.saveMetaColumnClrLvl();
                    }
                }]
            });
            $('#report-column-linkFunc-dlg').dialog({
                closed: true,
                modal: true,
                iconCls: 'icon-comment',
                left: (window.screen.width - 600) / 2,
                top: (window.screen.height - 300) / 2,
                width: 600,
                height: 300,
                buttons: [{
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#report-column-linkFunc-dlg").dialog('close');
                    }
                }, {
                    text: '应用',
                    iconCls: 'icon-save',
                    handler: function () {
                        DesignerMVC.Controller.saveMetaColumnLinkFunc();
                    }
                }]
            });
            //
            $('#report-query-param-dataType').combobox({
            	onSelect: function (rec) {
                    if (rec.value == "bool") {
                    	var optionList = [{
                    		value : 'checkbox',
                    		text : '复选框',
                    		selected : true
                    	}]; 
                    	$('#report-query-param-formElement').combobox('loadData', optionList);
                    }
                    else {
                    	var formElementVal = $('#report-query-param-formElement').combobox('getValue');
                    	if(formElementVal == 'checkbox'){
                    		var optionList = [{
                        		value : 'text',
                        		text : '文本'
                        	},{
                        		value : 'date',
                        		text : '日期'
                        	},{
                        		value : 'select',
                        		text : '下拉单选'
                        	},{
                        		value : 'selectMul',
                        		text : '下拉多选'
                        	}]; 
                    		
                        	$('#report-query-param-formElement').combobox('loadData', optionList);
                    	}
                    	
                    	if(rec.value == "date"){
                    		$('#report-query-param-formElement').combobox('setValue', 'date');
                    	}
                    	else {
                    		$('#report-query-param-formElement').combobox('setValue', 'text');
                    	}
                    	//
                        $('.for-query-param-defaultExpr').css('display', rec.value == 'date' ? '' : 'none');
                        if(rec.value == 'date'){//不要影响date的defaultExpr
                        	$('.for-query-param-cascName').css('display', 'none');
                        }
                    }
                }
            });
            $('#report-query-param-formElement').combobox({
            	onSelect: function (rec) {
                    var value = "text";
                    if (rec.value == "text" || rec.value == "date" || rec.value == "checkbox") {
                        value = 'none';
                        //
                        $('.for-query-param-cascName').css('display', 'none');
                    }
                    else {
                    	if($('#report-query-param-dataType').combobox('getValue') != 'date'){
                    		$('.for-query-param-cascName').css('display', '');
                    	}
                    }
                    $('#report-query-param-dataSource').combobox('setValue', value);
                }
            });
        },
        resizeDesignerElments: function () {
            DesignerMVC.Util.resizeDesignerDlgElments();
        },
        initSqlEditor: function () {
            var dom = document.getElementById("report-sqlText");
            DesignerMVC.View.SqlEditor = CodeMirror.fromTextArea(dom, {
                mode: 'text/x-mysql',
                theme: 'rubyblue',
                indentWithTabs: true,
                smartIndent: true,
                lineNumbers: true,
                styleActiveLine: true,
                matchBrackets: true,
                autofocus: true,
                extraKeys: {
                    "F11": function (cm) {
                        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    },
                    "Esc": function (cm) {
                        if (cm.getOption("fullScreen")) {
                            cm.setOption("fullScreen", false);
                        }
                    },
                    "Tab": "autocomplete"
                }
            });
            DesignerMVC.View.SqlEditor.on("change", function (cm, obj) {
                if (obj.origin == "setValue") {
                    $('#report-sqlTextIsChange').val(0);
                } else {
                    $('#report-sqlTextIsChange').val(1);
                }
            });
        },
        initHistorySqlEditor: function () {
            var dom = document.getElementById("report-history-sqlText");
            DesignerMVC.View.HistorySqlEditor = CodeMirror.fromTextArea(dom, {
                mode: 'text/x-mysql',
                theme: 'rubyblue',
                indentWithTabs: true,
                smartIndent: true,
                lineNumbers: true,
                matchBrackets: true,
                autofocus: true,
                extraKeys: {
                    "F11": function (cm) {
                        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    },
                    "Esc": function (cm) {
                        if (cm.getOption("fullScreen")) {
                            cm.setOption("fullScreen", false);
                        }
                    }
                }
            });
        },
        initPreviewSqlEditor: function () {
            var dom = document.getElementById("report-preview-sqlText");
            DesignerMVC.View.PreviewSqlEditor = CodeMirror.fromTextArea(dom, {
                mode: 'text/x-mysql',
                theme: 'rubyblue',
                indentWithTabs: true,
                smartIndent: true,
                lineNumbers: true,
                matchBrackets: true,
                autofocus: true
            });
        },
        bindEvent: function () {
            $('#btn-report-search').bind('click', DesignerMVC.Controller.find);
            $('#btn-report-parse-sql-var-names').bind('click', DesignerMVC.Controller.parseSqlVarNames);
            $('#btn-report-exec-sql').bind('click', DesignerMVC.Controller.executeSql);
            $('#btn-report-preview-sql').bind('click', DesignerMVC.Controller.previewSql);
            $('#btn-report-query-param-add').bind('click', function (e) {
                DesignerMVC.Controller.addOrEditQueryParam('add');
            });
            $('#btn-report-query-param-edit').bind('click', function (e) {
            	var edtIndex = $.trim($('#report-query-param-curIndex').val());
            	if(edtIndex == ''){
            		$.messager.show({
            			title : '提示',
            			msg : '请事先双击指定要修改的参数行',
            			timeout : 3000
            		});
            		return;
            	}
            	//
                DesignerMVC.Controller.addOrEditQueryParam('edit');
            });
            //
            $('#btn-refresh-report-ds-list').bind('click', DesignerMVC.Util.reloadDataSourceList);
            //
            $('#report-column-clrLvlStart').bind('change', function(){
            	$('#'+ this.id +'-feedback').css('background-color', this.value);
            });
            $('#report-column-clrLvlEnd').bind('change', function(){
            	$('#'+ this.id +'-feedback').css('background-color', this.value);
            });
        },
        bindValidate: function () {
        },
        initData: function () {
            DesignerMVC.Data.loadDataSourceList();
        }
    },
    Controller: {
        doOption: function (index, name) {
            $('#report-datagrid').datagrid('selectRow', index);
            if (name == "info") {
                return DesignerMVC.Controller.showDetail();
            }
            if (name == "edit") {
                return DesignerMVC.Controller.edit();
            }
            if (name == "copy") {
                return DesignerMVC.Controller.copy();
            }
            if (name == "preview") {
                return DesignerMVC.Controller.preview();
            }
            if (name == "remove") {
                return DesignerMVC.Controller.remove();
            }
            if (name == "history") {
                return DesignerMVC.Controller.showHistorySql();
            }
        },
        add: function () {
            var node = $('#category-tree').tree('getSelected');
            if (node) {
                var dsId = $('#report-dsId').combobox('getValue');
                var category = node.attributes;
                var options = DesignerMVC.Util.getOptions();
                options.title = '报表设计器--新增报表';
                EasyUIUtils.openAddDlg(options);
                DesignerMVC.Util.clearSqlEditor();
                EasyUIUtils.clearDatagrid('#report-meta-column-grid');
                EasyUIUtils.clearDatagrid('#report-query-param-grid');
                $('#report-query-param-form').form('reset');
                $('#report-query-param-curIndex').val('');
                //
                var row = {
                    name: "",
                    categoryId: category.id,
                    dsId: dsId,
                    status: 1,
                    sequence: 10,
                    options: {
                        layout: 2,
                        statColumnLayout: 1
                    }
                };
                DesignerMVC.Util.fillReportBasicConfForm(row, row.options);
            } else {
                $.messager.alert('警告', '请选中一个报表分类!', 'info');
            }
        },
        edit: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
            	EasyUIUtils.clearDatagrid('#report-meta-column-grid');
                EasyUIUtils.clearDatagrid('#report-query-param-grid');
                $('#report-query-param-form').form('reset');
                $('#report-query-param-curIndex').val('');
                //
                var options = DesignerMVC.Util.getOptions();
                options.iconCls = 'icon-designer';
                options.data = DesignerMVC.Util.copy(row);
                options.title = '报表设计器--修改[' + options.data.name + ']报表';
                DesignerMVC.Util.editOrCopy(options, 'edit');
            });
        },
        copy: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
            	EasyUIUtils.clearDatagrid('#report-meta-column-grid');
                EasyUIUtils.clearDatagrid('#report-query-param-grid');
                $('#report-query-param-form').form('reset');
                $('#report-query-param-curIndex').val('');
                //
                var options = DesignerMVC.Util.getOptions();
                options.iconCls = 'icon-designer';
                options.data = DesignerMVC.Util.copy(row);
                options.title = '报表设计器--复制[' + options.data.name + ']报表';
                DesignerMVC.Util.editOrCopy(options, 'copy');
                $('#modal-action').val("add");
            });
        },
        remove: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
                var options = {
                    rows: [row],
                    url: DesignerMVC.URLs.remove.url,
                    data: {
                        id: row.id
                    },
                    gridId: '#report-datagrid',
                    gridUrl: DesignerMVC.URLs.list.url + '?id=' + row.categoryId,
                    callback: function (rows) {
                    }
                };
                EasyUIUtils.remove(options);
            });
        },
        preview: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
                var url = DesignerMVC.URLs.Report.url + row.uid;
                parent.HomeIndex.addTab(row.id, row.name, url, "");
                //parent.HomeIndex.selectedTab();
            });
        },
        previewInNewWindow: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
                var url = DesignerMVC.URLs.Report.url + row.uid;
                var win = window.open(url, '_blank');
                win.focus();
            });
        },
        showDetail: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
                $('#report-detail-dlg').dialog('open').dialog('center');
                DesignerMVC.Util.fillDetailLabels(row);
            });
        },
        showJson: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
            	var dlgTitle = '报表JSON - 【 ' + row.name +' 】';
                $('#report-json-dlg').dialog('open').dialog('setTitle', dlgTitle);
                $('#report-json-text').val(JSON.stringify(row));
            });
        },
        addFromJson: function(){
            $('#report-json-copy-dlg').dialog('open');
            $('#report-json-src-text').val('/* 在这里粘贴 报表JSON，然后按确定即可 */');
        },
        showHistorySql: function () {
            DesignerMVC.Util.isRowSelected(function (row) {
                $('#report-history-sql-dlg').dialog('open').dialog('center');
                DesignerMVC.View.HistorySqlEditor.setValue('');
                DesignerMVC.View.HistorySqlEditor.refresh();
                var url = DesignerMVC.URLs.historyList.url + '?reportId=' + row.id;
                EasyUIUtils.loadDataWithCallback('#report-history-sql-grid', url, function () {
                    $('#report-history-sql-grid').datagrid('selectRow', 0);
                });
            });
        },
        showHistorySqlDetail: function (row) {
            DesignerMVC.View.HistorySqlEditor.setValue(row.sqlText || "");
            var data = EasyUIUtils.toPropertygridRows(row);
            $('#report-history-sql-pgrid').propertygrid('loadData', data);
        },
        find: function () {
            var keyword = $("#report-search-keyword").val();
            var url = DesignerMVC.URLs.find.url + '?fieldName=name&keyword=' + keyword;
            //
            var row = $("#report-datagrid").datagrid('getSelected');
            var reportId = row == null ? null : row.id;
            //
            EasyUIUtils.loadToDatagrid('#report-datagrid', url, function(){
            	DesignerMVC.Controller.selectRowById(reportId);
            });
        },
        selectRowById: function(reportId){
        	if(reportId != null){
        		var rows = $("#report-datagrid").datagrid('getRows');
            	var index = -1;
            	for(var i=0, c=rows.length; i<c; i++){
            		if(rows[i].id == reportId){
            			index = i;
            			break;
            		}
            	}
            	if(index != -1){
            		$("#report-datagrid").datagrid('selectRow', index);
            	}
        	}
        },
        parseSqlVarNames : function(){//解析sql脚本变量名
        	var sqlText = DesignerMVC.View.SqlEditor.getValue();
        	$.ajax(DesignerMVC.URLs.parseSqlVarNames.url, {
        		method : DesignerMVC.URLs.parseSqlVarNames.method,
        		contentType : "text/plain;charset=utf8",
        		dataType : 'json',
        		data : sqlText,
        		processData : false,
        		success : function(result){
        			if(result.code == 0){
        				var varNames = result.data;
        				//console.log(varNames);
        				var paramRows = $("#report-query-param-grid").datagrid('getRows');
        				//console.log(paramRows);
        				for(var i= 0, c = paramRows.length; i < c; i++){
        					var tmpRow = paramRows[i];
        					var index = varNames.indexOf(tmpRow.name);
        					if(index !=-1){
        						tmpRow.required = true;
        						//
        						varNames.splice(index, 1);
        					}
        				}
        				//
        				var newCount = varNames.length;
        				for(var i=0; i<newCount; i++){
        					var name = varNames[i];
        					var info = DesignerMVC.Util.newParamInfo(name);
        					paramRows.push(info);
        				}
        				//
        				$('#report-query-param-form').form('reset');
        				$('#report-query-param-curIndex').val('');
                        EasyUIUtils.clearDatagrid('#report-query-param-grid');
                        $("#report-query-param-grid").datagrid('loadData', paramRows);
        				$.messager.alert('提示', "参数列表已刷新" + (newCount >0 ? '，新发现并追加了变量参数：<br/></br>'+ varNames.join('、') + '</br></br>注意：补全变量参数类型和默认初值信息' : ''), 'info');
        			}
        			else {
        				$.messager.alert('提取sql变量失败', result.msg, 'error');
        			}
        		}
        	});
        },
        executeSql: function () {
            if (!DesignerMVC.Util.checkBasicConfParam()) return;

            $.messager.progress({
                title: '请稍后...',
                text: '正在执行中...'
            });

            $.post(DesignerMVC.URLs.execSqlText.url, {
                sqlText: DesignerMVC.View.SqlEditor.getValue(),
                dsId: $('#report-dsId').combobox('getValue'),
                queryParams: DesignerMVC.Util.getQueryParams()
            }, function (result) {
                $.messager.progress("close");
                if (!result.code) {
                    $("#report-meta-column-grid").datagrid('clearChecked');
                    var columns = DesignerMVC.Util.eachMetaColumns(result.data);
                    return DesignerMVC.Util.loadMetaColumns(columns);
                }
                return $.messager.alert('错误', result.msg);
            }, 'json');
        },
        previewSql: function () {
            if (!DesignerMVC.Util.checkBasicConfParam()) return;

            $.messager.progress({
                title: '请稍后...',
                text: '正在生成预览SQL...',
            });

            $.post(DesignerMVC.URLs.previewSqlText.url, {
                dsId: $('#report-dsId').combobox('getValue'),
                sqlText: DesignerMVC.View.SqlEditor.getValue(),
                queryParams: DesignerMVC.Util.getQueryParams()
            }, function (result) {
                $.messager.progress("close");
                if (!result.code) {
                    $('#report-preview-sql-dlg').dialog('open');
                    $('#report-preview-sql-dlg .CodeMirror').css("height", "99%");
                    return DesignerMVC.View.PreviewSqlEditor.setValue(result.data);
                }
                return $.messager.alert('错误', result.msg);
            }, 'json');
        },
        save: function () {
            if (!DesignerMVC.Util.checkBasicConfParam()) return;
            
            //检查列定义
            var rows = $("#report-meta-column-grid").datagrid('getRows');
            if (rows == null || rows.length == 0) {
                return $.messager.alert('失败', "没有任何报表SQL配置列选项！", 'error');
            }
            
            var metaColumns = DesignerMVC.Util.getMetaColumns(rows);
            var columnTypeMap = DesignerMVC.Util.getColumnTypeMap(metaColumns);
            if (columnTypeMap.layout == 0 || columnTypeMap.stat == 0) {
                return $.messager.alert('失败', "您没有设置布局列或者统计列", 'error');
            }

            var emptyExprColumns = DesignerMVC.Util.getEmptyExprColumns(metaColumns);
            if (emptyExprColumns && emptyExprColumns.length) {
                return $.messager.alert('失败', "计算列：[" + emptyExprColumns.join() + "]没有设置表达式！", 'error');
            }
            
            //检查参数定义
            var paramRows = $("#report-query-param-grid").datagrid('getRows');
            var paramMap = {};
            var cascNames = [];
            for(var i=0, c=paramRows.length; i<c; i++){
            	var row = paramRows[i];
            	var name = $.trim(row.name);
            	if(paramMap[name] != null){
            		return $.messager.alert('失败', "第 " + (i+1) + " 个参数【" + row.text +"】的代码和前面的参数重复", 'error');
            	}
            	paramMap[name] = row;
            	//
            	if(row.formElement == 'select' || row.formElement == 'selectMul'){
            		row.cascName = $.trim(row.cascName || '');
            		if(row.cascName){
            			if(cascNames.indexOf(row.cascName) == -1){
            				cascNames.push(row.cascName);
            			}
            		}
            	}
            	else {
            		row.cascName = '';
            	}
            }
            //检查cascName的有效性（必须指向下拉控件）
            for(var i=0, c=cascNames.length; i<c; i++){
            	var cascName = cascNames[i];
            	var target = paramMap[cascName];
            	if(target == null){
            		return $.messager.alert('失败', "级联参数代码【" + cascName +"】无效：不存在对应代码的参数", 'error');
            	}
            	if(target.formElement != 'select' && target.formElement != 'selectMul' || target.dataSource != 'sql'){
            		return $.messager.alert('失败', "级联参数代码【" + cascName +"】无效：对应的参数只能是基于sql的下拉控件类型的参数", 'error');
            	}
            }
            

            $.messager.progress({
                title: '请稍后...',
                text: '正在处理中...',
            });

            $('#report-queryParams').val(DesignerMVC.Util.getQueryParams(paramRows));

            var action = $('#modal-action').val();
            var actUrl = action === "edit" ? DesignerMVC.URLs.edit.url : DesignerMVC.URLs.add.url;
            var data = $('#report-basic-conf-form').serializeObject();
            var reportId = data.id;
            data.isChange = $('#report-sqlTextIsChange').val() != 0;
            data.sqlText = DesignerMVC.View.SqlEditor.getValue();
            data["options"] = JSON.stringify({
                layout: data.layout,
                statColumnLayout: data.statColumnLayout
            });
            data["metaColumns"] = JSON.stringify(metaColumns);

            $.post(actUrl, data, function (result) {
                $.messager.progress("close");
                if (!result.code) {
                    $('#report-sqlTextIsChange').val('0');
                    var catId = $("#report-categoryId").val();
                    return $.messager.alert('操作提示', "操作成功", 'info', function () {
                        $('#report-designer-dlg').dialog('close');
                        DesignerMVC.Controller.listReports(catId, reportId);
                    });
                }
                $.messager.alert('操作提示', result.msg, 'error');
            }, 'json');
        },
        reload: function () {
            EasyUIUtils.reloadDatagrid('#report-datagrid');
        },
        saveChanged: function (data, handler) {
            var isChanged = $("#report-sqlTextIsChange").val() != 0;
            if (!isChanged) {
                return handler(data);
            }
            $.messager.confirm('确认', '是否保存修改的数据?', function (r) {
                if (r) {
                    //MetaDataDesigner.save();
                }
                //handler(data);
            });
        },
        deleteQueryParam: function (index) {
            $("#report-query-param-grid").datagrid('deleteRow', index);
            //
            var edtIndex = $.trim($('#report-query-param-curIndex').val());
            if(edtIndex != ''){
            	edtIndex = parseInt(edtIndex);
            	if(edtIndex == index){//已删除编辑行
            		$('#report-query-param-form').form('reset');
                	$('#report-query-param-curIndex').val('');
                }
                else if(index < edtIndex){//删除了前面的行
                	//需要调整编辑行的索引
                	edtIndex--;
                	$('#report-query-param-curIndex').val(edtIndex);
                }
            }
            //
            var rows = $("#report-query-param-grid").datagrid('getRows');
            EasyUIUtils.clearDatagrid('#report-query-param-grid');
            $("#report-query-param-grid").datagrid('loadData', rows);
        },
        addOrEditQueryParam: function (act) {
            if ($("#report-query-param-form").form('validate')) {
                var row = $('#report-query-param-form').serializeObject()
                if (row.dataSource != "none" && $.trim(row.content).length == 0) {
                    $("#report-query-param-content").focus();
                    return $.messager.alert('提示', "内容不能为空", 'error');
                }
                if(row.formElement == 'select' || row.formElement == 'selectMul'){
                	if(row.cascName != null){
                		row.cascName = $.trim(row.cascName);
                	}
                }
                else {//清除非下拉的cascName
                	row.cascName = '';
                }
                row.required = $("#report-query-param-required").prop("checked");
                row.hidden = $("#report-query-param-hidden").prop("checked");
                row.autoComplete = $("#report-query-param-autoComplete").prop("checked");

                var index = -1;
                var rows = [];
                if (act == "add") {
                    $('#report-query-param-grid').datagrid('appendRow', row);
                    rows = $("#report-query-param-grid").datagrid('getRows');
                    index = rows.length -1;
                } else if (act == "edit") {
                    index = $("#report-query-param-curIndex").val();
                    index = parseInt(index);
                	$('#report-query-param-grid').datagrid('updateRow', {
                        index: index,
                        row: row
                    });
                	rows = $("#report-query-param-grid").datagrid('getRows');
                }
                $('#report-query-param-form').form('reset');
                $('#report-query-param-curIndex').val('');//
                //
                EasyUIUtils.clearDatagrid('#report-query-param-grid');
                $("#report-query-param-grid").datagrid('loadData', rows);
                $("#report-query-param-grid").datagrid('selectRow', index);
            }
        },
        showMetaColumnOption: function (index, name) {
            $("#report-meta-column-grid").datagrid('selectRow', index);
            var row = $("#report-meta-column-grid").datagrid('getSelected');
            if (name == "expression") {
                $('#report-column-expression-dlg').dialog('open');
                return $("#report-column-expression").val(row.expression);
            }
            if (name == "comment") {
                $('#report-column-comment-dlg').dialog('open');
                return $("#report-column-comment").val(row.comment);
            }
        },
        showMetaColumnClrLvl: function (index) {
            $("#report-meta-column-grid").datagrid('selectRow', index);
            var row = $("#report-meta-column-grid").datagrid('getSelected');
            $('#report-column-clrLvl-dlg').dialog('open');
            $("#report-column-clrLvlEnabled").prop('checked', row.clrLvlEnabled || false);
            $("#report-column-clrLvlValve").val(row.clrLvlValve || 3);
            $("#report-column-clrLvlStart").val(row.clrLvlStart);
            $("#report-column-clrLvlEnd").val(row.clrLvlEnd);
            $("#report-column-clrLvlIgnore0").prop('checked', row.clrLvlIgnore0 || false);
            //
            $("#report-column-clrLvlStart").trigger('change');
            $("#report-column-clrLvlEnd").trigger('change');
        },
        showMetaColumnLinkFunc: function (index) {
        	$("#report-meta-column-grid").datagrid('selectRow', index);
            var row = $("#report-meta-column-grid").datagrid('getSelected');
            $('#report-column-linkFunc-dlg').dialog('open');
            var linkFuncExpr = row.linkFuncExpr || '';
            if(linkFuncExpr == ''){
            	linkFuncExpr = 'showReportDetail( [ ' + row.name + ' ] )';
            }
            $("#report-column-linkFuncExpr").val(linkFuncExpr);
            $("#report-column-linkFuncIgnore0").prop('checked', row.linkFuncIgnore0 || false);
        },
        setMetaColumnFormat:function(){
        	var row = $("#report-meta-column-grid").datagrid('getSelected');
        	if(row == null){
        		$.messager.alert('默认格式设置', '请选中一个日期、时间或数值的列!', 'warning');
        		return;
        	}
        	var index = $("#report-meta-column-grid").datagrid('getRowIndex', row);
        	var colName = row.text || row.name;
        	var sqlTypeName = row.sqlType || "";
        	var format = null;
        	if("DATE" == sqlTypeName){
        		format = 'yyyy-MM-dd';
        	}
        	else if("TIMESTAMP" == sqlTypeName){
        		format = 'yyyy-MM-dd HH:mm:ss';
        	}
        	else if("TIME" == sqlTypeName){
        		format = 'HH:mm:ss';
        	}//
        	else if("DECIMAL" == sqlTypeName || "DOUBLE" == sqlTypeName || "FLOAT" == sqlTypeName || "REAL" == sqlTypeName){
        		format = '#0.00';
        	}
        	else if(sqlTypeName.indexOf('INT') != -1){
        		format = '#0';
        	}
        	if(format){
        		row.format = format;
        		var rows = $("#report-meta-column-grid").datagrid('getRows');
                $("#report-meta-column-grid").datagrid('loadData', rows);
                $("#report-meta-column-grid").datagrid('selectRow', index);
                //
                $.messager.show({
                	title:'提示',
                	msg: '【' + colName + '】的默认格式已设置',
                	timeout:5000,
                	showType:'slide'
                });
        	}
        },
        refreshMetaColumnList: function(){
        	var rows = $("#report-meta-column-grid").datagrid('getRows');
            if (rows == null || rows.length == 0) {
                return;
            }
            var index = -1;
        	var row = $("#report-meta-column-grid").datagrid('getSelected');
        	if(row){
        		index = $("#report-meta-column-grid").datagrid('getRowIndex', row);
        	}
            var metaColumns = DesignerMVC.Util.getMetaColumns(rows);
            $("#report-meta-column-grid").datagrid('loadData', metaColumns);
            if(index !=-1){
            	$("#report-meta-column-grid").datagrid('selectRow', index);
            }
        },
        saveMetaColumnClrLvl: function(){
            var row = $("#report-meta-column-grid").datagrid('getSelected');
            row.clrLvlEnabled = $("#report-column-clrLvlEnabled").prop('checked');
            row.clrLvlValve = parseInt($("#report-column-clrLvlValve").val()) || 3;
            row.clrLvlStart = $.trim($("#report-column-clrLvlStart").val());
            row.clrLvlEnd = $.trim($("#report-column-clrLvlEnd").val());
            row.clrLvlIgnore0 = $("#report-column-clrLvlIgnore0").prop('checked');
            //TODO 验证数据
            $('#report-column-clrLvl-dlg').dialog('close');
            //
            var index = $("#report-meta-column-grid").datagrid('getRowIndex', row);
            var rows = $("#report-meta-column-grid").datagrid('getRows');
            $("#report-meta-column-grid").datagrid('loadData', rows);
            $("#report-meta-column-grid").datagrid('selectRow', index);
        },
        saveMetaColumnLinkFunc: function(){
            var row = $("#report-meta-column-grid").datagrid('getSelected');
            row.linkFuncExpr = $.trim($("#report-column-linkFuncExpr").val()) || null;
            row.linkFuncIgnore0 = $("#report-column-linkFuncIgnore0").prop('checked');
            //TODO 验证数据
            $('#report-column-linkFunc-dlg').dialog('close');
            //
            var index = $("#report-meta-column-grid").datagrid('getRowIndex', row);
            var rows = $("#report-meta-column-grid").datagrid('getRows');
            $("#report-meta-column-grid").datagrid('loadData', rows);
            $("#report-meta-column-grid").datagrid('selectRow', index);
        },
        saveMetaColumnOption: function (name) {
            var row = $("#report-meta-column-grid").datagrid('getSelected');
            if (name == "expression") {
                row.expression = $("#report-column-expression").val();
                return $('#report-column-expression-dlg').dialog('close');
            }
            if (name == "comment") {
                row.comment = $("#report-column-comment").val();
                return $('#report-column-comment-dlg').dialog('close');
            }
        },
        listReports: function (catId, reportId) {
            var gridUrl = DesignerMVC.URLs.list.url + '?id=' + catId;
            //EasyUIUtils.loadDataWithUrl('#report-datagrid', gridUrl);
            EasyUIUtils.loadDataWithUrl('#report-datagrid', gridUrl, function(){
            	DesignerMVC.Controller.selectRowById(reportId);
            });
        }
    },
    Util: {
    	isJustVarName : function(chkStr){
    		var reg = /^\s*[a-z_0-0]*\s*$/ig;
    		return reg.test(chkStr);
    	},
    	copy: function(src){
    		var ret = null;
    		if(src != null){
    			if($.isArray(src)){
        			 ret = [];
        		}
    			else {
    				ret = {};
    			}
    			$.extend(true, ret, src);
    		}
    		return ret;
    	},
    	//智能判断参数类型
    	newParamInfo: function(name){
    		var info = {
				name : name,
				text : name,
				dataType : 'string',
				defaultValue : '',
				defaultExpr : '',
				cascName : '',
				formElement : 'text',
				dataSource : 'none',
				content : '',
				width : '100',
				required : true,
				hidden : false,
				autoComplete : false
			};
    		//
    		if(/(Date|_date|Time|_time|Ts|_ts)/.test(info.name)){
    			info.dataType = 'date';
    			info.formElement = 'date';
    			info.defaultValue = DesignerMVC.Util.getCurrentDate();
    			info.text = info.text.replace(/(Date|_date)/, '日期');
    			info.text = info.text.replace(/(Time|_time|Ts|_ts)/, '时间');
    			if(info.text.toLowerCase().indexOf('start') != -1){
    				info.text = info.text.replace(/start/i, '起始');
    				info.text = info.text.replace(/end/i, '截止');
    			}
    			else {
    				info.text = info.text.replace(/begin/i, '开始');
    				info.text = info.text.replace(/end/i, '结束');
    			}
    		}
    		else if(/^(is|has)|(ed|Flag|_flag)$/.test(info.name)){
    			info.dataType = 'bool';
    			info.formElement = 'checkbox';
    			info.defaultValue = 'false';
    			info.text = info.text.replace('is', '是否');
    			info.text = info.text.replace('has', '有无');
    			info.text = info.text.replace(/(Flag|_flag)/, '标记');
    			info.text = info.text.replace(/(disabled|disable)/i, '禁用');
    			info.text = info.text.replace(/(enabled|enable)/i, '启用');
    			info.text = info.text.replace(/(deleted|delete)/i, '删除');
    		}
    		else if(/(Count|_count|Quantity|Qty|_quantity|_qty|Times|_times|Id|_id|Hours|_hours|Minutes|Mins|_minutes|_mins|Seconds|Snds|_seconds|_snds)$/.test(info.name)){
    			info.dataType = 'integer';
    			info.defaultValue = '0';
    			info.text = info.text.replace(/(Count|_count)$/, '数量');
    			info.text = info.text.replace(/(Quantity|Qty|_quantity|_qty)$/, '数量');
    			info.text = info.text.replace(/(Times|_times)$/, '次数');
    			info.text = info.text.replace(/(Id|_id)$/, 'ID');
    			info.text = info.text.replace(/(Hours|_hours)$/, '小时数');
    			info.text = info.text.replace(/(Minutes|Mins|_minutes|_mins)$/, '分钟数');
    			info.text = info.text.replace(/(Seconds|Snds|_seconds|_snds)$/, '秒数');
    		}
    		else if(/(Price|_price|Amount|Amnt|_amount|_amnt|Rate|_rate|Ratio|_ratio|Percent|Pcnt|Pct|_percent|_pcnt|_pct|Money|Mny|_money|_mny)$/.test(info.name)){
    			info.dataType = 'float';
    			info.defaultValue = '0';
    			info.text = info.text.replace(/(Price|_price)$/, '价格');
    			info.text = info.text.replace(/(Amount|Amnt|_amount|_amnt)$/, '金额');
    			info.text = info.text.replace(/(Rate|_rate)$/, '比率');
    			info.text = info.text.replace(/(Ratio|_ratio)$/, '比例');
    			info.text = info.text.replace(/(Percent|Pcnt|Pct|_percent|_pcnt|_pct)$/, '百分比');
    			info.text = info.text.replace(/(Money|Mny|_money|_mny)$/, '钱数');
    		}
    		info.text = info.text.replace(/year/i, '年份');
    		info.text = info.text.replace(/month/i, '月份');
    		info.text = info.text.replace(/day/i, '日期');
    		//
    		info.text = info.text.replace('create', '创建');
			info.text = info.text.replace('update', '更新');
			info.text = info.text.replace('modify', '修改');
			info.text = info.text.replace('change', '变更');
			info.text = info.text.replace('disable', '禁用');
			info.text = info.text.replace('enable', '启用');
			info.text = info.text.replace('delete', '删除');
			//
			info.text = info.text.replace('total', '总的');
			info.text = info.text.replace('sum', '汇总');
			info.text = info.text.replace('avg', '平均');
			info.text = info.text.replace('max', '最大');
			info.text = info.text.replace('min', '最小');
			info.text = info.text.replace('top', '靠前');
			info.text = info.text.replace('btm', '垫底');
			info.text = info.text.replace('old', '老的');
			info.text = info.text.replace('new', '新的');
			info.text = info.text.replace('prev', '之前');
			info.text = info.text.replace('next', '之后');
			//
			info.text = info.text.replace(/from/i, '从');
    		info.text = info.text.replace(/to/i, '到');
    		//
			return info;
    	},
        getOptions: function () {
            return {
                dlgId: '#report-designer-dlg',
                formId: '#report-basic-conf-form',
                actId: '#modal-action',
                rowId: '#report-id',
                title: '',
                iconCls: 'icon-add',
                data: {},
                callback: function (arg) {
                },
                gridId: null,
            };
        },
        isRowSelected: function (func) {
            var row = $('#report-datagrid').datagrid('getSelected');
            if (row) {
                func(row);
            } else {
                $.messager.alert('警告', '请选中一条记录!', 'info');
            }
        },
        editOrCopy: function (options, act, reserveCopyName) {
        	reserveCopyName = reserveCopyName || false;//是否保留copy名称
        	//
            DesignerMVC.Util.clearSqlEditor();
            EasyUIUtils.openEditDlg(options);

            var row = options.data;
            if (act === 'copy') {
            	row.id = null; ////id不能省（后台实现的bug）
            	if(!reserveCopyName){
            		row.name = '';
            	}
            }
            DesignerMVC.Util.fillReportBasicConfForm(row, $.toJSON(row.options));
            DesignerMVC.View.SqlEditor.setValue(row.sqlText || "");
            DesignerMVC.Util.loadMetaColumns($.toJSON(row.metaColumns));
            DesignerMVC.Util.loadQueryParams($.toJSON(row.queryParams));
        },
        clearSqlEditor: function () {
            DesignerMVC.View.SqlEditor.setValue('');
            DesignerMVC.View.SqlEditor.refresh();

            DesignerMVC.View.PreviewSqlEditor.setValue('');
            DesignerMVC.View.PreviewSqlEditor.refresh();

            DesignerMVC.View.HistorySqlEditor.setValue('');
            DesignerMVC.View.HistorySqlEditor.refresh();
        },
        resizeDesignerDlgElments: function () {
            var panelOptions = $('#report-designer-dlg').panel('options');
            $('#report-sqlText-td>.CodeMirror').css({"width": panelOptions.width - 230});

            var tabHeight = panelOptions.height - 160;
            var confFormDivHeight = $('#report-basic-conf-form-div').height();
            var metaColumnDivHeight = (tabHeight - confFormDivHeight);
            if (metaColumnDivHeight <= 180) metaColumnDivHeight = 180;
            $('#report-meta-column-div').css({
                "height": metaColumnDivHeight
            });

            var queryParamFormDivHeight = $('#report-query-param-form-div').height();
            var queryParamDivHeight = (tabHeight - queryParamFormDivHeight - 200);
            if (queryParamDivHeight <= 180) queryParamDivHeight = 180;
            $('#report-query-param-div').css({
                "height": queryParamDivHeight
            });
        },
        fullscreenEdit: function () {
            DesignerMVC.View.SqlEditor.setOption("fullScreen", !DesignerMVC.View.SqlEditor.getOption("fullScreen"));
        },
        fillReportBasicConfForm: function (row, options) {
            $('#report-basic-conf-form').form('load', row);
            $('#report-basic-conf-form').form('load', options);
            $('#report-category-name').text(row.categoryName);
            $('#report-categoryId').text(row.categoryId);
        },
        fillDetailLabels: function (data) {
            $('#report-detail-dlg label').each(function (i) {
                $(this).text("");
            });

            for (var name in data) {
                var id = "#report-detail-" + name;
                var value = DesignerMVC.Util.getPropertyValue(name, data);
                $(id).text(value);
            }

            var options = $.toJSON(data.options);
            for (var name in options) {
                var id = "#report-detail-" + name;
                var value = DesignerMVC.Util.getPropertyValue(name, options);
                $(id).text(value);
            }
        },
        getPropertyValue: function (name, object) {
            var value = object[name];
            if (name == "layout" ||
                name == "statColumnLayout") {
                return DesignerMVC.Util.getLayoutName(value);
            }
            if (name == "status") {
                return value == 1 ? "启用" : "禁用";
            }
            return value;
        },
        getLayoutName: function (layout) {
            if (layout == 1) {
                return "横向布局";
            }
            if (layout == 2) {
                return "纵向布局";
            }
            return "无";
        },
        getColumnTypeValue: function (name) {
            if (name == "LAYOUT") {
                return 1;
            }
            if (name == "DIMENSION") {
                return 2;
            }
            if (name == "STATISTICAL") {
                return 3;
            }
            if (name == "COMPUTED") {
                return 4;
            }
            return 2;
        },
        getColumnSortTypeValue: function (name) {
            if (name == "DEFAULT") {
                return 0;
            }
            if (name == "DIGIT_ASCENDING") {
                return 1;
            }
            if (name == "DIGIT_DESCENDING") {
                return 2;
            }
            if (name == "CHAR_ASCENDING") {
                return 3;
            }
            if (name == "CHAR_DESCENDING") {
                return 4;
            }
            return 0;
        },
        isNumbericOrDateCol: function(sqlTypeName){
        	if(sqlTypeName.indexOf('INT') != -1 || "DECIMAL" == sqlTypeName || "DOUBLE" == sqlTypeName || "FLOAT" == sqlTypeName || "REAL" == sqlTypeName){
        		return true;
        	}
        	if("DATE" == sqlTypeName || "TIMESTAMP" == sqlTypeName || "TIME" == sqlTypeName){
        		return true;
        	}
        	return false;
        },
        isNumbericCol: function(sqlTypeName){
        	if(sqlTypeName.indexOf('INT') != -1 || "DECIMAL" == sqlTypeName || "DOUBLE" == sqlTypeName || "FLOAT" == sqlTypeName || "REAL" == sqlTypeName){
        		return true;
        	}
        	return false;
        },
        isDecimalCol: function(sqlTypeName){
        	if("DECIMAL" == sqlTypeName || "DOUBLE" == sqlTypeName || "FLOAT" == sqlTypeName || "REAL" == sqlTypeName){
        		return true;
        	}
        	return false;
        },
        checkBasicConfParam: function () {
            if (DesignerMVC.View.SqlEditor.getValue() == "") {
                $.messager.alert('失败', "未发现操作的SQL语句！", 'error');
                return false;
            }
            return $('#report-basic-conf-form').form('validate');
        },
        loadMetaColumns: function (newColumns) {
            var oldColumns = $("#report-meta-column-grid").datagrid('getRows');
            //如果列表中没有元数据列则直接设置成新的元数据列
            if (oldColumns == null || oldColumns.length == 0) {
                return $("#report-meta-column-grid").datagrid('loadData', newColumns);
            }
            //如果列表中存在旧的列则需要替换相同的列并增加新列
            oldColumns = DesignerMVC.Util.getMetaColumns(oldColumns);
            var oldColIndexMap = {};//旧有列
            var oldCalcColIndexMap = {};//旧有的计算列
            for (var i = 0; i < oldColumns.length; i++) {
            	var oldCol = oldColumns[i];
                var nameLwr = oldCol.name.toLowerCase();
                oldColIndexMap[nameLwr] = i;
                if(oldCol.type == 4){//计算列
                	oldCalcColIndexMap[nameLwr] = i;
                }
            }
            var newColCount = newColumns.length;
            for (var i = 0; i < newColCount; i++) {
            	var newCol = newColumns[i];
                var nameLwr = newCol.name.toLowerCase();
                var oldColIndex = oldColIndexMap[nameLwr];
                if (oldColIndex != null) {
                	var oldCol = oldColumns[oldColIndex];
                	oldCol.name = newCol.name;//
                	oldCol.sqlType = newCol.sqlType;
                	oldCol.theType = newCol.theType;
                	oldCol.className = newCol.className;
                	oldCol.widthInChars = newCol.widthInChars;
                	oldCol.align = oldCol.align || newCol.align;
                	if(DesignerMVC.Util.isJustVarName(oldCol.text)){
                		oldCol.text = newCol.text;
                	}
                	//
                	newColumns[i] = oldCol;
                }
                //删除重名计算列信息
                delete oldCalcColIndexMap[nameLwr];
            }
            //追加保留计算列
            for(var oldCalcName in oldCalcColIndexMap){
            	var oldColIndex = oldCalcColIndexMap[oldCalcName];
            	newColumns[newColCount++] = oldColumns[oldColIndex];
            }
            //
            return $("#report-meta-column-grid").datagrid('loadData', newColumns);
        },
        getMetaColumns: function (columns) {
        	var tmpVal = null;
            for (var rowIndex = 0; rowIndex < columns.length; rowIndex++) {
                var column = columns[rowIndex];
                var subOptions = DesignerMVC.Util.getCheckboxOptions(column.type);
                for (var optIndex = 0; optIndex < subOptions.length; optIndex++) {
                    var option = subOptions[optIndex];
                    var optionId = "#" + option.name + rowIndex;
                    column[option.name] = $(optionId).prop("checked");
                }
                column["name"] = $.trim($("#name" + rowIndex).val());
                column["text"] = $.trim($("#text" + rowIndex).val());
                column["type"] = $("#type" + rowIndex).val();
                tmpVal = parseInt($.trim($("#width" + rowIndex).val()));
                column["width"] = isNaN(tmpVal) ? null : tmpVal;
                column["align"] = $("#align" + rowIndex).val();
                column["format"] = $("#format" + rowIndex).val() || '';
                column["sortType"] = $("#sortType" + rowIndex).val();
                column["percent"] = $("#percent" + rowIndex).prop("checked") || false;
            }
            return columns;
        },
        eachMetaColumns: function (columns) {
            if (columns && columns.length) {
                for (var i = 0; i < columns.length; i++) {
                    var column = columns[i];
                    column.type = DesignerMVC.Util.getColumnTypeValue(column.type);
                    column.sortType = DesignerMVC.Util.getColumnSortTypeValue(column.sortType);
                }
            }
            return columns;
        },
        getCheckboxOptions: function (type) {
            var subOptions = [];
            if (type == 4) {
                subOptions = $.grep(DesignerMVC.Model.MetaColumnOptions, function (option, i) {
                    return option.type == 1;
                });
            } else if (type == 3) {
                subOptions = $.grep(DesignerMVC.Model.MetaColumnOptions, function (option, i) {
                    return option.type == 1 || option.type == 2;
                });
            } else {
                subOptions = $.grep(DesignerMVC.Model.MetaColumnOptions, function (option, i) {
                    return option.type == 3;
                });
            }
            return subOptions;
        },
        getEmptyExprColumns: function (columns) {
            var emptyColumns = [];
            for (var i = 0; i < columns.length; i++) {
                var column = columns[i];
                if (column.type == 4 && $.trim(column.expression) == "") {
                    emptyColumns.push(column.name);
                }
            }
            return emptyColumns;
        },
        getColumnTypeMap: function (rows) {
            var typeMap = {
                "layout": 0,
                "dim": 0,
                "stat": 0,
                "computed": 0
            };
            for (var i = 0; i < rows.length; i++) {
                if (rows[i].type == 1) {
                    typeMap.layout += 1;
                } else if (rows[i].type == 2) {
                    typeMap.dim += 1;
                } else if (rows[i].type == 3) {
                    typeMap.stat += 1;
                } else if (rows[i].type == 4) {
                    typeMap.computed += 1;
                }
            }
            return typeMap;
        },
        loadQueryParams: function (params) {
            EasyUIUtils.clearDatagrid('#report-query-param-grid');
            var jsonText = JSON.stringify(params);
            if (params instanceof Array) {
                $("#report-query-param-grid").datagrid('loadData', params);
            } else {
                jsonText = "";
            }
            $("#report-query-param-json").val(jsonText);
        },
        getQueryParams: function (curRows) {
            var rows = curRows == null ? $("#report-query-param-grid").datagrid('getRows') : curRows;
            return rows ? JSON.stringify(rows) : "";
        },
        //刷新/重新加载数据源列表
        reloadDataSourceList : function(){
        	var dsId = $('#report-dsId').combobox('getValue');
        	DesignerMVC.Data.loadDataSourceList(dsId);
        },
        getCurrentDate: function() {
            var d = new Date();
            var str = '', tmp = '';
            str += d.getFullYear() + '-';
            //
            tmp = d.getMonth() + 1 +'';
            if(tmp.length <2){
            	tmp = '0'+tmp;
            }
            str += tmp + '-';
            //
            tmp = d.getDate() + '';
            if(tmp.length <2){
            	tmp = '0'+tmp;
            }
            str += tmp;
            return str;
        }
    },
    Data: {
        loadDataSourceList: function (dsId) {
            $.getJSON(DesignerMVC.URLs.DataSource.listAll.url, function (result) {
                if (result.code) {
                    console.error(result.msg);
                }
                DesignerMVC.Model.DataSourceList = result.data || [];
                if(typeof dsId == 'undefined'){
                	dsId = null;
                }
                EasyUIUtils.fillCombox("#report-dsId", result.data, dsId, "id");
            });
        }
    }
};
