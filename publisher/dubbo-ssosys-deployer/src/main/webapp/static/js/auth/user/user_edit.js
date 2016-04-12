;
(function($, RAY) {

  // 页面初始化
  $(function() {

    $("#saveBtn").click(function() {
      var flag = true;
      $(".label,.label-danger").empty();

      var email = $("#email").val();
      if (email == null || email == "" || email == undefined) {
        flag = false;
        $("#email").parent().after($("<span class='label label-danger'>*请填写邮箱地址</span>"));
      }

      var userName = $("#userName").val();
      if (userName == null || userName == "" || userName == undefined) {
        flag = false;
        $("#userName").parent().after($("<span class='label label-danger'>*请填写用户名</span>"));
      }

      if (flag) {
        $.ajax({
          type: "POST",
          url: _ctx + "/user/edit",
          data: $('#userCommand').serialize(),// 序列化的form
          async: false,
          error: function(data) {
            showMessageDailog("警告", '用户编辑失败');
          },
          success: function(data) {
            if (data['status'] == 1) {
              alert(data['msg']);
              window.location.href = _ctx + "/user/list";
            } else {
              alert(data['msg']);
            }
          }
        });
      }
    });

  });

})(jQuery, window.RAY || (window.RAY = {}));