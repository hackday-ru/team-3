$("[data-toggle='sidebar']").on("click", function(){
	if($('.sidebar').css('display') == "none") {
		$('.sidebar').show();
		$('.content').removeClass('fullWidth');
	}
	else {
		$('.sidebar').hide();
		$('.content').addClass('fullWidth');
	}
});