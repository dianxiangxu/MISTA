//Warning: The same setUp method is used for multiple initial states.

<?php
include 'Blocks.php';
include 'ON.php';


class BlocksTester_TC extends PHPUnit_Framework_TestCase{

	protected $blocks;
	
	public function setUp() {
		$this->blocks = new Blocks;
		$this->blocks->getClears()->append("B3");
		$this->blocks->getClears()->append("B6");
		$this->blocks->getOntables()->append("B3");
		$this->blocks->getOntables()->append("B6");
	}
	
	public function setUp2() {
		$this->blocks = new Blocks;
		$this->blocks->getClears()->append("B1");
		$this->blocks->getClears()->append("B6");
		$this->blocks->getOns()->append(new ON("B1", "B3"));
		$this->blocks->getOntables()->append("B3");
		$this->blocks->getOntables()->append("B6");
	}
	
	public function test1() {
		echo "Test case 1";
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test2() {
		echo "Test case 2";
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test3() {
		echo "Test case 3";
		$this->blocks->unstack("B1", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

}
