<?php
include 'Blocks.php';
include 'ON.php';

class BlocksTest extends PHPUnit_Framework_TestCase
{
   
    protected $block;
    protected $clears;
    protected $ontables;

    /**
     * Sets up the fixture, for example, opens a network connection.
     * This method is called before a test is executed.
     */
    protected function setUp()
    {
        $this->block = new Blocks;
        
        $this->clears=$this->block->getClears();
        $this->block->addToArray('clears',"B3");
        $this->block->addToArray('clears',"B6");
        
        $this->ontables=$this->block->getOntables();
        $this->block->addToArray('ontables',"B3");
        $this->block->addToArray('ontables',"B6");
        
    }

    /**
     * Tears down the fixture, for example, closes a network connection.
     * This method is called after a test is executed.
     */
    protected function tearDown()
    {
    }

   
    public function test1()
    {
    	echo "TestCase 1";
    	
    	$this->block->pickup("B3");
    	
        $this->assertTrue($this->block->isClear("B6"));
        $this->assertTrue($this->block->isHolding("B3"));
        $this->assertTrue($this->block->isOntable("B6"));
        
        $this->block->putdown("B3");
        $this->assertTrue($this->block->isClear("B3"));
        $this->assertTrue($this->block->isClear("B6"));
        $this->assertTrue($this->block->isOntable("B3"));
        $this->assertTrue($this->block->isOntable("B6"));
        
    }
    
}