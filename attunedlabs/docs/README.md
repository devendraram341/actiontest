#Description
• The main purpose of this Hello-World Feature is to test on the kubernets cluster.

Steps to be Followed For Development Side:
==========================================
1)Create a hello world feature with the latest beta version of framework.

2)Once the feature is complete we will push the source code into the github.

Steps to be Followed For Infra Side :
=====================================

1)We have to create a docker configuration file for that feature

->pull the 3.6.3-openjdk-11-slim image i am thinking that image will be good for installing the java and maven version which has all the things which required with minimal RAM for us, or is their any other image we are having.

->set the JAVA and MAVEN HOME directory in system variables  by using .bashrc file

->replace the existing settings.xml file from maven home config location with settings.xml file provided by us.

->Go to the directory of leap attunedlabs/services directory to run the maven-build.sh or .bat file based on OS.

-> we have to start the hsql db, which is present in the directory path of attunedlabs/config/<profile>/runHsqlserver.bat or .sh file based on the OS.

->once the HSQL is started successfully then we have to go the directory of attunedlabs/services/featureInstaller and have to run the maven command to start the application.

-> If the application is started we have to allow to port outside of that network to test the API's.

-> If everything well fine then we have to push this image into Azure Private Registery of attunedlabs.

->After pushing the image to Azure then we have to work on the kubernets pod cluster setup
