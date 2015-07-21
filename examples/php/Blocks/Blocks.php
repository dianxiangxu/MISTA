<?php
class Blocks{
	
	private $ontables;
	private $clears;
	private $ons;
	private $holding = null;
	private $handempty = true;
	
	public function __construct(){
		$this->ontables = new ArrayObject();
		$this->clears = new ArrayObject();
		$this->ons = new ArrayObject();
	}
	
	public function pickup($a){
				
		if (in_array($a, (array)$this->ontables) && in_array($a, (array)$this->clears) && $this->handempty){
			
			self::arrayRemoveElement($this->ontables, $a);			
			self::arrayRemoveElement($this->clears, $a);
			$this->handempty = false;
			$this->holding = $a;
		}
		else
			echo "Cannot pickup ".$a;
	}
	
	public function putdown($a){
		if (!is_null($this->holding)){
			$this->holding = null;
			$this->ontables->append($a);
			$this->clears->append($a);
			$this->handempty = true;
		}
		else
			echo "Cannot put down ".a;
	}
	
	public function stack($a, $b){
		if ($a!=$b && !is_null($this->holding) && $this->holding==$a && in_array($b,(array)$this->clears)){
			$this->handempty=true;
			$this->ons->append(new ON($a,$b));
			self::arrayRemoveElement($this->clears, $b);			
			$this->clears[]=$a;
			$this->holding = null;
		}
		else
			echo "Cannot stack ".a." on ".b;
	}
	
	public function unstack($a,$b){
		if ($this->handempty && in_array($a, (array)$this->clears) && $this->isOn($a, $b)){			
			$this->handempty = false;
			self::arrayRemoveElement($this->clears, $a);			
			$this->removeOn($a, $b);
			$this->holding = $a;
			$this->clears[]=$b;
		}
		else
			echo "Cannot unstack ".$a." from ".$b;
	}
	
	public function getOntables(){
		return $this->ontables;
	}
	
	public function isOntable($a){
		return in_array($a, (array)$this->ontables);
	}
	
	public function getClears(){
		return $this->clears;
	}
	
	public function isClear($a){
		return in_array($a, (array)$this->clears);
	}
	
	public function getOns(){
		return $this->ons;
	}
	
	public function isOn($a, $b){
		$on2 = new ON($a, $b);
		foreach($this->ons as $on)
		if ($on==$on2)
			return true;
		return false;
	}
	
	private function removeOn($a, $b){
		$on2 = new ON($a, $b);		
		foreach ((array)$this->ons as $key => $value) {
			if($value==$on2){
				$this->ons->offsetUnset($key);
			}				
		}
		
	}
	
	public function getHolding(){
		return $this->holding;
	}
	
	public function isHolding($thing){
		return $this->holding!=null && $this->holding=$thing;
	}
	
	public function isHandempty(){
		return $this->handempty;
	}
	
	public function setHandempty($empty){
		$this->handempty = $empty;
	}
	
	public function toString(){
		$str = "";
				
		foreach ($this->ontables as $ontable)
			$str .= "\n\tontable(".$ontable.")";
		
		foreach ($this->clears as $clear)
			$str .= "\n\tclear(".$clear.")";
		
		//var_dump($this->ons);
		foreach ($this->ons as $key=>$value){
						
			$str .= "\n\t ON".$this->ons->offsetGet($key)->toString();
			
		}
		
		if (!is_null($this->holding))
			$str .= "\n\tholding(".$this->holding.")";
		
		if ($this->handempty)
			$str .= "\n\thandempty(\"r\")";
		
		return $str;
	}
	
	public function init(){
		
		$this->ontables->append(3);
		$this->ontables->append(6);
		
		$this->clears->append(1);
		$this->clears->append(6);
		
		$this->ons->append(new ON(1, 3));
		$this->handempty = true;
				
	}	
	
	// EXTRAS
	
	/**
	 * Simulation of arrayList.remove()
	 * @param Array $array
	 * @param int $elToRemove
	 */
	public static function arrayRemoveElement($array,$elToRemove){
		
		foreach ((array)$array as $key => $value) {
			if($value==$elToRemove){
				$array->offsetUnset($key);
			}			
		}
	}
	
	
}