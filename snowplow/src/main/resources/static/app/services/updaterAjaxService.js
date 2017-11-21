angular.module("SnowplowApp")
.service("updaterAjaxService", function($rootScope, $http, telemetryDataModelService, vehicleCommandDataModelService, zoneCellDataModelService){
	var self = this;
	
	this.fetchUpdate = function(){
		return $http({
			method: 'POST',
			url: '/clientupdate/fetch'
		});
	};
	
	/*********************************************************************************
	 * Joystick methods
	 *********************************************************************************/
	this.sendJoystickData = function(d){
		return $http({
			method: 'POST',
			url: '/joystick',
			data: d
		});
	};
	
	
	/*********************************************************************************
	 * Vehicle Command methods
	 *********************************************************************************/
	this.issueVehicleCommand = function(vehicleCommand){
		return $http({
			method: 'POST',
			url: '/vehiclecommand/issue',
			data: vehicleCommand
		});
	};
	
	this.rescendVehicleCommand = function(vehicleCommand){
		return $http({
			method: 'POST',
			url: '/vehiclecommand/rescend',
			data: vehicleCommand
		});
	};
	
	this.purgeVehicleCommands = function(){
		return $http({
			method: 'POST',
			url: '/vehiclecommand/purge'
		});
	};
	
	this.setOperationMode = function(mode){
		return $http({
			method: 'POST',
			url: '/vehiclecommand/mode',
			data: mode
		});
	};

	/*********************************************************************************
	 * Operational mode
	 *********************************************************************************/
	this.setOperationMode = function(d){
		return $http({
			method: 'POST',
			url: '/vehiclecommand/mode',
			data: d
		});
	};
}); 