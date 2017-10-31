<?php

/**
 * This is a collection of classes that allows you to create test suites of
 * test modules having test cases.  Each class supports various functionality
 * including pre and post test commands (command line output is captured and
 * output by the script).
 */

abstract class Tester { 

  const version = "1.1.2";

  private static $collapseIdCounter = 100;

  public static $exitCodes = array(0   => "No Error",
  	 		     	   127 => "'Something wrong with the machine?' (Yeah, that's the best POSIX documentation I could find--awesome, right?)",
				   134 => "Your job received an abort signal, weird, huh?",
				   137 => "CPU Time Limit Exceeded",
				   139 => "Segmentation Fault");

  /**
   * Returns the contents of the given file in an HTML formatted
   * manner.
   */
  public static function fileGetContents($fileName) {
    $result = "<p>File: " . basename($fileName) . "</p>";
    if(file_exists($fileName)) {
      $result .= "<pre>" . htmlentities(file_get_contents($fileName)) . "</pre>";
    } else {
      $result .= "<p><span style='color: red; font-weight: bold;'>ERROR: </span>File, $fileName does not exist.  Cannot display contents.</p>";
    }
    return $result;
  }

  /**
   * Returns a map of file names => file contents from all files
   * with the specified $extension contained in the given zip file
   */
  public static function getFileContentsFromZipArchive($zipFileName, $extension) {

    $fileContents = array();
    if(!file_exists($zipFileName)) {
      return $fileContents;
    }

    $tmpDir = "./temp_zipDir";
    mkdir($tmpDir);
    copy($zipFileName, "$tmpDir/$zipFileName");
    chdir($tmpDir);
    exec("unzip $zipFileName");


    $files = new RecursiveIteratorIterator(new RecursiveDirectoryIterator("./"), RecursiveIteratorIterator::SELF_FIRST);
    foreach($files as $name => $object){
      if(substr_compare($name, $extension, -strlen($extension), strlen($extension)) === 0) {
        $fname = $object->getFilename();
        $fileContents[$fname] = file_get_contents($name);
      }
    }

    chdir("..");
    exec("rm -R $tmpDir");
    return $fileContents;
  }

  public static function getFormattedFileCommand($fileName) {

    $result = "";
    if($fileName != null) {
      $result = "echo '+--------------------- $fileName ---------------------+'; nl $fileName | more";
    }

    return $result;
  }

  const RAW  = 0;
  const HTML = 1;

  protected $label;
  protected $expectedOutput;
  protected $expectedOutputIsFormatted;
  private $message;
  protected $isGrader;
  private $sourceFiles;
  private $preTestCommands;
  private $preTestCommandsGrader;
  private $postTestCommands;
  private $postTestCommandsGrader;
  private $requiredFiles;

  protected $outputType = Tester::HTML;

  public function __construct($label = null, $expectedOutput = null, $isGrader = false, $isFormatted = false) {
    $this->label = $label;
    $this->expectedOutput = $expectedOutput;
    $this->isGrader = $isGrader;
    $this->expectedOutputIsFormatted = $isFormatted;
    $this->sourceFiles = Array();
    $this->preTestCommands = Array();
    $this->postTestCommands = Array();
  }

  abstract protected function executeCommands();

  protected function executeCommand($cmd, &$output, &$exitCode) {
    $lastLineResult = exec($cmd . " 2>&1 ", $output, $exitCode);
    $fullOutput = "";
    foreach($output as $line) {
      $fullOutput .= $line . "\n";
    } 
    if($exitCode > 0) {
      $exitCodeMsg = isset(Tester::$exitCodes[$exitCode]) ? Tester::$exitCodes[$exitCode] : "Unknown";
      $fullOutput .= "WARNING: process exited with a(n) $exitCodeMsg ($exitCode) error code\n";
    }
    $output = $fullOutput;
  }

  public function addMessage($message) {
    $this->message = $message;
  }

  public function addSourceFile($fileName, $fileContents = null) {
    if($fileContents == null) {
      //file content not provided, so let's get it for them
      // but only if it exists
      if(file_exists($fileName)) {
        $fileContents = file_get_contents($fileName);
      } else {
        $fileContents = "ERROR: no such file";
      }
    }
    $this->sourceFiles[$fileName] = $fileContents;
    return $this;
  }

  public function addPreTestCommand($cmd, $graderOnlyFlag = false) {
    if($graderOnlyFlag) {
      $this->preTestCommandsGrader[] = $cmd;
    } else {
      $this->preTestCommands[] = $cmd;
    }
    return $this;
  }

