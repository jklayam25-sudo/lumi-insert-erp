package lumi.insert.app.utils.generator;

import java.awt.Color;

import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Utilities of Pdf's related.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public class PdfCellBuilder {
    
        PdfPCell pdfPCell;

        PdfCellBuilder(PdfPCell pdfPCell){
            this.pdfPCell = pdfPCell;
        }

        public PdfCellBuilder(){
            this.pdfPCell = new PdfPCell();
            this.pdfPCell.setBorder(0);
        }

        public PdfCellBuilder paragraph(Paragraph value){ 
            pdfPCell.setPhrase(value); 
            return new PdfCellBuilder(pdfPCell);
        } 

        public PdfCellBuilder paragraph(String value){ 
            pdfPCell.setPhrase(new Paragraph(value)); 
            return new PdfCellBuilder(pdfPCell);
        } 

        public PdfCellBuilder color(Color colour){
            pdfPCell.setBackgroundColor(colour);
            return new PdfCellBuilder(pdfPCell);
        } 

        public PdfCellBuilder hAlign(int align){
            pdfPCell.setHorizontalAlignment(align);
            return new PdfCellBuilder(pdfPCell);
        }

        public PdfCellBuilder vAlign(int align){
            pdfPCell.setVerticalAlignment(align);
            return new PdfCellBuilder(pdfPCell);
        }

        public PdfCellBuilder alignCenter(){
            pdfPCell.setVerticalAlignment(1);
            pdfPCell.setHorizontalAlignment(1);
            return new PdfCellBuilder(pdfPCell);
        }

        public PdfCellBuilder padding(float value){
            pdfPCell.setPadding(value);
            return new PdfCellBuilder(pdfPCell);
        }
 
        public PdfPCell build(){ 
            return this.pdfPCell;
        } 

        public static PdfPCell getUnborderedColouredCell(Object phrase, Color color){
            PdfPCell pdfPCell = new PdfPCell(new Paragraph(phrase.toString()));
            pdfPCell.setBorder(0); 
            if(color != null) pdfPCell.setBackgroundColor(color);
            return pdfPCell;
        }

        public static void setHorizontalLine(Document document, PdfWriter writer){
             PdfContentByte cbx = writer.getDirectContent();
            cbx.setLineWidth(1.0f);
            cbx.moveTo(document.leftMargin(), writer.getVerticalPosition(false) - 5);
            cbx.lineTo(document.getPageSize().getWidth() - document.rightMargin(), writer.getVerticalPosition(false) - 5);
            cbx.stroke();
            document.add(new Paragraph("\n"));  
        };
 
    
}
