import requests

def test_discovery():
    url = "http://localhost:5000/discover_devices"
    try:
        response = requests.get(url)
        print(f"Status: {response.status_code}")
        print(response.json())
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_discovery()
