package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPData {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2207;
        String studentCode = "B23DCCN731";
        String qCode = "EE29C059";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(host);

            String request = ";" + studentCode + ";" + qCode;
            byte[] sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            String[] parts = response.split(";");
            if (parts.length >= 2) {
                String requestId = parts[0];
                String[] nums = parts[1].split(",");

                long checksum = 0;
                for (int i = 0; i < nums.length; i++) {
                    long val = Long.parseLong(nums[i].trim());
                    checksum = (checksum + (long) (i + 1) * val) % 100000;
                }

                String result = requestId + ";" + checksum;
                byte[] resultBytes = result.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        resultBytes,
                        resultBytes.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                );
                socket.send(responsePacket);
            }
        } catch (Exception e) {
        }
    }
}