package com.tiemens.tictactoe.generate;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class DrawBoardImage extends JPanel {

    public static class DrawCellModel {
        private Rectangle rectangle;
        private String marker; 
        private Color markerColor;
        private int border;
      
        public DrawCellModel(int x, int y, int widthheight, String marker, Color markerColor, int border) {
            this.rectangle = new Rectangle(x, y, widthheight, widthheight);
            this.marker = marker;
            this.markerColor = markerColor;            
            this.border = border;

        }
        
        public void paintComponent(Graphics g) {
            // troubleshooting only:
            //g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            
            Color prev = g.getColor();
            g.setColor(markerColor);
            drawCenteredString(g, marker, rectangle, g.getFont(), border);
            g.setColor(prev);
            
            // troubleshooting:
            if (false) {
                g.drawLine(rectangle.x,  rectangle.y,  rectangle.x + rectangle.width, rectangle.y+rectangle.height);
                g.drawLine(rectangle.x+rectangle.width,  rectangle.y,  rectangle.x, rectangle.y+rectangle.height);
            }
        }
    }
    
    public static class DrawBoardModel {
        private List<DrawCellModel> cells = new ArrayList<>();
        private Dimension prefSize;

        public DrawBoardModel(int x, int y, int heightwidth, String string) {
            int pad = 8;
            heightwidth = heightwidth - pad;
            int row = 0;
            int col = 0;
            for (String marker : string.split("")) {
                marker = getNormalizedMarker(marker);
                Color markerColor = getColorForMarker(marker);
                
                int curx = (x + pad) + heightwidth * col;
                int cury = (y + pad) + heightwidth * row;
                cells.add(new DrawCellModel(curx, cury, heightwidth, marker, markerColor, 8));
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }
            prefSize = new Dimension(3 * heightwidth + pad + pad,
                                     3 * heightwidth + pad + pad);
        }

        private static String getNormalizedMarker(String in) {
            String ret = "";
            if ((in == null) || (in.length() != 1)) {
                ret = "-";
            } else if (in.equalsIgnoreCase("x")) {
                ret = "X";
            } else if (in.equalsIgnoreCase("o")) {
                ret = "O";
            } else {
                ret = "";
            }
            return ret;
        }
        private static Color getColorForMarker(String marker) {
            Color ret = Color.BLACK;
            if ((marker != null) && (marker.length() > 0)) {
                if (marker.equalsIgnoreCase("x")) {
                    ret = Color.BLUE;
                } else if (marker.equalsIgnoreCase("o")) {
                    ret = Color.RED;
                }
            }
            return ret;
        }

        public void paintComponent(Graphics g, Dimension dimension) {
            for (DrawCellModel dcm : cells) {
                dcm.paintComponent(g);
            }
            int border = 0;

            DrawCellModel left = cells.get(3);
            DrawCellModel right = cells.get(5);
            drawHorizLine(g, left, right, border);
            left = cells.get(6);
            right = cells.get(8);
            drawHorizLine(g, left, right, border);
            
            DrawCellModel top = cells.get(1);
            DrawCellModel bottom = cells.get(7);
            drawVertLine(g, top, bottom, border);
            top = cells.get(2);
            bottom = cells.get(8);
            drawVertLine(g, top,bottom, border);
    
            // troubleshoot size:
            if (false ) {
                Color prev = g.getColor();
                g.setColor(Color.RED);
                g.drawRect(0, 0, dimension.width - 1, dimension.height - 1);
                g.setColor(prev);
            }
        }
        private void drawVertLine(Graphics g, DrawCellModel top, DrawCellModel bottom, int border) {
            if (top.rectangle.x != bottom.rectangle.x) {
                throw new RuntimeException("Vertical line needs x to agree");
            }
            g.drawLine(top.rectangle.x, top.rectangle.y + border,  
                       bottom.rectangle.x, bottom.rectangle.y + bottom.rectangle.height - border);
        }

        public void drawHorizLine(Graphics g, DrawCellModel left, DrawCellModel right, int border) {
            if (left.rectangle.y != right.rectangle.y) {
                throw new RuntimeException("Horizontal line needs y to agree");
            }
            g.drawLine(left.rectangle.x + border, left.rectangle.y,  
                       right.rectangle.x + right.rectangle.width - border,  right.rectangle.y);
        }

        public Dimension getPreferredSize() {
            System.out.println("PrefSize=" + prefSize);
            return prefSize;
        }


        
    }
    
    
    DrawBoardModel dbm;
    Font useFont;
    public DrawBoardImage(String input, int height) {
        if (input == null) {
            input = "XOXOXOXOX";
        } else {
            input = input.toUpperCase();
        }
        this.dbm = new DrawBoardModel(0, 0, height, input);
        // for height 40:
        this.useFont = new Font("Courier", Font.PLAIN, 24);
    }
    
    
  

    @Transient
    public Dimension getPreferredSize() {
        return dbm.getPreferredSize();
    }
    
  public void paintComponent(Graphics g) {
      
      g.setFont(this.useFont);
      
      
      
//      g.setFont(new Font("TimesRoman", Font.PLAIN, 44));
      //String str = "X O";  // 
      //g.drawString(str, 20, 40);
      //g.drawString("40.80", 40, 80);
      //g.drawOval(20, 40, 5, 5);
      
      //g.drawString("40.120", 40, 120);
      
      
      //DrawCellModel dcm = new DrawCellModel(40, 120, 40, "X");
      //dcm.paintComponent(g);
     // dcm = new DrawCellModel(40 + 40, 120, 40, "O");
      //dcm.paintComponent(g);

      System.out.println("getSize=" + getSize());
      dbm.paintComponent(g, getSize());

  }
  public void saveImage(File file) {
      saveImageUtil(file, this.getSize().width, this.getSize().height, this);
  }
  
  
  public static void saveImageUtil(File file, int width, int height, Component component) {
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics g = bi.createGraphics();
      component.paint(g);
      g.dispose();
      try {
          ImageIO.write(bi,"png", file);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
  
  /**
   * Draw a String centered in the middle of a Rectangle.
   *
   * @param g The Graphics instance.
   * @param text The String to draw.
   * @param rect The Rectangle to center the text in.
   */
  public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font, int border) {
      // 
      int adjustX = 0;
      int adjustY = 1;
      // Get the FontMetrics
      FontMetrics metrics = g.getFontMetrics(font);
      // Determine the X coordinate for the text
      int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2 + adjustX;
      // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
      int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent() + adjustY;
      // Set the font
      g.setFont(font);
      // Draw the String
      g.drawString(text, x, y);
  }
  
  
  public static int SIZE_SMALL = 40;    // ends up 112
  public static int SIZE_KAGGLE = 100;   // needs 284, so divide by 3 = 90    564x282  564=188
 
  public static void main(String[] args) {
    //JFrame.setDefaultLookAndFeelDecorated(true);
    //JFrame frame = new JFrame("Draw Text");
    //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //frame.setBackground(Color.white);
    
 
    DrawBoardImage panel = new DrawBoardImage(null, SIZE_SMALL);
    panel.setSize(panel.getPreferredSize());
    //frame.setSize(300, 200);
    //frame.setSize(panel.getPreferredSize());
    //frame.setSize(new Dimension(130, 130));
    
    //frame.add(panel);
    //frame.pack();
 
    //frame.setVisible(true);
    
    panel.saveImage(new File("build/DrawBoardImage.png"));
    
    List<String> interesting = List.of("ooxxxooxx", "oxoxoxxox", "oxxxooxox");
    int size;
    String sizeExtraName;
    
    size = SIZE_SMALL;
    sizeExtraName = "";
    size = SIZE_KAGGLE;
    sizeExtraName = "-kaggle";
    
    for (String input : interesting) {
        DrawBoardImage dbi = new DrawBoardImage(input, size);
        dbi.setSize(dbi.getPreferredSize());
        dbi.saveImage(new File("build/" + input + sizeExtraName + ".png"));
    }
  }
}
 