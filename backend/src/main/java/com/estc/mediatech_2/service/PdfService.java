package com.estc.mediatech_2.service;

import com.estc.mediatech_2.models.FactureEntity;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Service
public class PdfService {

    public ByteArrayInputStream factureReport(FactureEntity facture) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Header
            Font font = FontFactory.getFont(FontFactory.COURIER, 14, BaseColor.BLACK);
            Paragraph para = new Paragraph("Facture: " + facture.getRef_facture(), font);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);
            document.add(Chunk.NEWLINE);

            // Client Info
            document.add(new Paragraph(
                    "Client: " + facture.getClient().getNom_client() + " " + facture.getClient().getPrenom_client()));
            document.add(new Paragraph("Date: " + facture.getDate_facture().toString()));
            document.add(Chunk.NEWLINE);

            // Table
            PdfPTable table = new PdfPTable(4);
            Stream.of("Produit", "Quantité", "Prix Unitaire", "Total")
                    .forEach(headerTitle -> {
                        PdfPCell header = new PdfPCell();
                        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setBorderWidth(2);
                        header.setPhrase(new Phrase(headerTitle, headFont));
                        table.addCell(header);
                    });

            facture.getLigneFactures().forEach(ligne -> {
                table.addCell(ligne.getProduit().getLibelle_produit());
                table.addCell(ligne.getQuantite().toString());
                table.addCell(ligne.getProduit().getPrix_unitaire().toString());
                table.addCell(ligne.getProduit().getPrix_unitaire()
                        .multiply(new java.math.BigDecimal(ligne.getQuantite())).toString());
            });

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
