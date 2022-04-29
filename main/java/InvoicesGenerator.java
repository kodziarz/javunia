import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class InvoicesGenerator {

    public static String generateInvoiceForCar(String buyer, String seller, App.Car car) {

        Document document = new Document(); // dokument pdf
        String path = "invoices/plik_" + System.currentTimeMillis() + ".pdf"; // lokalizacja zapisu
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();
        Font font1 = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Paragraph buyerParagraph = new Paragraph("Nabywca: " + buyer, font1); // akapit
        Paragraph sellerParagraph = new Paragraph("Sprzedawca: " + seller, font1);

        // faktura za co
        Font font2 = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.RED);
        Chunk subtitle = new Chunk("Faktura za auto: " + car.getModel(), font2);

        try {
            document.add(buyerParagraph);
            document.add(sellerParagraph);
            document.add(subtitle);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        //   tabelka

        PdfPTable table = new PdfPTable(4);

        // linia nagłówków
        Font tableFont = FontFactory.getFont(FontFactory.COURIER, 14, BaseColor.BLACK);


        PdfPCell c = new PdfPCell(new Phrase("lp", tableFont));
        table.addCell(c);
        c = new PdfPCell(new Phrase("cena", tableFont));
        table.addCell(c);
        c = new PdfPCell(new Phrase("vat", tableFont));
        table.addCell(c);
        c = new PdfPCell(new Phrase("wartość", tableFont));
        table.addCell(c);

        // drugi rząd
        c = new PdfPCell(new Phrase("1", tableFont));
        table.addCell(c);
        c = new PdfPCell(new Phrase(car.getPrice(), tableFont));
        table.addCell(c);
        c = new PdfPCell(new Phrase(car.getVat(), tableFont));
        table.addCell(c);
        c = new PdfPCell(new Phrase(car.getValue(), tableFont));
        table.addCell(c);




        try {
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        document.close();
        System.out.println("wykonało się");

        return "tak";
    }
}
