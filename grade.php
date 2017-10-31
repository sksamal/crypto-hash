#!/usr/bin/php

<?php

include_once('grade.inc.php');


if($argc != 2) {
  print "Usage: php grade.php cseLogin\n";
  exit(1);
}

$isGrader = false;
$isHonors = false;

chdir($argv[1]);


    $testSuite = new TestSuite("CSCE 156 - Spring 2014 - Assignment 4 - Database design - Grader Suite", null, $isGrader);

    $testSuite->addSourceFile($sqlSchemaFile, file_get_contents($sqlSchemaFile));
    $testSuite->addSourceFile($sqlQueryFile, file_get_contents($sqlQueryFile));

    $testModule = new TestModule("Cinco Invoice Project", null, $isGrader);

    $testModule->addRequiredFile($sqlSchemaFile);
    $testModule->addRequiredFile($sqlQueryFile);

    $testCase = new TestCase("MD5 Example with steps");

    $testCase->addMessage("<p><b>Note</b>: Your sql file should be inserting test data.  Since it is <i>your</i>
    test data, there cannot be an expected output.  The webgrader is only able to confirm that your files are handed in
    and represent syntactically valid SQL.  Graders will look at your schema and your queries to determine if they
    are correct.");

    $testCase->addTestCaseCommand("echo 'running student DDL file ($sqlSchemaFile)...'");
    $testCase->addTestCaseCommand("echo 'running student query file ($sqlQueryFile)...'");
    $testCase->addTestCaseCommand("java -jar ../schemaSpy_5.0.0.jar -host localhost -t mysql -db $account -u $account -p $passwd -dp ../mysql-connector-java-5.1.17-bin.jar -o ./"); 

    $testModule->addTestCase($testCase);

    $testSuite->addTestModule($testModule);

    print $testSuite->run();

?>
