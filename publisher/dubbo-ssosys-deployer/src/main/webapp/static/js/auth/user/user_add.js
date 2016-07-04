;
(function($, RAY) {

  // 页面初始化
  $(function() {

    $("#saveBtn").click(function() {
      var flag = true;
      $(".label,.label-danger").empty();

      var loginName = $("#loginName").val();
      if (loginName == null || loginName == "" || loginName == undefined) {
        flag = false;
        $("#loginName").parent().after($("<span class='label label-danger'>*请填写登录账号</span>"));
      }

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

      var password = $("#password").val();
      if (password == null || password == "" || password == undefined) {
        flag = false;
        $("#password").parent().after($("<span class='label label-danger'>*请填写密码</span>"));
      }

      var confirmPassword = $("#confirmPassword").val();
      if (confirmPassword == null || confirmPassword == "" || confirmPassword == undefined) {
        flag = false;
        $("#confirmPassword").parent().after($("<span class='label label-danger'>*请填写确认密码</span>"));
      }

      if (password != confirmPassword) {
        flag = false;
        $("#confirmPassword").parent().after($("<span class='label label-danger'>*两次密码不一致</span>"));
      }

      if (flag) {
        $.ajax({
          type: "POST",
          url: _ctx + "/user/add",
          data: $('#userCommand').serialize(),// 序列化的form
          async: false,
          error: function(data) {
            showMessageDailog("警告", '用户新增失败');
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