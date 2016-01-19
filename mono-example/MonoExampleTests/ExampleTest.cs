using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;

namespace MonoExampleTests
{
    [TestFixture]
    public class ExampleTest
    {
        [Test]
        public void MyTest()
        {
            int a = 10;
            Assert.AreEqual(10, a);
        }
    }
}
