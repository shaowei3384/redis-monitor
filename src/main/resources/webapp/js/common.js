var env_address = 'http://redis.monitor.mhealth365.cn/record';
function rspChange(rsp){
    var data  = rsp.data;
    var ret = {
        x:[],
        y:[]
    };
    if(data != undefined){
        for(var key in data){
            ret.x.push(key);
            var value = data[key];
            value = parseFloat(value)
            ret.y.push(value);
        }
    }
    return ret;
}

function createLine(x,y,renderEle,title,ytitle,xtitle) {
    var chart = new Highcharts.Chart({
        chart: {
            renderTo: renderEle,
            events: {
            }
        },
        title: {
            text: title
        },
        xAxis: {
            categories:x
        },
        yAxis: {
            title: {
                text: ytitle,
                margin: 80
            }
        },
        series: [{
            name: xtitle,
            data: y
        }]
    });
    return chart;
}

function renderChart(param) {
    $('#' + param.ele).append('<div id="' + param.metric + '"></div>');
    $.ajax({
        url: param.url,
        type:param.method,
        data:{
            action:param.action,
            ip:param.ip,
            port:param.port,
            metric:param.metric
        },
        success: function(point) {
            var ret = rspChange(point);
            createLine(ret.x,ret.y,param.metric,param.title,param.ytitle,param.xtitle);
        },
        cache: false
    });
}

function addHost(host,layer) {
    //ajax请求后台添加
    host.action='addHost'
    $.ajax({
        url: env_address,
        type:'GET',
        data:host,
        success: function(point) {
            layer.msg('添加主机成功');
            renderHosts();
        },
        cache: false
    });
}
function addConfig(config,layer) {
    //ajax请求后台添加
    config.action='addConfig'
    $.ajax({
        url: env_address,
        type:'GET',
        data:config,
        success: function(point) {
            layer.msg('添加配置成功');
            renderConfig();
        },
        cache: false
    });
}
function removeConfig(config,layer) {
    //ajax请求后台添加
    config.action='removeConfig'
    $.ajax({
        url: env_address,
        type:'GET',
        data:config,
        success: function(point) {
            layer.msg('删除配置成功');
            renderConfig();
        },
        cache: false
    });
}
function removeHost(host,layer) {
    host.action='removeHost'
    $.ajax({
        url: env_address,
        type:'GET',
        data:host,
        success: function(point) {
            layer.msg('删除主机成功');
            renderHosts();
        },
        cache: false
    });
}
function queryForCallBack(host,callback) {
    $.ajax({
        url: env_address,
        type:'GET',
        data:host,
        success: function(point) {
            callback(point.rows);
        },
        cache: false
    });
}
function queryForCmd(host,callback) {
    $.ajax({
        url: env_address,
        type:'GET',
        data:host,
        success: function(point) {
            callback(point.data);
        },
        cache: false
    });
}
function getSlowLogTableBegin() {
    var begin = '<table class="layui-table"> <colgroup> <col width="150"> <col width="200"> <col> </colgroup>'
        +'<thead> <tr> <th>执行耗时(s)</th><th>时间</th><th>命令</th></tr></thead>'
        +'<tbody>';
    return begin;
}
function getClientListTableBegin() {
    var begin = '<table class="layui-table"> <colgroup> <col width="150"> <col width="200"> <col> </colgroup>'
        +'<thead> <tr> <th>客户端ip端口</th><th>连接时长(s)</th><th>空闲时长(s)</th><th>DB</th><th>最新命令</th><th>flag</th></tr></thead>'
        +'<tbody>';
    return begin;
}
function getCmdListTableBegin() {
    var begin = '<table class="layui-table"> <colgroup> <col width="150"> <col width="200"> <col> </colgroup>'
        +'<thead> <tr> <th>命令</th><th>调用次数</th><th>总耗时</th><th>平均耗时</th></tr></thead>'
        +'<tbody>';
    return begin;
}
function getDBListTableBegin() {
    var begin = '<table class="layui-table"> <colgroup> <col width="150"> <col width="200"> <col> </colgroup>'
        +'<thead> <tr> <th>DB</th><th>过期（秒）</th><th>平均TTL</th><th>Key数</th></tr></thead>'
        +'<tbody>';
    return begin;
}
function getTableEnd() {
    return '</tbody></table>';
}
function getHostPopule(){
    return '<div id="addHostDiv">'
        +'<div class="layui-form-item">'
        +'<label class="layui-form-label">IP</label>'
        +'<div class="layui-input-block">'
        +'<input type="text" name="ip" required  lay-verify="required" placeholder="请输入IP" autocomplete="off" class="layui-input">'
        +'</div></div>'
        +'<div class="layui-form-item">'
        +'<label class="layui-form-label">端口</label>'
        +'<div class="layui-input-block">'
        +'<input type="text" name="port" required  lay-verify="required" placeholder="请输入端口" autocomplete="off" class="layui-input">'
        +'</div></div>'
        +'<div class="layui-form-item">'
        +'<label class="layui-form-label">密码</label>'
        +'<div class="layui-input-block">'
        +'<input type="text" name="passwd" required  lay-verify="required" placeholder="请输入密码" autocomplete="off" class="layui-input">'
        +'</div></div></div>';
}
function getConfigPopule(){
    return '<div id="addConfigDiv">'
        +'<div class="layui-form-item">'
        +'<label class="layui-form-label">KEY</label>'
        +'<div class="layui-input-block">'
        +'<input type="text" name="key" required  lay-verify="required" placeholder="请输入KEY" autocomplete="off" class="layui-input">'
        +'</div></div>'
        +'<div class="layui-form-item">'
        +'<label class="layui-form-label">VALUE</label>'
        +'<div class="layui-input-block">'
        +'<input type="text" name="value" required  lay-verify="required" placeholder="请输入VALUE" autocomplete="off" class="layui-input">'
        +'</div></div>'
        +'</div>';
}
var chartdata = {
    cache_hit_rate:{
        name:'cache_hit_rate',
        title:'cache_hit_rate',
        xtile:'时间',
        ytile:'cache_hit_rate'
    },qps:{
        name:'qps',
        title:'qps',
        xtile:'时间',
        ytile:'qps'
    },connected_clients:{
        name:'connected_clients',
        title:'connected_clients',
        xtile:'时间',
        ytile:'connected_clients'
    },blocked_clients:{
        name:'blocked_clients',
        title:'blocked_clients',
        xtile:'时间',
        ytile:'blocked_clients'
    },used_memory_human:{
        name:'used_memory_human',
        title:'used_memory_human',
        xtile:'时间',
        ytile:'used_memory_human'
    },used_memory_peak_human:{
        name:'used_memory_peak_human',
        title:'used_memory_peak_human',
        xtile:'时间',
        ytile:'used_memory_peak_human'
    },mem_fragmentation_ratio:{
        name:'mem_fragmentation_ratio',
        title:'mem_fragmentation_ratio',
        xtile:'时间',
        ytile:'mem_fragmentation_ratio'
    },rejected_connections:{
        name:'rejected_connections',
        title:'rejected_connections',
        xtile:'时间',
        ytile:'rejected_connections'
    },evicted_keys:{
        name:'evicted_keys',
        title:'evicted_keys',
        xtile:'时间',
        ytile:'evicted_keys'
    }
}