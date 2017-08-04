angular.module("SnowplowApp")
.service("telemetryDataModelService", function(){
	var self = this;
	
	/***************************************************************************************************
	 * Handle to the last polled vehicle state object
	 * {"motorATarget":0, "motorBTarget":0, "motorAValue":0, "motorBValue":0, "position":{"x":0,"y":0}}
	 ***************************************************************************************************/
	this.vehicleState = null;
	
	/**********************************************************************************
	 * Array of historical vehicle state objects. Most recent state is element 0
	 * If array exceeds maxLength, the oldest element is removed
	 **********************************************************************************/
	this.vehicleStateArr = [];
	this.vehicleStateArrMaxLength = 10;
	
	/**********************************************************************************
	 * Array of recent lidar detections
	 **********************************************************************************/
	this.lidarDetectionArr = [];
	this.lidarDetectionArrMaxLength = 250;
	
	/**********************************************************************************
	 * Array of anchor objects [{"name":"0x681c", "position":{"x":0,"y":0}}, ... ]
	 **********************************************************************************/
	this.anchorArr = [];
	
	this.updateVehicleState = function(newVehicleState){
		self.vehicleState = newVehicleState;
		self.vehicleStateArr.unshift(newVehicleState);
		if(self.vehicleStateArr.length > self.vehicleStateArrMaxLength){
			self.vehicleStateArr = self.vehicleStateArr.splice(-1); 
		}
	}
	
	this.updateLidarDetections = function(newLidarDetections){ //max=100; length is 300 (300 - 100 = 200)
		self.lidarDetectionArr = newLidarDetections.concat(self.lidarDetectionArr);
		if(self.lidarDetectionArr.length > self.lidarDetectionArrMaxLength){
			self.lidarDetectionArr = self.lidarDetectionArr.splice(0, self.lidarDetectionArrMaxLength);
		}
	}
}); 