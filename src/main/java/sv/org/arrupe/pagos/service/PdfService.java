/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sv.org.arrupe.pagos.model.Cartera;
import sv.org.arrupe.pagos.model.Transaccion;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.repository.CarteraRepository;
import sv.org.arrupe.pagos.repository.TransaccionRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 *
 * @author perez
 */
@Service
public class PdfService {

    @Autowired
    private CarteraRepository carteraRepository;
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    public byte[] generarEstadoDeCuentaPdf(Usuario usuario) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // --- Encabezado ---
        document.add(new Paragraph("Estado de Cuenta - Cartera Virtual")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
        document.add(new Paragraph("MarketCup Institucional")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));
        document.add(new Paragraph("Fecha de Emisión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginBottom(20));

        // --- Información del Usuario ---
        Cartera cartera = carteraRepository.findByAlumnoIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Cartera no encontrada para el usuario."));

        document.add(new Paragraph("Información del Titular").setFontSize(14).setBold().setMarginBottom(10));
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        infoTable.addCell(new Cell().add(new Paragraph("Nombre Completo:")).setBold());
        infoTable.addCell(usuario.getPrimerNombre() + " " + usuario.getPrimerApellido());
        infoTable.addCell(new Cell().add(new Paragraph("Carné:")).setBold());
        infoTable.addCell(usuario.getCarnet());
        infoTable.addCell(new Cell().add(new Paragraph("Correo:")).setBold());
        infoTable.addCell(usuario.getCorreo());
        infoTable.addCell(new Cell().add(new Paragraph("Saldo Actual:")).setBold().setFontColor(ColorConstants.BLUE));
        infoTable.addCell(new Paragraph(String.format("$%.2f", cartera.getSaldo())).setBold().setFontColor(ColorConstants.BLUE));
        document.add(infoTable);
        
        document.add(new Paragraph(" ").setMarginBottom(20)); // Espacio

        // --- Tabla de Transacciones ---
        document.add(new Paragraph("Historial de Transacciones").setFontSize(14).setBold().setMarginBottom(10));
        List<Transaccion> transacciones = transaccionRepository.findByCarteraIdCartera(cartera.getIdCartera());
        
        Table txTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 3})).useAllAvailableWidth();
        // Encabezados de la tabla
        txTable.addHeaderCell(new Cell().add(new Paragraph("Fecha")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBold());
        txTable.addHeaderCell(new Cell().add(new Paragraph("Tipo")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBold());
        txTable.addHeaderCell(new Cell().add(new Paragraph("Monto")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBold());
        txTable.addHeaderCell(new Cell().add(new Paragraph("Descripción")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBold());

        // Filas de la tabla
        for (Transaccion tx : transacciones) {
            txTable.addCell(tx.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            Cell tipoCell = new Cell().add(new Paragraph(tx.getTipo().toString()));
            if (tx.getTipo().toString().equalsIgnoreCase("RECARGA")) {
                tipoCell.setFontColor(ColorConstants.GREEN);
            } else {
                tipoCell.setFontColor(ColorConstants.RED);
            }
            txTable.addCell(tipoCell);
            
            txTable.addCell(new Paragraph(String.format("$%.2f", tx.getMonto())));
            txTable.addCell(tx.getDescripcion());
        }
        document.add(txTable);

        document.close();
        return baos.toByteArray();
    }
}
