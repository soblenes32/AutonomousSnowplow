angular.module("CTTApp")
.controller("ToolbarCtrl", function($scope, $window, $mdMedia, $mdSidenav, modelService){
	$scope.mdMedia = $mdMedia;
	$scope.modelService = modelService;
	$scope.signout = function(){
		$window.location.href = 'signout'
	};
	
	$scope.openMenu = function($mdOpenMenu, ev) {
		$mdOpenMenu(ev);
	};
	
	$scope.toggleNav = function(){
		modelService.isLeftNavOpenMobile = !modelService.isLeftNavOpenMobile;
	};
});