$(function () {
    MetaDataDs.init();
});

var MetaDataDs = {
    init: function () {
    	DsMVC.View.initData();//放到脚本最后面了
    	var loader = $.messager.progress({
            title: '请稍后...',
            text: '加载数据中...'
        });
    	setTimeout(function(){
    		 DsMVC.View.initControl();
    	     DsMVC.View.bindEvent();
    	     DsMVC.View.bindValidate();
    	     //
    	     $.messager.progress("close");
    	}, 1000);
    }
};

var DsCommon = {
    baseUrl: EasyReport.ctxPath + '/rest/report/ds/',
    baseConfUrl: EasyReport.ctxPath + '/rest/report/conf/',
    baseIconUrl: EasyReport.ctxPath + '/custom/easyui/themes/icons/',
    keys: {
        dbType: 'dbType',
        dbPoolType: 'dbPoolType'
    }
};

var DsMVC = {
    URLs: {
        add: {
            url: DsCommon.baseUrl + 'add',
            method: 'POST'
        },
        edit: {
            url: DsCommon.baseUrl + 'edit',
            method: 'POST'
        },
        list: {
            url: DsCommon.baseUrl + 'list',
            method: 'GET'
        },
        remove: {
            url: DsCommon.baseUrl + 'remove',
            method: 'POST'
        },
        testConnection: {
            url: DsCommon.baseUrl + 'testConnection',
            method: 'POST'
        },
        testConnectionById: {
            url: DsCommon.baseUrl + 'testConnectionById',
            method: 'POST'
        },
        getConfItems: {
            url: DsCommon.baseConfUrl + 'getConfItems',
            method: 'GET'
        }
    },
    Model: {
        dbTypes: {},
        dbPoolTypes: {}
    },
    View: {
    	initData: function () {
            DsMVC.Util.loadConfigItems();
        },
        initControl: function () {
            $('#ds-datagrid').datagrid({
                method: 'get',
                fit: true,
                fitColumns: true,
                singleSelect: true,
                pagination: true,
                rownumbers: true,
                pageSize: 50,
                url: DsMVC.URLs.list.url,
                toolbar: [{
                    iconCls: 'icon-add',
                    handler: function () {
                        DsMVC.Controller.add();
                    }
                }, '-', {
                    iconCls: 'icon-edit1',
                    handler: function () {
                        DsMVC.Controller.edit();
                    }
                }, '-', {
                    iconCls: 'icon-remove1',
                    handler: function () {
                        DsMVC.Controller.remove();
                    }
                }, '-', {
                    iconCls: 'icon-reload',
                    handler: function () {
                        EasyUIUtils.reloadDatagrid('#ds-datagrid');
                    }
                }],
                loadFilter: function (src) {
                    if (!src.code) {
                        return src.data;
                    }
                    return $.messager.alert('失败', src.msg, 'error');
                },
                columns: [[{
                    field: 'id',
                    title: '标识',
                    width: 50,
                    sortable: true
                }, {
                    field: 'name',
                    title: '名称',
                    width: 100,
                    sortable: true
                }, {
                    field: 'jdbcUrl',
                    title: 'JdbcUrl',
                    width: 200,
                    sortable: true
                }, {
                    field: 'driverClass',
                    title: '驱动类',
                    width: 100,
                    sortable: true
                }, {
                    field: 'queryerClass',
                    title: '查询器类',
                    width: 100,
                    sortable: true
                }, {
                    field: 'poolClass',
                    title: '连接池类',
                    width: 100,
                    sortable: true
                }, {
                    field: 'gmtCreated',
                    title: '创建时间',
                    width: 50,
                    sortable: true
                }, {
                    field: 'options',
                    title: '操作',
                    width: 100,
                    formatter: function (value, row, index) {
                        var icons = [{
                            "name": "edit",
                            "title": "编辑"
                        }, {
                            "name": "connect",
                            "title": "测试连接"
                        }, {
                            "name": "remove",
                            "title": "删除"
                        }];
                        var buttons = [];
                        for (var i = 0; i < icons.length; i++) {
                            var tmpl = '<a href="#" title ="${title}" '
                                + 'onclick="DsMVC.Controller.doOption(\'${index}\',\'${name}\')">'
                                + '<img src="${imgSrc}" alt="${title}"/"></a>';
                            var data = {
                                title: icons[i].title,
                                name: icons[i].name,
                                index: index,
                                imgSrc: DsCommon.baseIconUrl + icons[i].name + ".png"
                            };
                            buttons.push(juicer(tmpl, data));
                        }
                        return buttons.join(' ');
                    }
                }]],
                onDblClickRow: function (rowIndex, rowData) {
                    return DsMVC.Controller.edit();
                }
            });

            // dialogs
            $('#ds-dlg').dialog({
                closed: true,
                modal: false,
                width: 800,
                height: 600,
                iconCls: 'icon-add',
                buttons: [{
                    text: '测试连接',
                    iconCls: 'icon-connect',
                    handler: DsMVC.Controller.testConnection
                }, {
                    text: '关闭',
                    iconCls: 'icon-no',
                    handler: function () {
                        $("#ds-dlg").dialog('close');
                    }
                }, {
                    text: '保存',
                    iconCls: 'icon-save',
                    handler: DsMVC.Controller.save
                }]
            });

            $('#dbType').combobox({
                onChange: function (newValue, oldValue) {
                	if(newValue){
                		 var item = DsMVC.Model.dbTypes[newValue].value;
                         $('#jdbcUrl').textbox('setValue', item.jdbcUrl);
                         $('#driverClass').val(item.driverClass);
                         $('#queryerClass').val(item.queryerClass);
                	}
                	else {
                		$('#jdbcUrl').textbox('setValue', "");
                        $('#driverClass').val("");
                        $('#queryerClass').val("");
                	}
                }
            });

            $('#dbPoolType').combobox({
                onChange: function (newValue, oldValue) {
                	if(newValue){
                		var item = DsMVC.Model.dbPoolTypes[newValue].value;
                        $('#poolClass').val(item.poolClass);
                        var data = EasyUIUtils.toPropertygridRows(item.options);
                        $('#ds-options-pg').propertygrid('loadData', data);
                	}
                	else {
                        $('#poolClass').val("");
                        var data = EasyUIUtils.toPropertygridRows([]);
                        $('#ds-options-pg').propertygrid('loadData', data);
                	}
                }
            });

            $('#ds-options-pg').propertygrid({
                scrollbarSize: 0,
                width : 516,
                height: 260,
                columns: [[
                    {field: 'name', title: '配置项', width: 200, sortable: true},
                    {field: 'value', title: '配置值', width: 200, resizable: false}
                ]]
            });
        },
        bindEvent: function () {
            $('#btn-search').bind('click', DsMVC.Controller.find);
        },
        bindValidate: function () {
        }
    },
    Controller: {
        doOption: function (index, name) {
            $('#ds-datagrid').datagrid('selectRow', index);
            if (name == "edit") {
                return DsMVC.Controller.edit();
            }
            if (name == "remove") {
                return DsMVC.Controller.remove();
            }
            if (name == "connect") {
                return DsMVC.Controller.testConnectionById(index);
            }
        },
        add: function () {
            var options = DsMVC.Util.getOptions();
            options.title = '新增数据源';
            EasyUIUtils.openAddDlg(options);
            DsMVC.Util.fillCombox("#dbType", DsMVC.Model.dbTypes, "driverClass", null);
            DsMVC.Util.fillCombox("#dbPoolType", DsMVC.Model.dbPoolTypes, "poolClass", null);
        },
        edit: function () {
            var row = $('#ds-datagrid').datagrid('getSelected');
            if (row) {
            	row = DsMVC.Util.copy(row);//防止数据污染
            	//
                var options = DsMVC.Util.getOptions();
                options.iconCls = 'icon-edit1';
                options.data = row;
                options.title = '修改[' + options.data.name + ']数据源';
                EasyUIUtils.openEditDlg(options);
                DsMVC.Util.fillCombox("#dbType", DsMVC.Model.dbTypes, "driverClass", row.driverClass);
                DsMVC.Util.fillCombox("#dbPoolType", DsMVC.Model.dbPoolTypes, "poolClass", row.poolClass);
                $('#jdbcUrl').textbox('setValue', row.jdbcUrl);
                $('#options').val(row.options || "{}");
                EasyReport.utils.debug(row.options);
                var config = {
                	poolClass : row.poolClass,
                	options : $.toJSON(row.options)
                };
                DsMVC.Util.filterDsOptions(config);
                $('#ds-options-pg').propertygrid('loadData', EasyUIUtils.toPropertygridRows(config.options));
            } else {
                $.messager.alert('警告', '请选中一条记录!', 'info');
            }
        },
        find: function () {
            var fieldName = $("#field-name").combobox('getValue');
            var keyword = $("#keyword").val();
            var url = DsMVC.URLs.list.url + '?fieldName=' + fieldName + '&keyword=' + keyword;
            EasyUIUtils.loadToDatagrid('#ds-datagrid', url)
        },
        remove: function () {
            var row = $('#ds-datagrid').datagrid('getSelected');
            if (row) {
                var options = {
                    rows: [row],
                    url: DsMVC.URLs.remove.url,
                    data: {
                        id: row.id
                    },
                    gridId: '#ds-datagrid',
                    gridUrl: DsMVC.URLs.list.url,
                    callback: function (rows) {
                    }
                };
                EasyUIUtils.remove(options);
            }
        },
        testConnectionById: function (index) {
            $('#ds-datagrid').datagrid('selectRow', index);
            var row = $('#ds-datagrid').datagrid('getSelected');
            $.post(DsMVC.URLs.testConnectionById.url, {
                id: row.id
            }, function callback(data) {
                if (!data.code) {
                    $.messager.alert('成功', "测试成功", 'success');
                } else {
                    $.messager.alert('失败', "测试失败", 'error');
                }
            }, 'json');
        },
        testConnection: function () {
            var key = $("#dbType").combobox('getValue');
            var item = DsMVC.Model.dbTypes[key].value;
            var data = {
                driverClass: item.driverClass,
                url: $("#jdbcUrl").val(),
                pass: $("#password").val(),
                user: $("#user").val()
            };
            EasyReport.utils.debug(data);

            $.post(DsMVC.URLs.testConnection.url, data, function callback(data) {
                if (!data.code) {
                    $.messager.alert('成功', "测试成功", 'success');
                } else {
                    $.messager.alert('失败', "测试失败", 'error');
                }
            }, 'json');
        },
        save: function () {
            var rows = $('#ds-options-pg').propertygrid('getRows');
            $('#options').val(JSON.stringify(EasyUIUtils.toPropertygridMap(rows)));
            EasyReport.utils.debug($('#options').val());

            var action = $('#modal-action').val();
            var options = {
                gridId: null,
                gridUrl: DsMVC.URLs.list.url,
                dlgId: "#ds-dlg",
                formId: "#ds-form",
                url: null,
                callback: function () {
                }
            };

            options.url = (action === "edit" ? DsMVC.URLs.edit.url : DsMVC.URLs.add.url);
            options.gridId = '#ds-datagrid';
            return EasyUIUtils.save(options);
        }
    },
    Util: {
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
    	getOptions: function () {
            return {
                dlgId: '#ds-dlg',
                formId: '#ds-form',
                actId: '#modal-action',
                rowId: '#dsId',
                title: '',
                iconCls: 'icon-add',
                data: {},
                callback: function (arg) {
                },
                gridId: null,
            };
        },
        fillCombox: function (id, map, valPropName, value) {
            $(id).combobox('clear');
            var listData = [];
            var i = 0;
            var idxKey = null;
            for (var key in map) {
                var item = map[key];
                listData.push({
                    "value": item.key,
                    "name": item.name,
                    "selected": i == 0
                });
                if(idxKey == null && item.value[valPropName] == value){
                	idxKey = item.key;
                }
                //
                i++;
            }
            $(id).combobox('loadData', listData);
            if (idxKey != null) {
                $(id).combobox('setValue', idxKey);
            }
        },
        //补充默认validationQuery
        filterDsOptions: function(config){
        	var poolClass = config['poolClass'];
        	if(poolClass){
            	if(poolClass.indexOf('.DBCP2DataSourcePool') != -1 || poolClass.indexOf('.DruidDataSourcePool') != -1){
            		//这两个连接池支持设置validationQuery
            		var options = config['options'];
            		if(!options['validationQuery']){
            			options['validationQuery'] = 'SELECT 1 FROM dual';
            		}
            	}
            }
        },
        loadConfigItems: function () {
            $.getJSON(DsMVC.URLs.getConfItems.url + "?key=" + DsCommon.keys.dbType, function (result) {
                DsMVC.Util.toMap(DsMVC.Model.dbTypes, result.data);
            });
            $.getJSON(DsMVC.URLs.getConfItems.url + "?key=" + DsCommon.keys.dbPoolType, function (result) {
            	//console.log(result);
                DsMVC.Util.toMap(DsMVC.Model.dbPoolTypes, result.data);
                //补充默认validationQuery
                var dbPoolTypes = DsMVC.Model.dbPoolTypes || {};
                for(var key in dbPoolTypes){
                	var config = dbPoolTypes[key].value;
                	DsMVC.Util.filterDsOptions(config);
                }
            });
        },
        toMap: function (srcMap, data) {
            if (!data || data.length == 0) return {};
            for (var i = 0; i < data.length; i++) {
                var item = data[i];
                item.value = $.toJSON(item.value);
                srcMap[item.key] = item;
            }
            return srcMap;
        }
    }
};