function register_handler() {
    $("#classAddBtn").on("click", function(e) {
        $.ajax({
            url: '/srcs/add_class',
            data: {url: window.location.pathname},
            type: 'POST',
            xhrFields: {
              responseType: 'blob'
            },
            contentType: "application/x-www-form-urlencoded; charset:UTF-8",
            success: function(data, message, xhr) {
                window.location.reload(true);
            }
        });
    });
    $("#classDelBtn").on("click", function(e) {
        var name = $(this).text();
        var classNo = $(this).data("class_no");

        if(!confirm("삭제합니까?.")) {
            return;
        }

        $("#delete_class").submit();
    });

    $('.value_change').keyup(function(e) {
        var input = $(this);
        if(e.key == 'Enter') {
            var value = input.val();
            $.ajax({
                url: '/srcs/value_change',
                data: {variable_id : input.data("variable_id"), value},
                type: 'POST',
                xhrFields: {
                  responseType: 'blob'
                },
                contentType: "application/x-www-form-urlencoded; charset:UTF-8",
                success: function(data, message, xhr) {
                    window.location.reload(true);
                },
                error: function (xhr, status, error) {
                    alert("value 설정에 실패하였습니다.");
                }
            });
        }
    });

    $("#varAddBtn").on("click", function(e) {
        var static_check = $("#static_check").prop('checked');
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
                line = line.trim();
                line = line.replace(/\t/g, " ");
                var index = line.lastIndexOf(" ");
                var type = line.substring(0, index);
                var name = line.substring(index);
                var varItem = {
                    isStatic : static_check,
                    classId : area.data('class_id'),
                    type : type,
                    name : name,
                    value : value,
                    path : window.location.pathname,
                }
                dataArray.push(varItem);
            }
        }

        $.ajax({
            url: '/srcs/add_var',
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

    $('.varDelBtn').on("click", function(e) {
            var variable_id = $(this).data('variable_id');
            var class_id = $(this).data('class_id');
            $.ajax({
                url: '/srcs/del_var',
                data: {
                    variable_id : variable_id,
                    class_id : class_id,
                },
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
}

$(document).ready(function(e) {
    register_handler();
});