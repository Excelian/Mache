var http = require('http')
var assert = require('assert');

var base_url = "http://localhost:8080/map"

getShouldFailUnknownKey = function () {
    http.request(base_url + "/unknown/2", function(res) {
        assert.equal(404, res.statusCode)
    }).end();
}();

putAndGetShouldReturnSameValue = function () {
    var requestData = { 'make': 'honda', 'model': 'accord' }

    var putRequest = http.request({
        hostname: 'localhost',
        port: 8080,
        path: '/map/cars/1',
        method: "PUT",
        headers: {
            "content-type": "application/json",
        }
    }, function(res) {
        // Once put complete send the get
        assert.equal(200, res.statusCode)

        http.request({
            hostname: 'localhost',
            port: 8080,
            path: '/map/cars/1'
        }, function(resGet) {
            assert.equal(200, resGet.statusCode)

            var body = '';
            resGet.on('data', function (chunk) {
                body += chunk;
            });
            resGet.on('end', function () {
                var result = JSON.parse(body)
                assert.equal('honda', result.make)
            });
        }).end();
    })
    putRequest.write(JSON.stringify(requestData))
    putRequest.end();
}();
