using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using RestSharp;
using System.Net;

namespace MonoExampleTests
{
    [TestFixture]
    public class MacheRemoteTest
    {
		private const string BASE_URL = "http://localhost:8080/map";

        [Test]
        public void GetShouldReturnExpectedKey()
		{
			var client = new RestClient (BASE_URL);
			var response = PutCar (client);

			Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);

			var getRequest = new RestRequest ("cars/1", Method.GET);
			IRestResponse getResponse = client.Execute (getRequest);

			Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
			Assert.True (getResponse.Content.Contains ("nissan"));
		}

		[Test]
		public void DeleteShouldRemoveKey()
		{
			var client = new RestClient (BASE_URL);
			var putResponse = PutCar (client);

			Assert.AreEqual(HttpStatusCode.OK, putResponse.StatusCode);

			var deleteRequest = new RestRequest ("cars/1", Method.DELETE);
			IRestResponse deleteResponse = client.Execute(deleteRequest);

			Assert.AreEqual(HttpStatusCode.OK, deleteResponse.StatusCode);

			// Ensure not available
			var getRequest = new RestRequest ("cars/1", Method.GET);
			IRestResponse getResponse = client.Execute (getRequest);

			Assert.AreEqual(HttpStatusCode.BadRequest, getResponse.StatusCode);
		}

		private IRestResponse PutCar(RestClient client)
		{
			var putRequest = new RestRequest ("cars/1", Method.PUT);
			putRequest.AddParameter ("text/json", "{'make': 'nissan', 'model': 'primera'}", ParameterType.RequestBody);
			return client.Execute (putRequest);
		}
    }
}
