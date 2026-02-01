from pyngrok import ngrok
import sys
import time

try:
    # Check for existing tunnels
    existing_tunnels = ngrok.get_tunnels()
    if existing_tunnels:
        public_url = existing_tunnels[0].public_url
        print(f" * Existing ngrok tunnel found: {public_url}")
    else:
        # Create new tunnel
        public_url = ngrok.connect(5000).public_url
        print(f" * New ngrok tunnel created: {public_url}")

    # Keep alive
    while True:
        time.sleep(1)

except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
