Rest TX Bridge: Example Using Rest TX Bridge with JPA and JMS
======================================================
Author: Gytis Trikleris (gytist@gmail.com)
Technologies: Rest TX Bridge, JPA, JMS

What is it?
-----------
This example demonstrates the use of Restfull Transaction Bridge with services using JPA and JMS.

Quickstart consists of three modules: JPA, JMS and combined. First two are completely independent and can be executed separately. Combined example reuses first two applications. Therefore, in order to make it work JPA and JMS modules have to be installed first (use: mvn install). 

 * JPA module allows creating users and tasks through Restfull API.

 * JMS module allows sending messages to the queue and receiving messages from it.

 * Combined module only contains Arquillian tests which use both JPA and JMS services. 

Build and Deploy the Quickstart
-------------------------

In order to execute quickstart please follow these steps:

1. Download and deploy rest-tx coordinator (https://github.com/jbosstm/narayana/tree/master/rest-tx).

2. Make sure that "org.jboss.narayana.rts:restat-util:5.0.0.M2-SNAPSHOT" artifact is in your local Maven repository (comes from 1 step).

3. Enter correct jbossHome values in Arquillian configuration files

  * combined/src/test/resources/arquillian.xml
		
  * jms/src/test/resources/arquillian.xml
		
  * jpa/src/test/resources/arquillian.xml
		
4. Go to the root directory of the quickstart and execute following command.

        mvn clean install

5. Arquillian test will be executed and output will be printed to the console.