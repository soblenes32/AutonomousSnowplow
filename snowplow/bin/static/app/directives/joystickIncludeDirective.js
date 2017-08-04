angular.module("SnowplowApp")
.directive("joystickInclude", function() {
    return {
        templateUrl: 'app/components/joystick/joystick.html',
        controller: 'JoystickCtrl'
    };
});