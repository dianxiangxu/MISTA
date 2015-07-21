//Warning: The same setUp method is used for multiple initial states.

using Blocks;
using System;
using NUnit.Framework;

namespace test {

[TestFixture()]
	public class BlockTester_TC {

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
			Assert.True(block.IsClear("B6"), "1_1");
			Assert.True(block.IsHolding("B3"), "1_1");
			Assert.True(block.IsOntable("B6"), "1_1");
			block.Putdown("B3");
			Assert.True(block.IsClear("B3"), "1_1_1");
			Assert.True(block.IsClear("B6"), "1_1_1");
			Assert.True(block.IsOntable("B3"), "1_1_1");
			Assert.True(block.IsOntable("B6"), "1_1_1");
		}

	[Test]
		public void Test2() {
			Console.WriteLine("Test case 2");
			block.Pickup("B3");
			block.Stack("B3", "B6");
			Assert.True(block.IsClear("B3"), "1_1_2");
			Assert.True(block.IsOn("B3", "B6"), "1_1_2");
			Assert.True(block.IsOntable("B6"), "1_1_2");
		}

	[Test]
		public void Test3() {
			Console.WriteLine("Test case 3");
			block.Unstack("B1", "B3");
			Assert.True(block.IsClear("B3"), "2_1");
			Assert.True(block.IsClear("B6"), "2_1");
			Assert.True(block.IsHolding("B1"), "2_1");
			Assert.True(block.IsOntable("B3"), "2_1");
			Assert.True(block.IsOntable("B6"), "2_1");
		}

}	

} // End of namespace