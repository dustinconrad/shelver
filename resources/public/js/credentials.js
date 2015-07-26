function loadCredentialsWidget(widget) {
    if (widget) {
        switch(widget) {
            case "signin":
                $("#loginbox").show();
                $("#signupbox").hide();
                break;
            case "signup":
                $("#loginbox").hide();
                $("#signupbox").show();
                break;
            default:
                break;
        }
    }
}

var loadFragment = window.location.hash.substring(1);
if (loadFragment) {
    loadCredentialsWidget(loadFragment);
}

$(window).on('hashchange', function() {
    var changeFragment = window.location.hash.substring(1);
    loadCredentialsWidget(changeFragment);
});