<?php
class ON{
	private  $a, $b;
	
	public function __construct($a,$b){
		$this->a = $a;
		$this->b = $b;
	}
	
	public function equals($on2){
		return $a->equals($on2->a) && $b->equals($on2->b);
	}
	
	public function toString(){
		return "on("+$this->a+", "+$this->b+")";
	}
}