//Warning: The same setUp method is used for multiple initial states.

using Blocks;
using System;
using NUnit.Framework;

namespace test {

[TestFixture()]
	public class BlockTester_RT {

		private Block block;
		
	[SetUp()]
		public void Init() {
			block = new Block();
			block.GetClears().Add("B3");
			block.GetClears().Add("B6");
			block.GetOntables().Add("B3");
			block.GetOntables().Add("B6");
		}
		
	[SetUp()]
		public void Init2() {
			block = new Block();
			block.GetClears().Add("B1");
			block.GetClears().Add("B6");
			block.GetOns().Add(new ON("B1", "B3"));
			block.GetOntables().Add("B3");
			block.GetOntables().Add("B6");
		}
		
	[SetUp()]
		public void Init3() {
			block = new Block();
			block.GetClears().Add("B2");
			block.GetClears().Add("B5");
			block.GetOns().Add(new ON("B2", "B3"));
			block.GetOns().Add(new ON("B5", "B6"));
			block.GetOntables().Add("B3");
			block.GetOntables().Add("B6");
		}
		
		private void Assert(bool condition, string errorMessage) {
			if (!condition){
				Console.WriteLine(errorMessage);
				Console.WriteLine("\nPress any key to continue...");
				Console.Read();
				Environment.Exit(1);
			}
		}

	[Test]
		public void Test1() {
			Console.WriteLine("Test case 1");
			block.Pickup("B3");
			Assert.True(IsClear("B6"), "1_1");
			Assert.True(IsHolding("B3"), "1_1");
			Assert.True(IsOntable("B6"), "1_1");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "1_1_1");
			Assert.True(IsClear("B6"), "1_1_1");
			Assert.True(IsOntable("B3"), "1_1_1");
			Assert.True(IsOntable("B6"), "1_1_1");
		}