  public function addPostTestCommand($cmd, $graderOnlyFlag = false ) {
    if($graderOnlyFlag) {
      $this->postTestCommandsGrader[] = $cmd;
    } else {
      $this->postTestCommands[] = $cmd;
    }
    return $this;
  }

  public function addRequiredFile($file) {
    $this->requiredFiles[] = $file;
    return $this; 
  } 

  public function run() {
    
    Tester::$collapseIdCounter++;
    $result = "";

    if(!empty($this->label)) {
      if($this->outputType == Tester::HTML) {
        $cmd = "\$(\"#collapseId" . Tester::$collapseIdCounter . "\").toggle(\"blind\"); $(this).text() == \"[-]\"?$(this).text(\"[+]\"):$(this).text(\"[-]\");";
        $result .= "<div style='clear: both'><h2><span style='cursor: pointer;' onclick='$cmd'>[-]</span> $this</h2></div>\n"; 
      } else if ($this->outputType == Tester::RAW) {
        $result .= "$this \n";
      }
    }

    //wrap in a div here, start...
    if($this->outputType == Tester::HTML) {
      $result .= "<div id='collapseId" . Tester::$collapseIdCounter . "'>";
    }

    if(!empty($this->message)) {
      if($this->outputType == Tester::HTML) {
        $result .= "<div style='clear: both'><p>$this->message</p></div>\n";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "$this->message \n";
      }
    }

    if(count($this->requiredFiles) > 0) {
      if($this->outputType == Tester::HTML) {
        $result .= "<p>Checking for required files...</p>\n";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "Checking for required files...\n";
      }
      $fileMissing = false;
      foreach($this->requiredFiles as $file) {
        if(!file_exists($file)) {
	  if($this->outputType == Tester::HTML) {
            $result .= "<p><span style='color: red'>ERROR:</span> Required file $file not handed in, cannot be graded.\n</p>";
          } else if ($this->outputType == Tester::RAW) {
            $result .= "ERROR: Required file $file not handed in, cannot be graded.\n";
          }
          $fileMissing = true; 
        }
      }
      if($fileMissing) {
        if($this->outputType == Tester::HTML) {
 	  $result .= "<p><span style='color: red'>ERROR:</span> One or more files missing, skipping the rest of the grading process, hand these in first!</p>";
        } else if ($this->outputType == Tester::RAW) {
          $result .= "ERROR: one or more files missing, skipping the rest of the grading process, hand these in first!\n";
        }
        //end collapse div here since we short circuit the rest of the grading process...
        if($this->outputType == Tester::HTML) {
          $result .= "</div>";
        }
	return $result;
      }
    }

    //only print source code if its a grader
    if($this->isGrader) {
      foreach($this->sourceFiles as $file => $contents) {  
        if($this->outputType == Tester::HTML) {
          $result .= '<p><b>' . $file . '</b></p>';
          $result .= '<pre class="prettyprint linenums"><code">' . htmlentities($contents) . '</code></pre>';
        } else {
	  $result .= $file . "\n";
	  $result .= $contents . "\n";
	}
      }
    }

    if($this->isGrader) {
      if(count($this->preTestCommandsGrader) > 0) {
        if($this->outputType == Tester::HTML) {
          $result .= "<p>Running Pre Test Case (Grader) Commands...</p>\n";
        } else if ($this->outputType == Tester::RAW) {
          $result .= "Running Pre Test Case (Grader) Commands...\n";
        }
        $fullOutput = "";
        foreach($this->preTestCommandsGrader as $cmd) {
          $exitCode = "";
          $this->executeCommand($cmd, $output, $exitCode);
          $fullOutput .= $output;
        }
        if($this->outputType == Tester::HTML) {
          $result .= "<pre>".htmlentities($fullOutput)."</pre>\n";
        } else if ($this->outputType == Tester::RAW) {
          $result .= "$fullOutput\n";
        }
      }
    }

    if(count($this->preTestCommands) > 0) {
      if($this->outputType == Tester::HTML) {
        $result .= "<p>Running Pre Test Case Commands...</p>\n";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "Running Pre Test Case Commands...\n";
      }
      $fullOutput = "";
      foreach($this->preTestCommands as $cmd) {
	$exitCode = "";
	$this->executeCommand($cmd, $output, $exitCode);
	$fullOutput .= $output;
      }
      if($this->outputType == Tester::HTML) {
        $result .= "<pre>".htmlentities($fullOutput)."</pre>\n";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "$fullOutput\n";
      }
    }

    if(!empty($this->expectedOutput)) {
      if($this->outputType == Tester::RAW) {
        $result .= "-------------------------Expected Output START---------------------------\n";
        $result .= "$this->expectedOutput\n";
        $result .= "-------------------------Expected Output END-----------------------------\n";
      } else if ($this->outputType == Tester::HTML) {
        $result .= "<div style='padding: 0px; border-style: solid; border-width: 1px;'>";
        $result .= "<div style='float: left'>\n";
        $result .= "<h4>Expected Output</h4>\n";
	$result .= "<div style='padding: 10px; border-style: solid; border-width: 1px;'>\n"; 
	if($this->expectedOutputIsFormatted) {
          $result .= "<pre>".$this->expectedOutput."</pre>\n";
	} else {
          $result .= "<pre>".htmlentities($this->expectedOutput)."</pre>\n";
	}
	$result .= "</div>";
        $result .= "</div>";
	$result .= $this->executeCommands();
	$result .= "</div>";
      }
    } else {
      $result .= $this->executeCommands();
    }

    if(count($this->postTestCommands) > 0) {
      if($this->outputType == Tester::HTML) {
        $result .= "<p style='clear: both; padding: 10px;'>Running Post Test Case Commands...</p>\n";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "Running Post Test Case Commands...\n";
      }
      $fullOutput = "";
      foreach($this->postTestCommands as $cmd) {
        $exitCode = "";
        $this->executeCommand($cmd, $output, $exitCode);
        $fullOutput .= $output;
      }
      if($this->outputType == Tester::HTML) {
        $result .= "<pre>".htmlentities($fullOutput)."</pre>\n";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "$fullOutput\n";
      }
    }

    //end collapse div here since we short circuit the rest	of the grading process...
    if($this->outputType == Tester::HTML) {
      $result .= "</div>";
    }

    return $result;
  }

