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
* Execute ```java src/Server 8080``` in the first tab.
* In the second tab, the general format is ```java src/Client <IP Address> <Port Number> <-Flag> <File> <Permission> [Key]```:
* Note: [] indications option arguments, <> are mandatory arugments.
    * ```java src/Client 127.0.0.1 8080 -u yourfile.txt (--public/--visible) ``` to upload a file (choosing the relevant permission).
    * ```java src/Client 127.0.0.1 8080 -u yourfile.txt --private yoursecretkey ``` to upload a file privately
    * ```java src/Client 127.0.0.1 8080 -d yourfile.txt``` to download a file.
    * ```java src/Client 127.0.0.1 8080 -d yourfile.txt yoursecretkey``` to download a private file.
    * ```java src/Client 127.0.0.1 8080 -l``` to query the server for stored files (public and visible).
    * Note you should be able to specify just the file name if the file is in the current directory, otherwise specify an **absolute** path.
