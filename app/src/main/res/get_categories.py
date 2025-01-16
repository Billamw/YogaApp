import requests

# URL of the API endpoint
url = "https://yoga-api-nzy4.onrender.com/v1/categories"

try:
    # Send a GET request to the API
    response = requests.get(url)
    
    # Check if the response status code is 200 (OK)
    if response.status_code == 200:
        # Parse the JSON response
        data = response.json()
        
        # Print the JSON data
        print("Fetched JSON data:")
        print(data)
        for category in data:
            print(category["category_name"])
    else:
        print(f"Failed to fetch data. Status code: {response.status_code}")
except requests.exceptions.RequestException as e:
    print(f"An error occurred: {e}")