  public function __toString() {
    return "Test Case $this->label";
  }

}

class TestModule extends Tester {

  private $testCases;
  public function __construct($label = null, $expectedOutput = null, $isGrader = false) {
    parent::__construct($label, $expectedOutput, $isGrader);
    $this->testCases = Array();
  }

  public function addTestCase($testCase) {
    $this->testCases[] = $testCase;
  }

  protected function executeCommands() {
    $result = "";
    if(count($this->testCases) > 0) {
      $result .= "Running Test Cases...\n";
      foreach($this->testCases as $testCase) {
        $result .= $testCase->run();
      }
    }
    return $result;
  }

  public function __toString() {
    return "Test Module $this->label";
  }

}

class TestSuite extends Tester {
  private $testModules;

  public function __construct($label = null, $expectedOutput = null, $isGrader = false) {
    parent::__construct($label, $expectedOutput, $isGrader);
    $this->testModules = Array();
  }

  public function addTestModule($testModule) {
    $this->testModules[] = $testModule;
  }

  protected function executeCommands() {
    $result = "";
    if(count($this->testModules) > 0) {
      $result .= "Running Test Module Commands...\n";
      foreach($this->testModules as $testModule) {
        $result .= $testModule->run();
      }
    }
    return $result;
  }

  public function __toString() {
    return "Test Suite $this->label";
  }
}

class TestCase extends Tester {

  private $testCaseCommands;

  public function __construct($label = null, $expectedOutput = null, $isGrader = false, $isFormatted = false) {
    parent::__construct($label, $expectedOutput, $isGrader, $isFormatted);
    $this->testCaseCommands = Array();
  }

  public function addTestCaseCommand($cmd) {
    $this->testCaseCommands[] = $cmd;
    return $this;
  }

  protected function executeCommands() { 
    $result = "";
    if(count($this->testCaseCommands) > 0) {

      $fullOutput = "";
      foreach($this->testCaseCommands as $cmd) {
        $output = "";
        $exitCode = "";
        $this->executeCommand($cmd, $output, $exitCode);
	$fullOutput .= $output;
      }

      if($this->outputType == Tester::HTML) {
        //$result .= "<p>Running Test Case Commands...</p>\n";
	//if there was no expected output, float it left:
        if(!empty($this->expectedOutput)) {
          $result .= "<div style='float: right;'>\n";
        } else {
          $result .= "<div style='float: left;'>\n";
        }
	 $result .= "<h4>Program Output</h4>\n";
        $result .= "<div style='padding: 10px; border-style: solid; border-width: 1px;'><pre>".htmlentities($fullOutput)."</pre></div>\n";
        $result .= "</div>";
      } else if ($this->outputType == Tester::RAW) {
        $result .= "Running Test Case Commands...\n";
        $result .= "Program Ouput:\n";
        $result .= $output . "\n";
      }
    }
    return $result;
  }

  public function __toString() {
    return "Test Case $this->label";  
  }

}


?>