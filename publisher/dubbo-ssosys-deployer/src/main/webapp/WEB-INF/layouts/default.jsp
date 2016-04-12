<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title><sitemesh:write property='title' /></title>
<meta
	content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"
	name="viewport">
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<link rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.css">
<link rel="stylesheet"
	href="${ctx}/static/Font-Awesome/css/font-awesome.min.css">
<link rel="stylesheet"
	href="${ctx}/static/ionicons/css/ionicons.min.css">
<link rel="stylesheet" href="${ctx}/static/AdminLTE/css/AdminLTE.css">
<link rel="stylesheet"
	href="${ctx}/static/AdminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet" href="${ctx}/static/plugins/iCheck/flat/blue.css">
<link rel="stylesheet" href="${ctx}/static/plugins/morris/morris.css">
<link rel="stylesheet"
	href="${ctx}/static/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
	href="${ctx}/static/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
	href="${ctx}/static/plugins/daterangepicker/daterangepicker-bs3.css">
<link rel="stylesheet"
	href="${ctx}/static/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">

<!--[if lt IE 9]>
        <script src="${ctx}/static/plugins//html5shiv.min.js"></script>
        <script src="${ctx}/static/plugins/respond.min.js"></script>
    <![endif]-->
</head>
<body class="hold-transition skin-blue sidebar-mini">
	<div class="wrapper">
		<sitemesh:write property='body' />
		<%@ include file="/WEB-INF/layouts/footer.jsp"%>
	</div>
	<script src="${ctx}/static/jquery/jquery-1.9.1.js"></script>
	<script src="${ctx}/static/plugins/jQueryUI/jquery-ui.min.js"></script>
	<script>
		$.widget.bridge('uibutton', $.ui.button);
	</script>
	<script src="${ctx}/static/bootstrap/js/bootstrap.min.js"></script>
	<script src="${ctx}/static/plugins/raphael-min.js"></script>
	<script src="${ctx}/static/plugins/morris/morris.min.js"></script>
	<script src="${ctx}/static/plugins/sparkline/jquery.sparkline.min.js"></script>
	<script
		src="${ctx}/static/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
	<script
		src="${ctx}/static/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
	<script src="${ctx}/static/plugins/knob/jquery.knob.js"></script>
	<script src="${ctx}/static/plugins/moment.min.js"></script>
	<script src="${ctx}/static/plugins/daterangepicker/daterangepicker.js"></script>
	<script src="${ctx}/static/plugins/datepicker/bootstrap-datepicker.js"></script>
	<script
		src="${ctx}/static/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
	<script src="${ctx}/static/plugins/slimScroll/jquery.slimscroll.min.js"></script>
	<script src="${ctx}/static/plugins/fastclick/fastclick.min.js"></script>
	<script src="${ctx}/static/AdminLTE/js/app.min.js"></script>
	<script src="${ctx}/static/AdminLTE/js/pages/dashboard.js"></script>
	<script src="${ctx}/static/AdminLTE/js/demo.js"></script>
	<!-- jQuery 2.1.4 -->
	<script src="${ctx}/static/plugins/jQuery/jQuery-2.1.4.min.js"></script>
	<!-- Select2 -->
	<script src="${ctx}/static/plugins/select2/select2.full.min.js"></script>
	<!-- InputMask -->
	<script src="${ctx}/static/plugins/input-mask/jquery.inputmask.js"></script>
	<script
		src="${ctx}/static/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
	<script
		src="${ctx}/static/plugins/input-mask/jquery.inputmask.extensions.js"></script>

	<!-- date-range-picker -->
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.2/moment.min.js"></script>
	<script src="${ctx}/static/plugins/daterangepicker/daterangepicker.js"></script>
	<!-- bootstrap color picker -->
	<script
		src="${ctx}/static/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
	<!-- bootstrap time picker -->
	<script
		src="${ctx}/static/plugins/timepicker/bootstrap-timepicker.min.js"></script>
	<!-- SlimScroll 1.3.0 -->
	<script src="${ctx}/static/plugins/slimScroll/jquery.slimscroll.min.js"></script>
	<!-- iCheck 1.0.1 -->
	<script src="${ctx}/static/plugins/iCheck/icheck.min.js"></script>
	<!-- FastClick -->
	<script src="${ctx}/static/plugins/fastclick/fastclick.min.js"></script>
	<!-- AdminLTE App -->
	<script src="${ctx}/static/js/app.min.js"></script>
	<!-- AdminLTE for demo purposes -->
	<script src="${ctx}/static/js/demo.js"></script>


	<!-- page script -->
	<script>
		$(function() {
			$("#datemask").inputmask("yyyy-mm-dd", {
				"placeholder" : "yyyy-mm-dd"
			});
			$("[data-mask]").inputmask();
		});
	</script>
</body>
</html>
