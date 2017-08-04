angular.module("SnowplowApp")
.directive("joystick", function($interval) {
    return {
    	scope: {
        	d: "=d",
        	isActive:"=isActive"
        },
        link: function(scope, element, attributes) {
        	var joystick = null;
        	var positionReporter = null;
        	scope.$watch('isActive', function(){
        		if(!scope.isActive){
        			if(positionReporter){
        				$interval.cancel(positionReporter);
        				positionReporter = null;
        			}
        			return;
        		}
				joystick = new VirtualJoystick({
					container : element[0],
					mouseSupport : true,
					limitStickTravel: true,
					stickRadius	: 100
				});
				scope.d = scope.d||{};
				positionReporter = $interval(function(){
					scope.d.x=joystick.deltaX();
					scope.d.y=joystick.deltaY();
				}, 100);
        	});
        }
    };
});