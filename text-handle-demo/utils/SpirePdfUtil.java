package com.dover.pdf.util;

//import com.spire.pdf.PdfDocument;
//import com.spire.pdf.PdfPageBase;
//import com.spire.pdf.graphics.PdfBrushes;
//import com.spire.pdf.graphics.PdfStringFormat;
//import com.spire.pdf.graphics.PdfTextAlignment;
//import com.spire.pdf.graphics.PdfTilingBrush;
//import com.spire.pdf.graphics.PdfTrueTypeFont;


/**
 * @author dover
 * @since 2023/6/30
 */
public class SpirePdfUtil {



//    public static void spire() {
//        //create a PdfDocument instance
//        PdfDocument pdf = new PdfDocument();
//        //load the sample document
//        pdf.loadFromFile("D:\\Users\\80321000\\Desktop\\A.pdf");
//        //get the first page of the PDF
//        PdfPageBase page = pdf.getPages().get(0);
//        //use insertWatermark()to insert the watermark
//        insertWatermark(page, "aaaa");
//        //save the document to file
//        pdf.saveToFile("D:\\Users\\80321000\\Desktop\\B.pdf");
//    }
//
//    public static void insertWatermark(PdfPageBase page, String watermark) {
//        Dimension2D dimension2D = new Dimension();
//        dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 2,
//            page.getCanvas().getClientSize().getHeight() / 3);
//        PdfTilingBrush brush = new PdfTilingBrush(dimension2D);
//        brush.getGraphics().setTransparency(0.3F);
//        brush.getGraphics().save();
//        brush.getGraphics()
//            .translateTransform((float) brush.getSize().getWidth() / 2, (float) brush.getSize().getHeight() / 2);
//        brush.getGraphics().rotateTransform(-45);
////        final Font font = new Font("宋体", Font.PLAIN, 24);
//        brush.getGraphics()
//            .drawString(watermark, new PdfTrueTypeFont(FONT_CN, 24), PdfBrushes.getViolet(), 0, 0,
//                new PdfStringFormat(PdfTextAlignment.Center));
//        brush.getGraphics().restore();
//        brush.getGraphics().setTransparency(1);
//        Rectangle2D loRect = new Rectangle2D.Float();
//        loRect.setFrame(new Point2D.Float(0, 0), page.getCanvas().getClientSize());
//        page.getCanvas().drawRectangle(brush, loRect);
//    }
}
