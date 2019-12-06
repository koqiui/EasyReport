var TableReport = {
    init: function () {
        TableReportMVC.View.initControl();
        TableReportMVC.View.bindEvent();
        TableReportMVC.View.bindValidate();
        //TableReportMVC.View.initData();//禁用立刻查询 by koqiui
    }
};

var TableReportCommon = {
    baseUrl: EasyReport.ctxPath + '/report'
};

var TableReportMVC = {
    URLs: {
        getData: {
            url: TableReportCommon.baseUrl + '/table/getData.json',
            method: 'POST'
        },
        exportExcel: {
            url: TableReportCommon.baseUrl + '/table/exportExcel',
            method: 'POST'
        }
    },
    View: {
        initControl: function () {
            $.parser.parse('#table-report-div');
        },
        bindEvent: function () {
            $('#btn-generate').click(TableReportMVC.Controller.generate);
            $('#btn-export-excel').click(TableReportMVC.Controller.exportToExcel);
            $("#table-report-columns input[name='checkAllStatColumn']").click(function (e) {
                var checked = $("#table-report-columns input[name='checkAllStatColumn']").prop("checked");
                $("#table-report-columns input[name='statColumns']").prop("checked", checked);
            });
        },
        bindValidate: function () {
        },
        initData: function () {
            TableReportMVC.Controller.generate(TableReportMVC.Model.Mode.classic, null);
        }
    },
    Model: {
        Mode: {
            classic: 'classic',// 经典表格模式
            datatables: 'dt'// datatables控件表格模式
        }
    },
    Controller: {
        generate: function (mode, callback) {
            $('#table-report-isRowSpan').val($('#table-report-isMergeRow').prop('checked'));
            $.ajax({
                type: "POST",
                url: TableReportMVC.URLs.getData.url,
                data: $("#table-report-form").serialize(),
                dataType: "json",
                beforeSend: function () {
                    $.messager.progress({
                        title: '请稍后...',
                        text: '报表正在生成中...',
                    });
                },
                success: function (result) {
                    if (!result.code) {
                        $('#table-report-htmltext-div').html(result.data.htmlTable);
                        TableReportMVC.Util.render(mode || TableReportMVC.Model.Mode.classic);
                        TableReportMVC.Util.filterTable = TableReportMVC.Util.renderFilterTable(result.data);
                        if (callback instanceof Function) {
                            callback();
                        }
                    } else {
                        $.messager.alert('操作提示', result.data.msg, 'error');
                    }
                },
                complete: function () {
                    $.messager.progress("close");
                }
            });
        },
        exportToExcel: function (e) {
        	var dataTable = $('#easyreport');
            var htmlFilter = TableReportMVC.Util.filterTable;
            if(htmlFilter == null || dataTable.length < 1){
            	$.messager.show({
                    title: '提示',
                    msg: '没有报表可以导出（请先生成报表）'
                });
            	return;
            }
            
            $.messager.progress({
                title: '请稍后...',
                text: '报表正在生成中...',
            });
            
            var htmlTable = dataTable.get(0).outerHTML;
            //表格加边框
            htmlTable = htmlTable.replace(/<table\s+/ig, '<table cellpadding="3" cellspacing="0"  border="1" rull="all" style="border-collapse:collapse" ');
            
            //从页面直接下载
            //console.log(htmlTable);
            var htmlContent = htmlFilter + htmlTable;
            var dateName = TableReportMVC.Util.getCurrentTime();
            dateName = dateName.replace(/\s/g, '_').replace(/\-/g, '').replace(/\:/g, '');
            var fileName = $('#table-report-name').val() + '_' + dateName + '.xls';
            //
            TableReportMVC.Util.downloadHtmlAsXls(htmlContent, fileName);
            
            setTimeout(function(){
            	$.messager.progress("close");
            }, 1000);//从页面直接下载
            
            //提交服务器下载
//            var htmlTableLarge = false;//htmlTable内容是否过大
//            var bytes = TableReportMVC.Util.getExcelBytes(htmlTable);
//            if (bytes > 2000000) {
//            	htmlTableLarge = true;
//            }
//            var data = $('#table-report-form').serializeObject();
//            data["htmlFilter"] = htmlFilter;
//            if(htmlTableLarge){
//            	data["htmlTable"] = "";
//            }
//            else {
//            	data["htmlTable"] = htmlTable;
//            }
            
//
//            var url = TableReportMVC.URLs.exportExcel.url;
//            data = $.param(data, true);
//            $.fileDownload(url, {
//                httpMethod: "POST",
//                data: data
//            }).done(function () {
//                $.messager.progress("close");
//            }).fail(function () {
//                $.messager.progress("close");
//            });
            e.preventDefault();
        }
    },
    Util: {
        // 表格中是否跨行
        hasRowSpan: function () {
            var rowspans = $("#easyreport>tbody>tr>td[rowspan]");
            return (rowspans && rowspans.length);
        },
        render: function (mode) {
            var table = $("#easyreport");
            return TableReportMVC.Util.renderClassicTable(table);

            /*if (mode == TableReportMVC.Model.Mode.classic) {
             return TableReportMVC.renderClassicTable(table);
             }
             // 如果为dt模式但是表格存在跨行
             // 则转为经典表格模式,因为datatables控件不支持跨行
             if (TableReportMVC.hasRowSpan()) {
             return TableReportMVC.renderClassicTable(table);
             }
             return TableReportMVC.renderDatatables(table);*/
        },
        renderClassicTable: function (table) {
            $("#easyreport>tbody>tr").dblclick(function () {
                $(this).toggleClass('selected');
            });
            $('#easyreport>tbody>tr').mouseover(function (e) {
                $(this).addClass('hover');
            });
            $('#easyreport>tbody>tr').mouseleave(function (e) {
                $(this).removeClass('hover');
            });

            var noRowSpan = !TableReportMVC.Util.hasRowSpan();
            //can't scroll to left
            //table.data('isSort', noRowSpan).fixScroll();

            //如果表格中没有跨行rowspan(暂不支持跨行)
            if (noRowSpan) {
                table.tablesorter({
                    sortInitialOrder: 'desc'
                });
                table.find('easyreport>thead>tr').attr({
                    title: "点击可以排序"
                }).css({
                    cursor: "pointer"
                });
            }
        },
        renderDatatables: function (table) {
            $('#easyreport').removeClass("easyreport");
            $('#easyreport').addClass('table table-striped table-bordered');
            var dt = $('#easyreport').dataTable({
                "scrollY": "758",
                "scrollX": true,
                "scrollCollapse": true,
                "searching": false,
                "pageLength": 100,
                "lengthMenu": [50, 100, 200, 500, 1000],
                "language": {
                    processing: "数据正在加载中...",
                    search: "查询:",
                    lengthMenu: "每页显示 _MENU_ 条记录",
                    info: "从 _START_ 到 _END_ /共 _TOTAL_ 条记录",
                    infoEmpty: "从 0 到  0  共 0  条记录",
                    infoFiltered: "(从 _MAX_ 条数据中检索)",
                    infoPostFix: "",
                    thousands: ",",
                    loadingRecords: "数据加载中...",
                    zeroRecords: "没有检索到数据",
                    emptyTable: "没有数据",
                    paginate: {
                        first: "首页",
                        previous: "前一页",
                        next: "后一页",
                        last: "尾页"
                    },
                    aria: {
                        sortAscending: ": 升序",
                        sortDescending: ": 降序"
                    }
                }
            });
            $('#easyreport tbody').on('dblclick', 'tr', function () {
            	$(this).toggleClass('selected');
            });
        },
        // 将报表上面的过滤信息拼成table，用于写入excel中
        renderFilterTable: function (result) {//table-report-title
            var html = '<table>';
            html += '<tr><td align="center" colspan="' + result.metaDataColumnCount + '"><h3>' + $('#table-report-name').val() + '</h3></td></tr>';
            html += '<tr><td align="right" colspan="' + result.metaDataColumnCount + '"><h3>导出时间: ' + TableReportMVC.Util.getCurrentTime() + '</h3></td></tr>';
            $('#table-report-form .j-item').each(function () {
                var type = $(this).attr('ctrl-type');
                var hidden = $(this).attr('ctrl-hidden') == 'true';
                if(!hidden){//不输出隐藏参数信息
                	if (type == "datebox") {
                        var label = $(this).find('label').text().replace(':', '');
                        var val = $(this).find("input[class*='textbox-value']").val();
                        html += '<tr><td><strong>' + label + '</strong></td><td align="left">' + (val || '') + '</td></tr>';
                    }
                    else if(type == "combobox"){
                    	var label = $(this).find('label').text().replace(':', '');
                    	var val = null;
                        var inputs = $(this).find("input[class*='textbox-value']");
                        if(inputs.size() >1){
                        	val = [];
                        	$(inputs).each(function(input){
                        		val.push(this.value);
                        	});
                        }
                        else {
                        	val = inputs.val();
                        }
                        //console.log(val);
                        //优先从下来列表获取（显示文本）
                        var optMap = {};
                        var selCtrl = $(this).find('select');
                        if(selCtrl.size() > 0){
                        	var optList = selCtrl.get(0).options;
                    		for(var i=0, c=optList.length; i<c; i++){
                    			var opt = optList[i];
                    			optMap[opt.value] = opt.text;
                    		}
                        }
                        if($.isArray(val)){
                        	var vals = val;
                        	var txts = [];
                        	for(var i=0, c=vals.length; i<c; i++){
                        		var valTmp = vals[i];
                        		txts.push(optMap[valTmp] || valTmp);
                        	}
                        	val = txts.join('、');
                        }
                        else {
                        	val = optMap[val] || val;
                        }
                        html += '<tr><td><strong>' + label + '</strong></td><td align="left">' + (val || '') + '</td></tr>';
                    }
                    else if(type === 'textbox') {
                    	var label = $(this).find('label').text().replace(':', '');
                    	var input = $(this).find('input[type="text"]');
                    	var val = input.val();
                    	html += '<tr><td><strong>' + label + '</strong></td><td align="left">' + (val || '') + '</td></tr>';
                    }
                    else if (type == 'checkboxlist') {
                        html += '<tr><td><strong>筛选统计列:</strong></td><td align="left" colspan="' + (result.metaDataColumnCount-1) + '">';
                        var rowChoose = [];
                        $(this).find('input[type="checkbox"]:checked').each(function () {
                        	var dataName = $(this).attr('data-name');
                        	if(dataName){
                        		rowChoose.push(dataName);
                        	}
                        })
                        html += rowChoose.join('、');
                        html += '</td></tr>';
                    }
                    else if(type === 'checkbox') {//combobox
                    	var label = $(this).find('label').text().replace(':', '');
                    	var input = $(this).find('input[type="checkbox"]');
                    	var val = input.prop("checked") ? '是' : '否';
                    	html += '<tr><td><strong>' + label + '</strong></td><td  align="left">' + (val || '') + '</td></tr>';
                    } else if (type == 'date-range') {
                        var input = $(this).find('.combo-text');
                        html += '<tr><td><strong>时间范围:</strong></td><td align="left" colspan="' + (result.metaDataColumnCount -1) + '">' + input.eq(0).val() + '~' + input.eq(1).val() + '</td></tr>';
                    }
                }
            })
            html += '<tr></tr></table>';
            return html;
        },
        getExcelBytes: function (str) {
            var totalLength = 0;
            var i;
            var charCode;
            for (i = 0; i < str.length; i++) {
                charCode = str.charCodeAt(i);
                if (charCode < 0x007f) {
                    totalLength = totalLength + 1;
                } else if ((0x0080 <= charCode) && (charCode <= 0x07ff)) {
                    totalLength += 2;
                } else if ((0x0800 <= charCode) && (charCode <= 0xffff)) {
                    totalLength += 3;
                }
            }
            return totalLength;
        },
        downloadHtmlAsXls : function(htmlContent, fileName, style) {
            htmlContent = htmlContent || '';
            if(htmlContent.indexOf('<html') != 0) {
                //没有被html包裹
                style = style || '';
                htmlContent = '<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><style type="text/css">' + style + '</style></head><body>' + htmlContent + '</body></html>';
            }
            var blob = new Blob([htmlContent], {
                type: "application/vnd.ms-excel"
            });

            if(window.navigator && window.navigator.msSaveBlob) { //IE
                window.navigator.msSaveBlob(blob, fileName);
            } else { //非IE
                var tmpLink = document.createElement('A');
                document.body.appendChild(tmpLink);
                tmpLink.style = 'display: none';
                var fileUrl = window.URL.createObjectURL(blob);
                tmpLink.href = fileUrl;
                tmpLink.download = fileName;
                tmpLink.click();
                window.URL.revokeObjectURL(tmpLink.href);
                document.body.removeChild(tmpLink);
                tmpLink.remove();
            }
        },
        getCurrentTime: function() {
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
            str += tmp + ' ';
            //
            tmp =  d.getHours() + '';
            if(tmp.length <2){
            	tmp = '0'+tmp;
            }
            str += tmp + ':';
            //
            tmp =  d.getMinutes() + '';
            if(tmp.length <2){
            	tmp = '0'+tmp;
            }
            str += tmp + ':';
            //
            tmp =  d.getSeconds() + '';
            if(tmp.length <2){
            	tmp = '0'+tmp;
            }
            str += tmp;
            return str;
        },
        filterTable: null
    }
};

//仅供调试链接列使用
function showReportDetail(dataMap, reportCode) {
    console.log("-" + reportCode + "-");
    console.log(dataMap);
}
