angular.module("SnowplowApp")
.service("updaterService", function($rootScope, $interval, modelService, telemetryDataModelService, vehicleCommandDataModelService, zoneCellDataModelService, updaterAjaxService){
	var self = this;
	var vehicleOperationMode = 0;
	
	/*********************************************
	 ************** Fetch methods ****************
	 *********************************************/
	
	this.fetchUpdate = function(){
		updaterAjaxService.fetchUpdate().then(function(response){
			var p = response.data;
			
			//ZoneCell update
			if(p.zoneCellList && p.zoneCellList.length > 0){
				zoneCellDataModelService.updateZoneCells(p.zoneCellList);
			}
			//Vehicle command list update
			if(p.vehicleCommandList && p.vehicleCommandList.length > 0){
				vehicleCommandDataModelService.commandArr = p.vehicleCommandList;
			}
			//VehicleState update
			if(p.vehicleState){
				telemetryDataModelService.updateVehicleState(p.vehicleState);
			}
			//Anchor update
			if(p.anchorStateList && p.anchorStateList.length > 0){
				telemetryDataModelService.anchorArr = p.anchorStateList;
			}
			//Lidar detection list update
			if(p.lidarPointList && p.lidarPointList.length > 0){
				telemetryDataModelService.updateLidarDetections(p.lidarPointList);
			}
			
			//Allow the operating mode to be updated before it is interacted with by the user
			if(p.vehicleState && p.vehicleState.vehicleOperationMode){
				switch(p.vehicleState.vehicleOperationMode){
					case 'COMMAND_QUEUE': modelService.controlMode = 0; break;
					case 'PAUSED': modelService.controlMode = 1; break;
					case 'AUTONOMOUS': modelService.controlMode = 2; break;
				}
			}
			
		}, function(err){
			console.dir(err);
		})
	}
	
	
	/*********************************************
	 ************** Send methods *****************
	 *********************************************/
	
	/*********************************************************************************
	 * Joystick methods
	 *********************************************************************************/
	this.sendJoystickData = function(d){
		updaterAjaxService.sendJoystickData(d);
	}
	
	/*********************************************************************************
	 * Vehicle Command methods
	 *********************************************************************************/
	this.issueVehicleCommand = function(vehicleCommand){
		updaterAjaxService.issueVehicleCommand(vehicleCommand);
	}
	
	this.rescendVehicleCommand = function(vehicleCommand){
		updaterAjaxService.rescendVehicleCommand(vehicleCommand);
	}
	
	this.purgeVehicleCommands = function(){
		updaterAjaxService.purgeVehicleCommands();
	}
	
	this.setOperationMode = function(mode){
		updaterAjaxService.setOperationMode(mode);
	}
	
	/*********************************************************************************
	 * Zonecell update methods
	 *********************************************************************************/
	this.sendZoneCellData = function(d){
		//console.log("sending zonecell data: ");
		//console.dir(d);
		updaterAjaxService.sendZoneCellData(d).then(function(response){
			zoneCellDataModelService.updateZoneCells(response.data);
		}, function(err){
			console.log("Failure to transmit zonecell data");
			console.dir(err);
		});
	}
	
	/*********************************************
	 ********** Initial zonecell fetch ************
	 *********************************************/
	this.sendZoneCellData([]);

	/*********************************************
	 ************** Fetch Execution ****************
	 *********************************************/
	
	$interval(self.fetchUpdate, 400);
}); 