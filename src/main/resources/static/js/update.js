function set_multi_values() {
    var area = $("#varAddArea");
    var content = area.val();
    var tokens = content.split("\n");
    var dataArray = []
    var url = ""

    for (var i = 0; i < tokens.length; i++) {
      var line = tokens[i].trim();
      if (line.startsWith("url:")) {
        send_update_query(url, dataArray)
        url = line.replace("url:", "").trim()
        dataArray = []
        continue
      }
      var value = "";
      value = line.trim()
      dataArray.push(value);
    }
    send_update_query(url, dataArray)
}

function set_values() {
    var multi_check = $("#multi_check").prop('checked')
    var area = $("#varAddArea");
    var content = area.val();
    var tokens = content.split("\n");

    if (multi_check) {
        set_multi_values()
        return
    }

    var dataArray = []
    for (var i = 0; i < tokens.length; i++) {
        var line = tokens[i];
        var value = "";
        value = line.trim()
        dataArray.push(value);
    }

    var url = $("#targetPath").val()
    send_update_query(url, dataArray)
}

function send_update_query(url, dataArray) {
  if (url.trim() === "") {
    return
  }
  var item = {
      value: dataArray,
      static_check: $("#static_check").prop('checked'),
      start: $("#start").val(),
      url: url,
  }

  $.ajax({
      url: window.location.pathname,
      data: JSON.stringify(item),
      type: 'POST',
      contentType: 'application/json',
      success: function(data, message, xhr) {
          window.location.reload(true);
          localStorage.setItem('targetPath', url);
      },
      error: function (xhr, status, error) {
      }
  });
}

function register_update() {
    $("#varAddBtn").on("click", function(e) {
        set_values()
    });
    $("#instanceAddBtn").on("click", function(e) {
        var url = $("#url").val()
        var start = $("#start").val()
        var end = $("#end").val()
        if (url === "" || start === "" || end === "") {
            alert("all value must set")
            return
        }
        $.ajax({
            url: '/make_all',
            data: {
                url : url,
                start : start,
                end : end,
            },
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

}
$(document).ready(function(e) {
    register_update();
    var storedUrl = localStorage.getItem('targetPath');
    if (storedUrl) {
        $("#targetPath").val(storedUrl);
        localStorage.removeItem('targetPath');
    }

});