REST-AT Bridge: Example of Using REST-AT Bridge with JPA and JMS
======================================================
Author: Gytis Trikleris (gytist@gmail.com)
Technologies: REST-AT Bridge, JPA, JMS

What is it?
-----------
This example demonstrates the use of REST-AT Bridge with services using JPA and JMS.

Quickstart consists of three modules: JPA, JMS and combined. First two are completely independent and can be executed separately. Combined example reuses first two applications. Therefore, in order to make it work JPA and JMS modules have to be installed first (use: mvn install). 

 * JPA module allows creating users and tasks through Restfull API.

 * JMS module allows sending messages to the queue and receiving messages from it.

 * Combined module only contains Arquillian tests which use both JPA and JMS services. 

Build and Deploy the Quickstart
-------------------------

In order to execute quickstart please follow these steps:

1. Download and deploy rest-tx coordinator to your JBoss AS instance (https://github.com/jbosstm/narayana/tree/master/rest-tx).

2. Deploy the REST-AT Bridge as explained in https://github.com/Gytis/rest-tx-bridge.

3. Enter correct jbossHome values in Arquillian configuration files
  * combined/src/test/resources/arquillian.xml
  * jms/src/test/resources/arquillian.xml
  * jpa/src/test/resources/arquillian.xml
		
4. Go to the root directory of the quickstart and execute following command.

        mvn clean install

5. Arquillian test will be executed and output will be printed to the console.