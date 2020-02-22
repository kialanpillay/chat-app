# Networks Assignment I - CSC3002F
* Kialan Pillay (PLLKIA010)
* Insaaf Dhansay (DHNINS001)
* Ziyyaad Anthony (MUHANT001)

## Run
* Navigate to the project source code directory. ```cd ~/file-sharing-app/src```
* ```javac src/Connection.java && javac src/Server.java && javac src/Protocol.java && javac src/Client.java```
(Will use the Makefile once it is properly configured)
* Open two seperate terminal tabs.
* ``cd ..``
* Execute ```java src/Server``` in the first tab.
* In the second tab, the general format is ```java src/Client [IP Address] [Port Number] [-Flag] [File]```:
    * ```java src/Client 127.0.0.1 8080 -u yourfile.txt``` to upload a file.
    * ```java src/Client 127.0.0.1 8080 -u yourfile.txt``` to download a file.
    * ```java src/Client 127.0.0.1 8080 -l``` to query the server for stored files.
    * Note you should be able to specify just the file name if the file is in the current directory, otherwise specify an **absolute** path.