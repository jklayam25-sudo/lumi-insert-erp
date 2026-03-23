package lumi.insert.app.service.implement;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.time.LocalDateTime;
import java.util.List;

import org.openpdf.text.Document; 
import org.openpdf.text.Element;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory; 
import org.openpdf.text.Paragraph; 
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ExtendedColor;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter; 
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.repository.projection.ProductOutOfStock;
import lumi.insert.app.core.repository.projection.ProductRefund;
import lumi.insert.app.core.repository.projection.ProductSale;
import lumi.insert.app.dto.response.SupplyDetailResponse;
import lumi.insert.app.dto.response.SupplyItemResponse;
import lumi.insert.app.dto.response.TransactionDetailResponse; 
import lumi.insert.app.dto.response.TransactionItemResponse;
import lumi.insert.app.dto.response.TransactionItemStatisticResponse;
import lumi.insert.app.service.PdfService;
import lumi.insert.app.utils.generator.PdfCellBuilder;
import lumi.insert.app.utils.generator.PdfPageTemplate;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService{
 
    private Font helveticaBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

    @Override
    @ActivityLogger(
        entityName = "supplies",
        action = ActivityAction.EXPORT_DATA,
        actionMessage = "Suppy exported as PDF",
        entityIdFromSingleParam = true
    )
    public ByteArrayInputStream exportSupplyWithItems(SupplyDetailResponse data) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {     
                Document document = new Document(); 
                PdfWriter writer = PdfWriter.getInstance(document, out);
                writer.setPageEvent(new PdfPageTemplate());
                document.open(); 
        
                PdfPTable issuedFor = new PdfPTable(1);
                issuedFor.setWidthPercentage(100);  

                PdfPCell detailsCell = new PdfPCell();
                detailsCell.setBorder(Rectangle.NO_BORDER);
                detailsCell.setHorizontalAlignment(Element.ALIGN_LEFT);

                Paragraph companyName = new Paragraph("Supply Order", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                companyName.setAlignment(Element.ALIGN_LEFT);
                
                Paragraph address = new Paragraph("Invoice No: " + data.invoiceId() +"\n" +
                        "Supplier: " + data.supplierName() +"\n" +
                        "Total Items: " + data.totalItems() + "\n" +
                        "Issued At: " + data.createdAt() + "\n", 
                        FontFactory.getFont(FontFactory.HELVETICA, 10));
                address.setAlignment(Element.ALIGN_LEFT);

                detailsCell.addElement(companyName);
                detailsCell.addElement(address);
                issuedFor.addCell(detailsCell);
                issuedFor.setSpacingAfter(5f);
                document.add(issuedFor);

                PdfPTable itemsTable = new PdfPTable(4); 
                itemsTable.setWidthPercentage(100f);
                itemsTable.setWidths(new float[]{1f, 6f, 2f, 3f});  
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("No", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).padding(2f).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Product", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Quantity", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Price", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build()); 
                
                List<SupplyItemResponse> supplyItems = data.supplyItems();

                int i = 1;
                for (SupplyItemResponse item : supplyItems) {
                Color color = null;
                if(item.quantity() < 0) color = ExtendedColor.RED; 
                itemsTable.addCell(PdfCellBuilder.getUnborderedColouredCell(String.valueOf(i), color));
                itemsTable.addCell(PdfCellBuilder.getUnborderedColouredCell(item.product().name(), color));
                itemsTable.addCell(new PdfCellBuilder().paragraph(item.quantity().toString()).hAlign(Element.ALIGN_RIGHT).color(color).build());

                itemsTable.addCell(new PdfCellBuilder().paragraph(item.price().toString()).hAlign(Element.ALIGN_RIGHT).color(color).build()); 
                i++;
                }
                document.add(itemsTable);

                PdfContentByte cb = writer.getDirectContent();
                cb.setLineWidth(1.0f);
                cb.moveTo(document.leftMargin(), writer.getVerticalPosition(false) - 5);
                cb.lineTo(document.getPageSize().getWidth() - document.rightMargin(), writer.getVerticalPosition(false) - 5);
                cb.stroke();
                document.add(new Paragraph("\n")); 

                PdfPTable totalTable = new PdfPTable(4); 
                totalTable.setWidthPercentage(100f);
                totalTable.setWidths(new float[]{1f, 6f, 2f, 3f});  
                totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT); 

                Paragraph addInformation = new Paragraph("Additional Information:"); 
                PdfPCell infoCell = new PdfPCell(addInformation);
                infoCell.setColspan(2);
                infoCell.setBorder(0);
                infoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                totalTable.addCell(infoCell); 
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Subtotal", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.subTotal().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Add. Fee", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalFee().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Discount", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalDiscount().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Grandtotal", ExtendedColor.LIGHT_GRAY));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.grandTotal().toString()).color(ExtendedColor.LIGHT_GRAY).hAlign(Element.ALIGN_RIGHT).build());

                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));

                Paragraph adjustment = new Paragraph("Adjustment"); 
                PdfPCell adjustmentCell = new PdfPCell(adjustment);
                adjustmentCell.setColspan(2);
                adjustmentCell.setBorder(0);
                adjustmentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                totalTable.addCell(adjustmentCell); 

                long refundedValue = data.supplyItems().stream().filter(item -> item.quantity() < 0).mapToLong(item -> item.quantity() *  item.price()).sum();
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Refund value", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(String.valueOf(refundedValue)).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Unpaid", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalUnpaid().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Paid", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalPaid().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Un-refunded", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalUnrefunded().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Refunded", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalRefunded().toString()).hAlign(Element.ALIGN_RIGHT).build());

                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                Paragraph paragraph = new Paragraph("\n\n\n\n\n\n\nJekael\nWarehouse Staff" ); 
                PdfPCell issuer = new PdfPCell(paragraph);
                issuer.setColspan(4);
                issuer.setBorder(0);
                issuer.setHorizontalAlignment(Element.ALIGN_CENTER);

                totalTable.addCell(issuer); 
                document.add(totalTable); 

                
                document.close();

                return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
                throw new ExceptionConverter(e);
        }
    }

    @Override
    @ActivityLogger(
        entityName = "transactions",
        action = ActivityAction.EXPORT_DATA,
        actionMessage = "Transaction exported as PDF",
        entityIdFromSingleParam = true
    )
    public ByteArrayInputStream exportTransactionWithItems(TransactionDetailResponse data) {
        log.info("Creating Transaction PDF");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Document document = new Document(); 
                
                PdfWriter writer = PdfWriter.getInstance(document, out);
                writer.setPageEvent(new PdfPageTemplate());
                document.open(); 
        
                PdfPTable issuedFor = new PdfPTable(1);
                issuedFor.setWidthPercentage(100);  

                PdfPCell detailsCell = new PdfPCell();
                detailsCell.setBorder(Rectangle.NO_BORDER);
                detailsCell.setHorizontalAlignment(Element.ALIGN_LEFT);

                Paragraph companyName = new Paragraph("Transaction Order", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                companyName.setAlignment(Element.ALIGN_LEFT);
                
                Paragraph address = new Paragraph("Invoice No: " + data.invoiceId() +"\n" +
                        "Customer: " + data.customerName() +"\n" +
                        "Total Items: " + data.totalItems() + "\n" +
                        "Issued At: " + data.createdAt() + "\n", 
                        FontFactory.getFont(FontFactory.HELVETICA, 10));
                address.setAlignment(Element.ALIGN_LEFT);

                detailsCell.addElement(companyName);
                detailsCell.addElement(address);
                issuedFor.addCell(detailsCell);
                issuedFor.setSpacingAfter(5f);
                document.add(issuedFor);

                PdfPTable itemsTable = new PdfPTable(4); 
                itemsTable.setWidthPercentage(100f);
                itemsTable.setWidths(new float[]{1f, 6f, 2f, 3f});  
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("No", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).padding(2f).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Product", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Quantity", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Price", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build()); 
                
                List<TransactionItemResponse> items = data.transactionItems();

                int i = 1;
                for (TransactionItemResponse item : items) {
                        Color color = null;
                        if(item.quantity() < 0) color = ExtendedColor.RED; 
                        itemsTable.addCell(PdfCellBuilder.getUnborderedColouredCell(String.valueOf(i), color));
                        itemsTable.addCell(PdfCellBuilder.getUnborderedColouredCell(item.productName(), color));
                        itemsTable.addCell(new PdfCellBuilder().paragraph(item.quantity().toString()).hAlign(Element.ALIGN_RIGHT).color(color).build());

                        itemsTable.addCell(new PdfCellBuilder().paragraph(item.price().toString()).hAlign(Element.ALIGN_RIGHT).color(color).build()); 
                        i++;
                }
                document.add(itemsTable);

                PdfContentByte cb = writer.getDirectContent();
                cb.setLineWidth(1.0f);
                cb.moveTo(document.leftMargin(), writer.getVerticalPosition(false) - 5);
                cb.lineTo(document.getPageSize().getWidth() - document.rightMargin(), writer.getVerticalPosition(false) - 5);
                cb.stroke();
                document.add(new Paragraph("\n")); 

                PdfPTable totalTable = new PdfPTable(4); 
                totalTable.setWidthPercentage(100f);
                totalTable.setWidths(new float[]{1f, 6f, 2f, 3f});  
                totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT); 

                Paragraph addInformation = new Paragraph("Additional Information:"); 
                PdfPCell infoCell = new PdfPCell(addInformation);
                infoCell.setColspan(2);
                infoCell.setBorder(0);
                infoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                totalTable.addCell(infoCell); 
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Subtotal", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.subTotal().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Add. Fee", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalFee().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Discount", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalDiscount().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Grandtotal", ExtendedColor.LIGHT_GRAY));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.grandTotal().toString()).color(ExtendedColor.LIGHT_GRAY).hAlign(Element.ALIGN_RIGHT).build());

                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));

                Paragraph adjustment = new Paragraph("Adjustment"); 
                PdfPCell adjustmentCell = new PdfPCell(adjustment);
                adjustmentCell.setColspan(2);
                adjustmentCell.setBorder(0);
                adjustmentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                totalTable.addCell(adjustmentCell); 

                long refundedValue = data.transactionItems().stream().filter(item -> item.quantity() < 0).mapToLong(item -> item.quantity() *  item.price()).sum();
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Refund value", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(String.valueOf(refundedValue)).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Unpaid", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalUnpaid().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Paid", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalPaid().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Un-refunded", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalUnrefunded().toString()).hAlign(Element.ALIGN_RIGHT).build());
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("Refunded", null));
                totalTable.addCell(new PdfCellBuilder().paragraph(data.totalRefunded().toString()).hAlign(Element.ALIGN_RIGHT).build());

                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                totalTable.addCell(PdfCellBuilder.getUnborderedColouredCell("", null));
                Paragraph paragraph = new Paragraph("\n\n\n\n\n\n\nJekael\nCashier Staff" ); 
                PdfPCell issuer = new PdfPCell(paragraph);
                issuer.setColspan(4);
                issuer.setBorder(0);
                issuer.setHorizontalAlignment(Element.ALIGN_CENTER);

                totalTable.addCell(issuer); 
                document.add(totalTable); 

                
                document.close();
                log.info("Done created Transaction PDF");
                return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
                throw new ExceptionConverter(e);
        }
    }

    @Override
    @ActivityLogger(
        entityName = "products",
        action = ActivityAction.EXPORT_DATA,
        actionMessage = "Product statistics exported as PDF",
        entityIdFromSingleParam = true
    )
    public ByteArrayInputStream exportProductsStatistic(TransactionItemStatisticResponse statistic,
            List<ProductOutOfStock> oosProducts, LocalDateTime minTime, LocalDateTime maxTime) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                List<ProductSale> sales = statistic.getProductSales();
                List<ProductRefund> refunds = statistic.getProductRefunds();
                Document document = new Document(); 
                
                PdfWriter writer = PdfWriter.getInstance(document, out);
                writer.setPageEvent(new PdfPageTemplate());
                document.open(); 
        
                PdfPTable issuedFor = new PdfPTable(1);
                issuedFor.setWidthPercentage(100);  

                PdfPCell detailsCell = new PdfPCell();
                detailsCell.setBorder(Rectangle.NO_BORDER);
                detailsCell.setHorizontalAlignment(Element.ALIGN_LEFT);

                Paragraph subHeader = new Paragraph("Products statistic", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                subHeader.setAlignment(Element.ALIGN_LEFT);
                
                Paragraph information = new Paragraph(
                        "Request date: " + minTime.toLocalDate() + " - " + maxTime.toLocalDate() +"\n" +
                        "Ttl. Unique product sold: " + sales.size() + "\n" +
                        "Ttl. Unique product refund: " + refunds.size() + "\n" +
                        "Product quantity below minimum: " + oosProducts.size() + "\n", 
                        FontFactory.getFont(FontFactory.HELVETICA, 10));
                information.setAlignment(Element.ALIGN_LEFT);

                detailsCell.addElement(subHeader);
                detailsCell.addElement(information);
                issuedFor.addCell(detailsCell);
                issuedFor.setSpacingAfter(5f);
                document.add(issuedFor);

                Paragraph firstSub = new Paragraph("Best selling products: ", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                firstSub.setAlignment(Element.ALIGN_CENTER);

                PdfPTable itemsTable = new PdfPTable(3); 
                itemsTable.setWidthPercentage(100f);
                itemsTable.setWidths(new float[]{1f, 6f, 3f});  
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("No", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).padding(2f).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Product", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).build());
                itemsTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Total Sold (NET)", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build()); 
                
                int i = 1;
                for (ProductSale product : sales) {  
                        itemsTable.addCell(PdfCellBuilder.getUnborderedColouredCell(String.valueOf(i), null));
                        itemsTable.addCell(PdfCellBuilder.getUnborderedColouredCell(product.productName(), null));
                        itemsTable.addCell(new PdfCellBuilder().paragraph(product.totalSold().toString() + "Pcs" ).hAlign(Element.ALIGN_RIGHT).build());
        
                        i++;
                }
                document.add(firstSub);
                document.add(itemsTable);

                PdfCellBuilder.setHorizontalLine(document, writer);

                Paragraph secondsSub = new Paragraph("Most refunded products: ", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                secondsSub.setAlignment(Element.ALIGN_CENTER);

                PdfPTable refundTable = new PdfPTable(3); 
                refundTable.setWidthPercentage(100f);
                refundTable.setWidths(new float[]{1f, 6f, 3f});  
                refundTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("No", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).padding(2f).build());
                refundTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Product", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).build());
                refundTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Total Refunded", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build()); 
                
                int ii = 1;
                for (ProductRefund product : refunds) {  
                        refundTable.addCell(PdfCellBuilder.getUnborderedColouredCell(String.valueOf(ii), null));
                        refundTable.addCell(PdfCellBuilder.getUnborderedColouredCell(product.productName(), null));
                        refundTable.addCell(new PdfCellBuilder().paragraph(Math.abs(product.totalRefunded()) + "Pcs" ).hAlign(Element.ALIGN_RIGHT).build());
        
                ii++;
                }

                document.add(secondsSub);
                document.add(refundTable);

                PdfCellBuilder.setHorizontalLine(document, writer);

                Paragraph thirdSub = new Paragraph("Out of stock products: ", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                thirdSub.setAlignment(Element.ALIGN_CENTER);

                PdfPTable oosTable = new PdfPTable(4); 
                oosTable.setWidthPercentage(100f);
                oosTable.setWidths(new float[]{1f, 6f, 3f, 3f});  
                oosTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("No", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).padding(2f).build());
                oosTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Product", helveticaBold)).color(ExtendedColor.LIGHT_GRAY).vAlign(Element.ALIGN_CENTER).build());
                oosTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Stock left", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build()); 
                oosTable.addCell(new PdfCellBuilder().paragraph(new Paragraph("Minimum quantity", helveticaBold)).hAlign(Element.ALIGN_RIGHT).vAlign(Element.ALIGN_CENTER).color(ExtendedColor.LIGHT_GRAY).build()); 
                
                int iii = 1;
                for (ProductOutOfStock product : oosProducts) {  
                        oosTable.addCell(PdfCellBuilder.getUnborderedColouredCell(String.valueOf(iii), null));
                        oosTable.addCell(PdfCellBuilder.getUnborderedColouredCell(product.name(), null));
                        oosTable.addCell(new PdfCellBuilder().paragraph(product.stockQuantity().toString() + "Pcs" ).hAlign(Element.ALIGN_RIGHT).build());
                        oosTable.addCell(new PdfCellBuilder().paragraph(product.stockMinimum().toString() + "Pcs" ).hAlign(Element.ALIGN_RIGHT).build());

                        iii++;
                }

                document.add(thirdSub);
                document.add(oosTable);

                PdfContentByte cb = writer.getDirectContent();
                cb.setLineWidth(1.0f);
                cb.moveTo(document.leftMargin(), writer.getVerticalPosition(false) - 5);
                cb.lineTo(document.getPageSize().getWidth() - document.rightMargin(), writer.getVerticalPosition(false) - 5);
                cb.stroke();
                document.add(new Paragraph("\n"));  

                document.close();

                return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
                throw new ExceptionConverter(e);
        }
    }

}
