angular.module("CTTApp",["ngRoute","ngMaterial","ngMessages","ui.calendar","ngMaterialDatePicker", 'mdSteppers'])
.run(function($rootScope) {
	//Everything that needs to happen on routechange
})
.config(['$routeProvider', '$locationProvider', '$mdThemingProvider', '$compileProvider', function($routeProvider, $locationProvider, $mdThemingProvider, $compileProvider){
	$locationProvider.html5Mode(true);
	$compileProvider.preAssignBindingsEnabled(true);
	$routeProvider
	.when("/newuser", {
		templateUrl: "app/components/newuser/newUser.html",
        controller: 'NewUserCtrl',
        controllerAs: 'newuserctrl'
	})
	.when("/profile", {
		templateUrl: "app/components/profile/profile.html",
        controller: 'ProfileCtrl',
        controllerAs: 'profilectrl'
	})
	.when("/newrotation", {
		templateUrl: "app/components/newrotation/newrotation.html",
        controller: 'NewRotationCtrl',
        controllerAs: 'newRotationCtrl'
	})
	.when("/findrotation", {
		templateUrl: "app/components/findrotation/findrotation.html",
        controller: 'FindRotationCtrl',
        controllerAs: 'findRotationCtrl'
	})
	.when("/roleapprovals", {
		templateUrl: "app/components/roleapprovals/approvals.html",
        controller: 'ApprovalsCtrl',
        controllerAs: 'approvalsctrl'
	})
	.when("/instaffiliations", {
		templateUrl: "app/components/instaffiliations/instaffiliations.html",
        controller: 'InstAffiliationsCtrl',
        controllerAs: 'instaffiliationsctrl'
	})
	.when("/rotationsummary", {
		templateUrl: "app/components/rotationsummary/rotation.html",
        controller: 'RotationSummaryCtrl',
        controllerAs: 'rotationsummarycontrol'
	})
	.otherwise({
		templateUrl: "app/components/home/home.html",
		controller: 'HomeCtrl',
        controllerAs: 'home'
	});
	
	$mdThemingProvider.definePalette('healthPartnersBrandPalette', {
		'50': '009B48',  //Logo green
		'100': '522398', //Logo purple
		'200': '3D7EDB', //Logo blue
		'300': 'D9C89E', //Master tan
		'400': 'E1CD00', //Master yellow
		'500': '2C5697', //Master dark blue
		'600': 'BE4D00', //Master dark orange
		'700': 'B5BD00', //Master lime green
		'800': '84BD00', //Master barf green
		'900': '2DCCD3', //Master light blue
		'A100': '009CDE',//Master medium blue
		'A200': 'F2A900',//Master orange
		'A400': '9595D2',//Master light purple
		'A700': 'F64B7F',//Master pink
		'A800': '833177',//Master dark purple
		'contrastDefaultColor': 'light',    // whether, by default, text (contrast) on this palette should be dark or light
		'contrastDarkColors': ['50', '100', '200', '300', '400', 'A100'], //hues which contrast should be 'dark' by default
		'contrastLightColors': undefined    // could also specify this if default was 'dark'
	});
	
	$mdThemingProvider.theme('default')
	    .primaryPalette('healthPartnersBrandPalette' //);
	    ,{'default': '500', //dark blue
	       'hue-1': '300', //Tan
	       'hue-2': '800', //barf green
	       'hue-3': 'A100' //medium blue
	    })
        .backgroundPalette('grey', {
            'default': '200'
            //'hue-1':'300'
         });
}])
/* Main Navigation */
.controller("primaryCtrl", function($scope, $route, modelService){
	$scope.modelService = modelService;
	$scope.footer = {url: "app/components/footer/footer.html"};
	$scope.toolBar = {url: "app/components/toolbar/toolbar.html"};
});