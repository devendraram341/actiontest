-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: oaccdb
-- ------------------------------------------------------
-- Server version	5.7.17-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `oac_domain`
--

LOCK TABLES `oac_domain` WRITE;
/*!40000 ALTER TABLE `oac_domain` DISABLE KEYS */;
INSERT INTO `oac_domain` VALUES (0,'SYSDOMAIN',NULL),(2,'All',NULL),(3,'leap-dev',2),(4,'all-dev',2),(5,'RoiAuthTest',2);
/*!40000 ALTER TABLE `oac_domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_domcrperm_postcr_sys`
--

LOCK TABLES `oac_grant_domcrperm_postcr_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_domcrperm_postcr_sys` DISABLE KEYS */;
INSERT INTO `oac_grant_domcrperm_postcr_sys` VALUES (0,-303,1,1,0),(0,-302,1,1,0),(0,-301,1,1,0);
/*!40000 ALTER TABLE `oac_grant_domcrperm_postcr_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_domcrperm_sys`
--

LOCK TABLES `oac_grant_domcrperm_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_domcrperm_sys` DISABLE KEYS */;
INSERT INTO `oac_grant_domcrperm_sys` VALUES (0,-300,1,0);
/*!40000 ALTER TABLE `oac_grant_domcrperm_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_domperm_sys`
--

LOCK TABLES `oac_grant_domperm_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_domperm_sys` DISABLE KEYS */;
INSERT INTO `oac_grant_domperm_sys` VALUES (0,0,-301,1,0),(0,2,-303,1,0),(0,2,-302,1,0),(0,2,-301,1,0),(0,3,-303,1,0),(0,3,-302,1,0),(0,3,-301,1,0),(0,4,-303,1,0),(0,4,-302,1,0),(0,4,-301,1,0),(0,5,-303,1,0),(0,5,-302,1,0),(0,5,-301,1,0);
/*!40000 ALTER TABLE `oac_grant_domperm_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_global_resperm`
--

LOCK TABLES `oac_grant_global_resperm` WRITE;
/*!40000 ALTER TABLE `oac_grant_global_resperm` DISABLE KEYS */;
/*!40000 ALTER TABLE `oac_grant_global_resperm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_global_resperm_sys`
--

LOCK TABLES `oac_grant_global_resperm_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_global_resperm_sys` DISABLE KEYS */;
/*!40000 ALTER TABLE `oac_grant_global_resperm_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_rescrperm_postcr`
--

LOCK TABLES `oac_grant_rescrperm_postcr` WRITE;
/*!40000 ALTER TABLE `oac_grant_rescrperm_postcr` DISABLE KEYS */;
/*!40000 ALTER TABLE `oac_grant_rescrperm_postcr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_rescrperm_postcr_sys`
--

LOCK TABLES `oac_grant_rescrperm_postcr_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_rescrperm_postcr_sys` DISABLE KEYS */;
/*!40000 ALTER TABLE `oac_grant_rescrperm_postcr_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_rescrperm_sys`
--

LOCK TABLES `oac_grant_rescrperm_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_rescrperm_sys` DISABLE KEYS */;
/*!40000 ALTER TABLE `oac_grant_rescrperm_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_resperm`
--

LOCK TABLES `oac_grant_resperm` WRITE;
/*!40000 ALTER TABLE `oac_grant_resperm` DISABLE KEYS */;
INSERT INTO `oac_grant_resperm` VALUES (1,5,2,4,0,0),(1,5,2,5,0,0),(1,5,2,6,0,0),(1,10,2,4,0,0),(1,10,2,5,0,0),(1,10,2,6,0,0),(1,12,2,4,0,0),(1,12,2,5,0,0),(1,12,2,6,0,0),(1,13,2,4,0,0),(1,13,2,5,0,0),(1,13,2,6,0,0),(1,14,3,7,0,0),(1,14,3,8,0,0),(1,14,3,9,0,0),(1,18,3,7,0,0),(1,18,3,8,0,0),(1,18,3,9,0,0),(1,19,3,7,0,0),(1,19,3,8,0,0),(1,19,3,9,0,0),(1,20,3,7,0,0),(1,20,3,8,0,0),(1,20,3,9,0,0),(1,27,2,4,0,0),(1,27,2,5,0,0),(1,27,2,6,0,0),(1,28,2,4,0,0),(1,28,2,5,0,0),(1,28,2,6,0,0),(2,4,2,4,0,0),(2,4,2,5,0,0),(2,4,2,6,0,0),(2,5,2,4,0,0),(2,5,2,5,0,0),(2,5,2,6,0,0),(2,9,2,4,0,0),(2,9,2,5,0,0),(2,9,2,6,0,0),(2,10,2,4,0,0),(2,10,2,5,0,0),(2,10,2,6,0,0),(2,12,2,4,0,0),(2,12,2,5,0,0),(2,12,2,6,0,0),(2,13,2,4,0,0),(2,13,2,5,0,0),(2,13,2,6,0,0),(2,14,3,7,0,0),(2,14,3,8,0,0),(2,14,3,9,0,0),(2,15,3,7,0,0),(2,15,3,8,0,0),(2,15,3,9,0,0),(2,16,3,7,0,0),(2,16,3,8,0,0),(2,16,3,9,0,0),(2,17,3,7,0,0),(2,17,3,8,0,0),(2,17,3,9,0,0),(2,22,2,4,0,0),(2,22,2,5,0,0),(2,22,2,6,0,0),(2,23,2,4,0,0),(2,23,2,5,0,0),(2,23,2,6,0,0),(2,26,2,4,0,0),(2,26,2,5,0,0),(2,26,2,6,0,0),(2,29,2,4,0,0),(2,29,2,5,0,0),(2,29,2,6,0,0),(2,30,2,4,0,0),(2,30,2,5,0,0),(2,30,2,6,0,0),(2,31,2,4,0,0),(2,31,2,5,0,0),(2,31,2,6,0,0);
/*!40000 ALTER TABLE `oac_grant_resperm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_grant_resperm_sys`
--

LOCK TABLES `oac_grant_resperm_sys` WRITE;
/*!40000 ALTER TABLE `oac_grant_resperm_sys` DISABLE KEYS */;
/*!40000 ALTER TABLE `oac_grant_resperm_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_resource`
--

LOCK TABLES `oac_resource` WRITE;
/*!40000 ALTER TABLE `oac_resource` DISABLE KEYS */;
INSERT INTO `oac_resource` VALUES (0,0,0),(1,1,4),(2,1,3),(3,2,2),(4,2,2),(5,2,2),(6,2,2),(7,2,2),(8,2,2),(9,2,2),(10,2,2),(11,2,2),(12,2,2),(13,2,2),(14,3,2),(15,3,2),(16,3,2),(17,3,2),(18,3,2),(19,3,2),(20,3,2),(21,3,2),(22,2,2),(23,2,2),(24,2,2),(25,2,2),(26,2,2),(27,2,2),(28,2,2),(29,2,2),(30,2,2),(31,2,2);
/*!40000 ALTER TABLE `oac_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_resourceclass`
--

LOCK TABLES `oac_resourceclass` WRITE;
/*!40000 ALTER TABLE `oac_resourceclass` DISABLE KEYS */;
INSERT INTO `oac_resourceclass` VALUES (0,'SYSOBJECT',1,0),(1,'Users',0,0),(2,'FormFlow',0,0),(3,'Item',0,0);
/*!40000 ALTER TABLE `oac_resourceclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_resourceclasspermission`
--

LOCK TABLES `oac_resourceclasspermission` WRITE;
/*!40000 ALTER TABLE `oac_resourceclasspermission` DISABLE KEYS */;
INSERT INTO `oac_resourceclasspermission` VALUES (1,3,'Delete'),(1,2,'Edit'),(1,1,'View'),(2,6,'Delete'),(2,5,'Edit'),(2,4,'View'),(3,9,'Delete'),(3,8,'Edit'),(3,7,'View');
/*!40000 ALTER TABLE `oac_resourceclasspermission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_resourceexternalid`
--

LOCK TABLES `oac_resourceexternalid` WRITE;
/*!40000 ALTER TABLE `oac_resourceexternalid` DISABLE KEYS */;
INSERT INTO `oac_resourceexternalid` VALUES (22,'createinventory_flow.xsl'),(29,'createinventory_form.xsl'),(12,'FunctionKeyLabels.xsl'),(14,'Home'),(21,'Inbound'),(15,'Inventory'),(23,'inventorydetails_flow.xsl'),(31,'inventorydetails_form.xsl'),(26,'inventoryfilter_flow.xsl'),(30,'inventoryfilter_form.xsl'),(1,'John'),(17,'Labels'),(13,'LabelTitles.xsl'),(4,'login_flow.xsl'),(9,'login_form.xsl'),(2,'Mark'),(5,'menu_flow.xsl'),(10,'menu_form.xsl'),(20,'Outbound'),(18,'Printer'),(27,'printerconfig_flow.xsl'),(28,'printerconfig_form.xsl'),(16,'PutAway'),(19,'Receiving');
/*!40000 ALTER TABLE `oac_resourceexternalid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `oac_resourcepassword`
--

LOCK TABLES `oac_resourcepassword` WRITE;
/*!40000 ALTER TABLE `oac_resourcepassword` DISABLE KEYS */;
INSERT INTO `oac_resourcepassword` VALUES (0,'kcd1B+YfQLhU/aCnOWRrfeIKc3twi1HD9JzyAbsxcP/c7x1OgSHpTrFpvjiVPX9J');
/*!40000 ALTER TABLE `oac_resourcepassword` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'oaccdb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-04-11 16:28:59
