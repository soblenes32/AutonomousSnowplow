angular.module("SnowplowApp")
.service("telemetryAjaxService", function($http){
	var self = this;
	
	this.updateVehicleState = function(){
		return $http({
			method: 'GET',
			url: '/telemetry/update/vehicle'
		})
	};
	
	this.setVehicleStatePollRate = function(rateMs){
		return $http({
			method: 'GET',
			url: '/telemetry/poll/' + rateMs
		})
	};
	
	this.updateAnchorArr = function(){
		return $http({
			method: 'GET',
			url: '/telemetry/update/anchors'
		})
	};
	
	this.updateHeadingCalibration = function(headingCalibration){
		return $http({
			method: 'GET',
			url: '/telemetry/heading/calibrate/' + headingCalibration
		})
	};
	
	this.setAnchorsAutomatic = function(){
		return $http({
			method: 'POST',
			url: '/telemetry/update/anchors/automatic'
		})
	};
	
	this.setAnchorsManual = function(anchorArr){
		return $http({
			method: 'POST',
			url: '/telemetry/update/anchors/manual',
			data: anchorArr
		})
	};
}); 