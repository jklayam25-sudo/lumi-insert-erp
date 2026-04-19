package lumi.insert.app.utils.generator;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Utilities of Pdf's related.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public class PdfPageTemplate extends PdfPageEventHelper{
    
    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            document.setPageSize(PageSize.A4);
            document.setMargins(72f, 72f, 0f, 30f);

            document.addAuthor("LUMI Insert"); 

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1.5f, 4.0f}); 
            headerTable.setSpacingAfter(5f);  
    
            // Image logo = Image.getInstance("path/to/logo.png");
            // logo.scaleToFit(80, 80); 
            
            PdfPCell logoCell = new PdfPCell(new Phrase("LOGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);
    
            PdfPCell detailsCell = new PdfPCell();
            detailsCell.setBorder(Rectangle.NO_BORDER);
            detailsCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph companyName = new Paragraph("Lumi Insert Inc.", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            companyName.setAlignment(Element.ALIGN_RIGHT);
            
            Paragraph address = new Paragraph("Jl. Lumi No. 123, Kota Bandung\n" +
                    "Email: support@erp-anda.com | Telp: (0778) 123456\n" +
                    "Website: www.erp-anda.com", 
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            address.setAlignment(Element.ALIGN_RIGHT);

            detailsCell.addElement(companyName);
            detailsCell.addElement(address);
            headerTable.addCell(detailsCell);
    
            document.add(headerTable);
    
            PdfContentByte cb = writer.getDirectContent();
            cb.setLineWidth(1.0f);
            cb.moveTo(document.leftMargin(), writer.getVerticalPosition(false) - 5);
            cb.lineTo(document.getPageSize().getWidth() - document.rightMargin(), writer.getVerticalPosition(false) - 5);
            cb.stroke();
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
        
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable footer = new PdfPTable(2); 
        try {
            footer.setWidths(new int[]{3, 1});
            footer.setTotalWidth(527); 
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(20);
            footer.getDefaultCell().setBorder(Rectangle.TOP);  
 
            footer.addCell(new Phrase("Generated Document by LUMI Insert, contact internal for further question.", 
                           FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC)));
 
            PdfPCell pageNum = new PdfPCell(new Phrase(String.format("Page: %d", writer.getPageNumber()), FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD)));
            pageNum.setBorder(Rectangle.TOP);
            pageNum.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(pageNum);
 
            footer.writeSelectedRows(0, -1, 36, 30, writer.getDirectContent());
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
}
