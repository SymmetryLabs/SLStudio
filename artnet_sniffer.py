import socket
import binascii

ARTNET_PORT = 6454
BUFFER_SIZE = 1024  # Art-Net packets are much smaller, but just in case

print("Listening for Art-Net packets on UDP port 6454...")

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(("", ARTNET_PORT))

try:
    while True:
        data, addr = sock.recvfrom(BUFFER_SIZE)
        ip, port = addr
        print(f"\nReceived Art-Net packet from {ip}:{port}")
        print(f"Raw data ({len(data)} bytes):\n{binascii.hexlify(data).decode('ascii')}")
        # Optionally, parse Art-Net header/universe here
except KeyboardInterrupt:
    print("\nExiting Art-Net sniffer.")
finally:
    sock.close()
