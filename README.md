Swim Interview Exercise
=======================
Test notes:
* copy the repository
* run mvn clean install in main directory
* open multiple terminal windows
* run 'java -jar target/server-1.0-SNAPSHOT.jar 8000' in /server directory
* run 'java -jar target/client-1.0-SNAPSHOT.jar localhost 8000' in /client directory multiple times
* run '{"command":"subscribe"}' on Client3
* run '{"command":"get"}' on Client3
    * check if value returned is 0
* run '{"command":"set","value":1}' on Client1
* run '{"command":"set","value":2}' on Client2
* run '{"command":"set","value":3}' on Client1
* run '{"command":"set","value":4}' on Client2
* run '{"command":"set","value":5}' on Client1
* run '{"command":"set","value":6}' on Client2
    * check if output on Client3 terminal window prints all 6 updates.
* run '{"commd":"set","value":1}' on Client1
    * check if output informs about incorrect syntax
* run '{"command":"pet","value":1}' on Client1
    * check if output informs about unknown command
* run '{"command":"set"}' on Client1
    * check if output informs about missing value
* run '{"command":"set","value":999999999999999999999999999}' on Client2
    * check if output informs about incorrect syntax / max value