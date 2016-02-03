import json
import requests
import unittest

BASE_URL = "http://localhost:8080/map"


class MacheRestTest(unittest.TestCase):
    def test_get_404_missing_value(self):
        response = requests.get(BASE_URL + '/missing/no_key')
        self.assertEqual(404, response.status_code)

    def test_can_put_and_get_value(self):
        car = {'make': 'ford', 'model': 'mondeo', 'color': 'red'}
        response = requests.put(BASE_URL + '/car/1', data=json.dumps(car))
        self.assertEqual(200, response.status_code)

        response = requests.get(BASE_URL + '/car/1')
        self.assertEqual(200, response.status_code)

        self.assertEqual('ford', response.json()['make'])
        self.assertEqual('mondeo', response.json()['model'])

    def test_can_delete_key(self):
        car = {'make': 'mazda', 'model': '3', 'color': 'blue'}
        response = requests.put(BASE_URL + '/car/2', data=json.dumps(car))
        self.assertEqual(200, response.status_code)

        response = requests.delete(BASE_URL + '/car/2')
        self.assertEqual(200, response.status_code)


if __name__ == '__main__':
    unittest.main()
