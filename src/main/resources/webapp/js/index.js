$(document).ready(function() {
    layui.use(['laypage', 'layer','element'], function(){
        var element = layui.element();
        var layer = layui.layer;
        renderHosts();
        bindEvent(layer);
    });
    renderConfig();
    renderDescHost();
    renderAlerm();
});
function renderDescHost() {
    $.ajax({
        url: env_address,
        type: 'GET',
        data: {
            action: 'queryList',
            page: 1,
            rows: 9999,
        },
        success: function (json) {
            var rows = json.rows;
            for(var index in rows){
                $('#ipport').append('<option value="'+rows[index].ip+':'+rows[index].port+'">'+rows[index].ip+':'+rows[index].port+'</option>');
                $('#ipport_chart').append('<option value="'+rows[index].ip+':'+rows[index].port+'">'+rows[index].ip+':'+rows[index].port+'</option>');
            }
        }
    });
}
function bindEvent(layer) {
    $('#addHost').unbind('click').bind('click', function () {
        layer.open({
            title: '添加监控主机',
            content: getHostPopule(),
            btn: ['确定', '取消'],
            btn1: function (index, lay) {
                var host = {};
                $('#addHostDiv').find('input').each(function (index, ele) {
                    if (ele.name == 'ip') {
                        host.ip = ele.value;
                    }
                    if (ele.name == 'port') {
                        host.port = ele.value;
                    }
                    if (ele.name == 'passwd') {
                        host.passwd = ele.value;
                    }
                });
                addHost(host, layer);
            },
            btn2: function (index, lay) {
            }
        });
    });
    $('#addConfig').unbind('click').bind('click', function () {
        layer.open({
            title: '修改配置项',
            content: getConfigPopule(),
            btn: ['确定', '删除','取消'],
            btn1: function (index, lay) {
                var config = {};
                $('#addConfigDiv').find('input').each(function (index, ele) {
                    if (ele.name == 'key') {
                        config.key = ele.value;
                    }
                    if (ele.name == 'value') {
                        config.value = ele.value;
                    }
                });
                addConfig(config, layer);
            },
            btn2: function (index, lay) {
                var config = {};
                $('#addConfigDiv').find('input').each(function (index, ele) {
                    if (ele.name == 'key') {
                        config.key = ele.value;
                    }
                    if (ele.name == 'value') {
                        config.value = ele.value;
                    }
                });
                removeConfig(config, layer);
            },
            btn3: function (index, lay) {
            }
        });
    });
    $('#slowlogBtn').unbind('click').bind('click', function () {
        var host = {};
        var val = $('#ipport').val();
        host.ip = val.split(":")[0];
        host.port = val.split(":")[1];
        host.page = 1;
        host.rows = 99999;
        host.action = 'querySlowLog';
        var renderslow = function (rows) {
            var begin = getSlowLogTableBegin();
            for (var index in rows) {
                begin += '<tr><td>' + rows[index].executionTime + '</td><td>' + rows[index].datetime + '</td><td>' + rows[index].args + '</td></tr>';
            }
            begin += getTableEnd();
            $('#descTable').empty();
            $('#descTable').append(begin);
        };
        queryForCallBack(host, renderslow);
    });
    $('#clientListBtn').unbind('click').bind('click', function () {
        var host = {};
        var val = $('#ipport').val();
        host.ip = val.split(":")[0];
        host.port = val.split(":")[1];
        host.page = 1;
        host.rows = 99999;
        host.action = 'queryClientList';
        var renderClientList = function (rows) {
            var begin = getClientListTableBegin();
            for (var index in rows) {
                begin += '<tr><td>' + rows[index].addr + '</td><td>' + rows[index].age + '</td><td>' + rows[index].idle + '</td>'
                    +'<td>' + rows[index].db + '</td><td>' + rows[index].cmd + '</td><td>'+rows[index].flag+'</td></tr>';
            }
            begin += getTableEnd();
            $('#descTable').empty();
            $('#descTable').append(begin);
        };
        queryForCallBack(host, renderClientList);
    })
    $('#cmdListBtn').unbind('click').bind('click', function () {
        var host = {};
        var val = $('#ipport').val();
        host.ip = val.split(":")[0];
        host.port = val.split(":")[1];
        host.metric = 'cmdstat'
        host.action = 'queryMetrics';
        var renderCmd = function (rows) {
            var begin = getCmdListTableBegin();
            for (var index in rows) {
                begin += '<tr><td>' + rows[index].cmdName + '</td><td>' + rows[index].calls + '</td><td>' + rows[index].usec + '</td>'
                    +'<td>' + rows[index].usecPer + '</td></tr>';
            }
            begin += getTableEnd();
            $('#descTable').empty();
            $('#descTable').append(begin);
        };
        queryForCmd(host, renderCmd);
    });
    $('#dbBtn').unbind('click').bind('click', function () {
        var host = {};
        var val = $('#ipport').val();
        host.ip = val.split(":")[0];
        host.port = val.split(":")[1];
        host.metric = 'db'
        host.action = 'queryMetrics';
        var renderCmd = function (rows) {
            var begin = getDBListTableBegin();
            for (var index in rows) {
                begin += '<tr><td>' + index + '</td><td>' + rows[index].expires + '</td><td>' + rows[index].avgTtl + '</td>'
                    +'<td>' + rows[index].keys + '</td></tr>';
            }
            begin += getTableEnd();
            $('#descTable').empty();
            $('#descTable').append(begin);
        };
        queryForCmd(host, renderCmd);
    });
    $('#chartBtn').unbind('click').bind('click',function () {
        queryMetrics();
    });
}
function renderOs() {
    var host = {};
    var val = $('#ipport_chart').val();
    host.ip = val.split(":")[0];
    host.port = val.split(":")[1];
    host.action = 'queryMetrics';
    host.metric = 'server';
    $.ajax({
        url: env_address,
        type:'GET',
        data:host,
        success: function(json) {
            var data = json.data;
            var os = $("#os");
            os.empty();
            os.append('<tr>')
            for(var index in data){
                os.append('<td>'+data[index]+'</td>')
            }
            os.append('</tr>');
        },
        cache: false
    });
}
function renderConfig() {
    $.ajax({
        url: env_address,
        type:'GET',
        data:{
            action:'queryConfigList',
        },
        asyn:false,
        success: function(json) {
            var data = json.data;
            $("#config").empty();
            for(var index in data){
                $("#config").append('<tr>')
                    .append('<td>'+index+'</td>')
                    .append('<td>'+JSON.stringify(data[index])+'</td>')
                    .append('</tr>');
            }
        },
        cache: false
    });
}
function renderAlerm() {
    $.ajax({
        url: env_address,
        type:'GET',
        data:{
            action:'queryAlermList',
            page:1,
            rows:1000
        },
        asyn:false,
        success: function(json) {
            var rows = json.rows;
            $("#alerm").empty();
            if(undefined != rows){
                for(var index in rows){
                    $("#alerm").append('<tr>')
                        .append('<td><input type="text" group="'+rows[index].name+'" name="name" value="'+rows[index].name+'" </input></td>')
                        .append('<td><input type="text" group="'+rows[index].name+'" name="maxValue" value="'+rows[index].maxValue+'"</input></td>')
                        .append('<td><input type="text" group="'+rows[index].name+'" name="isNull" value="'+rows[index].isNull+'"</input></td>')
                        .append('<td><input type="text" group="'+rows[index].name+'" name="isUse" value="'+rows[index].isUse+'"</input></td>')
                        .append('<td><button class="layui-btn" group="'+rows[index].name+'" name="saveAlermBtn">保存</button></td>')
                        .append('</tr>');
                }
                $('button[name="saveAlermBtn"]').unbind('click').bind('click',function () {
                     var inputs = $(this).parent().parent().find('input[group="'+$(this).attr('group')+'"]');
                     var item = {};
                     for(var index in inputs){
                         if("name" == inputs[index].name){
                             item.name = inputs[index].value;
                         }
                         if("maxValue" == inputs[index].name){
                             item.maxValue = inputs[index].value;
                         }
                         if("isNull" == inputs[index].name){
                             item.isNull =  inputs[index].value;
                         }
                         if("isUse" == inputs[index].name){
                             item.isUse = inputs[index].value;
                         }
                     }
                    if(item.name.length <= 0){
                         alert('metric不能为空')
                     }
                    if(isNaN(item.maxValue)){
                        alert('最大值只能为数字')
                    }
                    if(item.isNull == 'true'){
                        item.isNull = true;
                    }else{
                        item.isNull = false;
                    }
                    if(item.isUse == 'true'){
                        item.isUse = true;
                    }else{
                        item.isUse = false;
                    }
                    item.maxValue = parseInt(item.maxValue);
                    var push = [];
                    push.push(item);
                    $.ajax({
                        url: env_address + '?action=addAlermList&json='+JSON.stringify(push),
                        type:'GET',
                        data:item,
                        success: function(point) {
                            alert('修改预警配置成功');
                            renderAlerm();
                        },
                        cache: false
                    });
                });
            }
        },
        cache: false
    })
}
function renderHosts() {
    $.ajax({
        url: env_address,
        type:'GET',
        data:{
            action:'queryList',
            page:1,
            rows:99999,
        },
        asyn:false,
        success: function(json) {
            var rows = json.rows;
            if(rows == undefined || rows.length == 0){
                console.info(undefined);
                return;
            }
            $("#host").empty();
            for(var index in rows){
                $("#host").append('<tr>')
                    .append('<td>'+rows[index].ip+'</td>')
                    .append('<td>'+rows[index].port+'</td>')
                    .append('<td>'+rows[index].passwd+'</td>')
                    .append('<td><button class="layui-btn" name="removeHost"' +
                        ' ip="'+rows[index].ip+'"' +
                        ' port="'+rows[index].port+'"' +
                        ' passwd="'+rows[index].passwd+'">删除主机</button></td>')
                    .append('</tr>');
            }
            $('button[name="removeHost"]').unbind('click').bind('click',function () {
                var host = {};
                host.ip = $(this).attr('ip');
                host.port = $(this).attr('port');
                removeHost(host,layer);
            });
        },
        cache: false
    });
}
function queryMetrics() {
    var host = {};
    var val = $('#ipport_chart').val();
    host.ip = val.split(":")[0];
    host.port = val.split(":")[1];
    host.action = 'queryMetricList';
    $.ajax({
        url: env_address,
        type:'GET',
        data:host,
        asyn:false,
        success: function(json) {
            renderOs();
            for(var i=0;i<json.rows.length;i++){
                var metric = json.rows[i];
                render(chartdata[metric],host);
            }
        },
        cache: false
    });
}
function render(metric,host) {
    if(undefined == metric){
        return;
    }
    var param = {
        url:env_address,
        method:'GET',
        action:'queryMetrics',
        ip:host.ip,
        port:host.port,
        metric:metric.name,
        ele:'chart',
        title:metric.title,
        ytitle:metric.ytile,
        xtitle:metric.xtile
    };
    renderChart(param);
}