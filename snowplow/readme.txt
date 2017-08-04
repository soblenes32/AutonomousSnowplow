Date: 2016/04/27 
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







Things to fix:
Add filter to motion input; if moves more than 0.5m since last frame, throw out the frame; cannot throw out two in a row
Preset anchor locations missing a 0
Upon page load, the zone cells should be initialized from the server