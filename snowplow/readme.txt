Date: 2017/07/27 
Author: Samuel O'Blenes 

Notes:
This project requires RXTX

1) Install RXTX on the pi. sudo apt-get install librxtx-java
2) Include the RXTX jar in your project. http://www.jcontrol.org/download/rxtx_en.html
3) Include the RXTX native binaries in your properties dir on the pi. 
sudo cp /usr/lib/jni/librxtxSerial.so /usr/lib
sudo cp /usr/lib/jni/librxtxParallel.so /usr/lib
Properties locations
/usr/java/packages/lib/arm:/lib:/usr/lib











Changes
9/25 - Added anchor preset file so that the application has an initial anchor configuration - ArduinoRxTxUsbService.java (70)
9/26 - Added angle preset so that the vehicle is calibrated to an initial predetermined heading - ArduinoRxTxUsbService.java (75)
9/26 - Added zonecell preset so that the work area is automatically initialized

Next: add an environment initialization parameter to allow the program to run without sensors




