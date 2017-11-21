angular.module("SnowplowApp")
.controller("ToolbarCtrl", function($scope, $window, $location, modelService, shutdownAjaxService, updaterService){
	//$scope.menu = {isOpen:false, direction:"left"};
	$scope.modelService = modelService;
	$scope.location = $location;

	$scope.signout = function(){
		$window.location.href = 'signout'
	};
	$scope.shutdownServer = function(){
		shutdownAjaxService.shutdownServer().then(function(response){
			$window.location.href = "http://www.google.com";
		}, function(error){
			console.dir(error);
		});
	};
	$scope.setCommandMode = function(controlMode){
		modelService.controlMode=controlMode;
		if(controlMode == 0){ //Manual
					updaterService.setOperationMode({vehicleOperationMode:"COMMAND_QUEUE"});
				}else if (controlMode == 1){ //Joystick
					updaterService.setOperationMode({vehicleOperationMode:"PAUSED"});
				}else if (controlMode == 2){ //Autonomous
					updaterService.setOperationMode({vehicleOperationMode:"AUTONOMOUS"});
		}
	}
});