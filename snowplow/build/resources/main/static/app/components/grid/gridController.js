angular.module("SnowplowApp")
.controller("GridCtrl", function($scope, $location, $mdSidenav, $mdDialog, $mdToast, telemetryDataModelService, vehicleCommandDataModelService, zoneCellDataModelService, webSocketService, telemetryAjaxService){
	$scope.tdms = telemetryDataModelService;
	$scope.obstructionArr = []; //Dummy hookup for now
	$scope.vcdms = vehicleCommandDataModelService;
	$scope.zcdms =  zoneCellDataModelService;
	$scope.activity = ""; //[waypoint, stop, plowzone, plowedsnowzone, parkzone, addobstruction, removeobstruction, moveanchors]
	$scope.form = {headingCalibration:$scope.tdms.vehicleState.headingCalibration}; //Local form updates
	
	//If anchor positions arr is not initialized, call the telemetry service to fetch required data
	if(!telemetryDataModelService.anchorArr || telemetryDataModelService.anchorArr.length == 0){
		telemetryAjaxService.updateAnchorArr();
	}
	
	$scope.toggleSideNav = function(){
		$mdSidenav('grid-right').toggle();
	};
	
	
	/*****************************************************************************
	 * Command methods
	 *****************************************************************************/
	
	$scope.stopVehicle = function(){
		webSocketService.purgeVehicleCommands();
	};
	
	/*****************************************************************************
	 * Calibration methods
	 *****************************************************************************/
	
	$scope.updateHeadingCalibration = function(){
		telemetryAjaxService.updateHeadingCalibration($scope.form.headingCalibration);
	};
	
	$scope.autoCalibrateAnchors = function(){
		telemetryAjaxService.setAnchorsAutomatic().then(function(response){
			$mdToast.show($mdToast.simple().textContent('Anchors updating. Please wait...').hideDelay(3000));
		}, function(error){
			$mdToast.show($mdToast.simple().textContent('Anchor update failed').hideDelay(3000));
		});
	};
	
	$scope.launchCalibrationDialog = function(ev){
		$mdDialog.show({
			controller: "AnchorConfigCtrl",
			templateUrl: 'app/components/grid/anchorconfig/anchorconfig.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			clickOutsideToClose:true
		});
	};
	
	$scope.launchZoneCellPresetDialog = function(ev){
		$mdDialog.show({
			controller: "ZoneCellPresetCtrl",
			templateUrl: 'app/components/grid/zonecellpreset/zonecellpreset.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			clickOutsideToClose:true
		});
	};
	
	
	/*****************************************************************************
	 * Methods to issue commands to the server
	 *****************************************************************************/
	$scope.uploadTransaction = function(argArr){
		//console.log("Sending message: " + $scope.activity + " with payload: ");
		//console.dir(argArr);
		switch ($scope.activity){
			case 'waypoint': $scope.addWaypoint(argArr); break;
			case 'moveanchors': $scope.manualCalibrateAnchors(); break;
			case 'plowzone': webSocketService.sendZoneCellData(argArr); break;
			case 'plowedsnowzone': webSocketService.sendZoneCellData(argArr); break;
			case 'parkingzone': webSocketService.sendZoneCellData(argArr); break;
			case 'addobstruction': webSocketService.sendZoneCellData(argArr); break;
		}
	}
	
	
	/*****************************************************************************
	 * Upload anchor positions to server
	 *****************************************************************************/
	$scope.manualCalibrateAnchors = function(){
		telemetryAjaxService.setAnchorsManual(telemetryDataModelService.anchorArr).then(function(response){
			$mdToast.show($mdToast.simple().textContent('Anchors updating. Please wait...').hideDelay(3000));
		}, function(err){
			console.dir(err); 
			$mdToast.show($mdToast.simple().textContent('Anchor update failed').hideDelay(3000));
		});
	};
	
	/*****************************************************************************
	 * Uploads a waypoint command to the server
	 * argArr must contain the coordinates of the new waypoint
	 *****************************************************************************/
	$scope.addWaypoint = function(argArr){
		var vehicleCommand = {
			vehicleCommandType:'NAV_TO', //'MOVE_TO',
			args: argArr
		};
		webSocketService.issueVehicleCommand(vehicleCommand);
	};
	
	/*****************************************************************************
	 * Clear zones
	 *****************************************************************************/
	$scope.clearAllZones = function(){
		zoneCellDataModelService.zoneCellArr.forEach(function(zoneCell){
			zoneCell.snowVolume = 0;
			zoneCell.obstruction = false;
			zoneCell.plowZone = false;
			zoneCell.plowedSnowZone = false;
			zoneCell.parkZone = false;
		});
	};
	
});