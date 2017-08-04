angular.module("CTTApp")
.controller("SidenavCtrl", function($scope, $mdMedia, $location, modelService, userPermissionService){
	$scope.modelService = modelService;
	$scope.mdMedia = $mdMedia;
	$scope.ups = userPermissionService;
	$scope.location = $location;
});