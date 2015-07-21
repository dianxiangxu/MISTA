<?php
include_once 'Blocks.php';
include_once 'ON.php';


$block = new Blocks();
$block->init();
$block->unstack(1, 3);
$block->putdown(1);
$block->pickup(6);
$block->stack(6, 1);

echo "Final State: ".$block->toString();


/*
Block block = new Block();
block.init();
block.unstack("1", "3");
block.putdown("1");
block.pickup("6");
block.stack("6", "1");
System.out.println("Final state: "+block);
*/