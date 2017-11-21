angular.module("SnowplowApp")
.controller("DiagnosticCtrl", function($scope, $location, $timeout, telemetryDataModelService, vehicleCommandDataModelService, telemetryAjaxService, updaterService){
	$scope.wss = telemetryDataModelService;
	$scope.vcdms = vehicleCommandDataModelService;
	$scope.updateAnchors = function(){
		console.log("Update anchors");
	};
	
	//If anchor positions arr is not initialized, call the telemetry service to fetch required data
	if(!telemetryDataModelService.anchorArr || telemetryDataModelService.anchorArr.length == 0){
		$timeout(function(){
			telemetryAjaxService.updateAnchorArr();
		}, 1000);
	}
	
	$scope.rescendCommand = function(command){
		if(!command.args){command.args = [];}
		updaterService.rescendVehicleCommand(command);
	}
});