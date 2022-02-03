package com.pramod.pdfmaster;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PDFGenerationUtils {
  private final String filePath;
  private final PDAcroForm acroForm;
  private PDPage page;
  public String TAG_REPORT_PAGE_COUNT_FORMAT = "{0} of {1}";
  private  int TITLE_FONT_SIZE = 20;
  private  int widgetDefaultFontSize = 12;
  private String REPORT_STORAGE_TEMP_IMAGE_FOLDER = "src/main/resources/temp";
  private int WIDGET_ICON_WIDTH = 13;
  private float MARGIN = 25;
  private PDDocument document;
  private int pageNo = -1;
  private Point currentPoint = new Point();
  private int pageWidth;
  private int leading = 10;

  /**
   * To set margin to pdf
   * TOP, BOTTOM, LEFT, RIGHT
   * @param MARGIN : margin
   */
  public void setMARGIN(float MARGIN) {
    this.MARGIN = MARGIN;
  }

  /**
   * To set leading
   * ie space between 2 lines
   * @param leading : leading
   */
  public void setLeading(int leading) {
    this.leading = leading;
  }

  /**
   * To store downloaded images in temp folder
   * @param REPORT_STORAGE_TEMP_IMAGE_FOLDER : folder path
   */
  public void setREPORT_STORAGE_TEMP_IMAGE_FOLDER(String REPORT_STORAGE_TEMP_IMAGE_FOLDER) {
    this.REPORT_STORAGE_TEMP_IMAGE_FOLDER = REPORT_STORAGE_TEMP_IMAGE_FOLDER;
  }


  public PDFGenerationUtils(String folderPath, String fileName){
    filePath = (folderPath.concat("/").concat(fileName));
    document = createEmptyPDF();

    PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    PDResources resources = new PDResources();
    resources.put(COSName.getPDFName("Helv"), font);

    // Add a new AcroForm and add that to the document
    acroForm = new PDAcroForm(document);
    document.getDocumentCatalog().setAcroForm(acroForm);

    // Add and set the resources and default appearance at the form level
    acroForm.setDefaultResources(resources);
    // Acrobat sets the font size on the form level to be
    // auto sized as default. This is done by setting the font size to '0'
    String defaultAppearanceString = "/Helv "+widgetDefaultFontSize+" Tf 0 g";
    acroForm.setDefaultAppearance(defaultAppearanceString);


    // Add a page to the document with proper size
    page = addNewPage(document);
    initDrawingPoints();
  }

  /**
   * To initialise starting point of drawing
   */
  private void initDrawingPoints() {
    PDRectangle rectangle = page.getMediaBox();
    pageWidth = (int) (rectangle.getWidth() - 2*MARGIN);
    currentPoint.x = (int) MARGIN;
    currentPoint.y = (int) (rectangle.getHeight() -  2*MARGIN);
  }

  /**
   * To create empty document
   * @return : document
   */
  private PDDocument createEmptyPDF() {
    // Creating PDF document object
    PDDocument document = new PDDocument();
    // Set document properties
//    setDocumentProperties(document);
    return document;
  }


  /**
   * TO add new page to document
   * @param document : document
   * @return : created page
   */
  private PDPage addNewPage(PDDocument document) {
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    pageNo++;
    return page;
  }

  /**
   * To get page width (drawing area width - 2*MARGIN)
   * @return : page width
   */
  public int getPageWidth() {
    return pageWidth;
  }

  /**
   * To insert image in pdf document page
   *
   * @param imageUrl  : image url
   * @param imgWidth  : image width
   * @param imgHeight : image height
   * @return
   */
  public int insertImage(String imageUrl,
                         int imgWidth, int imgHeight, Gravity gravity) {
    if (currentPoint.y <=  imgHeight + MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y - imgHeight;

    switch (gravity){
      case END -> currentPoint.x = (int) (pageWidth + MARGIN -  imgWidth);
      case START -> currentPoint.x = (int) MARGIN;
      case CENTER -> currentPoint.x = (int) ((pageWidth) - imgWidth) / 2;
    }

    x = currentPoint.x;

    int img_height=0;
    File file = null;
    try {
      if (imageUrl == null || imageUrl == "") {
        return img_height;
//                file = new File(REPORT_STORAGE_APP_IMAGE_PATH);
      } else if (imageUrl.contains("http")) {

        String filename = String.valueOf(imageUrl.hashCode());

        URL website = new URL(imageUrl);
        File folder = new File(REPORT_STORAGE_TEMP_IMAGE_FOLDER);
        if (!folder.exists())
          folder.mkdirs();
        file = new File(folder, filename+".jpg");

        if (file.exists()){
        }else {
          file.createNewFile();

          try {
            BufferedImage bufferedImage = ImageIO.read(website);
            ImageIO.write(bufferedImage, "jpg", file);

          } catch (IOException ex) {
            file.delete();
            return img_height;
          }
        }
      } else {
        file = new File(imageUrl);
      }

      // Get Content Stream for Writing Data
      PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

      // Creating PDImageXObject object
      //  PDImageXObject pdImage = PDImageXObject.createFromFile(file.getAbsolutePath(), document);
      BufferedImage bufferedImage = ImageIO.read(file);
      PDImageXObject pdImage = JPEGFactory.createFromImage(document,bufferedImage,0.2f);

      contentStream.drawImage(pdImage, x, y, imgWidth, imgHeight);

      // Closing the content stream
      contentStream.close();

    } catch (IOException e) {
      System.out.println(file.getAbsolutePath());
      e.printStackTrace();
    } finally {

    }

    currentPoint.y = (int) (y  - 4*leading);
    return img_height;
  }

  private int getHeightOfText(String text, float width, int fontSize) {

    PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    ArrayList<String> lines = new ArrayList<String>();
    int lastSpace = -1;
    while (text.length() > 0) {
      int spaceIndex = text.indexOf(' ', lastSpace + 1);
      if (spaceIndex < 0)
        spaceIndex = text.length();
      String subString = text.substring(0, spaceIndex);
      float size = 0;
      try {
        size = fontSize * font.getStringWidth(subString) / 1000;
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (size > width) {
        if (lastSpace < 0)
          lastSpace = spaceIndex;
        subString = text.substring(0, lastSpace);
        lines.add(subString);
        text = text.substring(lastSpace).trim();
        lastSpace = -1;
      } else if (spaceIndex == text.length()) {
        lines.add(text);
        text = "";
      } else {
        lastSpace = spaceIndex;
      }
    }
    float height = (font.getFontDescriptor().getCapHeight()) / 1000 * fontSize;
    return (int) (lines.size() * height);
  }

  /**
   * To add text in pdf page
   *  @param text     : text to add
   * @param fontSize : font size
   * @param isBold   : to to display bold text
   * @param color
   * @param gravity
   */
  public void addText(String text, int fontSize,
                      boolean isBold, PDColor color, Gravity gravity) {

    PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    if (isBold)
      font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    float textWidth = 0;
    try {
      textWidth = font.getStringWidth(text) /
        1000 * fontSize;
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (currentPoint.y <=  getHeightOfText(text, pageWidth, fontSize) + MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }

    float x = currentPoint.x;

    switch (gravity){
      case END -> currentPoint.x = (int) (pageWidth - MARGIN -  textWidth);
      case START -> currentPoint.x = (int) MARGIN;
      case CENTER -> currentPoint.x = (int) ((pageWidth) - textWidth) / 2;
    }

    x = currentPoint.x;
    float y = currentPoint.y;
    try {
      if (text == null || text.isEmpty())
        text = " ";
      PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
        true, false);

      float leading = 1.2f * fontSize;

      contentStream.setNonStrokingColor(color);

      ArrayList<String> lines = new ArrayList<String>();
      int lastSpace = -1;
      while (text.length() > 0) {
        int spaceIndex = text.indexOf(' ', lastSpace + 1);
        if (spaceIndex < 0)
          spaceIndex = text.length();
        String subString = text.substring(0, spaceIndex);
        float size = fontSize * font.getStringWidth(subString) / 1000;
        if (size > pageWidth) {
          if (lastSpace < 0)
            lastSpace = spaceIndex;
          subString = text.substring(0, lastSpace);
          lines.add(subString);
          text = text.substring(lastSpace).trim();
          lastSpace = -1;
        } else if (spaceIndex == text.length()) {
          lines.add(text);
          text = "";
        } else {
          lastSpace = spaceIndex;
        }
      }

      contentStream.beginText();
      contentStream.setFont(font, fontSize);
      contentStream.newLineAtOffset(x, y);
      for (String line : lines) {
        contentStream.showText(line);
        contentStream.newLineAtOffset(0, -leading);
        y = (int) (y - leading);
      }
      contentStream.endText();
      contentStream.close();

      currentPoint.y = (int) (y - this.leading);


    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * To add color text in pdf page
   *
   * @param fontSize : font size
   * @param isBold   : to to display bold text
   */
  public void addColorText(HashMap<String, PDColor> text, int fontSize,
                           boolean isBold, Gravity gravity) {

    AtomicReference<String> textToDisplay = new AtomicReference<>("");
    text.forEach((s, pdColor) -> {
      textToDisplay.set(textToDisplay.get().concat(s).concat(","));
      System.out.println("color text og: "+s);

    });

    if (currentPoint.y <=  getHeightOfText(textToDisplay.get(), pageWidth, fontSize) + MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }

    PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    if (isBold)
      font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    float textWidth = 0;
    try {
      textWidth = font.getStringWidth(textToDisplay.get()) /
        1000 * fontSize;
    } catch (IOException e) {
      e.printStackTrace();
    }

    float x = currentPoint.x;

    switch (gravity){
      case END -> currentPoint.x = (int) (pageWidth - MARGIN -  textWidth);
      case START -> currentPoint.x = (int) MARGIN;
      case CENTER -> currentPoint.x = (int) ((pageWidth) - textWidth) / 2;
    }

    x = currentPoint.x;
    float y = currentPoint.y;
    try {
      // Get Content Stream for Writing Data
      PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
        true);
      float leading = 1.2f * fontSize;

      ArrayList<String> lines = new ArrayList<String>();
      int lastSpace = -1;
      while (textToDisplay.get().length() > 0) {
        int spaceIndex = textToDisplay.get().indexOf(' ', lastSpace + 1);
        if (spaceIndex < 0)
          spaceIndex = textToDisplay.get().length();
        String subString = textToDisplay.get().substring(0, spaceIndex);
        float size = fontSize * font.getStringWidth(subString) / 1000;
        if (size > pageWidth) {
          if (lastSpace < 0)
            lastSpace = spaceIndex;
          subString = textToDisplay.get().substring(0, lastSpace);
          lines.add(subString);
          textToDisplay.set(textToDisplay.get().substring(lastSpace).trim());
          lastSpace = -1;
        } else if (spaceIndex == textToDisplay.get().length()) {
          lines.add(textToDisplay.get());
          textToDisplay.set("");
        } else {
          lastSpace = spaceIndex;
        }
      }

      contentStream.beginText();
      contentStream.setFont(font, fontSize);
      contentStream.newLineAtOffset(x, y);
      for (String line : lines) {
        contentStream.newLineAtOffset(0, -leading);
        String[] textToDraw = line.split(",");
        for (String s : textToDraw) {
          System.out.println("color text: "+s);
          contentStream.setNonStrokingColor(text.get(s));
          contentStream.showText(s.replaceAll(",",""));
        }
        y = (int) (y - leading);
      }
      contentStream.endText();
      contentStream.close();

      currentPoint.y = (int) (y - this.leading);


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * To draw line
   *
   */
  public void drawLines(int width, PDColor pdColor, Gravity gravity) {
    if (currentPoint.y <=  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y;
    switch (gravity){
      case END -> currentPoint.x = (int) (pageWidth + MARGIN -  width);
      case START -> currentPoint.x = (int) MARGIN;
      case CENTER -> currentPoint.x = (int) ((pageWidth) - width) / 2;
    }
    x = currentPoint.x;
    try {

      // Get Content Stream for Writing Data
      PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

      // Setting the non stroking color
      contentStream.setStrokingColor(pdColor);

      // lets make some lines
      contentStream.moveTo(x, y);
      contentStream.lineTo(x + width, y);
      contentStream.stroke();

      contentStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    currentPoint.x = currentPoint.x + width;
    currentPoint.y = (int) (y - 2*leading);

  }

  /**
   * TO add empty line in page
   */
  public void moveToNextLine(){
    currentPoint.x = (int) MARGIN;
    currentPoint.y = currentPoint.y - leading;
  }

  /**
   * To draw rectangle
   *
   * @param width :" width of rectangle
   * @param height : height of rectangle
   * @param color : rectangle fill color
   */
  public void drawRectangle(int width, int height,
                            PDColor color, Gravity gravity) {
    if (currentPoint.y <= height +  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y - height;

    switch (gravity){
      case END -> currentPoint.x = (int) (pageWidth + MARGIN -  width);
      case START -> currentPoint.x = (int) MARGIN;
      case CENTER -> currentPoint.x = (int) ((pageWidth) - width) / 2;
    }
    x = currentPoint.x;
    try {

      // Get Content Stream for Writing Data
      PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

      // Setting the non stroking color
      contentStream.setNonStrokingColor(color);

      // Drawing a rectangle
      contentStream.addRect(x, y, width, height);
      // Drawing a rectangle
      contentStream.fill();


      contentStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    currentPoint.y = (int) (y - leading);

  }


  /**
   * To draw rounded rectangle
   *
   * @param width : width of rectangle
   * @param height : height of rectangle
   * @param color : rectangle color
   */
  public void drawRoundedRectangle(int width, int height,
                                   int cornerRadius, boolean isToDrawOnlyTopRadius,
                                   PDColor color, Gravity gravity) {
    if (currentPoint.y <= height +  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y;
    switch (gravity){
      case END -> currentPoint.x = (int) (pageWidth + MARGIN -  width);
      case START -> currentPoint.x = (int) MARGIN;
      case CENTER -> currentPoint.x = (int) ((pageWidth) - width) / 2;
    }

    x = currentPoint.x;
    try {

      // Get Content Stream for Writing Data
      PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

      // Setting the non stroking color
      contentStream.setNonStrokingColor(color);

      RoundRect roundRect = new RoundRect(cornerRadius, isToDrawOnlyTopRadius);
      Position position = new Position(x, y);
      roundRect.add(contentStream, position, width, height);

      contentStream.fill();

      contentStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    currentPoint.y = (int) (y - height - 4* leading);

  }


  /**
   * To save pdf
   *

   * @throws IOException
   */
  public void savePdf() throws IOException {
    // Saving the document
    document.save(filePath);
    deleteTempFiles();
  }

  /**
   * Delete temp files
   */
  private void deleteTempFiles() {
    File file = new File(REPORT_STORAGE_TEMP_IMAGE_FOLDER);
    for (File listFile : file.listFiles()) {
      listFile.delete();
    }
  }

  /**
   * To convert rgb color to pdf color object
   * @param color : color
   * @return
   */
  public PDColor getColor(float[] color) {
    float[] components;
    if (color != null && color.length == 3) {
      components = new float[]{
        color[0] / 255f, color[1] / 255f, color[2] / 255f};
    } else {
      components = new float[]{
        0, 0, 0};
    }
    return new PDColor(components, PDDeviceRGB.INSTANCE);
  }


  /**
   * To add radio button
   * @param buttonList : hashmap of buttons
   * @param defaultValue : default values to set
   * @param pdColor : color of radio button text
   * @param partialName : name of button to identify in acroform
   */
  public void addRadioButtons(LinkedHashMap<String, String> buttonList,
                              String defaultValue,
                              PDColor pdColor, String partialName) {
    if (currentPoint.y <= 15 +  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float[] x = {currentPoint.x};
    float y = currentPoint.y;

    try {
      PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
      ArrayList<String> buttonValues = new ArrayList<String>();
      buttonList.forEach((s, s2) -> {
        buttonValues.add(s2);
      });

      PDRadioButton radioButton = new PDRadioButton(acroForm);
      radioButton.setPartialName(partialName);
      radioButton.setExportValues(buttonValues);

      PDAppearanceCharacteristicsDictionary appearanceCharacteristics = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
      appearanceCharacteristics.setBorderColour(new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE));
      appearanceCharacteristics.setBackground(new PDColor(new float[]{1, 1, 1}, PDDeviceRGB.INSTANCE));

      ArrayList<PDAnnotationWidget> widgets = new ArrayList<PDAnnotationWidget>();
      x[0] += 10;
      AtomicInteger index = new AtomicInteger();
      AtomicReference<String> oldValue = new AtomicReference<>("");
      buttonList.forEach((s, s2) -> {
        if (index.get() == 0)
          x[0] = x[0];
        else {
          try {
            float textWidth = font.getStringWidth(oldValue.get()) /
              1000 * widgetDefaultFontSize;
            x[0] = x[0] + textWidth + 6*leading;
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x[0], y, 15, 15));
        widget.setAppearanceCharacteristics(appearanceCharacteristics);
        PDBorderStyleDictionary borderStyleDictionary = new PDBorderStyleDictionary();
        borderStyleDictionary.setWidth(1);
        borderStyleDictionary.setStyle(PDBorderStyleDictionary.STYLE_SOLID);
        widget.setBorderStyle(borderStyleDictionary);
        widget.setPage(page);

        COSDictionary apNDict = new COSDictionary();
        try {
          apNDict.setItem(COSName.Off, createRadioButtonAppearanceStream(document, widget, false));
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          apNDict.setItem(s2, createRadioButtonAppearanceStream(document, widget, true));
        } catch (IOException e) {
          e.printStackTrace();
        }

        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        PDAppearanceEntry appearanceNEntry = new PDAppearanceEntry(apNDict);
        appearance.setNormalAppearance(appearanceNEntry);
        widget.setAppearance(appearance);
        widget.setAppearanceState("Off"); // don't forget this, or button will be invisible
        widgets.add(widget);
        try {
          page.getAnnotations().add(widget);
        } catch (IOException e) {
          e.printStackTrace();
        }

        PDPageContentStream contentStream = null;
        try {
          contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
            true, false);
          contentStream.beginText();
          contentStream.setFont(font, widgetDefaultFontSize);
          contentStream.newLineAtOffset(x[0] +25, y+2);
          contentStream.setNonStrokingColor(pdColor);
          contentStream.showText(s);
          contentStream.endText();
          contentStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

        index.getAndIncrement();
        oldValue.set(s);

      });

      radioButton.setWidgets(widgets);
      acroForm.getFields().add(radioButton);

      radioButton.setValue(defaultValue);


    } catch (IOException e) {
      e.printStackTrace();
    }

    currentPoint.y = (int) (y - 15 - 2*leading);

  }
  private PDAppearanceStream createRadioButtonAppearanceStream(
    final PDDocument document, PDAnnotationWidget widget, boolean on) throws IOException {

    PDRectangle rect = widget.getRectangle();
    PDAppearanceStream onAP = new PDAppearanceStream(document);
    onAP.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));
    PDPageContentStream onAPCS = new PDPageContentStream(document, onAP);

    PDAppearanceCharacteristicsDictionary appearanceCharacteristics = widget.getAppearanceCharacteristics();
    PDColor backgroundColor = appearanceCharacteristics.getBackground();
    PDColor borderColor = appearanceCharacteristics.getBorderColour();
    float lineWidth = getLineWidth(widget);
    onAPCS.setLineWidth(lineWidth); // border style (dash) ignored
    onAPCS.setNonStrokingColor(backgroundColor);
    float radius = Math.min(rect.getWidth() / 2, rect.getHeight() / 2);
    drawCircle(onAPCS, rect.getWidth() / 2, rect.getHeight() / 2, radius);
    onAPCS.fill();
    onAPCS.setStrokingColor(borderColor);
    drawCircle(onAPCS, rect.getWidth() / 2, rect.getHeight() / 2, radius - lineWidth / 2);
    onAPCS.stroke();
    if (on) {
      onAPCS.setNonStrokingColor(0f);
      drawCircle(onAPCS, rect.getWidth() / 2, rect.getHeight() / 2, (radius - lineWidth) / 2);
      onAPCS.fill();
    }

    onAPCS.close();
    return onAP;
  }

  private float getLineWidth(PDAnnotationWidget widget) {

    PDBorderStyleDictionary bs = widget.getBorderStyle();
    if (bs != null) {
      return bs.getWidth();
    }
    return 1;
  }

  private void drawCircle(PDPageContentStream cs, float x, float y, float r) throws IOException {

    float magic = r * 0.551784f;
    cs.moveTo(x, y + r);
    cs.curveTo(x + magic, y + r, x + r, y + magic, x + r, y);
    cs.curveTo(x + r, y - magic, x + magic, y - r, x, y - r);
    cs.curveTo(x - magic, y - r, x - r, y - magic, x - r, y);
    cs.curveTo(x - r, y + magic, x - magic, y + r, x, y + r);
    cs.closePath();

  }

  /**
   * To add check box
   * @param partialName
   * @param defaultValue
   * @param pdColor : color of radio button text
   * @param partialName : name of button to identify in acroform
   * @throws IOException
   */
  public void addCheckBox( String partialName, String defaultValue,
                   PDColor pdColor) throws IOException {

    if (currentPoint.y <= 15 +  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y;

    float yOffset = 0f;
    float yCurrent = y - yOffset;

    PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);


    COSDictionary cosDict = new COSDictionary();
    COSArray rect = new COSArray();
    rect.add(new COSFloat(x));
    rect.add(new COSFloat(yCurrent));
    rect.add(new COSFloat(x+20));
    rect.add(new COSFloat(yCurrent-20));

    cosDict.setItem(COSName.RECT, rect);
    cosDict.setItem(COSName.FT, COSName.CH); // Field Type
    cosDict.setItem(COSName.TYPE, COSName.ANNOT);
    cosDict.setItem(COSName.SUBTYPE, COSName.getPDFName("Widget"));
    cosDict.setItem(COSName.TU, new COSString("Test Checkbox with PDFBox"));
    cosDict.setItem(COSName.T, new COSString("testChk"));
    cosDict.setInt(COSName.F, 4);

    PDCheckBox checkbox = new PDCheckBox(acroForm);
    checkbox.setFieldFlags(4);
    checkbox.setPartialName(partialName);
//    checkbox.setValue("Off");

    COSDictionary normalAppearances = new COSDictionary();
    PDAppearanceDictionary pdAppearanceDictionary = new PDAppearanceDictionary();
    pdAppearanceDictionary.setNormalAppearance(new PDAppearanceEntry(normalAppearances));
    pdAppearanceDictionary.setDownAppearance(new PDAppearanceEntry(normalAppearances));


    PDAppearanceCharacteristicsDictionary appearanceCharacteristics = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
    appearanceCharacteristics.setBorderColour(new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE));
    appearanceCharacteristics.setBackground(new PDColor(new float[]{1, 1, 1}, PDDeviceRGB.INSTANCE));

    java.util.List<PDAnnotationWidget> widgets = checkbox.getWidgets();
    for (PDAnnotationWidget pdAnnotationWidget : widgets)
    {
      pdAnnotationWidget.setRectangle(new PDRectangle(x, yCurrent, 18, 18));
      pdAnnotationWidget.setPage(page);
      pdAnnotationWidget.setAppearanceCharacteristics(appearanceCharacteristics);
      page.getAnnotations().add(pdAnnotationWidget);

      pdAnnotationWidget.setAppearance(pdAppearanceDictionary);
    }

    PDPageContentStream contentStream = null;
    try {
      contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,
        true, false);
      contentStream.beginText();
      contentStream.setFont(font, widgetDefaultFontSize);
      contentStream.setNonStrokingColor(pdColor);
      contentStream.newLineAtOffset(x+25, yCurrent + 6);
      contentStream.showText(defaultValue);
      contentStream.endText();
      contentStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    acroForm.getFields().add(checkbox);

    currentPoint.y = (int) (y - 15 - 2*leading);

  }


  /**
   *
   * @param partialName : to identify in croform
   * @param defaultValue :
   * @param width
   * @param height
   * @throws IOException
   */
  public void addTextField(String partialName, String defaultValue,
                           int width, int height) throws IOException {
    System.out.println("TEXT FIELD partial name:"+partialName+" : "+defaultValue);
    PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    String defaultAppearance = "/Helv "+ widgetDefaultFontSize +" Tf 0 g";
    int padding = 4;

    currentPoint.y = currentPoint.y - 3*leading;
    if (currentPoint.y <= height +  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y;


    float yOffset = 15f;
    float yCurrent = y - yOffset - 2*leading;

    COSDictionary cosDict = new COSDictionary();
    COSArray rect = new COSArray();
    rect.add(new COSFloat(x));
    rect.add(new COSFloat(yCurrent));
    rect.add(new COSFloat(x+width));
    rect.add(new COSFloat(yCurrent-height));

    cosDict.setItem(COSName.RECT, rect);
    cosDict.setItem(COSName.FT, COSName.getPDFName(COSName.TX.getName())); // Field Type
    cosDict.setItem(COSName.TYPE, COSName.ANNOT);
    cosDict.setItem(COSName.SUBTYPE, COSName.getPDFName("Widget"));
    cosDict.setItem(COSName.DA, new COSString(defaultAppearance));

    PDTextField pdTextField = new PDTextField(acroForm);
    pdTextField.setFieldFlags(4);
    pdTextField.setDefaultAppearance(defaultAppearance);
    pdTextField.setPartialName(partialName);
    pdTextField.setDefaultValue(defaultValue);


    PDAppearanceCharacteristicsDictionary appearanceCharacteristics = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
    appearanceCharacteristics.setBorderColour(new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE));
    appearanceCharacteristics.setBackground(new PDColor(new float[]{1, 1, 1}, PDDeviceRGB.INSTANCE));

    java.util.List<PDAnnotationWidget> widgets = pdTextField.getWidgets();
    for (PDAnnotationWidget pdAnnotationWidget : widgets)
    {
      pdAnnotationWidget.setRectangle(new PDRectangle(x + padding, yCurrent - padding, width - padding*2,
        height - 2*padding));
      pdAnnotationWidget.setPage(page);
      pdAnnotationWidget.setAppearanceCharacteristics(appearanceCharacteristics);
//      pdAnnotationWidget.setAppearance(pdAppearanceDictionary);

      page.getAnnotations().add(pdAnnotationWidget);

    }
    acroForm.getFields().add(pdTextField);
    document.getDocumentCatalog().setAcroForm(acroForm);
    PDResources dr = new PDResources();
    dr.put(COSName.getPDFName("Helv"), font);
    acroForm.setDefaultResources(dr);

    currentPoint.y = (int) (yCurrent - 3*leading);

  }


  /**
   *
   * @param partialName
   * @param defaultValue
   * @param width
   * @param height
   * @param options
   * @throws IOException
   */
  public void addListField(String partialName, String defaultValue,
                           int width, int height,
                           java.util.List<String> options) throws IOException {

    String defaultAppearance = "/Helv "+widgetDefaultFontSize+" Tf 0 g";

    if (currentPoint.y <= height +  MARGIN) {
      page = addNewPage(document);
      initDrawingPoints();
    }
    float x = currentPoint.x;
    float y = currentPoint.y;

    float yOffset = 0;
    float yCurrent = y - yOffset;

    PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    PDComboBox comboBox = new PDComboBox(acroForm);
    comboBox.setPartialName(partialName);
    comboBox.setDefaultAppearance(defaultAppearance);

    PDAnnotationWidget widget = new PDAnnotationWidget();
    widget.setRectangle(new PDRectangle(x , yCurrent , width ,height ));
    widget.setAnnotationFlags(4);
    widget.setPage(page);
    widget.setParent(comboBox);

    comboBox.setOptions(options);
    if (!defaultValue.isEmpty())
      comboBox.setValue(defaultValue);

    PDAppearanceCharacteristicsDictionary appearanceCharacteristics = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
    appearanceCharacteristics.setBorderColour(new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE));
    appearanceCharacteristics.setBackground(new PDColor(new float[]{1, 1, 1}, PDDeviceRGB.INSTANCE));
    widget.setAppearanceCharacteristics(appearanceCharacteristics);

    List<PDAnnotationWidget> widgets = new ArrayList<>();
    widgets.add(widget);
    try {
      page.getAnnotations().add(widget);
    } catch (IOException e) {
      e.printStackTrace();
    }

    comboBox.setWidgets(widgets);

// new
    acroForm.getFields().add(comboBox);
    document.getDocumentCatalog().setAcroForm(acroForm);
    PDResources dr = new PDResources();
    dr.put(COSName.getPDFName("Helv"), font);
    acroForm.setDefaultResources(dr);

    currentPoint.y = (int) (yCurrent - 3*leading);


  }




}
