package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UDPString {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2208;
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
            int semiIdx = response.indexOf(';');
            if (semiIdx != -1) {
                String requestId = response.substring(0, semiIdx);
                String data = response.substring(semiIdx + 1);

                Map<Character, List<Integer>> posMap = new LinkedHashMap<>();
                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);
                    posMap.computeIfAbsent(c, k -> new ArrayList<>()).add(i + 1);
                }

                char maxChar = ' ';
                int maxCount = 0;
                for (Map.Entry<Character, List<Integer>> entry : posMap.entrySet()) {
                    if (entry.getValue().size() > maxCount) {
                        maxCount = entry.getValue().size();
                        maxChar = entry.getKey();
                    }
                }

                StringBuilder posStr = new StringBuilder();
                for (int pos : posMap.get(maxChar)) {
                    posStr.append(pos).append(",");
                }

                String result = requestId + ";" + maxChar + ":" + posStr.toString();
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