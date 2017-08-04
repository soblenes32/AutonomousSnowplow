angular.module("SnowplowApp")
.controller("JoystickCtrl", function($scope, $mdSidenav, modelService, webSocketService){
	$scope.modelService = modelService;
	$scope.activateJoystick = false;
	$scope.d = {x:0,y:0}; //Current joystick position
	$scope.$watch('modelService.controlMode', function() {
		if(modelService.controlMode == 1){
			//Show joystick
			$mdSidenav('left').open().then(function(){
				$scope.activateJoystick = true;
			}); 
		}else{
			$scope.activateJoystick = false;
			$mdSidenav('left').close(); //Hide joystick
		}
	});
	$scope.$watch('d', function(){
		//console.log("sending :" + $scope.d.x + ", " + $scope.d.y);
		try{
			webSocketService.sendJoystickData($scope.d);
		}catch(e){}
	}, true);
});