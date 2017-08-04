angular.module("SnowplowApp")
.controller("ToolbarCtrl", function($scope, $window, $location, modelService, shutdownAjaxService, webSocketService){
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
			webSocketService.setOperationMode("COMMAND_QUEUE");
		}else if (controlMode == 1){ //Joystick
			webSocketService.setOperationMode("PAUSED");
		}else if (controlMode == 2){ //Autonomous
			webSocketService.setOperationMode("AUTONOMOUS");
		}
	}
});