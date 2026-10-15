package UDP;

import UDP.Employee;
import java.io.*;
import java.net.*;
import java.io.Serializable;

public class UDPObject {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2209;
        String studentCode = "B23DCCN731";
        String qCode = "EE29C059";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(host);

            String request = ";" + studentCode + ";" + qCode;
            byte[] sendReq = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendReq, sendReq.length, address, port);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            byte[] fullData = receivePacket.getData();
            int totalLen = receivePacket.getLength();

            byte[] reqIdBytes = new byte[8];
            System.arraycopy(fullData, 0, reqIdBytes, 0, 8);

            ByteArrayInputStream bais = new ByteArrayInputStream(fullData, 8, totalLen - 8);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Employee emp = (Employee) ois.readObject();

            String[] words = emp.getName().trim().toLowerCase().split("\\s+");
            StringBuilder normName = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (!words[i].isEmpty()) {
                    normName.append(Character.toUpperCase(words[i].charAt(0)))
                            .append(words[i].substring(1));
                    if (i < words.length - 1) normName.append(" ");
                }
            }
            emp.setName(normName.toString());

            String rawDate = emp.getHireDate().trim();
            String[] dateParts = rawDate.split("-");
            String year = dateParts[0];
            int sumDigitsYear = 0;
            for (char ch : year.toCharArray()) {
                if (Character.isDigit(ch)) {
                    sumDigitsYear += ch - '0';
                }
            }

            double newSalary = emp.getSalary() * (1.0 + (double) sumDigitsYear / 100.0);
            newSalary = Math.round(newSalary * 100.0) / 100.0;
            emp.setSalary(newSalary);

            String newDate = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
            emp.setHireDate(newDate);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(emp);
            oos.flush();
            byte[] objBytes = baos.toByteArray();

            byte[] sendBackData = new byte[8 + objBytes.length];
            System.arraycopy(reqIdBytes, 0, sendBackData, 0, 8);
            System.arraycopy(objBytes, 0, sendBackData, 8, objBytes.length);

            DatagramPacket resultPacket = new DatagramPacket(
                    sendBackData,
                    sendBackData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort()
            );
            socket.send(resultPacket);

        } catch (Exception e) {
        }
    }
}
class Employee implements Serializable {
    private static final long serialVersionUID = 20261107L;

    private String id;
    private String name;
    private double salary;
    private String hireDate;

    public Employee(String id, String name, double salary, String hireDate) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }
}