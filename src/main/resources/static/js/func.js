function register_func_handler() {
    $('.this_change').keyup(function(e) {
        var input = $(this);
        if(e.key == 'Enter') {
            var url = input.val();
            if(!url.includes("/srcs/")) {
                alert("잘못된 주소입니다. /srcs/가 포함되어야 합니다.");
                return;
            }

            $.ajax({
                url: '/func/this_change',
                data: {funcId : input.data("func_id"), url : url},
                type: 'POST',
                xhrFields: {
                  responseType: 'blob'
                },
                contentType: "application/x-www-form-urlencoded; charset:UTF-8",
                success: function(data, message, xhr) {
                    window.location.reload(true);
                }
            });
        }
    });

    $("#funcAdd").keyup(function(e) {
        var input = $(this);

        if(e.key == 'Enter') {
            $.ajax({
                url: '/func/add_func',
                data: {name : input.val(), path : window.location.pathname},
                type: 'POST',
                xhrFields: {
                  responseType: 'blob'
                },
                contentType: "application/x-www-form-urlencoded; charset:UTF-8",
                success: function(data, message, xhr) {
                    window.location.reload(true);
                }
            });
        }
    });

    $('.collapsible').on("click", function (e) {
        var lastFuncId = $("#last_hidden").val();
        var name = $(this).text();
        var funcId = $(this).data("func_id");

        if(!confirm(name + " 항목을 삭제합니다.")) {
            return;
        }

        if(lastFuncId != funcId) {
            alert("마지막 함수만 삭제할 수 있습니다.");
            return;
        }

        $.ajax({
            url: '/func/del_func',
            data: {funcId : funcId},
            type: 'DELETE',
            xhrFields: {
              responseType: 'blob'
            },
            contentType: "application/x-www-form-urlencoded; charset:UTF-8",
            success: function(data, message, xhr) {
                window.location.reload(true);
            },
            error: function (xhr, status, error) {
                if(xhr.status == 400) {
                    alert("마지막 함수 호출만 삭제 가능합니다.");
                } else if (xhr.status == 409) {
                    alert("변수가 참조를 갖고 있습니다.");
                } else {
                    alert("알수 없는 이유로 함수 삭제에 실패하였습니다.");
                }

            }
        });
    });
}

function register_var_handler() {
    $('.var_change').keyup(function(e) {
        var input = $(this);
        if(e.key == 'Enter') {
            var value = input.val();
            $.ajax({
                url: '/func/var_change',
                data: {variableId : input.data("variable_id"), value: value},
                type: 'POST',
                xhrFields: {
                  responseType: 'blob'
                },
                contentType: "application/x-www-form-urlencoded; charset:UTF-8",
                success: function(data, message, xhr) {
                    window.location.reload(true);
                },
                error: function (xhr, status, error) {
                    alert("해당 값을 설정할 수 없습니다. instance가 생성되었는지 확인하세요");
                }
            });
        }
    });
    $('.varDelBtn').on("click", function(e) {
        var lastFuncId = $("#last_hidden").val();
        var funcId = $(this).data('func_id');
        if(lastFuncId != funcId) {
            alert("마지막 함수의 지역 변수만 삭제할 수 있습니다.");
            return;
        }

        var variableId = $(this).data('variable_id');
        $.ajax({
            url: '/func/del_var',
            data: {variableId : variableId},
            type: 'DELETE',
            contentType: "application/x-www-form-urlencoded; charset:UTF-8",
            success: function(data, message, xhr) {
                window.location.reload(true);
            },
            error: function (xhr, status, error) {
                alert("삭제에 실패하였습니다.");
            }
        });
    });
    $("#varAddBtn").on("click", function(e) {
        var last_hidden = $("#last_hidden");
        var area = $("#varAddArea");
        var content = area.val();
        var tokens = content.split("\n");
        var dataArray = [];

        for (var i = 0; i < tokens.length; i++) {
            var line = tokens[i];
            var value = "";
            if(line.includes("=")) {
                var index = line.lastIndexOf("=");
                value = line.substring(index + 1);
                line = line.substring(0, index).trim();
            }
            if(!(line === "")) {
                line = line.replace(/\t/g, " ");
                var index = line.lastIndexOf(" ");
                var type = line.substring(0, index);
                var name = line.substring(index);
                var varItem = {
                    funcId : last_hidden.val(),
                    type : type,
                    name : name,
                    value : value,
                    path : window.location.pathname,
                }
                dataArray.push(varItem);
            }
        }

        $.ajax({
            url: '/func/add_var',
            data: JSON.stringify(dataArray),
            type: 'POST',
            contentType: 'application/json',
            success: function(data, message, xhr) {
                window.location.reload(true);
            },
            error: function (xhr, status, error) {
            }
        });
    });
}

$(document).ready(function(e) {
    document.title = window.location.pathname;
    register_func_handler();
    register_var_handler();
});