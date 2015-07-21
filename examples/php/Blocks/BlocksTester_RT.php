//Warning: The same setUp method is used for multiple initial states.

<?php
include 'Blocks.php';
include 'ON.php';


class BlocksTester_RT extends PHPUnit_Framework_TestCase{

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
	
	public function setUp3() {
		$this->blocks = new Blocks;
		$this->blocks->getClears()->append("B2");
		$this->blocks->getClears()->append("B5");
		$this->blocks->getOns()->append(new ON("B2", "B3"));
		$this->blocks->getOns()->append(new ON("B5", "B6"));
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
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test3() {
		echo "Test case 3";
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test4() {
		echo "Test case 4";
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test5() {
		echo "Test case 5";
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B1", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B1", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test6() {
		echo "Test case 6";
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B1");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B1", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B6", "B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B1", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test7() {
		echo "Test case 7";
		$this->blocks->unstack("B1", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B1");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test8() {
		echo "Test case 8";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test9() {
		echo "Test case 9";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B1");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test10() {
		echo "Test case 10";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B1"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
	}

	public function test11() {
		echo "Test case 11";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B1");
		$this->blocks->unstack("B3", "B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test12() {
		echo "Test case 12";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B1");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test13() {
		echo "Test case 13";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B1");
		$this->blocks->stack("B1", "B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isOn("B1", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B1", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test14() {
		echo "Test case 14";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test15() {
		echo "Test case 15";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test16() {
		echo "Test case 16";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B1");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test17() {
		echo "Test case 17";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B1");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B1"));
		$this->assertTrue($this->blocks->isOntable("B1"));
	}

	public function test18() {
		echo "Test case 18";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B1");
		$this->blocks->unstack("B6", "B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test19() {
		echo "Test case 19";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->pickup("B1");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test20() {
		echo "Test case 20";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B1");
		$this->blocks->stack("B1", "B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isOn("B1", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B1", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test21() {
		echo "Test case 21";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->putdown("B1");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test22() {
		echo "Test case 22";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->stack("B1", "B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B1", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test23() {
		echo "Test case 23";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->stack("B1", "B6");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B1", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B1", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B1", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test24() {
		echo "Test case 24";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->stack("B1", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B1");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B1", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B1"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B3", "B1");
		$this->assertTrue($this->blocks->isClear("B1"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B1", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test25() {
		echo "Test case 25";
		$this->blocks->unstack("B1", "B3");
		$this->blocks->stack("B1", "B6");
		$this->blocks->unstack("B1", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B1"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test26() {
		echo "Test case 26";
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test27() {
		echo "Test case 27";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test28() {
		echo "Test case 28";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test29() {
		echo "Test case 29";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test30() {
		echo "Test case 30";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test31() {
		echo "Test case 31";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test32() {
		echo "Test case 32";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
	}

	public function test33() {
		echo "Test case 33";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test34() {
		echo "Test case 34";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test35() {
		echo "Test case 35";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test36() {
		echo "Test case 36";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test37() {
		echo "Test case 37";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test38() {
		echo "Test case 38";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
	}

	public function test39() {
		echo "Test case 39";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test40() {
		echo "Test case 40";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test41() {
		echo "Test case 41";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test42() {
		echo "Test case 42";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test43() {
		echo "Test case 43";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test44() {
		echo "Test case 44";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test45() {
		echo "Test case 45";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test46() {
		echo "Test case 46";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test47() {
		echo "Test case 47";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test48() {
		echo "Test case 48";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test49() {
		echo "Test case 49";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test50() {
		echo "Test case 50";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test51() {
		echo "Test case 51";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test52() {
		echo "Test case 52";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test53() {
		echo "Test case 53";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test54() {
		echo "Test case 54";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test55() {
		echo "Test case 55";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test56() {
		echo "Test case 56";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test57() {
		echo "Test case 57";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test58() {
		echo "Test case 58";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test59() {
		echo "Test case 59";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test60() {
		echo "Test case 60";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test61() {
		echo "Test case 61";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test62() {
		echo "Test case 62";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test63() {
		echo "Test case 63";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test64() {
		echo "Test case 64";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test65() {
		echo "Test case 65";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test66() {
		echo "Test case 66";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test67() {
		echo "Test case 67";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test68() {
		echo "Test case 68";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test69() {
		echo "Test case 69";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test70() {
		echo "Test case 70";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test71() {
		echo "Test case 71";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test72() {
		echo "Test case 72";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test73() {
		echo "Test case 73";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test74() {
		echo "Test case 74";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test75() {
		echo "Test case 75";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
	}

	public function test76() {
		echo "Test case 76";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test77() {
		echo "Test case 77";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test78() {
		echo "Test case 78";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test79() {
		echo "Test case 79";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test80() {
		echo "Test case 80";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
	}

	public function test81() {
		echo "Test case 81";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test82() {
		echo "Test case 82";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test83() {
		echo "Test case 83";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test84() {
		echo "Test case 84";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test85() {
		echo "Test case 85";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test86() {
		echo "Test case 86";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test87() {
		echo "Test case 87";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test88() {
		echo "Test case 88";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test89() {
		echo "Test case 89";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test90() {
		echo "Test case 90";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test91() {
		echo "Test case 91";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test92() {
		echo "Test case 92";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test93() {
		echo "Test case 93";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test94() {
		echo "Test case 94";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test95() {
		echo "Test case 95";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test96() {
		echo "Test case 96";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test97() {
		echo "Test case 97";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test98() {
		echo "Test case 98";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test99() {
		echo "Test case 99";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test100() {
		echo "Test case 100";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test101() {
		echo "Test case 101";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test102() {
		echo "Test case 102";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test103() {
		echo "Test case 103";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
	}

	public function test104() {
		echo "Test case 104";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test105() {
		echo "Test case 105";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test106() {
		echo "Test case 106";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test107() {
		echo "Test case 107";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test108() {
		echo "Test case 108";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test109() {
		echo "Test case 109";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test110() {
		echo "Test case 110";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test111() {
		echo "Test case 111";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
	}

	public function test112() {
		echo "Test case 112";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test113() {
		echo "Test case 113";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test114() {
		echo "Test case 114";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test115() {
		echo "Test case 115";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test116() {
		echo "Test case 116";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test117() {
		echo "Test case 117";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B5");
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test118() {
		echo "Test case 118";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test119() {
		echo "Test case 119";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test120() {
		echo "Test case 120";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test121() {
		echo "Test case 121";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test122() {
		echo "Test case 122";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test123() {
		echo "Test case 123";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->pickup("B2");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test124() {
		echo "Test case 124";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->pickup("B2");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test125() {
		echo "Test case 125";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test126() {
		echo "Test case 126";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B3");
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test127() {
		echo "Test case 127";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->putdown("B2");
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test128() {
		echo "Test case 128";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test129() {
		echo "Test case 129";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test130() {
		echo "Test case 130";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test131() {
		echo "Test case 131";
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test132() {
		echo "Test case 132";
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test133() {
		echo "Test case 133";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test134() {
		echo "Test case 134";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test135() {
		echo "Test case 135";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test136() {
		echo "Test case 136";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test137() {
		echo "Test case 137";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test138() {
		echo "Test case 138";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test139() {
		echo "Test case 139";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test140() {
		echo "Test case 140";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test141() {
		echo "Test case 141";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test142() {
		echo "Test case 142";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test143() {
		echo "Test case 143";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOntable("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test144() {
		echo "Test case 144";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test145() {
		echo "Test case 145";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test146() {
		echo "Test case 146";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test147() {
		echo "Test case 147";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test148() {
		echo "Test case 148";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test149() {
		echo "Test case 149";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test150() {
		echo "Test case 150";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test151() {
		echo "Test case 151";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test152() {
		echo "Test case 152";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test153() {
		echo "Test case 153";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B3", "B6"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B3", "B6");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test154() {
		echo "Test case 154";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B2");
		$this->blocks->unstack("B6", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test155() {
		echo "Test case 155";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test156() {
		echo "Test case 156";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B3");
		$this->blocks->unstack("B6", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
	}

	public function test157() {
		echo "Test case 157";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B5");
		$this->blocks->unstack("B2", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test158() {
		echo "Test case 158";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test159() {
		echo "Test case 159";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test160() {
		echo "Test case 160";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test161() {
		echo "Test case 161";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B2");
		$this->blocks->unstack("B3", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test162() {
		echo "Test case 162";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test163() {
		echo "Test case 163";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test164() {
		echo "Test case 164";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B5");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test165() {
		echo "Test case 165";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B3");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->putdown("B3");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test166() {
		echo "Test case 166";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B3");
		$this->blocks->stack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B3", "B5"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->unstack("B3", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B3"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test167() {
		echo "Test case 167";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B2");
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test168() {
		echo "Test case 168";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->pickup("B5");
		$this->blocks->stack("B5", "B3");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B6"));
		$this->assertTrue($this->blocks->isOn("B5", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test169() {
		echo "Test case 169";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->putdown("B5");
		$this->blocks->unstack("B2", "B3");
		$this->blocks->stack("B2", "B6");
		$this->blocks->unstack("B2", "B6");
		$this->assertTrue($this->blocks->isClear("B3"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B5"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test170() {
		echo "Test case 170";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
		$this->blocks->pickup("B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->putdown("B6");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test171() {
		echo "Test case 171";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->pickup("B6");
		$this->blocks->stack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOn("B6", "B5"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->blocks->unstack("B6", "B5");
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isHolding("B6"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B2"));
		$this->assertTrue($this->blocks->isOntable("B3"));
	}

	public function test172() {
		echo "Test case 172";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B2");
		$this->blocks->unstack("B5", "B2");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B6"));
		$this->assertTrue($this->blocks->isHolding("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

	public function test173() {
		echo "Test case 173";
		$this->blocks->unstack("B5", "B6");
		$this->blocks->stack("B5", "B6");
		$this->assertTrue($this->blocks->isClear("B2"));
		$this->assertTrue($this->blocks->isClear("B5"));
		$this->assertTrue($this->blocks->isOn("B2", "B3"));
		$this->assertTrue($this->blocks->isOn("B5", "B6"));
		$this->assertTrue($this->blocks->isOntable("B3"));
		$this->assertTrue($this->blocks->isOntable("B6"));
	}

}
