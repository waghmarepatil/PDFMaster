package com.pramod.pdfmaster;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Example {
  public static void main(String[] args) throws IOException {
    File folder = new File("src/main/resources/reports/");
    if (!folder.exists())
      folder.mkdirs();


    float[] colorBlack = new float[]{
      0, 0, 0
    };

    float[] colorBlue = new float[]{
      53, 121, 246};

    PDFGenerationUtils pdfGenerationUtils = new PDFGenerationUtils(folder.getAbsolutePath(), "test.pdf");
    pdfGenerationUtils.setMARGIN(25f);
    for (int i = 0; i <5; i++) {
      pdfGenerationUtils.addText("Hello "+i, 25, true,
        pdfGenerationUtils.getColor(colorBlack), Gravity.START);
    }

//    pdfGenerationUtils.insertImage("https://images.news18.com/ibnlive/uploads/2021/07/1627377451_nature-1600x900.jpg",
//      400, 200, Gravity.CENTER);
    pdfGenerationUtils.addText("Hello 50", 25, true,
      pdfGenerationUtils.getColor(colorBlack), Gravity.CENTER);
    pdfGenerationUtils.addText("Hello 51", 25, true,
      pdfGenerationUtils.getColor(colorBlack), Gravity.END);

    pdfGenerationUtils.drawLines(50, pdfGenerationUtils.getColor(colorBlack), Gravity.CENTER);

    pdfGenerationUtils.moveToNextLine();

    pdfGenerationUtils.addText("Hello 52", 25, true,
      pdfGenerationUtils.getColor(colorBlack), Gravity.START);

    pdfGenerationUtils.drawRectangle(100,50, pdfGenerationUtils.getColor(colorBlack), Gravity.END);

    pdfGenerationUtils.drawRoundedRectangle(200,100, 8, false,
      pdfGenerationUtils.getColor(colorBlack), Gravity.CENTER);

    pdfGenerationUtils.addText("Hello 53", 25, true,
      pdfGenerationUtils.getColor(colorBlack), Gravity.START);



    LinkedHashMap<String, String> list = new LinkedHashMap<>();
    list.put("Yes", "yes");
    list.put("No", "no");
    list.put("May be", "may be");
    list.put("May be1", "may be1");

    pdfGenerationUtils.addRadioButtons(list, "yes", pdfGenerationUtils.getColor(colorBlue), "rb1");

    pdfGenerationUtils.addText("Hello 53", 25, true,
      pdfGenerationUtils.getColor(colorBlack), Gravity.START);


    pdfGenerationUtils.addCheckBox("cb1","Is valid", pdfGenerationUtils.getColor(colorBlue));

    pdfGenerationUtils.addText("Hello 53", 25, true,
      pdfGenerationUtils.getColor(colorBlack), Gravity.START);


    pdfGenerationUtils.addTextField("tx1","Is valid",
      400, 100);

    pdfGenerationUtils.addText("Hello 58888", 25, true,
      pdfGenerationUtils.getColor(colorBlue), Gravity.START);

    ArrayList<String> optionList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      optionList.add("Option "+i);
    }
    pdfGenerationUtils.addListField("tx1","", 500, 20,
      optionList);

    pdfGenerationUtils.addText("Hello 9999999", 25, true,
      pdfGenerationUtils.getColor(colorBlue), Gravity.START);


    LinkedHashMap<String, PDColor> colorHashMap = new LinkedHashMap<>();
    for (int i = 0; i < 10; i++) {
        colorHashMap.put("Sample "+i+"  ", i%2 ==0? pdfGenerationUtils.getColor(colorBlack) :
        pdfGenerationUtils.getColor(colorBlue));
    }
    pdfGenerationUtils.addColorText(colorHashMap, 10, false, Gravity.START);

    pdfGenerationUtils.savePdf();
  }
}
