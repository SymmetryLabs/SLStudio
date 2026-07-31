from pythonosc import dispatcher
from pythonosc import osc_server

# Define what happens when any OSC message arrives
def default_handler(address, *args):
    print(f"DEBUG: {address}: {args}")

if __name__ == "__main__":
    ip = "127.0.0.1"
    port = 7000

    # Set up the dispatcher to catch all paths
    disp = dispatcher.Dispatcher()
    disp.set_default_handler(default_handler)

    # Start the server
    server = osc_server.ThreadingOSCUDPServer((ip, port), disp)
    print(f"Serving on {server.server_address}")
    
    # Keep the server running to detect incoming messages
    server.serve_forever()