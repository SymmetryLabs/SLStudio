from scapy.all import sniff, UDP, IP
import binascii

ARTNET_PORT = 6454

def handle_packet(packet):
    if UDP in packet and (packet[UDP].sport == ARTNET_PORT or packet[UDP].dport == ARTNET_PORT):
        src = packet[IP].src
        dst = packet[IP].dst
        sport = packet[UDP].sport
        dport = packet[UDP].dport
        payload = bytes(packet[UDP].payload)
        print(f"\nArt-Net packet: {src}:{sport} -> {dst}:{dport}")
        print(f"Raw data ({len(payload)} bytes):\n{binascii.hexlify(payload).decode('ascii')}")

print(f"Sniffing Art-Net UDP packets on port {ARTNET_PORT} (inbound & outbound)...")
print("Press Ctrl+C to stop.")

sniff(filter=f"udp port {ARTNET_PORT}", prn=handle_packet, store=0)
