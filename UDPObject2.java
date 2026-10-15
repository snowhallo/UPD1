package UDP;
import java.io.*;
import java.net.*;
import java.io.Serializable;


public class UDPObject2 {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2209;
        String studentCode = "B23DCCN731";
        String qCode = "eQkvAeId";

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
            Book book = (Book) ois.readObject();

            String[] titleWords = book.getTitle().trim().toLowerCase().split("\\s+");
            StringBuilder normTitle = new StringBuilder();
            for (int i = 0; i < titleWords.length; i++) {
                if (!titleWords[i].isEmpty()) {
                    normTitle.append(Character.toUpperCase(titleWords[i].charAt(0)))
                             .append(titleWords[i].substring(1));
                    if (i < titleWords.length - 1) normTitle.append(" ");
                }
            }
            book.setTitle(normTitle.toString());

            String[] authorWords = book.getAuthor().trim().toLowerCase().split("\\s+");
            String ho = authorWords[0].toUpperCase();
            StringBuilder ten = new StringBuilder();
            for (int i = 1; i < authorWords.length; i++) {
                if (!authorWords[i].isEmpty()) {
                    ten.append(Character.toUpperCase(authorWords[i].charAt(0)))
                       .append(authorWords[i].substring(1));
                    if (i < authorWords.length - 1) ten.append(" ");
                }
            }
            book.setAuthor(ho + ", " + ten.toString());

            String rawIsbn = book.getIsbn().replaceAll("[^0-9]", "");
            if (rawIsbn.length() == 13) {
                String formattedIsbn = rawIsbn.substring(0, 3) + "-"
                        + rawIsbn.substring(3, 4) + "-"
                        + rawIsbn.substring(4, 6) + "-"
                        + rawIsbn.substring(6, 12) + "-"
                        + rawIsbn.substring(12, 13);
                book.setIsbn(formattedIsbn);
            }

            String[] dateParts = book.getPublishDate().trim().split("-");
            if (dateParts.length == 3) {
                book.setPublishDate(dateParts[1] + "/" + dateParts[0]);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(book);
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

class Book implements Serializable {
    private static final long serialVersionUID = 20251107L;

    private String id;
    private String title;
    private String author;
    private String isbn;
    private String publishDate;

    public Book(String id, String title, String author, String isbn, String publishDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishDate = publishDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
}