	[Test]
		public void Test2() {
			Console.WriteLine("Test case 2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B3"), "1_1_2");
			Assert.True(IsOn("B3", "B6"), "1_1_2");
			Assert.True(IsOntable("B6"), "1_1_2");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B6"), "1_1_2_1");
			Assert.True(IsHolding("B3"), "1_1_2_1");
			Assert.True(IsOntable("B6"), "1_1_2_1");
		}

	[Test]
		public void Test3() {
			Console.WriteLine("Test case 3");
			block.Pickup("B6");
			Assert.True(IsClear("B3"), "1_2");
			Assert.True(IsHolding("B6"), "1_2");
			Assert.True(IsOntable("B3"), "1_2");
			block.Putdown("B6");
			Assert.True(IsClear("B3"), "1_2_1");
			Assert.True(IsClear("B6"), "1_2_1");
			Assert.True(IsOntable("B3"), "1_2_1");
			Assert.True(IsOntable("B6"), "1_2_1");
		}

	[Test]
		public void Test4() {
			Console.WriteLine("Test case 4");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B6"), "1_2_2");
			Assert.True(IsOn("B6", "B3"), "1_2_2");
			Assert.True(IsOntable("B3"), "1_2_2");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B3"), "1_2_2_1");
			Assert.True(IsHolding("B6"), "1_2_2_1");
			Assert.True(IsOntable("B3"), "1_2_2_1");
		}

	[Test]
		public void Test5() {
			Console.WriteLine("Test case 5");
			block.Pickup("B6");
			Assert.True(IsClear("B1"), "2_1");
			Assert.True(IsHolding("B6"), "2_1");
			Assert.True(IsOn("B1", "B3"), "2_1");
			Assert.True(IsOntable("B3"), "2_1");
			block.Putdown("B6");
			Assert.True(IsClear("B1"), "2_1_1");
			Assert.True(IsClear("B6"), "2_1_1");
			Assert.True(IsOn("B1", "B3"), "2_1_1");
			Assert.True(IsOntable("B3"), "2_1_1");
			Assert.True(IsOntable("B6"), "2_1_1");
		}

	[Test]
		public void Test6() {
			Console.WriteLine("Test case 6");
			block.Pickup("B6");
			block.Stack("B6", "B1");
			Assert.True(IsClear("B6"), "2_1_2");
			Assert.True(IsOn("B1", "B3"), "2_1_2");
			Assert.True(IsOn("B6", "B1"), "2_1_2");
			Assert.True(IsOntable("B3"), "2_1_2");
			block.Unstack("B6", "B1");
			Assert.True(IsClear("B1"), "2_1_2_1");
			Assert.True(IsHolding("B6"), "2_1_2_1");
			Assert.True(IsOn("B1", "B3"), "2_1_2_1");
			Assert.True(IsOntable("B3"), "2_1_2_1");
		}

	[Test]
		public void Test7() {
			Console.WriteLine("Test case 7");
			block.Unstack("B1", "B3");
			Assert.True(IsClear("B3"), "2_2");
			Assert.True(IsClear("B6"), "2_2");
			Assert.True(IsHolding("B1"), "2_2");
			Assert.True(IsOntable("B3"), "2_2");
			Assert.True(IsOntable("B6"), "2_2");
			block.Putdown("B1");
			Assert.True(IsClear("B1"), "2_2_1");
			Assert.True(IsClear("B3"), "2_2_1");
			Assert.True(IsClear("B6"), "2_2_1");
			Assert.True(IsOntable("B1"), "2_2_1");
			Assert.True(IsOntable("B3"), "2_2_1");
			Assert.True(IsOntable("B6"), "2_2_1");
			block.Pickup("B1");
			Assert.True(IsClear("B3"), "2_2_1_1");
			Assert.True(IsClear("B6"), "2_2_1_1");
			Assert.True(IsHolding("B1"), "2_2_1_1");
			Assert.True(IsOntable("B3"), "2_2_1_1");
			Assert.True(IsOntable("B6"), "2_2_1_1");
		}

	[Test]
		public void Test8() {
			Console.WriteLine("Test case 8");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			Assert.True(IsClear("B1"), "2_2_1_2");
			Assert.True(IsClear("B6"), "2_2_1_2");
			Assert.True(IsHolding("B3"), "2_2_1_2");
			Assert.True(IsOntable("B1"), "2_2_1_2");
			Assert.True(IsOntable("B6"), "2_2_1_2");
			block.Putdown("B3");
			Assert.True(IsClear("B1"), "2_2_1_2_1");
			Assert.True(IsClear("B3"), "2_2_1_2_1");
			Assert.True(IsClear("B6"), "2_2_1_2_1");
			Assert.True(IsOntable("B1"), "2_2_1_2_1");
			Assert.True(IsOntable("B3"), "2_2_1_2_1");
			Assert.True(IsOntable("B6"), "2_2_1_2_1");
		}

	[Test]
		public void Test9() {
			Console.WriteLine("Test case 9");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			block.Stack("B3", "B1");
			Assert.True(IsClear("B3"), "2_2_1_2_2");
			Assert.True(IsClear("B6"), "2_2_1_2_2");
			Assert.True(IsOn("B3", "B1"), "2_2_1_2_2");
			Assert.True(IsOntable("B1"), "2_2_1_2_2");
			Assert.True(IsOntable("B6"), "2_2_1_2_2");
			block.Pickup("B6");
			Assert.True(IsClear("B3"), "2_2_1_2_2_1");
			Assert.True(IsHolding("B6"), "2_2_1_2_2_1");
			Assert.True(IsOn("B3", "B1"), "2_2_1_2_2_1");
			Assert.True(IsOntable("B1"), "2_2_1_2_2_1");
			block.Putdown("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B1"), "");
			Assert.True(IsOntable("B1"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test10() {
			Console.WriteLine("Test case 10");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			block.Stack("B3", "B1");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B1"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B1"), "");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B1"), "");
			Assert.True(IsOntable("B1"), "");
		}

	[Test]
		public void Test11() {
			Console.WriteLine("Test case 11");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			block.Stack("B3", "B1");
			block.Unstack("B3", "B1");
			Assert.True(IsClear("B1"), "2_2_1_2_2_2");
			Assert.True(IsClear("B6"), "2_2_1_2_2_2");
			Assert.True(IsHolding("B3"), "2_2_1_2_2_2");
			Assert.True(IsOntable("B1"), "2_2_1_2_2_2");
			Assert.True(IsOntable("B6"), "2_2_1_2_2_2");
		}

	[Test]
		public void Test12() {
			Console.WriteLine("Test case 12");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B1"), "2_2_1_2_3");
			Assert.True(IsClear("B3"), "2_2_1_2_3");
			Assert.True(IsOn("B3", "B6"), "2_2_1_2_3");
			Assert.True(IsOntable("B1"), "2_2_1_2_3");
			Assert.True(IsOntable("B6"), "2_2_1_2_3");
			block.Pickup("B1");
			Assert.True(IsClear("B3"), "2_2_1_2_3_1");
			Assert.True(IsHolding("B1"), "2_2_1_2_3_1");
			Assert.True(IsOn("B3", "B6"), "2_2_1_2_3_1");
			Assert.True(IsOntable("B6"), "2_2_1_2_3_1");
			block.Putdown("B1");
			Assert.True(IsClear("B1"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B1"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test13() {
			Console.WriteLine("Test case 13");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B1");
			block.Stack("B1", "B3");
			Assert.True(IsClear("B1"), "");
			Assert.True(IsOn("B1", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B1", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B1"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test14() {
			Console.WriteLine("Test case 14");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B1"), "2_2_1_2_3_2");
			Assert.True(IsClear("B6"), "2_2_1_2_3_2");
			Assert.True(IsHolding("B3"), "2_2_1_2_3_2");
			Assert.True(IsOntable("B1"), "2_2_1_2_3_2");
			Assert.True(IsOntable("B6"), "2_2_1_2_3_2");
		}

	[Test]
		public void Test15() {
			Console.WriteLine("Test case 15");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			Assert.True(IsClear("B1"), "2_2_1_3");
			Assert.True(IsClear("B3"), "2_2_1_3");
			Assert.True(IsHolding("B6"), "2_2_1_3");
			Assert.True(IsOntable("B1"), "2_2_1_3");
			Assert.True(IsOntable("B3"), "2_2_1_3");
			block.Putdown("B6");
			Assert.True(IsClear("B1"), "2_2_1_3_1");
			Assert.True(IsClear("B3"), "2_2_1_3_1");
			Assert.True(IsClear("B6"), "2_2_1_3_1");
			Assert.True(IsOntable("B1"), "2_2_1_3_1");
			Assert.True(IsOntable("B3"), "2_2_1_3_1");
			Assert.True(IsOntable("B6"), "2_2_1_3_1");
		}

	[Test]
		public void Test16() {
			Console.WriteLine("Test case 16");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			block.Stack("B6", "B1");
			Assert.True(IsClear("B3"), "2_2_1_3_2");
			Assert.True(IsClear("B6"), "2_2_1_3_2");
			Assert.True(IsOn("B6", "B1"), "2_2_1_3_2");
			Assert.True(IsOntable("B1"), "2_2_1_3_2");
			Assert.True(IsOntable("B3"), "2_2_1_3_2");
			block.Pickup("B3");
			Assert.True(IsClear("B6"), "2_2_1_3_2_1");
			Assert.True(IsHolding("B3"), "2_2_1_3_2_1");
			Assert.True(IsOn("B6", "B1"), "2_2_1_3_2_1");
			Assert.True(IsOntable("B1"), "2_2_1_3_2_1");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B1"), "");
			Assert.True(IsOntable("B1"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test17() {
			Console.WriteLine("Test case 17");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			block.Stack("B6", "B1");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B1"), "");
			Assert.True(IsOntable("B1"), "");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B1"), "");
			Assert.True(IsOntable("B1"), "");
		}

	[Test]
		public void Test18() {
			Console.WriteLine("Test case 18");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			block.Stack("B6", "B1");
			block.Unstack("B6", "B1");
			Assert.True(IsClear("B1"), "2_2_1_3_2_2");
			Assert.True(IsClear("B3"), "2_2_1_3_2_2");
			Assert.True(IsHolding("B6"), "2_2_1_3_2_2");
			Assert.True(IsOntable("B1"), "2_2_1_3_2_2");
			Assert.True(IsOntable("B3"), "2_2_1_3_2_2");
		}

	[Test]
		public void Test19() {
			Console.WriteLine("Test case 19");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B1"), "2_2_1_3_3");
			Assert.True(IsClear("B6"), "2_2_1_3_3");
			Assert.True(IsOn("B6", "B3"), "2_2_1_3_3");
			Assert.True(IsOntable("B1"), "2_2_1_3_3");
			Assert.True(IsOntable("B3"), "2_2_1_3_3");
			block.Pickup("B1");
			Assert.True(IsClear("B6"), "2_2_1_3_3_1");
			Assert.True(IsHolding("B1"), "2_2_1_3_3_1");
			Assert.True(IsOn("B6", "B3"), "2_2_1_3_3_1");
			Assert.True(IsOntable("B3"), "2_2_1_3_3_1");
			block.Putdown("B1");
			Assert.True(IsClear("B1"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B1"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test20() {
			Console.WriteLine("Test case 20");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B1");
			block.Stack("B1", "B6");
			Assert.True(IsClear("B1"), "");
			Assert.True(IsOn("B1", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B1", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B1"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test21() {
			Console.WriteLine("Test case 21");
			block.Unstack("B1", "B3");
			block.Putdown("B1");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B1"), "2_2_1_3_3_2");
			Assert.True(IsClear("B3"), "2_2_1_3_3_2");
			Assert.True(IsHolding("B6"), "2_2_1_3_3_2");
			Assert.True(IsOntable("B1"), "2_2_1_3_3_2");
			Assert.True(IsOntable("B3"), "2_2_1_3_3_2");
		}

	[Test]
		public void Test22() {
			Console.WriteLine("Test case 22");
			block.Unstack("B1", "B3");
			block.Stack("B1", "B3");
			Assert.True(IsClear("B1"), "2_2_2");
			Assert.True(IsClear("B6"), "2_2_2");
			Assert.True(IsOn("B1", "B3"), "2_2_2");
			Assert.True(IsOntable("B3"), "2_2_2");
			Assert.True(IsOntable("B6"), "2_2_2");
		}

	[Test]
		public void Test23() {
			Console.WriteLine("Test case 23");
			block.Unstack("B1", "B3");
			block.Stack("B1", "B6");
			Assert.True(IsClear("B1"), "2_2_3");
			Assert.True(IsClear("B3"), "2_2_3");
			Assert.True(IsOn("B1", "B6"), "2_2_3");
			Assert.True(IsOntable("B3"), "2_2_3");
			Assert.True(IsOntable("B6"), "2_2_3");
			block.Pickup("B3");
			Assert.True(IsClear("B1"), "2_2_3_1");
			Assert.True(IsHolding("B3"), "2_2_3_1");
			Assert.True(IsOn("B1", "B6"), "2_2_3_1");
			Assert.True(IsOntable("B6"), "2_2_3_1");
			block.Putdown("B3");
			Assert.True(IsClear("B1"), "2_2_3_1_1");
			Assert.True(IsClear("B3"), "2_2_3_1_1");
			Assert.True(IsOn("B1", "B6"), "2_2_3_1_1");
			Assert.True(IsOntable("B3"), "2_2_3_1_1");
			Assert.True(IsOntable("B6"), "2_2_3_1_1");
		}

	[Test]
		public void Test24() {
			Console.WriteLine("Test case 24");
			block.Unstack("B1", "B3");
			block.Stack("B1", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B1");
			Assert.True(IsClear("B3"), "2_2_3_1_2");
			Assert.True(IsOn("B1", "B6"), "2_2_3_1_2");
			Assert.True(IsOn("B3", "B1"), "2_2_3_1_2");
			Assert.True(IsOntable("B6"), "2_2_3_1_2");
			block.Unstack("B3", "B1");
			Assert.True(IsClear("B1"), "2_2_3_1_2_1");
			Assert.True(IsHolding("B3"), "2_2_3_1_2_1");
			Assert.True(IsOn("B1", "B6"), "2_2_3_1_2_1");
			Assert.True(IsOntable("B6"), "2_2_3_1_2_1");
		}

	[Test]
		public void Test25() {
			Console.WriteLine("Test case 25");
			block.Unstack("B1", "B3");
			block.Stack("B1", "B6");
			block.Unstack("B1", "B6");
			Assert.True(IsClear("B3"), "2_2_3_2");
			Assert.True(IsClear("B6"), "2_2_3_2");
			Assert.True(IsHolding("B1"), "2_2_3_2");
			Assert.True(IsOntable("B3"), "2_2_3_2");
			Assert.True(IsOntable("B6"), "2_2_3_2");
		}

	[Test]
		public void Test26() {
			Console.WriteLine("Test case 26");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "3_1");
			Assert.True(IsClear("B5"), "3_1");
			Assert.True(IsHolding("B2"), "3_1");
			Assert.True(IsOn("B5", "B6"), "3_1");
			Assert.True(IsOntable("B3"), "3_1");
			Assert.True(IsOntable("B6"), "3_1");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "3_1_1");
			Assert.True(IsClear("B3"), "3_1_1");
			Assert.True(IsClear("B5"), "3_1_1");
			Assert.True(IsOn("B5", "B6"), "3_1_1");
			Assert.True(IsOntable("B2"), "3_1_1");
			Assert.True(IsOntable("B3"), "3_1_1");
			Assert.True(IsOntable("B6"), "3_1_1");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "3_1_1_1");
			Assert.True(IsClear("B5"), "3_1_1_1");
			Assert.True(IsHolding("B2"), "3_1_1_1");
			Assert.True(IsOn("B5", "B6"), "3_1_1_1");
			Assert.True(IsOntable("B3"), "3_1_1_1");
			Assert.True(IsOntable("B6"), "3_1_1_1");
		}

	[Test]
		public void Test27() {
			Console.WriteLine("Test case 27");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "3_1_1_2");
			Assert.True(IsClear("B5"), "3_1_1_2");
			Assert.True(IsHolding("B3"), "3_1_1_2");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2");
			Assert.True(IsOntable("B2"), "3_1_1_2");
			Assert.True(IsOntable("B6"), "3_1_1_2");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "3_1_1_2_1");
			Assert.True(IsClear("B3"), "3_1_1_2_1");
			Assert.True(IsClear("B5"), "3_1_1_2_1");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2_1");
			Assert.True(IsOntable("B2"), "3_1_1_2_1");
			Assert.True(IsOntable("B3"), "3_1_1_2_1");
			Assert.True(IsOntable("B6"), "3_1_1_2_1");
		}

	[Test]
		public void Test28() {
			Console.WriteLine("Test case 28");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "3_1_1_2_2");
			Assert.True(IsClear("B5"), "3_1_1_2_2");
			Assert.True(IsOn("B3", "B2"), "3_1_1_2_2");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2_2");
			Assert.True(IsOntable("B2"), "3_1_1_2_2");
			Assert.True(IsOntable("B6"), "3_1_1_2_2");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "3_1_1_2_2_1");
			Assert.True(IsClear("B5"), "3_1_1_2_2_1");
			Assert.True(IsHolding("B3"), "3_1_1_2_2_1");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2_2_1");
			Assert.True(IsOntable("B2"), "3_1_1_2_2_1");
			Assert.True(IsOntable("B6"), "3_1_1_2_2_1");
		}

	[Test]
		public void Test29() {
			Console.WriteLine("Test case 29");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B3"), "3_1_1_2_2_2");
			Assert.True(IsClear("B6"), "3_1_1_2_2_2");
			Assert.True(IsHolding("B5"), "3_1_1_2_2_2");
			Assert.True(IsOn("B3", "B2"), "3_1_1_2_2_2");
			Assert.True(IsOntable("B2"), "3_1_1_2_2_2");
			Assert.True(IsOntable("B6"), "3_1_1_2_2_2");
			block.Putdown("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test30() {
			Console.WriteLine("Test case 30");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test31() {
			Console.WriteLine("Test case 31");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B5");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test32() {
			Console.WriteLine("Test case 32");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
		}

	[Test]
		public void Test33() {
			Console.WriteLine("Test case 33");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test34() {
			Console.WriteLine("Test case 34");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test35() {
			Console.WriteLine("Test case 35");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test36() {
			Console.WriteLine("Test case 36");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test37() {
			Console.WriteLine("Test case 37");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test38() {
			Console.WriteLine("Test case 38");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
		}

	[Test]
		public void Test39() {
			Console.WriteLine("Test case 39");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test40() {
			Console.WriteLine("Test case 40");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test41() {
			Console.WriteLine("Test case 41");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B2"), "3_1_1_2_3");
			Assert.True(IsClear("B3"), "3_1_1_2_3");
			Assert.True(IsOn("B3", "B5"), "3_1_1_2_3");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2_3");
			Assert.True(IsOntable("B2"), "3_1_1_2_3");
			Assert.True(IsOntable("B6"), "3_1_1_2_3");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "3_1_1_2_3_1");
			Assert.True(IsHolding("B2"), "3_1_1_2_3_1");
			Assert.True(IsOn("B3", "B5"), "3_1_1_2_3_1");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2_3_1");
			Assert.True(IsOntable("B6"), "3_1_1_2_3_1");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test42() {
			Console.WriteLine("Test case 42");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test43() {
			Console.WriteLine("Test case 43");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B2"), "3_1_1_2_3_2");
			Assert.True(IsClear("B5"), "3_1_1_2_3_2");
			Assert.True(IsHolding("B3"), "3_1_1_2_3_2");
			Assert.True(IsOn("B5", "B6"), "3_1_1_2_3_2");
			Assert.True(IsOntable("B2"), "3_1_1_2_3_2");
			Assert.True(IsOntable("B6"), "3_1_1_2_3_2");
		}

	[Test]
		public void Test44() {
			Console.WriteLine("Test case 44");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B2"), "3_1_1_3");
			Assert.True(IsClear("B3"), "3_1_1_3");
			Assert.True(IsClear("B6"), "3_1_1_3");
			Assert.True(IsHolding("B5"), "3_1_1_3");
			Assert.True(IsOntable("B2"), "3_1_1_3");
			Assert.True(IsOntable("B3"), "3_1_1_3");
			Assert.True(IsOntable("B6"), "3_1_1_3");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "3_1_1_3_1");
			Assert.True(IsClear("B3"), "3_1_1_3_1");
			Assert.True(IsClear("B5"), "3_1_1_3_1");
			Assert.True(IsClear("B6"), "3_1_1_3_1");
			Assert.True(IsOntable("B2"), "3_1_1_3_1");
			Assert.True(IsOntable("B3"), "3_1_1_3_1");
			Assert.True(IsOntable("B5"), "3_1_1_3_1");
			Assert.True(IsOntable("B6"), "3_1_1_3_1");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "3_1_1_3_1_1");
			Assert.True(IsClear("B5"), "3_1_1_3_1_1");
			Assert.True(IsClear("B6"), "3_1_1_3_1_1");
			Assert.True(IsHolding("B2"), "3_1_1_3_1_1");
			Assert.True(IsOntable("B3"), "3_1_1_3_1_1");
			Assert.True(IsOntable("B5"), "3_1_1_3_1_1");
			Assert.True(IsOntable("B6"), "3_1_1_3_1_1");
		}

	[Test]
		public void Test45() {
			Console.WriteLine("Test case 45");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "3_1_1_3_1_2");
			Assert.True(IsClear("B5"), "3_1_1_3_1_2");
			Assert.True(IsClear("B6"), "3_1_1_3_1_2");
			Assert.True(IsHolding("B3"), "3_1_1_3_1_2");
			Assert.True(IsOntable("B2"), "3_1_1_3_1_2");
			Assert.True(IsOntable("B5"), "3_1_1_3_1_2");
			Assert.True(IsOntable("B6"), "3_1_1_3_1_2");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test46() {
			Console.WriteLine("Test case 46");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test47() {
			Console.WriteLine("Test case 47");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test48() {
			Console.WriteLine("Test case 48");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test49() {
			Console.WriteLine("Test case 49");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test50() {
			Console.WriteLine("Test case 50");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test51() {
			Console.WriteLine("Test case 51");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test52() {
			Console.WriteLine("Test case 52");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test53() {
			Console.WriteLine("Test case 53");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test54() {
			Console.WriteLine("Test case 54");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test55() {
			Console.WriteLine("Test case 55");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test56() {
			Console.WriteLine("Test case 56");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test57() {
			Console.WriteLine("Test case 57");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test58() {
			Console.WriteLine("Test case 58");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test59() {
			Console.WriteLine("Test case 59");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test60() {
			Console.WriteLine("Test case 60");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test61() {
			Console.WriteLine("Test case 61");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test62() {
			Console.WriteLine("Test case 62");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test63() {
			Console.WriteLine("Test case 63");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test64() {
			Console.WriteLine("Test case 64");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test65() {
			Console.WriteLine("Test case 65");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test66() {
			Console.WriteLine("Test case 66");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test67() {
			Console.WriteLine("Test case 67");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test68() {
			Console.WriteLine("Test case 68");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test69() {
			Console.WriteLine("Test case 69");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test70() {
			Console.WriteLine("Test case 70");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "3_1_1_3_1_3");
			Assert.True(IsClear("B3"), "3_1_1_3_1_3");
			Assert.True(IsClear("B6"), "3_1_1_3_1_3");
			Assert.True(IsHolding("B5"), "3_1_1_3_1_3");
			Assert.True(IsOntable("B2"), "3_1_1_3_1_3");
			Assert.True(IsOntable("B3"), "3_1_1_3_1_3");
			Assert.True(IsOntable("B6"), "3_1_1_3_1_3");
		}

	[Test]
		public void Test71() {
			Console.WriteLine("Test case 71");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "3_1_1_3_1_4");
			Assert.True(IsClear("B3"), "3_1_1_3_1_4");
			Assert.True(IsClear("B5"), "3_1_1_3_1_4");
			Assert.True(IsHolding("B6"), "3_1_1_3_1_4");
			Assert.True(IsOntable("B2"), "3_1_1_3_1_4");
			Assert.True(IsOntable("B3"), "3_1_1_3_1_4");
			Assert.True(IsOntable("B5"), "3_1_1_3_1_4");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test72() {
			Console.WriteLine("Test case 72");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test73() {
			Console.WriteLine("Test case 73");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test74() {
			Console.WriteLine("Test case 74");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test75() {
			Console.WriteLine("Test case 75");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
		}

	[Test]
		public void Test76() {
			Console.WriteLine("Test case 76");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test77() {
			Console.WriteLine("Test case 77");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test78() {
			Console.WriteLine("Test case 78");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test79() {
			Console.WriteLine("Test case 79");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Pickup("B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test80() {
			Console.WriteLine("Test case 80");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
		}

	[Test]
		public void Test81() {
			Console.WriteLine("Test case 81");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test82() {
			Console.WriteLine("Test case 82");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test83() {
			Console.WriteLine("Test case 83");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test84() {
			Console.WriteLine("Test case 84");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test85() {
			Console.WriteLine("Test case 85");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test86() {
			Console.WriteLine("Test case 86");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test87() {
			Console.WriteLine("Test case 87");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test88() {
			Console.WriteLine("Test case 88");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test89() {
			Console.WriteLine("Test case 89");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test90() {
			Console.WriteLine("Test case 90");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test91() {
			Console.WriteLine("Test case 91");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test92() {
			Console.WriteLine("Test case 92");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test93() {
			Console.WriteLine("Test case 93");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test94() {
			Console.WriteLine("Test case 94");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test95() {
			Console.WriteLine("Test case 95");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test96() {
			Console.WriteLine("Test case 96");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test97() {
			Console.WriteLine("Test case 97");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test98() {
			Console.WriteLine("Test case 98");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Pickup("B2");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test99() {
			Console.WriteLine("Test case 99");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test100() {
			Console.WriteLine("Test case 100");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test101() {
			Console.WriteLine("Test case 101");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B3"), "3_1_1_3_2");
			Assert.True(IsClear("B5"), "3_1_1_3_2");
			Assert.True(IsClear("B6"), "3_1_1_3_2");
			Assert.True(IsOn("B5", "B2"), "3_1_1_3_2");
			Assert.True(IsOntable("B2"), "3_1_1_3_2");
			Assert.True(IsOntable("B3"), "3_1_1_3_2");
			Assert.True(IsOntable("B6"), "3_1_1_3_2");
			block.Pickup("B3");
			Assert.True(IsClear("B5"), "3_1_1_3_2_1");
			Assert.True(IsClear("B6"), "3_1_1_3_2_1");
			Assert.True(IsHolding("B3"), "3_1_1_3_2_1");
			Assert.True(IsOn("B5", "B2"), "3_1_1_3_2_1");
			Assert.True(IsOntable("B2"), "3_1_1_3_2_1");
			Assert.True(IsOntable("B6"), "3_1_1_3_2_1");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test102() {
			Console.WriteLine("Test case 102");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test103() {
			Console.WriteLine("Test case 103");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
		}

	[Test]
		public void Test104() {
			Console.WriteLine("Test case 104");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test105() {
			Console.WriteLine("Test case 105");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test106() {
			Console.WriteLine("Test case 106");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test107() {
			Console.WriteLine("Test case 107");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			Assert.True(IsClear("B3"), "3_1_1_3_2_2");
			Assert.True(IsClear("B5"), "3_1_1_3_2_2");
			Assert.True(IsHolding("B6"), "3_1_1_3_2_2");
			Assert.True(IsOn("B5", "B2"), "3_1_1_3_2_2");
			Assert.True(IsOntable("B2"), "3_1_1_3_2_2");
			Assert.True(IsOntable("B3"), "3_1_1_3_2_2");
			block.Putdown("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test108() {
			Console.WriteLine("Test case 108");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test109() {
			Console.WriteLine("Test case 109");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test110() {
			Console.WriteLine("Test case 110");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Pickup("B3");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test111() {
			Console.WriteLine("Test case 111");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
		}

	[Test]
		public void Test112() {
			Console.WriteLine("Test case 112");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test113() {
			Console.WriteLine("Test case 113");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "3_1_1_3_2_3");
			Assert.True(IsClear("B3"), "3_1_1_3_2_3");
			Assert.True(IsClear("B6"), "3_1_1_3_2_3");
			Assert.True(IsHolding("B5"), "3_1_1_3_2_3");
			Assert.True(IsOntable("B2"), "3_1_1_3_2_3");
			Assert.True(IsOntable("B3"), "3_1_1_3_2_3");
			Assert.True(IsOntable("B6"), "3_1_1_3_2_3");
		}

	[Test]
		public void Test114() {
			Console.WriteLine("Test case 114");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B2"), "3_1_1_3_3");
			Assert.True(IsClear("B5"), "3_1_1_3_3");
			Assert.True(IsClear("B6"), "3_1_1_3_3");
			Assert.True(IsOn("B5", "B3"), "3_1_1_3_3");
			Assert.True(IsOntable("B2"), "3_1_1_3_3");
			Assert.True(IsOntable("B3"), "3_1_1_3_3");
			Assert.True(IsOntable("B6"), "3_1_1_3_3");
			block.Pickup("B2");
			Assert.True(IsClear("B5"), "3_1_1_3_3_1");
			Assert.True(IsClear("B6"), "3_1_1_3_3_1");
			Assert.True(IsHolding("B2"), "3_1_1_3_3_1");
			Assert.True(IsOn("B5", "B3"), "3_1_1_3_3_1");
			Assert.True(IsOntable("B3"), "3_1_1_3_3_1");
			Assert.True(IsOntable("B6"), "3_1_1_3_3_1");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test115() {
			Console.WriteLine("Test case 115");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test116() {
			Console.WriteLine("Test case 116");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test117() {
			Console.WriteLine("Test case 117");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B5");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test118() {
			Console.WriteLine("Test case 118");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test119() {
			Console.WriteLine("Test case 119");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test120() {
			Console.WriteLine("Test case 120");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "3_1_1_3_3_2");
			Assert.True(IsClear("B5"), "3_1_1_3_3_2");
			Assert.True(IsHolding("B6"), "3_1_1_3_3_2");
			Assert.True(IsOn("B5", "B3"), "3_1_1_3_3_2");
			Assert.True(IsOntable("B2"), "3_1_1_3_3_2");
			Assert.True(IsOntable("B3"), "3_1_1_3_3_2");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test121() {
			Console.WriteLine("Test case 121");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test122() {
			Console.WriteLine("Test case 122");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test123() {
			Console.WriteLine("Test case 123");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Pickup("B2");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test124() {
			Console.WriteLine("Test case 124");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Pickup("B2");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test125() {
			Console.WriteLine("Test case 125");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test126() {
			Console.WriteLine("Test case 126");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B3");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B2"), "3_1_1_3_3_3");
			Assert.True(IsClear("B3"), "3_1_1_3_3_3");
			Assert.True(IsClear("B6"), "3_1_1_3_3_3");
			Assert.True(IsHolding("B5"), "3_1_1_3_3_3");
			Assert.True(IsOntable("B2"), "3_1_1_3_3_3");
			Assert.True(IsOntable("B3"), "3_1_1_3_3_3");
			Assert.True(IsOntable("B6"), "3_1_1_3_3_3");
		}

	[Test]
		public void Test127() {
			Console.WriteLine("Test case 127");
			block.Unstack("B2", "B3");
			block.Putdown("B2");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B2"), "3_1_1_3_4");
			Assert.True(IsClear("B3"), "3_1_1_3_4");
			Assert.True(IsClear("B5"), "3_1_1_3_4");
			Assert.True(IsOn("B5", "B6"), "3_1_1_3_4");
			Assert.True(IsOntable("B2"), "3_1_1_3_4");
			Assert.True(IsOntable("B3"), "3_1_1_3_4");
			Assert.True(IsOntable("B6"), "3_1_1_3_4");
		}

	[Test]
		public void Test128() {
			Console.WriteLine("Test case 128");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "3_1_2");
			Assert.True(IsClear("B5"), "3_1_2");
			Assert.True(IsOn("B2", "B3"), "3_1_2");
			Assert.True(IsOn("B5", "B6"), "3_1_2");
			Assert.True(IsOntable("B3"), "3_1_2");
			Assert.True(IsOntable("B6"), "3_1_2");
		}

	[Test]
		public void Test129() {
			Console.WriteLine("Test case 129");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "3_1_3");
			Assert.True(IsClear("B3"), "3_1_3");
			Assert.True(IsOn("B2", "B5"), "3_1_3");
			Assert.True(IsOn("B5", "B6"), "3_1_3");
			Assert.True(IsOntable("B3"), "3_1_3");
			Assert.True(IsOntable("B6"), "3_1_3");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "3_1_3_1");
			Assert.True(IsHolding("B3"), "3_1_3_1");
			Assert.True(IsOn("B2", "B5"), "3_1_3_1");
			Assert.True(IsOn("B5", "B6"), "3_1_3_1");
			Assert.True(IsOntable("B6"), "3_1_3_1");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "3_1_3_1_1");
			Assert.True(IsClear("B3"), "3_1_3_1_1");
			Assert.True(IsOn("B2", "B5"), "3_1_3_1_1");
			Assert.True(IsOn("B5", "B6"), "3_1_3_1_1");
			Assert.True(IsOntable("B3"), "3_1_3_1_1");
			Assert.True(IsOntable("B6"), "3_1_3_1_1");
		}

	[Test]
		public void Test130() {
			Console.WriteLine("Test case 130");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "3_1_3_1_2");
			Assert.True(IsOn("B2", "B5"), "3_1_3_1_2");
			Assert.True(IsOn("B3", "B2"), "3_1_3_1_2");
			Assert.True(IsOn("B5", "B6"), "3_1_3_1_2");
			Assert.True(IsOntable("B6"), "3_1_3_1_2");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "3_1_3_1_2_1");
			Assert.True(IsHolding("B3"), "3_1_3_1_2_1");
			Assert.True(IsOn("B2", "B5"), "3_1_3_1_2_1");
			Assert.True(IsOn("B5", "B6"), "3_1_3_1_2_1");
			Assert.True(IsOntable("B6"), "3_1_3_1_2_1");
		}

	[Test]
		public void Test131() {
			Console.WriteLine("Test case 131");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B3"), "3_1_3_2");
			Assert.True(IsClear("B5"), "3_1_3_2");
			Assert.True(IsHolding("B2"), "3_1_3_2");
			Assert.True(IsOn("B5", "B6"), "3_1_3_2");
			Assert.True(IsOntable("B3"), "3_1_3_2");
			Assert.True(IsOntable("B6"), "3_1_3_2");
		}

	[Test]
		public void Test132() {
			Console.WriteLine("Test case 132");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B2"), "3_2");
			Assert.True(IsClear("B6"), "3_2");
			Assert.True(IsHolding("B5"), "3_2");
			Assert.True(IsOn("B2", "B3"), "3_2");
			Assert.True(IsOntable("B3"), "3_2");
			Assert.True(IsOntable("B6"), "3_2");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "3_2_1");
			Assert.True(IsClear("B5"), "3_2_1");
			Assert.True(IsClear("B6"), "3_2_1");
			Assert.True(IsOn("B2", "B3"), "3_2_1");
			Assert.True(IsOntable("B3"), "3_2_1");
			Assert.True(IsOntable("B5"), "3_2_1");
			Assert.True(IsOntable("B6"), "3_2_1");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "3_2_1_1");
			Assert.True(IsClear("B6"), "3_2_1_1");
			Assert.True(IsHolding("B5"), "3_2_1_1");
			Assert.True(IsOn("B2", "B3"), "3_2_1_1");
			Assert.True(IsOntable("B3"), "3_2_1_1");
			Assert.True(IsOntable("B6"), "3_2_1_1");
		}

	[Test]
		public void Test133() {
			Console.WriteLine("Test case 133");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "3_2_1_2");
			Assert.True(IsClear("B5"), "3_2_1_2");
			Assert.True(IsHolding("B6"), "3_2_1_2");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2");
			Assert.True(IsOntable("B3"), "3_2_1_2");
			Assert.True(IsOntable("B5"), "3_2_1_2");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "3_2_1_2_1");
			Assert.True(IsClear("B5"), "3_2_1_2_1");
			Assert.True(IsClear("B6"), "3_2_1_2_1");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2_1");
			Assert.True(IsOntable("B3"), "3_2_1_2_1");
			Assert.True(IsOntable("B5"), "3_2_1_2_1");
			Assert.True(IsOntable("B6"), "3_2_1_2_1");
		}

	[Test]
		public void Test134() {
			Console.WriteLine("Test case 134");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B5"), "3_2_1_2_2");
			Assert.True(IsClear("B6"), "3_2_1_2_2");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2_2");
			Assert.True(IsOn("B6", "B2"), "3_2_1_2_2");
			Assert.True(IsOntable("B3"), "3_2_1_2_2");
			Assert.True(IsOntable("B5"), "3_2_1_2_2");
			block.Pickup("B5");
			Assert.True(IsClear("B6"), "3_2_1_2_2_1");
			Assert.True(IsHolding("B5"), "3_2_1_2_2_1");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2_2_1");
			Assert.True(IsOn("B6", "B2"), "3_2_1_2_2_1");
			Assert.True(IsOntable("B3"), "3_2_1_2_2_1");
			block.Putdown("B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test135() {
			Console.WriteLine("Test case 135");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B5");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B5", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			block.Unstack("B5", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B3"), "");
		}

	[Test]
		public void Test136() {
			Console.WriteLine("Test case 136");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "3_2_1_2_2_2");
			Assert.True(IsClear("B5"), "3_2_1_2_2_2");
			Assert.True(IsHolding("B6"), "3_2_1_2_2_2");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2_2_2");
			Assert.True(IsOntable("B3"), "3_2_1_2_2_2");
			Assert.True(IsOntable("B5"), "3_2_1_2_2_2");
		}

	[Test]
		public void Test137() {
			Console.WriteLine("Test case 137");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B2"), "3_2_1_2_3");
			Assert.True(IsClear("B6"), "3_2_1_2_3");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2_3");
			Assert.True(IsOn("B6", "B5"), "3_2_1_2_3");
			Assert.True(IsOntable("B3"), "3_2_1_2_3");
			Assert.True(IsOntable("B5"), "3_2_1_2_3");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "3_2_1_2_3_1");
			Assert.True(IsClear("B6"), "3_2_1_2_3_1");
			Assert.True(IsHolding("B2"), "3_2_1_2_3_1");
			Assert.True(IsOn("B6", "B5"), "3_2_1_2_3_1");
			Assert.True(IsOntable("B3"), "3_2_1_2_3_1");
			Assert.True(IsOntable("B5"), "3_2_1_2_3_1");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test138() {
			Console.WriteLine("Test case 138");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B3"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test139() {
			Console.WriteLine("Test case 139");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test140() {
			Console.WriteLine("Test case 140");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test141() {
			Console.WriteLine("Test case 141");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B6", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test142() {
			Console.WriteLine("Test case 142");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B2"), "3_2_1_2_3_2");
			Assert.True(IsClear("B5"), "3_2_1_2_3_2");
			Assert.True(IsHolding("B6"), "3_2_1_2_3_2");
			Assert.True(IsOn("B2", "B3"), "3_2_1_2_3_2");
			Assert.True(IsOntable("B3"), "3_2_1_2_3_2");
			Assert.True(IsOntable("B5"), "3_2_1_2_3_2");
		}

	[Test]
		public void Test143() {
			Console.WriteLine("Test case 143");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			Assert.True(IsClear("B3"), "3_2_1_3");
			Assert.True(IsClear("B5"), "3_2_1_3");
			Assert.True(IsClear("B6"), "3_2_1_3");
			Assert.True(IsHolding("B2"), "3_2_1_3");
			Assert.True(IsOntable("B3"), "3_2_1_3");
			Assert.True(IsOntable("B5"), "3_2_1_3");
			Assert.True(IsOntable("B6"), "3_2_1_3");
			block.Putdown("B2");
			Assert.True(IsClear("B2"), "3_2_1_3_1");
			Assert.True(IsClear("B3"), "3_2_1_3_1");
			Assert.True(IsClear("B5"), "3_2_1_3_1");
			Assert.True(IsClear("B6"), "3_2_1_3_1");
			Assert.True(IsOntable("B2"), "3_2_1_3_1");
			Assert.True(IsOntable("B3"), "3_2_1_3_1");
			Assert.True(IsOntable("B5"), "3_2_1_3_1");
			Assert.True(IsOntable("B6"), "3_2_1_3_1");
		}

	[Test]
		public void Test144() {
			Console.WriteLine("Test case 144");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B3");
			Assert.True(IsClear("B2"), "3_2_1_3_2");
			Assert.True(IsClear("B5"), "3_2_1_3_2");
			Assert.True(IsClear("B6"), "3_2_1_3_2");
			Assert.True(IsOn("B2", "B3"), "3_2_1_3_2");
			Assert.True(IsOntable("B3"), "3_2_1_3_2");
			Assert.True(IsOntable("B5"), "3_2_1_3_2");
			Assert.True(IsOntable("B6"), "3_2_1_3_2");
		}

	[Test]
		public void Test145() {
			Console.WriteLine("Test case 145");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			Assert.True(IsClear("B2"), "3_2_1_3_3");
			Assert.True(IsClear("B3"), "3_2_1_3_3");
			Assert.True(IsClear("B6"), "3_2_1_3_3");
			Assert.True(IsOn("B2", "B5"), "3_2_1_3_3");
			Assert.True(IsOntable("B3"), "3_2_1_3_3");
			Assert.True(IsOntable("B5"), "3_2_1_3_3");
			Assert.True(IsOntable("B6"), "3_2_1_3_3");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "3_2_1_3_3_1");
			Assert.True(IsClear("B6"), "3_2_1_3_3_1");
			Assert.True(IsHolding("B3"), "3_2_1_3_3_1");
			Assert.True(IsOn("B2", "B5"), "3_2_1_3_3_1");
			Assert.True(IsOntable("B5"), "3_2_1_3_3_1");
			Assert.True(IsOntable("B6"), "3_2_1_3_3_1");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test146() {
			Console.WriteLine("Test case 146");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test147() {
			Console.WriteLine("Test case 147");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test148() {
			Console.WriteLine("Test case 148");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test149() {
			Console.WriteLine("Test case 149");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test150() {
			Console.WriteLine("Test case 150");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test151() {
			Console.WriteLine("Test case 151");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			Assert.True(IsClear("B2"), "3_2_1_3_3_2");
			Assert.True(IsClear("B3"), "3_2_1_3_3_2");
			Assert.True(IsHolding("B6"), "3_2_1_3_3_2");
			Assert.True(IsOn("B2", "B5"), "3_2_1_3_3_2");
			Assert.True(IsOntable("B3"), "3_2_1_3_3_2");
			Assert.True(IsOntable("B5"), "3_2_1_3_3_2");
			block.Putdown("B6");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test152() {
			Console.WriteLine("Test case 152");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Pickup("B3");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test153() {
			Console.WriteLine("Test case 153");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B3", "B6"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B3", "B6");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B6", "B2"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test154() {
			Console.WriteLine("Test case 154");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B2");
			block.Unstack("B6", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test155() {
			Console.WriteLine("Test case 155");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B6", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test156() {
			Console.WriteLine("Test case 156");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Pickup("B6");
			block.Stack("B6", "B3");
			block.Unstack("B6", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B6"), "");
			Assert.True(IsOn("B2", "B5"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
		}

	[Test]
		public void Test157() {
			Console.WriteLine("Test case 157");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B5");
			block.Unstack("B2", "B5");
			Assert.True(IsClear("B3"), "3_2_1_3_3_3");
			Assert.True(IsClear("B5"), "3_2_1_3_3_3");
			Assert.True(IsClear("B6"), "3_2_1_3_3_3");
			Assert.True(IsHolding("B2"), "3_2_1_3_3_3");
			Assert.True(IsOntable("B3"), "3_2_1_3_3_3");
			Assert.True(IsOntable("B5"), "3_2_1_3_3_3");
			Assert.True(IsOntable("B6"), "3_2_1_3_3_3");
		}

	[Test]
		public void Test158() {
			Console.WriteLine("Test case 158");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			Assert.True(IsClear("B2"), "3_2_1_3_4");
			Assert.True(IsClear("B3"), "3_2_1_3_4");
			Assert.True(IsClear("B5"), "3_2_1_3_4");
			Assert.True(IsOn("B2", "B6"), "3_2_1_3_4");
			Assert.True(IsOntable("B3"), "3_2_1_3_4");
			Assert.True(IsOntable("B5"), "3_2_1_3_4");
			Assert.True(IsOntable("B6"), "3_2_1_3_4");
			block.Pickup("B3");
			Assert.True(IsClear("B2"), "3_2_1_3_4_1");
			Assert.True(IsClear("B5"), "3_2_1_3_4_1");
			Assert.True(IsHolding("B3"), "3_2_1_3_4_1");
			Assert.True(IsOn("B2", "B6"), "3_2_1_3_4_1");
			Assert.True(IsOntable("B5"), "3_2_1_3_4_1");
			Assert.True(IsOntable("B6"), "3_2_1_3_4_1");
			block.Putdown("B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test159() {
			Console.WriteLine("Test case 159");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test160() {
			Console.WriteLine("Test case 160");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B5", "B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test161() {
			Console.WriteLine("Test case 161");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B2");
			block.Unstack("B3", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test162() {
			Console.WriteLine("Test case 162");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B6"), "");
			Assert.True(IsHolding("B2"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test163() {
			Console.WriteLine("Test case 163");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test164() {
			Console.WriteLine("Test case 164");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B5");
			Assert.True(IsClear("B2"), "3_2_1_3_4_2");
			Assert.True(IsClear("B3"), "3_2_1_3_4_2");
			Assert.True(IsHolding("B5"), "3_2_1_3_4_2");
			Assert.True(IsOn("B2", "B6"), "3_2_1_3_4_2");
			Assert.True(IsOntable("B3"), "3_2_1_3_4_2");
			Assert.True(IsOntable("B6"), "3_2_1_3_4_2");
			block.Putdown("B5");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B5"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test165() {
			Console.WriteLine("Test case 165");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
			block.Pickup("B3");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Putdown("B3");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test166() {
			Console.WriteLine("Test case 166");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			block.Pickup("B3");
			block.Stack("B3", "B5");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B3", "B5"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B6"), "");
			block.Unstack("B3", "B5");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsHolding("B3"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B2"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test167() {
			Console.WriteLine("Test case 167");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B2");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B3"), "");
			Assert.True(IsHolding("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test168() {
			Console.WriteLine("Test case 168");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Pickup("B5");
			block.Stack("B5", "B3");
			Assert.True(IsClear("B2"), "");
			Assert.True(IsClear("B5"), "");
			Assert.True(IsOn("B2", "B6"), "");
			Assert.True(IsOn("B5", "B3"), "");
			Assert.True(IsOntable("B3"), "");
			Assert.True(IsOntable("B6"), "");
		}

	[Test]
		public void Test169() {
			Console.WriteLine("Test case 169");
			block.Unstack("B5", "B6");
			block.Putdown("B5");
			block.Unstack("B2", "B3");
			block.Stack("B2", "B6");
			block.Unstack("B2", "B6");
			Assert.True(IsClear("B3"), "3_2_1_3_4_3");
			Assert.True(IsClear("B5"), "3_2_1_3_4_3");
			Assert.True(IsClear("B6"), "3_2_1_3_4_3");
			Assert.True(IsHolding("B2"), "3_2_1_3_4_3");
			Assert.True(IsOntable("B3"), "3_2_1_3_4_3");
			Assert.True(IsOntable("B5"), "3_2_1_3_4_3");
			Assert.True(IsOntable("B6"), "3_2_1_3_4_3");
		}

	[Test]
		public void Test170() {
			Console.WriteLine("Test case 170");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			Assert.True(IsClear("B5"), "3_2_2");
			Assert.True(IsClear("B6"), "3_2_2");
			Assert.True(IsOn("B2", "B3"), "3_2_2");
			Assert.True(IsOn("B5", "B2"), "3_2_2");
			Assert.True(IsOntable("B3"), "3_2_2");
			Assert.True(IsOntable("B6"), "3_2_2");
			block.Pickup("B6");
			Assert.True(IsClear("B5"), "3_2_2_1");
			Assert.True(IsHolding("B6"), "3_2_2_1");
			Assert.True(IsOn("B2", "B3"), "3_2_2_1");
			Assert.True(IsOn("B5", "B2"), "3_2_2_1");
			Assert.True(IsOntable("B3"), "3_2_2_1");
			block.Putdown("B6");
			Assert.True(IsClear("B5"), "3_2_2_1_1");
			Assert.True(IsClear("B6"), "3_2_2_1_1");
			Assert.True(IsOn("B2", "B3"), "3_2_2_1_1");
			Assert.True(IsOn("B5", "B2"), "3_2_2_1_1");
			Assert.True(IsOntable("B3"), "3_2_2_1_1");
			Assert.True(IsOntable("B6"), "3_2_2_1_1");
		}

	[Test]
		public void Test171() {
			Console.WriteLine("Test case 171");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Pickup("B6");
			block.Stack("B6", "B5");
			Assert.True(IsClear("B6"), "3_2_2_1_2");
			Assert.True(IsOn("B2", "B3"), "3_2_2_1_2");
			Assert.True(IsOn("B5", "B2"), "3_2_2_1_2");
			Assert.True(IsOn("B6", "B5"), "3_2_2_1_2");
			Assert.True(IsOntable("B3"), "3_2_2_1_2");
			block.Unstack("B6", "B5");
			Assert.True(IsClear("B5"), "3_2_2_1_2_1");
			Assert.True(IsHolding("B6"), "3_2_2_1_2_1");
			Assert.True(IsOn("B2", "B3"), "3_2_2_1_2_1");
			Assert.True(IsOn("B5", "B2"), "3_2_2_1_2_1");
			Assert.True(IsOntable("B3"), "3_2_2_1_2_1");
		}

	[Test]
		public void Test172() {
			Console.WriteLine("Test case 172");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B2");
			block.Unstack("B5", "B2");
			Assert.True(IsClear("B2"), "3_2_2_2");
			Assert.True(IsClear("B6"), "3_2_2_2");
			Assert.True(IsHolding("B5"), "3_2_2_2");
			Assert.True(IsOn("B2", "B3"), "3_2_2_2");
			Assert.True(IsOntable("B3"), "3_2_2_2");
			Assert.True(IsOntable("B6"), "3_2_2_2");
		}

	[Test]
		public void Test173() {
			Console.WriteLine("Test case 173");
			block.Unstack("B5", "B6");
			block.Stack("B5", "B6");
			Assert.True(IsClear("B2"), "3_2_3");
			Assert.True(IsClear("B5"), "3_2_3");
			Assert.True(IsOn("B2", "B3"), "3_2_3");
			Assert.True(IsOn("B5", "B6"), "3_2_3");
			Assert.True(IsOntable("B3"), "3_2_3");
			Assert.True(IsOntable("B6"), "3_2_3");
		}

}	

} // End of namespace